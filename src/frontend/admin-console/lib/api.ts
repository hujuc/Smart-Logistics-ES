/**
 * Admin Console API Configuration
 *
 * This file configures API endpoints for the admin console frontends.
 * Each interface (logistics, warehouse, customer-service) has its own BFF endpoint.
 */

import { getAccessToken } from './shared-auth';

// Get the base API URL from environment or default to localhost
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost';
const API_BASE = `${BASE_URL}/api`;
const LOGISTICS_BASE = `${API_BASE}/logistics`;
const WAREHOUSE_BASE = `${API_BASE}/warehouse`;
const CUSTOMER_SERVICE_BASE = `${API_BASE}/customer-service`;
const ADMIN_BASE = `${API_BASE}/admin`;

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
    stats: `${LOGISTICS_BASE}/stats`,
    warehouses: `${LOGISTICS_BASE}/warehouses`,

    // Helper functions for user-specific paths
    user: (userId: string) => `${LOGISTICS_BASE}/users/${userId}`,
    userPassword: (userId: string) => `${LOGISTICS_BASE}/users/${userId}/password`,
    userSearch: (query: string) => `${LOGISTICS_BASE}/users/search?query=${encodeURIComponent(query)}`,
    statsWithPeriod: (period: 'daily' | 'weekly' | 'monthly') => `${LOGISTICS_BASE}/stats?period=${period}`,

    // Helper functions for warehouse-specific paths
    warehouse: (warehouseId: number) => `${LOGISTICS_BASE}/warehouses/${warehouseId}`,
    warehouseOrders: (warehouseId: number, status?: string) =>
      `${LOGISTICS_BASE}/warehouses/${warehouseId}/orders${status ? `?status=${status}` : ''}`,
  },

  // Warehouse Staff BFF
  warehouse: {
    base: WAREHOUSE_BASE,
    health: `${WAREHOUSE_BASE}/health`,
    orders: `${WAREHOUSE_BASE}/orders`,
    drivers: `${WAREHOUSE_BASE}/drivers`, // Get all drivers for shipment assignment

    // Helper function to build order-specific paths
    order: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}`,
    orderDocuments: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}/documents`,
    orderDocument: (orderId: number, documentId: string) =>
      `${WAREHOUSE_BASE}/orders/${orderId}/documents/${encodeURIComponent(documentId)}`,
    orderDocumentDownload: (orderId: number, documentId: string) =>
      `${WAREHOUSE_BASE}/orders/${orderId}/documents/${encodeURIComponent(documentId)}/download`,
    orderShipmentDocuments: (orderId: number) => `${WAREHOUSE_BASE}/orders/${orderId}/shipment-documents`,
    orderShipmentDocumentDownload: (orderId: number, documentId: string) =>
      `${WAREHOUSE_BASE}/orders/${orderId}/shipment-documents/${encodeURIComponent(documentId)}/download`,
  },

  // Customer Service BFF
  customerService: {
    base: CUSTOMER_SERVICE_BASE,
    health: `${CUSTOMER_SERVICE_BASE}/health`,

    // Order search and lookup
    ordersSearch: (query: string) => `${CUSTOMER_SERVICE_BASE}/orders/search?query=${encodeURIComponent(query)}`,
    order: (orderId: number) => `${CUSTOMER_SERVICE_BASE}/orders/${orderId}`,
    orderEvents: (orderId: number) => `${API_BASE}/admin/orders/${orderId}/events`,
    orderTracking: (orderId: number) => `${CUSTOMER_SERVICE_BASE}/orders/${orderId}/tracking`,
    orderWithEvents: (orderId: number) => `${CUSTOMER_SERVICE_BASE}/orders/${orderId}/with-events`,

    // Delivery Exceptions (Tickets)
    exceptions: `${CUSTOMER_SERVICE_BASE}/delivery-exceptions`,
    exception: (exceptionId: string) => `${CUSTOMER_SERVICE_BASE}/delivery-exceptions/${exceptionId}`,
    exceptionClose: (exceptionId: string) => `${CUSTOMER_SERVICE_BASE}/delivery-exceptions/${exceptionId}/close`,
    exceptionContactCarrier: (exceptionId: string) => `${CUSTOMER_SERVICE_BASE}/delivery-exceptions/${exceptionId}/contact-carrier`,
    exceptionContactCustomer: (exceptionId: string) => `${CUSTOMER_SERVICE_BASE}/delivery-exceptions/${exceptionId}/contact-customer`,
    exceptionsSummary: `${CUSTOMER_SERVICE_BASE}/delivery-exceptions/summary`,
    exceptionsWithFilters: (filters: {
      status?: string;
      priority?: string;
      assignedTo?: string;
      severity?: string;
    }) => {
      const params = new URLSearchParams();
      if (filters.status) params.append('status', filters.status);
      if (filters.priority) params.append('priority', filters.priority);
      if (filters.assignedTo) params.append('assignedTo', filters.assignedTo);
      if (filters.severity) params.append('severity', filters.severity);
      const queryString = params.toString();
      return `${CUSTOMER_SERVICE_BASE}/delivery-exceptions${queryString ? `?${queryString}` : ''}`;
    },

    // User management
    customerServiceReps: `${CUSTOMER_SERVICE_BASE}/users/customer-service-reps`,
  },

  // Admin API - Direct backend endpoints (not through BFF)
  admin: {
    base: ADMIN_BASE,

    // Shipments
    shipments: `${ADMIN_BASE}/shipments`,
    shipment: (shipmentId: number) => `${ADMIN_BASE}/shipments/${shipmentId}`,
    shipmentDriver: (shipmentId: number) => `${ADMIN_BASE}/shipments/${shipmentId}/driver`,
    shipmentsByCarrier: (carrierId: number) => `${ADMIN_BASE}/shipments/carrier/${carrierId}`,
    shipmentsByDriver: (driverId: string) => `${ADMIN_BASE}/shipments/driver/${driverId}`,

    // Carriers
    carriers: `${ADMIN_BASE}/carriers`,
    carrier: (carrierId: number) => `${ADMIN_BASE}/carriers/${carrierId}`,
    carrierSync: `${ADMIN_BASE}/carriers/integrations/sync`,
    carrierSnapshot: `${ADMIN_BASE}/carriers/integrations/snapshot`,

    // Warehouses
    warehouses: `${ADMIN_BASE}/warehouses`,
    warehouse: (warehouseId: number) => `${ADMIN_BASE}/warehouses/${warehouseId}`,
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
