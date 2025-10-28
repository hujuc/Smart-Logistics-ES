"use client"

import { Button } from "@/components/ui/button"

interface KPIChartsNavProps {
  activeTab: "kpis" | "charts"
  onTabChange: (tab: "kpis" | "charts") => void
}

export function KPIChartsNav({ activeTab, onTabChange }: KPIChartsNavProps) {
  return (
    <div className="bg-white border rounded-lg p-2 shadow-sm w-fit ">
      <div className="flex gap-2">
        <Button
          variant={activeTab === "kpis" ? "default" : "ghost"}
          onClick={() => onTabChange("kpis")}
        >
          KPIs
        </Button>
        <Button
          variant={activeTab === "charts" ? "default" : "ghost"}
          onClick={() => onTabChange("charts")}
        >
          Performance Charts
        </Button>
      </div>
    </div>
  )
}

