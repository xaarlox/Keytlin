"""Server health check: used by the Docker healthcheck and for diagnostics"""

from fastapi import APIRouter
from fastapi.responses import JSONResponse
from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError

from app.dependencies import SessionDep
from app.schemas import HealthResponse

router = APIRouter(tags=["health"])


@router.get(
    "/health",
    response_model=HealthResponse,
    summary="Server status and database connectivity",
    responses={503: {"model": HealthResponse, "description": "Database is unavailable"}},
)
def health(session: SessionDep) -> HealthResponse | JSONResponse:
    try:
        session.execute(text("SELECT 1"))
    except SQLAlchemyError:
        return JSONResponse(status_code=503, content={"status": "degraded", "database": "unavailable"})
    return HealthResponse(status="ok", database="ok")