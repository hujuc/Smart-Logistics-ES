"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Search, MapPin, Navigation, Package, Clock } from "lucide-react"
import Link from "next/link"

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
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex gap-4">
          <div className="flex-1">
            <Label htmlFor="search">Track Package</Label>
            <div className="relative mt-2">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                id="search"
                type="text"
                placeholder="Enter tracking number..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <div className="flex items-end">
            <Button>
              <Navigation className="mr-2 h-4 w-4" />
              Track
            </Button>
          </div>
        </div>
      </div>

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
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-xl font-semibold text-black mb-4">
          Active Deliveries ({filteredTracking.length})
        </h2>
        <div className="space-y-4">
          {filteredTracking.map((item) => (
            <div
              key={item.id}
              className="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-start gap-4">
                  <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Package className="h-6 w-6 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-semibold text-lg text-black">
                      {item.trackingNumber}
                    </p>
                    <p className="text-sm text-gray-600">
                      Driver: {item.driver} ({item.vehicle})
                    </p>
                  </div>
                </div>
                <span
                  className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${
                    item.status === "In Transit"
                      ? "bg-blue-100 text-blue-700"
                      : "bg-green-100 text-green-700"
                  }`}
                >
                  {item.status}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div className="flex items-start gap-3">
                  <MapPin className="h-5 w-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Current Location</p>
                    <p className="text-sm font-medium text-black">
                      {item.currentLocation}
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <Navigation className="h-5 w-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Destination</p>
                    <p className="text-sm font-medium text-black">
                      {item.destination}
                    </p>
                  </div>
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Delivery Progress</span>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-gray-400" />
                    <span className="font-medium text-black">
                      ETA: {item.estimatedArrival}
                    </span>
                  </div>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${item.progress}%` }}
                  ></div>
                </div>
                <p className="text-xs text-gray-500">{item.progress}% Complete</p>
              </div>

              <div className="mt-4 flex gap-2">
                <Button variant="outline" size="sm">
                  View Details
                </Button>
                <Button variant="outline" size="sm">
                  Contact Driver
                </Button>
              </div>
            </div>
          ))}

          {filteredTracking.length === 0 && (
            <div className="text-center py-12 text-gray-500">
              <Package className="h-16 w-16 mx-auto mb-4 text-gray-300" />
              <p>No active deliveries found</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
