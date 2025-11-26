/**
 * Observability Module
 *
 * Centralized module for frontend observability using:
 * - Elastic APM RUM
 * - OpenTelemetry Browser SDK
 * - Core Web Vitals tracking
 */

export * from './types';
export * from './apm';
export * from './otel';
export * from './web-vitals';

export { initializeObservability } from './init';
