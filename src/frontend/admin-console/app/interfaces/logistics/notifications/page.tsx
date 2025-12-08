"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Search,
  Bell,
  CheckCircle,
  AlertTriangle,
  Info,
  XCircle,
  Check,
  Trash2,
  Mail,
  MailOpen,
  Package,
  Truck,
  MapPin
} from "lucide-react"

// Notification types
type NotificationType = "info" | "success" | "warning" | "error"
type NotificationStatus = "unread" | "read"

interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  timestamp: Date
  status: NotificationStatus
  relatedEntity?: string
  actionUrl?: string
}

// Mock data for static UI - Logistics-specific notifications
const mockNotifications: Notification[] = [
  {
    id: "1",
    type: "error",
    title: "Shipment Delayed",
    message: "Multiple packages in route RT-2024-12 are experiencing delays due to weather conditions.",
    timestamp: new Date(Date.now() - 1000 * 60 * 20),
    status: "unread",
    relatedEntity: "RT-2024-12",
    actionUrl: "/interfaces/logistics/tracking"
  },
  {
    id: "2",
    type: "success",
    title: "Batch Delivered",
    message: "Shipment batch #BATCH-2024-045 has been successfully delivered to all destinations.",
    timestamp: new Date(Date.now() - 1000 * 60 * 45),
    status: "unread",
    relatedEntity: "BATCH-2024-045",
    actionUrl: "/interfaces/logistics/tracking"
  },
  {
    id: "3",
    type: "warning",
    title: "Driver Overtime Alert",
    message: "Driver John Smith is approaching maximum hours. Route reassignment may be needed.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60),
    status: "read",
    relatedEntity: "John Smith",
    actionUrl: "/interfaces/logistics/users"
  },
  {
    id: "4",
    type: "info",
    title: "New Driver Onboarded",
    message: "Driver Sarah Johnson has completed onboarding and is now active in the system.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
    status: "read",
    relatedEntity: "Sarah Johnson",
    actionUrl: "/interfaces/logistics/users"
  },
  {
    id: "5",
    type: "success",
    title: "Route Optimized",
    message: "Route RT-2024-15 has been optimized, saving 2.5 hours of delivery time.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 3),
    status: "read",
    relatedEntity: "RT-2024-15",
    actionUrl: "/interfaces/logistics/tracking"
  },
  {
    id: "6",
    type: "warning",
    title: "High Traffic Area",
    message: "Heavy traffic reported in downtown area. Routes may need adjustment.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4),
    status: "read",
    actionUrl: "/interfaces/logistics/tracking"
  },
  {
    id: "7",
    type: "error",
    title: "Vehicle Maintenance Required",
    message: "Delivery vehicle #VH-2024-007 has exceeded maintenance interval.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 6),
    status: "read",
    relatedEntity: "VH-2024-007"
  },
  {
    id: "8",
    type: "info",
    title: "New Warehouse Added",
    message: "Warehouse facility 'Distribution Center West' has been added to the network.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 8),
    status: "read",
    relatedEntity: "DC West",
    actionUrl: "/interfaces/logistics/warehouses"
  },
  {
    id: "9",
    type: "success",
    title: "Delivery KPI Target Met",
    message: "98% on-time delivery rate achieved this week. Exceeding target of 95%.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
    status: "read"
  },
]

export default function LogisticsNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>(mockNotifications)
  const [searchTerm, setSearchTerm] = useState("")
  const [filterType, setFilterType] = useState<string>("All")
  const [filterStatus, setFilterStatus] = useState<string>("All")

  // Filter notifications
  const filteredNotifications = notifications.filter((notification) => {
    const matchesSearch =
      notification.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      notification.message.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (notification.relatedEntity?.toLowerCase().includes(searchTerm.toLowerCase()) ?? false)

    const matchesType = filterType === "All" || notification.type === filterType
    const matchesStatus = filterStatus === "All" || notification.status === filterStatus

    return matchesSearch && matchesType && matchesStatus
  })

  // Calculate stats
  const unreadCount = notifications.filter(n => n.status === "unread").length
  const warningCount = notifications.filter(n => n.type === "warning").length
  const errorCount = notifications.filter(n => n.type === "error").length
  const successCount = notifications.filter(n => n.type === "success").length

  // Get icon for notification type
  const getTypeIcon = (type: NotificationType) => {
    switch (type) {
      case "success":
        return <CheckCircle className="h-5 w-5 text-green-600" />
      case "warning":
        return <AlertTriangle className="h-5 w-5 text-yellow-600" />
      case "error":
        return <XCircle className="h-5 w-5 text-red-600" />
      case "info":
      default:
        return <Info className="h-5 w-5 text-blue-600" />
    }
  }

  // Get badge color for notification type
  const getTypeBadge = (type: NotificationType) => {
    switch (type) {
      case "success":
        return "bg-green-100 text-green-800"
      case "warning":
        return "bg-yellow-100 text-yellow-800"
      case "error":
        return "bg-red-100 text-red-800"
      case "info":
      default:
        return "bg-blue-100 text-blue-800"
    }
  }

  // Format time ago
  const formatTimeAgo = (date: Date) => {
    const seconds = Math.floor((Date.now() - date.getTime()) / 1000)

    if (seconds < 60) return "Just now"
    if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`
    if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`

    return date.toLocaleDateString()
  }

  // Handler functions (static for now)
  const handleMarkAsRead = (id: string) => {
    setNotifications(notifications.map(n =>
      n.id === id ? { ...n, status: "read" as NotificationStatus } : n
    ))
  }

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, status: "read" as NotificationStatus })))
  }

  const handleDelete = (id: string) => {
    setNotifications(notifications.filter(n => n.id !== id))
  }

  const handleDeleteAll = () => {
    if (confirm("Are you sure you want to delete all notifications?")) {
      setNotifications([])
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Truck className="h-8 w-8 text-orange-600" />
            <h1 className="text-3xl font-bold text-black">Logistics Notifications</h1>
          </div>
          <p className="text-gray-600 mt-1">Tracking, routing, and fleet management alerts</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleMarkAllAsRead} disabled={unreadCount === 0}>
            <Check className="h-4 w-4 mr-2" />
            Mark All as Read
          </Button>
          <Button variant="outline" onClick={handleDeleteAll} disabled={notifications.length === 0}>
            <Trash2 className="h-4 w-4 mr-2" />
            Delete All
          </Button>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="flex gap-4 flex-wrap">
        <div className="relative flex-1 min-w-[250px]">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
          <Input
            type="text"
            placeholder="Search notifications..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          className="border border-gray-300 rounded-md px-4 py-2 bg-white text-black"
        >
          <option value="All">All Types</option>
          <option value="info">Info</option>
          <option value="success">Success</option>
          <option value="warning">Warning</option>
          <option value="error">Error</option>
        </select>
        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value)}
          className="border border-gray-300 rounded-md px-4 py-2 bg-white text-black"
        >
          <option value="All">All Status</option>
          <option value="unread">Unread</option>
          <option value="read">Read</option>
        </select>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <Bell className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-600">Unread</span>
          </div>
          <p className="text-2xl font-bold text-black">{unreadCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-sm font-medium text-gray-600">Success</span>
          </div>
          <p className="text-2xl font-bold text-black">{successCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <AlertTriangle className="h-4 w-4 text-yellow-600" />
            <span className="text-sm font-medium text-gray-600">Warnings</span>
          </div>
          <p className="text-2xl font-bold text-black">{warningCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center gap-2 mb-2">
            <XCircle className="h-4 w-4 text-red-600" />
            <span className="text-sm font-medium text-gray-600">Errors</span>
          </div>
          <p className="text-2xl font-bold text-black">{errorCount}</p>
        </div>
      </div>

      {/* Notifications Table */}
      <div className="bg-white rounded-lg border border-gray-200">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Status</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Notification</TableHead>
              <TableHead>Related</TableHead>
              <TableHead>Time</TableHead>
              <TableHead className="text-center">Mark Read</TableHead>
              <TableHead className="text-center">Delete</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredNotifications.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                  {notifications.length === 0
                    ? 'No notifications yet. Logistics alerts will appear here.'
                    : 'No notifications found matching your filters'}
                </TableCell>
              </TableRow>
            ) : (
              filteredNotifications.map((notification) => (
                <TableRow
                  key={notification.id}
                  className={`hover:bg-gray-50 ${notification.status === "unread" ? "bg-blue-50/30" : ""}`}
                >
                  <TableCell>
                    {notification.status === "unread" ? (
                      <Mail className="h-5 w-5 text-blue-600" />
                    ) : (
                      <MailOpen className="h-5 w-5 text-gray-400" />
                    )}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      {getTypeIcon(notification.type)}
                      <span className={`px-2 py-1 text-xs rounded-full ${getTypeBadge(notification.type)}`}>
                        {notification.type}
                      </span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div>
                      <div className={`font-medium text-sm ${notification.status === "unread" ? "text-black" : "text-gray-700"}`}>
                        {notification.title}
                      </div>
                      <div className="text-xs text-gray-600 line-clamp-1 max-w-md">
                        {notification.message}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    {notification.relatedEntity && (
                      <span className="px-2 py-1 text-xs rounded-full bg-orange-100 text-orange-800 font-mono">
                        {notification.relatedEntity}
                      </span>
                    )}
                  </TableCell>
                  <TableCell className="text-sm text-gray-600">
                    {formatTimeAgo(notification.timestamp)}
                  </TableCell>
                  <TableCell className="text-center">
                    {notification.status === "unread" && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleMarkAsRead(notification.id)}
                        title="Mark as read"
                      >
                        <Check className="h-4 w-4" />
                      </Button>
                    )}
                  </TableCell>
                  <TableCell className="text-center">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(notification.id)}
                      title="Delete"
                    >
                      <Trash2 className="h-4 w-4 text-red-600" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
