/**
 * Customer Service API - API calls for customer service operations
 * Currently using mock data - ready to connect to backend when available
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type { Order, OrderEvent } from '@/types';
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
};

// ========================================
// Mock data for tickets (temporary)
// ========================================

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
   * Get all support tickets
   */
  async getAllTickets(filters?: {
    status?: string;
    priority?: string;
    search?: string;
  }): Promise<TicketWithDetails[]> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 300));

    let filtered = [...MOCK_TICKETS];

    if (filters?.status && filters.status !== 'All') {
      filtered = filtered.filter(t => t.status === filters.status);
    }

    if (filters?.priority) {
      filtered = filtered.filter(t => t.priority === filters.priority);
    }

    if (filters?.search) {
      const search = filters.search.toLowerCase();
      filtered = filtered.filter(t =>
        t.ticketNumber.toLowerCase().includes(search) ||
        t.customerName?.toLowerCase().includes(search) ||
        t.subject.toLowerCase().includes(search)
      );
    }

    return filtered;
  },

  /**
   * Get ticket by ID
   */
  async getTicketById(id: string): Promise<TicketWithDetails> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 200));

    const ticket = MOCK_TICKETS.find(t => t.id === id);
    if (!ticket) {
      throw new Error('Ticket not found');
    }

    // Add resolution history for tickets with notes
    if (ticket.resolutionNotes) {
      ticket.resolutionHistory = [
        {
          id: '1',
          ticketId: id,
          note: ticket.resolutionNotes,
          createdBy: 'Rita Lopes',
          createdAt: new Date(ticket.updatedAt)
        }
      ];
    }

    return ticket;
  },

  /**
   * Update a support ticket
   */
  async updateTicket(id: string, updates: {
    status?: string;
    priority?: 'Low' | 'Medium' | 'High';
    assignedTo?: string;
    resolutionNotes?: string;
  }): Promise<TicketWithDetails> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 400));

    const ticket = MOCK_TICKETS.find(t => t.id === id);
    if (!ticket) {
      throw new Error('Ticket not found');
    }

    // Apply updates
    Object.assign(ticket, updates);
    ticket.updatedAt = new Date();

    return ticket;
  },

  /**
   * Close a ticket with resolution notes
   */
  async closeTicket(id: string, resolutionNotes: string): Promise<TicketWithDetails> {
    return this.updateTicket(id, {
      status: 'Resolved',
      resolutionNotes
    });
  },

  /**
   * Add a resolution note to a ticket
   */
  async addResolutionNote(ticketId: string, note: string): Promise<ResolutionNote> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 300));

    const newNote: ResolutionNote = {
      id: Date.now().toString(),
      ticketId,
      note,
      createdBy: 'Rita Lopes',
      createdAt: new Date()
    };

    const ticket = MOCK_TICKETS.find(t => t.id === ticketId);
    if (ticket) {
      if (!ticket.resolutionHistory) {
        ticket.resolutionHistory = [];
      }
      ticket.resolutionHistory.push(newNote);
      ticket.updatedAt = new Date();
    }

    return newNote;
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
  }
};
