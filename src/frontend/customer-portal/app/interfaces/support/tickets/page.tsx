"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Search, MessageSquare, Plus, Clock, CheckCircle, AlertCircle, XCircle, Loader2 } from "lucide-react"
import { exceptionService } from "@/lib/services/exception.service"
import { featureFlagManager, isSupportTicketsEnabled, getTicketListView } from "@/lib/flagsmith"
import type { DeliveryExceptionResponse } from "@/types/exception"
import {
  ExceptionStatus,
  Priority,
  STATUS_LABELS,
  STATUS_COLORS,
  PRIORITY_COLORS,
  EXCEPTION_TYPE_LABELS,
} from "@/types/exception"

export default function CustomerTicketsPage() {
  const router = useRouter()
  
  // State
  const [searchTerm, setSearchTerm] = useState("")
  const [tickets, setTickets] = useState<DeliveryExceptionResponse[]>([])
  const [filterStatus, setFilterStatus] = useState<string>("All")
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  // Feature flags
  const [featureEnabled, setFeatureEnabled] = useState(true)
  const [listView, setListView] = useState<'table' | 'cards'>('table')

  // Initialize feature flags and load tickets
  useEffect(() => {
    const initializeAndLoad = async () => {
      try {
        // Initialize Flagsmith with mock customer ID
        // In production, this would come from auth context
        const userId = 'customer-' + Math.random().toString(36).substring(7)
        await featureFlagManager.initialize(userId)
        
        // Check if feature is enabled
        const enabled = isSupportTicketsEnabled()
        setFeatureEnabled(enabled)
        
        // Get list view preference
        const view = getTicketListView()
        setListView(view)
        
        console.log('[TicketsPage] Feature flags initialized:', {
          enabled,
          view,
          userId,
        })
        
        // Load tickets if feature is enabled
        if (enabled) {
          await loadTickets()
        } else {
          setIsLoading(false)
        }
      } catch (err) {
        console.error('[TicketsPage] Initialization failed:', err)
        setError('Failed to initialize. Please refresh the page.')
        setIsLoading(false)
      }
    }
    
    initializeAndLoad()
  }, [])

  const loadTickets = async () => {
    try {
      setIsLoading(true)
      setError(null)
      console.log('[TicketsPage] Loading tickets from API...')
      
      const data = await exceptionService.getAll()
      setTickets(data)
      
      console.log('[TicketsPage] Successfully loaded', data.length, 'tickets')
    } catch (err) {
      console.error('[TicketsPage] Failed to load tickets:', err)
      setError('Failed to load tickets. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleViewDetails = (ticketId: string) => {
    router.push(`/interfaces/support/tickets/${ticketId}`)
  }

  const handleNewTicket = () => {
    router.push("/interfaces/support/tickets/new")
  }

  // Filter tickets
  const filteredTickets = tickets.filter((ticket) => {
    const matchesSearch =
      exceptionService.formatTicketNumber(ticket).toLowerCase().includes(searchTerm.toLowerCase()) ||
      ticket.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ticket.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      EXCEPTION_TYPE_LABELS[ticket.type].toLowerCase().includes(searchTerm.toLowerCase())

    const matchesStatus = filterStatus === "All" || 
      STATUS_LABELS[ticket.status] === filterStatus ||
      ticket.status === filterStatus

    return matchesSearch && matchesStatus
  })

  // Calculate stats
  const stats = exceptionService.calculateSummary(tickets)

  const getStatusBadge = (status: ExceptionStatus) => {
    return STATUS_COLORS[status] || "bg-gray-100 text-gray-800"
  }

  const getPriorityBadge = (priority: Priority) => {
    return PRIORITY_COLORS[priority] || "bg-gray-100 text-gray-800"
  }

  const getStatusIcon = (status: ExceptionStatus) => {
    switch (status) {
      case ExceptionStatus.OPEN:
        return <AlertCircle className="h-4 w-4 text-blue-600" />
      case ExceptionStatus.ASSIGNED:
      case ExceptionStatus.IN_PROGRESS:
        return <Clock className="h-4 w-4 text-yellow-600" />
      case ExceptionStatus.RESOLVED:
      case ExceptionStatus.CLOSED:
        return <CheckCircle className="h-4 w-4 text-green-600" />
      case ExceptionStatus.ESCALATED:
        return <XCircle className="h-4 w-4 text-red-600" />
      default:
        return null
    }
  }

  // Feature disabled view
  if (!featureEnabled) {
    return (
      <div className="space-y-6">
        <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
          <div className="bg-blue-50 border-2 border-blue-200 rounded-lg p-8 max-w-md">
            <MessageSquare className="w-16 h-16 text-blue-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Support Tickets Coming Soon!</h2>
            <p className="text-gray-600 mb-4">
              We're working on bringing you an improved support ticket system. Check back soon!
            </p>
            <p className="text-sm text-gray-500">
              In the meantime, you can contact our support team directly.
            </p>
          </div>
        </div>
      </div>
    )
  }

  // Loading state
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-blue-600 animate-spin mx-auto mb-4" />
          <p className="text-gray-600">Loading your tickets...</p>
        </div>
      </div>
    )
  }

  // Error state
  if (error) {
    return (
      <div className="space-y-6">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <AlertCircle className="w-12 h-12 text-red-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-red-900 mb-2">Something went wrong</h2>
          <p className="text-red-700 mb-4">{error}</p>
          <Button onClick={loadTickets} variant="outline">
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-black">My Support Tickets</h1>
          <p className="text-gray-600 mt-1">View and manage your support requests</p>
        </div>
        <Button className="gap-2" onClick={handleNewTicket}>
          <Plus className="h-4 w-4" />
          New Ticket
        </Button>
      </div>

      {/* Search and Filters */}
      <div className="flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
          <Input
            type="text"
            placeholder="Search tickets..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="border border-gray-300 rounded-md px-4 py-2 bg-white text-black"
        >
          <option value="All">All Status</option>
          <option value={ExceptionStatus.OPEN}>{STATUS_LABELS[ExceptionStatus.OPEN]}</option>
          <option value={ExceptionStatus.IN_PROGRESS}>{STATUS_LABELS[ExceptionStatus.IN_PROGRESS]}</option>
          <option value={ExceptionStatus.RESOLVED}>{STATUS_LABELS[ExceptionStatus.RESOLVED]}</option>
          <option value={ExceptionStatus.CLOSED}>{STATUS_LABELS[ExceptionStatus.CLOSED]}</option>
        </select>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <AlertCircle className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-600">Open</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.totalOpen}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-gray-600">In Progress</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.totalInProgress}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium text-gray-600">Resolved</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.totalResolved}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <MessageSquare className="h-4 w-4 text-gray-600" />
            <span className="text-sm font-medium text-gray-600">Total</span>
          </div>
          <p className="text-2xl font-bold text-black">{tickets.length}</p>
        </div>
      </div>

      {/* Tickets Table or Cards */}
      {listView === 'table' ? (
        <div className="bg-white rounded-lg border border-gray-200">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ticket</TableHead>
                <TableHead>Type & Description</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredTickets.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                    {tickets.length === 0 ? 'No tickets yet. Create your first support ticket!' : 'No tickets found matching your search'}
                  </TableCell>
                </TableRow>
              ) : (
                filteredTickets.map((ticket) => (
                  <TableRow key={ticket.id} className="hover:bg-gray-50">
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-2">
                        {getStatusIcon(ticket.status)}
                        <span className="font-mono text-sm">{exceptionService.formatTicketNumber(ticket)}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div>
                        <div className="font-medium text-sm">{EXCEPTION_TYPE_LABELS[ticket.type]}</div>
                        <div className="text-xs text-gray-600 line-clamp-1">{ticket.description}</div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">
                        {ticket.category}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(ticket.status)}`}>
                        {STATUS_LABELS[ticket.status]}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span className={`px-2 py-1 text-xs rounded-full ${getPriorityBadge(ticket.priority)}`}>
                        {ticket.priority}
                      </span>
                    </TableCell>
                    <TableCell className="text-sm text-gray-600">
                      <div>
                        {new Date(ticket.reportedAt).toLocaleDateString()}
                      </div>
                      <div className="text-xs text-gray-500">
                        {new Date(ticket.reportedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Button variant="outline" size="sm" onClick={() => handleViewDetails(ticket.id)}>
                        View
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      ) : (
        /* Cards View */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTickets.length === 0 ? (
            <div className="col-span-full text-center py-12 text-gray-500">
              {tickets.length === 0 ? 'No tickets yet. Create your first support ticket!' : 'No tickets found matching your search'}
            </div>
          ) : (
            filteredTickets.map((ticket) => (
              <div
                key={ticket.id}
                className="bg-white rounded-lg border border-gray-200 p-5 hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => handleViewDetails(ticket.id)}
              >
                {/* Card Header */}
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    {getStatusIcon(ticket.status)}
                    <span className="font-mono text-sm font-medium text-gray-900">
                      {exceptionService.formatTicketNumber(ticket)}
                    </span>
                  </div>
                  <span className={`px-2 py-1 text-xs rounded-full ${getPriorityBadge(ticket.priority)}`}>
                    {ticket.priority}
                  </span>
                </div>

                {/* Type & Category */}
                <div className="mb-3">
                  <h3 className="font-semibold text-black mb-1">
                    {EXCEPTION_TYPE_LABELS[ticket.type]}
                  </h3>
                  <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">
                    {ticket.category}
                  </span>
                </div>

                {/* Description */}
                <p className="text-sm text-gray-600 line-clamp-2 mb-3">
                  {ticket.description}
                </p>

                {/* Footer */}
                <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(ticket.status)}`}>
                      {STATUS_LABELS[ticket.status]}
                    </span>
                  </div>
                  <div className="text-xs text-gray-500">
                    {new Date(ticket.reportedAt).toLocaleDateString()}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  )
}
