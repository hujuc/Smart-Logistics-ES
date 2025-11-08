"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Download, BarChart3, Package, Truck, Clock, CheckCircle, DollarSign, Timer, AlertTriangle, MessageSquare, Warehouse, Users } from "lucide-react"
import { KPICard } from "@/components/logistics/kpi-cards"
import { PerformanceCharts } from "@/components/logistics/performance-charts"
import { KPIChartsNav } from "@/components/logistics/kpi-charts-nav"
import { logisticsService, LogisticsStats, TrendPeriod, CarrierBreakdown, WarehouseBreakdown, StatusBreakdown } from "@/lib/services/logistics.service"
import Masonry from "react-masonry-css"

export default function LogisticsPage() {
  const [activeTab, setActiveTab] = useState<"kpis" | "charts">("kpis")
  const [period, setPeriod] = useState<TrendPeriod>("daily")
  const [stats, setStats] = useState<LogisticsStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Fetch statistics from backend
  useEffect(() => {
    async function fetchStats() {
      try {
        setLoading(true)
        setError(null)
        const data = await logisticsService.getStats(period)
        setStats(data)
      } catch (err) {
        console.error('Failed to fetch logistics statistics:', err)
        setError(err instanceof Error ? err.message : 'Failed to load statistics')
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [period])

  // Helper function to render breakdown sections
  const renderCarrierBreakdown = (carriers: CarrierBreakdown[]) => (
    <div className="space-y-2">
      <h4 className="text-sm font-semibold text-gray-700 mb-2">By Carrier</h4>
      {carriers.map((carrier) => (
        <div key={carrier.carrierCode} className="flex justify-between items-center text-sm">
          <span className="text-gray-600">{carrier.carrierName}</span>
          <span className="font-medium">{carrier.count} ({carrier.percentage}%)</span>
        </div>
      ))}
    </div>
  )

  const renderStatusBreakdown = (status: StatusBreakdown | Record<string, number>) => {
    // Convert StatusBreakdown to Record if needed
    const statusRecord: Record<string, number> = 'created' in status
      ? {
          created: status.created,
          'ready for pickup': status.readyForPickup,
          collected: status.collected,
          'in transit': status.inTransit,
          delivered: status.delivered,
          cancelled: status.cancelled
        }
      : status

    return (
      <div className="space-y-2">
        <h4 className="text-sm font-semibold text-gray-700 mb-2">By Status</h4>
        <div className="grid grid-cols-2 gap-2">
          {Object.entries(statusRecord).map(([key, value]) => (
            <div key={key} className="flex justify-between text-sm">
              <span className="text-gray-600 capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
              <span className="font-medium">{value}</span>
            </div>
          ))}
        </div>
      </div>
    )
  }

  const renderWarehouseBreakdown = (warehouses: WarehouseBreakdown[]) => (
    <div className="space-y-2">
      <h4 className="text-sm font-semibold text-gray-700 mb-2">By Warehouse</h4>
      {warehouses.map((wh) => (
        <div key={wh.warehouseId} className="flex justify-between items-center text-sm">
          <span className="text-gray-600">{wh.warehouseName}</span>
          <span className="font-medium">{wh.count} ({wh.percentage}%)</span>
        </div>
      ))}
    </div>
  )

  const renderKeyValueList = (title: string, items: Record<string, string | number>) => (
    <div className="space-y-2">
      <h4 className="text-sm font-semibold text-gray-700 mb-2">{title}</h4>
      {Object.entries(items).map(([key, value]) => (
        <div key={key} className="flex justify-between text-sm">
          <span className="text-gray-600 capitalize">{key.replace(/_/g, ' ').toLowerCase()}</span>
          <span className="font-medium">{value}</span>
        </div>
      ))}
    </div>
  )

  // KPI Card Configuration - All cards in a single masonry layout
  const allKPIConfig = stats ? [
    {
      id: 'orders-today',
      title: 'Orders Today',
      icon: Package,
      stat: stats.ordersToday,
      color: 'bg-blue-100',
      breakdown: (
        <div className="space-y-4">
          {stats.ordersToday.byWarehouse.length > 0 && renderWarehouseBreakdown(stats.ordersToday.byWarehouse)}
          {renderStatusBreakdown(stats.ordersToday.byStatus)}
        </div>
      )
    },
    {
      id: 'pending-pickups',
      title: 'Pending Pickups',
      icon: Truck,
      stat: stats.pendingPickups,
      color: 'bg-green-100',
      breakdown: (
        <div className="space-y-4">
          {renderKeyValueList('Urgency', {
            Overdue: stats.pendingPickups.overdueBreakdown.overdue,
            'Due Soon': stats.pendingPickups.overdueBreakdown.dueSoon,
            'On Time': stats.pendingPickups.overdueBreakdown.onTime
          })}
          {stats.pendingPickups.byWarehouse.length > 0 && renderWarehouseBreakdown(stats.pendingPickups.byWarehouse)}
        </div>
      )
    },
    {
      id: 'in-transit',
      title: 'In Transit Now',
      icon: Clock,
      stat: stats.inTransitNow,
      color: 'bg-yellow-100',
      breakdown: (
        <div className="space-y-4">
          {stats.inTransitNow.byCarrier.length > 0 && renderCarrierBreakdown(stats.inTransitNow.byCarrier)}
          {renderKeyValueList('Delivery Schedule', {
            'Within 24h': stats.inTransitNow.byDeliveryTime.deliveringSoon,
            '1-3 Days': stats.inTransitNow.byDeliveryTime.delivering1to3Days,
            '3+ Days': stats.inTransitNow.byDeliveryTime.deliveringLater
          })}
        </div>
      )
    },
    {
      id: 'delivered-today',
      title: 'Delivered Today',
      icon: CheckCircle,
      stat: stats.deliveredToday,
      color: 'bg-purple-100',
      breakdown: (
        <div className="space-y-4">
          {renderKeyValueList('Performance', {
            [`On Time (${stats.deliveredToday.onTimeBreakdown.onTimePercentage}%)`]: stats.deliveredToday.onTimeBreakdown.onTime,
            'Delayed': stats.deliveredToday.onTimeBreakdown.delayed
          })}
          {stats.deliveredToday.byCarrier.length > 0 && renderCarrierBreakdown(stats.deliveredToday.byCarrier)}
        </div>
      )
    },
    {
      id: 'total-shipments',
      title: 'Total Shipments',
      icon: Package,
      stat: stats.totalShipments,
      color: 'bg-indigo-100',
      breakdown: (
        <div className="space-y-4">
          {renderStatusBreakdown(stats.totalShipments.byStatus)}
          {stats.totalShipments.byCarrier.length > 0 && renderCarrierBreakdown(stats.totalShipments.byCarrier)}
        </div>
      )
    },
    {
      id: 'avg-cost',
      title: 'Avg Cost/Shipment',
      icon: DollarSign,
      stat: { ...stats.averageCostPerShipment, value: `$${stats.averageCostPerShipment.value.toFixed(2)}` },
      color: 'bg-green-100',
      breakdown: (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700 mb-2">Cost Analysis</h4>
          <p className="text-xs text-gray-500">Detailed cost breakdown by carrier coming soon</p>
        </div>
      )
    },
    {
      id: 'total-costs',
      title: 'Total Costs',
      icon: DollarSign,
      stat: { ...stats.totalShipmentCosts, value: `$${stats.totalShipmentCosts.value.toFixed(2)}` },
      color: 'bg-emerald-100',
      breakdown: (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700 mb-2">Total Spending</h4>
          <p className="text-xs text-gray-500">Detailed cost breakdown by carrier coming soon</p>
        </div>
      )
    },
    {
      id: 'avg-delivery-time',
      title: 'Avg Delivery Time',
      icon: Timer,
      stat: { ...stats.averageDeliveryTime, value: `${stats.averageDeliveryTime.value} ${stats.averageDeliveryTime.unit}` },
      color: 'bg-cyan-100',
      breakdown: (
        <div className="space-y-2">
          {renderKeyValueList('Time Distribution', {
            '< 24h': stats.averageDeliveryTime.byTimeRange.lessThan24Hours,
            '1-3 Days': stats.averageDeliveryTime.byTimeRange.oneToThreeDays,
            '3-7 Days': stats.averageDeliveryTime.byTimeRange.threeTo7Days,
            '> 7 Days': stats.averageDeliveryTime.byTimeRange.moreThan7Days
          })}
        </div>
      )
    },
    {
      id: 'total-exceptions',
      title: 'Total Exceptions',
      icon: AlertTriangle,
      stat: stats.totalExceptions,
      color: 'bg-red-100',
      breakdown: (
        <div className="space-y-4">
          {renderKeyValueList('By Type', stats.totalExceptions.byType)}
          {renderKeyValueList('By Severity', stats.totalExceptions.bySeverity)}
        </div>
      )
    },
    {
      id: 'total-tickets',
      title: 'Total Tickets',
      icon: MessageSquare,
      stat: stats.totalTickets,
      color: 'bg-orange-100',
      breakdown: (
        <div className="space-y-4">
          {renderKeyValueList('By Status', stats.totalTickets.byStatus)}
          <div className="text-sm">
            <span className="text-gray-600">Avg Resolution: </span>
            <span className="font-medium">{stats.totalTickets.averageResolutionTime} {stats.totalTickets.resolutionTimeUnit}</span>
          </div>
        </div>
      )
    },
    {
      id: 'warehouses',
      title: 'Warehouses',
      icon: Warehouse,
      stat: stats.totalWarehouses,
      color: 'bg-teal-100',
      breakdown: (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700 mb-2">Warehouses</h4>
          {stats.totalWarehouses.warehouses.map((wh) => (
            <div key={wh.warehouseId} className="flex justify-between items-center text-sm">
              <span className="text-gray-600">{wh.warehouseName}</span>
              <span className="font-medium">{wh.count} orders</span>
            </div>
          ))}
        </div>
      )
    },
    {
      id: 'active-carriers',
      title: 'Active Carriers',
      icon: Users,
      stat: stats.activeDrivers,
      color: 'bg-pink-100',
      breakdown: (
        <div className="space-y-4">
          {renderKeyValueList('Overview', {
            'Total Carriers': stats.activeDrivers.total,
            'Inactive': stats.activeDrivers.inactive
          })}
          {stats.activeDrivers.byCarrier.length > 0 && (
            <div className="space-y-2">
              <h4 className="text-sm font-semibold text-gray-700 mb-2">Carriers</h4>
              {stats.activeDrivers.byCarrier.map((carrier) => (
                <div key={carrier.carrierCode} className="flex justify-between items-center text-sm">
                  <span className="text-gray-600">{carrier.carrierName}</span>
                  <span className={`font-medium ${carrier.count > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {carrier.count > 0 ? 'Active' : 'Inactive'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )
    }
  ] : []

  // Masonry breakpoint configuration for responsive columns
  const breakpointColumns = {
    default: 4,  // 4 columns on large screens
    1024: 2,     // 2 columns on tablets
    640: 1       // 1 column on mobile
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col lg:flex-row gap-4 lg:justify-between lg:items-center">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-black">Logistics Operations Dashboard</h1>
          <p className="text-sm md:text-base text-gray-600 mt-1">Monitor performance, costs, and carrier metrics</p>
        </div>
        <div className="flex flex-col sm:flex-row gap-2 sm:gap-2">
          <select
            value={period}
            onChange={(e) => setPeriod(e.target.value as TrendPeriod)}
            className="px-3 py-2 border rounded-md text-sm"
          >
            <option value="daily">Daily</option>
            <option value="weekly">Weekly</option>
            <option value="monthly">Monthly</option>
          </select>
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

      {loading && (
        <div className="text-center py-12">
          <p className="text-gray-600">Loading statistics...</p>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">Error: {error}</p>
        </div>
      )}

      {!loading && !error && stats && (
        <>
          <KPIChartsNav activeTab={activeTab} onTabChange={setActiveTab} />

          {/* KPIs View - All Stats */}
          {activeTab === "kpis" && (
            <Masonry
              breakpointCols={breakpointColumns}
              className="masonry-grid"
              columnClassName="masonry-grid-column"
            >
              {allKPIConfig.map((config) => (
                <KPICard
                  key={config.id}
                  title={config.title}
                  icon={config.icon}
                  stat={config.stat}
                  color={config.color}
                  expandable
                  breakdown={config.breakdown}
                />
              ))}
            </Masonry>
          )}

          {/* Performance Charts View */}
          {activeTab === "charts" && <PerformanceCharts />}
        </>
      )}
    </div>
  )
}

