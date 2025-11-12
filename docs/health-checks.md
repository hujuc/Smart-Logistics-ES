# SLMS Health Check Implementation

## Overview

This document describes the health check implementation for all SLMS services, meeting the observability requirements for container-level readiness and liveness probes.

## Acceptance Criteria Compliance

✅ **Health endpoints implemented**: `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`
✅ **Container-level probes**: Implemented in Docker Compose with proper intervals and timeouts
✅ **HTTP 200 OK responses**: All endpoints verified returning 200 status
✅ **Dependency details**: Health checks include database, disk space, and SSL status

---

## Backend Service (Spring Boot)

### Health Endpoints

#### Main Health Endpoint
**URL**: `http://localhost/actuator/health`
**Status**: HTTP 200 OK

**Response**:
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"],
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 246124347392,
        "free": 9617248256,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "livenessState": {"status": "UP"},
    "ping": {"status": "UP"},
    "readinessState": {"status": "UP"},
    "ssl": {
      "status": "UP",
      "details": {
        "validChains": [],
        "invalidChains": []
      }
    }
  }
}
```

#### Liveness Probe
**URL**: `http://localhost/actuator/health/liveness`
**Status**: HTTP 200 OK
**Purpose**: Indicates if the application is running (used by Kubernetes/Docker to restart unhealthy containers)

**Response**:
```json
{"status": "UP"}
```

#### Readiness Probe
**URL**: `http://localhost/actuator/health/readiness`
**Status**: HTTP 200 OK
**Purpose**: Indicates if the application can accept traffic (checks dependencies)

**Response**:
```json
{"status": "UP"}
```

### Docker Health Check Configuration

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 90s
```

**Container Status**: `Up 39 minutes (healthy)`

---

## PostgreSQL Database

### Health Check Configuration

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U $POSTGRES_USER -d $POSTGRES_DB"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 10s
```

**Container Status**: `Up 39 minutes (healthy)`

**Check Method**: Uses `pg_isready` to verify PostgreSQL is accepting connections

---

## Keycloak (Authentication Service)

### Health Check Configuration

```yaml
healthcheck:
  test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 && exit 0 || exit 1"]
  interval: 15s
  timeout: 5s
  retries: 10
  start_period: 90s
```

**Container Status**: `Up 39 minutes (healthy)`

**Check Method**: TCP socket test to verify service is listening on port 8080

---

## MinIO (Object Storage)

### Health Check Configuration

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:9000/minio/health/live || exit 1"]
  interval: 15s
  timeout: 10s
  retries: 3
  start_period: 20s
```

**Container Status**: `Up 39 minutes (healthy)`

**Check Method**: HTTP GET to MinIO's built-in health endpoint

---

## Frontend Services

### Status

The following frontend services are running without explicit health checks (not critical as they're stateless Next.js applications):

- **slms-admin-console-dev**: `Up 39 minutes`
- **slms-customer-portal-dev**: `Up 39 minutes`
- **slms-driver-pwa-dev**: `Up 39 minutes`
- **slms-landing-page-dev**: `Up 39 minutes`

**Note**: Frontend health checks can be added in the future by implementing custom `/health` endpoints in Next.js if needed.

---

## Nginx (Reverse Proxy)

### Status

**slms-nginx-dev**: `Up 2 minutes`

### Configuration

Nginx is configured to route health check requests to the backend:

```nginx
# ACTUATOR - Spring Boot Actuator endpoints (health, metrics, prometheus)
location /actuator/ { proxy_pass http://backend:8080; }
```

This allows external health checks via:
- `http://localhost/actuator/health`
- `http://localhost/actuator/health/liveness`
- `http://localhost/actuator/health/readiness`

---

## Testing Health Checks

### Automated Test Script

Run the observability test script to verify all health checks:

```bash
bash docs/test-observability.sh
```

**Expected Output**:
```
✓ Backend is healthy
✓ Prometheus endpoint is exposing 104 HTTP metrics
✓ Generated 150 requests
✓ Trace IDs found in logs (trace correlation working)
✓ OpenTelemetry agent is active
```

### Manual Testing

#### Check All Container Health Status
```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

#### Test Backend Health Endpoint
```bash
curl -s http://localhost/actuator/health | jq '.'
```

#### Verify HTTP Status Codes
```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost/actuator/health
# Expected: 200
```

#### Check Liveness and Readiness
```bash
curl -s http://localhost/actuator/health/liveness
curl -s http://localhost/actuator/health/readiness
```

---

## Health Check Integration with Observability

### Spring Boot Actuator Configuration

File: `src/backend/src/main/resources/application.properties`

```properties
# Spring Boot Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus,env
management.endpoint.health.show-details=always
management.endpoint.health.probes.enabled=true
```

### Health Metrics in Elastic APM

Health check requests are automatically tracked in Elastic APM:
- **Transaction Type**: `GET /actuator/health`
- **Average Latency**: 5.7ms
- **Throughput**: 1.8 tpm
- **Error Rate**: 0%

These metrics contribute to the SLO for API availability.

---

## Troubleshooting

### Container Shows Unhealthy

1. Check container logs:
```bash
docker logs <container-name>
```

2. Inspect health check details:
```bash
docker inspect <container-name> --format='{{json .State.Health}}' | jq '.'
```

3. Manually run the health check command:
```bash
docker exec <container-name> <health-check-command>
```

### Backend Health Check Fails

Check dependency status:
```bash
curl -s http://localhost/actuator/health | jq '.components'
```

If a component shows "DOWN", investigate that specific service (database, disk space, etc.)

---

## Production Recommendations

1. **Adjust health check intervals**: Increase intervals in production to reduce overhead
2. **Monitor health check metrics**: Track health check success/failure rates
3. **Implement circuit breakers**: Prevent cascade failures when dependencies are unhealthy
4. **Add custom health indicators**: Implement business-specific health checks (e.g., external API availability)
5. **Use Kubernetes probes**: When deploying to Kubernetes, map Docker health checks to liveness/readiness probes

---

## Related Documentation

- [docs/observability.md](./observability.md) - SLIs/SLOs and observability strategy
- [docs/test-observability.sh](./test-observability.sh) - Automated testing script
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Document Version**: 1.0
**Last Updated**: 2025-11-11
**Owner**: Team G304
