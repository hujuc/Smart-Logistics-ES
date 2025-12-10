/**
 * Unified Observability Initialization
 *
 * Single function to initialize all observability components:
 * - Elastic APM RUM
 * - OpenTelemetry
 * - Web Vitals tracking
 */

import { initializeApm } from './apm';
import { initializeOpenTelemetry } from './otel';
import { initializeWebVitals } from './web-vitals';
import type { ObservabilityConfig } from './types';

/**
 * Initialize all observability components
 *
 * @param config - Observability configuration
 * @returns Object with initialization status
 */
export function initializeObservability(config: ObservabilityConfig) {
  // Only run in browser
  if (typeof window === 'undefined') {
    console.warn('[Observability] Skipping initialization - server-side environment');
    return {
      apm: null,
      otel: null,
      webVitals: false,
    };
  }

  console.log('[Observability] Initializing for:', config.serviceName);

  // Initialize Elastic APM RUM (primary monitoring)
  const apm = initializeApm(config);

  // Initialize OpenTelemetry (additional instrumentation - optional)
  // Currently disabled due to Next.js compatibility issues
  // Elastic APM RUM provides comprehensive monitoring without it
  let otel = null;
  // Uncomment below to enable OpenTelemetry if needed:
  // try {
  //   otel = initializeOpenTelemetry(config);
  // } catch (error) {
  //   console.warn('[Observability] OpenTelemetry initialization failed (non-critical):', error);
  // }

  // Initialize Web Vitals tracking (requires APM to be initialized)
  let webVitals = false;
  if (apm) {
    try {
      initializeWebVitals();
      webVitals = true;
    } catch (error) {
      console.error('[Observability] Web Vitals initialization failed:', error);
    }
  }

  const summary = {
    apm: apm ? 'initialized' : 'disabled',
    otel: otel ? 'initialized' : 'disabled',
    webVitals: webVitals ? 'initialized' : 'disabled',
  };

  console.log('[Observability] Initialization complete:', summary);

  return {
    apm,
    otel,
    webVitals,
  };
}
