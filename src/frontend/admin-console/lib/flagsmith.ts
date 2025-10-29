/**
 * Flagsmith Configuration
 * Feature flags for A/B testing and gradual rollouts
 */

// For demo purposes, we'll use a simple in-memory feature flag system
// In production, you would integrate with Flagsmith API
interface FeatureFlags {
  'order-lookup-version': 'A' | 'B';
  'carrier-view-mode': 'table' | 'cards';
}

const defaultFlags: FeatureFlags = {
  'order-lookup-version': 'A', // Default to version A
  'carrier-view-mode': 'table', // Default to table view
};

/**
 * Simple feature flag manager
 * In production, this would call Flagsmith API
 */
class FeatureFlagManager {
  private flags: FeatureFlags;
  private userId: string | null = null;

  constructor() {
    // Initialize with default flags
    this.flags = { ...defaultFlags };
    
    // Load from localStorage if available (for demo)
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

  /**
   * Initialize with user information
   * In production, this would fetch flags from Flagsmith based on user
   */
  async initialize(userId: string): Promise<void> {
    this.userId = userId;
    
    // For demo: randomly assign version based on user ID hash
    // In production, Flagsmith would handle this
    const hash = this.hashUserId(userId);
    const orderLookupVersion = hash % 2 === 0 ? 'A' : 'B';
    const carrierViewMode = hash % 2 === 0 ? 'table' : 'cards';
    
    this.flags['order-lookup-version'] = orderLookupVersion;
    this.flags['carrier-view-mode'] = carrierViewMode;
    
    // Store in localStorage for consistency
    if (typeof window !== 'undefined') {
      localStorage.setItem('featureFlags', JSON.stringify(this.flags));
    }
    
    console.log(`[Flagsmith] User ${userId} assigned to Order Lookup Version ${orderLookupVersion}`);
    console.log(`[Flagsmith] User ${userId} assigned to Carrier View Mode ${carrierViewMode}`);
  }

  /**
   * Get feature flag value
   */
  getFlag<K extends keyof FeatureFlags>(flagName: K): FeatureFlags[K] {
    return this.flags[flagName];
  }

  /**
   * Check if feature is enabled
   */
  isEnabled(flagName: keyof FeatureFlags): boolean {
    return this.flags[flagName] !== undefined;
  }

  /**
   * Manually set a flag (for testing/demo purposes)
   */
  setFlag<K extends keyof FeatureFlags>(flagName: K, value: FeatureFlags[K]): void {
    this.flags[flagName] = value;
    if (typeof window !== 'undefined') {
      localStorage.setItem('featureFlags', JSON.stringify(this.flags));
    }
    console.log(`[Flagsmith] Flag ${flagName} set to ${value}`);
  }

  /**
   * Simple hash function for user ID
   */
  private hashUserId(userId: string): number {
    let hash = 0;
    for (let i = 0; i < userId.length; i++) {
      const char = userId.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32bit integer
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
