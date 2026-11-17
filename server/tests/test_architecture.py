"""
Server architecture checks: startup, API contract, input validation,
database schema vs models, token masking in logs.
Database operations (VaultRepository) are tested separately
"""

import base64
import logging
import uuid

from alembic.autogenerate import compare_metadata
from alembic.migration import MigrationContext
from sqlalchemy import inspect

from app.config import MAX_BLOB_BYTES_LIMIT, get_settings
from app.database import Base, engine
from app.logging_config import TokenMaskingFilter, mask_tokens

TOKEN = "3f2b8c1e-9a4d-4e7f-8b6a-1c2d3e4f5a6b"


# Startup and health

def test_health_reports_database_ok(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok", "database": "ok"}


# API contract

def test_openapi_describes_all_vault_endpoints(client):
    paths = client.get("/openapi.json").json()["paths"]

    assert set(paths) == {"/health", "/api/vaults", "/api/vaults/{token}"}
    assert set(paths["/api/vaults"]) == {"post"}
    assert set(paths["/api/vaults/{token}"]) == {"get", "put", "delete"}

    put_responses = paths["/api/vaults/{token}"]["put"]["responses"]
    assert {"200", "404", "409", "413", "422"} <= set(put_responses)


def test_docs_page_is_served(client):
    assert client.get("/docs").status_code == 200


# Request validation (before any database access)

def test_invalid_token_format_is_rejected(client):
    assert client.get("/api/vaults/not-a-uuid").status_code == 422


def test_invalid_base64_is_rejected(client):
    response = client.put(f"/api/vaults/{TOKEN}", json={"blob": "not base64!", "base_version": 0})
    assert response.status_code == 422


def test_negative_base_version_is_rejected(client):
    blob = base64.b64encode(b"KTLN").decode()
    response = client.put(f"/api/vaults/{TOKEN}", json={"blob": blob, "base_version": -1})
    assert response.status_code == 422


def test_oversized_blob_is_rejected_with_413(client):
    blob = base64.b64encode(b"\0" * (get_settings().max_blob_bytes + 1)).decode()
    response = client.put(f"/api/vaults/{TOKEN}", json={"blob": blob, "base_version": 0})

    assert response.status_code == 413
    assert response.json()["code"] == "blob_too_large"


# Database

def test_database_schema_matches_models():
    """Same as `alembic check`: migrations are applied and match the models"""
    with engine.connect() as connection:
        diff = compare_metadata(MigrationContext.configure(connection), Base.metadata)
    assert diff == []


def test_vault_records_table_has_expected_columns_and_constraints():
    inspector = inspect(engine)
    columns = {c["name"] for c in inspector.get_columns("vault_records")}
    assert columns == {"token", "encrypted_blob", "version", "blob_size", "created_at", "updated_at"}

    checks = {c["name"] for c in inspector.get_check_constraints("vault_records")}
    assert checks == {"ck_vault_records_blob_size_limit", "ck_vault_records_version_non_negative"}
    assert get_settings().max_blob_bytes <= MAX_BLOB_BYTES_LIMIT


# Log safety

def test_tokens_are_masked_in_logs():
    assert mask_tokens(f"GET /api/vaults/{TOKEN} HTTP/1.1") == "GET /api/vaults/*** HTTP/1.1"


def test_access_log_record_is_masked():
    record = logging.LogRecord(
        "uvicorn.access", logging.INFO, __file__, 1,
        '%s - "%s %s HTTP/%s" %d', ("127.0.0.1:5000", "PUT", f"/api/vaults/{uuid.uuid4()}", "1.1", 200), None,
    )
    TokenMaskingFilter().filter(record)
    assert record.getMessage() == '127.0.0.1:5000 - "PUT /api/vaults/*** HTTP/1.1" 200'
