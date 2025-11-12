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
  ArrowRight,
} from "lucide-react"
import { customerServiceOrderApi } from "@/lib/services/customer-service.service"
import type { Order, OrderEvent, ShipmentOrderStatus } from "@/types"
import type { LucideIcon } from "lucide-react"

// Status configuration for display
const STATUS_CONFIG: Record<ShipmentOrderStatus, { icon: LucideIcon; color: string; label: string; bgColor: string }> = {
  CREATED: { icon: Package, color: "text-gray-700", label: "Created", bgColor: "bg-gray-100" },
  READY_FOR_PICKUP: { icon: Package, color: "text-blue-700", label: "Ready for Pickup", bgColor: "bg-blue-100" },
  COLLECTED: { icon: Package, color: "text-blue-700", label: "Collected", bgColor: "bg-blue-100" },
  ASSIGNED_TO_SHIPMENT: { icon: Package, color: "text-blue-700", label: "Assigned to Shipment", bgColor: "bg-blue-100" },
  IN_TRANSIT: { icon: MapPin, color: "text-orange-700", label: "In Transit", bgColor: "bg-orange-100" },
  DELIVERED: { icon: CheckCircle, color: "text-green-700", label: "Delivered", bgColor: "bg-green-100" },
  CANCELLED: { icon: AlertTriangle, color: "text-red-700", label: "Cancelled", bgColor: "bg-red-100" },
}

interface OrderLookupVersionBProps {
  onBack?: () => void;
}

/**
 * Version B: Compact table-based layout with horizontal timeline
 * Focus on efficiency and information density
 */
export default function OrderLookupVersionB({ onBack }: OrderLookupVersionBProps) {
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
      // Version B: Use optimized single endpoint that returns both order and events
      const data = await customerServiceOrderApi.getOrderWithEvents(order.id)
      setSelectedOrder(data.order)
      setOrderEvents(data.events)
    } catch (err) {
      console.error('Failed to load order details:', err)
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
    onBack?.()
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

  const formatDateShort = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-GB', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // Details view - Compact table layout
  if (selectedOrder) {
    const statusConfig = STATUS_CONFIG[selectedOrder.status]

    return (
      <div className="space-y-4">
        {/* Compact Header */}
        <div className="flex items-center justify-between bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center gap-4">
            <Button
              variant="outline"
              size="sm"
              onClick={handleBackToSearch}
            >
              <ArrowLeft className="w-4 h-4" />
            </Button>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-2xl font-bold text-black">{selectedOrder.reference}</h1>
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusConfig.color} ${statusConfig.bgColor}`}>
                  {statusConfig.label}
                </span>
              </div>
              <p className="text-sm text-gray-600 mt-1">{selectedOrder.description || "No description"}</p>
            </div>
          </div>
        </div>

        {/* Compact Info Grid */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <Label className="text-xs text-gray-500">Tracking #</Label>
              <p className="font-semibold">{selectedOrder.trackingNumber || "N/A"}</p>
            </div>
            <div>
              <Label className="text-xs text-gray-500">Warehouse</Label>
              <p className="font-semibold">ID {selectedOrder.warehouseId}</p>
            </div>
            <div>
              <Label className="text-xs text-gray-500">Est. Delivery</Label>
              <p className="font-semibold">
                {selectedOrder.estimatedDeliveryDate 
                  ? new Date(selectedOrder.estimatedDeliveryDate).toLocaleDateString()
                  : "N/A"}
              </p>
            </div>
            <div>
              <Label className="text-xs text-gray-500">Last Updated</Label>
              <p className="font-semibold">{formatDateShort(selectedOrder.updatedAt.toString())}</p>
            </div>
          </div>
          <div className="mt-3 pt-3 border-t">
            <Label className="text-xs text-gray-500">Shipping Address</Label>
            <p className="text-sm">{selectedOrder.shippingAddress}</p>
          </div>
        </div>

        {/* Horizontal Timeline */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <ArrowRight className="w-5 h-5" />
            Order Journey
          </h2>
          
          {isLoadingEvents ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
            </div>
          ) : orderEvents.length === 0 ? (
            <p className="text-gray-600 text-center py-8 text-sm">No timeline events available</p>
          ) : (
            <div className="overflow-x-auto">
              {/* Horizontal Timeline */}
              <div className="flex items-start gap-2 min-w-max pb-4">
                {orderEvents.map((event, index) => {
                  const eventStatusConfig = STATUS_CONFIG[event.eventType]
                  const EventIcon = eventStatusConfig.icon
                  const isLast = index === orderEvents.length - 1

                  return (
                    <div key={event.id} className="flex items-start">
                      {/* Event Card */}
                      <div className="flex flex-col items-center w-32">
                        {/* Icon */}
                        <div className={`w-12 h-12 rounded-full ${eventStatusConfig.bgColor} ${eventStatusConfig.color} flex items-center justify-center mb-2`}>
                          <EventIcon className="w-6 h-6" />
                        </div>
                        
                        {/* Label */}
                        <div className="text-center">
                          <p className="text-xs font-semibold text-gray-900">{eventStatusConfig.label}</p>
                          <p className="text-xs text-gray-500 mt-1">{formatDateShort(event.occurredAt)}</p>
                        </div>
                      </div>
                      
                      {/* Arrow */}
                      {!isLast && (
                        <div className="flex items-center pt-6 px-2">
                          <ArrowRight className="w-5 h-5 text-gray-300" />
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>

              {/* Event Details Table */}
              <div className="mt-6 border-t pt-4">
                <h3 className="text-sm font-semibold mb-3">Event Details</h3>
                <div className="space-y-2">
                  {orderEvents.map((event) => {
                    const eventStatusConfig = STATUS_CONFIG[event.eventType]
                    return (
                      <div key={event.id} className="flex items-start gap-3 text-sm bg-gray-50 p-3 rounded">
                        <Clock className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <span className="font-medium">{eventStatusConfig.label}</span>
                            <span className="text-xs text-gray-500">{formatDate(event.occurredAt)}</span>
                          </div>
                          <p className="text-gray-600 text-xs mt-1">{event.notes}</p>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            </div>
          )}
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-red-700 text-sm">
            {error}
          </div>
        )}
      </div>
    )
  }

  // Search view - Compact
  return (
    <div className="space-y-4">
      {/* Compact Header */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <h1 className="text-2xl font-bold text-black">Quick Order Lookup</h1>
        <p className="text-sm text-gray-600 mt-1">Enter order or tracking number</p>
      </div>

      {/* Compact Search */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <form onSubmit={handleSearch} className="flex gap-2">
          <Input
            placeholder="Order # or Tracking #"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            disabled={isSearching}
            className="flex-1"
          />
          <Button type="submit" disabled={isSearching}>
            <Search className="w-4 h-4 mr-2" />
            {isSearching ? "..." : "Search"}
          </Button>
        </form>
      </div>

      {/* Search Results - Table */}
      {searchResults.length > 1 && (
        <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <div className="p-4 border-b">
            <h2 className="font-semibold">Found {searchResults.length} orders</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="text-left p-3 font-medium">Order</th>
                  <th className="text-left p-3 font-medium">Description</th>
                  <th className="text-left p-3 font-medium">Tracking</th>
                  <th className="text-left p-3 font-medium">Status</th>
                  <th className="text-left p-3 font-medium">Action</th>
                </tr>
              </thead>
              <tbody>
                {searchResults.map((order) => {
                  const statusConfig = STATUS_CONFIG[order.status]
                  const StatusIcon = statusConfig.icon
                  
                  return (
                    <tr key={order.id} className="border-b hover:bg-gray-50">
                      <td className="p-3 font-semibold">{order.reference}</td>
                      <td className="p-3 text-gray-600">{order.description}</td>
                      <td className="p-3 font-mono text-xs">{order.trackingNumber || "N/A"}</td>
                      <td className="p-3">
                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${statusConfig.color} ${statusConfig.bgColor}`}>
                          <StatusIcon className="w-3 h-3" />
                          {statusConfig.label}
                        </span>
                      </td>
                      <td className="p-3">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => loadOrderDetails(order)}
                        >
                          View
                        </Button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Error */}
      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-yellow-800 text-sm">
          <p className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4" />
            {error}
          </p>
        </div>
      )}
    </div>
  )
}
