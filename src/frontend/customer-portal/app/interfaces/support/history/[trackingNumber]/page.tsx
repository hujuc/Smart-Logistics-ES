"use client"

import { useState, useEffect, useCallback } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import {
  ArrowLeft,
  Package,
  Truck,
  CheckCircle,
  Clock,
  AlertTriangle,
  Building2,
  FileText
} from "lucide-react"
import { customerOrderApi } from "@/lib/services/customer-order.service"
import { orderDocumentService } from "@/lib/services/order-document.service"
import type { Order, OrderEvent } from "@/types/common"
import { formatOrderShippingAddress } from "@/lib/address"
import Image from "next/image"
import { featureFlagManager, isProofOfDeliveryEnabled } from "@/lib/flagsmith"

export default function OrderTrackingPage() {
  const params = useParams()
  const router = useRouter()
  const trackingNumber = params.trackingNumber as string

  const [order, setOrder] = useState<Order | null>(null)
  const [events, setEvents] = useState<OrderEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [podImageUrl, setPodImageUrl] = useState<string | null>(null)
  const [isPodLoading, setIsPodLoading] = useState(false)
  const [podFeatureEnabled, setPodFeatureEnabled] = useState(true)

  // Initialize Flagsmith on mount
  useEffect(() => {
    const initFlags = async () => {
      const customerId = 'customer-' + Math.random().toString(36).substring(7);
      await featureFlagManager.initialize(customerId);
      const enabled = isProofOfDeliveryEnabled();
      setPodFeatureEnabled(enabled);
      console.log('[OrderTracking] POD feature enabled:', enabled);
    };
    initFlags();
  }, []);

  const loadOrderData = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      console.log('[OrderTracking] Loading order with tracking:', trackingNumber)
      const orderData = await customerOrderApi.getOrderByTrackingNumber(trackingNumber)
      console.log('[OrderTracking] Order loaded:', orderData)
      setOrder(orderData)

      if (orderData.id) {
        console.log('[OrderTracking] Loading events for order:', orderData.id)
        const eventsData = await customerOrderApi.getOrderEvents(orderData.id)
        console.log('[OrderTracking] Events loaded:', eventsData)
        console.log('[OrderTracking] Number of events:', eventsData?.length || 0)
        if (eventsData && eventsData.length > 0) {
          console.log('[OrderTracking] First event:', eventsData[0])
        }
        setEvents(eventsData)
      }
    } catch (err) {
      console.error('[OrderTracking] Error:', err)
      setError(err instanceof Error ? err.message : 'Failed to load order')
    } finally {
      setLoading(false)
    }
  }, [trackingNumber])

  useEffect(() => {
    loadOrderData()
  }, [loadOrderData])

  useEffect(() => {
    // POD feature check is now done in the initialization useEffect above
    const enabled = podFeatureEnabled;

    if (!order?.id || !enabled) {
      setPodImageUrl((prev) => {
        if (prev) {
          URL.revokeObjectURL(prev)
        }
        return null
      })
      return
    }

    let isMounted = true
    let currentUrl: string | null = null

    const loadPodImage = async () => {
      setIsPodLoading(true)

      try {
        const documents = await orderDocumentService.list(order.id)
        // Look for POD images - they typically don't start with "qr"
        const podDocument = documents.find((doc) =>
          !doc.documentId.toLowerCase().startsWith("qr") &&
          (doc.contentType?.startsWith("image/") ||
           doc.documentId.match(/\.(jpg|jpeg|png|gif|webp)$/i))
        )

        if (!podDocument || !isMounted) {
          return
        }

        const blob = await orderDocumentService.download(order.id, podDocument.documentId)

        if (!isMounted) {
          return
        }

        const objectUrl = URL.createObjectURL(blob)
        currentUrl = objectUrl
        setPodImageUrl((prev) => {
          if (prev) {
            URL.revokeObjectURL(prev)
          }
          return objectUrl
        })
      } catch (err) {
        console.error("Failed to load POD image:", err)
      } finally {
        if (isMounted) {
          setIsPodLoading(false)
        }
      }
    }

    loadPodImage()

    return () => {
      isMounted = false
      if (currentUrl) {
        URL.revokeObjectURL(currentUrl)
      }
    }
  }, [order?.id])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "DELIVERED":
        return <CheckCircle className="h-6 w-6 text-green-600" />
      case "IN_TRANSIT":
      case "COLLECTED":
        return <Truck className="h-6 w-6 text-blue-600" />
      case "CREATED":
      case "READY_FOR_PICKUP":
        return <Package className="h-6 w-6 text-yellow-600" />
      case "CANCELLED":
        return <AlertTriangle className="h-6 w-6 text-red-600" />
      default:
        return <Clock className="h-6 w-6 text-gray-400" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "DELIVERED":
        return "bg-green-100 text-green-800"
      case "IN_TRANSIT":
      case "COLLECTED":
        return "bg-blue-100 text-blue-800"
      case "CREATED":
      case "READY_FOR_PICKUP":
        return "bg-yellow-100 text-yellow-800"
      case "CANCELLED":
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
          <h2 className="text-xl font-semibold text-black mb-2">
            {error ? 'Error Loading Order' : 'Tracking Number Not Found'}
          </h2>
          <p className="text-gray-600">
            {error || `We could not find tracking number: ${trackingNumber}`}
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => router.back()}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">Track Order</h1>
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold mb-4">Package Information</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-600">Order Reference</p>
                <p className="font-medium">{order.reference}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Status</p>
                <p className="font-medium">{order.status}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Description</p>
                <p className="font-medium">{order.description || 'N/A'}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Shipping Address</p>
                <p className="font-medium">{formatOrderShippingAddress(order)}</p>
              </div>
              {order.estimatedDeliveryDate && (
                <div>
                  <p className="text-sm text-gray-600">Estimated Delivery</p>
                  <p className="font-medium">
                    {new Date(order.estimatedDeliveryDate).toLocaleDateString()}
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-xl font-semibold mb-6">Shipping Timeline</h2>
            {events.length > 0 ? (
              <div className="relative">
                {/* Timeline line */}
                <div className="absolute left-[15px] top-0 bottom-0 w-0.5 bg-gray-200" />

                {/* Events */}
                <div className="space-y-6">
                  {events.map((event, index) => {
                    const isLast = index === events.length - 1

                    return (
                      <div key={event.id} className="relative flex gap-4">
                        {/* Icon */}
                        <div className={`relative z-10 flex-shrink-0 w-8 h-8 rounded-full border-2 border-white ${
                          isLast ? 'bg-gray-200' : 'bg-blue-500'
                        } flex items-center justify-center`}>
                          {isLast ? (
                            <Clock className="w-4 h-4 text-gray-600" />
                          ) : (
                            <CheckCircle className="w-4 h-4 text-white" />
                          )}
                        </div>

                        {/* Content */}
                        <div className="flex-1 pb-6">
                          <div className="flex items-start justify-between">
                            <div>
                              <h3 className="font-semibold">{event.eventType}</h3>
                              {event.notes && (
                                <p className="text-sm text-gray-600 mt-1">{event.notes}</p>
                              )}
                            </div>
                            {event.occurredAt && (
                              <div className="text-sm text-gray-500 flex items-center gap-1">
                                <Clock className="w-3 h-3" />
                                {new Date(event.occurredAt).toLocaleString()}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            ) : (
              <p className="text-gray-500 text-center py-4">No tracking events available yet.</p>
            )}
          </div>

          {(order.status === 'DELIVERED' && podImageUrl && podFeatureEnabled) && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-xl font-semibold mb-4">Proof of Delivery</h2>
              {isPodLoading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                </div>
              ) : podImageUrl ? (
                <div className="flex flex-col items-center">
                  <div className="relative w-full max-w-md">
                    <Image
                      src={podImageUrl}
                      alt="Proof of delivery"
                      width={400}
                      height={400}
                      className="rounded-lg border bg-gray-50 object-contain w-full"
                    />
                  </div>
                  <p className="text-sm text-gray-500 mt-3 text-center">
                    Photo taken by driver upon delivery
                  </p>
                </div>
              ) : null}
            </div>
          )}
        </div>

        <div className="space-y-6">
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <h2 className="text-lg font-semibold mb-4">Quick Actions</h2>
            <div className="space-y-3">
              <Button className="w-full" variant="outline">
                <FileText className="mr-2 h-4 w-4" />
                Download Receipt
              </Button>
              <Button className="w-full" variant="outline" onClick={() => router.push('/interfaces/support')}>
                Contact Support
              </Button>
            </div>
          </div>

          {order.warehouseId && (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h2 className="text-lg font-semibold mb-4">Warehouse</h2>
              <div className="flex items-start gap-3">
                <Building2 className="h-5 w-5 text-gray-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium">Warehouse ID: {order.warehouseId}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
