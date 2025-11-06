/**
 * Carrier API Client
 * Handles all carrier-related API calls
 */

import { apiFetch } from './api'
import type {
  ShippingRateRequest,
  ShippingRateResponse,
  CarrierResponse,
  CarrierIntegrationSnapshot,
  AddressValidationRequest,
  AddressValidationResponse,
} from '@/types/carrier'

const CARRIER_BASE = '/api/admin/carriers'

/**
 * Calculate shipping rates for a shipment
 */
export async function calculateShippingRates(
  request: ShippingRateRequest
): Promise<ShippingRateResponse> {
  return apiFetch<ShippingRateResponse>(`${CARRIER_BASE}/rates/calculate`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

/**
 * Get all carriers
 */
export async function getAllCarriers(): Promise<CarrierResponse[]> {
  return apiFetch<CarrierResponse[]>(CARRIER_BASE)
}

/**
 * Get carrier integration snapshot (from external APIs like Shippo, ShipEngine)
 */
export async function getCarrierIntegrationSnapshot(): Promise<CarrierIntegrationSnapshot[]> {
  return apiFetch<CarrierIntegrationSnapshot[]>(`${CARRIER_BASE}/integrations/snapshot`)
}

/**
 * Synchronize carriers from external integrations
 */
export async function synchronizeCarriers(): Promise<CarrierResponse[]> {
  return apiFetch<CarrierResponse[]>(`${CARRIER_BASE}/integrations/sync`, {
    method: 'POST',
  })
}

/**
 * Validate an address using carrier APIs
 */
export async function validateAddress(
  request: AddressValidationRequest
): Promise<AddressValidationResponse> {
  return apiFetch<AddressValidationResponse>(`${CARRIER_BASE}/addresses/validate`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
