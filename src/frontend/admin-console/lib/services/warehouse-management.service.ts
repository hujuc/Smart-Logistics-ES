/**
 * Warehouse Management Service - API calls for warehouse CRUD operations
 * Manages warehouse entities (locations/facilities), not warehouse orders
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type { Warehouse, CreateWarehouseRequest, UpdateWarehouseRequest } from '@/types/warehouse';

export const warehouseManagementService = {
  /**
   * Get all warehouses
   */
  async getAll(): Promise<Warehouse[]> {
    return apiFetch<Warehouse[]>(API_ENDPOINTS.logistics.warehouses);
  },

  /**
   * Get warehouse by ID
   */
  async getById(id: number): Promise<Warehouse> {
    return apiFetch<Warehouse>(API_ENDPOINTS.logistics.warehouse(id));
  },

  /**
   * Create a new warehouse
   */
  async create(warehouse: CreateWarehouseRequest): Promise<Warehouse> {
    return apiFetch<Warehouse>(API_ENDPOINTS.logistics.warehouses, {
      method: 'POST',
      body: JSON.stringify(warehouse),
    });
  },

  /**
   * Update an existing warehouse
   */
  async update(id: number, warehouse: UpdateWarehouseRequest): Promise<Warehouse> {
    return apiFetch<Warehouse>(API_ENDPOINTS.logistics.warehouse(id), {
      method: 'PUT',
      body: JSON.stringify(warehouse),
    });
  },

  /**
   * Delete a warehouse
   */
  async delete(id: number): Promise<void> {
    await apiFetch<{ message: string }>(API_ENDPOINTS.logistics.warehouse(id), {
      method: 'DELETE',
    });
  },

  /**
   * Toggle warehouse active status
   */
  async toggleActive(id: number, active: boolean): Promise<Warehouse> {
    const warehouse = await this.getById(id);
    return this.update(id, {
      ...warehouse,
      active,
    });
  },
};
