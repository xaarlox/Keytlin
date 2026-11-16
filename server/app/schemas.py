"""
API request and response schemas (Pydantic). They also define the contract shown in /docs.

The blob is sent in JSON as a standard Base64 string (with "=" padding)
"""

import base64
import binascii
import uuid
from datetime import datetime
from typing import Annotated

from pydantic import BaseModel, BeforeValidator, ConfigDict, Field, PlainSerializer

from app.models import VaultRecord


def _decode_base64(value: object) -> bytes:
    if isinstance(value, bytes):
        return value
    if not isinstance(value, str):
        raise ValueError("blob must be a Base64 string")
    try:
        return base64.b64decode(value, validate=True)
    except (binascii.Error, ValueError) as exc:
        raise ValueError("blob is not valid Base64") from exc


# bytes in Python, Base64 string in JSON (both directions)
Base64Blob = Annotated[
    bytes,
    BeforeValidator(_decode_base64),
    PlainSerializer(lambda data: base64.b64encode(data).decode("ascii"), return_type=str),
    Field(json_schema_extra={"type": "string", "format": "byte"}),
]


class HealthResponse(BaseModel):
    status: str = Field(examples=["ok"])
    database: str = Field(examples=["ok"])


class ErrorResponse(BaseModel):
    detail: str = Field(examples=["Vault not found"])
    code: str = Field(examples=["vault_not_found"])


class VersionConflictResponse(ErrorResponse):
    current_version: int = Field(description="Current server version; the client must merge from it.")


class VaultCreatedResponse(BaseModel):
    token: uuid.UUID = Field(description="Sync code. Stored on the device as a secret.")
    version: int = Field(examples=[0])


class VaultResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    token: uuid.UUID
    blob: Base64Blob | None = Field(
        default=None,
        validation_alias="encrypted_blob",
        description="Encrypted vault, or null if it has not been uploaded yet.",
    )
    version: int
    updated_at: datetime

    @classmethod
    def from_record(cls, record: VaultRecord) -> "VaultResponse":
        return cls.model_validate(record)


class VaultUpdateRequest(BaseModel):
    blob: Base64Blob = Field(description="New encrypted vault in KTLN format, Base64-encoded.")
    base_version: int = Field(
        ge=0,
        description="Version the local copy is based on. If the server has a newer one: 409 Conflict.",
    )


class VaultUpdatedResponse(BaseModel):
    version: int = Field(description="New version after the update (base_version + 1).")
    updated_at: datetime
