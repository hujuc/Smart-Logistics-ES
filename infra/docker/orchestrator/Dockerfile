FROM python:3.11-slim

WORKDIR /app

# Install dependencies
RUN pip install requests

# Copy scripts
COPY infra/scripts/seed_demo_data.py .
COPY infra/scripts/orchestrate_orders.py .
COPY infra/docker/orchestrator/entrypoint.sh .

# Make entrypoint executable
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
