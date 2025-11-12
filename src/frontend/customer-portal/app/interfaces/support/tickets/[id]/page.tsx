"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import {
  ArrowLeft,
  Calendar,
  AlertTriangle,
  Package,
  CheckCircle2,
  Clock,
  User,
  FileText,
} from "lucide-react"
import { exceptionService } from "@/lib/services/exception.service"
import { 
  DeliveryExceptionResponse, 
  EXCEPTION_TYPE_LABELS, 
  STATUS_LABELS,
  ExceptionStatus,
  Priority,
  PRIORITY_LABELS,
  SEVERITY_LABELS
} from "@/types/exception"

export default function TicketDetailPage() {
  const params = useParams()
  const router = useRouter()
  const ticketId = params.id as string

  const [ticket, setTicket] = useState<DeliveryExceptionResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadTicket = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await exceptionService.getById(ticketId)
        setTicket(data)
      } catch (err: unknown) {
        console.error('Failed to load ticket:', err)
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('Failed to load ticket details')
        }
      } finally {
        setLoading(false)
      }
    }

    loadTicket()
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

  if (error || !ticket) {
    return (
      <div className="space-y-6">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Tickets
        </Button>
        <div className="bg-white border rounded-lg p-8 shadow-sm text-center">
          <AlertTriangle className="h-12 w-12 text-red-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-black mb-2">
            {error ? 'Error Loading Ticket' : 'Ticket Not Found'}
          </h2>
          <p className="text-gray-600">
            {error || "The ticket you're looking for doesn't exist or you don't have access to it."}
          </p>
        </div>
      </div>
    )
  }

  // Helper function to get status badge color
  const getStatusColor = (status: ExceptionStatus) => {
    switch (status) {
      case ExceptionStatus.OPEN:
        return "bg-blue-100 text-blue-700"
      case ExceptionStatus.ASSIGNED:
        return "bg-purple-100 text-purple-700"
      case ExceptionStatus.IN_PROGRESS:
        return "bg-yellow-100 text-yellow-700"
      case ExceptionStatus.RESOLVED:
        return "bg-green-100 text-green-700"
      case ExceptionStatus.CLOSED:
        return "bg-gray-100 text-gray-700"
      case ExceptionStatus.ESCALATED:
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  // Helper function to get priority badge color
  const getPriorityColor = (priority: Priority) => {
    switch (priority) {
      case Priority.CRITICAL:
        return "bg-red-100 text-red-700"
      case Priority.HIGH:
        return "bg-orange-100 text-orange-700"
      case Priority.MEDIUM:
        return "bg-yellow-100 text-yellow-700"
      case Priority.LOW:
        return "bg-blue-100 text-blue-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  return (
    <div className="space-y-6 max-w-7xl">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Tickets
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-black">Ticket #{ticket.id.slice(0, 8)}</h1>
            <p className="text-gray-600 mt-1">{ticket.category}</p>
          </div>
        </div>
        <div className="flex gap-2 flex-wrap">
          <span className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${getStatusColor(ticket.status)}`}>
            {STATUS_LABELS[ticket.status]}
          </span>
          <span className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${getPriorityColor(ticket.priority)}`}>
            {PRIORITY_LABELS[ticket.priority]} Priority
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
                    {ticket.reportedAt 
                      ? new Date(ticket.reportedAt).toLocaleString()
                      : 'N/A'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Clock className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Last Updated</p>
                  <p className="font-medium text-black">
                    {ticket.updatedAt 
                      ? new Date(ticket.updatedAt).toLocaleString()
                      : 'N/A'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <FileText className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Type</p>
                  <p className="font-medium text-black">
                    {EXCEPTION_TYPE_LABELS[ticket.type]}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <AlertTriangle className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Severity</p>
                  <p className="font-medium text-black">
                    {SEVERITY_LABELS[ticket.severity]}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Issue Description */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Your Message</h2>
            <div className="mb-4 pb-4 border-b">
              <p className="text-sm text-gray-600 mb-1">Category</p>
              <p className="font-medium text-black">{ticket.category}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 mb-2">Description</p>
              <p className="text-gray-700 whitespace-pre-wrap">{ticket.description}</p>
            </div>
          </div>

          {/* Order Information */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Related Order</h2>
            <div className="flex items-center gap-3">
              <Package className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm text-gray-600">Order ID</p>
                <p className="font-medium text-black">#{ticket.orderId}</p>
              </div>
            </div>
          </div>

          {/* Resolution Notes */}
          {ticket.resolutionNotes && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-black mb-4">Resolution Notes</h2>
              <div className="border-l-4 border-green-500 pl-4 py-2">
                <p className="text-gray-700 whitespace-pre-wrap">{ticket.resolutionNotes}</p>
                {ticket.resolvedAt && (
                  <p className="text-sm text-gray-600 mt-2">
                    Resolved on {new Date(ticket.resolvedAt).toLocaleString()}
                  </p>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-black mb-4">Status</h2>
            {ticket.status === ExceptionStatus.RESOLVED || ticket.status === ExceptionStatus.CLOSED ? (
              <div className="flex items-start gap-3 p-4 bg-green-50 border border-green-200 rounded-lg">
                <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-green-900">Ticket {STATUS_LABELS[ticket.status]}</p>
                  <p className="text-sm text-green-700 mt-1">
                    Your issue has been resolved. If you need further assistance, please create a new ticket.
                  </p>
                </div>
              </div>
            ) : ticket.status === ExceptionStatus.ESCALATED ? (
              <div className="flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-lg">
                <AlertTriangle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-red-900">Escalated</p>
                  <p className="text-sm text-red-700 mt-1">
                    This ticket has been escalated to a senior support team member for urgent attention.
                  </p>
                </div>
              </div>
            ) : (
              <div className="flex items-start gap-3 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                <Clock className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-blue-900">{STATUS_LABELS[ticket.status]}</p>
                  <p className="text-sm text-blue-700 mt-1">
                    {ticket.status === ExceptionStatus.ASSIGNED 
                      ? "A support agent has been assigned to your ticket."
                      : ticket.status === ExceptionStatus.IN_PROGRESS
                      ? "Our support team is working on your request. We'll update you soon."
                      : "Your ticket has been received and will be reviewed shortly."}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Assigned To */}
          {ticket.assignedTo && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-lg font-semibold text-black mb-4">Assigned Agent</h2>
              <div className="flex items-center gap-3">
                <User className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Support Agent</p>
                  <p className="font-medium text-black">{ticket.assignedTo}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
