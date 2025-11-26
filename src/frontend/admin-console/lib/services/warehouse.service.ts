/**
 * Warehouse Service - API calls for warehouse operations
 * Uses API_ENDPOINTS from api.ts for proper BFF endpoint configuration
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type { Order, ShipmentOrderStatus, OrderEvent } from '@/types';

export interface CreateOrderRequest {
  reference: string;
  description?: string;
  shippingAddress: string;
  shippingAddressLine1?: string;
  shippingAddressLine2?: string;
  shippingCity?: string;
  shippingStateOrProvince?: string;
  shippingPostalCode?: string;
  shippingCountry?: string;
  shippingPhone?: string;
  shippingEmail?: string;
  estimatedDeliveryDate?: string; // ISO date string
  warehouseId: number;
  trackingNumber?: string;
  status?: ShipmentOrderStatus;
  carrierId?: number;
}

export const warehouseService = {
  /**
   * Get all orders
   */
  async getAllOrders(): Promise<Order[]> {
    return apiFetch<Order[]>(API_ENDPOINTS.warehouse.orders);
  },

  /**
   * Get orders by status
   * @param statuses Array of statuses to filter by (e.g., ["CREATED", "ASSIGNED_TO_SHIPMENT"])
   */
  async getOrdersByStatus(statuses: string[]): Promise<Order[]> {
    const statusParam = statuses.join(',');
    return apiFetch<Order[]>(`${API_ENDPOINTS.warehouse.orders}?status=${statusParam}`);
  },

  /**
   * Get order by ID
   */
  async getOrderById(id: number): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.warehouse.order(id));
  },

  /**
   * Create a new order
   */
  async createOrder(order: CreateOrderRequest): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.warehouse.orders, {
      method: 'POST',
      body: JSON.stringify(order),
    });
  },

  /**
   * Update an existing order
   */
  async updateOrder(id: number, order: Partial<CreateOrderRequest>): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.warehouse.order(id), {
      method: 'PUT',
      body: JSON.stringify(order),
    });
  },

  /**
   * Update order status
   */
  async updateOrderStatus(id: number, status: string): Promise<Order> {
    // Get the current order to preserve other fields
    const currentOrder = await this.getOrderById(id);
    
    // Update only the status field
    return apiFetch<Order>(API_ENDPOINTS.warehouse.order(id), {
      method: 'PUT',
      body: JSON.stringify({
        reference: currentOrder.reference,
        description: currentOrder.description,
        status: status,
        shippingAddress: currentOrder.shippingAddress,
        shippingAddressLine1: currentOrder.shippingAddressLine1,
        shippingAddressLine2: currentOrder.shippingAddressLine2,
        shippingCity: currentOrder.shippingCity,
        shippingStateOrProvince: currentOrder.shippingStateOrProvince,
        shippingPostalCode: currentOrder.shippingPostalCode,
        shippingCountry: currentOrder.shippingCountry,
        shippingPhone: currentOrder.shippingPhone,
        shippingEmail: currentOrder.shippingEmail,
        estimatedDeliveryDate: currentOrder.estimatedDeliveryDate instanceof Date ? currentOrder.estimatedDeliveryDate.toISOString() : currentOrder.estimatedDeliveryDate as string,
        warehouseId: currentOrder.warehouseId,
        carrierId: currentOrder.carrierId,
        trackingNumber: currentOrder.trackingNumber,
      }),
    });
  },

  /**
   * Delete an order
   */
  async deleteOrder(id: number): Promise<void> {
    await apiFetch<void>(API_ENDPOINTS.warehouse.order(id), {
      method: 'DELETE',
    });
  },

  /**
   * Assign carrier to an order
   * When a carrier is assigned, the order status is updated to ASSIGNED_TO_SHIPMENT
   */
  async assignCarrier(
    id: number,
    carrierName: string,
    carrierService: string,
    carrierCost: number,
    transitTime: string
  ): Promise<Order> {
    // Get the current order to preserve other fields
    const currentOrder = await this.getOrderById(id);

    // Update with carrier information and status
    // Store carrier details in available fields
    return apiFetch<Order>(API_ENDPOINTS.warehouse.order(id), {
      method: 'PUT',
      body: JSON.stringify({
        reference: currentOrder.reference,
        description: `${currentOrder.description || ''} | Carrier: ${carrierName} - ${carrierService} ($${carrierCost}, ${transitTime})`.trim(),
        status: 'ASSIGNED_TO_SHIPMENT', // Update status when carrier is assigned
        shippingAddress: currentOrder.shippingAddress,
        shippingAddressLine1: currentOrder.shippingAddressLine1,
        shippingAddressLine2: currentOrder.shippingAddressLine2,
        shippingCity: currentOrder.shippingCity,
        shippingStateOrProvince: currentOrder.shippingStateOrProvince,
        shippingPostalCode: currentOrder.shippingPostalCode,
        shippingCountry: currentOrder.shippingCountry,
        shippingPhone: currentOrder.shippingPhone,
        shippingEmail: currentOrder.shippingEmail,
        estimatedDeliveryDate: currentOrder.estimatedDeliveryDate instanceof Date ? currentOrder.estimatedDeliveryDate.toISOString() : currentOrder.estimatedDeliveryDate as string,
        warehouseId: currentOrder.warehouseId,
        trackingNumber: currentOrder.trackingNumber,
      }),
    });
  },

  /**
   * Get order events (timeline)
   */
  async getOrderEvents(id: number): Promise<OrderEvent[]> {
    return apiFetch<OrderEvent[]>(API_ENDPOINTS.customerService.orderEvents(id));
  },
};
