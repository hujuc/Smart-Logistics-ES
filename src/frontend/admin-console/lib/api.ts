/**
 * Admin Console API Configuration
 *
 * This file configures API endpoints for the admin console frontends.
 * Each interface (logistics, warehouse, customer-service) has its own BFF endpoint.
 */

import { getAccessToken } from './shared-auth';

// Get the base API URL from environment or default to localhost
const API_BASE = process.env.NEXT_PUBLIC_API_URL || '/api';
const LOGISTICS_BASE = `${API_BASE}/logistics`;
const WAREHOUSE_BASE = `${API_BASE}/warehouse`;
const CUSTOMER_SERVICE_BASE = `${API_BASE}/customer-service`;

/**
 * API endpoints for different admin interfaces
 */
export const API_ENDPOINTS = {
  // Logistics Manager BFF
  logistics: {
    base: LOGISTICS_BASE,
    health: `${LOGISTICS_BASE}/health`,
    orders: `${LOGISTICS_BASE}/orders`,
    users: `${LOGISTICS_BASE}/users`,

    // Helper functions for user-specific paths
    user: (userId: string) => `${LOGISTICS_BASE}/users/${userId}`,
    userPassword: (userId: string) => `${LOGISTICS_BASE}/users/${userId}/password`,
    userSearch: (query: string) => `${LOGISTICS_BASE}/users/search?query=${encodeURIComponent(query)}`,
  },

  // Warehouse Staff BFF
  warehouse: {
    base: WAREHOUSE_BASE,
    health: `${WAREHOUSE_BASE}/health`,
    orders: `${WAREHOUSE_BASE}/orders`,
    
    // Helper function to build order-specific paths
    order: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}`,
    orderDocuments: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}/documents`,
    orderDocument: (orderId: number, documentId: string) =>
      `${WAREHOUSE_BASE}/orders/${orderId}/documents/${encodeURIComponent(documentId)}`,
    orderDocumentDownload: (orderId: number, documentId: string) =>
      `${WAREHOUSE_BASE}/orders/${orderId}/documents/${encodeURIComponent(documentId)}/download`,
    orderCollect: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}/collect`,
  },

  // Customer Service BFF
  customerService: {
    base: CUSTOMER_SERVICE_BASE,
    health: `${CUSTOMER_SERVICE_BASE}/health`,
  },
};

/**
 * Create headers with authorization
 */
export function createAuthHeaders(contentType: string = 'application/json'): HeadersInit {
  const token = getAccessToken();
  const headers: HeadersInit = {};

  if (contentType) {
    headers['Content-Type'] = contentType;
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return headers;
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
    const errorData = await response.json().catch(() => ({ error: 'Request failed' }));
    console.error('API Error:', {
      url,
      status: response.status,
      statusText: response.statusText,
      errorData,
      requestBody: options.body,
    });
    throw new Error(errorData.error || errorData.message || `HTTP ${response.status}: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Health check for a specific BFF
 */
export async function checkHealth(endpoint: string): Promise<{ status: string; service: string; timestamp: string }> {
  return apiFetch(`${endpoint}/health`);
}
