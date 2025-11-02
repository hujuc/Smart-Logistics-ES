/**
 * Carrier-related types
 * These types match the backend DTOs for carrier integration
 */

export interface Address {
  name?: string
  company?: string
  addressLine1: string
  addressLine2?: string
  city: string
  stateOrProvince?: string
  postalCode: string
  country: string
  residential?: boolean
  phone?: string
  email?: string
}

export interface Parcel {
  weight: number
  weightUnit: string
  length?: number
  width?: number
  height?: number
  distanceUnit?: string
}

export interface ShippingRateRequest {
  fromAddress: Address
  toAddress: Address
  parcels: Parcel[]
}

export interface RateQuote {
  provider: string
  carrierAccount: string
  carrierCode: string
  serviceLevel: string
  amount: number
  currency: string
  estimatedDays?: number
  deliveryDate?: string
}

export interface ShippingRateResponse {
  shipmentId: string
  rates: RateQuote[]
  messages: string[]
}

export interface CarrierResponse {
  id: number
  source: string
  externalId: string
  friendlyName?: string
  carrierName: string
  accountNumber: string
  active: boolean
}

export interface CarrierIntegrationSnapshot {
  source: string
  carriers: CarrierSourceDetails[]
}

export interface CarrierSourceDetails {
  externalId: string
  friendlyName?: string
  carrierName: string
  accountNumber: string
}

export interface AddressValidationRequest {
  address: Address
}

export interface AddressValidationResponse {
  valid: boolean
  originalAddress: Address
  validatedAddress?: Address
  messages: string[]
}
