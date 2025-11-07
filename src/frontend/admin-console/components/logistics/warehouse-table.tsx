"use client"

import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { MapPin, Edit, Trash2, CheckCircle, XCircle, Eye } from "lucide-react"
import { Warehouse } from "@/types/warehouse"
import Link from "next/link"

interface WarehouseTableProps {
  warehouses: Warehouse[]
  onEditWarehouse?: (warehouse: Warehouse) => void
  onDeleteWarehouse?: (warehouseId: number) => void
  onToggleActive?: (warehouseId: number, currentStatus: boolean) => void
}

export function WarehouseTable({
  warehouses,
  onEditWarehouse,
  onDeleteWarehouse,
  onToggleActive,
}: WarehouseTableProps) {
  const formatAddress = (warehouse: Warehouse) => {
    const parts = [
      warehouse.addressLine1,
      warehouse.city,
      warehouse.stateOrProvince,
      warehouse.postalCode,
      warehouse.country,
    ].filter(Boolean)
    return parts.join(", ")
  }

  return (
    <div className="bg-white border rounded-lg shadow-sm">
      <div className="p-6">
        <h2 className="text-xl font-semibold text-black mb-4">
          Warehouses ({warehouses.length})
        </h2>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Location</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Integrations</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {warehouses.map((warehouse) => (
              <TableRow key={warehouse.id}>
                <TableCell className="font-medium">
                  <Link
                    href={`/interfaces/logistics/warehouses/${warehouse.id}`}
                    className="text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    {warehouse.code}
                  </Link>
                </TableCell>
                <TableCell>
                  <Link
                    href={`/interfaces/logistics/warehouses/${warehouse.id}`}
                    className="text-blue-600 hover:text-blue-800 hover:underline font-medium"
                  >
                    {warehouse.name}
                  </Link>
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-1 text-sm text-gray-600">
                    <MapPin className="h-3 w-3" />
                    <span className="max-w-xs truncate" title={formatAddress(warehouse)}>
                      {formatAddress(warehouse)}
                    </span>
                  </div>
                </TableCell>
                <TableCell>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                      warehouse.active
                        ? "bg-green-100 text-green-700"
                        : "bg-gray-100 text-gray-700"
                    }`}
                  >
                    {warehouse.active ? (
                      <>
                        <CheckCircle className="h-3 w-3 mr-1" />
                        Active
                      </>
                    ) : (
                      <>
                        <XCircle className="h-3 w-3 mr-1" />
                        Inactive
                      </>
                    )}
                  </span>
                </TableCell>
                <TableCell>
                  <div className="flex gap-1 text-xs">
                    {warehouse.shippoLocationToken && (
                      <span className="bg-blue-100 text-blue-700 px-2 py-1 rounded">
                        Shippo
                      </span>
                    )}
                    {warehouse.shipstationWarehouseId && (
                      <span className="bg-purple-100 text-purple-700 px-2 py-1 rounded">
                        ShipStation
                      </span>
                    )}
                    {!warehouse.shippoLocationToken && !warehouse.shipstationWarehouseId && (
                      <span className="text-gray-500">None</span>
                    )}
                  </div>
                </TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    <Link href={`/interfaces/logistics/warehouses/${warehouse.id}`}>
                      <Button
                        variant="ghost"
                        size="sm"
                        title="View details"
                      >
                        <Eye className="h-4 w-4" />
                      </Button>
                    </Link>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onEditWarehouse?.(warehouse)}
                      title="Edit warehouse"
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onToggleActive?.(warehouse.id, warehouse.active)}
                      title={warehouse.active ? "Deactivate" : "Activate"}
                    >
                      {warehouse.active ? (
                        <XCircle className="h-4 w-4 text-orange-600" />
                      ) : (
                        <CheckCircle className="h-4 w-4 text-green-600" />
                      )}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDeleteWarehouse?.(warehouse.id)}
                      title="Delete warehouse"
                    >
                      <Trash2 className="h-4 w-4 text-red-600" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {warehouses.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No warehouses found. Create one to get started.
          </div>
        )}
      </div>
    </div>
  )
}
