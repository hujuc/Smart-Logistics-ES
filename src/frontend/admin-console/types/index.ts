/**
 * Central export point for all application types
 */

// Common types
export type {
  Priority,
  IssueStatus,
  ShipmentOrderStatus,
  IssueType,
  Carrier,
  Order,
  OrderEvent,
  OrderDocument,
  ContactInfo,
  Address,
  StatCard,
  DateRange,
} from "./common"

// Warehouse types
export type {
  WarehousePackage,
  WarehouseStats,
} from "./warehouse"

export { STATUS_COLORS, STATUS_LABELS } from "./warehouse"

// Logistics types
export type {
  LogisticsKPIs,
  CarrierPerformance,
  DeliveryMetrics,
  ShipmentCard,
} from "./logistics"

// Customer service types
export type {
  DeliveryIssue,
  CustomerServiceStats,
  SupportTicket,
} from "./customer-service"

// Carrier API types
export type {
  ShippingRateRequest,
  ShippingRateResponse,
  RateQuote,
  CarrierResponse,
  CarrierIntegrationSnapshot,
  CarrierSourceDetails,
  AddressValidationRequest,
  AddressValidationResponse,
  Parcel,
} from "./carrier"
