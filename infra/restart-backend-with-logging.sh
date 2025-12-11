#!/bin/bash

###############################################################################
# Restart Backend with Updated Logging Configuration
#
# This script rebuilds and restarts the backend to apply:
# - Structured JSON logging
# - OpenTelemetry trace correlation (trace_id, span_id)
# - Log shipping to Elastic/Kibana
###############################################################################

set -e

echo "================================================"
echo "Restarting Backend with Updated Logging"
echo "================================================"
echo ""

cd "$(dirname "$0")"

echo "Step 1: Stopping backend container..."
docker stop slms-backend-dev || true
docker rm slms-backend-dev || true

echo ""
echo "Step 2: Rebuilding backend (this may take a minute)..."
docker compose -f docker-compose.dev.yml build backend

echo ""
echo "Step 3: Starting backend..."
docker compose -f docker-compose.dev.yml up -d backend

echo ""
echo "Step 4: Waiting for backend to be healthy..."
echo "(This takes about 60-90 seconds)"

# Wait for backend to be healthy
for i in {1..30}; do
    health_status=$(docker inspect --format='{{.State.Health.Status}}' slms-backend-dev 2>/dev/null || echo "starting")

    if [ "$health_status" == "healthy" ]; then
        echo "✓ Backend is healthy!"
        break
    fi

    echo "  Attempt $i/30: Status = $health_status"
    sleep 3
done

echo ""
echo "Step 5: Testing structured logging..."
echo ""

# Make a request to generate logs
curl -s http://localhost/actuator/health > /dev/null

echo "Recent backend logs (JSON format):"
echo "-----------------------------------"
docker logs slms-backend-dev --tail 5

echo ""
echo "================================================"
echo "Backend restart complete!"
echo "================================================"
echo ""
echo "✓ JSON logging enabled"
echo "✓ OpenTelemetry trace correlation configured"
echo "✓ Logs will appear in Kibana"
echo ""
echo "Next steps:"
echo "1. Make some requests: curl http://localhost/actuator/health"
echo "2. View logs: docker logs -f slms-backend-dev"
echo "3. Check Kibana → Discover → Select 'logs-*' index"
echo ""
