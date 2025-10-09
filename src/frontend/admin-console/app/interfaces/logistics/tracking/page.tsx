"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { MapPin } from "lucide-react"
import Link from "next/link"
import { TrackPackageSearch } from "@/components/logistics/track-package-search"
import { ActiveDeliveries } from "@/components/logistics/active-deliveries"

// Mock data for real-time tracking
const mockTrackingData = [
  {
    id: "TRK001",
    trackingNumber: "TRK123456789",
    currentLocation: "Distribution Center A",
    destination: "Customer Location - 123 Main St",
    estimatedArrival: "2:30 PM",
    status: "In Transit",
    coordinates: { lat: 40.7128, lng: -74.0060 },
    driver: "Mike Johnson",
    vehicle: "VAN-123",
    progress: 65,
  },
  {
    id: "TRK002",
    trackingNumber: "TRK987654321",
    currentLocation: "Warehouse B",
    destination: "Customer Location - 456 Oak Ave",
    estimatedArrival: "4:15 PM",
    status: "Out for Delivery",
    coordinates: { lat: 40.7580, lng: -73.9855 },
    driver: "Sarah Williams",
    vehicle: "TRK-456",
    progress: 85,
  },
]

export default function TrackingPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [trackingData] = useState(mockTrackingData)

  const filteredTracking = trackingData.filter(
    (item) =>
      item.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.driver.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Real-Time Tracking</h1>
        <Link href="/interfaces/logistics">
          <Button variant="outline">Back to Routes</Button>
        </Link>
      </div>

      {/* Search Bar */}
      <TrackPackageSearch
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        onTrack={() => alert("Tracking package...")}
      />

      {/* Map Placeholder */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <div className="relative h-96 bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center">
          <div className="text-center">
            <MapPin className="h-16 w-16 mx-auto mb-4 text-blue-600" />
            <p className="text-gray-600 font-medium">Interactive Map View</p>
            <p className="text-sm text-gray-500 mt-2">
              Real-time package locations would be displayed here
            </p>
          </div>
          {/* In a real app, integrate Google Maps or similar */}
        </div>
      </div>

      {/* Active Deliveries */}
      <ActiveDeliveries
        deliveries={filteredTracking}
        onViewDetails={(id) => alert(`Viewing details for ${id}`)}
        onContactDriver={(id) => alert(`Contacting driver for ${id}`)}
      />
    </div>
  )
}
