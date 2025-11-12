"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
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
import { Search, Clock, User, MessageSquare, Package, AlertCircle, CheckCircle, Filter, Settings } from "lucide-react"
import { customerServiceService } from "@/lib/services/customer-service.service"
import { featureFlagManager } from "@/lib/flagsmith"
import { getUserData } from "@/shared/lib/auth"
import type { Order } from "@/types"
import { formatOrderShippingAddress } from "@/lib/address"
import type { TicketWithDetails } from "@/types/customer-service"


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
  const router = useRouter()
  const [searchTerm, setSearchTerm] = useState("")
  const [tickets, setTickets] = useState<TicketWithDetails[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filterStatus, setFilterStatus] = useState("All")

  // Feature flag for ticket visibility
  const [ticketVisibilityMode, setTicketVisibilityMode] = useState<'all' | 'assigned'>('all')
  const [showModeToggle, setShowModeToggle] = useState(false)
  const [currentUserName, setCurrentUserName] = useState<string>("")

  // Additional filters and sorting
  const [filterPriority, setFilterPriority] = useState("All")
  const [filterCategory, setFilterCategory] = useState("All")
  const [filterAssignedTo, setFilterAssignedTo] = useState("All")
  const [sortBy, setSortBy] = useState<"date" | "priority" | "status" | "customer" | "category" | "assignedTo">("date")
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc")

  // Order lookup state (US 7)
  const [orderResult, setOrderResult] = useState<Order | null>(null)
  const [isSearching, setIsSearching] = useState(false)
  const [searchError, setSearchError] = useState<string | null>(null)

  // Initialize feature flags and get current user
  useEffect(() => {
    const userData = getUserData()
    if (userData) {
      const fullName = userData.firstName && userData.lastName
        ? `${userData.firstName} ${userData.lastName}`
        : userData.username || ''
      setCurrentUserName(fullName)
    }

    // Initialize feature flags
    const userId = userData?.username || 'customer_service'
    featureFlagManager.initialize(userId).then(() => {
      const mode = featureFlagManager.getFlag('ticket-visibility-mode')
      setTicketVisibilityMode(mode)
    })

    // Show mode toggle in development
    if (process.env.NODE_ENV === 'development') {
      setShowModeToggle(true)
    }
  }, [])

  // Load tickets from API
  const loadTickets = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await customerServiceService.getAllTickets({
        status: filterStatus !== "All" ? filterStatus : undefined,
        priority: filterPriority !== "All" ? filterPriority : undefined,
      })
      setTickets(data)
    } catch (err) {
      console.error("Failed to load tickets:", err)
      setError("Failed to load tickets. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTickets()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterStatus, filterPriority])

  const filteredAndSortedTickets = tickets
    .filter((ticket) => {
      const matchesSearch =
        ticket.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (ticket.customerName?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
        ticket.subject.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesCategory = filterCategory === "All" || ticket.category === filterCategory
      const matchesAssignedTo = filterAssignedTo === "All" || ticket.assignedTo === filterAssignedTo

      // Feature flag: ticket visibility mode
      let matchesVisibility = true
      if (ticketVisibilityMode === 'assigned') {
        // Mode B: Only show tickets assigned to current user or unassigned
        matchesVisibility = !ticket.assignedTo || ticket.assignedTo === currentUserName
      }
      // Mode A: Show all tickets (default)

      return matchesSearch && matchesCategory && matchesAssignedTo && matchesVisibility
    })
    .sort((a, b) => {
      let comparison = 0

      switch (sortBy) {
        case "date":
          comparison = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          break
        case "priority":
          const priorityOrder = { High: 3, Medium: 2, Low: 1 }
          comparison = priorityOrder[b.priority as keyof typeof priorityOrder] - priorityOrder[a.priority as keyof typeof priorityOrder]
          break
        case "status":
          const statusOrder = { Open: 4, Escalated: 3, "In Progress": 2, Resolved: 1, Closed: 0 }
          comparison = statusOrder[b.status as keyof typeof statusOrder] - statusOrder[a.status as keyof typeof statusOrder]
          break
        case "customer":
          comparison = (a.customerName || '').localeCompare(b.customerName || '')
          break
        case "category":
          comparison = (a.category || '').localeCompare(b.category || '')
          break
        case "assignedTo":
          comparison = (a.assignedTo || '').localeCompare(b.assignedTo || '')
          break
      }

      return sortOrder === "asc" ? -comparison : comparison
    })

  // Get unique values for filters
  const categories = Array.from(new Set(tickets.map((t) => t.category).filter(Boolean)))
  const priorities = ["High", "Medium", "Low"]
  const assignees = Array.from(new Set(tickets.map((t) => t.assignedTo).filter(Boolean)))

  // Handle column sorting
  const handleSort = (column: typeof sortBy) => {
    if (sortBy === column) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc")
    } else {
      setSortBy(column)
      setSortOrder("asc")
    }
  }

  // Sortable header component
  const SortableHeader = ({ column, children }: { column: typeof sortBy; children: React.ReactNode }) => (
    <TableHead
      onClick={() => handleSort(column)}
      className="cursor-pointer hover:bg-gray-50 select-none"
    >
      <div className="flex items-center gap-1">
        {children}
        {sortBy === column && (
          <span className="text-blue-600">
            {sortOrder === "asc" ? "↑" : "↓"}
          </span>
        )}
      </div>
    </TableHead>
  )

  const statusCounts = {
    All: tickets.length,
    Open: tickets.filter((t) => t.status === "Open").length,
    "In Progress": tickets.filter((t) => t.status === "In Progress").length,
    Escalated: tickets.filter((t) => t.status === "Escalated").length,
    Resolved: tickets.filter((t) => t.status === "Resolved").length,
  }

  // Unified search handler - searches for orders by reference/tracking (US 7)
  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      setSearchError("Please enter a search term")
      return
    }

    // Check if it looks like an order reference (starts with ORD- or TRK)
    const term = searchTerm.trim().toUpperCase()
    if (term.startsWith("ORD-") || term.startsWith("TRK")) {
      setIsSearching(true)
      setSearchError(null)
      setOrderResult(null)

      try {
        const order = await customerServiceService.getOrderByReference(term)
        if (order) {
          setOrderResult(order)
        } else {
          setSearchError("Order not found. Please check the reference number.")
        }
      } catch (error) {
        setSearchError("Failed to lookup order. Please try again.")
        console.error("Order lookup error:", error)
      } finally {
        setIsSearching(false)
      }
    }
    // Otherwise, it filters the tickets table (already handled by filteredAndSortedTickets)
  }

  const handleSearchKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleSearch()
    }
  }

  const handleViewDetails = (ticketId: string) => {
    router.push(`/interfaces/customer-service/tickets/${ticketId}`)
  }

  const handleToggleVisibilityMode = () => {
    const newMode = ticketVisibilityMode === 'all' ? 'assigned' : 'all'
    featureFlagManager.setFlag('ticket-visibility-mode', newMode)
    setTicketVisibilityMode(newMode)
  }

  return (
    <div className="space-y-4 md:space-y-6">
      {/* Feature Flag Toggle (Development Only) */}
      {showModeToggle && (
        <div className={`border-l-4 p-4 ${featureFlagManager.isUsingAPI() ? 'bg-green-50 border-green-400' : 'bg-yellow-50 border-yellow-400'}`}>
          <div className="flex items-start">
            <Settings className={`h-5 w-5 mt-0.5 ${featureFlagManager.isUsingAPI() ? 'text-green-400' : 'text-yellow-400'}`} />
            <div className="ml-3 flex-1">
              <h3 className={`text-sm font-medium ${featureFlagManager.isUsingAPI() ? 'text-green-800' : 'text-yellow-800'}`}>
                Feature Flag: Ticket Visibility Mode {featureFlagManager.isUsingAPI() ? '(Flagsmith API)' : '(Demo Mode)'}
              </h3>
              {!featureFlagManager.isUsingAPI() && (
                <div className="mt-2 flex gap-2">
                  <Button
                    size="sm"
                    variant={ticketVisibilityMode === 'all' ? 'default' : 'outline'}
                    onClick={handleToggleVisibilityMode}
                    className="h-7 text-xs"
                  >
                    {ticketVisibilityMode === 'all' ? '✓ Mode A (All Tickets)' : 'Mode A (All Tickets)'}
                  </Button>
                  <Button
                    size="sm"
                    variant={ticketVisibilityMode === 'assigned' ? 'default' : 'outline'}
                    onClick={handleToggleVisibilityMode}
                    className="h-7 text-xs"
                  >
                    {ticketVisibilityMode === 'assigned' ? '✓ Mode B (My Tickets)' : 'Mode B (My Tickets)'}
                  </Button>
                </div>
              )}
              <p className={`text-xs mt-2 ${featureFlagManager.isUsingAPI() ? 'text-green-700' : 'text-yellow-700'}`}>
                {featureFlagManager.isUsingAPI() ? (
                  <>
                    <strong>✓ Connected to Flagsmith API</strong>
                    <br />
                    Current mode: <span className="font-mono font-semibold">{ticketVisibilityMode === 'all' ? 'A (All Tickets)' : 'B (Assigned to Me + Unassigned)'}</span>
                    <br />
                    <em>Changes are controlled from Flagsmith Dashboard</em>
                  </>
                ) : (
                  <>
                    Current mode: <span className="font-mono font-semibold">{ticketVisibilityMode === 'all' ? 'A (All Tickets)' : 'B (Assigned to Me + Unassigned)'}</span> 
                    <br />
                    <em>Set NEXT_PUBLIC_FLAGSMITH_ENV_KEY to use Flagsmith API</em>
                  </>
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <div className="flex flex-col sm:flex-row gap-3 sm:justify-between sm:items-center">
        <h1 className="text-2xl md:text-3xl font-bold text-black">Support Tickets</h1>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
            <p className="text-gray-600">Loading tickets...</p>
          </div>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-red-600" />
            <p className="text-red-700">{error}</p>
          </div>
          <Button onClick={loadTickets} variant="outline" size="sm" className="mt-3">
            Try Again
          </Button>
        </div>
      )}

      {!loading && !error && (
        <>
          {/* Status Filter Tabs */}
          <div className="bg-white border rounded-lg shadow-sm">
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

      {/* Search Bar with Order Lookup - US 7 */}
      <div className="bg-white border rounded-lg p-4 md:p-6 shadow-sm">
        <div className="space-y-4">
          <div>
            <Label htmlFor="search">Search Orders or Tickets</Label>
            <div className="flex flex-col sm:flex-row gap-2 mt-2">
              <Input
                id="search"
                type="text"
                placeholder="Enter order reference (ORD-2024-123), tracking number, ticket #, or customer name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={handleSearchKeyPress}
                className="flex-1"
              />
              <Button onClick={handleSearch} disabled={isSearching} className="w-full sm:w-auto">
                <Search className="w-4 h-4 mr-2" />
                {isSearching ? "Searching..." : "Search"}
              </Button>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              Tip: Enter ORD- or TRK to search orders, or search tickets by name/subject
            </p>
          </div>

          {/* Filters and Sort */}
          <div className="flex flex-wrap gap-3 items-center pt-2 border-t">
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-gray-500" />
              <span className="text-sm font-medium text-gray-700">Filters:</span>
            </div>

            {/* Priority Filter */}
            <select
              value={filterPriority}
              onChange={(e) => setFilterPriority(e.target.value)}
              className="text-sm border rounded-md px-3 py-1.5 bg-white text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="All">All Priorities</option>
              {priorities.map((priority) => (
                <option key={priority} value={priority}>
                  {priority} Priority
                </option>
              ))}
            </select>

            {/* Category Filter */}
            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className="text-sm border rounded-md px-3 py-1.5 bg-white text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="All">All Categories</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>

            {/* Assigned To Filter */}
            <select
              value={filterAssignedTo}
              onChange={(e) => setFilterAssignedTo(e.target.value)}
              className="text-sm border rounded-md px-3 py-1.5 bg-white text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="All">All Assignees</option>
              {assignees.map((assignee) => (
                <option key={assignee} value={assignee}>
                  {assignee}
                </option>
              ))}
            </select>

            {/* Clear Filters */}
            {(filterPriority !== "All" || filterCategory !== "All" || filterAssignedTo !== "All" || filterStatus !== "All") && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setFilterPriority("All")
                  setFilterCategory("All")
                  setFilterAssignedTo("All")
                  setFilterStatus("All")
                }}
                className="text-xs"
              >
                Clear Filters
              </Button>
            )}
          </div>

          {/* Order result display */}
          {orderResult && (
            <div className="mt-4 p-4 border rounded-lg bg-green-50 border-green-200">
              <div className="flex items-start gap-3">
                <CheckCircle className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                <div className="flex-1 space-y-2">
                  <h3 className="font-semibold text-green-900 flex items-center gap-2">
                    <Package className="h-4 w-4" />
                    Order Found
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                    <div>
                      <span className="text-gray-600">Reference:</span>
                      <span className="ml-2 font-medium text-black">{orderResult.reference}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Status:</span>
                      <span className={`ml-2 inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        orderResult.status === "DELIVERED" ? "bg-green-100 text-green-700" :
                        orderResult.status === "IN_TRANSIT" ? "bg-blue-100 text-blue-700" :
                        orderResult.status === "ASSIGNED_TO_SHIPMENT" ? "bg-yellow-100 text-yellow-700" :
                        "bg-gray-100 text-gray-700"
                      }`}>
                        {orderResult.status.replace(/_/g, ' ')}
                      </span>
                    </div>
                    {orderResult.trackingNumber && (
                      <div>
                        <span className="text-gray-600">Tracking #:</span>
                        <span className="ml-2 font-medium text-black">{orderResult.trackingNumber}</span>
                      </div>
                    )}
                    {orderResult.estimatedDeliveryDate && (
                      <div>
                        <span className="text-gray-600">Est. Delivery:</span>
                        <span className="ml-2 font-medium text-black">
                          {new Date(orderResult.estimatedDeliveryDate).toLocaleDateString()}
                        </span>
                      </div>
                    )}
                    <div className="md:col-span-2">
                      <span className="text-gray-600">Destination:</span>
                      <span className="ml-2 font-medium text-black">
                        {formatOrderShippingAddress(orderResult)}
                      </span>
                    </div>
                    {orderResult.description && (
                      <div className="md:col-span-2">
                        <span className="text-gray-600">Description:</span>
                        <span className="ml-2 text-black">{orderResult.description}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Error display */}
          {searchError && (
            <div className="mt-4 p-4 border rounded-lg bg-red-50 border-red-200">
              <div className="flex items-start gap-3">
                <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
                <div>
                  <h3 className="font-semibold text-red-900">Error</h3>
                  <p className="text-sm text-red-700">{searchError}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Tickets Table */}
      <div className="bg-white border rounded-lg shadow-sm overflow-hidden">
        <div className="p-4 md:p-6">
          <h2 className="text-lg md:text-xl font-semibold text-black mb-4">
            Tickets ({filteredAndSortedTickets.length})
          </h2>
          <div className="overflow-x-auto -mx-4 md:mx-0">
            <div className="inline-block min-w-full align-middle">
              <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ticket #</TableHead>
                <SortableHeader column="customer">Customer</SortableHeader>
                <TableHead>Subject</TableHead>
                <SortableHeader column="category">Category</SortableHeader>
                <SortableHeader column="priority">Priority</SortableHeader>
                <SortableHeader column="status">Status</SortableHeader>
                <SortableHeader column="assignedTo">Assigned To</SortableHeader>
                <SortableHeader column="date">Last Update</SortableHeader>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredAndSortedTickets.map((ticket) => (
                <TableRow key={ticket.id}>
                  <TableCell className="font-medium">
                    {ticket.ticketNumber}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4 text-gray-400" />
                      {ticket.customerName || 'Unknown'}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium text-black">{ticket.subject}</p>
                      <div className="flex items-center gap-1 text-xs text-gray-500 mt-1">
                        <MessageSquare className="h-3 w-3" />
                        {ticket.messages || 0} messages
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
                      {new Date(ticket.updatedAt).toLocaleString()}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Button variant="ghost" size="sm" onClick={() => handleViewDetails(ticket.id)}>
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
              </Table>
            </div>
          </div>
          {filteredAndSortedTickets.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No tickets found matching your criteria.
            </div>
          )}
        </div>
      </div>
        </>
      )}
    </div>
  )
}
