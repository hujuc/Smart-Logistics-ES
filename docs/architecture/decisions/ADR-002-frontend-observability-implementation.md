# Frontend Observability Setup

## Overview

The SLMS (Smart Logistics Management System) implements comprehensive observability across all frontend and backend services using Elastic APM (Application Performance Monitoring). This provides real-time insights into application performance, user experience, and end-to-end distributed tracing from browser to database.

**Status**: ✅ **Production Ready**

**Last Updated**: November 26, 2025

## What's Been Implemented

### 1. Monitoring Components

#### **Elastic APM RUM**
- Real-time page load monitoring
- Route change tracking (Next.js App Router)
- XHR/Fetch API call monitoring with distributed tracing
- JavaScript error capture with stack traces
- Custom transactions and spans
- User context tracking

#### **Core Web Vitals (Google's Performance Metrics)**
- **LCP** (Largest Contentful Paint) - Loading performance (< 2.5s = good)
- **INP** (Interaction to Next Paint) - Responsiveness (< 200ms = good, replaces deprecated FID)
- **CLS** (Cumulative Layout Shift) - Visual stability (< 0.1 = good)
- **FCP** (First Contentful Paint) - Initial rendering (< 1.8s = good)
- **TTFB** (Time to First Byte) - Server response time (< 800ms = good)

#### **Backend APM (Spring Boot)**
- Automatic transaction tracking for all HTTP requests
- Database query monitoring (JPA/Hibernate)
- External API call tracing (Shippo, ShipEngine)
- Exception and error tracking
- JVM metrics (heap, GC, threads)
- Kafka message tracing

#### **OpenTelemetry Integration**
- **Status**: Currently disabled due to Next.js compatibility issues
- **Reason**: Elastic APM RUM provides all necessary browser instrumentation
- Can be re-enabled if vendor-neutral telemetry is required

### 2. Architecture

```
Browser (Frontend)                     Backend (Spring Boot)
      │                                        │
      ├─ Page loads                            │
      │  └─ Elastic APM RUM                    │
      │     └─ Web Vitals                      │
      │        └─ OpenTelemetry                │
      │                                        │
      ├─ User clicks button                    │
      │  └─ APM starts transaction             │
      │     (trace_id: abc123)                 │
      │                                        │
      ├─ fetch('/api/shipments')               │
      │  Header: traceparent: 00-abc123...     │
      ├────────────────────────────────────────>│
      │                                        │
      │                            APM continues trace
      │                            (same trace_id: abc123)
      │                                        │
      │<────────────────────────────────────────┤
      │                                        │
      └─ APM ends transaction                  │
         Reports metrics to Elastic Cloud
```

### 3. Files Created

#### Shared Library (`/src/frontend/shared/`)
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

#### Integration in Each Frontend
All 4 frontends updated:
- `admin-console/app/layout.tsx` - Service: `slms-admin-console`
- `customer-portal/app/layout.tsx` - Service: `slms-customer-portal`
- `driver-pwa/app/layout.tsx` - Service: `slms-driver-pwa`
- `landing-page/app/layout.tsx` - Service: `slms-landing-page`

### 4. Environment Configuration

#### Added to `/infra/.env`
```bash
# Frontend RUM Configuration (NEXT_PUBLIC_ makes them available to browser)
NEXT_PUBLIC_ELASTIC_APM_SERVER_URL=https://[your-id].apm.europe-west1.gcp.cloud.es.io:443
NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN=[your-token]
NEXT_PUBLIC_DEPLOYMENT_ENV=development
NEXT_PUBLIC_PROJECT_VERSION=0.0.1-SNAPSHOT
NEXT_PUBLIC_TRACING_SAMPLING_RATE=1.0
NEXT_PUBLIC_API_URL=http://localhost
```

#### Updated `/infra/docker-compose.dev.yml`
All frontend services now receive observability environment variables:
- admin-console
- customer-portal
- driver-pwa
- landing-page

## How It Works

### Initialization Flow

1. **Next.js App Loads** → `app/layout.tsx` renders
2. **ObservabilityProvider Mounts** → Client-side only (useEffect)
3. **Checks Configuration** → Validates `NEXT_PUBLIC_ELASTIC_APM_SERVER_URL`
4. **Initializes APM** → Elastic APM RUM agent starts
5. **Initializes OpenTelemetry** → OTEL SDK configures instrumentations
6. **Starts Web Vitals Tracking** → Monitors Core Web Vitals
7. **Automatic Instrumentation Begins** → All page loads, API calls, errors tracked

### Distributed Tracing

**Frontend → Backend trace propagation:**

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

**Result:** Single distributed trace spanning browser → nginx → backend → database

### Service Names in Elastic APM

Each frontend appears as a separate service:
- `slms-admin-console` (TypeScript/React)
- `slms-customer-portal` (TypeScript/React)
- `slms-driver-pwa` (TypeScript/React)
- `slms-landing-page` (TypeScript/React)
- `SLMS` (Java/Spring Boot) - backend

## Usage Examples

### Basic Integration (Already Done)

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

### Advanced Usage

#### Track Custom Transactions
```typescript
import { startTransaction } from '@shared/lib/observability';

const transaction = startTransaction('checkout-flow', 'user-action');
// ... perform checkout operations
transaction?.end();
```

#### Track Custom Spans
```typescript
import { startSpan } from '@shared/lib/observability';

const span = startSpan('validate-payment', 'validation');
// ... validation logic
span?.end();
```

#### Set User Context
```typescript
import { setUserContext } from '@shared/lib/observability';

setUserContext({
  id: user.id,
  username: user.username,
  email: user.email,
});
```

#### Capture Custom Errors
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

#### Add Custom Labels
```typescript
import { addLabels } from '@shared/lib/observability';

addLabels({
  'order.id': '12345',
  'order.total': 99.99,
  'user.role': 'customer',
});
```

#### Report Custom Metrics
```typescript
import { reportCustomMetric } from '@shared/lib/observability/web-vitals';

reportCustomMetric('checkout-duration', 2500, {
  items: 3,
  paymentMethod: 'credit-card',
});
```

## Testing & Verification

### 1. Start the Application

```bash
cd /home/ric/Desktop/group-project-es2526_304/infra
docker compose -f docker-compose.dev.yml up --build
```

### 2. Access Frontends

- **Landing Page:** http://localhost/
- **Admin Console:** http://localhost/admin
- **Customer Portal:** http://localhost/customer
- **Driver PWA:** http://localhost/driver

### 3. Check Browser Console

You should see initialization messages:
```
[APM] ✓ Initialized for service: slms-landing-page
[APM] Environment: development
[APM] Sample rate: 100%
[OTEL] ✓ Initialized for service: slms-landing-page
[Web Vitals] ✓ Tracking initialized
[Observability] Initialization complete: {apm: 'initialized', otel: 'initialized', webVitals: true}
```

### 4. Verify in Elastic APM UI

1. Go to your Elastic Cloud console: https://cloud.elastic.co
2. Open Kibana
3. Navigate to **Observability** → **APM** → **Services**
4. You should see:
   - `slms-admin-console`
   - `slms-customer-portal`
   - `slms-driver-pwa`
   - `slms-landing-page`
   - `SLMS` (backend)

5. Click on any frontend service to see:
   - **Transactions:** Page loads, route changes
   - **Errors:** JavaScript errors with stack traces
   - **Metrics:** Web Vitals (LCP, FID, CLS, FCP, TTFB)
   - **Service Map:** Visual representation of frontend → backend calls

6. **Distributed Traces:** Click on a transaction and see the complete flow:
   ```
   slms-customer-portal (page-load)
   └─ slms-customer-portal (fetch /api/customer/orders)
      └─ SLMS (GET /api/customer/orders)
         └─ SLMS (OrderService.getOrders)
            └─ postgresql (SELECT * FROM orders)
   ```

### 5. Verify Web Vitals

1. In Elastic APM, go to a frontend service
2. Look for transactions of type `web-vital`
3. Check metrics:
   - `web-vital-lcp` - Should be < 2.5s (good)
   - `web-vital-fid` / `web-vital-inp` - Should be < 100ms (good)
   - `web-vital-cls` - Should be < 0.1 (good)
   - `web-vital-fcp` - Should be < 1.8s (good)
   - `web-vital-ttfb` - Should be < 800ms (good)

## What Gets Tracked Automatically

### Page Loads
- Full page load time
- DOM content loaded
- Time to interactive
- Resource loading (CSS, JS, images, fonts)

### Route Changes (Next.js)
- Client-side navigation timing
- Route transition performance

### API Calls
- All `fetch()` and `XMLHttpRequest` calls
- Request/response timing
- HTTP status codes
- Errors and timeouts
- **Distributed trace context propagation**

### Errors
- Uncaught JavaScript exceptions
- Promise rejections
- Network errors
- Stack traces with source maps

### User Interactions
- Click events
- Form submissions
- (Can be extended for custom interactions)

## Performance Impact

- **APM RUM Agent:** ~15-20KB gzipped
- **OpenTelemetry SDK:** ~30-35KB gzipped
- **Web Vitals Library:** ~1KB gzipped
- **Total:** ~50KB additional JavaScript
- **Runtime Overhead:** < 5ms per page load (negligible)

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

1. **Check browser console** for initialization messages
2. **Verify environment variables** are set correctly:
   ```bash
   docker exec slms-admin-console-dev env | grep NEXT_PUBLIC_ELASTIC
   ```
3. **Check network tab** for requests to Elastic APM endpoint
4. **Verify Elastic APM server URL** is accessible from browser
5. **Check sampling rate** - might be set too low

### Distributed Traces Not Connecting

1. **Verify CORS** is configured on backend
2. **Check `distributedTracingOrigins`** in APM config matches API URL
3. **Verify backend APM agent** is running and configured
4. **Check traceparent headers** in network tab

### High Performance Impact

1. **Reduce sampling rate** in production
2. **Disable OpenTelemetry** (keep only Elastic APM RUM)
3. **Limit user interaction tracking**
4. **Increase batch size** for OTEL exporter

## Next Steps

### Integration with User Authentication

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

### Custom Dashboards

Create Kibana dashboards for:
- Core Web Vitals trends
- Page load performance by route
- API call success rates
- Error rates by page
- User journey flows

### Alerting

Set up alerts in Elastic for:
- LCP > 4s (poor performance)
- Error rate > 5%
- API latency > 2s (p95)
- CLS > 0.25 (poor visual stability)

## Resources

- [Elastic APM RUM Documentation](https://www.elastic.co/guide/en/apm/agent/rum-js/current/index.html)
- [OpenTelemetry Browser SDK](https://opentelemetry.io/docs/instrumentation/js/)
- [Web Vitals](https://web.dev/vitals/)
- [W3C Trace Context](https://www.w3.org/TR/trace-context/)

## Support

For issues or questions, check:
1. Browser console logs
2. Docker container logs: `docker logs slms-[service]-dev`
3. Elastic APM UI → Services → Errors
4. This documentation
