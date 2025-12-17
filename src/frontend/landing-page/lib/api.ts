import { useUserStore } from '@shared/lib/user-store';

const API_URL = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost'}/api/landing-page`;

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}

/**
 * Parse JWT token and extract payload
 */
function parseJwt(token: string): any {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('Failed to parse JWT:', e);
    return {};
  }
}

/**
 * Login user via Landing Page BFF
 */
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Include cookies
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    let errorMessage = `Login failed with status ${response.status}`;
    try {
      const error = await response.json();
      if (error.error) {
        errorMessage = error.error;
      }
    } catch (parseError) {
      // If response has no JSON body, use the default error message
    }
    throw new Error(errorMessage);
  }

  return response.json();
}

/**
 * Register new user via Landing Page BFF
 */
export async function register(userData: SignUpRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_URL}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Include cookies
    body: JSON.stringify(userData),
  });

  if (!response.ok) {
    let errorMessage = `Registration failed with status ${response.status}`;
    try {
      const error = await response.json();
      if (error.error) {
        errorMessage = error.error;
      }
    } catch (parseError) {
      // If response has no JSON body, use the default error message
    }
    throw new Error(errorMessage);
  }

  return response.json();
}

/**
 * Logout user via Landing Page BFF
 */
export async function logout(): Promise<void> {
  await fetch(`${API_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
}

/**
 * Get role-based redirect URL - redirects to specific interface pages
 * Uses relative paths to work in both development and production
 */
export function getRoleBasedRedirect(roles: string[]): string {
  if (!roles || roles.length === 0) {
    return '/';
  }

  // Redirect to specific interface pages based on primary role
  // Priority order: Logistics Manager > Warehouse Staff > Customer Service > Driver > Customer

  // Check for both lowercase (from Keycloak) and uppercase (legacy) role names
  if (roles.includes('logistics_manager') || roles.includes('LOGISTICS_MANAGER')) {
    return '/admin/interfaces/logistics';
  }

  if (roles.includes('warehouse_staff') || roles.includes('WAREHOUSE_STAFF')) {
    return '/admin/interfaces/warehouse';
  }

  if (roles.includes('customer_service_rep') || roles.includes('CUSTOMER_SERVICE_REP')) {
    return '/admin/interfaces/customer-service';
  }

  if (roles.includes('driver') || roles.includes('DRIVER')) {
    return '/driver';
  }

  if (roles.includes('customer') || roles.includes('CUSTOMER')) {
    return '/customer';
  }

  // Default to root if no recognized role
  return '/';
}

/**
 * Store access token in localStorage and cookies, then redirect based on role
 */
export function handleSuccessfulAuth(response: LoginResponse): void {
  // Parse JWT to extract user ID from 'sub' claim
  const jwtPayload = parseJwt(response.accessToken);
  const userId = jwtPayload.sub || '';  // Keycloak stores user UUID in 'sub' claim

  const userData = {
    userId,
    username: response.username,
    email: response.email,
    firstName: response.firstName,
    lastName: response.lastName,
    roles: response.roles,
  };

  // Store access token in localStorage
  localStorage.setItem('access_token', response.accessToken);
  localStorage.setItem('user', JSON.stringify(userData));

  // Update user store for reactive state management
  useUserStore.getState().setUser(userData);

  // Also store in cookie for middleware access
  // Set cookie with path=/ so it's accessible across all frontends
  const expiresInMs = response.expiresIn * 1000; // Convert seconds to milliseconds
  const expiryDate = new Date(Date.now() + expiresInMs);
  document.cookie = `access_token=${response.accessToken}; path=/; expires=${expiryDate.toUTCString()}; SameSite=Lax`;

  // Redirect to appropriate frontend based on role
  const redirectUrl = getRoleBasedRedirect(response.roles);
  window.location.href = redirectUrl;
}
