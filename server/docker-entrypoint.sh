#!/bin/sh
# Bring the database schema up to date before starting the server
set -e

alembic upgrade head

# --proxy-headers: correct client IPs when nginx or a load balancer sits in front
exec uvicorn app.main:app --host 0.0.0.0 --port 8000 --proxy-headers