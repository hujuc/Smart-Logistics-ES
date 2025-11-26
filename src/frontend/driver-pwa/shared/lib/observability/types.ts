/**
 * TypeScript types for observability configuration
 */

export interface ObservabilityConfig {
  serviceName: string;
  serverUrl?: string;
  secretToken?: string;
  environment?: string;
  serviceVersion?: string;
  distributedTracingOrigins?: string[];
  enabled?: boolean;
  sampleRate?: number;
}

export interface WebVitalsMetric {
  name: 'CLS' | 'LCP' | 'FCP' | 'TTFB' | 'INP';
  value: number;
  rating: 'good' | 'needs-improvement' | 'poor';
  delta: number;
  id: string;
  navigationType: string;
}
