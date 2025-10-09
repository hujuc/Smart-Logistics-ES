"use client"

import { Button } from "@/components/ui/button"
import {
  BarChart,
  Download,
  TrendingUp,
  TrendingDown,
  MessageSquare,
  Clock,
  CheckCircle2,
  AlertCircle,
} from "lucide-react"
import Link from "next/link"

export default function ReportsPage() {
  const stats = {
    totalInquiries: 1247,
    resolvedInquiries: 1089,
    avgResponseTime: "2.3 hours",
    satisfactionRate: 94.5,
    trendsUp: true,
  }

  const monthlyData = [
    { month: "Jun", inquiries: 180, resolved: 165 },
    { month: "Jul", inquiries: 210, resolved: 195 },
    { month: "Aug", inquiries: 195, resolved: 180 },
    { month: "Sep", inquiries: 230, resolved: 215 },
    { month: "Oct", inquiries: 185, resolved: 172 },
  ]

  const categoryBreakdown = [
    { category: "Delivery Issues", count: 342, percentage: 27.4 },
    { category: "Tracking Inquiries", count: 298, percentage: 23.9 },
    { category: "Refund Requests", count: 245, percentage: 19.6 },
    { category: "General Inquiries", count: 187, percentage: 15.0 },
    { category: "Other", count: 175, percentage: 14.1 },
  ]

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Customer Service Reports</h1>
        <div className="flex gap-3">
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" />
            Export Report
          </Button>
          <Link href="/interfaces/costumer-service">
            <Button variant="outline">Back to Inquiries</Button>
          </Link>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Inquiries</p>
              <p className="text-3xl font-bold text-black">{stats.totalInquiries}</p>
            </div>
            <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <MessageSquare className="h-6 w-6 text-blue-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            {stats.trendsUp ? (
              <>
                <TrendingUp className="h-4 w-4 text-green-600" />
                <span className="text-green-600 font-medium">+12%</span>
              </>
            ) : (
              <>
                <TrendingDown className="h-4 w-4 text-red-600" />
                <span className="text-red-600 font-medium">-5%</span>
              </>
            )}
            <span className="text-gray-500">vs last month</span>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Resolved</p>
              <p className="text-3xl font-bold text-black">{stats.resolvedInquiries}</p>
            </div>
            <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
              <CheckCircle2 className="h-6 w-6 text-green-600" />
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center justify-between text-sm mb-1">
              <span className="text-gray-500">Resolution Rate</span>
              <span className="font-medium text-black">87.3%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: "87.3%" }}></div>
            </div>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Avg Response Time</p>
              <p className="text-3xl font-bold text-black">{stats.avgResponseTime}</p>
            </div>
            <div className="h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <Clock className="h-6 w-6 text-yellow-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            <TrendingDown className="h-4 w-4 text-green-600" />
            <span className="text-green-600 font-medium">-18 min</span>
            <span className="text-gray-500">faster</span>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Satisfaction Rate</p>
              <p className="text-3xl font-bold text-black">{stats.satisfactionRate}%</p>
            </div>
            <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <TrendingUp className="h-6 w-6 text-purple-600" />
            </div>
          </div>
          <div className="mt-4 flex items-center gap-1 text-sm">
            <TrendingUp className="h-4 w-4 text-green-600" />
            <span className="text-green-600 font-medium">+2.1%</span>
            <span className="text-gray-500">improvement</span>
          </div>
        </div>
      </div>

      {/* Monthly Trends Chart */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-blue-100 rounded-lg flex items-center justify-center">
            <BarChart className="h-6 w-6 text-blue-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Monthly Trends</h2>
        </div>
        
        <div className="space-y-2 mb-4">
          <div className="flex items-center gap-4 text-sm">
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 bg-blue-500 rounded"></div>
              <span className="text-gray-600">Total Inquiries</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 bg-green-500 rounded"></div>
              <span className="text-gray-600">Resolved</span>
            </div>
          </div>
        </div>

        <div className="h-64 flex items-end justify-between gap-4">
          {monthlyData.map((data) => {
            const maxValue = Math.max(...monthlyData.map((d) => d.inquiries))
            const inquiriesHeight = (data.inquiries / maxValue) * 100
            const resolvedHeight = (data.resolved / maxValue) * 100

            return (
              <div key={data.month} className="flex-1 flex flex-col items-center gap-2">
                <div className="w-full flex items-end justify-center gap-2 h-48">
                  <div className="relative flex-1 bg-blue-500 rounded-t hover:bg-blue-600 transition-colors group">
                    <div
                      className="w-full bg-blue-500 rounded-t"
                      style={{ height: `${inquiriesHeight}%` }}
                    ></div>
                    <div className="absolute -top-6 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                      {data.inquiries}
                    </div>
                  </div>
                  <div className="relative flex-1 bg-green-500 rounded-t hover:bg-green-600 transition-colors group">
                    <div
                      className="w-full bg-green-500 rounded-t"
                      style={{ height: `${resolvedHeight}%` }}
                    ></div>
                    <div className="absolute -top-6 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                      {data.resolved}
                    </div>
                  </div>
                </div>
                <span className="text-sm font-medium text-gray-600">{data.month}</span>
              </div>
            )
          })}
        </div>
      </div>

      {/* Category Breakdown */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-purple-100 rounded-lg flex items-center justify-center">
            <AlertCircle className="h-6 w-6 text-purple-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Inquiries by Category</h2>
        </div>
        
        <div className="space-y-4">
          {categoryBreakdown.map((item, index) => (
            <div key={item.category}>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-black">{item.category}</span>
                <div className="flex items-center gap-3">
                  <span className="text-sm text-gray-600">{item.count} inquiries</span>
                  <span className="text-sm font-semibold text-black">{item.percentage}%</span>
                </div>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className={`h-2 rounded-full ${
                    index === 0
                      ? "bg-red-500"
                      : index === 1
                      ? "bg-yellow-500"
                      : index === 2
                      ? "bg-blue-500"
                      : index === 3
                      ? "bg-green-500"
                      : "bg-purple-500"
                  }`}
                  style={{ width: `${item.percentage}%` }}
                ></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
