"use client"

import { BarChart3 } from "lucide-react"

interface ChartPlaceholder {
  title: string
  subtitle: string
}

const charts: ChartPlaceholder[] = [
  { title: "Delivery performance over time", subtitle: "Chart visualization" },
  { title: "Carrier performance comparison", subtitle: "Chart visualization" },
  { title: "Carrier Rating Over Time", subtitle: "Chart visualization" },
  { title: "Shipment status distribution", subtitle: "Pie chart" },
  { title: "Delivery across performances", subtitle: "Heat map" },
  { title: "Shipment status distribution", subtitle: "Horizontal bars" },
]

export function PerformanceCharts() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {charts.map((chart, index) => (
        <div
          key={index}
          className="bg-white border rounded-lg p-6 shadow-sm h-64 flex items-center justify-center"
        >
          <div className="text-center text-gray-500">
            <BarChart3 className="h-16 w-16 mx-auto mb-2 text-gray-300" />
            <p className="text-sm">{chart.title}</p>
            <p className="text-xs text-gray-400 mt-1">{chart.subtitle}</p>
          </div>
        </div>
      ))}
    </div>
  )
}

