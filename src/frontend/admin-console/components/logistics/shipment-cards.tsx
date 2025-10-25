"use client"

import { Button } from "@/components/ui/button"
import { Plus, Check } from "lucide-react"
import { ShipmentCard } from "@/types"

interface ShipmentCardsProps {
  shipments: ShipmentCard[]
  selectedShipments?: string[]
  onAddShipment?: (shipmentId: string) => void
}

export function ShipmentCards({ shipments, selectedShipments = [], onAddShipment }: ShipmentCardsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {shipments.map((shipment) => {
        const isSelected = selectedShipments.includes(shipment.id)
        return (
          <div
            key={shipment.id}
            className={`bg-white border-2 ${isSelected ? 'border-brand-purple bg-purple-50' : 'border'} rounded-lg p-4 shadow-sm hover:shadow-md transition-all cursor-pointer`}
            onClick={() => onAddShipment?.(shipment.id)}
          >
            <div className="flex justify-between items-start mb-3">
              <div>
                <p className="font-medium text-sm text-gray-600">ID: {shipment.id}</p>
              </div>
              <Button
                variant={isSelected ? "default" : "ghost"}
                size="sm"
                className={`h-8 w-8 p-0 ${isSelected ? 'bg-brand-purple hover:bg-brand-purple-hover' : ''}`}
                onClick={(e) => {
                  e.stopPropagation()
                  onAddShipment?.(shipment.id)
                }}
              >
                {isSelected ? <Check className="h-4 w-4" /> : <Plus className="h-4 w-4" />}
              </Button>
            </div>

          <div className="space-y-2">
            <div>
              <p className="text-sm text-gray-600">Weight: {shipment.weight}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Destination: {shipment.destination}</p>
            </div>
            <div className="flex items-center gap-2">
              <div
                className={`h-4 w-4 rounded-full ${
                  shipment.priority === "High"
                    ? "bg-red-100"
                    : shipment.priority === "Medium"
                    ? "bg-yellow-100"
                    : "bg-green-100"
                }`}
              >
                <div
                  className={`h-2 w-2 rounded-full mx-auto mt-1 ${
                    shipment.priority === "High"
                      ? "bg-red-500"
                      : shipment.priority === "Medium"
                      ? "bg-yellow-500"
                      : "bg-green-500"
                  }`}
                ></div>
              </div>
              <span className="text-sm text-gray-600">{shipment.priority} Priority</span>
            </div>
            <div>
              <p className="text-sm text-gray-600">Date of Order: {shipment.date}</p>
            </div>
          </div>
        </div>
      )})}
    </div>
  )
}

