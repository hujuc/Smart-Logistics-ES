/**
 * Delivery Exception (Support Ticket) Types
 * 
 * These types map to the backend DeliveryException entity and DTOs
 * Used for customer support tickets related to order/delivery issues
 */

// ========================================
// Enums (must match backend Java enums)
// ========================================

/**
 * Type of delivery exception/issue
 * Maps to backend: Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.ExceptionType
 */
export enum ExceptionType {
  DAMAGED = 'DAMAGED',           // Package arrived damaged
  LOST = 'LOST',                 // Package missing/lost
  WRONG_ITEM = 'WRONG_ITEM',     // Wrong item received
  DELAYED = 'DELAYED',           // Significant delay
  WRONG_ADDRESS = 'WRONG_ADDRESS',       // Address issue
  NOT_DELIVERED = 'NOT_DELIVERED',       // Delivery not completed
  CUSTOMER_NOT_AVAILABLE = 'CUSTOMER_NOT_AVAILABLE',  // Driver-reported: Customer not available
  ACCESS_DENIED = 'ACCESS_DENIED',       // Driver-reported: Access denied to delivery location
  WEATHER = 'WEATHER',           // Driver-reported: Weather/force majeure
  OTHER = 'OTHER',               // Other issue
}

/**
 * Severity level of the exception
 * Maps to backend: DeliveryException.Severity
 */
export enum Severity {
  LOW = 'LOW',           // Minor issue, can wait
  MEDIUM = 'MEDIUM',     // Standard issue
  HIGH = 'HIGH',         // Important issue, needs attention
  CRITICAL = 'CRITICAL', // Urgent issue, immediate attention needed
}

/**
 * Source of the exception report
 * Maps to backend: DeliveryException.Source
 */
export enum Source {
  CUSTOMER = 'CUSTOMER',   // Reported by customer
  DRIVER = 'DRIVER',       // Reported by driver
  CARRIER = 'CARRIER',     // Reported by carrier
  SYSTEM = 'SYSTEM',       // Detected by system
}

/**
 * Current status of the exception
 * Maps to backend: DeliveryException.ExceptionStatus
 */
export enum ExceptionStatus {
  OPEN = 'OPEN',           // Newly created, not yet assigned
  ASSIGNED = 'ASSIGNED',   // Assigned to a CSR
  IN_PROGRESS = 'IN_PROGRESS', // CSR is working on it
  RESOLVED = 'RESOLVED',   // Issue resolved, awaiting closure
  CLOSED = 'CLOSED',       // Fully closed
  ESCALATED = 'ESCALATED', // Escalated to higher level
}

/**
 * Priority level for handling the exception
 * Maps to backend: DeliveryException.Priority
 */
export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

/**
 * Type of resolution applied
 * Maps to backend: DeliveryException.ResolutionType
 */
export enum ResolutionType {
  REFUND_ISSUED = 'REFUND_ISSUED',
  REPLACEMENT_SENT = 'REPLACEMENT_SENT',
  REDELIVERY_SCHEDULED = 'REDELIVERY_SCHEDULED',
  CUSTOMER_CONTACTED = 'CUSTOMER_CONTACTED',
  NO_ACTION_NEEDED = 'NO_ACTION_NEEDED',
  OTHER = 'OTHER',
}

// ========================================
// Interfaces
// ========================================

/**
 * Request to create a new delivery exception (support ticket)
 * Maps to backend: DeliveryExceptionRequest
 */
export interface DeliveryExceptionRequest {
  orderId: number;                    // Required: Which order has the issue
  type: ExceptionType;                // Required: Type of issue
  category: string;                   // Required: Category/subcategory
  description: string;                // Required: Description of the issue
  
  // Optional fields
  severity?: Severity;                // Defaults to MEDIUM if not provided
  source?: Source;                    // Defaults to CUSTOMER for customer portal
  reportedBy?: string;                // UUID of reporter (auto-filled from JWT)
  customerReported?: boolean;         // Always true for customer portal
  customerId?: string;                // UUID of customer (auto-filled from JWT)
  assignedTo?: string;                // UUID of CSR (usually null for customers)
  priority?: Priority;                // Defaults to MEDIUM if not provided
}

/**
 * Full delivery exception response from backend
 * Maps to backend: DeliveryExceptionResponse
 */
export interface DeliveryExceptionResponse {
  // Core fields
  id: string;                         // UUID
  orderId: number;
  type: ExceptionType;
  category: string;
  severity: Severity;
  description: string;
  status: ExceptionStatus;
  priority: Priority;
  
  // Source & Reporter info
  source: Source;
  reportedBy?: string;                // UUID
  reportedAt: string;                 // ISO timestamp - when exception was reported
  customerReported: boolean;
  customerId?: string;                // UUID
  
  // Assignment
  assignedTo?: string;                // UUID of CSR
  
  // Resolution
  resolutionType?: string;
  resolutionNotes?: string;
  resolvedBy?: string;                // UUID
  resolvedAt?: string;                // ISO timestamp
  
  // SLA tracking
  slaDeadline?: string;               // ISO timestamp
  slaBreached: boolean;
  
  // Customer feedback
  customerFeedback?: number;          // Rating
  feedbackComment?: string;
  
  // Audit
  updatedAt?: string;                 // ISO timestamp - last update
  history?: string;                   // Audit trail
}

/**
 * Request to update an existing exception
 * Maps to backend: DeliveryExceptionUpdateRequest
 * Note: Customers typically can't update exceptions, only CSRs can
 */
export interface DeliveryExceptionUpdateRequest {
  status?: ExceptionStatus;
  priority?: Priority;
  severity?: Severity;
  assignedTo?: string;                // UUID
  resolutionType?: ResolutionType;
  resolutionNotes?: string;
}

/**
 * Summary statistics for exceptions dashboard
 * Maps to backend: ExceptionSummaryResponse
 */
export interface ExceptionSummaryResponse {
  totalOpen: number;
  totalInProgress: number;
  totalResolved: number;
  totalClosed: number;
  totalEscalated: number;
  
  highPriority: number;
  criticalPriority: number;
  
  slaBreached: number;
  avgResolutionTimeHours: number;
}

// ========================================
// UI Helper Types
// ========================================

/**
 * Simplified ticket view for list display
 * Contains only essential fields for the tickets list
 */
export interface TicketListItem {
  id: string;
  ticketNumber: string;               // Formatted ID (e.g., "TKT-2025-001")
  subject: string;                    // Human-readable summary
  type: ExceptionType;
  status: ExceptionStatus;
  priority: Priority;
  severity: Severity;
  createdAt: string;
  lastUpdate: string;
}

/**
 * Categories for different exception types
 * Used in the ticket creation form
 */
export interface ExceptionCategory {
  type: ExceptionType;
  label: string;
  description: string;
  subcategories: string[];
}

/**
 * Filter options for tickets list
 */
export interface TicketFilters {
  status?: ExceptionStatus[];
  type?: ExceptionType[];
  priority?: Priority[];
  severity?: Severity[];
  dateFrom?: string;
  dateTo?: string;
  searchQuery?: string;
}

// ========================================
// Constants & Helpers
// ========================================

/**
 * Human-readable labels for exception types
 */
export const EXCEPTION_TYPE_LABELS: Record<ExceptionType, string> = {
  [ExceptionType.DAMAGED]: 'Damaged Package',
  [ExceptionType.LOST]: 'Lost Package',
  [ExceptionType.WRONG_ITEM]: 'Wrong Item',
  [ExceptionType.DELAYED]: 'Delivery Delayed',
  [ExceptionType.WRONG_ADDRESS]: 'Wrong Address',
  [ExceptionType.NOT_DELIVERED]: 'Not Delivered',
  [ExceptionType.CUSTOMER_NOT_AVAILABLE]: 'Customer Not Available',
  [ExceptionType.ACCESS_DENIED]: 'Access Denied',
  [ExceptionType.WEATHER]: 'Weather / Force Majeure',
  [ExceptionType.OTHER]: 'Other Issue',
};

/**
 * Human-readable labels for status
 */
export const STATUS_LABELS: Record<ExceptionStatus, string> = {
  [ExceptionStatus.OPEN]: 'Open',
  [ExceptionStatus.ASSIGNED]: 'Assigned',
  [ExceptionStatus.IN_PROGRESS]: 'In Progress',
  [ExceptionStatus.RESOLVED]: 'Resolved',
  [ExceptionStatus.CLOSED]: 'Closed',
  [ExceptionStatus.ESCALATED]: 'Escalated',
};

/**
 * Human-readable labels for priority
 */
export const PRIORITY_LABELS: Record<Priority, string> = {
  [Priority.LOW]: 'Low',
  [Priority.MEDIUM]: 'Medium',
  [Priority.HIGH]: 'High',
  [Priority.CRITICAL]: 'Critical',
};

/**
 * Human-readable labels for severity
 */
export const SEVERITY_LABELS: Record<Severity, string> = {
  [Severity.LOW]: 'Low',
  [Severity.MEDIUM]: 'Medium',
  [Severity.HIGH]: 'High',
  [Severity.CRITICAL]: 'Critical',
};

/**
 * Color classes for status badges (Tailwind CSS)
 */
export const STATUS_COLORS: Record<ExceptionStatus, string> = {
  [ExceptionStatus.OPEN]: 'bg-blue-100 text-blue-800',
  [ExceptionStatus.ASSIGNED]: 'bg-purple-100 text-purple-800',
  [ExceptionStatus.IN_PROGRESS]: 'bg-yellow-100 text-yellow-800',
  [ExceptionStatus.RESOLVED]: 'bg-green-100 text-green-800',
  [ExceptionStatus.CLOSED]: 'bg-gray-100 text-gray-800',
  [ExceptionStatus.ESCALATED]: 'bg-red-100 text-red-800',
};

/**
 * Color classes for priority badges (Tailwind CSS)
 */
export const PRIORITY_COLORS: Record<Priority, string> = {
  [Priority.LOW]: 'bg-blue-100 text-blue-800',
  [Priority.MEDIUM]: 'bg-orange-100 text-orange-800',
  [Priority.HIGH]: 'bg-red-100 text-red-800',
  [Priority.CRITICAL]: 'bg-red-600 text-white',
};

/**
 * Predefined exception categories with subcategories
 * Used in ticket creation form
 */
export const EXCEPTION_CATEGORIES: ExceptionCategory[] = [
  {
    type: ExceptionType.DAMAGED,
    label: 'Damaged Package',
    description: 'Package arrived damaged or broken',
    subcategories: [
      'Box damaged',
      'Item damaged inside',
      'Leaked contents',
      'Crushed package',
      'Other damage',
    ],
  },
  {
    type: ExceptionType.LOST,
    label: 'Lost Package',
    description: 'Package not received or lost',
    subcategories: [
      'Never arrived',
      'Lost in transit',
      'Stolen from doorstep',
      'Missing items from package',
      'Other',
    ],
  },
  {
    type: ExceptionType.WRONG_ITEM,
    label: 'Wrong Item',
    description: 'Received incorrect item',
    subcategories: [
      'Different product',
      'Wrong size',
      'Wrong color',
      'Wrong quantity',
      'Other',
    ],
  },
  {
    type: ExceptionType.DELAYED,
    label: 'Delivery Delayed',
    description: 'Significant delivery delay',
    subcategories: [
      'Past estimated delivery date',
      'No tracking updates',
      'Stuck in transit',
      'Carrier delay',
      'Other',
    ],
  },
  {
    type: ExceptionType.WRONG_ADDRESS,
    label: 'Wrong Address',
    description: 'Address issue or incorrect location',
    subcategories: [
      'Wrong address on package',
      'Delivered to wrong location',
      'Address not found',
      'Need to update address',
      'Other',
    ],
  },
  {
    type: ExceptionType.NOT_DELIVERED,
    label: 'Not Delivered',
    description: 'Delivery was not completed',
    subcategories: [
      'No one available to receive',
      'Refused by recipient',
      'Access denied',
      'Other',
    ],
  },
  {
    type: ExceptionType.CUSTOMER_NOT_AVAILABLE,
    label: 'Customer Not Available',
    description: 'Customer was not available to receive delivery (Driver-reported)',
    subcategories: [
      'No one home',
      'No answer at door',
      'Business closed',
      'Recipient unavailable',
      'Other',
    ],
  },
  {
    type: ExceptionType.ACCESS_DENIED,
    label: 'Access Denied',
    description: 'Unable to access delivery location (Driver-reported)',
    subcategories: [
      'Gated community - no access code',
      'Security denied entry',
      'Building locked',
      'Road closed',
      'Unsafe conditions',
      'Other',
    ],
  },
  {
    type: ExceptionType.WEATHER,
    label: 'Weather / Force Majeure',
    description: 'Extreme weather or uncontrollable events preventing delivery (Driver-reported)',
    subcategories: [
      'Severe storm',
      'Flooding',
      'Snow/ice conditions',
      'Road closures',
      'Natural disaster',
      'Other',
    ],
  },
  {
    type: ExceptionType.OTHER,
    label: 'Other Issue',
    description: 'Other delivery or order issue',
    subcategories: [
      'Quality issue',
      'Missing documentation',
      'Customs issue',
      'Other',
    ],
  },
];
