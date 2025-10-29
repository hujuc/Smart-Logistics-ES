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
   * @param statuses Array of statuses to filter by (e.g., ["CREATED", "READY_FOR_PICKUP"])
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
   * Confirm pickup for an order
   */
  async confirmPickup(id: number): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.warehouse.orderCollect(id), {
      method: 'POST',
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
};
