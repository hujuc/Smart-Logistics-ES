/**
 * Customer Portal API Configuration
 *
 * This file configures API endpoints for the customer portal.
 */

// Get the base API URL from environment or default to localhost
const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost'}/api/customer`;

/**
 * Customer BFF API endpoints
 */
export const API_ENDPOINTS = {
  base: `${API_BASE_URL}`,
  health: `${API_BASE_URL}/health`,
  
  // Orders
  orders: `${API_BASE_URL}/orders`,
  order: (orderId: number) => `${API_BASE_URL}/orders/${orderId}`,
  orderByTracking: (trackingNumber: string) => `${API_BASE_URL}/orders/tracking/${trackingNumber}`,
  orderEvents: (orderId: number) => `${API_BASE_URL}/orders/${orderId}/events`,
};

/**
 * Get access token from cookies
 */
export function getAccessToken(): string | null {
  if (typeof window === 'undefined') {
    console.log('[API] getAccessToken: Running on server side, returning null');
    return null;
  }
  
  // Get token from cookies
  const cookies = document.cookie.split(';');
  console.log('[API] getAccessToken: All cookies:', cookies.map(c => c.trim().split('=')[0]));
  
  const tokenCookie = cookies.find(cookie => cookie.trim().startsWith('access_token='));
  
  if (tokenCookie) {
    const token = tokenCookie.split('=')[1];
    console.log('[API] getAccessToken: Token found, length:', token?.length || 0);
    console.log('[API] getAccessToken: Token prefix:', token?.substring(0, 20) + '...');
    return token;
  }
  
  console.log('[API] getAccessToken: No access_token cookie found');
  return null;
}

/**
 * Create headers with authorization
 */
export function createAuthHeaders(): HeadersInit {
  const token = getAccessToken();
  console.log('[API] createAuthHeaders: Token present?', token ? 'YES' : 'NO');
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
  
  console.log('[API] createAuthHeaders: Headers:', Object.keys(headers));
  return headers;
}

/**
 * Generic API fetch wrapper
 */
export async function apiFetch<T>(
  url: string,
  options: RequestInit = {}
): Promise<T> {
  console.log('[API] apiFetch: Starting request to:', url);
  console.log('[API] apiFetch: Options:', { method: options.method, hasBody: !!options.body });
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...createAuthHeaders(),
      ...options.headers,
    },
    credentials: 'include',
  });

  console.log('[API] apiFetch: Response status:', response.status, response.statusText);
  console.log('[API] apiFetch: Response headers:', Object.fromEntries(response.headers.entries()));

  if (!response.ok) {
    const error = await response.json().catch(() => ({ 
      error: `HTTP ${response.status}: ${response.statusText}` 
    }));
    console.error('[API] apiFetch: Request failed:', error);
    throw new Error(error.error || `HTTP ${response.status}: ${response.statusText}`);
  }

  const data = await response.json();
  console.log('[API] apiFetch: Success! Response data:', Array.isArray(data) ? `Array with ${data.length} items` : typeof data);
  return data;
}

/**
 * Health check
 */
export async function checkHealth(): Promise<{ status: string; service: string; timestamp: string }> {
  return apiFetch(API_ENDPOINTS.health);
}
