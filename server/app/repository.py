"""
Data access layer: all SQL operations on the vault_records table

Routes never use SQLAlchemy directly — only through this class.
This keeps HTTP logic and database logic separate and independently testable
"""

import uuid

from sqlalchemy.orm import Session

from app.models import VaultRecord


class VaultRepository:
    def __init__(self, session: Session) -> None:
        self._session = session

    def create(self) -> VaultRecord:
        """
        Creates an empty record (blob = NULL, version = 0) with a new random token

        Implementation: INSERT INTO vault_records DEFAULT VALUES RETURNING ...
        """
        raise NotImplementedError

    def get(self, token: uuid.UUID) -> VaultRecord:
        """
        Returns the record for the token

        :raises VaultNotFoundError: if there is no such record
        """
        raise NotImplementedError

    def update(self, token: uuid.UUID, blob: bytes, base_version: int) -> VaultRecord:
        """
        Atomically writes a new vault version (optimistic locking):

            UPDATE vault_records
            SET encrypted_blob = :blob, blob_size = :size,
                version = version + 1, updated_at = now()
            WHERE token = :token AND version = :base_version
            RETURNING version, updated_at;

        :raises VaultNotFoundError: if there is no such record.
        :raises VersionConflictError: if the server version != base_version
        """
        raise NotImplementedError

    def delete(self, token: uuid.UUID) -> None:
        """
        Deletes the record together with its blob

        :raises VaultNotFoundError: if there is no such record
        """
        raise NotImplementedError
