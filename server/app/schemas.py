"""API request and response schemas (Pydantic). They also define the contract in the /docs documentation"""

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = Field(examples=["ok"])
    database: str = Field(examples=["ok"])
