"use client"

import { FloatingNavbar } from "@/components/floating-navbar"
import { warehouseNavItems } from "@/lib/nav-config"

export default function WarehouseLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <FloatingNavbar navItems={warehouseNavItems} />
      {children}
    </>
  )
}

