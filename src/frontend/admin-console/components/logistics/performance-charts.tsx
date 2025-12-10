"use client"

import { BarChart, Bar, PieChart, Pie, Cell, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import type { LogisticsStats } from '@/lib/services/logistics.service'

interface PerformanceChartsProps {
  stats: LogisticsStats
}

// Helper function to get time frame label
const getTimeFrameLabel = (period: string): string => {
  switch (period.toLowerCase()) {
    case 'daily':
      return 'today'
    case 'weekly':
      return 'this week'
    case 'monthly':
      return 'this month'
    default:
      return 'current period'
  }
}

const COLORS = {
  primary: '#2962FF',
  secondary: '#00BFA5',
  success: '#00C853',
  warning: '#FFB300',
  error: '#D32F2F',
  info: '#0288D1',
  purple: '#7B1FA2',
  orange: '#F57C00',
  teal: '#00897B',
  pink: '#C2185B',
  indigo: '#303F9F',
  gray: '#616161'
}

const STATUS_COLORS: Record<string, string> = {
  created: COLORS.info,
  'ready for pickup': COLORS.warning,
  readyForPickup: COLORS.warning,
  collected: COLORS.secondary,
  'in transit': COLORS.primary,
  inTransit: COLORS.primary,
  delivered: COLORS.success,
  cancelled: COLORS.error
}

export function PerformanceCharts({ stats }: PerformanceChartsProps) {
  const timeFrame = getTimeFrameLabel(stats.period)

  // Prepare data for Shipment Status Distribution (Pie Chart)
  const shipmentStatusData = [
    { name: 'Created', value: stats.totalShipments.byStatus.created },
    { name: 'Ready for Pickup', value: stats.totalShipments.byStatus.readyForPickup },
    { name: 'Collected', value: stats.totalShipments.byStatus.collected },
    { name: 'In Transit', value: stats.totalShipments.byStatus.inTransit },
    { name: 'Delivered', value: stats.totalShipments.byStatus.delivered },
    { name: 'Cancelled', value: stats.totalShipments.byStatus.cancelled }
  ].filter(item => item.value > 0)

  // Prepare data for Carrier Performance Comparison (Bar Chart)
  const carrierPerformanceData = stats.totalShipments.byCarrier.map(carrier => ({
    name: carrier.carrierName,
    shipments: carrier.count,
    percentage: carrier.percentage
  }))

  // Prepare data for Delivery Time Distribution (Bar Chart)
  const deliveryTimeData = [
    { range: '< 24h', count: stats.averageDeliveryTime.byTimeRange.lessThan24Hours },
    { range: '1-3 Days', count: stats.averageDeliveryTime.byTimeRange.oneToThreeDays },
    { range: '3-7 Days', count: stats.averageDeliveryTime.byTimeRange.threeTo7Days },
    { range: '> 7 Days', count: stats.averageDeliveryTime.byTimeRange.moreThan7Days }
  ]

  // Prepare data for On-Time Delivery Performance (Pie Chart)
  const onTimeDeliveryData = [
    { name: 'On Time', value: stats.deliveredToday.onTimeBreakdown.onTime },
    { name: 'Delayed', value: stats.deliveredToday.onTimeBreakdown.delayed }
  ].filter(item => item.value > 0)

  // Prepare data for Warehouse Orders Distribution (Bar Chart)
  const warehouseOrdersData = stats.ordersToday.byWarehouse.map(wh => ({
    name: wh.warehouseName,
    orders: wh.count,
    percentage: wh.percentage
  }))

  // Prepare data for Exception Analysis (Bar Chart)
  const exceptionTypeData = Object.entries(stats.totalExceptions.byType)
    .map(([type, count]) => ({
      type: type.replace(/_/g, ' ').toLowerCase(),
      count
    }))
    .filter(item => item.count > 0)

  // Prepare data for In Transit Status (Horizontal Bar Chart)
  const inTransitTimeData = [
    { label: 'Within 24h', count: stats.inTransitNow.byDeliveryTime.deliveringSoon },
    { label: '1-3 Days', count: stats.inTransitNow.byDeliveryTime.delivering1to3Days },
    { label: '3+ Days', count: stats.inTransitNow.byDeliveryTime.deliveringLater }
  ]

  // Prepare data for Pending Pickups Urgency (Pie Chart)
  const pendingPickupsData = [
    { name: 'Overdue', value: stats.pendingPickups.overdueBreakdown.overdue },
    { name: 'Due Soon', value: stats.pendingPickups.overdueBreakdown.dueSoon },
    { name: 'On Time', value: stats.pendingPickups.overdueBreakdown.onTime }
  ].filter(item => item.value > 0)

  const urgencyColors: Record<string, string> = {
    'Overdue': COLORS.error,
    'Due Soon': COLORS.warning,
    'On Time': COLORS.success
  }

  const onTimeColors: Record<string, string> = {
    'On Time': COLORS.success,
    'Delayed': COLORS.error
  }

  const NoDataMessage = () => (
    <div className="flex items-center justify-center h-[300px] text-gray-500">
      <p>No data available</p>
    </div>
  )

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Shipment Status Distribution - Pie Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">Shipment Status Distribution</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Total shipments aggregated by status ({timeFrame})</p>
        {shipmentStatusData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={shipmentStatusData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${((percent ?? 0) * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {shipmentStatusData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={STATUS_COLORS[entry.name.toLowerCase().replace(/ /g, '')] || COLORS.gray} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <NoDataMessage />
        )}
      </div>

      {/* Carrier Performance Comparison - Bar Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">Carrier Performance Comparison</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Shipments distributed across carriers ({timeFrame})</p>
        {carrierPerformanceData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={carrierPerformanceData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="shipments" fill={COLORS.primary} name="Deliveries" />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <NoDataMessage />
        )}
      </div>

      {/* Delivery Time Distribution - Area Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">Delivery Time Distribution</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Delivered orders categorized by delivery time ranges ({timeFrame})</p>
        {deliveryTimeData.some(d => d.count > 0) ? (
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={deliveryTimeData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="range" />
              <YAxis />
              <Tooltip />
              <Area type="monotone" dataKey="count" stroke={COLORS.secondary} fill={COLORS.secondary} fillOpacity={0.6} name="Orders" />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <NoDataMessage />
        )}
      </div>

      {/* On-Time Delivery Performance - Pie Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">On-Time Delivery Performance</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Deliveries completed, classified by on-time status ({timeFrame})</p>
        {onTimeDeliveryData.length > 0 ? (
          <>
            <div className="text-center mb-2">
              <span className="text-3xl font-bold text-green-600">{stats.deliveredToday.onTimeBreakdown.onTimePercentage}%</span>
              <p className="text-sm text-gray-600">On-Time Rate</p>
            </div>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={onTimeDeliveryData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name}: ${value}`}
                  outerRadius={70}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {onTimeDeliveryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={onTimeColors[entry.name] || COLORS.gray} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </>
        ) : (
          <NoDataMessage />
        )}
      </div>

      {/* Warehouse Orders Distribution - Bar Chart */}
      {warehouseOrdersData.length > 0 && (
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold mb-1">Warehouse Orders Distribution</h3>
          <p className="text-xs text-gray-500 mb-3">Source: Orders grouped by warehouse location ({timeFrame})</p>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={warehouseOrdersData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="name" type="category" width={120} />
              <Tooltip />
              <Legend />
              <Bar dataKey="orders" fill={COLORS.teal} name="Orders" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Exception Analysis - Bar Chart */}
      {exceptionTypeData.length > 0 && (
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold mb-1">Exception Analysis by Type</h3>
          <p className="text-xs text-gray-500 mb-3">Source: Shipping exceptions categorized by exception type ({timeFrame})</p>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={exceptionTypeData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="type" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="count" fill={COLORS.error} name="Exceptions" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* In Transit Delivery Schedule - Horizontal Bar Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">In Transit Delivery Schedule</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Shipments currently in transit, grouped by expected delivery time</p>
        {inTransitTimeData.some(d => d.count > 0) ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={inTransitTimeData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="label" type="category" width={100} />
              <Tooltip />
              <Legend />
              <Bar dataKey="count" fill={COLORS.orange} name="Shipments" />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <NoDataMessage />
        )}
      </div>

      {/* Pending Pickups Urgency - Pie Chart */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <h3 className="text-lg font-semibold mb-1">Pending Pickups Urgency</h3>
        <p className="text-xs text-gray-500 mb-3">Source: Shipments awaiting pickup, classified by urgency level</p>
        {pendingPickupsData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pendingPickupsData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, value }) => `${name}: ${value}`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pendingPickupsData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={urgencyColors[entry.name] || COLORS.gray} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <NoDataMessage />
        )}
      </div>
    </div>
  )
}
