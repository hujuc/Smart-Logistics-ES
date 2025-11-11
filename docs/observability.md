# SLMS Observability Documentation

## Observability Stack

### Architecture

```
┌─────────────────┐
│  Spring Boot    │
│   Application   │
│                 │
│  - Logback      │──┐
│  - OTel Agent   │  │
│  - Actuator     │  │
└─────────────────┘  │
                     │
                     ▼
            ┌────────────────┐
            │ Elastic Cloud  │
            │                │
            │ - APM Server   │
            │ - Elasticsearch│
            │ - Kibana       │
            └────────────────┘
```

### Components

#### 1. Logs (Structured JSON)
- **Format**: JSON via logstash-logback-encoder
- **Fields**: timestamp, level, logger, message, trace_id, span_id
- **Collection**: Elastic APM agent (autodiscovery)
- **Correlation**: Logs linked to traces via trace_id

#### 2. Metrics (OpenTelemetry + Micrometer)
- **OTLP Export**: Metrics sent to Elastic APM every 10s
- **Prometheus Endpoint**: `/actuator/prometheus` (backup/monitoring)
- **Key Metrics**:
  - JVM metrics (heap, GC, threads)
  - HTTP server metrics (requests, latency, status codes)
  - Database connection pool metrics
  - Custom business metrics

#### 3. Traces (OpenTelemetry)
- **Protocol**: OTLP/gRPC to Elastic APM
- **Sampling**: 100% in development, tune for production
- **Instrumentation**: Automatic for Spring Boot, JDBC, HTTP clients
- **Span Attributes**: service.name, deployment.environment, service.version

#### 4. Health Checks
- **Endpoint**: `/actuator/health`
- **Probes**:
  - Liveness: Application is running
  - Readiness: Application can accept traffic
- **Dependencies**: PostgreSQL, Keycloak, MinIO availability

---

## Alerting Strategy

### Alert Levels

#### P0 - Critical (Page immediately)
- API availability < 99% in last 5 minutes
- Error rate > 5% in last 5 minutes
- Database connection pool exhausted
- All health checks failing

#### P1 - High (Alert during business hours)
- API availability < 99.5% in last 15 minutes
- p99 latency > 2000ms for 10 minutes
- Error rate > 2% in last 10 minutes

#### P2 - Medium (Create ticket)
- Approaching error budget (< 20% remaining)
- p95 latency > 800ms for 30 minutes
- Elevated 4xx error rate

### Alert Configuration

Alerts will be configured in Elastic Kibana:
1. Navigate to **Observability → Alerts**
2. Create rules based on SLO thresholds
3. Configure notification channels (email, Slack, etc.)

---

## Dashboards

### 1. Service Overview Dashboard
- Service availability (last 24h, 7d, 30d)
- Request rate and latency (p50, p95, p99)
- Error rate by status code
- Active incidents and SLO compliance

### 2. User Journey Dashboard
- End-to-end latency for critical paths
- Success rate by user journey
- Funnel analysis (shipment creation flow)

### 3. Infrastructure Dashboard
- JVM heap usage and GC activity
- Database connection pool utilization
- Thread pool usage
- Container resource usage (CPU, memory)

### 4. Business Metrics Dashboard
- Shipments created per hour
- Active users
- Document uploads
- Carrier integration success rate

---

## Incident Response

### When SLO is Breached

1. **Detect**: Alert fires based on SLO violation
2. **Assess**: Check dashboards to understand impact scope
3. **Investigate**: Use traces to identify root cause
4. **Mitigate**: Apply fix or rollback
5. **Resolve**: Verify metrics return to normal
6. **Document**: Write post-mortem (if critical)

### Using Observability for Debugging

1. **Start with metrics**: Identify when issue started
2. **Filter traces**: Find slow or failing requests
3. **Examine logs**: View detailed context with trace_id
4. **Correlate spans**: Identify bottleneck service/operation

---

## Testing Observability

### Verification Steps

1. **Logs**: Check JSON structure and trace correlation
   ```bash
   curl http://localhost/api/v1/health
   # Check backend logs for structured JSON with trace_id
   ```

2. **Metrics**: Verify Prometheus endpoint
   ```bash
   curl http://localhost/actuator/prometheus | grep http_server_requests
   ```

3. **Traces**: Generate traffic and view in Kibana APM
   ```bash
   for i in {1..100}; do curl http://localhost/api/v1/shipments; done
   # View traces in Kibana → APM → Services → SLMS → Transactions
   ```

4. **Health Checks**: Verify container health
   ```bash
   docker ps --format "table {{.Names}}\t{{.Status}}"
   ```

---

## Production Recommendations

### Before Deploying to Production

1. **Adjust sampling rate**: Set `TRACING_SAMPLING_RATE=0.1` (10%)
2. **Secure APM credentials**: Use secrets management
3. **Configure log retention**: Set appropriate retention policies in Elastic
4. **Set up alerting**: Configure all P0/P1 alerts
5. **Create runbooks**: Document incident response procedures
6. **Load testing**: Validate SLOs under expected production load

### Cost Optimization

- Use sampling for traces (10% in prod)
- Configure log filtering (exclude verbose debug logs)
- Set index lifecycle policies in Elasticsearch
- Monitor Elastic Cloud usage and adjust as needed

---

## References

- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Elastic APM Java Agent](https://www.elastic.co/guide/en/apm/agent/java/current/index.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [SLO/SLI Best Practices](https://sre.google/workbook/implementing-slos/)
- Course Material: ES_08_Monitoring_Observability.pdf (Software Engineering 2025/2026)

---

**Document Version**: 1.0
**Last Updated**: 2025-01-11
**Owner**: Team G304
