"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select } from "@/components/ui/select"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/lib/shared-auth"
import { warehouseService } from "@/lib/services/warehouse.service"
import { buildShippingAddressString, formatOrderShippingAddress, type AddressFieldName } from "@/lib/address"
import type { Order, ShipmentDocument, ShipmentOrderStatus } from "@/types"
import { STATUS_LABELS } from "@/types"
import { orderDocumentService } from "@/lib/services/order-document.service"
import { shipmentDocumentService } from "@/lib/services/shipment-document.service"

interface PackageDetailsPageProps {
  params: Promise<{ id: string }>
}

export default function PackageDetailsPage({ params }: PackageDetailsPageProps) {
  const router = useRouter()
  const [isEditing, setIsEditing] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [order, setOrder] = useState<Order | null>(null)
  const [orderId, setOrderId] = useState<string | null>(null)
  const [isQrLoading, setIsQrLoading] = useState(false)
  const [qrError, setQrError] = useState<string | null>(null)
  const [qrImageUrl, setQrImageUrl] = useState<string | null>(null)
  const [isPodLoading, setIsPodLoading] = useState(false)
  const [podError, setPodError] = useState<string | null>(null)
  const [podImageUrl, setPodImageUrl] = useState<string | null>(null)
  const [isStatusChanging, setIsStatusChanging] = useState(false)
  const [shipmentDocuments, setShipmentDocuments] = useState<ShipmentDocument[]>([])
  const [isShipmentDocsLoading, setIsShipmentDocsLoading] = useState(false)
  const [shipmentDocsError, setShipmentDocsError] = useState<string | null>(null)

  // Unwrap params Promise
  useEffect(() => {
    params.then(p => setOrderId(p.id))
  }, [params])

  // Fetch order data
  useEffect(() => {
    if (!orderId) return

    if (!isAuthenticated()) {
      setError("You must be logged in to view order details.")
      setIsLoading(false)
      router.push("/")
      return
    }

    const loadOrder = async () => {
      try {
        setIsLoading(true)
        setError(null)
        const data = await warehouseService.getOrderById(parseInt(orderId))
        setOrder(data)
      } catch (err) {
        console.error('Failed to load order:', err)
        setError('Failed to load order details. Please try again.')
      } finally {
        setIsLoading(false)
      }
    }

    loadOrder()
  }, [orderId, router])

  useEffect(() => {
    if (!orderId) {
      setQrImageUrl((prev) => {
        if (prev) {
          URL.revokeObjectURL(prev)
        }
        return null
      })
      return
    }

    if (!isAuthenticated()) return

    let isMounted = true
    let currentUrl: string | null = null

    const resetQrImage = () => {
      setQrImageUrl((prev) => {
        if (prev) {
          URL.revokeObjectURL(prev)
        }
        return null
      })
      currentUrl = null
    }

    const loadQrCode = async () => {
      setIsQrLoading(true)
      setQrError(null)
      resetQrImage()

      try {
        const orderNumericId = parseInt(orderId, 10)
        if (Number.isNaN(orderNumericId)) {
          throw new Error("Invalid order identifier")
        }

        const documents = await orderDocumentService.list(orderNumericId)
        const qrDocument = documents.find((doc) =>
          doc.documentId.toLowerCase().startsWith("qr"),
        )

        if (!qrDocument) {
          return
        }

        const blob = await orderDocumentService.download(
          orderNumericId,
          qrDocument.documentId,
        )

        if (!isMounted) {
          return
        }

        const objectUrl = URL.createObjectURL(blob)
        currentUrl = objectUrl
        setQrImageUrl((prev) => {
          if (prev) {
            URL.revokeObjectURL(prev)
          }
          return objectUrl
        })
      } catch (err) {
        console.error("Failed to load QR code:", err)
        if (isMounted) {
          setQrError("Failed to load the QR code. Please try again later.")
          resetQrImage()
        }
      } finally {
        if (isMounted) {
          setIsQrLoading(false)
        }
      }
    }

    loadQrCode()

    return () => {
      isMounted = false
      if (currentUrl) {
        URL.revokeObjectURL(currentUrl)
      }
    }
  }, [orderId])

  useEffect(() => {
    if (!orderId) {
      setPodImageUrl((prev) => {
        if (prev) {
          URL.revokeObjectURL(prev)
        }
        return null
      })
      return
    }

    if (!isAuthenticated()) return

    let isMounted = true
    let currentUrl: string | null = null

    const resetPodImage = () => {
      setPodImageUrl((prev) => {
        if (prev) {
          URL.revokeObjectURL(prev)
        }
        return null
      })
      currentUrl = null
    }

    const loadPodImage = async () => {
      setIsPodLoading(true)
      setPodError(null)
      resetPodImage()

      try {
        const orderNumericId = parseInt(orderId, 10)
        if (Number.isNaN(orderNumericId)) {
          throw new Error("Invalid order identifier")
        }

        const documents = await orderDocumentService.list(orderNumericId)
        // Look for POD images - they typically don't start with "qr"
        const podDocument = documents.find((doc) =>
          !doc.documentId.toLowerCase().startsWith("qr") &&
          (doc.contentType?.startsWith("image/") ||
           doc.documentId.match(/\.(jpg|jpeg|png|gif|webp)$/i))
        )

        if (!podDocument) {
          return
        }

        const blob = await orderDocumentService.download(
          orderNumericId,
          podDocument.documentId,
        )

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
        if (isMounted) {
          setPodError("Failed to load proof of delivery photo.")
          resetPodImage()
        }
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
  }, [orderId])

  useEffect(() => {
    if (!orderId) {
      setShipmentDocuments([])
      return
    }

    const numericId = parseInt(orderId, 10)
    if (Number.isNaN(numericId)) {
      setShipmentDocuments([])
      return
    }

    if (!order?.shipmentId) {
      setShipmentDocuments([])
      return
    }

    let isMounted = true
    setIsShipmentDocsLoading(true)
    setShipmentDocsError(null)

    shipmentDocumentService
      .list(numericId)
      .then((docs) => {
        if (!isMounted) return
        setShipmentDocuments(docs)
      })
      .catch((err) => {
        if (!isMounted) return
        setShipmentDocsError(err instanceof Error ? err.message : "Failed to load shipping documents")
      })
      .finally(() => {
        if (!isMounted) return
        setIsShipmentDocsLoading(false)
      })

    return () => {
      isMounted = false
    }
  }, [orderId, order?.shipmentId])

  const handleSave = async () => {
    if (!order) return

    try {
      const normalizedAddress = buildShippingAddressString(order)
      await warehouseService.updateOrder(order.id, {
        reference: order.reference,
        description: order.description,
        status: order.status,
        shippingAddress: normalizedAddress || order.shippingAddress,
        shippingAddressLine1: order.shippingAddressLine1,
        shippingAddressLine2: order.shippingAddressLine2,
        shippingCity: order.shippingCity,
        shippingStateOrProvince: order.shippingStateOrProvince,
        shippingPostalCode: order.shippingPostalCode,
        shippingCountry: order.shippingCountry,
        shippingPhone: order.shippingPhone,
        shippingEmail: order.shippingEmail,
        warehouseId: order.warehouseId,
        trackingNumber: order.trackingNumber,
        estimatedDeliveryDate: order.estimatedDeliveryDate instanceof Date ? order.estimatedDeliveryDate.toISOString() : order.estimatedDeliveryDate as string,
        carrierId: order.carrierId,
      })
      setIsEditing(false)
    } catch (err) {
      console.error('Failed to update order:', err)
      alert('Failed to update order. Please try again.')
    }
  }

  const handleAddressFieldChange = (field: AddressFieldName, value: string) => {
    if (!order) return

    const updatedOrder: Order = {
      ...order,
      [field]: value,
    } as Order

    const normalized = buildShippingAddressString(updatedOrder)
    updatedOrder.shippingAddress = normalized || order.shippingAddress
    setOrder(updatedOrder)
  }

  const handleStatusChange = async (newStatus: ShipmentOrderStatus) => {
    if (!order || !orderId) return

    try {
      setIsStatusChanging(true)
      await warehouseService.updateOrderStatus(parseInt(orderId), newStatus)
      
      // Reload order to get updated data
      const updatedOrder = await warehouseService.getOrderById(parseInt(orderId))
      setOrder(updatedOrder)
      
      alert(`Status successfully changed to ${STATUS_LABELS[newStatus]}`)
    } catch (err) {
      console.error('Failed to update status:', err)
      alert('Failed to update status. Please try again.')
    } finally {
      setIsStatusChanging(false)
    }
  }

  const handleShipmentDocumentDownload = async (doc: ShipmentDocument) => {
    if (!orderId) return

    try {
      const orderNumericId = parseInt(orderId, 10)
      if (Number.isNaN(orderNumericId)) {
        throw new Error("Invalid order identifier")
      }

      const blob = await shipmentDocumentService.download(orderNumericId, doc.documentId)
      const url = URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.download = doc.fileName || doc.documentId
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      setTimeout(() => URL.revokeObjectURL(url), 500)
    } catch (err) {
      console.error("Failed to download shipment document", err)
      setShipmentDocsError(err instanceof Error ? err.message : "Failed to download shipping document")
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading order details...</p>
        </div>
      </div>
    )
  }

  if (error || !order) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error || 'Order not found'}</p>
          <Link href="/interfaces/warehouse">
            <Button>Back to Warehouse</Button>
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/interfaces/warehouse">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <h1 className="text-3xl font-bold text-black">Package Details</h1>
      </div>

      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-black">
            {order.trackingNumber || order.reference}
          </h2>
          {!isEditing ? (
            <Button onClick={() => setIsEditing(true)}>Edit</Button>
          ) : (
            <div className="flex gap-2">
              <Button variant="outline" onClick={() => setIsEditing(false)}>
                Cancel
              </Button>
              <Button onClick={handleSave}>Save Changes</Button>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Order Information
            </h3>
            <div>
              <Label htmlFor="reference">Reference</Label>
              <Input
                id="reference"
                value={order.reference}
                onChange={(e) =>
                  setOrder({ ...order, reference: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="trackingNumber">Tracking Number</Label>
              <Input
                id="trackingNumber"
                value={order.trackingNumber || ""}
                onChange={(e) =>
                  setOrder({ ...order, trackingNumber: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="origin">Origin</Label>
              <Input
                id="origin"
                value={order.origin || ""}
                disabled
                className="mt-1"
              />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Shipping Information
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="shippingAddressLine1">Address Line 1</Label>
                <Input
                  id="shippingAddressLine1"
                  value={order.shippingAddressLine1 || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingAddressLine1", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingAddressLine2">Address Line 2</Label>
                <Input
                  id="shippingAddressLine2"
                  value={order.shippingAddressLine2 || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingAddressLine2", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingCity">City</Label>
                <Input
                  id="shippingCity"
                  value={order.shippingCity || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingCity", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingState">State / Province</Label>
                <Input
                  id="shippingState"
                  value={order.shippingStateOrProvince || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingStateOrProvince", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingPostalCode">Postal Code</Label>
                <Input
                  id="shippingPostalCode"
                  value={order.shippingPostalCode || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingPostalCode", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingCountry">Country</Label>
                <Input
                  id="shippingCountry"
                  value={order.shippingCountry || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingCountry", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingPhone">Phone</Label>
                <Input
                  id="shippingPhone"
                  value={order.shippingPhone || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingPhone", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="shippingEmail">Email</Label>
                <Input
                  id="shippingEmail"
                  type="email"
                  value={order.shippingEmail || ""}
                  onChange={(e) =>
                    handleAddressFieldChange("shippingEmail", e.target.value)
                  }
                  disabled={!isEditing}
                  className="mt-1"
                />
              </div>
            </div>
            <div>
              <Label htmlFor="shippingAddressPreview">Shipping Address Preview</Label>
              <Input
                id="shippingAddressPreview"
                value={formatOrderShippingAddress(order)}
                disabled
                className="mt-1"
              />
              <p className="text-xs text-gray-500 mt-1">
                The preview above is synced to the legacy shippingAddress field for existing integrations.
              </p>
            </div>
            <div>
              <Label htmlFor="estimatedDelivery">Estimated Delivery</Label>
              <Input
                id="estimatedDelivery"
                value={order.estimatedDeliveryDate ? new Date(order.estimatedDeliveryDate).toLocaleDateString() : ""}
                disabled
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="actualDelivery">Actual Delivery</Label>
              <Input
                id="actualDelivery"
                value={order.actualDelivery ? new Date(order.actualDelivery).toLocaleDateString() : "Not delivered"}
                disabled
                className="mt-1"
              />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Package Details
            </h3>
            <div>
              <Label htmlFor="status">Status</Label>
              <Select
                id="status"
                value={order.status}
                onChange={(e) => handleStatusChange(e.target.value as ShipmentOrderStatus)}
                disabled={isStatusChanging}
                className="mt-1"
              >
                <option value="CREATED">Created</option>
                <option value="ASSIGNED_TO_SHIPMENT">Assigned to Shipment</option>
                <option value="IN_TRANSIT">In Transit</option>
                <option value="DELIVERED">Delivered</option>
                <option value="CANCELLED">Cancelled</option>
              </Select>
              {isStatusChanging && (
                <p className="text-sm text-gray-500 mt-1">Updating status...</p>
              )}
            </div>
            <div>
              <Label htmlFor="priority">Priority</Label>
              <Input
                id="priority"
                value={order.priority}
                disabled
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="weight">Weight (kg)</Label>
              <Input
                id="weight"
                type="number"
                step="0.1"
                value={order.weight || ""}
                disabled
                className="mt-1"
              />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Carrier Information
            </h3>
            <div>
              <Label htmlFor="carrierName">Carrier Name</Label>
              <Input
                id="carrierName"
                value={order.carrierName || "-"}
                disabled
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="carrierCode">Carrier Code</Label>
              <Input
                id="carrierCode"
                value={order.carrierCode || "-"}
                disabled
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="scannedAt">Scanned At</Label>
              <Input
                id="scannedAt"
                value={order.scannedAt ? new Date(order.scannedAt).toLocaleString() : "Not scanned"}
                disabled
                className="mt-1"
              />
            </div>
          </div>

          <div className="md:col-span-2 space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Description
            </h3>
            <div>
              <Label htmlFor="description">Description</Label>
              <textarea
                id="description"
                value={order.description || ""}
                onChange={(e) =>
                  setOrder({ ...order, description: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1 flex min-h-[80px] w-full rounded-md border bg-white px-3 py-2 text-base text-black shadow-sm placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="bg-white border rounded-lg p-6 shadow-sm lg:col-span-2">
          <h3 className="font-semibold text-black mb-4">Order Timeline</h3>
          <div className="space-y-3">
            {order.actualDelivery && (
              <div className="flex gap-4 pb-3 border-b">
                <div className="text-sm text-gray-500 w-48">
                  {new Date(order.actualDelivery).toLocaleString()}
                </div>
                <div className="text-sm text-black">Package delivered</div>
              </div>
            )}
            {order.scannedAt && (
              <div className="flex gap-4 pb-3 border-b">
                <div className="text-sm text-gray-500 w-48">
                  {new Date(order.scannedAt).toLocaleString()}
                </div>
                <div className="text-sm text-black">Package scanned at warehouse</div>
              </div>
            )}
            <div className="flex gap-4 pb-3 border-b">
              <div className="text-sm text-gray-500 w-48">
                {new Date(order.updatedAt).toLocaleString()}
              </div>
              <div className="text-sm text-black">Last updated</div>
            </div>
            <div className="flex gap-4 pb-3">
              <div className="text-sm text-gray-500 w-48">
                {new Date(order.createdAt).toLocaleString()}
              </div>
              <div className="text-sm text-black">Order created</div>
            </div>
          </div>
      </div>

        <div className="bg-white border rounded-lg p-6 shadow-sm lg:col-span-1">
          <h3 className="font-semibold text-black mb-4">Package QR Code</h3>
          {isQrLoading ? (
            <p className="text-gray-600 text-sm">Loading QR code...</p>
          ) : qrError ? (
            <p className="text-red-600 text-sm">{qrError}</p>
          ) : qrImageUrl ? (
            <div className="flex flex-col items-center">
              <Image
                src={qrImageUrl}
                alt="Order QR code"
                width={224}
                height={224}
                className="rounded-md border bg-gray-50 p-4"
              />
            </div>
          ) : (
            <p className="text-gray-600 text-sm">
              No QR code is available for this order.
            </p>
          )}
        </div>
      </div>

      {(order.status === 'DELIVERED' || podImageUrl) && (
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <h3 className="font-semibold text-black mb-4">Proof of Delivery</h3>
          {isPodLoading ? (
            <p className="text-gray-600 text-sm">Loading proof of delivery...</p>
          ) : podError ? (
            <p className="text-red-600 text-sm">{podError}</p>
          ) : podImageUrl ? (
            <div className="flex flex-col items-center">
              <Image
                src={podImageUrl}
                alt="Proof of delivery"
                width={400}
                height={400}
                className="rounded-md border bg-gray-50 object-contain max-h-[600px]"
              />
              <p className="text-sm text-gray-500 mt-2">
                Photo taken by driver upon delivery
              </p>
            </div>
          ) : (
            <p className="text-gray-600 text-sm">
              No proof of delivery photo is available for this order.
            </p>
          )}
        </div>
      )}

      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-black">Carrier Shipping Documents</h3>
          {order.shipmentCode && (
            <span className="text-xs text-gray-500">Shipment {order.shipmentCode}</span>
          )}
        </div>
        {!order.shipmentId && (
          <p className="text-sm text-gray-600">
            Assign this order to a shipment to generate carrier documents.
          </p>
        )}
        {order.shipmentId && (
          <div className="space-y-3">
            {isShipmentDocsLoading && (
              <p className="text-sm text-gray-600">Loading shipping documents...</p>
            )}
            {shipmentDocsError && (
              <p className="text-sm text-red-600">{shipmentDocsError}</p>
            )}
            {!isShipmentDocsLoading && !shipmentDocsError && shipmentDocuments.length === 0 && (
              <p className="text-sm text-gray-600">No shipping documents are available yet.</p>
            )}
            {!isShipmentDocsLoading && shipmentDocuments.length > 0 && (
              <ul className="space-y-3">
                {shipmentDocuments.map((doc) => (
                  <li key={doc.documentId} className="flex items-center justify-between rounded-md border p-3">
                    <div>
                      <p className="text-sm font-medium text-black">{doc.fileName}</p>
                      <p className="text-xs text-gray-500">
                        {doc.carrierCode || 'Carrier'} • {(doc.size / 1024).toFixed(1)} KB
                      </p>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleShipmentDocumentDownload(doc)}
                    >
                      Download
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
