/**
 * Flagsmith Configuration
 * Feature flags for A/B testing and gradual rollouts
 * 
 * Setup:
 * 1. Create account at https://flagsmith.com/
 * 2. Create project and get Environment Key
 * 3. Set NEXT_PUBLIC_FLAGSMITH_ENV_KEY in .env.local
 * 4. Create flags in Flagsmith dashboard:
 *    - order-lookup-version (String, values: "A" or "B")
 *    - carrier-view-mode (String, values: "table" or "cards")
 *    - ticket-visibility-mode (String, values: "all" or "assigned")
 */

import flagsmith from 'flagsmith';

interface FeatureFlags {
  'order-lookup-version': 'A' | 'B';
  'carrier-view-mode': 'table' | 'cards';
  'ticket-visibility-mode': 'all' | 'assigned';
  'proof-of-delivery-enabled': boolean;
}

const defaultFlags: FeatureFlags = {
  'order-lookup-version': 'A',
  'carrier-view-mode': 'table',
  'ticket-visibility-mode': 'all',
  'proof-of-delivery-enabled': true,
};

// Flagsmith Environment Key
// Get from: https://app.flagsmith.com/ → Your Project → Settings
const FLAGSMITH_ENV_KEY = process.env.NEXT_PUBLIC_FLAGSMITH_ENV_KEY || '';

/**
 * Feature Flag Manager with Flagsmith Integration
 */
class FeatureFlagManager {
  private flags: FeatureFlags;
  private userId: string | null = null;
  private initialized: boolean = false;
  private useFlagsmithAPI: boolean = false;

  constructor() {
    this.flags = { ...defaultFlags };
    
    // Check if Flagsmith API key is configured
    this.useFlagsmithAPI = FLAGSMITH_ENV_KEY.length > 0;
    
    if (!this.useFlagsmithAPI) {
      console.warn('[Flagsmith] No API key configured. Using demo mode with localStorage.');
      console.warn('[Flagsmith] Set NEXT_PUBLIC_FLAGSMITH_ENV_KEY in .env.local to use Flagsmith API.');
      
      // Load from localStorage for demo mode
      if (typeof window !== 'undefined') {
        const stored = localStorage.getItem('featureFlags');
        if (stored) {
          try {
            this.flags = { ...this.flags, ...JSON.parse(stored) };
          } catch (e) {
            console.error('Failed to parse stored flags:', e);
          }
        }
      }
    }
  }

  /**
   * Initialize with user information
   */
  async initialize(userId: string): Promise<void> {
    this.userId = userId;
    
    if (this.useFlagsmithAPI) {
      // Real Flagsmith API integration
      try {
        await flagsmith.init({
          environmentID: FLAGSMITH_ENV_KEY,
          identity: userId,
          onChange: (oldFlags, params) => {
            // Callback when flags change
            console.log('[Flagsmith] Flags updated:', params);
            this.loadFlagsFromFlagsmith();
          },
        });
        
        this.loadFlagsFromFlagsmith();
        this.initialized = true;
        console.log(`[Flagsmith] Initialized for user ${userId}`);
      } catch (error) {
        console.error('[Flagsmith] Failed to initialize:', error);
        console.warn('[Flagsmith] Falling back to demo mode');
        this.useFlagsmithAPI = false;
      }
    } else {
      // Demo mode: Simple hash-based assignment
      const hash = this.hashUserId(userId);
      this.flags['order-lookup-version'] = hash % 2 === 0 ? 'A' : 'B';
      this.flags['carrier-view-mode'] = hash % 2 === 0 ? 'table' : 'cards';
      // ticket-visibility-mode stays at default 'all' unless manually changed
      
      if (typeof window !== 'undefined') {
        localStorage.setItem('featureFlags', JSON.stringify(this.flags));
      }
      
      console.log(`[Flagsmith Demo] User ${userId} assigned flags:`, this.flags);
    }
  }

  /**
   * Load flags from Flagsmith API
   */
  private loadFlagsFromFlagsmith(): void {
    if (!this.useFlagsmithAPI) return;

    // Get flag values from Flagsmith
    const orderLookupVersion = flagsmith.getValue('order-lookup-version') as 'A' | 'B' | null;
    const carrierViewMode = flagsmith.getValue('carrier-view-mode') as 'table' | 'cards' | null;
    const ticketVisibilityMode = flagsmith.getValue('ticket-visibility-mode') as 'all' | 'assigned' | null;
    const podEnabled = flagsmith.hasFeature('proof-of-delivery-enabled') &&
      flagsmith.getValue('proof-of-delivery-enabled') === true;

    // Update local flags with values from Flagsmith, fallback to defaults
    this.flags['order-lookup-version'] = orderLookupVersion || defaultFlags['order-lookup-version'];
    this.flags['carrier-view-mode'] = carrierViewMode || defaultFlags['carrier-view-mode'];
    this.flags['ticket-visibility-mode'] = ticketVisibilityMode || defaultFlags['ticket-visibility-mode'];
    this.flags['proof-of-delivery-enabled'] = podEnabled;

    console.log('[Flagsmith] Loaded flags:', this.flags);
  }

  /**
   * Get feature flag value
   */
  getFlag<K extends keyof FeatureFlags>(flagName: K): FeatureFlags[K] {
    if (this.useFlagsmithAPI && this.initialized) {
      // Get fresh value from Flagsmith
      const value = flagsmith.getValue(flagName) as FeatureFlags[K] | null;
      return value || this.flags[flagName];
    }
    return this.flags[flagName];
  }

  /**
   * Check if feature is enabled
   */
  isEnabled(flagName: keyof FeatureFlags): boolean {
    return this.flags[flagName] === true;
  }

  /**
   * Manually set a flag (only works in demo mode)
   */
  setFlag<K extends keyof FeatureFlags>(flagName: K, value: FeatureFlags[K]): void {
    if (this.useFlagsmithAPI) {
      console.warn('[Flagsmith] Cannot manually set flags when using Flagsmith API.');
      console.warn('[Flagsmith] Please change flags in the Flagsmith dashboard.');
      return;
    }
    
    // Demo mode: Allow manual flag changes
    this.flags[flagName] = value;
    if (typeof window !== 'undefined') {
      localStorage.setItem('featureFlags', JSON.stringify(this.flags));
    }
    console.log(`[Flagsmith Demo] Flag ${flagName} set to ${value}`);
  }

  /**
   * Simple hash function for user ID (demo mode)
   */
  private hashUserId(userId: string): number {
    let hash = 0;
    for (let i = 0; i < userId.length; i++) {
      const char = userId.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash);
  }

  /**
   * Reset all flags to default
   */
  reset(): void {
    this.flags = { ...defaultFlags };
    if (typeof window !== 'undefined') {
      localStorage.removeItem('featureFlags');
    }
  }

  /**
   * Check if using real Flagsmith API
   */
  isUsingAPI(): boolean {
    return this.useFlagsmithAPI;
  }
}

// Singleton instance
export const featureFlagManager = new FeatureFlagManager();

/**
 * Hook-like function to get feature flag value
 */
export function useFeatureFlag<K extends keyof FeatureFlags>(
  flagName: K
): FeatureFlags[K] {
  return featureFlagManager.getFlag(flagName);
}

/**
 * Get order lookup version for current user
 */
export function getOrderLookupVersion(): 'A' | 'B' {
  return featureFlagManager.getFlag('order-lookup-version');
}

/**
 * Check if Proof of Delivery feature is enabled (shared across all apps)
 * Convenience function for showing/hiding POD photos
 *
 * @example
 * if (isProofOfDeliveryEnabled()) {
 *   return <PODPhotoSection />
 * }
 */
export function isProofOfDeliveryEnabled(): boolean {
  return featureFlagManager.isEnabled('proof-of-delivery-enabled');
}
