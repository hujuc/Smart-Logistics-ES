/**
 * Delivery Exception (Support Tickets) Service
 * 
 * API service for managing support tickets (delivery exceptions)
 * Connects to the CustomerBffController backend endpoints
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type {
  DeliveryExceptionRequest,
  DeliveryExceptionResponse,
  ExceptionSummaryResponse,
  TicketFilters,
} from '@/types/exception';

/**
 * API endpoints for delivery exceptions
 */
const EXCEPTION_ENDPOINTS = {
  // Get all exceptions for current customer
  getAll: `${API_ENDPOINTS.base}/delivery-exceptions`,
  
  // Get exception by ID
  getById: (id: string) => `${API_ENDPOINTS.base}/delivery-exceptions/${id}`,
  
  // Create new exception
  create: `${API_ENDPOINTS.base}/delivery-exceptions`,
  
  // Get exceptions for specific order
  getByOrderId: (orderId: number) => `${API_ENDPOINTS.base}/orders/${orderId}/exceptions`,
};

/**
 * Delivery Exception Service
 * Provides methods to interact with the backend API
 */
export const exceptionService = {
  /**
   * Get all delivery exceptions for the authenticated customer
   * @returns List of customer's exceptions/tickets
   */
  async getAll(): Promise<DeliveryExceptionResponse[]> {
    console.log('[ExceptionService] Fetching all exceptions for customer');
    
    try {
      const exceptions = await apiFetch<DeliveryExceptionResponse[]>(
        EXCEPTION_ENDPOINTS.getAll,
        {
          method: 'GET',
        }
      );
      
      console.log('[ExceptionService] Successfully fetched', exceptions.length, 'exceptions');
      return exceptions;
    } catch (error) {
      console.error('[ExceptionService] Failed to fetch exceptions:', error);
      throw error;
    }
  },

  /**
   * Get delivery exception by ID
   * @param id - Exception UUID
   * @returns Exception details
   */
  async getById(id: string): Promise<DeliveryExceptionResponse> {
    console.log('[ExceptionService] Fetching exception:', id);
    
    try {
      const exception = await apiFetch<DeliveryExceptionResponse>(
        EXCEPTION_ENDPOINTS.getById(id),
        {
          method: 'GET',
        }
      );
      
      console.log('[ExceptionService] Successfully fetched exception:', id);
      return exception;
    } catch (error) {
      console.error('[ExceptionService] Failed to fetch exception:', id, error);
      throw error;
    }
  },

  /**
   * Create a new delivery exception (support ticket)
   * @param request - Exception creation request
   * @returns Created exception
   */
  async create(request: DeliveryExceptionRequest): Promise<DeliveryExceptionResponse> {
    console.log('[ExceptionService] Creating new exception for order:', request.orderId);
    
    try {
      const exception = await apiFetch<DeliveryExceptionResponse>(
        EXCEPTION_ENDPOINTS.create,
        {
          method: 'POST',
          body: JSON.stringify(request),
        }
      );
      
      console.log('[ExceptionService] Successfully created exception:', exception.id);
      return exception;
    } catch (error) {
      console.error('[ExceptionService] Failed to create exception:', error);
      throw error;
    }
  },

  /**
   * Get all exceptions for a specific order
   * @param orderId - Order ID
   * @returns List of exceptions for the order
   */
  async getByOrderId(orderId: number): Promise<DeliveryExceptionResponse[]> {
    console.log('[ExceptionService] Fetching exceptions for order:', orderId);
    
    try {
      const exceptions = await apiFetch<DeliveryExceptionResponse[]>(
        EXCEPTION_ENDPOINTS.getByOrderId(orderId),
        {
          method: 'GET',
        }
      );
      
      console.log('[ExceptionService] Successfully fetched', exceptions.length, 'exceptions for order:', orderId);
      return exceptions;
    } catch (error) {
      console.error('[ExceptionService] Failed to fetch exceptions for order:', orderId, error);
      throw error;
    }
  },

  /**
   * Filter exceptions locally (client-side filtering)
   * Use this when you already have all exceptions loaded
   * @param exceptions - List of exceptions to filter
   * @param filters - Filter criteria
   * @returns Filtered exceptions
   */
  filterLocal(
    exceptions: DeliveryExceptionResponse[],
    filters: TicketFilters
  ): DeliveryExceptionResponse[] {
    console.log('[ExceptionService] Filtering', exceptions.length, 'exceptions locally');
    
    let filtered = exceptions;

    // Filter by status
    if (filters.status && filters.status.length > 0) {
      filtered = filtered.filter(e => filters.status!.includes(e.status));
    }

    // Filter by type
    if (filters.type && filters.type.length > 0) {
      filtered = filtered.filter(e => filters.type!.includes(e.type));
    }

    // Filter by priority
    if (filters.priority && filters.priority.length > 0) {
      filtered = filtered.filter(e => filters.priority!.includes(e.priority));
    }

    // Filter by severity
    if (filters.severity && filters.severity.length > 0) {
      filtered = filtered.filter(e => filters.severity!.includes(e.severity));
    }

    // Filter by date range
    if (filters.dateFrom) {
      const fromDate = new Date(filters.dateFrom);
      filtered = filtered.filter(e => new Date(e.reportedAt) >= fromDate);
    }

    if (filters.dateTo) {
      const toDate = new Date(filters.dateTo);
      filtered = filtered.filter(e => new Date(e.reportedAt) <= toDate);
    }

    // Search query (searches in description and category)
    if (filters.searchQuery && filters.searchQuery.trim() !== '') {
      const query = filters.searchQuery.toLowerCase();
      filtered = filtered.filter(e =>
        e.description.toLowerCase().includes(query) ||
        e.category.toLowerCase().includes(query) ||
        e.id.toLowerCase().includes(query)
      );
    }

    console.log('[ExceptionService] Filtered to', filtered.length, 'exceptions');
    return filtered;
  },

  /**
   * Calculate summary statistics from exceptions list
   * @param exceptions - List of exceptions
   * @returns Summary statistics
   */
  calculateSummary(exceptions: DeliveryExceptionResponse[]): ExceptionSummaryResponse {
    console.log('[ExceptionService] Calculating summary for', exceptions.length, 'exceptions');
    
    const totalOpen = exceptions.filter(e => e.status === 'OPEN').length;
    const totalInProgress = exceptions.filter(e => e.status === 'IN_PROGRESS' || e.status === 'ASSIGNED').length;
    const totalResolved = exceptions.filter(e => e.status === 'RESOLVED').length;
    const totalClosed = exceptions.filter(e => e.status === 'CLOSED').length;
    const totalEscalated = exceptions.filter(e => e.status === 'ESCALATED').length;
    
    const highPriority = exceptions.filter(e => e.priority === 'HIGH').length;
    const criticalPriority = exceptions.filter(e => e.priority === 'CRITICAL').length;
    
    const slaBreached = exceptions.filter(e => e.slaBreached).length;
    
    // Calculate average resolution time (for resolved exceptions)
    const resolvedExceptions = exceptions.filter(e => e.resolvedAt);
    let avgResolutionTimeHours = 0;
    
    if (resolvedExceptions.length > 0) {
      const totalHours = resolvedExceptions.reduce((sum, e) => {
        const reported = new Date(e.reportedAt);
        const resolved = new Date(e.resolvedAt!);
        const hours = (resolved.getTime() - reported.getTime()) / (1000 * 60 * 60);
        return sum + hours;
      }, 0);
      
      avgResolutionTimeHours = totalHours / resolvedExceptions.length;
    }
    
    return {
      totalOpen,
      totalInProgress,
      totalResolved,
      totalClosed,
      totalEscalated,
      highPriority,
      criticalPriority,
      slaBreached,
      avgResolutionTimeHours,
    };
  },

  /**
   * Format exception for display in list
   * @param exception - Exception to format
   * @returns Formatted ticket number
   */
  formatTicketNumber(exception: DeliveryExceptionResponse): string {
    // Format: TKT-2025-001 (last 3 chars of UUID)
    const year = new Date(exception.reportedAt).getFullYear();
    const shortId = exception.id.substring(exception.id.length - 3).toUpperCase();
    return `TKT-${year}-${shortId}`;
  },

  /**
   * Get human-readable subject/title from exception
   * @param exception - Exception
   * @returns Subject line
   */
  getSubject(exception: DeliveryExceptionResponse): string {
    // Use the description (truncated) as subject
    const maxLength = 50;
    if (exception.description.length <= maxLength) {
      return exception.description;
    }
    return exception.description.substring(0, maxLength) + '...';
  },

  /**
   * Check if exception is overdue (past SLA deadline)
   * @param exception - Exception to check
   * @returns true if overdue
   */
  isOverdue(exception: DeliveryExceptionResponse): boolean {
    if (!exception.slaDeadline) return false;
    if (exception.status === 'RESOLVED' || exception.status === 'CLOSED') return false;
    
    const deadline = new Date(exception.slaDeadline);
    const now = new Date();
    return now > deadline;
  },

  /**
   * Get time remaining until SLA deadline
   * @param exception - Exception to check
   * @returns Human-readable time remaining, or null if no deadline
   */
  getTimeRemaining(exception: DeliveryExceptionResponse): string | null {
    if (!exception.slaDeadline) return null;
    if (exception.status === 'RESOLVED' || exception.status === 'CLOSED') return null;
    
    const deadline = new Date(exception.slaDeadline);
    const now = new Date();
    const diffMs = deadline.getTime() - now.getTime();
    
    if (diffMs < 0) {
      // Overdue
      const hours = Math.floor(Math.abs(diffMs) / (1000 * 60 * 60));
      return `Overdue by ${hours}h`;
    } else {
      // Time remaining
      const hours = Math.floor(diffMs / (1000 * 60 * 60));
      if (hours < 24) {
        return `${hours}h remaining`;
      } else {
        const days = Math.floor(hours / 24);
        return `${days}d remaining`;
      }
    }
  },
};

// Export type for use in components
export type ExceptionService = typeof exceptionService;
