/**
 * Customer Order Service
 * 
 * API service for customer order operations
 */

import { apiFetch, API_ENDPOINTS } from '../api';
import { Order, OrderEvent, Warehouse } from '@/types/common';

/**
 * Request payload for creating an order
 */
export interface CreateOrderRequest {
  reference: string;
  description?: string;
  shippingAddress: string;
  estimatedDeliveryDate?: string; // ISO date string
  warehouseId: number;
  trackingNumber?: string;
}

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
   * Get order by tracking number
   */
  async getOrderByTrackingNumber(trackingNumber: string): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.orderByTracking(trackingNumber), {
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

  /**
   * Create a new order
   */
  async createOrder(order: CreateOrderRequest): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.orders, {
      method: 'POST',
      body: JSON.stringify(order),
    });
  },

  /**
   * Get all warehouses
   */
  async getWarehouses(): Promise<Warehouse[]> {
    return apiFetch<Warehouse[]>('/api/customer/warehouses', {
      method: 'GET',
    });
  },
};
