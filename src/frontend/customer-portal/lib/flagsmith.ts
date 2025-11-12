/**
 * Flagsmith Configuration for Customer Portal
 * Feature flags for gradual rollouts and A/B testing of Support Tickets (Delivery Exceptions)
 * 
 * This system allows us to:
 * - Enable/disable features without deployment (Feature Toggles)
 * - Gradually roll out features to percentage of users (Release Toggles)
 * - A/B test different implementations (Multivariate Toggles)
 */

import flagsmith from 'flagsmith';

// Flagsmith Environment Key
// Get this from: Flagsmith Dashboard -> Environments -> Client-side Environment Key
const FLAGSMITH_ENV_KEY = process.env.NEXT_PUBLIC_FLAGSMITH_ENV_KEY || '';

// Use Flagsmith API in production, fallback to in-memory for development
const USE_FLAGSMITH_API = FLAGSMITH_ENV_KEY.length > 0;

// For demo purposes, we'll use a simple in-memory feature flag system
// In production, you would integrate with Flagsmith API: https://flagsmith.com/
interface FeatureFlags {
  // ========================================
  // FEATURE TOGGLES (On/Off switches)
  // ========================================
  
  /**
   * Master switch for support tickets (delivery exceptions) feature
   * When false: Users see "Feature coming soon" message
   * When true: Users can access tickets functionality
   */
  'support-tickets-enabled': boolean;
  
  /**
   * Allow users to upload files/photos when creating tickets
   * Phase 1 (false): Text-only tickets
   * Phase 2 (true): Tickets with attachments
   */
  'ticket-file-upload-enabled': boolean;
  
  /**
   * Auto-assign tickets to available Customer Service Representatives
   * When false: Tickets go to general queue
   * When true: Intelligent assignment based on workload/availability
   */
  'ticket-auto-assign-enabled': boolean;
  
  // ========================================
  // RELEASE TOGGLES (Gradual rollout)
  // ========================================
  
  /**
   * Percentage of users who can see the support tickets feature
   * 0 = Nobody sees it (testing in dev only)
   * 10 = 10% of users see it (alpha testing)
   * 50 = 50% of users see it (beta testing)
   * 100 = Everyone sees it (full release)
   */
  'tickets-rollout-percentage': number;
  
  // ========================================
  // MULTIVARIATE TOGGLES (A/B Testing)
  // ========================================
  
  /**
   * Which version of the ticket creation form to show
   * 'basic' = Simple form (type, description only)
   * 'advanced' = Full form (all fields: severity, priority, category, etc.)
   */
  'ticket-creation-version': 'basic' | 'advanced';
  
  /**
   * How to display the tickets list
   * 'table' = Traditional table view (more compact)
   * 'cards' = Card-based view (more visual, modern)
   */
  'ticket-list-view': 'table' | 'cards';
}

/**
 * Default flag values
 * These are used when flags haven't been set yet
 */
const defaultFlags: FeatureFlags = {
  // Feature toggles - Start with everything enabled for development
  'support-tickets-enabled': true,
  'ticket-file-upload-enabled': false, // Disable attachments initially
  'ticket-auto-assign-enabled': false, // Disable auto-assign initially
  
  // Release toggle - Start with 100% rollout for development
  // In production, you'd start at 0-10% and gradually increase
  'tickets-rollout-percentage': 100,
  
  // Multivariate toggles - Default to simple versions
  'ticket-creation-version': 'basic',
  'ticket-list-view': 'table',
};

/**
 * Feature Flag Manager
 * Manages feature flags for the customer portal
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
      const stored = localStorage.getItem('customerPortalFeatureFlags');
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
   * Initialize feature flags for a specific user
   * In production, this would fetch flags from Flagsmith API based on user traits
   * 
   * @param userId - The unique identifier for the user
   * @param userTraits - Optional user traits for advanced targeting (e.g., plan, region)
   */
  async initialize(userId: string, userTraits?: Record<string, string | number | boolean>): Promise<void> {
    this.userId = userId;
    
    console.log(`[Flagsmith] Initializing flags for user: ${userId}`);
    
    if (USE_FLAGSMITH_API) {
      // Use real Flagsmith API
      try {
        console.log('[Flagsmith] Using Flagsmith API');
        
        await flagsmith.init({
          environmentID: FLAGSMITH_ENV_KEY,
          identity: userId,
          traits: userTraits,
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
      
      // For demo: Assign flags based on user ID hash
      const hash = this.hashUserId(userId);
      
      // Determine rollout percentage for this user
      const rolloutPercentage = this.flags['tickets-rollout-percentage'];
      const userBucket = hash % 100; // User falls into bucket 0-99
      const isInRollout = userBucket < rolloutPercentage;
      
      if (!isInRollout) {
        // User is not in rollout - disable feature
        this.flags['support-tickets-enabled'] = false;
        console.log(`[Flagsmith] User ${userId} not in rollout (bucket: ${userBucket}, rollout: ${rolloutPercentage}%)`);
      } else {
        console.log(`[Flagsmith] User ${userId} IN rollout (bucket: ${userBucket}, rollout: ${rolloutPercentage}%)`);
        
        // Assign A/B test variants based on user hash
        this.flags['ticket-creation-version'] = hash % 2 === 0 ? 'basic' : 'advanced';
        this.flags['ticket-list-view'] = hash % 3 === 0 ? 'cards' : 'table';
      }
    }
    
    // Store in localStorage for consistency
    if (typeof window !== 'undefined') {
      localStorage.setItem('customerPortalFeatureFlags', JSON.stringify(this.flags));
    }
    
    // Log assigned flags
    console.log('[Flagsmith] Flags assigned:', {
      ticketsEnabled: this.flags['support-tickets-enabled'],
      rolloutPercentage: this.flags['tickets-rollout-percentage'],
      creationVersion: this.flags['ticket-creation-version'],
      listView: this.flags['ticket-list-view'],
      fileUpload: this.flags['ticket-file-upload-enabled'],
      autoAssign: this.flags['ticket-auto-assign-enabled'],
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
    this.flags['support-tickets-enabled'] = 
      flagsmith.hasFeature('support-tickets-enabled') && 
      flagsmith.getValue('support-tickets-enabled') === true;
    
    this.flags['ticket-file-upload-enabled'] = 
      flagsmith.hasFeature('ticket-file-upload-enabled') && 
      flagsmith.getValue('ticket-file-upload-enabled') === true;
    
    this.flags['ticket-auto-assign-enabled'] = 
      flagsmith.hasFeature('ticket-auto-assign-enabled') && 
      flagsmith.getValue('ticket-auto-assign-enabled') === true;
    
    // Sync numeric flag (rollout percentage)
    const rolloutValue = flagsmith.getValue('tickets-rollout-percentage');
    this.flags['tickets-rollout-percentage'] = rolloutValue 
      ? parseInt(rolloutValue as string, 10) 
      : 100;
    
    // Sync multivariate flags
    const creationVersion = flagsmith.getValue('ticket-creation-version');
    this.flags['ticket-creation-version'] = 
      (creationVersion === 'basic' || creationVersion === 'advanced') 
        ? creationVersion 
        : 'basic';
    
    const listView = flagsmith.getValue('ticket-list-view');
    this.flags['ticket-list-view'] = 
      (listView === 'table' || listView === 'cards') 
        ? listView 
        : 'table';
    
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
      localStorage.setItem('customerPortalFeatureFlags', JSON.stringify(this.flags));
    }
    
    console.log(`[Flagsmith] Flag '${flagName}' manually set to:`, value);
  }

  /**
   * Check if user is in rollout based on rollout percentage
   * This is useful for progressive rollouts
   * 
   * @returns true if user should see the feature, false otherwise
   */
  isInRollout(): boolean {
    if (!this.userId) {
      console.warn('[Flagsmith] User ID not set, assuming in rollout');
      return true;
    }
    
    const rolloutPercentage = this.flags['tickets-rollout-percentage'];
    const hash = this.hashUserId(this.userId);
    const userBucket = hash % 100;
    
    return userBucket < rolloutPercentage;
  }

  /**
   * Hash function to consistently assign users to buckets
   * Same user ID always produces same hash
   * 
   * @param userId - User ID to hash
   * @returns Hash value
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
   * Reset all flags to default values
   * Useful for testing or clearing user state
   */
  reset(): void {
    this.flags = { ...defaultFlags };
    this.userId = null;
    
    if (typeof window !== 'undefined') {
      localStorage.removeItem('customerPortalFeatureFlags');
    }
    
    console.log('[Flagsmith] Flags reset to defaults');
  }

  /**
   * Get current user ID
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
 * const version = useFeatureFlag('ticket-creation-version')
 * if (version === 'basic') { ... }
 */
export function useFeatureFlag<K extends keyof FeatureFlags>(
  flagName: K
): FeatureFlags[K] {
  return featureFlagManager.getFlag(flagName);
}

/**
 * Check if support tickets feature is enabled for current user
 * Convenience function for the most common check
 * 
 * @example
 * if (isSupportTicketsEnabled()) {
 *   return <TicketsPage />
 * } else {
 *   return <ComingSoonMessage />
 * }
 */
export function isSupportTicketsEnabled(): boolean {
  return featureFlagManager.isEnabled('support-tickets-enabled');
}

/**
 * Get the ticket creation form version for current user
 * Convenience function for A/B testing the form
 * 
 * @example
 * const version = getTicketCreationVersion()
 * return version === 'basic' ? <BasicForm /> : <AdvancedForm />
 */
export function getTicketCreationVersion(): 'basic' | 'advanced' {
  return featureFlagManager.getFlag('ticket-creation-version');
}

/**
 * Get the ticket list view mode for current user
 * Convenience function for A/B testing the list view
 * 
 * @example
 * const view = getTicketListView()
 * return view === 'table' ? <TableView /> : <CardsView />
 */
export function getTicketListView(): 'table' | 'cards' {
  return featureFlagManager.getFlag('ticket-list-view');
}

// Export the FeatureFlags type for use in other files
export type { FeatureFlags };
