"use client"

import { usePathname, useSearchParams } from "next/navigation"
import { navConfig, warehouseNavItems, logisticsNavItems, customerServiceNavItems } from "@/lib/nav-config"
import { FloatingNavbar } from "./floating-navbar"

export function DynamicFloatingNav() {
  const pathname = usePathname()
  const searchParams = useSearchParams()
  
  // Special handling for settings page
  if (pathname === "/settings") {
    const from = searchParams.get("from")
    let navItems = warehouseNavItems // default
    
    if (from === "logistics") {
      navItems = logisticsNavItems
    } else if (from === "costumer-service" || from === "customer-service") {
      navItems = customerServiceNavItems
    }
    
    return <FloatingNavbar navItems={navItems} />
  }
  
  const currentNav = navConfig.find(nav => 
    pathname?.startsWith(nav.basePath)
  )

  if (!currentNav) return null

  return <FloatingNavbar navItems={currentNav.items} />
}

