"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Download, BarChart3, Package, Truck, Clock, CheckCircle } from "lucide-react"
import { LogisticsKPIs } from "@/types"
import { KPICard } from "@/components/logistics/kpi-cards"
import { PerformanceCharts } from "@/components/logistics/performance-charts"
import { KPIChartsNav } from "@/components/logistics/kpi-charts-nav"

// Mock data for KPIs (formerly Quick Stats)
const quickStats: LogisticsKPIs = {
  ordersToday: { value: 186, trend: -16, label: "vs last yesterday" },
  pendingPickups: { value: 23, alert: "Active Overdue", hasWarning: true },
  inTransitNow: { value: 15 },
  deliveredToday: { value: 103, trend: -16, label: "vs from yesterday" }
}

// KPI configuration for dynamic rendering
const kpiConfig = [
  { key: "ordersToday", title: "Orders Today", icon: Package, color: "bg-blue-100" },
  { key: "pendingPickups", title: "Pending Pickups", icon: Truck, color: "bg-green-100" },
  { key: "inTransitNow", title: "In Transit Now", icon: Clock, color: "bg-yellow-100" },
  { key: "deliveredToday", title: "Delivered Today", icon: CheckCircle, color: "bg-purple-100" },
] as const

export default function LogisticsPage() {
  const [activeTab, setActiveTab] = useState<"kpis" | "charts">("kpis")

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col lg:flex-row gap-4 lg:justify-between lg:items-center">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-black">Logistics Operations Dashboard</h1>
          <p className="text-sm md:text-base text-gray-600 mt-1">Monitor performance, costs, and carrier metrics</p>
        </div>
        <div className="flex flex-col sm:flex-row gap-2 sm:gap-2">
          <Button variant="outline" onClick={() => alert("Opening Analytics...")} className="w-full sm:w-auto">
            <BarChart3 className="mr-2 h-4 w-4" />
            Analytics
          </Button>
          <Button variant="outline" onClick={() => alert("Exporting report...")} className="w-full sm:w-auto">
            <Download className="mr-2 h-4 w-4" />
            Export Report
          </Button>
        </div>
      </div>

      <KPIChartsNav activeTab={activeTab} onTabChange={setActiveTab} />

      {/* KPIs View - Quick Stats */}
      {activeTab === "kpis" && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {kpiConfig.map(({ key, title, icon, color }) => (
            <KPICard
              key={key}
              title={title}
              icon={icon}
              stat={quickStats[key]}
              color={color}
            />
          ))}
        </div>
      )}

      {/* Performance Charts View */}
      {activeTab === "charts" && <PerformanceCharts />}

    </div>
  )
}

