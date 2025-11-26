/**
 * Logistics Service
 * Handles API calls for logistics dashboard statistics
 */

import { API_ENDPOINTS, apiFetch, createAuthHeaders } from '../api';

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
 * Export logistics KPI report as PDF
 *
 * @param period - Time period for the report (daily, weekly, monthly)
 * @returns Promise that triggers PDF download
 */
export async function exportLogisticsReport(period: TrendPeriod = 'daily'): Promise<void> {
  try {
    const response = await fetch(API_ENDPOINTS.logistics.exportReport(period), {
      method: 'GET',
      credentials: 'include',
      headers: {
        ...createAuthHeaders(''),
        'Accept': 'application/pdf',
      },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ error: 'Failed to export report' }));
      throw new Error(errorData.error || 'Failed to export report');
    }

    // Get filename from Content-Disposition header or create default
    const contentDisposition = response.headers.get('Content-Disposition');
    let filename = `logistics_kpi_report_${period}_${new Date().toISOString().split('T')[0]}.pdf`;

    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '');
      }
    }

    // Convert response to blob and trigger download
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Error exporting logistics report:', error);
    throw error;
  }
}

/**
 * Logistics service object with all available methods
 */
export const logisticsService = {
  getStats: getLogisticsStats,
  exportReport: exportLogisticsReport,
};
