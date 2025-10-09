"use client"

import { usePathname } from "next/navigation"
import { navConfig } from "@/lib/nav-config"
import FloatingNavbar from "./floating-navbar"

export function DynamicFloatingNav() {
  const pathname = usePathname()
  
  const currentNav = navConfig.find(nav => 
    pathname?.startsWith(nav.basePath)
  )

  if (!currentNav) return null

  return <FloatingNavbar items={currentNav.items} />
}

