"use client"

import { Button } from "@/components/ui/button"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { useNotificationAnimation } from "../shared/hooks/use-notification-animation"
import { useUnreadNotifications } from "../shared/hooks/use-notifications"

export function FloatingNavbar({ navItems }) {
  const pathname = usePathname()
  const unreadCount = useUnreadNotifications()
  const shouldAnimate = useNotificationAnimation(unreadCount)

  // Safety check
  if (!navItems || !Array.isArray(navItems)) {
    return null
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
    <nav className="fixed left-6 top-1/2 -translate-y-1/2 z-50">
      <div className="flex flex-col gap-3 bg-background border border-border rounded-3xl p-4 shadow-lg">
        {navItems.map((item) => {
          const Icon = item.icon
          const active = isActive(item.href)

          return (
            <Link key={item.href} href={item.href}>
              <div className="relative">
                <Button
                  variant="ghost"
                  size="icon"
                  className={`h-14 w-14 rounded-2xl transition-all duration-200 ${active
                    ? "bg-primary text-primary-foreground shadow-md"
                    : "text-muted-foreground hover:bg-primary/50 hover:text-foreground"
                    }`}
                  aria-label={item.label}
                  title={item.label}
                >
                  <Icon className="h-6 w-6" />
                </Button>
                {/* Notification badge */}
                {item.label === "Notifications" && unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs font-bold text-white animate-in fade-in zoom-in duration-300">
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
