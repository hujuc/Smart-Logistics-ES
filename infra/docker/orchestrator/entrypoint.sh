#!/bin/bash
set -e

# Wait for backend to be ready
echo "Waiting for backend to be ready at $API_BASE_URL..."
until python3 -c "import requests; base='$API_BASE_URL'.replace('/api', ''); requests.get(f'{base}/actuator/health', timeout=5).raise_for_status()" > /dev/null 2>&1; do
  echo "Backend not ready yet. Sleeping..."
  sleep 5
done
echo "Backend is ready!"

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready at http://slms-keycloak-prod:8080..."
until python3 -c "import requests; requests.get('http://slms-keycloak-prod:8080', timeout=5, allow_redirects=False)" > /dev/null 2>&1; do
  echo "Keycloak not ready yet. Sleeping..."
  sleep 5
done
echo "Keycloak is ready!"

# Run seed script (it will skip if data exists)
echo "Running seed script..."
python3 -u seed_demo_data.py --api-base "$API_BASE_URL"

# Run orchestrator (default: 600s = 10 minutes)
echo "Starting orchestrator..."
python3 -u orchestrate_orders.py --api-base "$API_BASE_URL" --interval "${ORCHESTRATOR_INTERVAL:-600}"
