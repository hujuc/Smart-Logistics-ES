"use client"

import { useState } from "react"
import { TrendingDown, TrendingUp, AlertTriangle, LucideIcon, ChevronDown, ChevronUp } from "lucide-react"
import { StatCard } from "@/types"

interface KPICardProps {
  title: string
  icon: LucideIcon
  stat: StatCard
  color: string
  breakdown?: React.ReactNode
  expandable?: boolean
}

export function KPICard({ title, icon: Icon, stat, color, breakdown, expandable = false }: KPICardProps) {
  const [isExpanded, setIsExpanded] = useState(false)

  return (
    <div className="bg-white border rounded-lg shadow-sm">
      <div className="p-6">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-3">
            <div className={`${color} rounded-lg p-2`}>
              <Icon className="h-5 w-5" />
            </div>
            <p className="text-sm font-medium text-gray-600">{title}</p>
          </div>
          {expandable && breakdown && (
            <button
              onClick={() => setIsExpanded(!isExpanded)}
              className="text-gray-500 hover:text-gray-700 transition-colors"
              aria-label={isExpanded ? "Collapse" : "Expand"}
            >
              {isExpanded ? <ChevronUp className="h-5 w-5" /> : <ChevronDown className="h-5 w-5" />}
            </button>
          )}
        </div>
        <p className="text-4xl font-bold text-black mb-2">{stat.value}</p>

        {stat.trend !== undefined && stat.trend !== null && (
          <div className={`flex items-center gap-1 ${stat.trend >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {stat.trend >= 0 ? (
              <TrendingUp className="h-4 w-4" />
            ) : (
              <TrendingDown className="h-4 w-4" />
            )}
            <span className="text-sm font-medium">{Math.abs(stat.trend)}%</span>
            {stat.label && <span className="text-xs text-gray-500">{stat.label}</span>}
          </div>
        )}

        {stat.hasWarning && stat.alert && (
          <div className="flex items-center gap-1 text-yellow-600">
            <AlertTriangle className="h-4 w-4" />
            <span className="text-sm font-medium">{stat.alert}</span>
          </div>
        )}
      </div>

      {/* Expandable breakdown section */}
      {isExpanded && breakdown && (
        <div className="border-t bg-gray-50 p-4">
          {breakdown}
        </div>
      )}
    </div>
  )
}

