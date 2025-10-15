"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import { QuickStats } from "@/components/warehouse/quick-stats"
import { OrderTable } from "@/components/warehouse/order-table"
import { WarehousePackage } from "@/types"

// Mock data for packages
const mockPackages: WarehousePackage[] = [
  {
    id: "PKG001",
    reference: "ORD-2024-001",
    status: "Pending Labels",
    priority: "High",
    origin: "Porto",
    destination: "Lisboa",
    scannedAt: "2025-10-09 10:30",
    weight: 2.5,
  },
  {
    id: "PKG002",
    reference: "ORD-2024-002",
    status: "Ready for Dispatch",
    priority: "Medium",
    origin: "Braga",
    destination: "Coimbra",
    scannedAt: "2025-10-09 09:15",
    weight: 1.8,
  },
  {
    id: "PKG003",
    reference: "ORD-2024-003",
    status: "In Transit",
    priority: "Low",
    origin: "Aveiro",
    destination: "Faro",
    scannedAt: "2025-10-08 14:20",
    weight: 3.2,
  },
]

export default function WarehousePage() {
  const [packages] = useState(mockPackages)

  const stats = {
    totalPackages: packages.length,
    readyForDispatch: packages.filter(p => p.status === "Ready for Dispatch").length,
    pendingLabels: packages.filter(p => p.status === "Pending Labels").length,
    inTransit: packages.filter(p => p.status === "In Transit").length,
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Order Processing Dashboard</h1>
          <p className="text-gray-600 mt-1">Manage incoming orders and prepare for dispatch</p>
        </div>
        <Link href="/interfaces/warehouse/new-package">
          <Button>Add New Package</Button>
        </Link>
      </div>

      {/* Quick Stats */}
      <QuickStats stats={stats} />

      {/* Orders Table */}
      <OrderTable 
        packages={packages}
        onViewPackage={(id) => window.location.href = `/interfaces/warehouse/package/${id}`}
      />
    </div>
  )
}

