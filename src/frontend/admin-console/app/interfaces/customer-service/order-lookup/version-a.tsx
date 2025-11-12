"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Search,
  Package,
  MapPin,
  CheckCircle,
  Clock,
  AlertTriangle,
  ArrowLeft,
  Calendar,
} from "lucide-react"
import { customerServiceOrderApi } from "@/lib/services/customer-service.service"
import type { Order, OrderEvent, ShipmentOrderStatus } from "@/types"
import type { LucideIcon } from "lucide-react"

// Status configuration for display
const STATUS_CONFIG: Record<ShipmentOrderStatus, { icon: LucideIcon; color: string; label: string }> = {
  CREATED: { icon: Package, color: "text-gray-500", label: "Created" },
  READY_FOR_PICKUP: { icon: Package, color: "text-blue-500", label: "Ready for Pickup" },
  COLLECTED: { icon: Package, color: "text-blue-500", label: "Collected" },
  ASSIGNED_TO_SHIPMENT: { icon: Package, color: "text-blue-500", label: "Assigned to Shipment" },
  IN_TRANSIT: { icon: MapPin, color: "text-orange-500", label: "In Transit" },
  DELIVERED: { icon: CheckCircle, color: "text-green-500", label: "Delivered" },
  CANCELLED: { icon: AlertTriangle, color: "text-red-500", label: "Cancelled" },
}

/**
 * Version A: Card-based layout with vertical timeline
 * Original design with emphasis on visual presentation
 */
export default function OrderLookupVersionA() {
  const [searchQuery, setSearchQuery] = useState("")
  const [isSearching, setIsSearching] = useState(false)
  const [searchResults, setSearchResults] = useState<Order[]>([])
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
  const [orderEvents, setOrderEvents] = useState<OrderEvent[]>([])
  const [isLoadingEvents, setIsLoadingEvents] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!searchQuery.trim()) {
      setError("Please enter an order number or tracking number")
      return
    }

    setIsSearching(true)
    setError(null)
    setSearchResults([])
    setSelectedOrder(null)
    setOrderEvents([])

    try {
      const results = await customerServiceOrderApi.searchOrders(searchQuery.trim())
      setSearchResults(results)
      
      if (results.length === 0) {
        setError(`No orders found for "${searchQuery}"`)
      } else if (results.length === 1) {
        // Auto-select if only one result
        await loadOrderDetails(results[0])
      }
    } catch (err) {
      console.error('Search error:', err)
      setError('Failed to search orders. Please try again.')
    } finally {
      setIsSearching(false)
    }
  }

  const loadOrderDetails = async (order: Order) => {
    setSelectedOrder(order)
    setIsLoadingEvents(true)
    setError(null)

    try {
      const events = await customerServiceOrderApi.getOrderEvents(order.id)
      setOrderEvents(events)
    } catch (err) {
      console.error('Failed to load order events:', err)
      setError('Failed to load order timeline')
    } finally {
      setIsLoadingEvents(false)
    }
  }

  const handleBackToSearch = () => {
    setSelectedOrder(null)
    setOrderEvents([])
    setSearchResults([])
    setSearchQuery("")
    setError(null)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-GB', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // If order is selected, show details view
  if (selectedOrder) {
    const statusConfig = STATUS_CONFIG[selectedOrder.status]
    const StatusIcon = statusConfig.icon

    return (
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button
            variant="outline"
            size="sm"
            onClick={handleBackToSearch}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to Search
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-black">Order Details</h1>
            <p className="text-gray-600 mt-1">{selectedOrder.reference}</p>
          </div>
        </div>

        {/* Order Info Card */}
        <div className="bg-white rounded-lg border border-gray-200 p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold">Order #{selectedOrder.reference}</h2>
              <p className="text-gray-600">{selectedOrder.description || "No description"}</p>
            </div>
            <div className={`flex items-center gap-2 ${statusConfig.color}`}>
              <StatusIcon className="w-5 h-5" />
              <span className="font-semibold">{statusConfig.label}</span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
            <div>
              <Label className="text-gray-600">Tracking Number</Label>
              <p className="font-semibold">{selectedOrder.trackingNumber || "N/A"}</p>
            </div>
            <div>
              <Label className="text-gray-600">Warehouse ID</Label>
              <p className="font-semibold">{selectedOrder.warehouseId}</p>
            </div>
            <div>
              <Label className="text-gray-600">Shipping Address</Label>
              <p className="font-semibold">{selectedOrder.shippingAddress}</p>
            </div>
            <div>
              <Label className="text-gray-600">Estimated Delivery</Label>
              <p className="font-semibold flex items-center gap-2">
                <Calendar className="w-4 h-4" />
                {selectedOrder.estimatedDeliveryDate 
                  ? new Date(selectedOrder.estimatedDeliveryDate).toLocaleDateString()
                  : "N/A"}
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
            <div>
              <Label className="text-gray-600">Created At</Label>
              <p className="text-sm">{formatDate(selectedOrder.createdAt.toString())}</p>
            </div>
            <div>
              <Label className="text-gray-600">Last Updated</Label>
              <p className="text-sm">{formatDate(selectedOrder.updatedAt.toString())}</p>
            </div>
          </div>
        </div>

        {/* Timeline */}
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-xl font-semibold mb-6">Order Timeline</h2>
          
          {isLoadingEvents ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
            </div>
          ) : orderEvents.length === 0 ? (
            <p className="text-gray-600 text-center py-8">No timeline events available</p>
          ) : (
            <div className="relative">
              {/* Timeline line */}
              <div className="absolute left-[15px] top-0 bottom-0 w-0.5 bg-gray-200" />
              
              {/* Events */}
              <div className="space-y-6">
                {orderEvents.map((event, index) => {
                  const eventStatusConfig = STATUS_CONFIG[event.eventType]
                  const EventIcon = eventStatusConfig.icon
                  const isLast = index === orderEvents.length - 1

                  return (
                    <div key={event.id} className="relative flex gap-4">
                      {/* Icon */}
                      <div className={`relative z-10 flex-shrink-0 w-8 h-8 rounded-full border-2 border-white ${
                        isLast ? 'bg-gray-200' : 'bg-green-500'
                      } flex items-center justify-center`}>
                        <EventIcon className={`w-4 h-4 ${isLast ? 'text-gray-600' : 'text-white'}`} />
                      </div>

                      {/* Content */}
                      <div className="flex-1 pb-6">
                        <div className="flex items-start justify-between">
                          <div>
                            <h3 className="font-semibold">{eventStatusConfig.label}</h3>
                            <p className="text-sm text-gray-600 mt-1">{event.notes}</p>
                          </div>
                          <div className="text-sm text-gray-500 flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {formatDate(event.occurredAt)}
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            {error}
          </div>
        )}
      </div>
    )
  }

  // Search view
  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-black">Order Status Lookup</h1>
        <p className="text-gray-600 mt-1">Search by order number or tracking number</p>
      </div>

      {/* Search Form */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <form onSubmit={handleSearch} className="space-y-4">
          <div>
            <Label htmlFor="search">Order Number or Tracking Number</Label>
            <div className="flex gap-2 mt-2">
              <Input
                id="search"
                placeholder="Enter order number (e.g., ORD-2025-001) or tracking number"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                disabled={isSearching}
              />
              <Button type="submit" disabled={isSearching}>
                <Search className="w-4 h-4 mr-2" />
                {isSearching ? "Searching..." : "Search"}
              </Button>
            </div>
          </div>
        </form>
      </div>

      {/* Search Results */}
      {searchResults.length > 1 && (
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h2 className="text-xl font-semibold mb-4">Search Results ({searchResults.length})</h2>
          <div className="space-y-2">
            {searchResults.map((order) => {
              const statusConfig = STATUS_CONFIG[order.status]
              const StatusIcon = statusConfig.icon
              
              return (
                <button
                  key={order.id}
                  onClick={() => loadOrderDetails(order)}
                  className="w-full text-left p-4 border border-gray-200 rounded-lg hover:border-gray-300 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-semibold">{order.reference}</p>
                      <p className="text-sm text-gray-600">{order.description}</p>
                      <p className="text-xs text-gray-500 mt-1">
                        Tracking: {order.trackingNumber || "N/A"}
                      </p>
                    </div>
                    <div className={`flex items-center gap-2 ${statusConfig.color}`}>
                      <StatusIcon className="w-5 h-5" />
                      <span className="font-medium">{statusConfig.label}</span>
                    </div>
                  </div>
                </button>
              )
            })}
          </div>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 text-yellow-800">
          <p className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5" />
            {error}
          </p>
        </div>
      )}
    </div>
  )
}
