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

  // Load tickets on mount
  useEffect(() => {
    loadTickets()
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

      {/* Tickets Table */}
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
    </div>
  )
}
