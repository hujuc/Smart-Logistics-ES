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
   * Delete an order
   */
  async deleteOrder(id: number): Promise<void> {
    await apiFetch<void>(API_ENDPOINTS.warehouse.order(id), {
      method: 'DELETE',
    });
  },
};
