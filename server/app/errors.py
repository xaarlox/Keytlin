"""Domain errors and their mapping to HTTP responses with a common {detail, code} format"""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse


class VaultNotFoundError(Exception):
    """No vault with this token exists (or it was deleted)"""


class VersionConflictError(Exception):
    """base_version is outdated: another device has already written a newer version"""

    def __init__(self, current_version: int) -> None:
        super().__init__(f"Current version is {current_version}")
        self.current_version = current_version


class BlobTooLargeError(Exception):
    """The vault size exceeds the allowed limit"""

    def __init__(self, size: int, limit: int) -> None:
        super().__init__(f"Blob size {size} exceeds limit {limit}")
        self.size = size
        self.limit = limit


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(VaultNotFoundError)
    async def _not_found(_: Request, __: VaultNotFoundError) -> JSONResponse:
        return JSONResponse(status_code=404, content={"detail": "Vault not found", "code": "vault_not_found"})

    @app.exception_handler(VersionConflictError)
    async def _conflict(_: Request, exc: VersionConflictError) -> JSONResponse:
        return JSONResponse(
            status_code=409,
            content={
                "detail": "Vault was updated by another device",
                "code": "version_conflict",
                "current_version": exc.current_version,
            },
        )

    @app.exception_handler(BlobTooLargeError)
    async def _too_large(_: Request, exc: BlobTooLargeError) -> JSONResponse:
        return JSONResponse(
            status_code=413,
            content={"detail": f"Vault exceeds {exc.limit} bytes", "code": "blob_too_large"},
        )

    @app.exception_handler(NotImplementedError)
    async def _not_implemented(_: Request, __: NotImplementedError) -> JSONResponse:
        return JSONResponse(status_code=501, content={"detail": "Not implemented yet", "code": "not_implemented"})
