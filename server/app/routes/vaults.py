"""
Vault REST API

Routes only parse the HTTP request, validate it and build the response.
Database work lives in VaultRepository; errors → HTTP codes in errors.py
"""

import uuid

from fastapi import APIRouter, Response, status

from app.dependencies import SettingsDep, VaultRepositoryDep
from app.errors import BlobTooLargeError
from app.schemas import (
    ErrorResponse,
    VaultCreatedResponse,
    VaultResponse,
    VaultUpdatedResponse,
    VaultUpdateRequest,
    VersionConflictResponse,
)

router = APIRouter(prefix="/api/vaults", tags=["vaults"])

NOT_FOUND = {404: {"model": ErrorResponse, "description": "No vault with this token"}}


@router.post(
    "",
    status_code=status.HTTP_201_CREATED,
    response_model=VaultCreatedResponse,
    summary="Register a new vault",
    description="Implicit registration on the first sync: the server creates an empty record "
                "and returns a token — the sync code. The request body is empty.",
)
def create_vault(repository: VaultRepositoryDep) -> VaultCreatedResponse:
    record = repository.create()
    return VaultCreatedResponse(token=record.token, version=record.version)


@router.get(
    "/{token}",
    response_model=VaultResponse,
    summary="Get the encrypted vault",
    description="Used by a second device when restoring by sync code, and for merging after a 409.",
    responses=NOT_FOUND,
)
def get_vault(token: uuid.UUID, repository: VaultRepositoryDep) -> VaultResponse:
    return VaultResponse.from_record(repository.get(token))


@router.put(
    "/{token}",
    response_model=VaultUpdatedResponse,
    summary="Upload a new vault version",
    description="Optimistic locking: the write succeeds only if `base_version` equals the current "
                "server version. Otherwise 409 — the client must download the server version, merge the entries "
                "and retry.",
    responses={
        **NOT_FOUND,
        409: {"model": VersionConflictResponse, "description": "Vault was changed by another device"},
        413: {"model": ErrorResponse, "description": "Vault is too large"},
    },
)
def update_vault(
        token: uuid.UUID,
        body: VaultUpdateRequest,
        repository: VaultRepositoryDep,
        settings: SettingsDep,
) -> VaultUpdatedResponse:
    if len(body.blob) > settings.max_blob_bytes:
        raise BlobTooLargeError(size=len(body.blob), limit=settings.max_blob_bytes)
    record = repository.update(token, body.blob, body.base_version)
    return VaultUpdatedResponse(version=record.version, updated_at=record.updated_at)


@router.delete(
    "/{token}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete the vault from the server",
    description="Local copies on devices are not affected.",
    responses=NOT_FOUND,
)
def delete_vault(token: uuid.UUID, repository: VaultRepositoryDep) -> Response:
    repository.delete(token)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
