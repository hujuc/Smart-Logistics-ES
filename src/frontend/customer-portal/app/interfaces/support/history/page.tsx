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
import { Search, Package, MapPin, Truck, CheckCircle, Clock } from "lucide-react"

// Mock data for customer's order history
const mockOrders = [
  {
    id: "ORD001",
    orderNumber: "ORD-2025-001",
    trackingNumber: "TRK123456789",
    description: "Electronics Package",
    status: "Delivered",
    destination: "123 Main St, Aveiro",
    orderDate: "2025-10-20",
    deliveryDate: "2025-10-25",
    carrier: "DHL Express",
  },
  {
    id: "ORD002",
    orderNumber: "ORD-2025-002",
    trackingNumber: "TRK987654321",
    description: "Books - 3 items",
    status: "In Transit",
    destination: "456 Oak Ave, Porto",
    orderDate: "2025-10-26",
    estimatedDelivery: "2025-10-29",
    carrier: "CTT",
  },
  {
    id: "ORD003",
    orderNumber: "ORD-2025-003",
    trackingNumber: "TRK456789123",
    description: "Clothing Package",
    status: "Processing",
    destination: "789 Pine Rd, Lisboa",
    orderDate: "2025-10-27",
    estimatedDelivery: "2025-10-30",
    carrier: "UPS",
  },
  {
    id: "ORD004",
    orderNumber: "ORD-2025-004",
    trackingNumber: "TRK321654987",
    description: "Home Decor",
    status: "Delivered",
    destination: "321 Elm St, Braga",
    orderDate: "2025-10-15",
    deliveryDate: "2025-10-19",
    carrier: "FedEx",
  },
]

export default function OrderHistoryPage() {
  const router = useRouter()
  const [searchTerm, setSearchTerm] = useState("")
  const [orders] = useState(mockOrders)
  const [filterStatus, setFilterStatus] = useState("All")

  const handleTrackOrder = (trackingNumber: string) => {
    router.push(`/interfaces/support/history/${trackingNumber}`)
  }

  const filteredOrders = orders
    .filter((order) => {
      const matchesSearch =
        order.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.description.toLowerCase().includes(searchTerm.toLowerCase())

      const matchesStatus = filterStatus === "All" || order.status === filterStatus

      return matchesSearch && matchesStatus
    })

  const getStatusBadge = (status: string) => {
    const styles = {
      Processing: "bg-blue-100 text-blue-800",
      "In Transit": "bg-yellow-100 text-yellow-800",
      Delivered: "bg-green-100 text-green-800",
      Cancelled: "bg-red-100 text-red-800",
    }
    return styles[status as keyof typeof styles] || "bg-gray-100 text-gray-800"
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Processing":
        return <Clock className="h-4 w-4 text-blue-600" />
      case "In Transit":
        return <Truck className="h-4 w-4 text-yellow-600" />
      case "Delivered":
        return <CheckCircle className="h-4 w-4 text-green-600" />
      default:
        return <Package className="h-4 w-4 text-gray-600" />
    }
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
          <option value="Processing">Processing</option>
          <option value="In Transit">In Transit</option>
          <option value="Delivered">Delivered</option>
          <option value="Cancelled">Cancelled</option>
        </select>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-600">Processing</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {orders.filter((o) => o.status === "Processing").length}
          </p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Truck className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-gray-600">In Transit</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {orders.filter((o) => o.status === "In Transit").length}
          </p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium text-gray-600">Delivered</span>
          </div>
          <p className="text-2xl font-bold text-black">
            {orders.filter((o) => o.status === "Delivered").length}
          </p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Package className="h-4 w-4 text-gray-600" />
            <span className="text-sm font-medium text-gray-600">Total Orders</span>
          </div>
          <p className="text-2xl font-bold text-black">{orders.length}</p>
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white rounded-lg border border-gray-200">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Order</TableHead>
              <TableHead>Tracking</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Destination</TableHead>
              <TableHead>Order Date</TableHead>
              <TableHead>Delivery</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredOrders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-gray-500">
                  No orders found
                </TableCell>
              </TableRow>
            ) : (
              filteredOrders.map((order) => (
                <TableRow key={order.id} className="hover:bg-gray-50">
                  <TableCell className="font-medium">
                    <div className="flex items-center gap-2">
                      <Package className="h-4 w-4 text-gray-400" />
                      {order.orderNumber}
                    </div>
                  </TableCell>
                  <TableCell className="font-mono text-sm">{order.trackingNumber}</TableCell>
                  <TableCell>{order.description}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getStatusIcon(order.status)}
                      <span className={`px-2 py-1 text-xs rounded-full ${getStatusBadge(order.status)}`}>
                        {order.status}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <MapPin className="h-3 w-3" />
                      {order.destination}
                    </div>
                  </TableCell>
                  <TableCell className="text-sm text-gray-600">
                    {new Date(order.orderDate).toLocaleDateString()}
                  </TableCell>
                  <TableCell className="text-sm">
                    {order.status === "Delivered" ? (
                      <span className="text-green-600">
                        {order.deliveryDate && new Date(order.deliveryDate).toLocaleDateString()}
                      </span>
                    ) : (
                      <span className="text-gray-600">
                        Est: {order.estimatedDelivery && new Date(order.estimatedDelivery).toLocaleDateString()}
                      </span>
                    )}
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm" onClick={() => handleTrackOrder(order.trackingNumber)}>
                        Track
                      </Button>
                    </div>
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
