"use client"

import { FloatingNavbar } from "@/components/floating-navbar"
import { customerServiceNavItems } from "@/lib/nav-config"

export default function CustomerServiceLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <>
      <FloatingNavbar navItems={customerServiceNavItems} />
      {children}
    </>
  )
}
