"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Warehouse as WarehouseIcon, CheckCircle2, AlertCircle } from "lucide-react"
import { WarehouseTable } from "@/components/logistics/warehouse-table"
import { WarehouseForm } from "@/components/logistics/warehouse-form"
import { WarehouseDeleteDialog } from "@/components/logistics/warehouse-delete-dialog"
import { warehouseManagementService } from "@/lib/services/warehouse-management.service"
import type { Warehouse, WarehouseFormData } from "@/types/warehouse"

export default function WarehousesPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [formMode, setFormMode] = useState<"create" | "edit">("create")
  const [selectedWarehouse, setSelectedWarehouse] = useState<Warehouse | null>(null)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [warehouseToDelete, setWarehouseToDelete] = useState<Warehouse | null>(null)

  // Load warehouses on mount
  useEffect(() => {
    loadWarehouses()
  }, [])

  const loadWarehouses = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await warehouseManagementService.getAll()
      setWarehouses(data)
    } catch (error) {
      console.error("Failed to load warehouses:", error)
      setError("Failed to load warehouses. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const showSuccess = (message: string) => {
    setSuccessMessage(message)
    setTimeout(() => setSuccessMessage(null), 3000)
  }

  const showError = (message: string) => {
    setError(message)
    setTimeout(() => setError(null), 5000)
  }

  const handleCreateWarehouse = () => {
    setFormMode("create")
    setSelectedWarehouse(null)
    setFormOpen(true)
  }

  const handleEditWarehouse = (warehouse: Warehouse) => {
    setFormMode("edit")
    setSelectedWarehouse(warehouse)
    setFormOpen(true)
  }

  const handleDeleteWarehouse = (warehouseId: number) => {
    const warehouse = warehouses.find((wh) => wh.id === warehouseId)
    if (warehouse) {
      setWarehouseToDelete(warehouse)
      setDeleteDialogOpen(true)
    }
  }

  const handleConfirmDelete = async () => {
    if (!warehouseToDelete) return

    try {
      await warehouseManagementService.delete(warehouseToDelete.id)
      showSuccess("Warehouse deleted successfully.")
      loadWarehouses()
    } catch (error) {
      console.error("Failed to delete warehouse:", error)
      throw error
    }
  }

  const handleToggleActive = async (warehouseId: number, currentStatus: boolean) => {
    try {
      await warehouseManagementService.toggleActive(warehouseId, !currentStatus)
      showSuccess(
        `Warehouse ${!currentStatus ? "activated" : "deactivated"} successfully.`
      )
      loadWarehouses()
    } catch (error) {
      console.error("Failed to toggle warehouse status:", error)
      showError("Failed to update warehouse status. Please try again.")
    }
  }

  const handleFormSubmit = async (data: WarehouseFormData) => {
    try {
      if (formMode === "create") {
        await warehouseManagementService.create(data)
        showSuccess("Warehouse created successfully.")
      } else if (selectedWarehouse) {
        await warehouseManagementService.update(selectedWarehouse.id, data)
        showSuccess("Warehouse updated successfully.")
      }
      loadWarehouses()
    } catch (error) {
      console.error("Failed to save warehouse:", error)
      throw error
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-gray-500">Loading warehouses...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Warehouse Management</h1>
          <p className="text-gray-600 mt-1">Manage warehouse locations and facilities</p>
        </div>
        <Button onClick={handleCreateWarehouse}>
          <WarehouseIcon className="mr-2 h-4 w-4" />
          Add New Warehouse
        </Button>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg flex items-center gap-2">
          <CheckCircle2 className="h-5 w-5" />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg flex items-center gap-2">
          <AlertCircle className="h-5 w-5" />
          <span>{error}</span>
        </div>
      )}

      {/* Warehouses Table */}
      <WarehouseTable
        warehouses={warehouses}
        onEditWarehouse={handleEditWarehouse}
        onDeleteWarehouse={handleDeleteWarehouse}
        onToggleActive={handleToggleActive}
      />

      {/* Warehouse Form */}
      <WarehouseForm
        open={formOpen}
        onOpenChange={setFormOpen}
        onSubmit={handleFormSubmit}
        warehouse={selectedWarehouse}
        mode={formMode}
      />

      {/* Delete Confirmation Dialog */}
      <WarehouseDeleteDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        onConfirm={handleConfirmDelete}
        warehouse={warehouseToDelete}
      />
    </div>
  )
}
