"use client"

import { Package, CheckCircle, Clock, Truck } from "lucide-react"
import { WarehouseStats } from "@/types"

interface QuickStatsProps {
  stats: WarehouseStats
}

export function QuickStats({ stats }: QuickStatsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <Package className="h-8 w-8 text-blue-600" />
          <div>
            <p className="text-sm text-gray-600">Total Packages</p>
            <p className="text-3xl font-bold text-black">{stats.totalPackages}</p>
          </div>
        </div>
      </div>
      
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <CheckCircle className="h-8 w-8 text-green-600" />
          <div>
            <p className="text-sm text-gray-600">Ready for Dispatch</p>
            <p className="text-3xl font-bold text-black">{stats.readyForDispatch}</p>
          </div>
        </div>
      </div>
      
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <Clock className="h-8 w-8 text-yellow-600" />
          <div>
            <p className="text-sm text-gray-600">Pending Labels</p>
            <p className="text-3xl font-bold text-black">{stats.pendingLabels}</p>
          </div>
        </div>
      </div>
      
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <Truck className="h-8 w-8 text-purple-600" />
          <div>
            <p className="text-sm text-gray-600">In Transit</p>
            <p className="text-3xl font-bold text-black">{stats.inTransit}</p>
          </div>
        </div>
      </div>
    </div>
  )
}

