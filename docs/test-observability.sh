#!/bin/bash

# SLMS Observability Test Script
# This script generates traffic to test telemetry export to Elastic APM

echo "========================================="
echo "SLMS Observability Test Script"
echo "========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="http://localhost"
ACTUATOR_URL="http://localhost/actuator"

# Step 1: Check if backend is healthy
echo -e "${YELLOW}[1/5] Checking backend health...${NC}"
if curl -sf "$ACTUATOR_URL/health" > /dev/null; then
    echo -e "${GREEN}✓ Backend is healthy${NC}"
else
    echo -e "${RED}✗ Backend is not responding. Please start docker-compose first.${NC}"
    exit 1
fi

# Step 2: Check Prometheus metrics endpoint
echo -e "\n${YELLOW}[2/5] Checking Prometheus metrics endpoint...${NC}"
METRIC_COUNT=$(curl -s "$ACTUATOR_URL/prometheus" | grep -c "^http_server_requests")
if [ "$METRIC_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Prometheus endpoint is exposing $METRIC_COUNT HTTP metrics${NC}"
else
    echo -e "${RED}✗ Prometheus endpoint not found or no metrics${NC}"
fi

# Step 3: Generate sample traffic
echo -e "\n${YELLOW}[3/5] Generating sample traffic...${NC}"
echo "  - Making 50 requests to various endpoints..."

for i in {1..50}; do
    # Mix of different endpoints to create variety
    curl -s -o /dev/null "$BACKEND_URL/actuator/health" &
    curl -s -o /dev/null "$BACKEND_URL/actuator/metrics" &
    curl -s -o /dev/null "$BACKEND_URL/actuator/prometheus" &

    # Show progress every 10 requests
    if [ $((i % 10)) -eq 0 ]; then
        echo "    Progress: $i/50 requests sent"
    fi
done

wait
echo -e "${GREEN}✓ Generated 150 requests${NC}"

# Step 4: Check for trace context in logs
echo -e "\n${YELLOW}[4/5] Checking backend logs for trace correlation...${NC}"
if docker logs slms-backend-dev 2>&1 | tail -20 | grep -q "trace_id"; then
    echo -e "${GREEN}✓ Trace IDs found in logs (trace correlation working)${NC}"
    echo "  Sample log with trace:"
    docker logs slms-backend-dev 2>&1 | grep "trace_id" | tail -1 | cut -c1-120
else
    echo -e "${YELLOW}⚠ No trace_id found in recent logs (may take a moment)${NC}"
fi

# Step 5: Check OTel configuration
echo -e "\n${YELLOW}[5/5] Verifying OpenTelemetry configuration...${NC}"
if docker logs slms-backend-dev 2>&1 | grep -qi "opentelemetry"; then
    echo -e "${GREEN}✓ OpenTelemetry agent is active${NC}"
else
    echo -e "${YELLOW}⚠ No OpenTelemetry startup logs found${NC}"
fi

# Summary and next steps
echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Test Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "Next steps to verify in Elastic Cloud:"
echo ""
echo "1. Open Kibana: https://cloud.elastic.co/"
echo "2. Go to: Menu → Observability → APM"
echo "3. You should see a service named: 'SLMS'"
echo "4. Click on it to view:"
echo "   - Transactions (API requests)"
echo "   - Errors"
echo "   - Metrics (latency, throughput)"
echo "   - Service Map"
echo ""
echo "5. Go to: Menu → Observability → Logs"
echo "   - Filter by: service.name: SLMS"
echo "   - You should see JSON structured logs with trace_id"
echo ""
echo "6. Click on a log entry, then click 'View in APM'"
echo "   - This shows log-to-trace correlation"
echo ""
echo -e "${YELLOW}Note: It may take 10-30 seconds for data to appear in Elastic${NC}"
