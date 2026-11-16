from fastapi import FastAPI

from app.config import get_settings

settings = get_settings()

app = FastAPI(title=settings.app_name, version="1.0.0")


@app.get("/")
def read_root():
    return {"app": settings.app_name, "status": "running"}
