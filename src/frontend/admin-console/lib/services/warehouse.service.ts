/**
 * Warehouse Service - API calls for warehouse operations
 */

import { apiClient } from '../api-client';
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
    return apiClient.get<Order[]>('/admin/orders');
  },

  /**
   * Get order by ID
   */
  async getOrderById(id: number): Promise<Order> {
    return apiClient.get<Order>(`/admin/orders/${id}`);
  },

  /**
   * Create a new order
   */
  async createOrder(order: CreateOrderRequest): Promise<Order> {
    return apiClient.post<Order>('/admin/orders', order);
  },

  /**
   * Update an existing order
   */
  async updateOrder(id: number, order: Partial<CreateOrderRequest>): Promise<Order> {
    return apiClient.put<Order>(`/admin/orders/${id}`, order);
  },

  /**
   * Confirm pickup for an order
   */
  async confirmPickup(id: number): Promise<Order> {
    return apiClient.post<Order>(`/admin/orders/${id}/collect`);
  },

  /**
   * Delete an order
   */
  async deleteOrder(id: number): Promise<void> {
    return apiClient.delete(`/admin/orders/${id}`);
  },
};
