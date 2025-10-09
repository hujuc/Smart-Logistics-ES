"use client"

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Eye } from "lucide-react"
import { WarehousePackage, STATUS_COLORS } from "@/types"

interface OrderTableProps {
  packages: WarehousePackage[]
  onViewPackage?: (id: string) => void
}

export function OrderTable({ packages, onViewPackage }: OrderTableProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
      <div className="p-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Reference</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Priority</TableHead>
              <TableHead>Origin</TableHead>
              <TableHead>Destination</TableHead>
              <TableHead>Scanned At</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {packages.map((pkg) => (
              <TableRow key={pkg.id}>
                <TableCell className="font-medium">{pkg.reference}</TableCell>
                <TableCell>
                  <span className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${STATUS_COLORS[pkg.status]}`}>
                    {pkg.status}
                  </span>
                </TableCell>
                <TableCell>
                  <span className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                    pkg.priority === "High" ? "bg-red-100 text-red-700" :
                    pkg.priority === "Medium" ? "bg-yellow-100 text-yellow-700" :
                    "bg-blue-100 text-blue-700"
                  }`}>
                    {pkg.priority}
                  </span>
                </TableCell>
                <TableCell>{pkg.origin}</TableCell>
                <TableCell>{pkg.destination}</TableCell>
                <TableCell>{pkg.scannedAt || "-"}</TableCell>
                <TableCell>
                  <Button variant="ghost" size="sm" onClick={() => onViewPackage?.(pkg.id)}>
                    <Eye className="h-4 w-4" />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}

