/**
 * Customer Portal API Configuration
 *
 * This file configures API endpoints for the customer portal.
 */

// Get the base API URL from environment or default to /api/customer
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/customer';

/**
 * Customer BFF API endpoints
 */
export const API_ENDPOINTS = {
  base: `${API_BASE_URL}`,
  health: `${API_BASE_URL}/health`,
  
  // Orders
  orders: `${API_BASE_URL}/orders`,
  order: (orderId: number) => `${API_BASE_URL}/orders/${orderId}`,
  orderEvents: (orderId: number) => `${API_BASE_URL}/orders/${orderId}/events`,
};

/**
 * Get access token from cookies
 */
export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  
  // Get token from cookies
  const cookies = document.cookie.split(';');
  const tokenCookie = cookies.find(cookie => cookie.trim().startsWith('access_token='));
  
  if (tokenCookie) {
    return tokenCookie.split('=')[1];
  }
  
  return null;
}

/**
 * Create headers with authorization
 */
export function createAuthHeaders(): HeadersInit {
  const token = getAccessToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

/**
 * Generic API fetch wrapper
 */
export async function apiFetch<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...createAuthHeaders(),
      ...options.headers,
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ 
      error: `HTTP ${response.status}: ${response.statusText}` 
    }));
    throw new Error(error.error || `HTTP ${response.status}: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Health check
 */
export async function checkHealth(): Promise<{ status: string; service: string; timestamp: string }> {
  return apiFetch(API_ENDPOINTS.health);
}
