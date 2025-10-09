"use client"

import { FloatingNavbar } from "@/components/floating-navbar"
import { logisticsNavItems } from "@/lib/nav-config"

export default function LogisticsLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <FloatingNavbar navItems={logisticsNavItems} />
      {children}
    </>
  )
}

