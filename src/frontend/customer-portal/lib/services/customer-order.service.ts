/**
 * Customer Order Service
 * 
 * API service for customer order operations
 */

import { apiFetch, API_ENDPOINTS } from '../api';
import { Order, OrderEvent } from '@/types/common';

/**
 * Customer Order API methods
 */
export const customerOrderApi = {
  /**
   * Get all orders for the customer
   */
  async getAllOrders(): Promise<Order[]> {
    return apiFetch<Order[]>(API_ENDPOINTS.orders, {
      method: 'GET',
    });
  },

  /**
   * Get order by ID
   */
  async getOrderById(id: number): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.order(id), {
      method: 'GET',
    });
  },

  /**
   * Get order events (timeline)
   */
  async getOrderEvents(id: number): Promise<OrderEvent[]> {
    return apiFetch<OrderEvent[]>(API_ENDPOINTS.orderEvents(id), {
      method: 'GET',
    });
  },
};
