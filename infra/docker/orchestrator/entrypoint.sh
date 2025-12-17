#!/bin/bash
set -e

# Default to production backend if API_BASE_URL not set
: ${API_BASE_URL:=http://slms-backend-prod:8080/api}

# Wait for backend to be ready
echo "Waiting for backend to be ready at $API_BASE_URL..."
until python3 -c "import requests; requests.get('$API_BASE_URL/actuator/health', timeout=5).raise_for_status()" > /dev/null 2>&1; do
  echo "Backend not ready yet. Sleeping..."
  sleep 5
done
echo "Backend is ready!"

# Run seed script (it will skip if data exists)
echo "Running seed script..."
python3 seed_demo_data.py --api-base "$API_BASE_URL"

# Run orchestrator
echo "Starting orchestrator..."
python3 orchestrate_orders.py --api-base "$API_BASE_URL" --interval 18000
