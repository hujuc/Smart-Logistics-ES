/**
 * Zustand store for user information state
 * Provides reactive access to user data across all frontends
 */

import { create } from 'zustand';
import { UserData, getUserData, clearAuth } from './auth';

interface UserState {
  user: UserData | null;
  isLoading: boolean;
  setUser: (user: UserData | null) => void;
  clearUser: () => void;
  loadUser: () => void;
  hasRole: (role: string) => boolean;
}

export const useUserStore = create<UserState>((set, get) => ({
  user: null,
  isLoading: true,

  /**
   * Set user data in store and localStorage
   */
  setUser: (user: UserData | null) => {
    set({ user, isLoading: false });
    if (user) {
      // Sync to localStorage for persistence
      localStorage.setItem('user', JSON.stringify(user));
    }
  },

  /**
   * Clear user data from store and all storage
   */
  clearUser: () => {
    clearAuth(); // Clear tokens and user from localStorage/cookies
    set({ user: null, isLoading: false });

    // Broadcast to other tabs
    if (typeof window !== 'undefined') {
      localStorage.setItem('auth_event', JSON.stringify({ type: 'logout', timestamp: Date.now() }));
    }
  },

  /**
   * Load user data from localStorage
   */
  loadUser: () => {
    const userData = getUserData();
    set({ user: userData, isLoading: false });
  },

  /**
   * Check if current user has a specific role
   */
  hasRole: (role: string) => {
    const state = get();
    if (!state.user || !state.user.roles) {
      return false;
    }
    return state.user.roles.includes(role);
  },
}));

/**
 * Initialize user store from localStorage
 * Call this once when your app mounts
 */
export function initializeUserStore() {
  useUserStore.getState().loadUser();
}

/**
 * Set up cross-tab synchronization
 * Call this once in your app root
 */
export function setupUserStoreSync() {
  if (typeof window === 'undefined') {
    return;
  }

  // Listen for storage changes from other tabs
  const handleStorageChange = (event: StorageEvent) => {
    // User data changed in another tab
    if (event.key === 'user') {
      const userData = getUserData();
      useUserStore.setState({ user: userData });
    }

    // Auth event (logout) from another tab
    if (event.key === 'auth_event') {
      try {
        const authEvent = JSON.parse(event.newValue || '{}');
        if (authEvent.type === 'logout') {
          useUserStore.setState({ user: null });
        }
      } catch {
        // Ignore invalid JSON
      }
    }
  };

  window.addEventListener('storage', handleStorageChange);

  // Return cleanup function
  return () => {
    window.removeEventListener('storage', handleStorageChange);
  };
}
