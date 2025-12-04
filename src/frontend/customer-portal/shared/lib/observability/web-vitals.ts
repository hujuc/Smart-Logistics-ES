/**
 * Core Web Vitals Tracking
 *
 * Captures and reports Core Web Vitals metrics to Elastic APM:
 * - LCP (Largest Contentful Paint) - Loading performance
 * - INP (Interaction to Next Paint) - Responsiveness (replaces deprecated FID)
 * - CLS (Cumulative Layout Shift) - Visual stability
 * - FCP (First Contentful Paint) - Initial rendering
 * - TTFB (Time to First Byte) - Server response time
 */

import { onCLS, onLCP, onFCP, onTTFB, onINP, type Metric } from 'web-vitals';
import { getApm } from './apm';
import type { WebVitalsMetric } from './types';

/**
 * Send web vital metric to Elastic APM as a custom transaction
 */
function sendToApm(metric: Metric) {
  const apm = getApm();
  if (!apm) return;

  try {
    // Create a custom transaction for the metric
    const transaction = apm.startTransaction(
      `web-vital-${metric.name.toLowerCase()}`,
      'web-vital',
      {
        startTime: metric.entries[0]?.startTime || 0,
      }
    );

    if (transaction) {
      // Add metric details as labels
      transaction.addLabels({
        'metric.name': metric.name,
        'metric.value': metric.value,
        'metric.rating': metric.rating,
        'metric.delta': metric.delta,
        'metric.id': metric.id,
        'metric.navigationType': metric.navigationType,
      });

      // Add as custom context
      transaction.addContext({
        webVital: {
          name: metric.name,
          value: metric.value,
          rating: metric.rating,
          delta: metric.delta,
          entries: metric.entries.length,
        },
      });

      // Set the duration based on the metric value
      transaction.end(metric.value);

      console.log(`[Web Vitals] ${metric.name}:`, {
        value: Math.round(metric.value),
        rating: metric.rating,
      });
    }
  } catch (error) {
    console.error(`[Web Vitals] Error sending ${metric.name}:`, error);
  }
}

/**
 * Initialize Core Web Vitals tracking
 * Should be called once in the browser
 */
export function initializeWebVitals() {
  if (typeof window === 'undefined') {
    return;
  }

  const apm = getApm();
  if (!apm) {
    console.warn('[Web Vitals] APM not initialized, skipping web vitals tracking');
    return;
  }

  try {
    // Track Largest Contentful Paint (Loading performance)
    onLCP(sendToApm, {
      reportAllChanges: false, // Only report final value
    });

    // Track Interaction to Next Paint (Responsiveness - replaces FID)
    onINP(sendToApm, {
      reportAllChanges: false,
    });

    // Track Cumulative Layout Shift (Visual stability)
    onCLS(sendToApm, {
      reportAllChanges: false,
    });

    // Track First Contentful Paint (Initial rendering)
    onFCP(sendToApm, {
      reportAllChanges: false,
    });

    // Track Time to First Byte (Server response time)
    onTTFB(sendToApm, {
      reportAllChanges: false,
    });

    console.log('[Web Vitals] ✓ Tracking initialized (LCP, INP, CLS, FCP, TTFB)');
  } catch (error) {
    console.error('[Web Vitals] Initialization failed:', error);
  }
}

/**
 * Manually report a custom performance metric
 */
export function reportCustomMetric(
  name: string,
  value: number,
  context?: Record<string, unknown>
) {
  const apm = getApm();
  if (!apm) return;

  try {
    const transaction = apm.startTransaction(`custom-metric-${name}`, 'performance');

    if (transaction) {
      transaction.addLabels({
        'metric.name': name,
        'metric.value': value,
      });

      if (context) {
        transaction.addContext({ customMetric: context });
      }

      transaction.end(value);
      console.log(`[Custom Metric] ${name}: ${value}`);
    }
  } catch (error) {
    console.error(`[Custom Metric] Error reporting ${name}:`, error);
  }
}
