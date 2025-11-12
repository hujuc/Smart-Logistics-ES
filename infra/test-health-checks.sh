#!/bin/bash

###############################################################################
# Health Check Testing Script for SLMS
#
# This script tests all health check endpoints defined in the observability
# implementation following ES_08_Monitoring_Observability course guidelines.
#
# Usage:
#   chmod +x test-health-checks.sh
#   ./test-health-checks.sh
#
# Requirements:
#   - Docker containers must be running (docker-compose up)
#   - curl and jq must be installed
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test results
TESTS_PASSED=0
TESTS_FAILED=0

echo "================================================"
echo "SLMS Health Check Testing Script"
echo "================================================"
echo ""

###############################################################################
# Helper Functions
###############################################################################

test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="${3:-200}"

    echo -n "Testing $name... "

    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null || echo "ERROR\n000")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        if command -v jq &> /dev/null && [ -n "$body" ]; then
            echo "$body" | jq '.' 2>/dev/null | sed 's/^/  /' || echo "  $body"
        else
            echo "  $body"
        fi
        ((TESTS_PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected HTTP $expected_status, got $http_code)"
        echo "  Response: $body"
        ((TESTS_FAILED++))
        return 1
    fi
}

test_docker_health() {
    local container="$1"
    echo -n "Testing Docker health: $container... "

    health_status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "not_found")

    case "$health_status" in
        "healthy")
            echo -e "${GREEN}✓ PASSED${NC} (healthy)"
            ((TESTS_PASSED++))
            return 0
            ;;
        "unhealthy")
            echo -e "${RED}✗ FAILED${NC} (unhealthy)"
            ((TESTS_FAILED++))
            return 1
            ;;
        "starting")
            echo -e "${YELLOW}⚠ STARTING${NC} (still initializing)"
            ((TESTS_FAILED++))
            return 1
            ;;
        "not_found")
            echo -e "${RED}✗ FAILED${NC} (container not found)"
            ((TESTS_FAILED++))
            return 1
            ;;
        "")
            echo -e "${YELLOW}⚠ NO HEALTHCHECK${NC} (health check not configured)"
            ((TESTS_PASSED++))
            return 0
            ;;
        *)
            echo -e "${RED}✗ FAILED${NC} (unknown status: $health_status)"
            ((TESTS_FAILED++))
            return 1
            ;;
    esac
}

###############################################################################
# Docker Container Health Checks
###############################################################################

echo "===== Docker Container Health Checks ====="
echo ""

test_docker_health "slms-postgres"
test_docker_health "slms-keycloak-dev"
test_docker_health "minio"
test_docker_health "slms-backend-dev"
test_docker_health "slms-admin-console-dev"
test_docker_health "slms-customer-portal-dev"
test_docker_health "slms-driver-pwa-dev"
test_docker_health "slms-landing-page-dev"
test_docker_health "slms-nginx-dev"

echo ""

###############################################################################
# HTTP Health Check Endpoints
###############################################################################

echo "===== HTTP Health Check Endpoints ====="
echo ""

# Nginx health endpoint
test_endpoint "Nginx /health" "http://localhost/health"

# Backend health endpoint
test_endpoint "Backend /actuator/health" "http://localhost/actuator/health"

# Frontend health endpoints (via nginx)
test_endpoint "Admin Console /api/health" "http://localhost/admin/api/health"
test_endpoint "Customer Portal /api/health" "http://localhost/customer/api/health"
test_endpoint "Driver PWA /api/health" "http://localhost/driver/api/health"
test_endpoint "Landing Page /api/health" "http://localhost/api/health"

echo ""

###############################################################################
# Backend Actuator Endpoints
###############################################################################

echo "===== Backend Actuator Endpoints ====="
echo ""

test_endpoint "Actuator Info" "http://localhost/actuator/info"
test_endpoint "Actuator Metrics" "http://localhost/actuator/metrics"
test_endpoint "Actuator Prometheus" "http://localhost/actuator/prometheus"

echo ""

###############################################################################
# Infrastructure Services
###############################################################################

echo "===== Infrastructure Services ====="
echo ""

# PostgreSQL via pg_isready
echo -n "Testing PostgreSQL connection... "
if docker exec slms-postgres pg_isready -U postgres -d slms_db &>/dev/null; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((TESTS_FAILED++))
fi

# MinIO health endpoint
test_endpoint "MinIO /minio/health/live" "http://localhost:9000/minio/health/live"

# Keycloak health endpoint
echo -n "Testing Keycloak health... "
if docker exec slms-keycloak-dev curl -sf http://localhost:8080/health/ready &>/dev/null; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((TESTS_PASSED++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((TESTS_FAILED++))
fi

echo ""

###############################################################################
# Summary
###############################################################################

echo "================================================"
echo "Test Summary"
echo "================================================"
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All health checks passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}Some health checks failed! ✗${NC}"
    echo ""
    echo "Troubleshooting tips:"
    echo "1. Check if all containers are running: docker ps"
    echo "2. View container logs: docker logs <container-name>"
    echo "3. Check health check history: docker inspect <container-name> | jq '.[0].State.Health'"
    echo "4. Verify services are listening on expected ports"
    exit 1
fi
