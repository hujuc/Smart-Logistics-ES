/**
 * Logistics Service
 * Handles API calls for logistics dashboard statistics
 */

import { API_ENDPOINTS, apiFetch } from '../api';

/**
 * Period type for trend calculation
 */
export type TrendPeriod = 'daily' | 'weekly' | 'monthly';

/**
 * Comprehensive logistics statistics response
 */
export interface LogisticsStats {
  period: string;
  totalShipments: TotalShipmentsMetric;
  ordersToday: OrdersMetric;
  pendingPickups: PendingPickupsMetric;
  inTransitNow: InTransitMetric;
  deliveredToday: DeliveredMetric;
  averageCostPerShipment: CostMetric;
  totalShipmentCosts: CostMetric;
  averageDeliveryTime: TimeMetric;
  totalExceptions: ExceptionsMetric;
  totalTickets: TicketsMetric;
  totalWarehouses: WarehousesMetric;
  activeDrivers: DriversMetric;
}

export interface StatWithTrend {
  value: number;
  trend: number | null;
  label: string;
  alert?: string;
  hasWarning?: boolean;
}

export interface TotalShipmentsMetric extends StatWithTrend {
  byStatus: StatusBreakdown;
  byCarrier: CarrierBreakdown[];
}

export interface OrdersMetric extends StatWithTrend {
  byWarehouse: WarehouseBreakdown[];
  byStatus: Record<string, number>;
}

export interface PendingPickupsMetric extends StatWithTrend {
  byWarehouse: WarehouseBreakdown[];
  overdueBreakdown: OverdueBreakdown;
}

export interface InTransitMetric extends StatWithTrend {
  byCarrier: CarrierBreakdown[];
  byDeliveryTime: DeliveryTimeBreakdown;
}

export interface DeliveredMetric extends StatWithTrend {
  byCarrier: CarrierBreakdown[];
  onTimeBreakdown: OnTimeBreakdown;
}

export interface CostMetric extends StatWithTrend {
  currency: string;
  byCarrier: CarrierCostBreakdown[];
  byCostRange: CostRangeBreakdown[];
}

export interface TimeMetric extends StatWithTrend {
  unit: string;
  byCarrier: CarrierTimeBreakdown[];
  byTimeRange: TimeRangeBreakdown;
}

export interface ExceptionsMetric extends StatWithTrend {
  byType: Record<string, number>;
  byCarrier: CarrierBreakdown[];
  byStatus: Record<string, number>;
  bySeverity: Record<string, number>;
}

export interface TicketsMetric extends StatWithTrend {
  byStatus: Record<string, number>;
  byCategory: Record<string, number>;
  averageResolutionTime: number;
  resolutionTimeUnit: string;
}

export interface WarehousesMetric extends StatWithTrend {
  warehouses: WarehouseBreakdown[];
}

export interface DriversMetric extends StatWithTrend {
  byCarrier: CarrierBreakdown[];
  total: number;
  inactive: number;
}

// Breakdown types
export interface StatusBreakdown {
  created: number;
  readyForPickup: number;
  collected: number;
  inTransit: number;
  delivered: number;
  cancelled: number;
}

export interface CarrierBreakdown {
  carrierCode: string;
  carrierName: string;
  count: number;
  percentage: number;
}

export interface CarrierCostBreakdown {
  carrierCode: string;
  carrierName: string;
  totalCost: number;
  averageCost: number;
  shipmentCount: number;
}

export interface CarrierTimeBreakdown {
  carrierCode: string;
  carrierName: string;
  averageTime: number;
  unit: string;
  shipmentCount: number;
}

export interface WarehouseBreakdown {
  warehouseId: number;
  warehouseName: string;
  count: number;
  percentage: number;
}

export interface OverdueBreakdown {
  overdue: number;
  dueSoon: number;
  onTime: number;
}

export interface DeliveryTimeBreakdown {
  deliveringSoon: number;
  delivering1to3Days: number;
  deliveringLater: number;
}

export interface OnTimeBreakdown {
  onTime: number;
  delayed: number;
  onTimePercentage: number;
}

export interface CostRangeBreakdown {
  range: string;
  count: number;
  totalCost: number;
}

export interface TimeRangeBreakdown {
  lessThan24Hours: number;
  oneToThreeDays: number;
  threeTo7Days: number;
  moreThan7Days: number;
}

/**
 * Fetch logistics dashboard statistics
 *
 * @param period - Time period for trend calculation (daily, weekly, monthly)
 * @returns Promise with comprehensive logistics statistics
 */
export async function getLogisticsStats(period: TrendPeriod = 'daily'): Promise<LogisticsStats> {
  return apiFetch<LogisticsStats>(API_ENDPOINTS.logistics.statsWithPeriod(period));
}

/**
 * Logistics service object with all available methods
 */
export const logisticsService = {
  getStats: getLogisticsStats,
};
