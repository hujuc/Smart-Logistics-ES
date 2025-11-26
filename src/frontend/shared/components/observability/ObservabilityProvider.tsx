'use client';

/**
 * ObservabilityProvider Component
 *
 * Client-side component that initializes Elastic APM RUM, OpenTelemetry,
 * and Web Vitals tracking for Next.js applications.
 *
 * Usage:
 * ```tsx
 * <ObservabilityProvider serviceName="slms-admin-console">
 *   {children}
 * </ObservabilityProvider>
 * ```
 */

import { useEffect, type ReactNode } from 'react';
import { initializeObservability } from '@shared/lib/observability';
import type { ObservabilityConfig } from '@shared/lib/observability/types';

interface ObservabilityProviderProps {
  children: ReactNode;
  serviceName: string;
  enabled?: boolean;
  config?: Partial<ObservabilityConfig>;
}

export function ObservabilityProvider({
  children,
  serviceName,
  enabled = true,
  config = {},
}: ObservabilityProviderProps) {
  useEffect(() => {
    // Only initialize in browser
    if (typeof window === 'undefined') {
      return;
    }

    // Skip if explicitly disabled
    if (!enabled) {
      console.log('[ObservabilityProvider] Disabled via props');
      return;
    }

    // Check if APM server URL is configured
    if (!process.env.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL) {
      console.warn(
        '[ObservabilityProvider] NEXT_PUBLIC_ELASTIC_APM_SERVER_URL not set. Skipping initialization.'
      );
      return;
    }

    // Initialize observability
    const observabilityConfig: ObservabilityConfig = {
      serviceName,
      enabled,
      ...config,
    };

    try {
      initializeObservability(observabilityConfig);
    } catch (error) {
      console.error('[ObservabilityProvider] Initialization failed:', error);
    }

    // Cleanup on unmount (optional, browser usually handles this)
    return () => {
      console.log('[ObservabilityProvider] Unmounting');
    };
  }, [serviceName, enabled, config]);

  return <>{children}</>;
}
