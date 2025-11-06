/**
 * Carrier Service - API calls for carrier operations
 * Handles carrier management and synchronization from external providers (Shippo, ShipStation)
 */

import { API_ENDPOINTS, apiFetch } from '../api';

export interface CarrierResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  supportEmail?: string;
  supportPhone?: string;
  website?: string;
  active: boolean;
  shippoCarrierId?: string;
  shippoAccountId?: string;
  shipEngineCarrierId?: string;
  shipEngineAccountNumber?: string;
  serviceLevelsSummary?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateOrProvince?: string;
  postalCode?: string;
  country?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCarrierRequest {
  code: string;
  name: string;
  description?: string;
  supportEmail?: string;
  supportPhone?: string;
  website?: string;
  active?: boolean;
  shippoCarrierId?: string;
  shippoAccountId?: string;
  shipEngineCarrierId?: string;
  shipEngineAccountNumber?: string;
  serviceLevelsSummary?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  stateOrProvince?: string;
  postalCode?: string;
  country?: string;
}

export const carrierService = {
  /**
   * Get all carriers from the database
   */
  async getAllCarriers(): Promise<CarrierResponse[]> {
    return apiFetch<CarrierResponse[]>(API_ENDPOINTS.admin.carriers);
  },

  /**
   * Get carrier by ID
   */
  async getCarrierById(id: number): Promise<CarrierResponse> {
    return apiFetch<CarrierResponse>(API_ENDPOINTS.admin.carrier(id));
  },

  /**
   * Create a new carrier
   */
  async createCarrier(carrier: CreateCarrierRequest): Promise<CarrierResponse> {
    return apiFetch<CarrierResponse>(API_ENDPOINTS.admin.carriers, {
      method: 'POST',
      body: JSON.stringify(carrier),
    });
  },

  /**
   * Update an existing carrier
   */
  async updateCarrier(id: number, carrier: Partial<CreateCarrierRequest>): Promise<CarrierResponse> {
    return apiFetch<CarrierResponse>(API_ENDPOINTS.admin.carrier(id), {
      method: 'PUT',
      body: JSON.stringify(carrier),
    });
  },

  /**
   * Delete a carrier
   */
  async deleteCarrier(id: number): Promise<void> {
    await apiFetch<void>(API_ENDPOINTS.admin.carrier(id), {
      method: 'DELETE',
    });
  },

  /**
   * Synchronize carriers from external providers (Shippo, ShipStation)
   * Should be called on app startup or manually by admin
   */
  async syncCarriers(): Promise<CarrierResponse[]> {
    return apiFetch<CarrierResponse[]>(API_ENDPOINTS.admin.carrierSync, {
      method: 'POST',
    });
  },

  /**
   * Find a carrier by name (case-insensitive)
   * Returns null if not found
   */
  async findCarrierByName(name: string): Promise<CarrierResponse | null> {
    const carriers = await this.getAllCarriers();
    const normalizedName = name.toLowerCase().trim();

    return carriers.find(c =>
      c.name.toLowerCase().trim() === normalizedName ||
      c.code.toLowerCase().trim() === normalizedName
    ) || null;
  },

  /**
   * Get or create a carrier by name
   * If carrier doesn't exist, creates it with minimal info
   */
  async getOrCreateCarrierByName(name: string): Promise<CarrierResponse> {
    // Try to find existing carrier
    const existing = await this.findCarrierByName(name);
    if (existing) {
      return existing;
    }

    // Create new carrier with minimal info
    const code = name.toUpperCase().replace(/\s+/g, '_').substring(0, 64);
    return this.createCarrier({
      code,
      name,
      description: `Auto-created carrier for ${name}`,
      active: true,
    });
  },
};
