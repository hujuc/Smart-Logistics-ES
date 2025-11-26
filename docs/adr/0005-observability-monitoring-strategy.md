# ADR-0005 – Observability and Monitoring Strategy

**Sprint 4**

## Context

As SLMS grows in complexity with multiple modules (Order, Carrier, Route, User Management), external API integrations (Shippo, ShipEngine), and distributed components (backend, frontends, databases, Keycloak, MinIO), we need comprehensive observability to ensure system reliability and meet user expectations.

### Problem

Without proper observability, we face:
- **Blind spots**: Cannot detect performance degradation or failures early
- **Slow debugging**: Difficult to identify root causes when issues occur
- **No SLO tracking**: Cannot measure if we're meeting user expectations
- **Production risks**: Cannot confidently deploy without monitoring

### Requirements

Following Software Engineering course principles (ES_08_Monitoring_Observability), we need:

1. **User-facing metrics** (slide 13): Users care about availability, latency, and reliability
2. **Monitor from user perspective** (pattern #2, slide 17): Track critical user journeys end-to-end
3. **Define SLIs/SLOs** (pages 8-9): Quantitative measures and targets for reliability
4. **Three pillars of observability**: Logs, metrics, and traces
5. **Actionable alerts**: Detect SLO violations with minimal false positives
6. **Error budgets**: Balance velocity with reliability

### Constraints

- Must integrate with existing Docker-based infrastructure
- Should minimize operational overhead for small team
- Must support debugging across distributed components
- Need to correlate logs, metrics, and traces
- Budget constraints favor managed services over self-hosted

## Decision

We will implement a **comprehensive observability stack** using:

### 1. Technology Stack

**Elastic Cloud Suite** as the unified observability platform:
- **Elastic APM**: Application Performance Monitoring
- **Elasticsearch**: Time-series data storage and search
- **Kibana**: Visualization and alerting dashboards

**OpenTelemetry** for instrumentation:
- Automatic instrumentation for Spring Boot, JDBC, HTTP
- OTLP protocol for metrics and traces
- Structured JSON logging with trace correlation

### 2. Service Level Indicators (SLIs)

| SLI | Definition | Target SLO | Measurement Window |
|-----|-----------|------------|-------------------|
| **API Availability** | % of successful HTTP requests (non-5xx) | 99.9% | 30 days |
| **API Latency (p95)** | 95th percentile request duration | < 500ms | 5 minutes |
| **API Latency (p99)** | 99th percentile request duration | < 1500ms | 5 minutes |
| **Error Rate** | % of failed requests (5xx + exceptions) | < 0.1% | 1 hour |
| **Data Integrity** | % of successful database operations | 99.99% | 24 hours |

**Rationale for SLOs:**
- **99.9% availability**: Allows 43.2 minutes downtime/month (appropriate for logistics operations)
- **p95 < 500ms**: 95% of users get fast responses (good UX)
- **p99 < 1500ms**: Even slowest requests remain usable (< 1 second expected)
- **< 0.1% error rate**: High confidence in system correctness
- **99.99% data integrity**: Trust in business data accuracy

### 3. User Journey Monitoring

Following pattern #2 (monitor from user perspective), we track:

**Journey 1: Shipment Creation**
- SLI: End-to-end success rate > 99.5%
- Steps: Login → Create Shipment → Select Carrier → Generate Label → Confirmation

**Journey 2: Real-time Tracking**
- SLI: Query latency p95 < 300ms + data freshness < 30s
- Steps: Access Tracking Page → Query Status → Display Updates

**Journey 3: Driver Package Scan**
- SLI: Scan-to-sync latency p99 < 2000ms
- Steps: Scan QR Code → Update Status → Sync to Server

### 4. Observability Architecture

```
┌─────────────────┐
│  Spring Boot    │
│   Application   │
│                 │
│  - Logback      │──┐ (Structured JSON)
│  - OTel Agent   │  │ (OTLP/gRPC)
│  - Actuator     │  │ (Health checks)
└─────────────────┘  │
                     ▼
            ┌────────────────┐
            │ Elastic Cloud  │
            │                │
            │ - APM Server   │
            │ - Elasticsearch│
            │ - Kibana       │
            └────────────────┘
```

### 5. Error Budget Policy

**Monthly error budget**: 43.2 minutes (0.1% of 30 days)

- **Budget healthy (> 50% remaining)**: Normal deployment cadence, experiment with features
- **Budget warning (20-50% remaining)**: Increase caution, enhanced testing
- **Budget exhausted (< 20% remaining)**: FREEZE deployments, focus on reliability only

### 6. Alerting Strategy

**P0 - Critical (page immediately)**:
- API availability < 99% in last 5 minutes
- Error rate > 5% in last 5 minutes
- Database connection pool exhausted

**P1 - High (alert during business hours)**:
- API availability < 99.5% in last 15 minutes
- p99 latency > 2000ms for 10 minutes
- Error rate > 2% in last 10 minutes

**P2 - Medium (create ticket)**:
- Approaching error budget (< 20% remaining)
- p95 latency > 800ms for 30 minutes

## Consequences

### Positive

- **Proactive incident detection**: Alerts fire before users notice issues
- **Fast root cause analysis**: Distributed tracing shows exact bottleneck
- **Data-driven decisions**: SLOs guide prioritization of reliability work
- **User-centric approach**: Metrics reflect actual user experience
- **Reduced MTTR**: Correlated logs/traces/metrics speed debugging
- **Confidence in deployments**: Can measure impact of changes
- **Clear reliability targets**: Error budgets balance velocity vs stability
- **Production readiness**: Comprehensive monitoring meets industry standards

### Negative

- **Operational complexity**: Additional service (Elastic Cloud) to manage
- **Cost**: Elastic Cloud subscription fees (can be significant at scale)
- **Learning curve**: Team needs to understand SLIs/SLOs, Kibana dashboards, query language
- **Performance overhead**: OpenTelemetry agent adds ~2-5% CPU/memory usage
- **Data retention costs**: Need to balance retention vs storage costs
- **Alert fatigue risk**: Must carefully tune alerts to avoid false positives

## Alternatives Considered

### Option 1: Prometheus + Grafana (Self-hosted)

**Why not chosen:**
- Requires self-hosting and maintenance (Prometheus server, Grafana instance)
- No built-in distributed tracing (would need Jaeger/Tempo separately)
- More operational overhead for small team
- Steeper learning curve for team unfamiliar with PromQL
- **Verdict**: Better for teams with DevOps resources

### Option 2: Datadog (Managed SaaS)

**Why not chosen:**
- More expensive than Elastic Cloud at scale
- Less flexible for custom queries compared to Elasticsearch
- Proprietary agent instead of open standard (OpenTelemetry)
- **Verdict**: Good alternative if budget permits and want simpler setup

### Option 3: AWS CloudWatch + X-Ray

**Why not chosen:**
- Tight coupling to AWS (not cloud-agnostic)
- Limited query capabilities compared to Elasticsearch
- Fragmented experience (separate tools for logs, metrics, traces)
- Current infrastructure is Docker-based, not AWS-native
- **Verdict**: Good if already heavily invested in AWS

### Option 4: Minimal Logging Only (No APM)

**Why not chosen:**
- No metrics or traces = blind to performance issues
- Cannot track SLOs or error budgets
- Debugging production issues would be extremely slow
- Not suitable for production-grade system
- **Verdict**: Acceptable only for prototypes/demos

## Related Decisions

- ADR-0001 – Initial System Architecture (Sprint 1)
- ADR-0003 – Architecture Update - Database Integration (Sprint 2)
- ADR-0004 – Authentication and Security Architecture (Sprint 3)

## Notes

- Full implementation details in [docs/per-sprint/sprint4/observability.md](../per-sprint/sprint4/observability.md)
- Health check implementation in [docs/per-sprint/sprint4/health-checks-implementation.md](../per-sprint/sprint4/health-checks-implementation.md)
- OpenTelemetry sampling set to 100% in development, will tune for production (10% recommended)
- Elastic Cloud credentials secured via environment variables, not committed to repo
- SLO targets are initial estimates and should be validated under production load
- Team trained on incident response procedures and using Kibana for debugging
- Log retention policy: 30 days for development, 90 days minimum for production (compliance)
- Monthly review of SLO attainment and error budget consumption
