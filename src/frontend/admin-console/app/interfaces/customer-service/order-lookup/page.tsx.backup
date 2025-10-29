"use client"

import { useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Search,
  Package,
  MapPin,
  Truck,
  CheckCircle,
  Clock,
  AlertTriangle,
  Home,
  Building2,
  FileText,
  ArrowLeft,
  Calendar,
  Weight,
  User,
  Phone,
  Mail,
  Info
} from "lucide-react"

// Mock data - in real app, this would come from API
const mockOrderDetails: Record<string, any> = {
  "ORD-2025-001": {
    trackingNumber: "TRK123456789",
    orderNumber: "ORD-2025-001",
    status: "In Transit",
    description: "Electronics Package",
    weight: "2.5 kg",
    dimensions: "40x30x15 cm",
    estimatedDelivery: "2025-10-29",
    carrier: "FastShip Express",
    carrierService: "Express Delivery",
    shippingCost: "€12.50",
    insurance: "€50.00",
    priority: "High",
    createdAt: "2025-10-25T10:00:00",
    scannedAt: "2025-10-26T08:30:00",
    customer: {
      name: "John Doe",
      email: "john.doe@email.com",
      phone: "+351 912 345 678",
      customerId: "CUST-123"
    },
    origin: {
      name: "Amazon Warehouse",
      address: "Rua do Comércio, 123",
      city: "Lisboa",
      postalCode: "1000-100",
      country: "Portugal",
      contactPerson: "Maria Silva",
      contactPhone: "+351 211 234 567"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      postalCode: "3810-193",
      country: "Portugal",
      contactPhone: "+351 912 345 678"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-25T10:00:00",
        description: "Order has been confirmed and is being prepared for shipment.",
        completed: true,
        agent: "System"
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Lisboa Distribution Center",
        timestamp: "2025-10-26T08:30:00",
        description: "Package collected from seller and in transit to sorting facility.",
        completed: true,
        agent: "Pickup Driver #42"
      },
      {
        id: 3,
        status: "In Transit",
        location: "Coimbra Transit Hub",
        timestamp: "2025-10-27T14:20:00",
        description: "Package is on the way to the destination city.",
        completed: true,
        agent: "Transit Hub Processing"
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: null,
        description: "Package will be delivered today.",
        completed: false,
        agent: "TBD"
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: null,
        description: "Package successfully delivered to recipient.",
        completed: false,
        agent: "TBD"
      }
    ],
    notes: [
      {
        id: 1,
        note: "Customer requested contactless delivery",
        createdBy: "Agent Smith",
        createdAt: "2025-10-25T11:30:00"
      }
    ]
  },
  "TRK123456789": {
    trackingNumber: "TRK123456789",
    orderNumber: "ORD-2025-001",
    status: "In Transit",
    description: "Electronics Package",
    weight: "2.5 kg",
    dimensions: "40x30x15 cm",
    estimatedDelivery: "2025-10-29",
    carrier: "FastShip Express",
    carrierService: "Express Delivery",
    shippingCost: "€12.50",
    insurance: "€50.00",
    priority: "High",
    createdAt: "2025-10-25T10:00:00",
    scannedAt: "2025-10-26T08:30:00",
    customer: {
      name: "John Doe",
      email: "john.doe@email.com",
      phone: "+351 912 345 678",
      customerId: "CUST-123"
    },
    origin: {
      name: "Amazon Warehouse",
      address: "Rua do Comércio, 123",
      city: "Lisboa",
      postalCode: "1000-100",
      country: "Portugal",
      contactPerson: "Maria Silva",
      contactPhone: "+351 211 234 567"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      postalCode: "3810-193",
      country: "Portugal",
      contactPhone: "+351 912 345 678"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-25T10:00:00",
        description: "Order has been confirmed and is being prepared for shipment.",
        completed: true,
        agent: "System"
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Lisboa Distribution Center",
        timestamp: "2025-10-26T08:30:00",
        description: "Package collected from seller and in transit to sorting facility.",
        completed: true,
        agent: "Pickup Driver #42"
      },
      {
        id: 3,
        status: "In Transit",
        location: "Coimbra Transit Hub",
        timestamp: "2025-10-27T14:20:00",
        description: "Package is on the way to the destination city.",
        completed: true,
        agent: "Transit Hub Processing"
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: null,
        description: "Package will be delivered today.",
        completed: false,
        agent: "TBD"
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: null,
        description: "Package successfully delivered to recipient.",
        completed: false,
        agent: "TBD"
      }
    ],
    notes: [
      {
        id: 1,
        note: "Customer requested contactless delivery",
        createdBy: "Agent Smith",
        createdAt: "2025-10-25T11:30:00"
      }
    ]
  },
  "ORD-2025-002": {
    trackingNumber: "TRK987654321",
    orderNumber: "ORD-2025-002",
    status: "Delivered",
    description: "Books and Stationery",
    weight: "1.2 kg",
    dimensions: "30x25x10 cm",
    estimatedDelivery: "2025-10-26",
    actualDelivery: "2025-10-26T16:45:00",
    carrier: "QuickDeliver Co.",
    carrierService: "Standard Delivery",
    shippingCost: "€8.00",
    insurance: "€20.00",
    priority: "Medium",
    createdAt: "2025-10-23T14:00:00",
    scannedAt: "2025-10-24T09:00:00",
    customer: {
      name: "Jane Smith",
      email: "jane.smith@email.com",
      phone: "+351 913 456 789",
      customerId: "CUST-456"
    },
    origin: {
      name: "BookStore Warehouse",
      address: "Avenida Central, 789",
      city: "Porto",
      postalCode: "4000-100",
      country: "Portugal",
      contactPerson: "João Santos",
      contactPhone: "+351 222 345 678"
    },
    destination: {
      name: "Jane Smith",
      address: "Rua das Flores, 123",
      city: "Aveiro",
      postalCode: "3800-200",
      country: "Portugal",
      contactPhone: "+351 913 456 789"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-23T14:00:00",
        description: "Order has been confirmed.",
        completed: true,
        agent: "System"
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Porto Distribution Center",
        timestamp: "2025-10-24T09:00:00",
        description: "Package collected from seller.",
        completed: true,
        agent: "Pickup Driver #18"
      },
      {
        id: 3,
        status: "In Transit",
        location: "On Route to Aveiro",
        timestamp: "2025-10-25T11:30:00",
        description: "Package is on the way.",
        completed: true,
        agent: "Transit Processing"
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: "2025-10-26T08:00:00",
        description: "Package is out for delivery.",
        completed: true,
        agent: "Delivery Driver #25"
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: "2025-10-26T16:45:00",
        description: "Package delivered successfully. Signed by recipient.",
        completed: true,
        agent: "Delivery Driver #25"
      }
    ],
    notes: []
  },
  "TRK987654321": {
    trackingNumber: "TRK987654321",
    orderNumber: "ORD-2025-002",
    status: "Delivered",
    description: "Books and Stationery",
    weight: "1.2 kg",
    dimensions: "30x25x10 cm",
    estimatedDelivery: "2025-10-26",
    actualDelivery: "2025-10-26T16:45:00",
    carrier: "QuickDeliver Co.",
    carrierService: "Standard Delivery",
    shippingCost: "€8.00",
    insurance: "€20.00",
    priority: "Medium",
    createdAt: "2025-10-23T14:00:00",
    scannedAt: "2025-10-24T09:00:00",
    customer: {
      name: "Jane Smith",
      email: "jane.smith@email.com",
      phone: "+351 913 456 789",
      customerId: "CUST-456"
    },
    origin: {
      name: "BookStore Warehouse",
      address: "Avenida Central, 789",
      city: "Porto",
      postalCode: "4000-100",
      country: "Portugal",
      contactPerson: "João Santos",
      contactPhone: "+351 222 345 678"
    },
    destination: {
      name: "Jane Smith",
      address: "Rua das Flores, 123",
      city: "Aveiro",
      postalCode: "3800-200",
      country: "Portugal",
      contactPhone: "+351 913 456 789"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-23T14:00:00",
        description: "Order has been confirmed.",
        completed: true,
        agent: "System"
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Porto Distribution Center",
        timestamp: "2025-10-24T09:00:00",
        description: "Package collected from seller.",
        completed: true,
        agent: "Pickup Driver #18"
      },
      {
        id: 3,
        status: "In Transit",
        location: "On Route to Aveiro",
        timestamp: "2025-10-25T11:30:00",
        description: "Package is on the way.",
        completed: true,
        agent: "Transit Processing"
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: "2025-10-26T08:00:00",
        description: "Package is out for delivery.",
        completed: true,
        agent: "Delivery Driver #25"
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: "2025-10-26T16:45:00",
        description: "Package delivered successfully. Signed by recipient.",
        completed: true,
        agent: "Delivery Driver #25"
      }
    ],
    notes: []
  }
}

export default function OrderLookupPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [searchTerm, setSearchTerm] = useState(searchParams.get("ref") || "")
  const [isSearching, setIsSearching] = useState(false)
  const [orderDetails, setOrderDetails] = useState<any>(null)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [newNote, setNewNote] = useState("")

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      setSearchError("Please enter an order number or tracking number")
      return
    }

    setIsSearching(true)
    setSearchError(null)
    setOrderDetails(null)

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 500))

    const term = searchTerm.trim().toUpperCase()
    const found = mockOrderDetails[term]

    if (found) {
      setOrderDetails(found)
    } else {
      setSearchError("Order not found. Please check the reference number and try again.")
    }

    setIsSearching(false)
  }

  const handleSearchKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleSearch()
    }
  }

  const handleAddNote = () => {
    if (!newNote.trim() || !orderDetails) return

    const note = {
      id: orderDetails.notes.length + 1,
      note: newNote,
      createdBy: "Current Agent",
      createdAt: new Date().toISOString()
    }

    setOrderDetails({
      ...orderDetails,
      notes: [...orderDetails.notes, note]
    })
    setNewNote("")
  }

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      "Delivered": "text-green-600",
      "In Transit": "text-blue-600",
      "Processing": "text-yellow-600",
      "Out for Delivery": "text-purple-600",
      "Delayed": "text-red-600",
      "Exception": "text-red-600"
    }
    return colors[status] || "text-gray-600"
  }

  const getStatusBadge = (status: string) => {
    const styles: Record<string, string> = {
      "Delivered": "bg-green-100 text-green-800",
      "In Transit": "bg-blue-100 text-blue-800",
      "Processing": "bg-yellow-100 text-yellow-800",
      "Out for Delivery": "bg-purple-100 text-purple-800",
      "Delayed": "bg-red-100 text-red-800",
      "Exception": "bg-red-100 text-red-800"
    }
    return styles[status] || "bg-gray-100 text-gray-800"
  }

  const getPriorityBadge = (priority: string) => {
    const styles: Record<string, string> = {
      "High": "bg-red-100 text-red-800",
      "Medium": "bg-yellow-100 text-yellow-800",
      "Low": "bg-green-100 text-green-800"
    }
    return styles[priority] || "bg-gray-100 text-gray-800"
  }

  const formatDateTime = (dateString: string | null) => {
    if (!dateString) return "Pending"
    const date = new Date(dateString)
    return date.toLocaleString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  const handleExampleClick = (exampleValue: string) => {
    setSearchTerm(exampleValue)
    setSearchError(null)
  }

  return (
    <div className="space-y-6" style={{ paddingBottom: "80px" }}>
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-black">Order Lookup</h1>
          <p className="text-gray-600 mt-1">Search and view detailed order information</p>
        </div>
        <Button variant="outline" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
      </div>

      {/* Search Bar */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <Label htmlFor="search" className="text-base font-semibold">
          Search by Order Number or Tracking Number
        </Label>
        <div className="flex gap-4 mt-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <Input
              id="search"
              type="text"
              placeholder="Enter ORD-2025-001 or TRK123456789..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyPress={handleSearchKeyPress}
              className="pl-10"
            />
          </div>
          <Button onClick={handleSearch} disabled={isSearching}>
            {isSearching ? "Searching..." : "Search"}
          </Button>
        </div>
        <p className="text-xs text-gray-500 mt-2">
          Enter either an order number (ORD-XXXX-XXX) or tracking number (TRKXXXXXXXXX)
        </p>
      </div>

      {/* Error Message */}
      {searchError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <AlertTriangle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div>
              <h3 className="font-semibold text-red-900">Error</h3>
              <p className="text-sm text-red-800 mt-1">{searchError}</p>
            </div>
          </div>
        </div>
      )}

      {/* Order Details */}
      {orderDetails && (
        <div className="space-y-6">
          {/* Order Summary Card */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <Package className="h-6 w-6 text-blue-600" />
                <div>
                  <h2 className="text-2xl font-bold text-black">
                    {orderDetails.orderNumber}
                  </h2>
                  <p className="text-sm text-gray-600">
                    Tracking: {orderDetails.trackingNumber}
                  </p>
                </div>
              </div>
              <div className="text-right space-y-2">
                <div className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${getStatusBadge(orderDetails.status)}`}>
                  {orderDetails.status}
                </div>
                <div className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ml-2 ${getPriorityBadge(orderDetails.priority)}`}>
                  {orderDetails.priority} Priority
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t">
              <div>
                <p className="text-sm text-gray-600">Carrier</p>
                <p className="font-medium text-black">{orderDetails.carrier}</p>
                <p className="text-xs text-gray-500">{orderDetails.carrierService}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Weight</p>
                <p className="font-medium text-black">{orderDetails.weight}</p>
                <p className="text-xs text-gray-500">{orderDetails.dimensions}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Shipping Cost</p>
                <p className="font-medium text-black">{orderDetails.shippingCost}</p>
                <p className="text-xs text-gray-500">Insurance: {orderDetails.insurance}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">
                  {orderDetails.actualDelivery ? "Delivered" : "Est. Delivery"}
                </p>
                <p className="font-medium text-black">
                  {formatDateTime(orderDetails.actualDelivery || orderDetails.estimatedDelivery)}
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Main Content */}
            <div className="lg:col-span-2 space-y-6">
              {/* Package Information */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-xl font-semibold text-black mb-4">Package Information</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">Description</p>
                    <p className="font-medium text-black">{orderDetails.description}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Created At</p>
                    <p className="font-medium text-black">{formatDateTime(orderDetails.createdAt)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Scanned At</p>
                    <p className="font-medium text-black">{formatDateTime(orderDetails.scannedAt)}</p>
                  </div>
                </div>
              </div>

              {/* Customer Information */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-4">
                  <User className="h-5 w-5 text-gray-400" />
                  <h2 className="text-xl font-semibold text-black">Customer Information</h2>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="flex items-center gap-3">
                    <User className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-600">Name</p>
                      <p className="font-medium text-black">{orderDetails.customer.name}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <Mail className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-600">Email</p>
                      <p className="font-medium text-black">{orderDetails.customer.email}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <Phone className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-600">Phone</p>
                      <p className="font-medium text-black">{orderDetails.customer.phone}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <FileText className="h-4 w-4 text-gray-400" />
                    <div>
                      <p className="text-sm text-gray-600">Customer ID</p>
                      <p className="font-medium text-black">{orderDetails.customer.customerId}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Timeline */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-xl font-semibold text-black mb-4">Shipment Timeline</h2>
                <div className="space-y-4">
                  {orderDetails.timeline.map((event: any, index: number) => (
                    <div key={event.id} className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <div
                          className={`flex items-center justify-center w-10 h-10 rounded-full border-2 ${
                            event.completed
                              ? "bg-blue-50 border-blue-600"
                              : "bg-gray-50 border-gray-300"
                          }`}
                        >
                          {event.completed ? (
                            event.status === "Exception" || event.status === "Delayed" ? (
                              <AlertTriangle className="h-4 w-4 text-orange-600" />
                            ) : (
                              <CheckCircle className="h-4 w-4 text-blue-600" />
                            )
                          ) : (
                            <Clock className="h-4 w-4 text-gray-400" />
                          )}
                        </div>
                        {index < orderDetails.timeline.length - 1 && (
                          <div
                            className={`w-0.5 h-16 ${
                              event.completed ? "bg-blue-600" : "bg-gray-300"
                            }`}
                          />
                        )}
                      </div>
                      <div className="flex-1 pb-8">
                        <div className="flex items-start justify-between">
                          <div>
                            <p
                              className={`font-semibold ${
                                event.completed ? "text-black" : "text-gray-500"
                              }`}
                            >
                              {event.status}
                            </p>
                            <p className="text-sm text-gray-600 mt-0.5 flex items-center gap-1">
                              <MapPin className="h-3 w-3" />
                              {event.location}
                            </p>
                            <p className="text-sm text-gray-500 mt-1">
                              {event.description}
                            </p>
                            <p className="text-xs text-gray-500 mt-1">
                              Agent: {event.agent}
                            </p>
                          </div>
                          {event.timestamp && (
                            <p className="text-xs text-gray-500 whitespace-nowrap ml-4">
                              {formatDateTime(event.timestamp)}
                            </p>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Internal Notes */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-xl font-semibold text-black mb-4">Internal Notes</h2>
                
                {/* Add Note */}
                <div className="mb-4">
                  <Label htmlFor="newNote">Add New Note</Label>
                  <div className="flex gap-2 mt-2">
                    <Input
                      id="newNote"
                      type="text"
                      placeholder="Enter internal note about this order..."
                      value={newNote}
                      onChange={(e) => setNewNote(e.target.value)}
                      onKeyPress={(e) => e.key === "Enter" && handleAddNote()}
                    />
                    <Button onClick={handleAddNote}>Add</Button>
                  </div>
                </div>

                {/* Notes List */}
                {orderDetails.notes.length > 0 ? (
                  <div className="space-y-3">
                    {orderDetails.notes.map((note: any) => (
                      <div key={note.id} className="bg-gray-50 rounded-lg p-3 border">
                        <p className="text-sm text-black">{note.note}</p>
                        <div className="flex items-center gap-2 mt-2 text-xs text-gray-500">
                          <User className="h-3 w-3" />
                          <span>{note.createdBy}</span>
                          <span>•</span>
                          <span>{formatDateTime(note.createdAt)}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500 border-2 border-dashed rounded-lg">
                    <Info className="h-8 w-8 mx-auto mb-2 text-gray-400" />
                    <p>No internal notes yet</p>
                  </div>
                )}
              </div>
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              {/* Origin */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-4">
                  <Building2 className="h-5 w-5 text-gray-400" />
                  <h2 className="text-lg font-semibold text-black">Origin</h2>
                </div>
                <div className="space-y-2 text-sm">
                  <p className="font-medium text-black">{orderDetails.origin.name}</p>
                  <p className="text-gray-600">{orderDetails.origin.address}</p>
                  <p className="text-gray-600">
                    {orderDetails.origin.postalCode} {orderDetails.origin.city}
                  </p>
                  <p className="text-gray-600">{orderDetails.origin.country}</p>
                  <div className="pt-3 border-t mt-3">
                    <p className="text-xs text-gray-500">Contact Person</p>
                    <p className="text-gray-700">{orderDetails.origin.contactPerson}</p>
                    <p className="text-gray-700 flex items-center gap-1 mt-1">
                      <Phone className="h-3 w-3" />
                      {orderDetails.origin.contactPhone}
                    </p>
                  </div>
                </div>
              </div>

              {/* Destination */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <div className="flex items-center gap-2 mb-4">
                  <Home className="h-5 w-5 text-gray-400" />
                  <h2 className="text-lg font-semibold text-black">Destination</h2>
                </div>
                <div className="space-y-2 text-sm">
                  <p className="font-medium text-black">{orderDetails.destination.name}</p>
                  <p className="text-gray-600">{orderDetails.destination.address}</p>
                  <p className="text-gray-600">
                    {orderDetails.destination.postalCode} {orderDetails.destination.city}
                  </p>
                  <p className="text-gray-600">{orderDetails.destination.country}</p>
                  <div className="pt-3 border-t mt-3">
                    <p className="text-xs text-gray-500">Contact Phone</p>
                    <p className="text-gray-700 flex items-center gap-1">
                      <Phone className="h-3 w-3" />
                      {orderDetails.destination.contactPhone}
                    </p>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-lg font-semibold text-black mb-4">Actions</h2>
                <div className="space-y-3">
                  <Button className="w-full" variant="outline">
                    <FileText className="mr-2 h-4 w-4" />
                    Create Ticket
                  </Button>
                  <Button className="w-full" variant="outline">
                    <Mail className="mr-2 h-4 w-4" />
                    Email Customer
                  </Button>
                  <Button className="w-full" variant="outline">
                    <Truck className="mr-2 h-4 w-4" />
                    Contact Carrier
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Help Text when no order is displayed */}
      {!orderDetails && !searchError && (
        <div className="bg-gray-50 border-2 border-dashed rounded-lg p-12">
          <div className="text-center">
            <Package className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-700 mb-2">
              Search for an Order
            </h3>
            <p className="text-gray-600 max-w-md mx-auto mb-6">
              Enter an order number or tracking number in the search bar above to view
              detailed information about the shipment, including timeline, customer details,
              and delivery status.
            </p>
            
            {/* Examples section */}
            <div className="bg-white border rounded-lg p-6 max-w-2xl mx-auto">
              <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center justify-center gap-2">
                <Info className="h-4 w-4" />
                Try these example orders:
              </h4>
              <div className="space-y-4">
                {/* In Transit Order */}
                <div>
                  <p className="text-sm font-semibold text-blue-900 mb-2">📦 In Transit Order</p>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleExampleClick("ORD-2025-001")}
                      className="flex-1 bg-blue-50 border border-blue-200 rounded-md p-2 hover:bg-blue-100 transition-colors cursor-pointer"
                    >
                      <p className="text-xs text-blue-700 font-mono">ORD-2025-001</p>
                    </button>
                    <button
                      onClick={() => handleExampleClick("TRK123456789")}
                      className="flex-1 bg-blue-50 border border-blue-200 rounded-md p-2 hover:bg-blue-100 transition-colors cursor-pointer"
                    >
                      <p className="text-xs text-blue-700 font-mono">TRK123456789</p>
                    </button>
                  </div>
                </div>

                {/* Delivered Order */}
                <div>
                  <p className="text-sm font-semibold text-green-900 mb-2">✅ Delivered Order</p>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleExampleClick("ORD-2025-002")}
                      className="flex-1 bg-green-50 border border-green-200 rounded-md p-2 hover:bg-green-100 transition-colors cursor-pointer"
                    >
                      <p className="text-xs text-green-700 font-mono">ORD-2025-002</p>
                    </button>
                    <button
                      onClick={() => handleExampleClick("TRK987654321")}
                      className="flex-1 bg-green-50 border border-green-200 rounded-md p-2 hover:bg-green-100 transition-colors cursor-pointer"
                    >
                      <p className="text-xs text-green-700 font-mono">TRK987654321</p>
                    </button>
                  </div>
                </div>
              </div>
              <p className="text-xs text-gray-500 mt-4">
                💡 Click on any code above to automatically fill the search bar
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
