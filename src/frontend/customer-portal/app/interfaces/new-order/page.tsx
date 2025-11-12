"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ArrowLeft, MapPin } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/shared/lib/auth"
import { customerOrderApi } from "@/lib/services/customer-order.service"
import { Warehouse } from "@/types/common"
import { buildShippingAddressString, formatOrderShippingAddress } from "@/lib/address"

type OrderFormState = {
  reference: string;
  description: string;
  shippingAddressLine1: string;
  shippingAddressLine2: string;
  shippingCity: string;
  shippingStateOrProvince: string;
  shippingPostalCode: string;
  shippingCountry: string;
  shippingPhone: string;
  shippingEmail: string;
  warehouseId: string;
  trackingNumber: string;
}

export default function NewOrderPage() {
  const router = useRouter()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [isLoadingWarehouses, setIsLoadingWarehouses] = useState(true)
  const [formData, setFormData] = useState<OrderFormState>({
    reference: "",
    description: "",
    shippingAddressLine1: "",
    shippingAddressLine2: "",
    shippingCity: "",
    shippingStateOrProvince: "",
    shippingPostalCode: "",
    shippingCountry: "",
    shippingPhone: "",
    shippingEmail: "",
    warehouseId: "", // Will be set after loading warehouses
    trackingNumber: "",
  })
  const updateForm = (field: keyof OrderFormState, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const shippingFields = {
    shippingAddressLine1: formData.shippingAddressLine1,
    shippingAddressLine2: formData.shippingAddressLine2,
    shippingCity: formData.shippingCity,
    shippingStateOrProvince: formData.shippingStateOrProvince,
    shippingPostalCode: formData.shippingPostalCode,
    shippingCountry: formData.shippingCountry,
    shippingPhone: formData.shippingPhone,
    shippingEmail: formData.shippingEmail,
  }

  const shippingPreview = formatOrderShippingAddress(shippingFields)

  useEffect(() => {
    // Check if user is authenticated
    if (!isAuthenticated()) {
      router.push("/")
    }
  }, [router])

  useEffect(() => {
    // Load warehouses
    const loadWarehouses = async () => {
      try {
        setIsLoadingWarehouses(true)
        const data = await customerOrderApi.getWarehouses()
        setWarehouses(data.filter(w => w.active))
      } catch (err) {
        console.error("Failed to load warehouses:", err)
        setError("Failed to load warehouses. Please refresh the page.")
      } finally {
        setIsLoadingWarehouses(false)
      }
    }

    loadWarehouses()
  }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    setError(null)

    try {
      const warehouseId = parseInt(formData.warehouseId, 10)
      if (Number.isNaN(warehouseId)) {
        setError("Please select a warehouse.")
        setIsSubmitting(false)
        return
      }

      const shippingAddress = buildShippingAddressString(shippingFields)
      if (!shippingAddress) {
        setError("Please provide a valid shipping address.")
        setIsSubmitting(false)
        return
      }

      await customerOrderApi.createOrder({
        reference: formData.reference,
        description: formData.description || undefined,
        shippingAddress,
        shippingAddressLine1: formData.shippingAddressLine1,
        shippingAddressLine2: formData.shippingAddressLine2 || undefined,
        shippingCity: formData.shippingCity,
        shippingStateOrProvince: formData.shippingStateOrProvince,
        shippingPostalCode: formData.shippingPostalCode,
        shippingCountry: formData.shippingCountry,
        shippingPhone: formData.shippingPhone || undefined,
        shippingEmail: formData.shippingEmail || undefined,
        warehouseId,
        trackingNumber: formData.trackingNumber || undefined,
      })

      router.push("/interfaces/support/history")
    } catch (err: unknown) {
      console.error("Failed to create order:", err)
      setError(err instanceof Error ? err.message : "Failed to create order. Please try again.")
      setIsSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/interfaces/support/history">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <h1 className="text-3xl font-bold text-black">Create New Order</h1>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Order Information
              </h3>
              <div>
                <Label htmlFor="reference">Order Reference *</Label>
                <Input
                  id="reference"
                  value={formData.reference}
                  onChange={(e) => updateForm("reference", e.target.value)}
                  required
                  placeholder="ORD-2024-001"
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="trackingNumber">Tracking Number</Label>
                <Input
                  id="trackingNumber"
                  value={formData.trackingNumber}
                  onChange={(e) => updateForm("trackingNumber", e.target.value)}
                  placeholder="TRK..."
                  className="mt-1"
                />
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Shipping Information
              </h3>
              <div>
                <Label htmlFor="warehouse">Warehouse *</Label>
                <div className="relative mt-1">
                  <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-500" />
                  <select
                    id="warehouse"
                    value={formData.warehouseId}
                    onChange={(e) => updateForm("warehouseId", e.target.value)}
                    required
                    disabled={isLoadingWarehouses}
                    className="flex h-10 w-full rounded-md border bg-white pl-10 pr-3 py-2 text-base text-black shadow-sm placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 md:text-sm disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    <option value="">Select a warehouse</option>
                    {isLoadingWarehouses ? (
                      <option disabled>Loading warehouses...</option>
                    ) : warehouses.length === 0 ? (
                      <option disabled>No warehouses available</option>
                    ) : (
                      warehouses.map((warehouse) => (
                        <option key={warehouse.id} value={warehouse.id}>
                          {warehouse.name} - {warehouse.city}, {warehouse.country}
                        </option>
                      ))
                    )}
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="shippingAddressLine1">Address Line 1 *</Label>
                  <Input
                    id="shippingAddressLine1"
                    value={formData.shippingAddressLine1}
                    onChange={(e) => updateForm("shippingAddressLine1", e.target.value)}
                    required
                    placeholder="123 Market St"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingAddressLine2">Address Line 2</Label>
                  <Input
                    id="shippingAddressLine2"
                    value={formData.shippingAddressLine2}
                    onChange={(e) => updateForm("shippingAddressLine2", e.target.value)}
                    placeholder="Suite / Apartment"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingCity">City *</Label>
                  <Input
                    id="shippingCity"
                    value={formData.shippingCity}
                    onChange={(e) => updateForm("shippingCity", e.target.value)}
                    required
                    placeholder="San Jose"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingState">State / Province *</Label>
                  <Input
                    id="shippingState"
                    value={formData.shippingStateOrProvince}
                    onChange={(e) => updateForm("shippingStateOrProvince", e.target.value)}
                    required
                    placeholder="CA"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingPostalCode">Postal Code *</Label>
                  <Input
                    id="shippingPostalCode"
                    value={formData.shippingPostalCode}
                    onChange={(e) => updateForm("shippingPostalCode", e.target.value)}
                    required
                    placeholder="95122"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingCountry">Country *</Label>
                  <Input
                    id="shippingCountry"
                    value={formData.shippingCountry}
                    onChange={(e) => updateForm("shippingCountry", e.target.value)}
                    required
                    placeholder="United States"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingPhone">Contact Phone</Label>
                  <Input
                    id="shippingPhone"
                    type="tel"
                    value={formData.shippingPhone}
                    onChange={(e) => updateForm("shippingPhone", e.target.value)}
                    placeholder="+1 555 123 4567"
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="shippingEmail">Contact Email</Label>
                  <Input
                    id="shippingEmail"
                    type="email"
                    value={formData.shippingEmail}
                    onChange={(e) => updateForm("shippingEmail", e.target.value)}
                    placeholder="customer@example.com"
                    className="mt-1"
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="shippingPreview">Shipping Address Preview</Label>
                <Input
                  id="shippingPreview"
                  value={shippingPreview || ""}
                  disabled
                  className="mt-1"
                />
                <p className="text-xs text-gray-500 mt-1">
                  We send this combined address to carriers and the warehouse team when processing your shipment.
                </p>
              </div>
            </div>

            <div className="md:col-span-2 space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Additional Information
              </h3>
              <div>
                <Label htmlFor="description">Description / Notes</Label>
                <textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => updateForm("description", e.target.value)}
                  className="mt-1 flex min-h-[80px] w-full rounded-md border bg-white px-3 py-2 text-base text-black shadow-sm placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 md:text-sm"
                  placeholder="Package contents, special handling instructions..."
                />
              </div>
            </div>
          </div>

          <div className="flex gap-4 mt-6 pt-6 border-t">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creating..." : "Create Order"}
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push("/interfaces/support/history")}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
          </div>
        </div>
      </form>
    </div>
  )
}
