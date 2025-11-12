"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { X } from "lucide-react"
import { Warehouse, WarehouseFormData } from "@/types/warehouse"

interface WarehouseFormProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (data: WarehouseFormData) => Promise<void>
  warehouse?: Warehouse | null
  mode: "create" | "edit"
}

export function WarehouseForm({
  open,
  onOpenChange,
  onSubmit,
  warehouse,
  mode,
}: WarehouseFormProps) {
  const [formData, setFormData] = useState<WarehouseFormData>({
    code: "",
    name: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    stateOrProvince: "",
    country: "",
    postalCode: "",
    active: true,
    shippoLocationToken: "",
    shipstationWarehouseId: "",
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (mode === "edit" && warehouse) {
      setFormData({
        code: warehouse.code,
        name: warehouse.name,
        addressLine1: warehouse.addressLine1,
        addressLine2: warehouse.addressLine2 || "",
        city: warehouse.city,
        stateOrProvince: warehouse.stateOrProvince,
        country: warehouse.country,
        postalCode: warehouse.postalCode,
        active: warehouse.active,
        shippoLocationToken: warehouse.shippoLocationToken || "",
        shipstationWarehouseId: warehouse.shipstationWarehouseId || "",
      })
    } else if (mode === "create") {
      setFormData({
        code: "",
        name: "",
        addressLine1: "",
        addressLine2: "",
        city: "",
        stateOrProvince: "",
        country: "",
        postalCode: "",
        active: true,
        shippoLocationToken: "",
        shipstationWarehouseId: "",
      })
    }
    setError(null)
  }, [warehouse, mode, open])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)

    try {
      await onSubmit(formData)
      onOpenChange(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 z-0"
        onClick={() => onOpenChange(false)}
      />

      {/* Dialog */}
      <div className="relative z-50 bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h2 className="text-xl font-semibold text-black">
              {mode === "create" ? "Create New Warehouse" : "Edit Warehouse"}
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              {mode === "create"
                ? "Add a new warehouse location to the system."
                : "Update warehouse information."}
            </p>
          </div>
          <button
            onClick={() => onOpenChange(false)}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Code */}
            <div className="space-y-2">
              <Label htmlFor="code">Warehouse Code *</Label>
              <Input
                id="code"
                value={formData.code}
                onChange={(e) =>
                  setFormData({ ...formData, code: e.target.value })
                }
                required
                maxLength={64}
                placeholder="e.g., WH-001"
              />
            </div>

            {/* Name */}
            <div className="space-y-2">
              <Label htmlFor="name">Warehouse Name *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                required
                placeholder="e.g., Main Distribution Center"
              />
            </div>
          </div>

          {/* Address Line 1 */}
          <div className="space-y-2">
            <Label htmlFor="addressLine1">Address Line 1 *</Label>
            <Input
              id="addressLine1"
              value={formData.addressLine1}
              onChange={(e) =>
                setFormData({ ...formData, addressLine1: e.target.value })
              }
              required
              placeholder="Street address"
            />
          </div>

          {/* Address Line 2 */}
          <div className="space-y-2">
            <Label htmlFor="addressLine2">Address Line 2</Label>
            <Input
              id="addressLine2"
              value={formData.addressLine2}
              onChange={(e) =>
                setFormData({ ...formData, addressLine2: e.target.value })
              }
              placeholder="Apartment, suite, unit, building, floor, etc."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* City */}
            <div className="space-y-2">
              <Label htmlFor="city">City *</Label>
              <Input
                id="city"
                value={formData.city}
                onChange={(e) =>
                  setFormData({ ...formData, city: e.target.value })
                }
                required
              />
            </div>

            {/* State/Province */}
            <div className="space-y-2">
              <Label htmlFor="stateOrProvince">State/Province *</Label>
              <Input
                id="stateOrProvince"
                value={formData.stateOrProvince}
                onChange={(e) =>
                  setFormData({ ...formData, stateOrProvince: e.target.value })
                }
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Postal Code */}
            <div className="space-y-2">
              <Label htmlFor="postalCode">Postal Code *</Label>
              <Input
                id="postalCode"
                value={formData.postalCode}
                onChange={(e) =>
                  setFormData({ ...formData, postalCode: e.target.value })
                }
                required
              />
            </div>

            {/* Country */}
            <div className="space-y-2">
              <Label htmlFor="country">Country *</Label>
              <Input
                id="country"
                value={formData.country}
                onChange={(e) =>
                  setFormData({ ...formData, country: e.target.value })
                }
                required
              />
            </div>
          </div>

          {/* Active Status */}
          <div className="space-y-2">
            <Label htmlFor="active">Status</Label>
            <select
              id="active"
              value={formData.active ? "true" : "false"}
              onChange={(e) =>
                setFormData({ ...formData, active: e.target.value === "true" })
              }
              className="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
            >
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>

          {/* Integration Tokens */}
          <div className="border-t pt-4">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">
              Integration Settings (Optional)
            </h3>

            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="shippoLocationToken">Shippo Location Token</Label>
                <Input
                  id="shippoLocationToken"
                  value={formData.shippoLocationToken}
                  onChange={(e) =>
                    setFormData({ ...formData, shippoLocationToken: e.target.value })
                  }
                  placeholder="Shippo location token"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="shipstationWarehouseId">ShipStation Warehouse ID</Label>
                <Input
                  id="shipstationWarehouseId"
                  value={formData.shipstationWarehouseId}
                  onChange={(e) =>
                    setFormData({ ...formData, shipstationWarehouseId: e.target.value })
                  }
                  placeholder="ShipStation warehouse ID"
                />
              </div>
            </div>
          </div>

          {error && (
            <div className="text-sm text-red-600 bg-red-50 p-3 rounded">
              {error}
            </div>
          )}

          {/* Footer */}
          <div className="flex gap-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting} className="flex-1">
              {isSubmitting
                ? "Saving..."
                : mode === "create"
                ? "Create Warehouse"
                : "Update Warehouse"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
