"use client"

import { usePathname, useSearchParams } from "next/navigation"
import { Suspense } from "react"
import { navConfig, warehouseNavItems, logisticsNavItems, customerServiceNavItems } from "@/lib/nav-config"
import { FloatingNavbar } from "./floating-navbar"

function DynamicFloatingNavContent() {
  const pathname = usePathname()
  const searchParams = useSearchParams()

  console.log("[DynamicFloatingNav] pathname:", pathname)
  console.log("[DynamicFloatingNav] searchParams:", searchParams.toString())

  // Special handling for settings page
  if (pathname === "/settings" || pathname === "/settings/") {
    const from = searchParams.get("from")
    console.log("[DynamicFloatingNav] Settings page detected, from parameter:", from)
    let navItems = warehouseNavItems // default
    let navType = "warehouse (default)"

    if (from === "logistics") {
      navItems = logisticsNavItems
      navType = "logistics"
    } else if (from === "customer-service") {
      navItems = customerServiceNavItems
      navType = "customer-service"
    } else if (from === "warehouse") {
      navItems = warehouseNavItems
      navType = "warehouse"
    }

    console.log("[DynamicFloatingNav] Using nav type:", navType)
    console.log("[DynamicFloatingNav] Nav items count:", navItems.length)

    return <FloatingNavbar navItems={navItems} />
  }

  const currentNav = navConfig.find(nav =>
    pathname?.startsWith(nav.basePath)
  )

  console.log("[DynamicFloatingNav] Current nav found:", currentNav?.basePath)

  if (!currentNav) return null

  return <FloatingNavbar navItems={currentNav.items} />
}

export function DynamicFloatingNav() {
  return (
    <Suspense fallback={null}>
      <DynamicFloatingNavContent />
    </Suspense>
  )
}

