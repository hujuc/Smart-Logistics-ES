"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

export default function PackageDetailsPage() {
  const [isEditing, setIsEditing] = useState(false)

  // Mock data - in real app, this would be fetched based on params.id
  const [packageData, setPackageData] = useState({
    trackingNumber: "TRK123456789",
    sender: "John Doe",
    senderAddress: "123 Main St, City, State 12345",
    recipient: "Jane Smith",
    recipientAddress: "456 Oak Ave, Town, State 67890",
    status: "In Warehouse",
    location: "Shelf A-12",
    weight: "2.5",
    dimensions: "30x20x15 cm",
    notes: "Handle with care - fragile items",
  })

  const handleSave = () => {
    // Handle save logic here
    setIsEditing(false)
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

      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-black">
            {packageData.trackingNumber}
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
              Sender Information
            </h3>
            <div>
              <Label htmlFor="sender">Sender Name</Label>
              <Input
                id="sender"
                value={packageData.sender}
                onChange={(e) =>
                  setPackageData({ ...packageData, sender: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="senderAddress">Sender Address</Label>
              <Input
                id="senderAddress"
                value={packageData.senderAddress}
                onChange={(e) =>
                  setPackageData({
                    ...packageData,
                    senderAddress: e.target.value,
                  })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Recipient Information
            </h3>
            <div>
              <Label htmlFor="recipient">Recipient Name</Label>
              <Input
                id="recipient"
                value={packageData.recipient}
                onChange={(e) =>
                  setPackageData({ ...packageData, recipient: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="recipientAddress">Recipient Address</Label>
              <Input
                id="recipientAddress"
                value={packageData.recipientAddress}
                onChange={(e) =>
                  setPackageData({
                    ...packageData,
                    recipientAddress: e.target.value,
                  })
                }
                disabled={!isEditing}
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
              <Input
                id="status"
                value={packageData.status}
                onChange={(e) =>
                  setPackageData({ ...packageData, status: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="location">Warehouse Location</Label>
              <Input
                id="location"
                value={packageData.location}
                onChange={(e) =>
                  setPackageData({ ...packageData, location: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="font-semibold text-black border-b pb-2">
              Measurements
            </h3>
            <div>
              <Label htmlFor="weight">Weight (kg)</Label>
              <Input
                id="weight"
                type="number"
                step="0.1"
                value={packageData.weight}
                onChange={(e) =>
                  setPackageData({ ...packageData, weight: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="dimensions">Dimensions</Label>
              <Input
                id="dimensions"
                value={packageData.dimensions}
                onChange={(e) =>
                  setPackageData({
                    ...packageData,
                    dimensions: e.target.value,
                  })
                }
                disabled={!isEditing}
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
                value={packageData.notes}
                onChange={(e) =>
                  setPackageData({ ...packageData, notes: e.target.value })
                }
                disabled={!isEditing}
                className="mt-1 flex min-h-[80px] w-full rounded-md border border-gray-200 bg-white px-3 py-2 text-base text-black shadow-sm placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <h3 className="font-semibold text-black mb-4">Package History</h3>
        <div className="space-y-3">
          <div className="flex gap-4 pb-3 border-b">
            <div className="text-sm text-gray-500 w-32">2024-10-02 10:30</div>
            <div className="text-sm text-black">Package received at warehouse</div>
          </div>
          <div className="flex gap-4 pb-3 border-b">
            <div className="text-sm text-gray-500 w-32">2024-10-01 16:45</div>
            <div className="text-sm text-black">In transit to warehouse</div>
          </div>
          <div className="flex gap-4 pb-3">
            <div className="text-sm text-gray-500 w-32">2024-10-01 09:00</div>
            <div className="text-sm text-black">Package picked up from sender</div>
          </div>
        </div>
      </div>
    </div>
  )
}
