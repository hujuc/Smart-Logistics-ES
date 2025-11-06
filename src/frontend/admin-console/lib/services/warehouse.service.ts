/**
 * Warehouse Service - API calls for warehouse operations
 * Uses API_ENDPOINTS from api.ts for proper BFF endpoint configuration
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type { Order } from '@/types';

export interface CreateOrderRequest {
  reference: string;
  description?: string;
  shippingAddress: string;
  estimatedDeliveryDate?: string; // ISO date string
  warehouseId: number;
  trackingNumber?: string;
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
        estimatedDeliveryDate: currentOrder.estimatedDeliveryDate,
        warehouseId: currentOrder.warehouseId,
        carrierId: currentOrder.carrierId,
        trackingNumber: currentOrder.trackingNumber,
        customerId: currentOrder.customerId, // Preserve customer association
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
        estimatedDeliveryDate: currentOrder.estimatedDeliveryDate,
        warehouseId: currentOrder.warehouseId,
        trackingNumber: currentOrder.trackingNumber,
      }),
    });
  },
};
