"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/shared/lib/auth"
import { customerOrderApi } from "@/lib/services/customer-order.service"

export default function NewOrderPage() {
  const router = useRouter()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    reference: "",
    description: "",
    shippingAddress: "",
    warehouseId: "1", // Default warehouse ID
    trackingNumber: "",
  })

  useEffect(() => {
    // Check if user is authenticated
    if (!isAuthenticated()) {
      router.push("/")
    }
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)
    setError(null)

    try {
      await customerOrderApi.createOrder({
        reference: formData.reference,
        description: formData.description || undefined,
        shippingAddress: formData.shippingAddress,
        warehouseId: parseInt(formData.warehouseId),
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
                  onChange={(e) =>
                    setFormData({ ...formData, reference: e.target.value })
                  }
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
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      trackingNumber: e.target.value,
                    })
                  }
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
                <Label htmlFor="shippingAddress">Shipping Address *</Label>
                <Input
                  id="shippingAddress"
                  value={formData.shippingAddress}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      shippingAddress: e.target.value,
                    })
                  }
                  required
                  placeholder="Full shipping address"
                  className="mt-1"
                />
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
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
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
