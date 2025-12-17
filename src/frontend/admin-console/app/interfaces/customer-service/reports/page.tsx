"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import {
  TrendingUp,
  MessageSquare,
  Clock,
  CheckCircle2,
  AlertCircle,
} from "lucide-react"
import Link from "next/link"
import { API_ENDPOINTS, createAuthHeaders } from "@/lib/api"

interface ExceptionSummary {
  totalExceptions: number
  openExceptions: number
  inProgressExceptions: number
  resolvedToday: number
  highPriorityExceptions: number
  slaBreachedExceptions: number
  averageResolutionTimeHours: number
  customerSatisfactionRate: number
}

export default function ReportsPage() {
  const [summary, setSummary] = useState<ExceptionSummary | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        setIsLoading(true)
        const response = await fetch(API_ENDPOINTS.customerService.dashboardSummary, {
          headers: createAuthHeaders(),
          credentials: 'include',
        })

        if (!response.ok) {
          throw new Error(`Failed to fetch dashboard summary: ${response.status} ${response.statusText}`)
        }

        const data = await response.json()
        setSummary(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred')
      } finally {
        setIsLoading(false)
      }
    }

    fetchSummary()
  }, [])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading reports...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <AlertCircle className="h-12 w-12 text-red-600 mx-auto" />
          <p className="mt-4 text-red-600">Error: {error}</p>
          <Link href="/interfaces/customer-service">
            <Button variant="outline" className="mt-4">Back to Inquiries</Button>
          </Link>
        </div>
      </div>
    )
  }

  if (!summary) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <p className="text-gray-600">No data available</p>
          <Link href="/interfaces/customer-service">
            <Button variant="outline" className="mt-4">Back to Inquiries</Button>
          </Link>
        </div>
      </div>
    )
  }

  // Calculate resolution rate
  const resolvedCount = summary.totalExceptions - summary.openExceptions - summary.inProgressExceptions
  const resolutionRate = summary.totalExceptions > 0
    ? ((resolvedCount / summary.totalExceptions) * 100).toFixed(1)
    : 0

  // Format average resolution time
  const avgResolutionTime = summary.averageResolutionTimeHours
    ? `${summary.averageResolutionTimeHours.toFixed(1)} hours`
    : 'N/A'

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Customer Service Reports</h1>
        <div className="flex gap-3">
          <Link href="/interfaces/customer-service">
            <Button variant="outline">Back to Inquiries</Button>
          </Link>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Inquiries</p>
              <p className="text-3xl font-bold text-black">{summary.totalExceptions}</p>
            </div>
            <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <MessageSquare className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            <span className="text-gray-500">
              {summary.openExceptions} open, {summary.inProgressExceptions} in progress
            </span>
          </div>
        </div>

        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Resolved Today</p>
              <p className="text-3xl font-bold text-black">{summary.resolvedToday}</p>
            </div>
            <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
              <CheckCircle2 className="h-6 w-6 text-green-600" />
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center justify-between text-sm mb-1">
              <span className="text-gray-500">Resolution Rate</span>
              <span className="font-medium text-black">{resolutionRate}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: `${resolutionRate}%` }}></div>
            </div>
          </div>
        </div>

        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Avg Response Time</p>
              <p className="text-3xl font-bold text-black">{avgResolutionTime}</p>
            </div>
            <div className="h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <Clock className="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            <span className="text-gray-500">{summary.slaBreachedExceptions} SLA breached</span>
          </div>
        </div>

        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Satisfaction Rate</p>
              <p className="text-3xl font-bold text-black">
                {summary.customerSatisfactionRate
                  ? `${summary.customerSatisfactionRate.toFixed(1)}%`
                  : 'N/A'}
              </p>
            </div>
            <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <TrendingUp className="h-6 w-6 text-purple-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            <span className="text-gray-500">Based on customer feedback</span>
          </div>
        </div>
      </div>

      {/* Priority Breakdown */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-red-100 rounded-lg flex items-center justify-center">
            <AlertCircle className="h-6 w-6 text-red-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Priority Breakdown</h2>
        </div>

        <div className="space-y-4">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-black">High Priority</span>
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">{summary.highPriorityExceptions} cases</span>
                <span className="text-sm font-semibold text-black">
                  {summary.totalExceptions > 0
                    ? ((summary.highPriorityExceptions / summary.totalExceptions) * 100).toFixed(1)
                    : 0}%
                </span>
              </div>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="h-2 rounded-full bg-red-500"
                style={{
                  width: `${summary.totalExceptions > 0
                    ? (summary.highPriorityExceptions / summary.totalExceptions) * 100
                    : 0}%`
                }}
              ></div>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-black">In Progress</span>
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">{summary.inProgressExceptions} cases</span>
                <span className="text-sm font-semibold text-black">
                  {summary.totalExceptions > 0
                    ? ((summary.inProgressExceptions / summary.totalExceptions) * 100).toFixed(1)
                    : 0}%
                </span>
              </div>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="h-2 rounded-full bg-yellow-500"
                style={{
                  width: `${summary.totalExceptions > 0
                    ? (summary.inProgressExceptions / summary.totalExceptions) * 100
                    : 0}%`
                }}
              ></div>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-black">Open</span>
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">{summary.openExceptions} cases</span>
                <span className="text-sm font-semibold text-black">
                  {summary.totalExceptions > 0
                    ? ((summary.openExceptions / summary.totalExceptions) * 100).toFixed(1)
                    : 0}%
                </span>
              </div>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="h-2 rounded-full bg-blue-500"
                style={{
                  width: `${summary.totalExceptions > 0
                    ? (summary.openExceptions / summary.totalExceptions) * 100
                    : 0}%`
                }}
              ></div>
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-black">Resolved</span>
              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">{resolvedCount} cases</span>
                <span className="text-sm font-semibold text-black">{resolutionRate}%</span>
              </div>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="h-2 rounded-full bg-green-500"
                style={{ width: `${resolutionRate}%` }}
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
