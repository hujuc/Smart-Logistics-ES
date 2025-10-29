import { MessageSquare, FileText, Settings } from "lucide-react"
import { LucideIcon } from "lucide-react"

export interface NavItem {
  href: string
  icon: LucideIcon
  label: string
}

// Customer Portal Support Navigation
export const supportNavItems: NavItem[] = [
  {
    href: "/interfaces/support/tickets",
    icon: MessageSquare,
    label: "My Tickets",
  },
  {
    href: "/interfaces/support/history",
    icon: FileText,
    label: "Order History",
  },
  {
    href: "/settings",
    icon: Settings,
    label: "Settings",
  },
]

// Navigation configuration for dynamic floating nav
export const navConfig = [
  {
    basePath: "/interfaces/support",
    items: supportNavItems,
  },
  {
    basePath: "/settings",
    items: supportNavItems,
  },
]
