/**
 * Shared authentication utilities for all frontends
 * Manages JWT access tokens stored in cookies and localStorage
 */

export interface UserData {
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}

/**
 * Get access token from cookie
 */
export function getAccessTokenFromCookie(): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'access_token') {
      return decodeURIComponent(value);
    }
  }
  return null;
}

/**
 * Get access token from localStorage (fallback)
 */
export function getAccessTokenFromLocalStorage(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return localStorage.getItem('access_token');
}

/**
 * Get access token from either cookie or localStorage
 */
export function getAccessToken(): string | null {
  return getAccessTokenFromCookie() || getAccessTokenFromLocalStorage();
}

/**
 * Get user data from localStorage
 */
export function getUserData(): UserData | null {
  if (typeof window === 'undefined') {
    return null;
  }

  const userStr = localStorage.getItem('user');
  if (!userStr) {
    return null;
  }

  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

/**
 * Get user's primary role
 * Returns the first role from the user's roles array
 */
export function getUserRole(): string | null {
  const user = getUserData();
  if (!user || !user.roles || user.roles.length === 0) {
    return null;
  }
  return user.roles[0];
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
  return !!getAccessToken();
}

/**
 * Clear authentication data (logout)
 */
export function clearAuth(): void {
  if (typeof window === 'undefined') {
    return;
  }

  // Clear localStorage
  localStorage.removeItem('access_token');
  localStorage.removeItem('user');

  // Clear cookie
  document.cookie = 'access_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
}

/**
 * Store authentication data after login
 */
export function storeAuth(accessToken: string, user: UserData, expiresIn: number): void {
  if (typeof window === 'undefined') {
    return;
  }

  // Store in localStorage
  localStorage.setItem('access_token', accessToken);
  localStorage.setItem('user', JSON.stringify(user));

  // Store in cookie
  const expiresInMs = expiresIn * 1000;
  const expiryDate = new Date(Date.now() + expiresInMs);
  document.cookie = `access_token=${accessToken}; path=/; expires=${expiryDate.toUTCString()}; SameSite=Lax`;
}

/**
 * Decode JWT token (without verification - for debugging only)
 */
export function decodeJWT(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload) as Record<string, unknown>;
  } catch (error) {
    console.error('Failed to decode JWT:', error);
    return null;
  }
}

/**
 * Debug: Log current token information
 */
export function debugToken(): void {
  const token = getAccessToken();
  const user = getUserData();

  console.log('=== Auth Debug ===');
  console.log('Has token:', !!token);
  console.log('User data:', user);

  if (token) {
    const decoded = decodeJWT(token);
    console.log('Token payload:', decoded);

    // Safely extract roles from token
    const realmAccess = decoded?.realm_access as Record<string, unknown> | undefined;
    const realmRoles = realmAccess?.roles;
    const directRoles = decoded?.roles;
    console.log('Token roles:', realmRoles || directRoles || 'No roles found');
  }
  console.log('=================');
}
