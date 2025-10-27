"use client"

import { LucideIcon } from "lucide-react"

interface DriverStatCardProps {
  title: string
  value: number
  icon: LucideIcon
  iconBgColor: string
  iconColor: string
}

export function DriverStatCard({
  title,
  value,
  icon: Icon,
  iconBgColor,
  iconColor,
}: DriverStatCardProps) {
  return (
    <div className="bg-white border rounded-lg p-6 shadow-sm">
      <div className="flex items-center gap-3">
        <div className={`h-12 w-12 ${iconBgColor} rounded-lg flex items-center justify-center`}>
          <Icon className={`h-6 w-6 ${iconColor}`} />
        </div>
        <div>
          <p className="text-sm text-gray-600">{title}</p>
          <p className="text-2xl font-bold text-black">{value}</p>
        </div>
      </div>
    </div>
  )
}

