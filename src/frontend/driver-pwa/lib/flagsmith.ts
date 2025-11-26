/**
 * Flagsmith Configuration for Driver PWA
 * Feature flags for gradual rollouts and A/B testing of driver features
 *
 * This system allows us to:
 * - Enable/disable features without deployment (Feature Toggles)
 * - Gradually roll out features to percentage of drivers (Release Toggles)
 * - A/B test different implementations (Multivariate Toggles)
 */

import flagsmith from 'flagsmith';

// Flagsmith Environment Key
// Get this from: Flagsmith Dashboard -> Environments -> Client-side Environment Key
const FLAGSMITH_ENV_KEY = process.env.NEXT_PUBLIC_FLAGSMITH_ENV_KEY || '';

// Use Flagsmith API in production, fallback to in-memory for development
const USE_FLAGSMITH_API = FLAGSMITH_ENV_KEY.length > 0;

// Feature flags interface for Driver PWA
interface FeatureFlags {
  // ========================================
  // FEATURE TOGGLES (On/Off switches)
  // ========================================

  /**
   * Master switch for Proof of Delivery (POD) feature
   * When false: Scan Package button is hidden/disabled
   * When true: Drivers can scan packages and upload POD photos
   */
  'proof-of-delivery-enabled': boolean;

  /**
   * Enable photo compression before upload
   * Phase 1 (false): Upload original photos (may be large)
   * Phase 2 (true): Compress photos to reduce bandwidth/storage
   */
  'pod-photo-compression-enabled': boolean;

  /**
   * Require GPS location when uploading POD
   * When false: Location is optional
   * When true: Location is mandatory (helps verify delivery location)
   */
  'pod-require-location': boolean;

  // ========================================
  // RELEASE TOGGLES (Gradual rollout)
  // ========================================

  /**
   * Percentage of drivers who can see the POD feature
   * 0 = Nobody sees it (testing in dev only)
   * 10 = 10% of drivers see it (alpha testing)
   * 50 = 50% of drivers see it (beta testing)
   * 100 = Everyone sees it (full release)
   */
  'pod-rollout-percentage': number;
}

/**
 * Default flag values
 * These are used when flags haven't been set yet
 */
const defaultFlags: FeatureFlags = {
  // Feature toggles - Start with POD enabled for development
  'proof-of-delivery-enabled': true,
  'pod-photo-compression-enabled': false, // Disable compression initially
  'pod-require-location': false, // Don't require location initially

  // Release toggle - Start with 100% rollout for development
  // In production, you'd start at 0-10% and gradually increase
  'pod-rollout-percentage': 100,
};

/**
 * Feature Flag Manager
 * Manages feature flags for the driver PWA
 * In production, this would integrate with Flagsmith API
 */
class FeatureFlagManager {
  private flags: FeatureFlags;
  private userId: string | null = null;

  constructor() {
    // Initialize with default flags
    this.flags = { ...defaultFlags };

    // Load from localStorage if available (for consistency across page loads)
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('driverPWAFeatureFlags');
      if (stored) {
        try {
          const parsedFlags = JSON.parse(stored);
          this.flags = { ...this.flags, ...parsedFlags };
        } catch (e) {
          console.error('[Flagsmith] Failed to parse stored flags:', e);
        }
      }
    }
  }

  /**
   * Initialize feature flags for a specific driver
   * In production, this would fetch flags from Flagsmith API based on driver ID
   *
   * @param driverId - The unique identifier for the driver
   * @param driverTraits - Optional driver traits for advanced targeting (e.g., region, experience level)
   */
  async initialize(driverId: string, driverTraits?: Record<string, string | number | boolean>): Promise<void> {
    this.userId = driverId;

    console.log(`[Flagsmith] Initializing flags for driver: ${driverId}`);

    if (USE_FLAGSMITH_API) {
      // Use real Flagsmith API
      try {
        console.log('[Flagsmith] Using Flagsmith API');

        await flagsmith.init({
          environmentID: FLAGSMITH_ENV_KEY,
          identity: driverId,
          traits: driverTraits,
          onChange: (oldFlags, params) => {
            console.log('[Flagsmith] Flags updated from API:', params.flagsChanged);
            this.syncFromFlagsmith();
          },
        });

        // Sync flags from Flagsmith to our local state
        this.syncFromFlagsmith();

        console.log('[Flagsmith] Successfully initialized with API');
      } catch (error) {
        console.error('[Flagsmith] API initialization failed, falling back to defaults:', error);
        // Continue with default flags on error
      }
    } else {
      // Use in-memory fallback (development mode)
      console.log('[Flagsmith] Using in-memory flags (no API key configured)');

      // For demo: Assign flags based on driver ID hash
      const hash = this.hashUserId(driverId);

      // Determine rollout percentage for this driver
      const rolloutPercentage = this.flags['pod-rollout-percentage'];
      const userBucket = hash % 100; // Driver falls into bucket 0-99
      const isInRollout = userBucket < rolloutPercentage;

      if (!isInRollout) {
        // Driver is not in rollout - disable feature
        this.flags['proof-of-delivery-enabled'] = false;
        console.log(`[Flagsmith] Driver ${driverId} not in rollout (bucket: ${userBucket}, rollout: ${rolloutPercentage}%)`);
      } else {
        console.log(`[Flagsmith] Driver ${driverId} IN rollout (bucket: ${userBucket}, rollout: ${rolloutPercentage}%)`);
      }
    }

    // Store in localStorage for consistency
    if (typeof window !== 'undefined') {
      localStorage.setItem('driverPWAFeatureFlags', JSON.stringify(this.flags));
    }

    // Log assigned flags
    console.log('[Flagsmith] Flags assigned:', {
      podEnabled: this.flags['proof-of-delivery-enabled'],
      rolloutPercentage: this.flags['pod-rollout-percentage'],
      photoCompression: this.flags['pod-photo-compression-enabled'],
      requireLocation: this.flags['pod-require-location'],
    });
  }

  /**
   * Sync flags from Flagsmith API to local state
   * Called after Flagsmith initialization and on flag changes
   */
  private syncFromFlagsmith(): void {
    if (!USE_FLAGSMITH_API) return;

    console.log('[Flagsmith] Syncing flags from API...');

    // Sync boolean flags - use getValue() and check if it's enabled AND true
    this.flags['proof-of-delivery-enabled'] =
      flagsmith.hasFeature('proof-of-delivery-enabled') &&
      flagsmith.getValue('proof-of-delivery-enabled') === true;

    this.flags['pod-photo-compression-enabled'] =
      flagsmith.hasFeature('pod-photo-compression-enabled') &&
      flagsmith.getValue('pod-photo-compression-enabled') === true;

    this.flags['pod-require-location'] =
      flagsmith.hasFeature('pod-require-location') &&
      flagsmith.getValue('pod-require-location') === true;

    // Sync numeric flag (rollout percentage)
    const rolloutValue = flagsmith.getValue('pod-rollout-percentage');
    this.flags['pod-rollout-percentage'] = rolloutValue
      ? parseInt(rolloutValue as string, 10)
      : 100;

    console.log('[Flagsmith] Synced flags:', this.flags);
  }

  /**
   * Get feature flag value
   * @param flagName - Name of the flag to retrieve
   * @returns The flag value
   */
  getFlag<K extends keyof FeatureFlags>(flagName: K): FeatureFlags[K] {
    return this.flags[flagName];
  }

  /**
   * Check if a boolean feature is enabled
   * @param flagName - Name of the boolean flag
   * @returns true if enabled, false otherwise
   */
  isEnabled(flagName: keyof FeatureFlags): boolean {
    const value = this.flags[flagName];
    return value === true;
  }

  /**
   * Manually set a flag value (for testing/demo purposes)
   * In production, flags should only be changed via Flagsmith dashboard
   *
   * @param flagName - Name of the flag to set
   * @param value - New value for the flag
   */
  setFlag<K extends keyof FeatureFlags>(flagName: K, value: FeatureFlags[K]): void {
    this.flags[flagName] = value;

    if (typeof window !== 'undefined') {
      localStorage.setItem('driverPWAFeatureFlags', JSON.stringify(this.flags));
    }

    console.log(`[Flagsmith] Flag '${flagName}' manually set to:`, value);
  }

  /**
   * Check if driver is in rollout based on rollout percentage
   * This is useful for progressive rollouts
   *
   * @returns true if driver should see the feature, false otherwise
   */
  isInRollout(): boolean {
    if (!this.userId) {
      console.warn('[Flagsmith] Driver ID not set, assuming in rollout');
      return true;
    }

    const rolloutPercentage = this.flags['pod-rollout-percentage'];
    const hash = this.hashUserId(this.userId);
    const userBucket = hash % 100;

    return userBucket < rolloutPercentage;
  }

  /**
   * Hash function to consistently assign drivers to buckets
   * Same driver ID always produces same hash
   *
   * @param driverId - Driver ID to hash
   * @returns Hash value
   */
  private hashUserId(driverId: string): number {
    let hash = 0;
    for (let i = 0; i < driverId.length; i++) {
      const char = driverId.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32bit integer
    }
    return Math.abs(hash);
  }

  /**
   * Reset all flags to default values
   * Useful for testing or clearing driver state
   */
  reset(): void {
    this.flags = { ...defaultFlags };
    this.userId = null;

    if (typeof window !== 'undefined') {
      localStorage.removeItem('driverPWAFeatureFlags');
    }

    console.log('[Flagsmith] Flags reset to defaults');
  }

  /**
   * Get current driver ID
   */
  getUserId(): string | null {
    return this.userId;
  }
}

// ========================================
// Singleton instance and exports
// ========================================

/**
 * Singleton instance of the feature flag manager
 * Import and use this throughout the application
 */
export const featureFlagManager = new FeatureFlagManager();

/**
 * Hook-like function to get feature flag value
 * Use this in components for cleaner code
 *
 * @example
 * const compressionEnabled = useFeatureFlag('pod-photo-compression-enabled')
 * if (compressionEnabled) { ... }
 */
export function useFeatureFlag<K extends keyof FeatureFlags>(
  flagName: K
): FeatureFlags[K] {
  return featureFlagManager.getFlag(flagName);
}

/**
 * Check if Proof of Delivery feature is enabled for current driver
 * Convenience function for the most common check
 *
 * @example
 * if (isProofOfDeliveryEnabled()) {
 *   return <ScanPackageButton />
 * } else {
 *   return <ComingSoonMessage />
 * }
 */
export function isProofOfDeliveryEnabled(): boolean {
  return featureFlagManager.isEnabled('proof-of-delivery-enabled');
}

// Export the FeatureFlags type for use in other files
export type { FeatureFlags };
