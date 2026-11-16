"""
Alembic configuration

- The database URL is loaded from the application settings (KEYTLIN_DATABASE_URL) rather than
  alembic.ini to maintain a single source of truth.
- target_metadata — model metadata: `alembic revision --autogenerate` uses it to compare
  the models against the actual database and generate a migration
"""

from logging.config import fileConfig

from sqlalchemy import engine_from_config, pool

from alembic import context
from app import models  # noqa: F401 — import registers tables in Base.metadata
from app.config import get_settings
from app.database import Base

config = context.config
config.set_main_option("sqlalchemy.url", get_settings().database_url.replace("%", "%%"))

if config.config_file_name is not None:
    fileConfig(config.config_file_name)

target_metadata = Base.metadata


def run_migrations_offline() -> None:
    """Generates an SQL script without a database connection: `alembic upgrade head --sql`"""
    context.configure(
        url=config.get_main_option("sqlalchemy.url"),
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
        compare_type=True,
    )
    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """Applies migrations to the database: `alembic upgrade head`"""
    connectable = engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )
    with connectable.connect() as connection:
        context.configure(connection=connection, target_metadata=target_metadata, compare_type=True)
        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
