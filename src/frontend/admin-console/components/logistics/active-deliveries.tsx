"use client"

import { Button } from "@/components/ui/button"
import { Package, MapPin, Navigation, Clock } from "lucide-react"
import { STATUS_LABELS, STATUS_COLORS } from "@/types"
import { ActiveDeliveriesProps } from "@/types/common"

export function ActiveDeliveries({
  deliveries,
  onViewDetails,
  onContactDriver,
}: ActiveDeliveriesProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
      <h2 className="text-xl font-semibold text-black mb-4">
        Active Deliveries ({deliveries.length})
      </h2>
      <div className="space-y-4">
        {deliveries.map((item) => (
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
                className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-medium ${STATUS_COLORS[item.status]}`}
              >
                {STATUS_LABELS[item.status]}
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
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewDetails?.(item.id)}
              >
                View Details
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => onContactDriver?.(item.id)}
              >
                Contact Driver
              </Button>
            </div>
          </div>
        ))}

        {deliveries.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            <Package className="h-16 w-16 mx-auto mb-4 text-gray-300" />
            <p>No active deliveries found</p>
          </div>
        )}
      </div>
    </div>
  )
}

