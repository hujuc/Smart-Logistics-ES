"use client"

import { TrendingDown, TrendingUp, AlertTriangle, LucideIcon } from "lucide-react"
import { StatCard } from "@/types"

interface KPICardProps {
  title: string
  icon: LucideIcon
  stat: StatCard
  color: string
}

export function KPICard({ title, icon: Icon, stat, color }: KPICardProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
      <div className="flex items-center gap-3 mb-2">
        <div className={`${color} rounded-lg p-2`}>
          <Icon className="h-5 w-5" />
        </div>
        <p className="text-sm font-medium text-gray-600">{title}</p>
      </div>
      <p className="text-4xl font-bold text-black mb-2">{stat.value}</p>
      
      {stat.trend !== undefined && (
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
  )
}

