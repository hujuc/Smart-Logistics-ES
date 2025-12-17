"use client"

import { Button } from "@/components/ui/button"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { useState, useEffect } from "react"
import { ChevronRight, ChevronLeft } from "lucide-react"
import { useNotificationAnimation } from "../shared/hooks/use-notification-animation"
import { useUnreadNotifications } from "../shared/hooks/use-notifications"

export function FloatingNavbar({ navItems }) {
  const pathname = usePathname()
  const [isCollapsed, setIsCollapsed] = useState(false)
  const [mounted, setMounted] = useState(false)

  // Notification state
  const unreadCount = useUnreadNotifications()
  const shouldAnimate = useNotificationAnimation(unreadCount)

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

    // Special case: When viewing order details (/interfaces/logistics/orders/1),
    // ONLY highlight the Warehouses nav item, not Overview
    if (pathname?.startsWith('/interfaces/logistics/orders/')) {
      return pathWithoutQuery === '/interfaces/logistics/warehouses'
    }

    // For child routes not in the nav (like /interfaces/warehouse/package/123),
    // highlight the most specific parent
    const hasExactMatch = navItems.some(item => {
      const itemPathWithoutQuery = item.href.split('?')[0]
      return itemPathWithoutQuery === pathname
    })

    if (!hasExactMatch && pathname?.startsWith(pathWithoutQuery + '/')) {
      // Find the longest (most specific) matching nav item
      const matchingItems = navItems.filter(item => {
        const itemPathWithoutQuery = item.href.split('?')[0]
        return pathname?.startsWith(itemPathWithoutQuery + '/')
      })

      if (matchingItems.length > 0) {
        // Sort by length (longest first) and only return true for the longest match
        const longestMatch = matchingItems.reduce((longest, current) => {
          const currentPath = current.href.split('?')[0]
          const longestPath = longest.href.split('?')[0]
          return currentPath.length > longestPath.length ? current : longest
        })

        return longestMatch.href.split('?')[0] === pathWithoutQuery
      }

      return true
    }

    return false
  }

  return (
    <nav className={`fixed top-1/2 -translate-y-1/2 z-50 transition-all duration-300 ${isCollapsed ? 'left-2' : 'left-4 md:left-6'
      }`}>
      <div className={`flex flex-col gap-3 bg-background border border-border rounded-3xl shadow-lg transition-all duration-300 ${isCollapsed ? 'p-2' : 'p-3 md:p-4'
        }`}>
        {/* Toggle Button */}
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleCollapse}
          className={`rounded-2xl transition-all duration-200 hover:bg-primary/20 ${isCollapsed ? 'h-10 w-10' : 'h-12 w-12 md:h-14 md:w-14'
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
              <div className="relative">
                <Button
                  variant="ghost"
                  size="icon"
                  className={`h-12 w-12 md:h-14 md:w-14 rounded-2xl transition-all duration-200 ${active
                    ? "bg-primary text-primary-foreground shadow-md"
                    : "text-muted-foreground hover:bg-primary/50 hover:text-foreground"
                    }`}
                  aria-label={item.label}
                  title={item.label}
                >
                  <Icon className="h-5 w-5 md:h-6 md:w-6" />
                </Button>

                {/* Notification badge */}
                {item.label === "Notifications" && unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs font-bold text-white animate-in fade-in zoom-in duration-300 pointer-events-none">
                    {unreadCount > 9 ? '9+' : unreadCount}
                    {/* Pulse animation - only when new notification arrives */}
                    {shouldAnimate && (
                      <span className="absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75 animate-ping"></span>
                    )}
                  </span>
                )}
              </div>
            </Link>
          )
        })}
      </div>
    </nav>
  )
}
