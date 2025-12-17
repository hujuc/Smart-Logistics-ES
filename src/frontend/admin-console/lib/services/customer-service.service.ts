/**
 * Customer Service API - API calls for customer service operations
 * Currently using mock data - ready to connect to backend when available
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import { getUserData } from '@/shared/lib/auth';
import type { Order, OrderEvent } from '@/types';
import type { IssueStatus } from '@/types/common';
import type {
  TicketWithDetails,
  ResolutionNote,
  DeliveryIssue,
  CustomerServiceStats
} from '@/types/customer-service';

// ========================================
// Real API methods for Order Lookup
// ========================================

interface OrderTrackingInfo {
  orderId: number;
  trackingNumber: string;
  status: string;
  estimatedDelivery: string;
  events: unknown[];
}

export const customerServiceOrderApi = {
  /**
   * Search orders by reference or tracking number
   */
  async searchOrders(query: string): Promise<Order[]> {
    return apiFetch<Order[]>(API_ENDPOINTS.customerService.ordersSearch(query));
  },

  /**
   * Get order by ID
   */
  async getOrderById(id: number): Promise<Order> {
    return apiFetch<Order>(API_ENDPOINTS.customerService.order(id));
  },

  /**
   * Get order events (timeline)
   */
  async getOrderEvents(id: number): Promise<OrderEvent[]> {
    return apiFetch<OrderEvent[]>(API_ENDPOINTS.customerService.orderEvents(id));
  },

  /**
   * Get order tracking information
   */
  async getOrderTracking(id: number): Promise<OrderTrackingInfo> {
    return apiFetch<OrderTrackingInfo>(API_ENDPOINTS.customerService.orderTracking(id));
  },

  /**
   * Get order with events in a single optimized call (Version B)
   * This endpoint reduces latency by fetching both order and events in one request
   */
  async getOrderWithEvents(id: number): Promise<{ order: Order; events: OrderEvent[] }> {
    return apiFetch<{ order: Order; events: OrderEvent[] }>(
      API_ENDPOINTS.customerService.orderWithEvents(id),
      {
        method: 'GET',
      }
    );
  },
};

// ========================================
// Real API methods for Delivery Exceptions (Tickets)
// ========================================

/**
 * Backend DeliveryException types matching the Java backend
 */
export interface DeliveryException {
  id: string;
  orderId: number;
  customerId: string | null;
  type: 'DAMAGED_PACKAGE' | 'LOST_PACKAGE' | 'DELAYED_DELIVERY' | 'WRONG_ADDRESS' | 'CUSTOMER_NOT_AVAILABLE' | 'WEATHER_DELAY' | 'CUSTOMS_HOLD' | 'OTHER';
  category: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  description: string;
  resolutionNotes?: string;
  reportedAt: string;
  resolvedAt?: string;
  createdAt: string;
  updatedAt: string;
  source: 'CUSTOMER' | 'SYSTEM' | 'CARRIER' | 'WAREHOUSE';
  assignedTo?: string;
  carrierName?: string;
  trackingNumber?: string;
}

export interface DeliveryExceptionRequest {
  orderId: number;
  customerId: string | null;
  type: DeliveryException['type'];
  category: string;
  severity: DeliveryException['severity'];
  priority: DeliveryException['priority'];
  description: string;
  source: DeliveryException['source'];
  assignedTo?: string;
}

export interface DeliveryExceptionUpdateRequest {
  status?: DeliveryException['status'];
  priority?: DeliveryException['priority'];
  severity?: DeliveryException['severity'];
  assignedTo?: string;
  resolutionNotes?: string;
}

export interface ExceptionSummaryResponse {
  totalOpen: number;
  totalInProgress: number;
  totalResolved: number;
  totalClosed: number;
  highPriority: number;
  criticalSeverity: number;
  avgResolutionTimeHours: number;
}

export const customerServiceExceptionsApi = {
  /**
   * Get all delivery exceptions with optional filters
   */
  async getAll(filters?: {
    status?: DeliveryException['status'];
    priority?: DeliveryException['priority'];
    assignedTo?: string;
    severity?: DeliveryException['severity'];
  }): Promise<DeliveryException[]> {
    const endpoint = filters
      ? API_ENDPOINTS.customerService.exceptionsWithFilters(filters)
      : API_ENDPOINTS.customerService.exceptions;
    return apiFetch<DeliveryException[]>(endpoint);
  },

  /**
   * Get exception by ID
   */
  async getById(id: string): Promise<DeliveryException> {
    return apiFetch<DeliveryException>(API_ENDPOINTS.customerService.exception(id));
  },

  /**
   * Create a new delivery exception
   */
  async create(request: DeliveryExceptionRequest): Promise<DeliveryException> {
    return apiFetch<DeliveryException>(API_ENDPOINTS.customerService.exceptions, {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  /**
   * Update an existing exception
   */
  async update(id: string, request: DeliveryExceptionUpdateRequest): Promise<DeliveryException> {
    return apiFetch<DeliveryException>(API_ENDPOINTS.customerService.exception(id), {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  },

  /**
   * Close an exception with resolution notes
   */
  async close(id: string, resolutionNotes?: string): Promise<DeliveryException> {
    return apiFetch<DeliveryException>(API_ENDPOINTS.customerService.exceptionClose(id), {
      method: 'POST',
      body: JSON.stringify({ resolutionNotes }),
    });
  },

  /**
   * Contact carrier about an exception
   */
  async contactCarrier(id: string, message: string): Promise<{ status: string; message: string }> {
    return apiFetch<{ status: string; message: string }>(
      API_ENDPOINTS.customerService.exceptionContactCarrier(id),
      {
        method: 'POST',
        body: JSON.stringify({ message }),
      }
    );
  },

  /**
   * Contact customer about an exception
   */
  async contactCustomer(id: string, message: string): Promise<{ status: string; message: string }> {
    return apiFetch<{ status: string; message: string }>(
      API_ENDPOINTS.customerService.exceptionContactCustomer(id),
      {
        method: 'POST',
        body: JSON.stringify({ message }),
      }
    );
  },

  /**
   * Get exception statistics summary
   */
  async getSummary(): Promise<ExceptionSummaryResponse> {
    return apiFetch<ExceptionSummaryResponse>(API_ENDPOINTS.customerService.dashboardSummary);
  },
};

// ========================================
// Legacy Mock data for backwards compatibility
// Will be removed once all components migrate to real API
// ========================================

/**
 * User information from Keycloak
 */
interface UserInfo {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  realmRoles?: string[];
}

/**
 * Cache for user information to avoid repeated API calls
 */
const userInfoCache = new Map<string, UserInfo>();

/**
 * Check if a string is a valid UUID format
 */
function isValidUUID(str: string): boolean {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  return uuidRegex.test(str);
}

/**
 * Fetch user information from backend
 */
async function fetchUserInfo(userId: string | null | undefined): Promise<UserInfo | null> {
  // Return null if userId is null or undefined
  if (!userId) {
    return null;
  }

  // If userId is not a valid UUID (e.g., it's a username like "customer"),
  // skip the API call and return null to use the fallback formatting
  if (!isValidUUID(userId)) {
    return null;
  }

  // Check cache first
  if (userInfoCache.has(userId)) {
    return userInfoCache.get(userId)!;
  }

  try {
    const userInfo = await apiFetch<UserInfo>(`/api/users/${userId}`);
    userInfoCache.set(userId, userInfo);
    return userInfo;
  } catch (error) {
    console.error(`Failed to fetch user info for ${userId}:`, error);
    return null;
  }
}

/**
 * Helper function to get customer name from user ID
 */
async function getCustomerName(customerId: string | null | undefined): Promise<string> {
  // Return fallback if customerId is null or undefined
  if (!customerId) {
    return 'Unknown Customer';
  }

  const userInfo = await fetchUserInfo(customerId);

  if (userInfo && userInfo.firstName && userInfo.lastName) {
    return `${userInfo.firstName} ${userInfo.lastName}`;
  }

  // Fallback to formatted customer ID
  return formatCustomerName(customerId);
}

/**
 * Helper function to get assignee name from user ID
 */
async function getAssigneeName(assigneeId: string | null | undefined): Promise<string> {
  if (!assigneeId) {
    return 'Unassigned';
  }

  const userInfo = await fetchUserInfo(assigneeId);
  
  if (userInfo && userInfo.firstName && userInfo.lastName) {
    return `${userInfo.firstName} ${userInfo.lastName}`;
  }
  
  // Fallback to "Unassigned" if we can't get the info
  return 'Unassigned';
}

/**
 * Helper function to format customer ID into a more readable name
 * Used as fallback when user info is not available
 */
function formatCustomerName(customerId: string | null | undefined): string {
  // Return default if customerId is null or undefined
  if (!customerId) {
    return 'Unknown Customer';
  }

  // If it looks like an email, extract the name part
  if (customerId.includes('@')) {
    const namePart = customerId.split('@')[0];
    return namePart.split('.').map(part =>
      part.charAt(0).toUpperCase() + part.slice(1)
    ).join(' ');
  }

  // If it's a UUID (contains dashes and is long), show first part only
  if (customerId.includes('-') && customerId.length > 20) {
    return `Customer ${customerId.split('-')[0].toUpperCase()}`;
  }

  // If it's a simple username (like "customer"), capitalize it
  if (customerId.length < 20 && !customerId.includes('-')) {
    return customerId.charAt(0).toUpperCase() + customerId.slice(1);
  }

  // Otherwise, just capitalize
  return customerId.charAt(0).toUpperCase() + customerId.slice(1);
}

/**
 * Helper function to map backend DeliveryException to frontend TicketWithDetails
 */
async function mapExceptionToTicket(exception: DeliveryException): Promise<TicketWithDetails> {
  // Map exception type to human-readable format (for display purposes)
  const typeDisplayMap: Record<DeliveryException['type'], string> = {
    'DAMAGED_PACKAGE': 'Damaged Package',
    'LOST_PACKAGE': 'Lost Package',
    'DELAYED_DELIVERY': 'Delivery Delayed',
    'WRONG_ADDRESS': 'Wrong Address',
    'CUSTOMER_NOT_AVAILABLE': 'Customer Not Available',
    'WEATHER_DELAY': 'Weather Delay',
    'CUSTOMS_HOLD': 'Customs Hold',
    'OTHER': 'Other',
  };

  // Map status to frontend format
  const statusMap: Record<DeliveryException['status'], IssueStatus> = {
    'OPEN': 'Open',
    'IN_PROGRESS': 'In Progress',
    'RESOLVED': 'Resolved',
    'CLOSED': 'Closed',
  };

  // Map severity to priority display (showing severity as priority in UI)
  const severityToPriorityMap: Record<DeliveryException['severity'], 'Low' | 'Medium' | 'High'> = {
    'LOW': 'Low',
    'MEDIUM': 'Medium',
    'HIGH': 'High',
    'CRITICAL': 'High', // Map CRITICAL severity to High priority for display
  };

  // Get customer ID - if not in exception, fetch from order
  let customerIdToUse = exception.customerId;
  if (!customerIdToUse && exception.orderId) {
    try {
      const order = await customerServiceOrderApi.getOrderById(exception.orderId);
      customerIdToUse = order.customerId || null;
    } catch (error) {
      console.error(`Failed to fetch order ${exception.orderId} to get customer ID:`, error);
    }
  }

  // Get customer name from user service
  const customerName = await getCustomerName(customerIdToUse);
  
  // Get assignee name from user service
  const assigneeName = await getAssigneeName(exception.assignedTo);

  const ticket: TicketWithDetails = {
    id: exception.id,
    ticketNumber: `TKT-${exception.id.substring(0, 8)}`,
    customerId: customerIdToUse,
    customerName: customerName,
    customerEmail: customerIdToUse && customerIdToUse.includes('@') ? customerIdToUse : undefined,
    subject: exception.category, // Use category as the main subject
    description: exception.description,
    status: statusMap[exception.status] || 'Open',
    priority: severityToPriorityMap[exception.severity] || 'Medium',
    category: exception.category, // Use the category field directly from backend
    assignedTo: assigneeName,  // Display name
    assignedToId: exception.assignedTo,  // Keep the UUID for form selection
    orderId: exception.orderId,  // Order ID for navigation
    messages: 0,
    createdAt: exception.createdAt ? new Date(exception.createdAt) : new Date(),
    updatedAt: exception.updatedAt ? new Date(exception.updatedAt) : new Date(),
    resolutionNotes: exception.resolutionNotes,
  };

  // Add exception details with proper formatting
  ticket.exceptionDetails = {
    type: typeDisplayMap[exception.type] || exception.type,
    carrier: exception.carrierName,
    lastLocation: 'Unknown',
    expectedDelivery: '',
    reason: exception.description,
  };

  // Add resolution history if notes exist
  if (exception.resolutionNotes) {
    // Split by double newline separator (notes are concatenated)
    const noteParts = exception.resolutionNotes.split('\n\n');
    
    ticket.resolutionHistory = noteParts.map((notePart, index) => {
      // Parse format: [timestamp] author: note
      const match = notePart.match(/^\[(.*?)\]\s*(.*?):\s*([\s\S]*)$/);
      
      if (match) {
        const [, timestamp, author, noteText] = match;
        return {
          id: `${index + 1}`,
          ticketId: exception.id,
          note: noteText.trim(),
          createdBy: author.trim(),
          createdAt: new Date(timestamp),
        };
      }
      
      // Fallback if format doesn't match (old notes without format)
      return {
        id: `${index + 1}`,
        ticketId: exception.id,
        note: notePart.trim(),
        createdBy: exception.assignedTo || 'System',
        createdAt: new Date(exception.updatedAt),
      };
    });
  }

  return ticket;
}

const MOCK_TICKETS: TicketWithDetails[] = [
  {
    id: 'TKT001',
    ticketNumber: 'TKT-2025-001',
    customerId: 'CUST-123',
    customerName: 'John Doe',
    customerEmail: 'john.doe@example.com',
    subject: 'Package not delivered',
    description: 'Customer reports that package was not delivered despite tracking showing it as delivered. Customer checked with neighbors and building reception but package cannot be located.',
    status: 'Open',
    priority: 'High',
    category: 'Delivery Issue',
    assignedTo: 'Agent Smith',
    messages: 3,
    createdAt: new Date('2025-10-06T09:30:00'),
    updatedAt: new Date('2025-10-06T10:15:00'),
    exceptionDetails: {
      type: 'Failed Delivery',
      carrier: 'DHL Express',
      lastLocation: 'Local Distribution Center',
      expectedDelivery: '2025-10-05',
      reason: 'Marked as delivered but customer did not receive package'
    }
  },
  {
    id: 'TKT002',
    ticketNumber: 'TKT-2025-002',
    customerId: 'CUST-456',
    customerName: 'Jane Smith',
    customerEmail: 'jane.smith@example.com',
    subject: 'Unable to track package',
    description: 'Customer cannot track their package using the tracking number provided. The tracking system shows "No information available" for the tracking number.',
    status: 'In Progress',
    priority: 'Medium',
    category: 'Tracking',
    assignedTo: 'Agent Jones',
    messages: 5,
    createdAt: new Date('2025-10-05T14:20:00'),
    updatedAt: new Date('2025-10-06T08:45:00'),
    exceptionDetails: {
      type: 'Tracking Issue',
      carrier: 'FedEx',
      lastLocation: 'Origin Facility',
      expectedDelivery: '2025-10-08',
      reason: 'Tracking number not yet active in system'
    },
    resolutionNotes: 'Contacted carrier to verify tracking number. Carrier confirmed package is in transit and tracking will update within 24 hours.'
  },
  {
    id: 'TKT003',
    ticketNumber: 'TKT-2025-003',
    customerId: 'CUST-789',
    customerName: 'Bob Williams',
    customerEmail: 'bob.williams@example.com',
    subject: 'Request refund for damaged item',
    description: 'Customer received package but contents were damaged. Box was crushed and items inside were broken. Customer has provided photos of the damage.',
    status: 'Escalated',
    priority: 'High',
    category: 'Refund',
    assignedTo: 'Manager Brown',
    messages: 8,
    createdAt: new Date('2025-10-04T11:00:00'),
    updatedAt: new Date('2025-10-05T16:30:00'),
    exceptionDetails: {
      type: 'Damaged Package',
      carrier: 'UPS',
      lastLocation: 'Delivered',
      expectedDelivery: '2025-10-04',
      actualDelivery: '2025-10-04',
      reason: 'Package arrived damaged, contents broken'
    }
  },
  {
    id: 'TKT004',
    ticketNumber: 'TKT-2025-004',
    customerId: 'CUST-321',
    customerName: 'Alice Johnson',
    customerEmail: 'alice.johnson@example.com',
    subject: 'Question about shipping rates',
    description: 'Customer inquiring about shipping rates for international delivery to Canada.',
    status: 'Resolved',
    priority: 'Low',
    category: 'General Inquiry',
    assignedTo: 'Agent Davis',
    messages: 2,
    createdAt: new Date('2025-10-03T10:15:00'),
    updatedAt: new Date('2025-10-04T09:00:00'),
    resolutionNotes: 'Provided customer with shipping rate information and estimated delivery times for Canada. Customer satisfied with the response.'
  }
];

const MOCK_STATS: CustomerServiceStats = {
  openIssues: 15,
  resolvedToday: 8,
  avgResolutionTime: 2.5,
  customerSatisfaction: 94.5
};

export const customerServiceService = {
  /**
   * Get all support tickets (maps to delivery exceptions)
   */
  async getAllTickets(filters?: {
    status?: string;
    priority?: string;
    search?: string;
  }): Promise<TicketWithDetails[]> {
    const apiFilters: {
      status?: DeliveryException['status'];
      priority?: DeliveryException['priority'];
    } = {};

    // Map filter values to API format
    if (filters?.status && filters.status !== 'All') {
      apiFilters.status = filters.status.toUpperCase().replace(' ', '_') as DeliveryException['status'];
    }
    if (filters?.priority) {
      apiFilters.priority = filters.priority.toUpperCase() as DeliveryException['priority'];
    }

    const exceptions = await customerServiceExceptionsApi.getAll(apiFilters);

    // Map backend exceptions to frontend ticket format (with async user lookups)
    let tickets = await Promise.all(exceptions.map(mapExceptionToTicket));

    // Apply client-side search filter
    if (filters?.search) {
      const search = filters.search.toLowerCase();
      tickets = tickets.filter(t =>
        t.ticketNumber.toLowerCase().includes(search) ||
        t.customerName?.toLowerCase().includes(search) ||
        t.subject.toLowerCase().includes(search)
      );
    }

    return tickets;
  },

  /**
   * Get ticket by ID (maps to delivery exception)
   */
  async getTicketById(id: string): Promise<TicketWithDetails> {
    const exception = await customerServiceExceptionsApi.getById(id);
    return await mapExceptionToTicket(exception);
  },

  /**
   * Update a support ticket (maps to exception update)
   */
  async updateTicket(id: string, updates: {
    status?: string;
    priority?: 'Low' | 'Medium' | 'High';
    assignedTo?: string;
    resolutionNotes?: string;
  }): Promise<TicketWithDetails> {
    const request: DeliveryExceptionUpdateRequest = {};

    if (updates.status) {
      request.status = updates.status.toUpperCase().replace(' ', '_') as DeliveryException['status'];
    }
    if (updates.priority) {
      request.priority = updates.priority.toUpperCase() as DeliveryException['priority'];
    }
    if (updates.assignedTo) {
      request.assignedTo = updates.assignedTo;
    }
    if (updates.resolutionNotes) {
      request.resolutionNotes = updates.resolutionNotes;
    }

    const exception = await customerServiceExceptionsApi.update(id, request);
    return mapExceptionToTicket(exception);
  },

  /**
   * Close a ticket with resolution notes
   */
  async closeTicket(id: string, resolutionNotes: string): Promise<TicketWithDetails> {
    // First, get the current ticket to append to existing notes
    const currentException = await customerServiceExceptionsApi.getById(id);
    
    // Get current user info
    const userData = getUserData();
    const author = userData?.firstName && userData?.lastName
      ? `${userData.firstName} ${userData.lastName}`
      : userData?.username || 'Customer Service';
    
    // Format the new note with timestamp and author
    const timestamp = new Date().toLocaleString();
    const formattedNote = `[${timestamp}] ${author}: ${resolutionNotes}`;
    
    // Append to existing notes (or create new if none exist)
    const updatedNotes = currentException.resolutionNotes 
      ? `${currentException.resolutionNotes}\n\n${formattedNote}`
      : formattedNote;
    
    // Close with all notes appended
    const exception = await customerServiceExceptionsApi.close(id, updatedNotes);
    return mapExceptionToTicket(exception);
  },

  /**
   * Add a resolution note to a ticket
   */
  async addResolutionNote(ticketId: string, note: string): Promise<ResolutionNote> {
    // First, get the current ticket to append to existing notes
    const currentException = await customerServiceExceptionsApi.getById(ticketId);
    
    // Get current user info
    const userData = getUserData();
    const author = userData?.firstName && userData?.lastName
      ? `${userData.firstName} ${userData.lastName}`
      : userData?.username || 'Customer Service';
    
    // Format the new note with timestamp and author
    const timestamp = new Date().toLocaleString();
    const formattedNote = `[${timestamp}] ${author}: ${note}`;
    
    // Append to existing notes (or create new if none exist)
    const updatedNotes = currentException.resolutionNotes 
      ? `${currentException.resolutionNotes}\n\n${formattedNote}`
      : formattedNote;
    
    // Update the exception with the appended notes
    await customerServiceExceptionsApi.update(ticketId, {
      resolutionNotes: updatedNotes,
    });

    // Return as resolution note format
    return {
      id: Date.now().toString(),
      ticketId,
      note,
      createdBy: author,
      createdAt: new Date(),
    };
  },

  /**
   * Get order by reference/tracking number for status lookup
   */
  async getOrderByReference(reference: string): Promise<Order | null> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 400));

    // Mock order data for demonstration
    const mockOrders: Record<string, Order> = {
      'ORD-2024-123': {
        id: 1,
        reference: 'ORD-2024-123',
        description: 'Electronics shipment',
        status: 'IN_TRANSIT',
        priority: 'High',
        origin: 'Warehouse A',
        shippingAddress: '123 Main St, New York, NY 10001',
        estimatedDeliveryDate: new Date('2024-01-20'),
        trackingNumber: 'TRK123456789',
        scannedAt: new Date('2024-01-15T08:30:00'),
        weight: 2.5,
        createdAt: new Date('2024-01-14T10:00:00'),
        updatedAt: new Date('2024-01-15T14:20:00'),
        warehouseId: 1,
        shippoShipmentId: '',
        shippoTransactionId: '',
        shipstationOrderId: '',
        shipstationCarrierCode: '',
        shipstationServiceCode: ''
      }
    };

    const upperRef = reference.toUpperCase();
    return mockOrders[upperRef] || null;
  },

  /**
   * Get all delivery issues/exceptions
   */
  async getDeliveryIssues(filters?: {
    status?: string;
    orderId?: string;
  }): Promise<DeliveryIssue[]> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 300));

    // Mock delivery issues
    const issues: DeliveryIssue[] = [
      {
        id: '1',
        orderId: 'ORD-2024-123',
        issueType: 'Address Issue',
        status: 'Open',
        priority: 'High',
        description: 'Incorrect delivery address',
        reportedAt: new Date('2024-01-15T10:30:00'),
        assignedTo: 'Rita Lopes'
      },
      {
        id: '2',
        orderId: 'ORD-2024-456',
        issueType: 'Delivery Delay',
        status: 'In Progress',
        priority: 'Medium',
        description: 'Weather-related delay',
        reportedAt: new Date('2024-01-14T09:15:00'),
        assignedTo: 'Rita Lopes'
      }
    ];

    return issues.filter(issue => {
      if (filters?.status && issue.status !== filters.status) return false;
      if (filters?.orderId && issue.orderId !== filters.orderId) return false;
      return true;
    });
  },

  /**
   * Get customer service statistics
   */
  async getStats(): Promise<CustomerServiceStats> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 200));

    return MOCK_STATS;
  },

  /**
   * Get unread notifications count
   */
  async getUnreadNotificationsCount(): Promise<number> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 150));

    // Mock: return count of open tickets
    return MOCK_TICKETS.filter(t => t.status === 'Open').length;
  },

  /**
   * Mark notification as read
   */
  async markNotificationAsRead(ticketId: string): Promise<void> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 100));

    // In real implementation, this would mark the notification as read
    console.log(`Notification for ticket ${ticketId} marked as read`);
  },

  /**
   * Get all customer service representatives for ticket assignment
   */
  async getCustomerServiceReps(): Promise<Array<{ id: string; name: string; email: string }>> {
    try {
      const csrUsers = await apiFetch<UserInfo[]>(API_ENDPOINTS.customerService.customerServiceReps);
      return csrUsers.map(user => ({
        id: user.id,
        name: `${user.firstName} ${user.lastName}`.trim() || user.username,
        email: user.email,
      }));
    } catch (error) {
      console.error('Failed to fetch customer service representatives:', error);
      return [];
    }
  }
};
