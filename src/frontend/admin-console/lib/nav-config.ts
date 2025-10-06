import { Package, QrCode, Settings, Truck, MapPin, Users, MessageSquare, FileText, BarChart } from "lucide-react"
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
    href: "/interfaces/warehouse/qr-scanner",
    icon: QrCode,
    label: "QR Scanner",
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
    icon: Truck,
    label: "Routes",
  },
  {
    href: "/interfaces/logistics/tracking",
    icon: MapPin,
    label: "Tracking",
  },
  {
    href: "/interfaces/logistics/drivers",
    icon: Users,
    label: "Drivers",
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
    href: "/interfaces/costumer-service",
    icon: MessageSquare,
    label: "Inquiries",
  },
  {
    href: "/interfaces/costumer-service/tickets",
    icon: FileText,
    label: "Tickets",
  },
  {
    href: "/interfaces/costumer-service/reports",
    icon: BarChart,
    label: "Reports",
  },
  {
    href: "/settings?from=customer-service",
    icon: Settings,
    label: "Settings",
  },
]
