/**
 * Elastic APM Real User Monitoring (RUM) Configuration
 *
 * Captures:
 * - Page load times
 * - Route changes (Next.js App Router)
 * - API calls (XHR/Fetch) with distributed tracing
 * - JavaScript errors
 * - User interactions
 * - Custom transactions and spans
 */

import { init as initApm, ApmBase } from '@elastic/apm-rum';
import type { ObservabilityConfig } from './types';

let apmInstance: ApmBase | null = null;
let apmConfig: ObservabilityConfig | null = null;

/**
 * Initialize Elastic APM RUM agent
 * Should be called once at application startup (client-side only)
 */
export function initializeApm(config: ObservabilityConfig): ApmBase | null {
  // Only run in browser
  if (typeof window === 'undefined') {
    console.warn('[APM] Skipping initialization - server-side environment');
    return null;
  }

  // Return existing instance if already initialized
  if (apmInstance) {
    console.log('[APM] Already initialized');
    return apmInstance;
  }

  // Check if APM is configured
  const serverUrl = config.serverUrl || process.env.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL;
  const secretToken = config.secretToken || process.env.NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN;

  console.log('[APM] Configuration check:', {
    serverUrl: serverUrl ? 'SET' : 'NOT SET',
    secretToken: secretToken ? 'SET' : 'NOT SET',
    serviceName: config.serviceName,
    enabled: config.enabled !== false,
  });

  if (!serverUrl) {
    console.warn('[APM] Server URL not configured. Skipping APM initialization.');
    return null;
  }

  if (config.enabled === false) {
    console.log('[APM] Explicitly disabled via config');
    return null;
  }

  try {
    const sampleRate = config.sampleRate ??
      parseFloat(process.env.NEXT_PUBLIC_TRACING_SAMPLING_RATE || '1.0');

    // Store config for later retrieval
    apmConfig = config;

    apmInstance = initApm({
      // Required
      serviceName: config.serviceName,
      serverUrl,

      // Authentication
      ...(secretToken && { secretToken }),

      // Environment info
      environment: config.environment ||
        process.env.NEXT_PUBLIC_DEPLOYMENT_ENV ||
        'development',
      serviceVersion: config.serviceVersion ||
        process.env.NEXT_PUBLIC_PROJECT_VERSION ||
        '0.0.1',

      // Distributed tracing - automatically propagate trace context to backend
      distributedTracingOrigins: config.distributedTracingOrigins || [
        'http://localhost',
        'http://localhost:8080',
        typeof window !== 'undefined' ? window.location.origin : '',
      ].filter(Boolean),

      // Performance monitoring
      breakdownMetrics: true,

      // Capture settings
      active: true,
      instrument: true,
      disableInstrumentations: [],

      // Transaction settings
      pageLoadTraceId: undefined, // Auto-generate
      pageLoadSpanId: undefined,   // Auto-generate
      pageLoadSampled: true,

      // Error handling
      errorThrottleLimit: 20,
      errorThrottleInterval: 30000, // 30 seconds

      // Sampling
      transactionSampleRate: sampleRate,

      // Context propagation (W3C Trace Context standard)
      propagateTracestate: true,

      // Logging
      logLevel: process.env.NODE_ENV === 'development' ? 'debug' : 'warn',
    });

    console.log(`[APM] ✓ Initialized for service: ${config.serviceName}`);
    console.log(`[APM] Environment: ${config.environment || process.env.NEXT_PUBLIC_DEPLOYMENT_ENV || 'development'}`);
    console.log(`[APM] Sample rate: ${sampleRate * 100}%`);

    return apmInstance;
  } catch (error) {
    console.error('[APM] Failed to initialize:', error);
    return null;
  }
}

/**
 * Get the current APM instance
 */
export function getApm(): ApmBase | null {
  return apmInstance;
}

/**
 * Get the APM configuration
 * Returns the config used to initialize APM
 */
export function getApmConfig(): ObservabilityConfig | null {
  return apmConfig;
}

/**
 * Start a custom transaction
 * Useful for tracking specific user flows or operations
 */
export function startTransaction(name: string, type = 'custom') {
  const apm = getApm();
  if (!apm) return null;

  return apm.startTransaction(name, type, {
    managed: true,
  });
}

/**
 * Start a custom span within the current transaction
 * Useful for tracking specific operations within a transaction
 */
export function startSpan(name: string, type = 'custom') {
  const apm = getApm();
  if (!apm) return null;

  return apm.startSpan(name, type);
}

/**
 * Set user context for APM
 * Associates transactions with a specific user
 */
export function setUserContext(user: { id?: string; username?: string; email?: string }) {
  const apm = getApm();
  if (!apm) return;

  apm.setUserContext(user);
  console.log('[APM] User context set:', user.id || user.username || user.email);
}

/**
 * Add custom context to current transaction
 */
export function addContext(key: string, value: unknown) {
  const apm = getApm();
  if (!apm) return;

  const transaction = apm.getCurrentTransaction();
  if (transaction) {
    transaction.addContext({ [key]: value });
  }
}

/**
 * Capture a custom error
 */
export function captureError(error: Error | string, labels?: Record<string, string | number | boolean>) {
  const apm = getApm();
  if (!apm) {
    console.error('[APM] Cannot capture error - APM not initialized:', error);
    return;
  }

  apm.captureError(error, labels ? { labels } : undefined);
  console.log('[APM] Error captured:', error);
}

/**
 * Add labels/tags to current transaction
 */
export function addLabels(labels: Record<string, string | number | boolean>) {
  const apm = getApm();
  if (!apm) return;

  const transaction = apm.getCurrentTransaction();
  if (transaction) {
    transaction.addLabels(labels);
  }
}

/**
 * Mark backend correlation for distributed tracing
 * This adds custom labels to help correlate frontend and backend transactions
 */
export function markBackendCall(endpoint: string, method: string) {
  const apm = getApm();
  if (!apm) return;

  const transaction = apm.getCurrentTransaction();
  if (transaction) {
    transaction.addLabels({
      'http.method': method,
      'http.url': endpoint,
      'span.type': 'api-call',
    });
  }
}
