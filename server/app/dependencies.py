"""FastAPI dependencies (DI): what each request handler receives"""

from typing import Annotated

from fastapi import Depends
from sqlalchemy.orm import Session

from app.config import Settings, get_settings
from app.database import get_session
from app.repository import VaultRepository


def get_vault_repository(session: Annotated[Session, Depends(get_session)]) -> VaultRepository:
    return VaultRepository(session)


SessionDep = Annotated[Session, Depends(get_session)]
SettingsDep = Annotated[Settings, Depends(get_settings)]
VaultRepositoryDep = Annotated[VaultRepository, Depends(get_vault_repository)]
