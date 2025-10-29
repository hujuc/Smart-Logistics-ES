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
import { Search, Package, MapPin, Truck, CheckCircle, Clock, AlertCircle, XCircle } from "lucide-react"
import { customerOrderApi } from "@/lib/services/customer-order.service"
import type { Order, ShipmentOrderStatus } from "@/types/common"

export default function OrderHistoryPage() {
  const router = useRouter()
  const [searchTerm, setSearchTerm] = useState("")
  const [orders, setOrders] = useState<Order[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filterStatus, setFilterStatus] = useState<string>("All")

  // Load orders on mount
  useEffect(() => {
    loadOrders()
  }, [])

  const loadOrders = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await customerOrderApi.getAllOrders()
      setOrders(data)
    } catch (err) {
      console.error('Failed to load orders:', err)
      setError('Failed to load orders. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleTrackOrder = (trackingNumber: string) => {
    router.push(`/interfaces/support/history/${trackingNumber}`)
  }

  const filteredOrders = orders
    .filter((order) => {
      const matchesSearch =
        order.reference.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.description.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesStatus = filterStatus === "All" || order.status === filterStatus

      return matchesSearch && matchesStatus
    })

  const getStatusBadge = (status: ShipmentOrderStatus) => {
    const styles: Record<ShipmentOrderStatus, string> = {
      CREATED: "bg-gray-100 text-gray-800",
      READY_FOR_PICKUP: "bg-blue-100 text-blue-800",
      COLLECTED: "bg-purple-100 text-purple-800",
      IN_TRANSIT: "bg-yellow-100 text-yellow-800",
      DELIVERED: "bg-green-100 text-green-800",
      CANCELLED: "bg-red-100 text-red-800",
    }
    return styles[status] || "bg-gray-100 text-gray-800"
  }

  const getStatusIcon = (status: ShipmentOrderStatus) => {
    switch (status) {
      case "CREATED":
      case "READY_FOR_PICKUP":
        return <Clock className="h-4 w-4 text-blue-600" />
      case "COLLECTED":
      case "IN_TRANSIT":
        return <Truck className="h-4 w-4 text-yellow-600" />
      case "DELIVERED":
        return <CheckCircle className="h-4 w-4 text-green-600" />
      case "CANCELLED":
        return <XCircle className="h-4 w-4 text-red-600" />
      default:
        return <Package className="h-4 w-4 text-gray-600" />
    }
  }

  const getStatusLabel = (status: ShipmentOrderStatus) => {
    const labels: Record<ShipmentOrderStatus, string> = {
      CREATED: "Created",
      READY_FOR_PICKUP: "Ready for Pickup",
      COLLECTED: "Collected",
      IN_TRANSIT: "In Transit",
      DELIVERED: "Delivered",
      CANCELLED: "Cancelled",
    }
    return labels[status] || status
  }

  const formatDate = (dateString: Date | string) => {
    return new Date(dateString).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    })
  }

  // Calculate stats
  const stats = {
    processing: orders.filter(o => ['CREATED', 'READY_FOR_PICKUP'].includes(o.status)).length,
    inTransit: orders.filter(o => ['COLLECTED', 'IN_TRANSIT'].includes(o.status)).length,
    delivered: orders.filter(o => o.status === 'DELIVERED').length,
    total: orders.length,
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-black">Order History</h1>
        <p className="text-gray-600 mt-1">Track your deliveries and view past orders</p>
      </div>

      {/* Search and Filters */}
      <div className="flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
          <Input
            type="text"
            placeholder="Search by order number, tracking number, or description..."
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
          <option value="CREATED">Created</option>
          <option value="READY_FOR_PICKUP">Ready for Pickup</option>
          <option value="COLLECTED">Collected</option>
          <option value="IN_TRANSIT">In Transit</option>
          <option value="DELIVERED">Delivered</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-600">Processing</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.processing}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Truck className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-gray-600">In Transit</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.inTransit}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium text-gray-600">Delivered</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.delivered}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Package className="h-4 w-4 text-gray-600" />
            <span className="text-sm font-medium text-gray-600">Total Orders</span>
          </div>
          <p className="text-2xl font-bold text-black">{stats.total}</p>
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Loading orders...</p>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-red-600" />
            <p className="text-red-800">{error}</p>
          </div>
          <Button onClick={loadOrders} className="mt-2" variant="outline" size="sm">
            Try Again
          </Button>
        </div>
      )}

      {/* Orders Table */}
      {!isLoading && !error && (
        <div className="bg-white rounded-lg border border-gray-200">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Order Number</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Destination</TableHead>
                <TableHead>Order Date</TableHead>
                <TableHead>Tracking</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredOrders.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                    No orders found
                  </TableCell>
                </TableRow>
              ) : (
                filteredOrders.map((order) => (
                  <TableRow key={order.id} className="hover:bg-gray-50">
                    <TableCell className="font-medium">
                      {order.reference}
                    </TableCell>
                    <TableCell>{order.description}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {getStatusIcon(order.status)}
                        <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(order.status)}`}>
                          {getStatusLabel(order.status)}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1 text-sm text-gray-600">
                        <MapPin className="h-3 w-3" />
                        {order.shippingAddress}
                      </div>
                    </TableCell>
                    <TableCell className="text-sm text-gray-600">
                      {formatDate(order.createdAt)}
                    </TableCell>
                    <TableCell className="font-mono text-sm">{order.trackingNumber}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="outline" size="sm" onClick={() => handleTrackOrder(order.trackingNumber)}>
                        Track
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  )
}
