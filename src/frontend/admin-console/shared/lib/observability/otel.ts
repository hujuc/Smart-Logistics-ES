/**
 * OpenTelemetry Browser Instrumentation
 *
 * Provides additional instrumentation beyond Elastic APM RUM:
 * - Document load events
 * - User interactions (clicks, inputs)
 * - Fetch/XHR with trace propagation
 * - Resource timing
 *
 * Note: This is complementary to Elastic APM RUM and provides
 * additional detailed metrics for performance analysis.
 */

import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { BatchSpanProcessor, SpanProcessor, ReadableSpan } from '@opentelemetry/sdk-trace-base';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { DocumentLoadInstrumentation } from '@opentelemetry/instrumentation-document-load';
import { UserInteractionInstrumentation } from '@opentelemetry/instrumentation-user-interaction';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request';
import type { ObservabilityConfig } from './types';
import { Span } from '@opentelemetry/api';

let tracerProvider: WebTracerProvider | null = null;

/**
 * Custom span processor to add service metadata to all spans
 * This is the browser-compatible way to add resource attributes
 */
class ServiceAttributesSpanProcessor implements SpanProcessor {
  private attributes: Record<string, string>;

  constructor(attributes: Record<string, string>) {
    this.attributes = attributes;
  }

  onStart(span: Span): void {
    // Add service attributes to every span
    Object.entries(this.attributes).forEach(([key, value]) => {
      span.setAttribute(key, value);
    });
  }

  onEnd(_span: ReadableSpan): void {
    // No-op
  }

  shutdown(): Promise<void> {
    return Promise.resolve();
  }

  forceFlush(): Promise<void> {
    return Promise.resolve();
  }
}

/**
 * Initialize OpenTelemetry browser instrumentation
 * Should be called once at application startup (client-side only)
 */
export function initializeOpenTelemetry(config: ObservabilityConfig): WebTracerProvider | null {
  // Only run in browser
  if (typeof window === 'undefined') {
    console.warn('[OTEL] Skipping initialization - server-side environment');
    return null;
  }

  // Return existing instance if already initialized
  if (tracerProvider) {
    console.log('[OTEL] Already initialized');
    return tracerProvider;
  }

  // Check if OTEL is configured
  const serverUrl = config.serverUrl || process.env.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL;
  const secretToken = config.secretToken || process.env.NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN;

  if (!serverUrl) {
    console.warn('[OTEL] Server URL not configured. Skipping OpenTelemetry initialization.');
    return null;
  }

  if (config.enabled === false) {
    console.log('[OTEL] Explicitly disabled via config');
    return null;
  }

  try {
    // Service metadata as span attributes (browser-compatible approach)
    const serviceAttributes = {
      'service.name': config.serviceName,
      'service.version': config.serviceVersion ||
        process.env.NEXT_PUBLIC_PROJECT_VERSION ||
        '0.0.1',
      'deployment.environment': config.environment ||
        process.env.NEXT_PUBLIC_DEPLOYMENT_ENV ||
        'development',
      'telemetry.sdk.language': 'javascript',
      'telemetry.sdk.name': 'opentelemetry',
    };

    // Configure OTLP exporter to send to Elastic APM
    const exporter = new OTLPTraceExporter({
      url: `${serverUrl}/intake/v2/rum/events`, // Elastic APM RUM endpoint
      headers: {
        ...(secretToken && { 'Authorization': `Bearer ${secretToken}` }),
        'Content-Type': 'application/json',
      },
    });

    // Create span processors (required before provider initialization in v2.x)
    const spanProcessors: SpanProcessor[] = [
      // Custom processor to inject service attributes into all spans
      new ServiceAttributesSpanProcessor(serviceAttributes),
      // Batch processor for performance (batches spans before sending)
      new BatchSpanProcessor(exporter, {
        maxQueueSize: 100,
        maxExportBatchSize: 10,
        scheduledDelayMillis: 5000, // Send every 5 seconds
      }),
    ];

    // Create tracer provider with span processors (OpenTelemetry v2.x API)
    tracerProvider = new WebTracerProvider({
      spanProcessors,
    });

    // Register the provider
    tracerProvider.register();

    // Register auto-instrumentations
    registerInstrumentations({
      instrumentations: [
        // Document load timing
        new DocumentLoadInstrumentation({
          enabled: true,
        }),

        // User interaction tracking (clicks, etc.)
        new UserInteractionInstrumentation({
          enabled: true,
          eventNames: ['click', 'submit'], // Only track significant interactions
        }),

        // Fetch API instrumentation with trace propagation
        new FetchInstrumentation({
          enabled: true,
          propagateTraceHeaderCorsUrls: config.distributedTracingOrigins || [
            /http:\/\/localhost(:\d+)?/,
            new RegExp(window.location.origin),
          ],
          clearTimingResources: true,
        }),

        // XMLHttpRequest instrumentation
        new XMLHttpRequestInstrumentation({
          enabled: true,
          propagateTraceHeaderCorsUrls: config.distributedTracingOrigins || [
            /http:\/\/localhost(:\d+)?/,
            new RegExp(window.location.origin),
          ],
        }),
      ],
    });

    console.log(`[OTEL] ✓ Initialized for service: ${config.serviceName}`);
    return tracerProvider;
  } catch (error) {
    console.error('[OTEL] Failed to initialize:', error);
    return null;
  }
}

/**
 * Get the current tracer provider
 */
export function getTracerProvider(): WebTracerProvider | null {
  return tracerProvider;
}

/**
 * Shutdown OpenTelemetry gracefully
 * Call this when unmounting or during cleanup
 */
export async function shutdownOpenTelemetry(): Promise<void> {
  if (tracerProvider) {
    try {
      await tracerProvider.shutdown();
      console.log('[OTEL] Shutdown complete');
      tracerProvider = null;
    } catch (error) {
      console.error('[OTEL] Shutdown failed:', error);
    }
  }
}
