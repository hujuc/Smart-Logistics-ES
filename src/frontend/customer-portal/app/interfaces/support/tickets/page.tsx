"use client"

import { useState } from "react"
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
import { Search, MessageSquare, Plus, Clock, CheckCircle, AlertCircle } from "lucide-react"

// Mock data for customer's own tickets
const mockCustomerTickets = [
  {
    id: "TKT001",
    ticketNumber: "TKT-2025-001",
    subject: "Package not delivered",
    category: "Delivery Issue",
    status: "Open",
    priority: "High",
    createdAt: "2025-10-26 09:30",
    lastUpdate: "2025-10-27 10:15",
  },
  {
    id: "TKT002",
    ticketNumber: "TKT-2025-002",
    subject: "Unable to track my package",
    category: "Tracking",
    status: "In Progress",
    priority: "Medium",
    createdAt: "2025-10-25 14:20",
    lastUpdate: "2025-10-27 08:45",
  },
  {
    id: "TKT003",
    ticketNumber: "TKT-2025-003",
    subject: "Request refund for damaged item",
    category: "Refund",
    status: "Resolved",
    priority: "High",
    createdAt: "2025-10-24 11:00",
    lastUpdate: "2025-10-26 16:30",
  },
  {
    id: "TKT004",
    ticketNumber: "TKT-2025-004",
    subject: "Question about shipping rates",
    category: "General Inquiry",
    status: "Resolved",
    priority: "Low",
    createdAt: "2025-10-23 10:15",
    lastUpdate: "2025-10-24 09:00",
  },
]

export default function CustomerTicketsPage() {
  const router = useRouter()
  const [searchTerm, setSearchTerm] = useState("")
  const [tickets] = useState(mockCustomerTickets)
  const [filterStatus, setFilterStatus] = useState("All")

  const handleViewDetails = (ticketId: string) => {
    router.push(`/interfaces/support/tickets/${ticketId}`)
  }

  const handleNewTicket = () => {
    router.push("/interfaces/support/tickets/new")
  }

  const filteredTickets = tickets
    .filter((ticket) => {
      const matchesSearch =
        ticket.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        ticket.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
        ticket.category.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesStatus = filterStatus === "All" || ticket.status === filterStatus

      return matchesSearch && matchesStatus
    })

  const getStatusBadge = (status: string) => {
    const styles = {
      Open: "bg-blue-100 text-blue-800",
      "In Progress": "bg-yellow-100 text-yellow-800",
      Resolved: "bg-green-100 text-green-800",
      Closed: "bg-gray-100 text-gray-800",
    }
    return styles[status as keyof typeof styles] || "bg-gray-100 text-gray-800"
  }

  const getPriorityBadge = (priority: string) => {
    const styles = {
      High: "bg-red-100 text-red-800",
      Medium: "bg-orange-100 text-orange-800",
      Low: "bg-blue-100 text-blue-800",
    }
    return styles[priority as keyof typeof styles] || "bg-gray-100 text-gray-800"
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Open":
        return <AlertCircle className="h-4 w-4 text-blue-600" />
      case "In Progress":
        return <Clock className="h-4 w-4 text-yellow-600" />
      case "Resolved":
      case "Closed":
        return <CheckCircle className="h-4 w-4 text-green-600" />
      default:
        return null
    }
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
          <option value="Open">Open</option>
          <option value="In Progress">In Progress</option>
          <option value="Resolved">Resolved</option>
          <option value="Closed">Closed</option>
        </select>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <AlertCircle className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-600">Open</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {tickets.filter((t) => t.status === "Open").length}
          </p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-gray-600">In Progress</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {tickets.filter((t) => t.status === "In Progress").length}
          </p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium text-gray-600">Resolved</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {tickets.filter((t) => t.status === "Resolved").length}
          </p>
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
              <TableHead>Subject</TableHead>
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
                <TableCell colSpan={8} className="text-center py-8 text-gray-500">
                  No tickets found
                </TableCell>
              </TableRow>
            ) : (
              filteredTickets.map((ticket) => (
                <TableRow key={ticket.id} className="hover:bg-gray-50 cursor-pointer">
                  <TableCell className="font-medium">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(ticket.status)}
                      {ticket.ticketNumber}
                    </div>
                  </TableCell>
                  <TableCell>{ticket.subject}</TableCell>
                  <TableCell>
                    <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">
                      {ticket.category}
                    </span>
                  </TableCell>
                  <TableCell>
                    <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(ticket.status)}`}>
                      {ticket.status}
                    </span>
                  </TableCell>
                  <TableCell>
                    <span className={`px-2 py-1 text-xs rounded-full ${getPriorityBadge(ticket.priority)}`}>
                      {ticket.priority}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-600">
                    {new Date(ticket.createdAt).toLocaleDateString()}
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
