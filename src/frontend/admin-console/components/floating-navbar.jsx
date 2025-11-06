"use client"

import { Button } from "@/components/ui/button"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { ChevronRight, ChevronLeft } from "lucide-react"
import { useState, useEffect } from "react"

export function FloatingNavbar({ navItems }) {
  const pathname = usePathname()
  const [isCollapsed, setIsCollapsed] = useState(false)
  const [mounted, setMounted] = useState(false)

  // Load collapsed state from localStorage on mount
  useEffect(() => {
    setMounted(true)
    const saved = localStorage.getItem('navbarCollapsed')
    if (saved !== null) {
      setIsCollapsed(saved === 'true')
    }
  }, [])

  // Prevent hydration mismatch by not rendering dynamic content until mounted
  if (!mounted) {
    // Render with default state during SSR and initial hydration
    return (
      <nav className="fixed top-1/2 -translate-y-1/2 z-50 transition-all duration-300 left-4 md:left-6">
        <div className="flex flex-col gap-3 bg-background border border-border rounded-3xl shadow-lg transition-all duration-300 p-3 md:p-4">
          <Button
            variant="ghost"
            size="icon"
            className="rounded-2xl transition-all duration-200 hover:bg-primary/20 h-12 w-12 md:h-14 md:w-14"
            aria-label="Collapse navigation"
            title="Collapse navigation"
          >
            <ChevronLeft className="h-5 w-5" />
          </Button>
          {navItems.map((item) => {
            const Icon = item.icon
            return (
              <Link key={item.href} href={item.href}>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-12 w-12 md:h-14 md:w-14 rounded-2xl transition-all duration-200 text-muted-foreground hover:bg-primary/50 hover:text-foreground"
                  aria-label={item.label}
                  title={item.label}
                >
                  <Icon className="h-5 w-5 md:h-6 md:w-6" />
                </Button>
              </Link>
            )
          })}
        </div>
      </nav>
    )
  }

  // Save collapsed state to localStorage
  const toggleCollapse = () => {
    const newState = !isCollapsed
    setIsCollapsed(newState)
    localStorage.setItem('navbarCollapsed', newState.toString())
  }

  const isActive = (path) => {
    // Strip query parameters from the path for comparison
    const pathWithoutQuery = path.split('?')[0]

    // Exact match for main navigation items
    if (pathname === pathWithoutQuery) return true

    // For child routes not in the nav (like /interfaces/warehouse/package/123),
    // highlight the parent if no exact match exists
    const hasExactMatch = navItems.some(item => {
      const itemPathWithoutQuery = item.href.split('?')[0]
      return itemPathWithoutQuery === pathname
    })
    if (!hasExactMatch && pathname?.startsWith(pathWithoutQuery + '/')) {
      return true
    }

    return false
  }

  return (
    <nav className={`fixed top-1/2 -translate-y-1/2 z-50 transition-all duration-300 ${
      isCollapsed ? 'left-2' : 'left-4 md:left-6'
    }`}>
      <div className={`flex flex-col gap-3 bg-background border border-border rounded-3xl shadow-lg transition-all duration-300 ${
        isCollapsed ? 'p-2' : 'p-3 md:p-4'
      }`}>
        {/* Toggle Button */}
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleCollapse}
          className={`rounded-2xl transition-all duration-200 hover:bg-primary/20 ${
            isCollapsed ? 'h-10 w-10' : 'h-12 w-12 md:h-14 md:w-14'
          }`}
          aria-label={isCollapsed ? "Expand navigation" : "Collapse navigation"}
          title={isCollapsed ? "Expand navigation" : "Collapse navigation"}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5" />
          ) : (
            <ChevronLeft className="h-5 w-5" />
          )}
        </Button>

        {/* Navigation Items - Hidden when collapsed */}
        {!isCollapsed && navItems.map((item) => {
          const Icon = item.icon
          const active = isActive(item.href)

          return (
            <Link key={item.href} href={item.href}>
              <Button
                variant="ghost"
                size="icon"
                className={`h-12 w-12 md:h-14 md:w-14 rounded-2xl transition-all duration-200 ${
                  active
                    ? "bg-primary text-primary-foreground shadow-md"
                    : "text-muted-foreground hover:bg-primary/50 hover:text-foreground"
                }`}
                aria-label={item.label}
                title={item.label}
              >
                <Icon className="h-5 w-5 md:h-6 md:w-6" />
              </Button>
            </Link>
          )
        })}
      </div>
    </nav>
  )
}
