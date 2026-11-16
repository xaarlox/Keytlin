from fastapi import FastAPI

from app.config import get_settings
from app.errors import register_exception_handlers
from app.routes import health, vaults
from app.logging_config import install_token_masking

DESCRIPTION = """
Zero-knowledge sync server for the **Keytlin** password manager.

The server stores only encrypted vaults (blobs) and their version number.
Encryption (Argon2id + AES-256-GCM) and entry merging happen on the device;
the server has no key and cannot see the contents.

Access to a vault is by token (UUID), issued by the server on registration.
"""


def create_app() -> FastAPI:
    settings = get_settings()
    install_token_masking()

    app = FastAPI(
        title=settings.app_name,
        version="1.0.0",
        description=DESCRIPTION,
        docs_url="/docs" if settings.enable_docs else None,
        redoc_url=None,
        openapi_url="/openapi.json" if settings.enable_docs else None,
    )
    app.include_router(health.router)
    app.include_router(vaults.router)
    register_exception_handlers(app)
    return app


app = create_app()
