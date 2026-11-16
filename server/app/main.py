from fastapi import FastAPI

from app.config import get_settings
from app.routes import health

settings = get_settings()

app = FastAPI(title=settings.app_name, version="1.0.0")
app.include_router(health.router)
