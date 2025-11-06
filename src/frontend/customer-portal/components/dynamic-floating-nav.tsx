"use client"

import { usePathname } from "next/navigation"
import { useEffect, useState } from "react"
import { navConfig } from "@/lib/nav-config"
import { FloatingNavbar } from "./floating-navbar"

export function DynamicFloatingNav() {
  const pathname = usePathname()
  const [mounted, setMounted] = useState(false)
  
  useEffect(() => {
    setMounted(true)
  }, [])
  
  if (!mounted) return null
  
  const currentNav = navConfig.find(nav => 
    pathname?.startsWith(nav.basePath)
  )

  if (!currentNav) return null

  return <FloatingNavbar navItems={currentNav.items} />
}

