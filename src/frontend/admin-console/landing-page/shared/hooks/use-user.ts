/**
 * Convenience hooks for accessing user data from the store
 */

import { useUserStore } from '../lib/user-store';
import { UserData } from '../lib/auth';

/**
 * Get current user data
 * Returns null if user is not logged in
 *
 * @example
 * const user = useUser();
 * if (user) {
 *   return <div>Welcome {user.firstName}!</div>
 * }
 */
export function useUser(): UserData | null {
  return useUserStore((state) => state.user);
}

/**
 * Get user's roles array
 * Returns empty array if user is not logged in
 *
 * @example
 * const roles = useUserRoles();
 * if (roles.includes('LOGISTICS_MANAGER')) {
 *   // Show admin features
 * }
 */
export function useUserRoles(): string[] {
  const user = useUserStore((state) => state.user);
  return user?.roles || [];
}

/**
 * Check if current user has a specific role
 *
 * @example
 * const isAdmin = useHasRole('LOGISTICS_MANAGER');
 * if (isAdmin) {
 *   return <AdminPanel />;
 * }
 */
export function useHasRole(role: string): boolean {
  return useUserStore((state) => state.hasRole(role));
}

/**
 * Check if user is authenticated
 *
 * @example
 * const isAuthenticated = useIsAuthenticated();
 * if (!isAuthenticated) {
 *   return <LoginPrompt />;
 * }
 */
export function useIsAuthenticated(): boolean {
  const user = useUserStore((state) => state.user);
  return user !== null;
}

/**
 * Get loading state
 * Returns true while user data is being loaded from storage
 *
 * @example
 * const isLoading = useUserLoading();
 * if (isLoading) {
 *   return <Spinner />;
 * }
 */
export function useUserLoading(): boolean {
  return useUserStore((state) => state.isLoading);
}

/**
 * Get user actions (setUser, clearUser, loadUser)
 *
 * @example
 * const { setUser, clearUser } = useUserActions();
 * const handleLogout = () => clearUser();
 */
export function useUserActions() {
  return {
    setUser: useUserStore((state) => state.setUser),
    clearUser: useUserStore((state) => state.clearUser),
    loadUser: useUserStore((state) => state.loadUser),
  };
}
