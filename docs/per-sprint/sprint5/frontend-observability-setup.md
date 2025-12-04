# Frontend Observability Setup

## Overview

The SLMS implements comprehensive observability across all frontend services using Elastic APM (Application Performance Monitoring). This provides real-time insights into application performance, user experience, and end-to-end distributed tracing from browser to database.

Status: Production Ready

Last Updated: November 26, 2025

## Implementation

### Monitoring Components

**Elastic APM RUM**
- Real-time page load monitoring
- Route change tracking (Next.js App Router)
- XHR/Fetch API call monitoring with distributed tracing
- JavaScript error capture with stack traces
- Custom transactions and spans
- User context tracking

**Core Web Vitals (Google's Performance Metrics)**
- LCP (Largest Contentful Paint) - Loading performance (< 2.5s = good)
- INP (Interaction to Next Paint) - Responsiveness (< 200ms = good, replaces FID)
- CLS (Cumulative Layout Shift) - Visual stability (< 0.1 = good)
- FCP (First Contentful Paint) - Initial rendering (< 1.8s = good)
- TTFB (Time to First Byte) - Server response time (< 800ms = good)

**OpenTelemetry Integration**
- Status: Currently disabled due to Next.js compatibility issues
- Reason: Elastic APM RUM provides all necessary browser instrumentation
- Can be re-enabled if vendor-neutral telemetry is required

### Architecture

```
Browser (Frontend)                     Backend (Spring Boot)
      |                                        |
      ├─ Page loads                            |
      |  └─ Elastic APM RUM                    |
      |     └─ Web Vitals                      |
      |                                        |
      ├─ User clicks button                    |
      |  └─ APM starts transaction             |
      |     (trace_id: abc123)                 |
      |                                        |
      ├─ fetch('/api/shipments')               |
      |  Header: traceparent: 00-abc123...     |
      ├────────────────────────────────────────>|
      |                                        |
      |                            APM continues trace
      |                            (same trace_id: abc123)
      |                                        |
      |<────────────────────────────────────────┤
      |                                        |
      └─ APM ends transaction                  |
         Reports metrics to Elastic Cloud
```

### Files Structure

**Shared Library** (`/src/frontend/shared/`)

```
shared/
├── lib/observability/
│   ├── types.ts              # TypeScript type definitions
│   ├── apm.ts                # Elastic APM RUM initialization
│   ├── otel.ts               # OpenTelemetry browser setup
│   ├── web-vitals.ts         # Core Web Vitals tracking
│   ├── init.ts               # Unified initialization
│   └── index.ts              # Module exports
└── components/observability/
    └── ObservabilityProvider.tsx  # React component for easy integration
```

**Integration in Each Frontend**

All 4 frontends updated in their `app/layout.tsx`:
- `admin-console` - Service: `slms-admin-console`
- `customer-portal` - Service: `slms-customer-portal`
- `driver-pwa` - Service: `slms-driver-pwa`
- `landing-page` - Service: `slms-landing-page`

### Environment Configuration

Required environment variables (added to `/infra/.env`):

```bash
# Frontend RUM Configuration (NEXT_PUBLIC_ makes them available to browser)
NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=https://[your-id].apm.europe-west1.gcp.cloud.es.io:443
NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=[your-token]
NEXT_PUBLIC_DEPLOYMENT_ENV=development
NEXT_PUBLIC_PROJECT_VERSION=0.0.1-SNAPSHOT
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0
NEXT_PUBLIC_API_URL=http://localhost
```

All frontend services in docker-compose.dev.yml receive these environment variables.

## How It Works

### Initialization Flow

1. Next.js App Loads - `app/layout.tsx` renders
2. ObservabilityProvider Mounts - Client-side only (useEffect)
3. Checks Configuration - Validates `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL`
4. Initializes APM - Elastic APM RUM agent starts
5. Initializes OpenTelemetry - OTEL SDK configures instrumentations (currently disabled)
6. Starts Web Vitals Tracking - Monitors Core Web Vitals
7. Automatic Instrumentation Begins - All page loads, API calls, errors tracked

### Distributed Tracing

Frontend to Backend trace propagation:

```typescript
// Frontend automatically adds traceparent header
fetch('http://localhost/api/shipments')
// Header: traceparent: 00-abc123def456-789ghi012jkl-01

// Backend Elastic APM agent automatically:
// 1. Extracts trace context from traceparent header
// 2. Continues the trace with same trace_id
// 3. Adds backend spans (controller, service, database)
// 4. Sends complete trace to Elastic APM
```

Result: Single distributed trace spanning browser → nginx → backend → database

### Service Names in Elastic APM

Each frontend appears as a separate service:
- `slms-admin-console` (TypeScript/React)
- `slms-customer-portal` (TypeScript/React)
- `slms-driver-pwa` (TypeScript/React)
- `slms-landing-page` (TypeScript/React)
- `SLMS` (Java/Spring Boot) - backend

## Usage

### Basic Integration (Already Configured)

```tsx
// app/layout.tsx
import { ObservabilityProvider } from '@shared/components/observability/ObservabilityProvider';

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <ObservabilityProvider serviceName="slms-admin-console">
          {children}
        </ObservabilityProvider>
      </body>
    </html>
  );
}
```

### Advanced Usage Examples

**Track Custom Transactions**
```typescript
import { startTransaction } from '@shared/lib/observability';

const transaction = startTransaction('checkout-flow', 'user-action');
// ... perform checkout operations
transaction?.end();
```

**Track Custom Spans**
```typescript
import { startSpan } from '@shared/lib/observability';

const span = startSpan('validate-payment', 'validation');
// ... validation logic
span?.end();
```

**Set User Context**
```typescript
import { setUserContext } from '@shared/lib/observability';

setUserContext({
  id: user.id,
  username: user.username,
  email: user.email,
});
```

**Capture Custom Errors**
```typescript
import { captureError } from '@shared/lib/observability';

try {
  // risky operation
} catch (error) {
  captureError(error, {
    operation: 'checkout',
    orderId: '12345',
  });
}
```

**Add Custom Labels**
```typescript
import { addLabels } from '@shared/lib/observability';

addLabels({
  'order.id': '12345',
  'order.total': 99.99,
  'user.role': 'customer',
});
```

**Report Custom Metrics**
```typescript
import { reportCustomMetric } from '@shared/lib/observability/web-vitals';

reportCustomMetric('checkout-duration', 2500, {
  items: 3,
  paymentMethod: 'credit-card',
});
```

## Testing & Verification

### Start the Application

```bash
cd /path/to/project/infra
docker compose -f docker-compose.dev.yml up --build
```

### Access Frontends

- Landing Page: http://localhost/
- Admin Console: http://localhost/admin
- Customer Portal: http://localhost/customer
- Driver PWA: http://localhost/driver

### Check Browser Console

You should see initialization messages:
```
[APM] ✓ Initialized for service: slms-landing-page
[APM] Environment: development
[APM] Sample rate: 100%
[Web Vitals] ✓ Tracking initialized
[Observability] Initialization complete
```

### Verify in Elastic APM UI

1. Go to your Elastic Cloud console
2. Open Kibana
3. Navigate to Observability → APM → Services
4. Verify all services appear: 4 frontends + 1 backend
5. Click on any frontend service to see transactions, errors, metrics
6. View distributed traces showing complete flow from browser to database

### Verify Web Vitals

1. In Elastic APM, go to a frontend service
2. Look for transactions of type `web-vital`
3. Verify metrics meet thresholds (LCP < 2.5s, INP < 200ms, CLS < 0.1, etc.)

## What Gets Tracked Automatically

**Page Loads**
- Full page load time
- DOM content loaded
- Time to interactive
- Resource loading (CSS, JS, images, fonts)

**Route Changes (Next.js)**
- Client-side navigation timing
- Route transition performance

**API Calls**
- All fetch() and XMLHttpRequest calls
- Request/response timing
- HTTP status codes
- Errors and timeouts
- Distributed trace context propagation

**Errors**
- Uncaught JavaScript exceptions
- Promise rejections
- Network errors
- Stack traces with source maps

**User Interactions**
- Click events
- Form submissions
- Can be extended for custom interactions

## Performance Impact

- APM RUM Agent: ~15-20KB gzipped
- OpenTelemetry SDK: ~30-35KB gzipped (currently disabled)
- Web Vitals Library: ~1KB gzipped
- Total: ~20-25KB additional JavaScript
- Runtime Overhead: < 5ms per page load (negligible)

## Sampling

Control the percentage of traces sent to Elastic:

```bash
# In .env
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0  # 100% (dev/staging)
NEXT_PUBLIC_TRACING_SAMPLING_RATE=0.1  # 10% (production, high traffic)
```

## Disabling Observability

To disable for a specific frontend:

```tsx
<ObservabilityProvider serviceName="slms-admin-console" enabled={false}>
  {children}
</ObservabilityProvider>
```

Or remove the `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL` environment variable.

## Troubleshooting

### No Data in Elastic APM

1. Check browser console for initialization messages
2. Verify environment variables are set correctly
3. Check network tab for requests to Elastic APM endpoint
4. Verify Elastic APM server URL is accessible from browser
5. Check sampling rate (might be set too low)

### Distributed Traces Not Connecting

1. Verify CORS is configured on backend
2. Check `distributedTracingOrigins` in APM config matches API URL
3. Verify backend APM agent is running and configured
4. Check traceparent headers in network tab

### High Performance Impact

1. Reduce sampling rate in production
2. Disable OpenTelemetry (keep only Elastic APM RUM)
3. Limit user interaction tracking
4. Increase batch size for OTEL exporter

## Integration with Authentication

Add user context tracking when users log in:

```typescript
// After successful login
import { setUserContext } from '@shared/lib/observability';

setUserContext({
  id: user.id,
  username: user.username,
  email: user.email,
});
```

## References

- [Elastic APM RUM Documentation](https://www.elastic.co/guide/en/apm/agent/rum-js/current/index.html)
- [OpenTelemetry Browser SDK](https://opentelemetry.io/docs/instrumentation/js/)
- [Web Vitals](https://web.dev/vitals/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)
- [ADR-0005: Observability Architecture](../adr/0005-observability-monitoring-strategy.md)
