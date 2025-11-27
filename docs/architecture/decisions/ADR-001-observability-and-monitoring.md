# ADR-001: Observability and Monitoring Architecture

**Status**: Accepted

**Date**: 2025-11-26

**Deciders**: Development Team

**Technical Story**: Implement comprehensive observability and monitoring across the SLMS platform to ensure production readiness, enable proactive issue detection, and provide real-time insights into system performance and user experience.

---

## Context and Problem Statement

The SLMS (Smart Logistics Management System) is a distributed microservices architecture consisting of:
- **4 Next.js frontend applications** (Admin Console, Customer Portal, Driver PWA, Landing Page)
- **1 Spring Boot backend** (Java 21)
- **Supporting infrastructure** (PostgreSQL, Keycloak, Kafka, MinIO, Nginx)

We needed a comprehensive observability solution that could:
1. Monitor real user experience in browsers (RUM - Real User Monitoring)
2. Track backend API performance and bottlenecks
3. Provide **end-to-end distributed tracing** from browser → backend → database
4. Capture and analyze Core Web Vitals for UX optimization
5. Centralize logs, metrics, and traces in a single platform
6. Support production debugging and incident response
7. Enable proactive performance monitoring and alerting

## Decision Drivers

### Technical Requirements
- **Full-stack visibility**: Monitor both frontend (browser) and backend (server)
- **Distributed tracing**: Correlate requests across service boundaries
- **Real-time monitoring**: Detect issues as they happen
- **Performance analytics**: Measure and optimize Core Web Vitals
- **Error tracking**: Capture and correlate frontend and backend errors
- **Low overhead**: Minimal performance impact on production systems

### Business Requirements
- **User experience optimization**: Ensure fast, responsive applications
- **SLA compliance**: Meet performance SLAs (99.9% uptime, <2s page loads)
- **Cost effectiveness**: Avoid expensive monitoring solutions
- **Scalability**: Support growing traffic without linear cost increase
- **Developer productivity**: Easy to use, minimal configuration

### Constraints
- Limited budget for observability tools
- Must integrate with existing Elastic Cloud deployment
- Frontend must work with Next.js 15 and React 19
- Backend uses Spring Boot 3.5 with Elastic APM agent
- Deployment via Docker Compose (dev) and CI/CD (production)

## Considered Options

### Option 1: Elastic APM (Chosen)
**Description**: Use Elastic APM for both backend (Java agent) and frontend (RUM JavaScript agent)

**Pros**:
- ✅ Unified platform for frontend and backend
- ✅ Native distributed tracing with automatic context propagation
- ✅ Excellent Spring Boot integration
- ✅ Built-in RUM support for browser monitoring
- ✅ Already have Elastic Cloud deployment
- ✅ Comprehensive Core Web Vitals tracking
- ✅ Free tier suitable for development/staging
- ✅ Powerful Kibana UI for visualization

**Cons**:
- ❌ Vendor lock-in to Elastic ecosystem
- ❌ Cost scales with data volume in production
- ❌ Free trial limited to 14 days (requires new deployment for testing)

### Option 2: OpenTelemetry + Jaeger + Prometheus + Grafana
**Description**: Vendor-neutral observability stack

**Pros**:
- ✅ Vendor-neutral (OpenTelemetry standard)
- ✅ Open source (lower long-term costs)
- ✅ Flexible backend (can switch providers)
- ✅ Active community support

**Cons**:
- ❌ Complex setup (3+ tools to configure)
- ❌ No built-in RUM solution for browsers
- ❌ Manual correlation between frontend and backend
- ❌ Higher operational overhead
- ❌ Requires self-hosting infrastructure


## Decision Outcome

**Chosen Option: Elastic APM (Option 1)**

### Rationale

We selected Elastic APM as our observability solution because:

1. **Unified Platform**: Single platform for all observability needs (logs, metrics, traces, RUM)
2. **Existing Infrastructure**: Already have Elastic Cloud deployment
3. **Automatic Distributed Tracing**: Seamless trace propagation from browser to database
4. **Production-Ready**: Battle-tested in large-scale deployments
5. **Developer Experience**: Simple setup, minimal code changes
6. **Cost-Effective for Current Scale**: Free tier covers development and staging

### Implementation Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Elastic Cloud                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Kibana     │  │  APM Server  │  │Elasticsearch │          │
│  │  (UI/Viz)    │  │ (Collector)  │  │   (Storage)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ▲
                              │ HTTPS (intake/v2)
                              │
┌─────────────────────────────┼─────────────────────────────────┐
│                       Docker Compose                            │
│                                                                 │
│  ┌──────────────────────────┴──────────────────────────┐      │
│  │              Nginx Reverse Proxy                     │      │
│  │          (http://localhost:80)                       │      │
│  └──┬────────────┬────────────┬────────────┬───────────┘      │
│     │            │            │            │                   │
│  ┌──▼──┐     ┌──▼──┐     ┌───▼─┐     ┌───▼─┐                │
│  │Admin│     │Cust.│     │Driver│    │Land.│   Frontend      │
│  │Console    │Portal│     │ PWA │    │Page │   (Next.js)     │
│  └──┬──┘     └──┬──┘     └───┬─┘     └───┬─┘                │
│     │           │            │            │                   │
│     │  @elastic/apm-rum (Browser RUM)     │                   │
│     │  - Service: slms-admin-console      │                   │
│     │  - Service: slms-customer-portal    │                   │
│     │  - Service: slms-driver-pwa         │                   │
│     │  - Service: slms-landing-page       │                   │
│     │                                       │                   │
│     └───────────────┬───────────────────────┘                  │
│                     │                                           │
│                 ┌───▼────┐                                     │
│                 │ Backend │  Spring Boot 3.5                   │
│                 │  (SLMS) │                                    │
│                 └───┬────┘                                     │
│                     │                                           │
│            Elastic APM Java Agent 1.53.0                       │
│            - Service: SLMS                                      │
│            - Auto-instrumentation                              │
│            - Distributed tracing                               │
│                     │                                           │
│         ┌───────────┼───────────┐                             │
│         │           │           │                              │
│      ┌──▼──┐    ┌──▼───┐   ┌──▼──┐                           │
│      │PostgreSQL │Kafka  │ │MinIO│                            │
│      │   DB    │        │  │(S3) │                            │
│      └─────────┘ └──────┘  └─────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

## Implementation Details

### Backend Monitoring (Spring Boot)

**Technology**: Elastic APM Java Agent 1.53.0

**Configuration**:
```java
// src/main/java/Operimus/SLMS/SlmsApplication.java
ElasticApmAttacher.attach(Map.of(
    "server_url", System.getenv("ELASTIC_APM_SERVER_URL"),
    "secret_token", System.getenv("ELASTIC_APM_SECRET_TOKEN"),
    "service_name", "SLMS",
    "environment", System.getenv("DEPLOYMENT_ENV"),
    "application_packages", "Operimus.SLMS"
));
```

**What's Monitored**:
- HTTP requests (all REST endpoints)
- Database queries (JPA/Hibernate)
- External API calls (RestTemplate, WebClient)
- Kafka message processing
- JVM metrics (heap, GC, CPU)
- Exceptions and errors

**Automatic Instrumentation**: Zero code changes required for basic monitoring

### Frontend Monitoring (Next.js)

**Technology**: Elastic APM RUM (@elastic/apm-rum 5.17.0)

**Architecture Pattern**: Shared library + per-app configuration

**Services**:
1. `slms-admin-console` - Warehouse, logistics, and customer service management
2. `slms-customer-portal` - Customer order tracking and management
3. `slms-driver-pwa` - Driver delivery interface
4. `slms-landing-page` - Public marketing site

**Configuration**:
```typescript
// Shared library: src/frontend/shared/lib/observability/
- apm.ts           // Elastic APM RUM initialization
- web-vitals.ts    // Core Web Vitals tracking
- init.ts          // Unified initialization
- types.ts         // TypeScript definitions

// Per-app integration: app/layout.tsx
<ObservabilityProvider serviceName="slms-admin-console">
  {children}
</ObservabilityProvider>
```

**What's Monitored**:
- Page load performance
- Route changes (Next.js App Router)
- API calls (fetch/XHR) with distributed tracing
- JavaScript errors and exceptions
- User interactions (clicks, form submissions)
- Core Web Vitals (LCP, INP, CLS, FCP, TTFB)
- Resource timing (CSS, JS, images)

### Distributed Tracing

**Flow**:
```
User clicks button in Admin Console
  ↓
[Browser] APM RUM starts transaction (trace_id: abc123)
  ↓
fetch('/api/logistics/warehouses')
  ↓
[Browser] Adds W3C traceparent header: 00-abc123-def456-01
  ↓
[Nginx] Proxies request to backend
  ↓
[Backend] Elastic APM Java agent extracts trace context
  ↓
[Backend] Continues trace (same trace_id: abc123)
  ↓
[Backend] Calls PostgreSQL (traced as child span)
  ↓
[Backend] Returns response with trace headers
  ↓
[Browser] Receives response, ends transaction
  ↓
[Elastic APM] Shows complete trace: Browser → Backend → Database
```

**Trace Context Propagation**: Automatic via W3C Trace Context standard

### Core Web Vitals Tracking

**Metrics**:
| Metric | Description | Good Threshold | Implementation |
|--------|-------------|----------------|----------------|
| LCP | Largest Contentful Paint | < 2.5s | `web-vitals` library → APM transaction |
| INP | Interaction to Next Paint | < 200ms | `web-vitals` library → APM transaction |
| CLS | Cumulative Layout Shift | < 0.1 | `web-vitals` library → APM transaction |
| FCP | First Contentful Paint | < 1.8s | `web-vitals` library → APM transaction |
| TTFB | Time to First Byte | < 800ms | `web-vitals` library → APM transaction |

**Usage**:
```typescript
// Automatic tracking via ObservabilityProvider
initializeWebVitals(); // Called on mount

// Each metric sent as APM transaction
onLCP((metric) => {
  apm.startTransaction('web-vital-lcp', 'web-vital')
     .addLabels({ value: metric.value, rating: metric.rating })
     .end();
});
```

### Environment Configuration

**Development**:
```bash
# .env.local (local development only, gitignored)
NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=https://<deployment-id>.apm.elastic-cloud.com:443
NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=<token>
NEXT_PUBLIC_DEPLOYMENT_ENV=development
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0  # 100% sampling in dev
```

**Production**:
```yaml
# GitHub Actions (from secrets)
docker build \
  --build-arg NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=${{ secrets.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL }} \
  --build-arg NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=${{ secrets.NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN }} \
  --build-arg NEXT_PUBLIC_DEPLOYMENT_ENV=production \
  --build-arg NEXT_PUBLIC_TRACING_SAMPLING_RATE=0.1  # 10% sampling in prod
```

**Security**:
- `NEXT_PUBLIC_*` variables are **baked into client-side JavaScript** (unavoidable for browser RUM)
- Secret token is **write-only** (can only send data, not read)
- Elastic APM validates all incoming data server-side
- No sensitive application data exposed (only telemetry)

## Consequences

### Positive

1. **✅ Full Observability Stack**: Complete visibility from browser to database
2. **✅ Automatic Distributed Tracing**: Zero-config trace propagation
3. **✅ Developer Productivity**: Issues detected and debugged faster
4. **✅ Performance Insights**: Data-driven optimization decisions
5. **✅ User Experience Monitoring**: Real-time Core Web Vitals tracking
6. **✅ Incident Response**: Faster MTTR (Mean Time To Recovery)
7. **✅ SLA Monitoring**: Track and validate performance SLAs
8. **✅ Cost Effective**: Free tier covers dev/staging environments

### Negative

1. **❌ Vendor Lock-in**: Dependent on Elastic ecosystem
2. **❌ Cost at Scale**: Production costs increase with data volume
3. **❌ Free Trial Limits**: 14-day trial requires migration to new deployment
4. **❌ Browser Extension Conflicts**: Some ad blockers block APM requests
5. **❌ Learning Curve**: Team needs training on Kibana and query languages

## Alternatives Considered and Rejected

### OpenTelemetry Browser SDK
**Status**: Initially attempted, then disabled

**Reason**:
- Compatibility issues with Next.js 15 webpack bundling
- `@opentelemetry/resources` package not browser-compatible
- Elastic APM RUM provides all necessary functionality
- Adds complexity without significant benefit

**Decision**: Disabled OTEL, kept only Elastic APM RUM

### Self-Hosted Elastic Stack
**Reason for Rejection**:
- Operational overhead (Kubernetes cluster, storage, backups)
- No expertise in Elastic infrastructure management
- Cost of infrastructure vs. Elastic Cloud not justified
- Security and compliance complexity

### Logs-Only Approach (ELK Stack)
**Reason for Rejection**:
- No distributed tracing
- No automatic performance metrics
- No browser RUM support
- Manual correlation between frontend and backend

## Monitoring and Alerting Strategy

### Key Metrics to Monitor

**Frontend (RUM)**:
- Page load time (p50, p95, p99)
- Core Web Vitals (LCP, INP, CLS)
- JavaScript error rate
- API call success rate and latency
- User interaction latency

**Backend (APM)**:
- Request throughput (requests/minute)
- Response time (p50, p95, p99)
- Error rate (5xx errors)
- Database query performance
- External API latency (Shippo, ShipEngine)
- JVM heap usage and GC pauses

**Infrastructure**:
- Container health and restarts
- Database connection pool usage
- Kafka consumer lag
- Nginx request rate and errors

### Alerting Rules (Recommended)

| Metric | Threshold | Severity | Action |
|--------|-----------|----------|--------|
| Page Load Time (p95) | > 3s | Warning | Investigate performance |
| LCP | > 4s | Critical | Immediate optimization needed |
| Backend Error Rate | > 5% | Critical | Page ops team |
| Database Response Time (p95) | > 500ms | Warning | Check slow queries |
| JVM Heap Usage | > 85% | Warning | Check for memory leaks |
| API Error Rate (Shippo/ShipEngine) | > 10% | Critical | Check external service status |

## References

- [Elastic APM Documentation](https://www.elastic.co/guide/en/apm/guide/current/index.html)
- [Elastic APM RUM Agent](https://www.elastic.co/guide/en/apm/agent/rum-js/current/index.html)
- [Core Web Vitals](https://web.dev/vitals/)
- [W3C Trace Context Specification](https://www.w3.org/TR/trace-context/)
- [Spring Boot Elastic APM Integration](https://www.elastic.co/guide/en/apm/agent/java/current/spring-boot.html)

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-11-10 | Adopt Elastic APM for full-stack observability | Unified platform, automatic distributed tracing, existing infrastructure |
| 2025-11-25 | Disable OpenTelemetry browser SDK | Next.js compatibility issues, Elastic APM RUM sufficient |
| 2025-11-25 | Use 100% sampling in dev, 10% in production | Balance between visibility and cost |
| 2025-11-26 | Deploy RUM to all 4 frontends | Comprehensive user experience monitoring |
| 2025-11-26 | Store secrets in GitHub Secrets | Secure deployment pipeline |

---
