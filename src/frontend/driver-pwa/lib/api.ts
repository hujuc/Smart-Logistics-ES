/**
 * Driver PWA API Configuration
 *
 * This file configures API endpoints for the driver PWA.
 */

// Get the base API URL from environment or default to localhost
const API_BASE_URL = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost'}/api/driver`;

/**
 * Driver BFF API endpoints
 */
export const API_ENDPOINTS = {
  base: `${API_BASE_URL}`,
  health: `${API_BASE_URL}/health`,
  dailySchedule: `${API_BASE_URL}/daily-schedule`,
  dashboard: `${API_BASE_URL}/dashboard`,
};

/**
 * Get access token from localStorage
 */
export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('access_token');
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
    const error = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(error.error || `HTTP ${response.status}`);
  }

  return response.json();
}

/**
 * Health check
 */
export async function checkHealth(): Promise<{ status: string; service: string; timestamp: string }> {
  return apiFetch(API_ENDPOINTS.health);
}

/**
 * Daily Schedule types
 */
export interface WarehouseAddress {
  name: string;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  stateOrProvince: string;
  country: string;
  postalCode: string;
}

export interface DailyScheduleDelivery {
  orderId: number;
  orderReference: string;
  stopNumber: number;
  customerName: string;
  address: string;
  phone: string;
  status: string;
  weight: number;
  weightUnit: string;
  estimatedDeliveryDate: string;
  shippingCity: string;
  shippingPostalCode: string;
  shippingCountry: string;
  shippingStateOrProvince: string;
  warehouseAddress: WarehouseAddress;
}

export interface DailyScheduleResponse {
  shipmentCode: string;
  shipmentId: number;
  totalStops: number;
  totalPackages: number;
  totalWeight: number;
  weightUnit: string;
  deliveries: DailyScheduleDelivery[];
}

/**
 * Get driver's daily schedule
 */
export async function getDailySchedule(): Promise<DailyScheduleResponse> {
  return apiFetch(API_ENDPOINTS.dailySchedule);
}

/**
 * Dashboard types
 */
export interface TodayStats {
  totalDeliveries: number;
  completed: number;
  remaining: number;
  nextStopETA: string;
}

export interface NextDeliveryPreview {
  orderId: number;
  stopNumber: number;
  customerName: string;
  address: string;
  priority: string;
}

export interface DashboardResponse {
  todayStats: TodayStats;
  nextDeliveries: NextDeliveryPreview[];
}

/**
 * Get driver's dashboard data
 */
export async function getDashboard(): Promise<DashboardResponse> {
  return apiFetch(API_ENDPOINTS.dashboard);
}

/**
 * Shipment acceptance types
 */
export interface PendingShipment {
  id: number;
  shipmentCode: string;
  status: string;
  carrierName: string;
  totalOrders: number;
  estimatedDeliveryDate: string | null;
}

/**
 * Get pending shipments for driver acceptance
 */
export async function getPendingShipments(): Promise<PendingShipment[]> {
  // This endpoint doesn't exist yet, but we'll create it
  // For now, we can fetch from the daily schedule and filter by status
  return apiFetch(`${API_ENDPOINTS.base}/pending-shipments`);
}

/**
 * Accept a shipment
 */
export async function acceptShipment(shipmentId: number): Promise<any> {
  return apiFetch(`${API_ENDPOINTS.base}/shipments/${shipmentId}/accept`, {
    method: 'POST',
  });
}

/**
 * Reject a shipment
 */
export async function rejectShipment(shipmentId: number, reason?: string): Promise<any> {
  return apiFetch(`${API_ENDPOINTS.base}/shipments/${shipmentId}/reject`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  });
}
