"""Server settings from environment variables (KEYTLIN_ prefix) or a .env file"""

from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

MAX_BLOB_BYTES_LIMIT = 2 * 1024 * 1024  # Maximum allowed encrypted storage size (2 MB)


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="KEYTLIN_", env_file=".env", extra="ignore")

    app_name: str = "Keytlin Sync API"
    database_url: str  # required: set via KEYTLIN_DATABASE_URL or the .env file
    max_blob_bytes: int = Field(default=MAX_BLOB_BYTES_LIMIT, gt=0, le=MAX_BLOB_BYTES_LIMIT)
    enable_docs: bool = True


@lru_cache
def get_settings() -> Settings:
    return Settings()
