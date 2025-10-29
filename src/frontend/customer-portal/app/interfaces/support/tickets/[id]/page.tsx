"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import {
  ArrowLeft,
  Calendar,
  AlertTriangle,
  Package,
  MapPin,
  Truck,
  CheckCircle2,
  Clock,
} from "lucide-react"

// Mock data - in real app, this would come from API
const mockTickets = [
  {
    id: "TKT001",
    ticketNumber: "TKT-2025-001",
    subject: "Package not delivered",
    category: "Delivery Issue",
    priority: "High",
    status: "Open",
    createdAt: "2025-10-06T09:30:00",
    updatedAt: "2025-10-06T10:15:00",
    description: "I ordered a package that was supposed to arrive yesterday, but I haven't received it yet. The tracking shows it was delivered, but there's nothing at my door. This is urgent as it contains important documents for work.",
    exceptionDetails: {
      type: "Delivery Failed",
      carrier: "FastShip Express",
      lastLocation: "Local Distribution Center, Aveiro",
      expectedDelivery: "2025-10-07",
      reason: "Recipient not available at delivery address"
    },
    resolutionHistory: [
      {
        id: "RES001",
        note: "Contacted carrier for investigation. They will check with the delivery driver.",
        createdBy: "Support Agent Smith",
        createdAt: "2025-10-06T10:15:00"
      }
    ]
  },
  {
    id: "TKT002",
    ticketNumber: "TKT-2025-002",
    subject: "Unable to track package",
    category: "Tracking",
    priority: "Medium",
    status: "In Progress",
    createdAt: "2025-10-05T14:20:00",
    updatedAt: "2025-10-06T08:45:00",
    description: "The tracking number provided doesn't work on any tracking website. Can you help me locate my package?",
    messages: 5,
    resolutionHistory: [
      {
        id: "RES002",
        note: "Verified tracking number with carrier. Package is in transit, tracking will be updated within 24 hours.",
        createdBy: "Support Agent Jones",
        createdAt: "2025-10-05T16:00:00"
      },
      {
        id: "RES003",
        note: "Tracking system now shows package details. Customer notified via email.",
        createdBy: "Support Agent Jones",
        createdAt: "2025-10-06T08:45:00"
      }
    ]
  },
  {
    id: "TKT003",
    ticketNumber: "TKT-2025-003",
    subject: "Request refund for damaged item",
    category: "Refund",
    priority: "High",
    status: "Escalated",
    createdAt: "2025-10-04T11:00:00",
    updatedAt: "2025-10-05T16:30:00",
    description: "The item I received was damaged during shipping. The box was clearly mishandled. I would like a full refund or replacement.",
    exceptionDetails: {
      type: "Package Damaged",
      carrier: "QuickDeliver Co.",
      lastLocation: "Delivered to customer",
      expectedDelivery: "2025-10-03",
      reason: "Package mishandled during transit"
    },
    resolutionHistory: [
      {
        id: "RES004",
        note: "Photos of damaged item received. Escalating to refund team for processing.",
        createdBy: "Support Agent Davis",
        createdAt: "2025-10-04T14:30:00"
      },
      {
        id: "RES005",
        note: "Refund approved. Processing will take 5-7 business days.",
        createdBy: "Manager Brown",
        createdAt: "2025-10-05T16:30:00"
      }
    ]
  },
  {
    id: "TKT004",
    ticketNumber: "TKT-2025-004",
    subject: "Question about shipping rates",
    category: "General Inquiry",
    priority: "Low",
    status: "Resolved",
    createdAt: "2025-10-03T10:15:00",
    updatedAt: "2025-10-04T09:00:00",
    description: "I'm planning to send multiple packages this month. Do you offer any bulk shipping discounts?",
    resolutionHistory: [
      {
        id: "RES006",
        note: "Provided information about our Business Shipping Program with volume discounts. Sent registration link to customer.",
        createdBy: "Support Agent Davis",
        createdAt: "2025-10-03T11:30:00"
      },
      {
        id: "RES007",
        note: "Customer confirmed they received the information and registered for the program. Closing ticket.",
        createdBy: "Support Agent Davis",
        createdAt: "2025-10-04T09:00:00"
      }
    ]
  },
]

export default function TicketDetailPage() {
  const params = useParams()
  const router = useRouter()
  const ticketId = params.id as string

  const [ticket, setTicket] = useState<typeof mockTickets[0] | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      const foundTicket = mockTickets.find(t => t.id === ticketId)
      setTicket(foundTicket || null)
      setLoading(false)
    }, 500)
  }, [ticketId])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading ticket details...</p>
        </div>
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="space-y-6">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Tickets
        </Button>
        <div className="bg-white border rounded-lg p-8 shadow-sm text-center">
          <AlertTriangle className="h-12 w-12 text-red-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-black mb-2">Ticket Not Found</h2>
          <p className="text-gray-600">The ticket you&apos;re looking for doesn&apos;t exist or you don&apos;t have access to it.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Tickets
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-black">{ticket.ticketNumber}</h1>
            <p className="text-gray-600 mt-1">{ticket.subject}</p>
          </div>
        </div>
        <div className="flex gap-2">
          <span
            className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${
              ticket.status === "Open"
                ? "bg-blue-100 text-blue-700"
                : ticket.status === "In Progress"
                ? "bg-yellow-100 text-yellow-700"
                : ticket.status === "Resolved"
                ? "bg-green-100 text-green-700"
                : "bg-red-100 text-red-700"
            }`}
          >
            {ticket.status}
          </span>
          <span
            className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${
              ticket.priority === "High"
                ? "bg-red-100 text-red-700"
                : ticket.priority === "Medium"
                ? "bg-yellow-100 text-yellow-700"
                : "bg-blue-100 text-blue-700"
            }`}
          >
            {ticket.priority} Priority
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Ticket Information */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Ticket Details</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-center gap-3">
                <Calendar className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Created</p>
                  <p className="font-medium text-black">
                    {new Date(ticket.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Clock className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Last Updated</p>
                  <p className="font-medium text-black">
                    {new Date(ticket.updatedAt).toLocaleString()}
                  </p>
                </div>
              </div>
             
            </div>
          </div>

          {/* Issue Description */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Your Message</h2>
            <p className="text-gray-700">{ticket.description}</p>
          </div>

          {/* Exception Details */}
          {ticket.exceptionDetails && (
            <div className="bg-white border border-orange-200 rounded-lg p-6 shadow-sm">
              <div className="flex items-center gap-2 mb-4">
                <AlertTriangle className="h-5 w-5 text-orange-600" />
                <h2 className="text-xl font-semibold text-black">Delivery Exception Details</h2>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="flex items-start gap-3">
                  <Package className="h-5 w-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-sm text-gray-600">Exception Type</p>
                    <p className="font-medium text-black">{ticket.exceptionDetails.type}</p>
                  </div>
                </div>
                {ticket.exceptionDetails.carrier && (
                  <div className="flex items-start gap-3">
                    <Truck className="h-5 w-5 text-gray-400 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-600">Carrier</p>
                      <p className="font-medium text-black">{ticket.exceptionDetails.carrier}</p>
                    </div>
                  </div>
                )}
                {ticket.exceptionDetails.lastLocation && (
                  <div className="flex items-start gap-3">
                    <MapPin className="h-5 w-5 text-gray-400 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-600">Last Known Location</p>
                      <p className="font-medium text-black">{ticket.exceptionDetails.lastLocation}</p>
                    </div>
                  </div>
                )}
                {ticket.exceptionDetails.expectedDelivery && (
                  <div className="flex items-start gap-3">
                    <Calendar className="h-5 w-5 text-gray-400 mt-0.5" />
                    <div>
                      <p className="text-sm text-gray-600">Expected Delivery</p>
                      <p className="font-medium text-black">{ticket.exceptionDetails.expectedDelivery}</p>
                    </div>
                  </div>
                )}
                {ticket.exceptionDetails.reason && (
                  <div className="md:col-span-2">
                    <p className="text-sm text-gray-600 mb-1">Reason</p>
                    <p className="text-black">{ticket.exceptionDetails.reason}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Resolution History */}
          {ticket.resolutionHistory && ticket.resolutionHistory.length > 0 && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-black mb-4">Support Updates</h2>
              <div className="space-y-4">
                {ticket.resolutionHistory.map((note) => (
                  <div key={note.id} className="border-l-4 border-blue-500 pl-4 py-2">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <span className="font-medium text-black">{note.createdBy}</span>
                      <span>•</span>
                      <span>{new Date(note.createdAt).toLocaleString()}</span>
                    </div>
                    <p className="text-gray-700">{note.note}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-black mb-4">Status</h2>
            {ticket.status === "Resolved" ? (
              <div className="flex items-start gap-3 p-4 bg-green-50 border border-green-200 rounded-lg">
                <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-green-900">Ticket Resolved</p>
                  <p className="text-sm text-green-700 mt-1">
                    Your issue has been resolved. If you need further assistance, please create a new ticket.
                  </p>
                </div>
              </div>
            ) : (
              <div className="flex items-start gap-3 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <Clock className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-blue-900">In Progress</p>
                  <p className="text-sm text-blue-700 mt-1">
                    Our support team is working on your request. We&apos;ll update you soon.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
