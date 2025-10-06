"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"

export default function NewPackagePage() {
  const router = useRouter()
  const [formData, setFormData] = useState({
    trackingNumber: "",
    sender: "",
    senderAddress: "",
    recipient: "",
    recipientAddress: "",
    location: "",
    weight: "",
    dimensions: "",
    notes: "",
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Handle form submission here
    console.log("Form submitted:", formData)
    router.push("/interfaces/warehouse")
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/interfaces/warehouse">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <h1 className="text-3xl font-bold text-black">Add New Package</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Sender Information
              </h3>
              <div>
                <Label htmlFor="sender">Sender Name *</Label>
                <Input
                  id="sender"
                  value={formData.sender}
                  onChange={(e) =>
                    setFormData({ ...formData, sender: e.target.value })
                  }
                  required
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="senderAddress">Sender Address *</Label>
                <Input
                  id="senderAddress"
                  value={formData.senderAddress}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      senderAddress: e.target.value,
                    })
                  }
                  required
                  className="mt-1"
                />
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Recipient Information
              </h3>
              <div>
                <Label htmlFor="recipient">Recipient Name *</Label>
                <Input
                  id="recipient"
                  value={formData.recipient}
                  onChange={(e) =>
                    setFormData({ ...formData, recipient: e.target.value })
                  }
                  required
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="recipientAddress">Recipient Address *</Label>
                <Input
                  id="recipientAddress"
                  value={formData.recipientAddress}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      recipientAddress: e.target.value,
                    })
                  }
                  required
                  className="mt-1"
                />
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Package Details
              </h3>
              <div>
                <Label htmlFor="trackingNumber">Tracking Number *</Label>
                <Input
                  id="trackingNumber"
                  value={formData.trackingNumber}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      trackingNumber: e.target.value,
                    })
                  }
                  required
                  placeholder="TRK..."
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="location">Warehouse Location *</Label>
                <Input
                  id="location"
                  value={formData.location}
                  onChange={(e) =>
                    setFormData({ ...formData, location: e.target.value })
                  }
                  required
                  placeholder="e.g., Shelf A-12"
                  className="mt-1"
                />
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Measurements
              </h3>
              <div>
                <Label htmlFor="weight">Weight (kg) *</Label>
                <Input
                  id="weight"
                  type="number"
                  step="0.1"
                  value={formData.weight}
                  onChange={(e) =>
                    setFormData({ ...formData, weight: e.target.value })
                  }
                  required
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="dimensions">Dimensions</Label>
                <Input
                  id="dimensions"
                  value={formData.dimensions}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      dimensions: e.target.value,
                    })
                  }
                  placeholder="e.g., 30x20x15 cm"
                  className="mt-1"
                />
              </div>
            </div>

            <div className="md:col-span-2 space-y-4">
              <h3 className="font-semibold text-black border-b pb-2">
                Additional Notes
              </h3>
              <div>
                <Label htmlFor="notes">Notes</Label>
                <textarea
                  id="notes"
                  value={formData.notes}
                  onChange={(e) =>
                    setFormData({ ...formData, notes: e.target.value })
                  }
                  className="mt-1 flex min-h-[80px] w-full rounded-md border border-gray-200 bg-white px-3 py-2 text-base text-black shadow-sm placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 md:text-sm"
                  placeholder="Any special handling instructions..."
                />
              </div>
            </div>
          </div>

          <div className="flex gap-4 mt-6 pt-6 border-t">
            <Button type="submit">Save Package</Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push("/interfaces/warehouse")}
            >
              Cancel
            </Button>
          </div>
        </div>
      </form>
    </div>
  )
}
