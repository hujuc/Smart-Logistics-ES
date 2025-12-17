import { Package, Settings, Truck, MapPin, Users, FileText, BarChart, Search, Warehouse, Bell } from "lucide-react"
import { LucideIcon } from "lucide-react"

export interface NavItem {
  href: string
  icon: LucideIcon
  label: string
}

// Warehouse Interface Navigation
export const warehouseNavItems: NavItem[] = [
  {
    href: "/interfaces/warehouse",
    icon: Package,
    label: "Warehouse",
  },
  {
    href: "/interfaces/warehouse/carrier-comparison",
    icon: Truck,
    label: "New Shipment",
  },
  {
    href: "/interfaces/warehouse/notifications",
    icon: Bell,
    label: "Notifications",
  },
  {
    href: "/settings?from=warehouse",
    icon: Settings,
    label: "Settings",
  },
]

// Logistics Interface Navigation
export const logisticsNavItems: NavItem[] = [
  {
    href: "/interfaces/logistics",
    icon: BarChart,
    label: "Overview",
  },
  {
    href: "/interfaces/logistics/tracking",
    icon: MapPin,
    label: "Tracking",
  },
  {
    href: "/interfaces/logistics/warehouses",
    icon: Warehouse,
    label: "Warehouses",
  },
  {
    href: "/interfaces/logistics/users",
    icon: Users,
    label: "Users",
  },
  {
    href: "/interfaces/logistics/notifications",
    icon: Bell,
    label: "Notifications",
  },
  {
    href: "/settings?from=logistics",
    icon: Settings,
    label: "Settings",
  },
]

// Customer Service Interface Navigation
export const customerServiceNavItems: NavItem[] = [
  {
    href: "/interfaces/customer-service/tickets",
    icon: FileText,
    label: "Tickets",
  },
  {
    href: "/interfaces/customer-service/order-lookup",
    icon: Search,
    label: "Order Lookup",
  },
  {
    href: "/interfaces/customer-service/reports",
    icon: BarChart,
    label: "Reports",
  },
  {
    href: "/interfaces/customer-service/notifications",
    icon: Bell,
    label: "Notifications",
  },
  {
    href: "/settings?from=customer-service",
    icon: Settings,
    label: "Settings",
  },
]

// Navigation configuration for dynamic floating nav
export const navConfig = [
  {
    basePath: "/interfaces/warehouse",
    items: warehouseNavItems,
  },
  {
    basePath: "/interfaces/logistics",
    items: logisticsNavItems,
  },
  {
    basePath: "/interfaces/customer-service",
    items: customerServiceNavItems,
  },
  {
    basePath: "/settings",
    items: warehouseNavItems, // Default to warehouse, will be handled by component
  },
]
