"use client"

import { useState, useEffect, useCallback } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import {
  ArrowLeft,
  User,
  Mail,
  Calendar,
  AlertTriangle,
  Package,
  MapPin,
  Truck,
  CheckCircle2,
  Clock
} from "lucide-react"
import { customerServiceService } from "@/lib/services/customer-service.service"
import type { TicketWithDetails } from "@/types/customer-service"

export default function TicketDetailPage() {
  const params = useParams()
  const router = useRouter()
  const ticketId = params.id as string

  const [ticket, setTicket] = useState<TicketWithDetails | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  //  Resolution form state (US 8 & 9)
  const [resolutionNote, setResolutionNote] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadTicketDetails = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await customerServiceService.getTicketById(ticketId)
      setTicket(data)
    } catch (err) {
      console.error("Failed to load ticket:", err)
      setError("Failed to load ticket details. Please try again.")
    } finally {
      setLoading(false)
    }
  }, [ticketId])

  useEffect(() => {
    loadTicketDetails()
  }, [loadTicketDetails])

  const handleAddNote = async () => {
    if (!resolutionNote.trim() || !ticket) return

    try {
      setIsSubmitting(true)
      await customerServiceService.addResolutionNote(ticket.id, resolutionNote.trim())
      setResolutionNote("")
      // Reload ticket to show new note
      await loadTicketDetails()
    } catch (err) {
      console.error("Failed to add note:", err)
      alert("Failed to add note. Please try again.")
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleCloseTicket = async () => {
    if (!ticket) return

    if (!resolutionNote.trim()) {
      alert("Please add resolution notes before closing the ticket.")
      return
    }

    try {
      setIsSubmitting(true)
      await customerServiceService.closeTicket(ticket.id, resolutionNote.trim())
      alert("Ticket closed successfully!")
      router.push("/interfaces/customer-service/tickets")
    } catch (err) {
      console.error("Failed to close ticket:", err)
      alert("Failed to close ticket. Please try again.")
      setIsSubmitting(false)
    }
  }

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
          <h2 className="text-xl font-semibold text-black mb-2">Error Loading Ticket</h2>
          <p className="text-gray-600">{error || "Ticket not found"}</p>
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
                : "bg-gray-100 text-gray-700"
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
          {/* Customer Information */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Customer Information</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-center gap-3">
                <User className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Customer Name</p>
                  <p className="font-medium text-black">{ticket.customerName || "N/A"}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Mail className="h-5 w-5 text-gray-400" />
                <div>
                  <p className="text-sm text-gray-600">Email</p>
                  <p className="font-medium text-black">{ticket.customerEmail || "N/A"}</p>
                </div>
              </div>
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
            <h2 className="text-xl font-semibold text-black mb-4">Issue Description</h2>
            <p className="text-gray-700">{ticket.description}</p>
          </div>

          {/* Exception Details - US 8 */}
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

          {/* Resolution History - US 8 & 9 */}
          {ticket.resolutionHistory && ticket.resolutionHistory.length > 0 && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-black mb-4">Resolution History</h2>
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

        {/* Sidebar - Resolution Form (US 8 & 9) */}
        <div className="space-y-6">
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-black mb-4">Quick Actions</h2>
            <div className="space-y-2">
              <p className="text-sm text-gray-600">
                <span className="font-medium">Category:</span> {ticket.category || "N/A"}
              </p>
              <p className="text-sm text-gray-600">
                <span className="font-medium">Assigned To:</span> {ticket.assignedTo || "Unassigned"}
              </p>
              {ticket.messages && (
                <p className="text-sm text-gray-600">
                  <span className="font-medium">Messages:</span> {ticket.messages}
                </p>
              )}
            </div>
          </div>

          {/* Resolution Workflow - US 8 & 9 */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-black mb-4">
              {ticket.status === "Resolved" ? "Ticket Resolved" : "Add Resolution Notes"}
            </h2>

            {ticket.status === "Resolved" ? (
              <div className="flex items-start gap-3 p-4 bg-green-50 border border-green-200 rounded-lg">
                <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-green-900">Ticket Closed</p>
                  <p className="text-sm text-green-700 mt-1">
                    This ticket has been successfully resolved.
                  </p>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div>
                  <Label htmlFor="resolution">Resolution Notes</Label>
                  <textarea
                    id="resolution"
                    className="mt-2 w-full min-h-[120px] rounded-md border bg-white px-3 py-2 text-sm text-black shadow-sm transition-colors placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950"
                    placeholder="Enter resolution notes, customer communication details, or next steps..."
                    value={resolutionNote}
                    onChange={(e) => setResolutionNote(e.target.value)}
                  />
                  <p className="text-xs text-gray-500 mt-1">
                    Document the resolution, customer interaction, and any follow-up actions.
                  </p>
                </div>

                <div className="space-y-2">
                  <Button
                    className="w-full"
                    onClick={handleAddNote}
                    disabled={isSubmitting || !resolutionNote.trim()}
                  >
                    Add Note
                  </Button>
                  <Button
                    className="w-full"
                    variant="outline"
                    onClick={handleCloseTicket}
                    disabled={isSubmitting || !resolutionNote.trim()}
                  >
                    <CheckCircle2 className="mr-2 h-4 w-4" />
                    Close Ticket
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
