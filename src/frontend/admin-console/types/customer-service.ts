/**
 * Customer service-related types
 */

import type { IssueStatus, IssueType } from "./common"

export interface DeliveryIssue {
  id: string
  orderId: string
  issueType: IssueType
  status: IssueStatus
  priority: "Low" | "Medium" | "High"
  description: string
  reportedAt: Date
  resolvedAt?: Date
  assignedTo?: string
}

export interface CustomerServiceStats {
  openIssues: number
  resolvedToday: number
  avgResolutionTime: number // in hours
  customerSatisfaction: number // percentage
}

export interface SupportTicket {
  id: string
  ticketNumber: string
  customerId: string
  subject: string
  description: string
  status: IssueStatus
  priority: "Low" | "Medium" | "High"
  createdAt: Date
  updatedAt: Date
}

export interface TicketWithDetails extends SupportTicket {
  customerName?: string
  customerEmail?: string
  category?: string
  assignedTo?: string  // Display name of assigned user
  assignedToId?: string  // ID of assigned user (UUID from Keycloak)
  resolutionNotes?: string
  messages?: number
  resolutionHistory?: ResolutionNote[]
  exceptionDetails?: ExceptionDetails
}

export interface ExceptionDetails {
  type: string
  carrier?: string
  lastLocation?: string
  expectedDelivery?: string
  actualDelivery?: string
  reason?: string
}

export interface ResolutionNote {
  id: string
  ticketId: string
  note: string
  createdBy: string
  createdAt: Date
}

