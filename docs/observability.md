# SLMS Observability Documentation

## Service Level Indicators (SLIs) and Objectives (SLOs)

Following the principles from Software Engineering course (ES_08_Monitoring_Observability, pages 8-9), we define quantitative measures (SLIs) and targets (SLOs) for the SLMS system.

### Core SLIs

#### 1. Availability SLI
**Definition**: Percentage of successful HTTP requests (non-5xx responses)

**Measurement**:
```
availability = (successful_requests / total_requests) × 100%
where successful_requests = requests with HTTP status 200-499
```

**Data Source**:
- OpenTelemetry metrics: `http.server.requests` with status code labels
- Elastic APM: Service transaction success rate

#### 2. Latency SLI
**Definition**: Percentage of requests served within acceptable time thresholds

**Measurement**:
```
latency_sli = (requests_under_threshold / total_requests) × 100%
```

**Thresholds**:
- **Fast**: < 300ms (target for 95% of requests)
- **Acceptable**: < 1000ms (target for 99% of requests)
- **Slow**: > 1000ms (should be < 1%)

**Data Source**:
- OpenTelemetry metrics: `http.server.requests` duration histogram
- Elastic APM: Transaction latency percentiles (p50, p95, p99)

#### 3. Error Rate SLI
**Definition**: Percentage of failed requests and transactions

**Measurement**:
```
error_rate = (failed_requests / total_requests) × 100%
where failed_requests = HTTP 5xx + application exceptions
```

**Data Source**:
- OpenTelemetry metrics: `http.server.requests{status=~"5.."}`
- Application logs: ERROR level count
- Elastic APM: Error transaction rate

#### 4. Data Integrity SLI
**Definition**: Percentage of successful database operations

**Measurement**:
```
data_integrity = (successful_db_ops / total_db_ops) × 100%
```

**Data Source**:
- JDBC connection metrics
- PostgreSQL query success rate
- Elastic APM: Database span success rate

### Service Level Objectives (SLOs)

Based on industry best practices and system requirements, we set the following SLOs:

| SLI | SLO Target | Measurement Window | Error Budget |
|-----|------------|-------------------|--------------|
| **API Availability** | 99.9% | 30 days | 43.2 minutes/month |
| **API Latency (p95)** | < 500ms | 5 minutes | N/A |
| **API Latency (p99)** | < 1500ms | 5 minutes | N/A |
| **Error Rate** | < 0.1% | 1 hour | 0.1% of requests |
| **Data Integrity** | 99.99% | 24 hours | 8.6 seconds/day |

### SLO Rationale

#### Availability: 99.9% (Three Nines)
- **Why**: Logistics operations require high availability but can tolerate brief outages
- **Allows**: 43.2 minutes of downtime per month
- **Error Budget**: Can be spent on deployments, incidents, or experiments
- **User Impact**: Users experience service unavailability ~0.1% of the time

#### Latency: p95 < 500ms, p99 < 1500ms
- **Why**: User experience research shows users expect web apps to respond within 1 second
- **p95 < 500ms**: 95% of users get fast responses (good UX)
- **p99 < 1500ms**: Even slowest requests remain usable
- **User Impact**: Ensures responsive interface for shipment tracking and management

#### Error Rate: < 0.1%
- **Why**: Critical business operations (shipment creation, tracking) must be reliable
- **Allows**: 1 error per 1000 requests
- **User Impact**: High confidence in system correctness

#### Data Integrity: 99.99%
- **Why**: Database operations must be highly reliable for business data
- **Allows**: Very minimal data operation failures
- **User Impact**: Trust in shipment and customer data accuracy

### Error Budgets

**Error Budget** = Allowable amount of downtime/errors within SLO target

#### Availability Error Budget Calculation
```
SLO: 99.9% availability
Error Budget: 100% - 99.9% = 0.1%

Monthly Error Budget:
30 days × 24 hours × 60 minutes × 0.001 = 43.2 minutes/month

Weekly Error Budget:
7 days × 24 hours × 60 minutes × 0.001 = 10.08 minutes/week
```

#### Error Budget Policy
- **Budget Healthy (> 50% remaining)**:
  - Normal deployment cadence
  - Can experiment with new features
  - Focus on velocity

- **Budget Warning (20-50% remaining)**:
  - Increase caution in deployments
  - Enhanced testing before releases
  - Begin reliability improvements

- **Budget Exhausted (< 20% remaining)**:
  - **FREEZE**: No new feature deployments
  - Focus exclusively on reliability
  - Mandatory incident post-mortems
  - Implement corrective actions

### User Journey SLIs

Following Pattern #2 (Monitor from User Perspective, slide 17), we define SLIs for critical user journeys:

#### Journey 1: Shipment Creation
**Steps**: Login → Create Shipment → Select Carrier → Generate Label → Confirmation

**SLI**: End-to-end success rate
```
shipment_creation_success = (successful_completions / attempts) × 100%
Target: > 99.5%
```

#### Journey 2: Real-time Tracking
**Steps**: Access Tracking Page → Query Shipment Status → Display Updates

**SLI**: Query latency + data freshness
```
tracking_latency_p95 < 300ms
data_freshness < 30 seconds
Target: Meet both thresholds for 99% of requests
```

#### Journey 3: Driver Package Scan
**Steps**: Scan QR Code → Update Status → Sync to Server

**SLI**: Scan-to-sync latency
```
scan_latency_p99 < 2000ms
Target: 99% of scans complete within 2 seconds
```

### Monitoring User-Facing Metrics (Slide 13)

As emphasized in the course material (slide 13), end-users expect:

1. **Availability**: Is the system online?
   - Measured: HTTP health checks + synthetic monitoring
   - Target: 99.9% uptime

2. **Latency**: Does it take too long to access and use?
   - Measured: Request duration percentiles
   - Target: p95 < 500ms

3. **Reliability**: Can the user rely on this application?
   - Measured: Error rate + data integrity
   - Target: < 0.1% errors

---

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
