"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Search, FileText, Clock, User, MessageSquare } from "lucide-react"

// Mock data for support tickets
const mockTickets = [
  {
    id: "TKT001",
    ticketNumber: "TKT-2025-001",
    customer: "John Doe",
    category: "Delivery Issue",
    subject: "Package not delivered",
    priority: "High",
    status: "Open",
    assignedTo: "Agent Smith",
    createdAt: "2025-10-06 09:30",
    lastUpdate: "2025-10-06 10:15",
    messages: 3,
  },
  {
    id: "TKT002",
    ticketNumber: "TKT-2025-002",
    customer: "Jane Smith",
    category: "Tracking",
    subject: "Unable to track package",
    priority: "Medium",
    status: "In Progress",
    assignedTo: "Agent Jones",
    createdAt: "2025-10-05 14:20",
    lastUpdate: "2025-10-06 08:45",
    messages: 5,
  },
  {
    id: "TKT003",
    ticketNumber: "TKT-2025-003",
    customer: "Bob Williams",
    category: "Refund",
    subject: "Request refund for damaged item",
    priority: "High",
    status: "Escalated",
    assignedTo: "Manager Brown",
    createdAt: "2025-10-04 11:00",
    lastUpdate: "2025-10-05 16:30",
    messages: 8,
  },
  {
    id: "TKT004",
    ticketNumber: "TKT-2025-004",
    customer: "Alice Johnson",
    category: "General Inquiry",
    subject: "Question about shipping rates",
    priority: "Low",
    status: "Resolved",
    assignedTo: "Agent Davis",
    createdAt: "2025-10-03 10:15",
    lastUpdate: "2025-10-04 09:00",
    messages: 2,
  },
]

export default function TicketsPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [tickets] = useState(mockTickets)
  const [filterStatus, setFilterStatus] = useState("All")

  const filteredTickets = tickets.filter((ticket) => {
    const matchesSearch =
      ticket.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ticket.customer.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ticket.subject.toLowerCase().includes(searchTerm.toLowerCase())
    
    const matchesStatus = filterStatus === "All" || ticket.status === filterStatus

    return matchesSearch && matchesStatus
  })

  const statusCounts = {
    All: tickets.length,
    Open: tickets.filter((t) => t.status === "Open").length,
    "In Progress": tickets.filter((t) => t.status === "In Progress").length,
    Escalated: tickets.filter((t) => t.status === "Escalated").length,
    Resolved: tickets.filter((t) => t.status === "Resolved").length,
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Support Tickets</h1>
        <Button>
          <FileText className="mr-2 h-4 w-4" />
          Create New Ticket
        </Button>
      </div>

      {/* Status Filter Tabs */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
        <div className="flex overflow-x-auto">
          {Object.entries(statusCounts).map(([status, count]) => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`flex-1 px-6 py-4 text-sm font-medium border-b-2 transition-colors ${
                filterStatus === status
                  ? "border-blue-600 text-blue-600"
                  : "border-transparent text-gray-600 hover:text-gray-900 hover:border-gray-300"
              }`}
            >
              {status} ({count})
            </button>
          ))}
        </div>
      </div>

      {/* Search Bar */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex gap-4">
          <div className="flex-1">
            <Label htmlFor="search">Search Tickets</Label>
            <div className="relative mt-2">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                id="search"
                type="text"
                placeholder="Enter ticket number, customer name, or subject..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <div className="flex items-end">
            <Button variant="outline">Advanced Filter</Button>
          </div>
        </div>
      </div>

      {/* Tickets Table */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
        <div className="p-6">
          <h2 className="text-xl font-semibold text-black mb-4">
            Tickets ({filteredTickets.length})
          </h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ticket #</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Subject</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Assigned To</TableHead>
                <TableHead>Last Update</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredTickets.map((ticket) => (
                <TableRow key={ticket.id}>
                  <TableCell className="font-medium">
                    {ticket.ticketNumber}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4 text-gray-400" />
                      {ticket.customer}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium text-black">{ticket.subject}</p>
                      <div className="flex items-center gap-1 text-xs text-gray-500 mt-1">
                        <MessageSquare className="h-3 w-3" />
                        {ticket.messages} messages
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>{ticket.category}</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        ticket.priority === "High"
                          ? "bg-red-100 text-red-700"
                          : ticket.priority === "Medium"
                          ? "bg-yellow-100 text-yellow-700"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {ticket.priority}
                    </span>
                  </TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        ticket.status === "Open"
                          ? "bg-blue-100 text-blue-700"
                          : ticket.status === "In Progress"
                          ? "bg-yellow-100 text-yellow-700"
                          : ticket.status === "Escalated"
                          ? "bg-red-100 text-red-700"
                          : "bg-green-100 text-green-700"
                      }`}
                    >
                      {ticket.status}
                    </span>
                  </TableCell>
                  <TableCell>{ticket.assignedTo}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <Clock className="h-3 w-3" />
                      {ticket.lastUpdate}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Button variant="ghost" size="sm">
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {filteredTickets.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No tickets found matching your criteria.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
