"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import {
  ArrowLeft,
  Package,
  Truck,
  CheckCircle,
  Clock,
  AlertTriangle,
  Home,
  Building2,
  FileText
} from "lucide-react"

interface OrderDetails {
  trackingNumber: string;
  orderNumber: string;
  status: string;
  description: string;
  weight: string;
  estimatedDelivery: string;
  carrier: string;
  origin: {
    name: string;
    address: string;
    city: string;
    country: string;
  };
  destination: {
    name: string;
    address: string;
    city: string;
    country: string;
  };
  timeline: Array<{
    id: string | number;
    status: string;
    location: string;
    timestamp: string | null;
    completed: boolean;
    description: string;
    hasException?: boolean;
  }>;
  exception?: {
    type: string;
    reason: string;
    nextAttempt?: string;
  };
  actualDelivery?: string;
}

// Mock data - in real app, this would come from API
const mockOrderDetails: Record<string, OrderDetails> = {
  "TRK123456789": {
    trackingNumber: "TRK123456789",
    orderNumber: "ORD-2025-001",
    status: "In Transit",
    description: "Electronics Package",
    weight: "2.5 kg",
    estimatedDelivery: "2025-10-29",
    carrier: "FastShip Express",
    origin: {
      name: "Amazon Warehouse",
      address: "Rua do Comércio, 123",
      city: "Lisboa",
      country: "Portugal"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      country: "Portugal"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-25T10:00:00",
        description: "Your order has been confirmed and is being prepared for shipment.",
        completed: true
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Lisboa Distribution Center",
        timestamp: "2025-10-26T08:30:00",
        description: "Package collected from seller and in transit to sorting facility.",
        completed: true
      },
      {
        id: 3,
        status: "In Transit",
        location: "Coimbra Transit Hub",
        timestamp: "2025-10-27T14:20:00",
        description: "Package is on the way to the destination city.",
        completed: true
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: null,
        description: "Package will be delivered today.",
        completed: false
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: null,
        description: "Package successfully delivered to recipient.",
        completed: false
      }
    ]
  },
  "TRK987654321": {
    trackingNumber: "TRK987654321",
    orderNumber: "ORD-2025-002",
    status: "Delivered",
    description: "Books and Stationery",
    weight: "1.2 kg",
    estimatedDelivery: "2025-10-26",
    actualDelivery: "2025-10-26T16:45:00",
    carrier: "QuickDeliver Co.",
    origin: {
      name: "BookStore Warehouse",
      address: "Avenida Central, 789",
      city: "Porto",
      country: "Portugal"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      country: "Portugal"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-23T14:00:00",
        description: "Your order has been confirmed.",
        completed: true
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Porto Distribution Center",
        timestamp: "2025-10-24T09:00:00",
        description: "Package collected from seller.",
        completed: true
      },
      {
        id: 3,
        status: "In Transit",
        location: "On Route to Aveiro",
        timestamp: "2025-10-25T11:30:00",
        description: "Package is on the way.",
        completed: true
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: "2025-10-26T08:00:00",
        description: "Package is out for delivery.",
        completed: true
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: "2025-10-26T16:45:00",
        description: "Package delivered successfully. Signed by recipient.",
        completed: true
      }
    ]
  },
  "TRK456789123": {
    trackingNumber: "TRK456789123",
    orderNumber: "ORD-2025-003",
    status: "Processing",
    description: "Clothing Items",
    weight: "0.8 kg",
    estimatedDelivery: "2025-11-02",
    carrier: "SpeedyPost",
    origin: {
      name: "Fashion Store",
      address: "Rua da Moda, 321",
      city: "Braga",
      country: "Portugal"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      country: "Portugal"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-27T16:30:00",
        description: "Your order has been confirmed and is being prepared.",
        completed: true
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Braga",
        timestamp: null,
        description: "Package will be picked up soon.",
        completed: false
      },
      {
        id: 3,
        status: "In Transit",
        location: "En route",
        timestamp: null,
        description: "Package will be in transit.",
        completed: false
      },
      {
        id: 4,
        status: "Out for Delivery",
        location: "Aveiro",
        timestamp: null,
        description: "Package will be out for delivery.",
        completed: false
      },
      {
        id: 5,
        status: "Delivered",
        location: "Aveiro",
        timestamp: null,
        description: "Package will be delivered.",
        completed: false
      }
    ]
  },
  "TRK321654987": {
    trackingNumber: "TRK321654987",
    orderNumber: "ORD-2025-004",
    status: "Exception",
    description: "Home Appliances",
    weight: "5.0 kg",
    estimatedDelivery: "2025-10-24",
    carrier: "ReliableShip Ltd.",
    exception: {
      type: "Delivery Failed",
      reason: "Recipient not available at delivery address",
      nextAttempt: "2025-10-29T10:00:00"
    },
    origin: {
      name: "Tech Warehouse",
      address: "Zona Industrial, Lote 5",
      city: "Setúbal",
      country: "Portugal"
    },
    destination: {
      name: "John Doe",
      address: "Rua da Universidade, 456",
      city: "Aveiro",
      country: "Portugal"
    },
    timeline: [
      {
        id: 1,
        status: "Order Placed",
        location: "Online",
        timestamp: "2025-10-20T11:00:00",
        description: "Your order has been confirmed.",
        completed: true
      },
      {
        id: 2,
        status: "Package Picked Up",
        location: "Setúbal Distribution Center",
        timestamp: "2025-10-21T10:00:00",
        description: "Package collected from seller.",
        completed: true
      },
      {
        id: 3,
        status: "In Transit",
        location: "Lisboa Transit Hub",
        timestamp: "2025-10-22T14:00:00",
        description: "Package in transit.",
        completed: true
      },
      {
        id: 4,
        status: "Delivery Attempted",
        location: "Aveiro",
        timestamp: "2025-10-24T15:00:00",
        description: "Delivery attempted but recipient was not available. Will retry.",
        completed: true,
        hasException: true
      },
      {
        id: 5,
        status: "Pending Redelivery",
        location: "Aveiro Distribution Center",
        timestamp: "2025-10-24T18:00:00",
        description: "Package at local facility. Next delivery attempt scheduled.",
        completed: false
      }
    ]
  }
}

export default function OrderTrackingPage() {
  const params = useParams()
  const router = useRouter()
  const trackingNumber = params.trackingNumber as string

  const [order, setOrder] = useState<OrderDetails | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      const foundOrder = mockOrderDetails[trackingNumber]
      setOrder(foundOrder || null)
      setLoading(false)
    }, 500)
  }, [trackingNumber])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Delivered":
        return <CheckCircle className="h-6 w-6 text-green-600" />
      case "In Transit":
      case "Out for Delivery":
        return <Truck className="h-6 w-6 text-blue-600" />
      case "Processing":
        return <Package className="h-6 w-6 text-yellow-600" />
      case "Exception":
        return <AlertTriangle className="h-6 w-6 text-red-600" />
      default:
        return <Clock className="h-6 w-6 text-gray-400" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Delivered":
        return "bg-green-100 text-green-800"
      case "In Transit":
      case "Out for Delivery":
        return "bg-blue-100 text-blue-800"
      case "Processing":
        return "bg-yellow-100 text-yellow-800"
      case "Exception":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading tracking information...</p>
        </div>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="space-y-6">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Order History
        </Button>
        <div className="bg-white border rounded-lg p-8 shadow-sm text-center">
          <AlertTriangle className="h-12 w-12 text-red-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-black mb-2">Tracking Number Not Found</h2>
          <p className="text-gray-600">We couldn&apos;t find any information for tracking number: {trackingNumber}</p>
          <p className="text-sm text-gray-500 mt-2">Please check the number and try again.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Order History
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-black">Track Order</h1>
            <p className="text-gray-600 mt-1">Tracking: {order.trackingNumber}</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {getStatusIcon(order.status)}
          <span className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-medium ${getStatusColor(order.status)}`}>
            {order.status}
          </span>
        </div>
      </div>

      {/* Exception Alert */}
      {order.exception && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <AlertTriangle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div className="flex-1">
              <h3 className="font-semibold text-red-900">{order.exception.type}</h3>
              <p className="text-red-700 mt-1">{order.exception.reason}</p>
              {order.exception.nextAttempt && (
                <p className="text-sm text-red-600 mt-2">
                  Next delivery attempt: {new Date(order.exception.nextAttempt).toLocaleString()}
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Package Information */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-4">Package Information</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Order Number</p>
                <p className="font-medium text-black">{order.orderNumber}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Carrier</p>
                <p className="font-medium text-black">{order.carrier}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Description</p>
                <p className="font-medium text-black">{order.description}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Weight</p>
                <p className="font-medium text-black">{order.weight}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Estimated Delivery</p>
                <p className="font-medium text-black">
                  {new Date(order.estimatedDelivery).toLocaleDateString()}
                </p>
              </div>
              {order.actualDelivery && (
                <div>
                  <p className="text-sm text-gray-600">Delivered On</p>
                  <p className="font-medium text-green-700">
                    {new Date(order.actualDelivery).toLocaleString()}
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Shipping Timeline */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-black mb-6">Shipping Timeline</h2>
            <div className="relative">
              {order.timeline.map((event, index: number) => (
                <div key={event.id} className="relative pb-8 last:pb-0">
                  {index < order.timeline.length - 1 && (
                    <div
                      className={`absolute left-4 top-8 w-0.5 h-full ${
                        event.completed ? "bg-blue-500" : "bg-gray-300"
                      }`}
                    />
                  )}
                  <div className="flex items-start gap-4">
                    <div
                      className={`relative z-10 flex h-8 w-8 items-center justify-center rounded-full ${
                        event.completed
                          ? event.hasException
                            ? "bg-orange-100"
                            : "bg-blue-100"
                          : "bg-gray-100"
                      }`}
                    >
                      {event.completed ? (
                        event.hasException ? (
                          <AlertTriangle className="h-4 w-4 text-orange-600" />
                        ) : (
                          <CheckCircle className="h-4 w-4 text-blue-600" />
                        )
                      ) : (
                        <Clock className="h-4 w-4 text-gray-400" />
                      )}
                    </div>
                    <div className="flex-1 pt-0.5">
                      <div className="flex items-start justify-between">
                        <div>
                          <p
                            className={`font-semibold ${
                              event.completed ? "text-black" : "text-gray-500"
                            }`}
                          >
                            {event.status}
                          </p>
                          <p className="text-sm text-gray-600 mt-0.5">
                            {event.location}
                          </p>
                          <p className="text-sm text-gray-500 mt-1">
                            {event.description}
                          </p>
                        </div>
                        {event.timestamp && (
                          <p className="text-xs text-gray-500 whitespace-nowrap ml-4">
                            {new Date(event.timestamp).toLocaleString()}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
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
            <div className="space-y-1">
              <p className="font-medium text-black">{order.origin.name}</p>
              <p className="text-sm text-gray-600">{order.origin.address}</p>
              <p className="text-sm text-gray-600">
                {order.origin.city}, {order.origin.country}
              </p>
            </div>
          </div>

          {/* Destination */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <Home className="h-5 w-5 text-gray-400" />
              <h2 className="text-lg font-semibold text-black">Destination</h2>
            </div>
            <div className="space-y-1">
              <p className="font-medium text-black">{order.destination.name}</p>
              <p className="text-sm text-gray-600">{order.destination.address}</p>
              <p className="text-sm text-gray-600">
                {order.destination.city}, {order.destination.country}
              </p>
            </div>
          </div>

          {/* Actions */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-black mb-4">Need Help?</h2>
            <div className="space-y-3">
              <Button
                className="w-full"
                variant="outline"
                onClick={() => {
                  const params = new URLSearchParams({
                    category: "Delivery Issue",
                    orderNumber: order.orderNumber,
                    trackingNumber: order.trackingNumber,
                    subject: `Issue with order ${order.orderNumber}`,
                    description: `I need help with my order ${order.orderNumber} (Tracking: ${order.trackingNumber}).\n\nOrder Details:\n- Status: ${order.status}\n- Description: ${order.description}\n- Carrier: ${order.carrier}\n- Expected Delivery: ${order.estimatedDelivery}\n\nIssue description: `
                  })
                  router.push(`/interfaces/support/tickets/new?${params.toString()}`)
                }}
              >
                <FileText className="mr-2 h-4 w-4" />
                Report a Ticket
              </Button>
              <p className="text-xs text-gray-500 text-center">
                Having problems with your delivery? Create a support ticket and we&apos;ll help you.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
