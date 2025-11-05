/**
 * Shipment Service - API calls for shipment operations
 * Manages shipment creation, updates, and retrieval
 */

import { API_ENDPOINTS, apiFetch } from '../api';

export interface ShipmentResponse {
  id: number;
  shipmentCode: string;

  // Assignment
  carrierId: number;
  carrierCode: string;
  carrierName: string;
  driverId: string; // Keycloak driver UUID

  // Shippo/ShipStation integration
  shippoShipmentId?: string;
  shippoTransactionId?: string;
  shipstationOrderId?: string;
  shipstationCarrierCode?: string;
  shipstationServiceCode?: string;

  // Shipment details
  totalOrders?: number;
  totalWeight?: number;
  weightUnit?: string;
  estimatedDeliveryDate?: string; // ISO date string

  // Costs
  shippingCost?: number;
  transitTime?: string;

  // Timestamps
  createdAt: string;
  updatedAt: string;
}

export interface CreateShipmentRequest {
  shipmentCode: string;
  carrierId: number;
  driverId: string; // Keycloak driver UUID (required)
  orderIds?: number[]; // IDs of orders to include in shipment

  // Shippo/ShipStation integration fields (optional)
  shippoShipmentId?: string;
  shippoTransactionId?: string;
  shipstationOrderId?: string;
  shipstationCarrierCode?: string;
  shipstationServiceCode?: string;

  // Shipment details (optional)
  totalOrders?: number;
  totalWeight?: number;
  weightUnit?: string;
  estimatedDeliveryDate?: string; // ISO date string

  // Costs (optional)
  shippingCost?: number;
  transitTime?: string;
}

export const shipmentService = {
  /**
   * Get all shipments
   */
  async getAllShipments(): Promise<ShipmentResponse[]> {
    return apiFetch<ShipmentResponse[]>(API_ENDPOINTS.admin.shipments);
  },

  /**
   * Get shipment by ID
   */
  async getShipmentById(id: number): Promise<ShipmentResponse> {
    return apiFetch<ShipmentResponse>(API_ENDPOINTS.admin.shipment(id));
  },

  /**
   * Create a new shipment
   */
  async createShipment(shipment: CreateShipmentRequest): Promise<ShipmentResponse> {
    return apiFetch<ShipmentResponse>(API_ENDPOINTS.admin.shipments, {
      method: 'POST',
      body: JSON.stringify(shipment),
    });
  },

  /**
   * Update driver for a shipment
   */
  async updateDriver(shipmentId: number, driverId: string): Promise<ShipmentResponse> {
    return apiFetch<ShipmentResponse>(`${API_ENDPOINTS.admin.shipmentDriver(shipmentId)}?driverId=${driverId}`, {
      method: 'PUT',
    });
  },

  /**
   * Get shipments by carrier ID
   */
  async getShipmentsByCarrier(carrierId: number): Promise<ShipmentResponse[]> {
    return apiFetch<ShipmentResponse[]>(API_ENDPOINTS.admin.shipmentsByCarrier(carrierId));
  },

  /**
   * Get shipments by driver ID
   */
  async getShipmentsByDriver(driverId: string): Promise<ShipmentResponse[]> {
    return apiFetch<ShipmentResponse[]>(API_ENDPOINTS.admin.shipmentsByDriver(driverId));
  },
};
