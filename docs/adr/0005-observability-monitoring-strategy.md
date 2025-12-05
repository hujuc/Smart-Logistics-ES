# 0005 – Observability and Monitoring Architecture

**Sprint 4**

## Context

As SLMS grows in complexity with multiple modules, external API integrations, and distributed components, we need comprehensive observability to ensure system reliability and meet user expectations.

The SLMS consists of:
- 4 Next.js frontend applications (Admin Console, Customer Portal, Driver PWA, Landing Page)
- 1 Spring Boot backend (Java 21)
- Supporting infrastructure (PostgreSQL, Keycloak, Kafka, MinIO, Nginx)

### Problem

Without proper observability, we face blind spots in performance, slow debugging, no SLO tracking, and production deployment risks.

### Requirements

- Monitor real user experience in browsers (RUM)
- Track backend API performance and bottlenecks
- Provide end-to-end distributed tracing from browser to database
- Capture and analyze Core Web Vitals for UX optimization
- Centralize logs, metrics, and traces in a single platform
- Support production debugging and incident response
- Enable proactive performance monitoring and alerting

### Constraints

- Must integrate with existing Docker-based infrastructure
- Should minimize operational overhead for small team
- Budget constraints favor managed services over self-hosted
- Frontend must work with Next.js 15 and React 19
- Backend uses Spring Boot 3.5

## Decision

We adopted **Elastic APM** as the unified observability platform for both backend and frontend monitoring with automatic distributed tracing.

### Technology Stack

**Elastic Cloud Suite:**
- Elastic APM - Application Performance Monitoring
- Elasticsearch - Time-series data storage and search
- Kibana - Visualization and alerting dashboards

**Backend**: Elastic APM Java Agent 1.53.0 with automatic instrumentation
**Frontend**: Elastic APM RUM (@elastic/apm-rum 5.17.0) for browser monitoring

### Implementation Architecture

**Backend Monitoring (Spring Boot)**
- Elastic APM Java agent attached at application startup
- Automatic instrumentation: HTTP requests, database queries, external API calls, Kafka messages
- JVM metrics: heap, GC, CPU
- Zero code changes required for basic monitoring

**Frontend Monitoring (Next.js)**
- Elastic APM RUM integrated via shared library
- Separate services: slms-admin-console, slms-customer-portal, slms-driver-pwa, slms-landing-page
- Monitors: page load performance, route changes, API calls, JavaScript errors, user interactions
- Core Web Vitals tracking: LCP, INP, CLS, FCP, TTFB

**Distributed Tracing**
- Automatic trace propagation via W3C Trace Context standard
- Flow: Browser starts transaction → fetch adds traceparent header → Backend continues trace → Database traced as child span
- Complete visibility: Browser → Nginx → Backend → Database

**Core Web Vitals**
- LCP (Largest Contentful Paint) < 2.5s
- INP (Interaction to Next Paint) < 200ms
- CLS (Cumulative Layout Shift) < 0.1
- Automatic tracking via web-vitals library sent as APM transactions

### Configuration

Environment variables:
- `ELASTIC_APM_SERVER_URL` - APM server endpoint
- `ELASTIC_APM_SECRET_TOKEN` - Authentication token
- `DEPLOYMENT_ENV` - Environment identifier (development/staging/production)
- `TRACING_SAMPLING_RATE` - 100% in dev, 10% in production

Security: Secret tokens are write-only, no sensitive application data exposed.

## Consequences

**Positive:**

- Full observability stack with visibility from browser to database
- Automatic distributed tracing with zero-config trace propagation
- Developer productivity improvements: faster issue detection and debugging
- Performance insights enable data-driven optimization decisions
- Real-time Core Web Vitals tracking for user experience monitoring
- Faster incident response (MTTR reduction)
- SLA monitoring and validation capabilities
- Cost effective: free tier covers dev/staging environments
- Production-ready solution battle-tested at scale

**Negative:**

- Vendor lock-in to Elastic ecosystem
- Production costs increase with data volume
- Free trial limited to 14 days (requires migration for extended testing)
- Some browser extensions (ad blockers) may block APM requests
- Team learning curve for Kibana and query languages

## Alternatives Considered

### Option 1: OpenTelemetry + Jaeger + Prometheus + Grafana
**Why not chosen:**
- Complex setup requiring 3+ tools to configure
- No built-in RUM solution for browsers
- Manual correlation between frontend and backend
- Higher operational overhead
- Requires self-hosting infrastructure
- Longer initial setup time

### Option 2: OpenTelemetry Browser SDK
**Why not chosen:**
- Compatibility issues with Next.js 15 webpack bundling
- @opentelemetry/resources package not browser-compatible
- Elastic APM RUM provides all necessary functionality
- Adds complexity without significant benefit
- **Decision**: Disabled OTEL, kept only Elastic APM RUM

### Option 3: Self-Hosted Elastic Stack
**Why not chosen:**
- Operational overhead (Kubernetes, storage, backups)
- No team expertise in Elastic infrastructure management
- Cost of infrastructure vs. Elastic Cloud not justified
- Security and compliance complexity

### Option 4: Logs-Only Approach (ELK Stack)
**Why not chosen:**
- No distributed tracing capabilities
- No automatic performance metrics
- No browser RUM support
- Manual correlation between frontend and backend

## Related Decisions

- ADR-0001 – Initial System Architecture
- ADR-0003 – Architecture Update (Docker, DB integration)

## Notes

- Monitoring strategy includes alerts for page load time, Core Web Vitals, error rates, database performance, and JVM metrics
- Recommended alerting thresholds: Page load p95 > 3s (warning), LCP > 4s (critical), Backend error rate > 5% (critical)
- Key metrics: Frontend (page load, Core Web Vitals, JS errors, API calls), Backend (throughput, response time, error rate, DB queries, external API latency, JVM metrics)
- Decision log: Adopted Elastic APM (Nov 10, 2025), Disabled OTEL browser SDK (Nov 25), Deployed RUM to all 4 frontends (Nov 26)
- Implementation details available in handbook: observability.md
- Elastic APM validated for production use as of Nov 26, 2025
