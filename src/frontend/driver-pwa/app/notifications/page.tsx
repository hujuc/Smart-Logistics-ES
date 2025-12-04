"use client"

import { useState } from "react"
import {
  Bell,
  CheckCircle,
  AlertTriangle,
  Info,
  XCircle,
  Check,
  Trash2,
  Mail,
  MailOpen,
  ArrowLeft,
  Search,
  Truck
} from "lucide-react"
import Link from "next/link"

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

// Mock data for static UI - Driver notifications
const mockNotifications: Notification[] = [
  {
    id: "1",
    type: "info",
    title: "New Delivery Assignment",
    message: "You have been assigned 8 new deliveries for today. Check your daily schedule.",
    timestamp: new Date(Date.now() - 1000 * 60 * 15),
    status: "unread",
    relatedEntity: "Today's Route",
    actionUrl: "/interfaces/daily-schedule"
  },
  {
    id: "2",
    type: "warning",
    title: "Route Update",
    message: "Traffic detected on your route. Consider using the alternative path suggested in navigation.",
    timestamp: new Date(Date.now() - 1000 * 60 * 45),
    status: "unread",
    actionUrl: "/interfaces/navigation"
  },
  {
    id: "3",
    type: "success",
    title: "Delivery Confirmed",
    message: "Package #PKG-2024-1234 delivery has been confirmed by the customer.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60),
    status: "read",
    relatedEntity: "PKG-2024-1234"
  },
  {
    id: "4",
    type: "info",
    title: "Customer Note Added",
    message: "Customer at 123 Main St added special delivery instructions. Please review before delivery.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
    status: "read",
    relatedEntity: "123 Main St",
    actionUrl: "/interfaces/daily-schedule"
  },
  {
    id: "5",
    type: "error",
    title: "Delivery Issue",
    message: "Unable to complete delivery at 456 Oak Ave. Customer not available. Please contact support.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 3),
    status: "read",
    relatedEntity: "456 Oak Ave"
  },
  {
    id: "6",
    type: "success",
    title: "Route Completed",
    message: "All morning deliveries completed successfully. Great work!",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 5),
    status: "read"
  },
  {
    id: "7",
    type: "info",
    title: "Schedule Change",
    message: "Your afternoon route has been updated with 2 additional stops.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
    status: "read",
    actionUrl: "/interfaces/daily-schedule"
  },
]

export default function NotificationsPage() {
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
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`
    if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`

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
    <div className="min-h-screen bg-white">
      {/* Header - Black background like homepage */}
      <div className="bg-black text-white p-6">
        <div className="flex items-center gap-3 mb-4">
          <Link href="/" className="text-white hover:text-gray-300">
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <Truck className="h-6 w-6 text-white" />
              <h1 className="text-2xl md:text-3xl font-bold">Driver Notifications</h1>
            </div>
            <p className="text-gray-400 text-sm md:text-base">Route updates and delivery alerts</p>
          </div>
        </div>

        {/* Action buttons */}
        <div className="flex gap-2">
          <button
            onClick={handleMarkAllAsRead}
            disabled={unreadCount === 0}
            className="flex-1 md:flex-none inline-flex items-center justify-center px-3 md:px-4 py-2 border-2 border-white rounded-lg text-sm font-semibold text-white hover:bg-white hover:text-black transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent disabled:hover:text-white"
          >
            <Check className="h-4 w-4 md:mr-2" />
            <span className="hidden md:inline">Mark All Read</span>
          </button>
          <button
            onClick={handleDeleteAll}
            disabled={notifications.length === 0}
            className="flex-1 md:flex-none inline-flex items-center justify-center px-3 md:px-4 py-2 border-2 border-white rounded-lg text-sm font-semibold text-white hover:bg-white hover:text-black transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-transparent disabled:hover:text-white"
          >
            <Trash2 className="h-4 w-4 md:mr-2" />
            <span className="hidden md:inline">Delete All</span>
          </button>
        </div>
      </div>

      <div className="p-4 md:p-6">

      {/* Search and Filters */}
      <div className="flex flex-col md:flex-row gap-2 md:gap-4 mb-4 md:mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
          <input
            type="text"
            placeholder="Search notifications..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md bg-white text-black text-sm"
          />
        </div>
        <div className="flex gap-2">
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="flex-1 md:flex-none border border-gray-300 rounded-md px-3 py-2 bg-white text-black text-sm"
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
            className="flex-1 md:flex-none border border-gray-300 rounded-md px-3 py-2 bg-white text-black text-sm"
          >
            <option value="All">All Status</option>
            <option value="unread">Unread</option>
            <option value="read">Read</option>
          </select>
        </div>
      </div>

      {/* Stats Cards - similar style to homepage */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4 mb-4 md:mb-6">
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-3 md:p-4">
          <Bell className="w-6 h-6 md:w-8 md:h-8 text-black mb-2" />
          <p className="text-2xl md:text-3xl font-bold text-black">{unreadCount}</p>
          <p className="text-xs md:text-sm text-gray-600">Unread</p>
        </div>
        <div className="bg-green-50 border border-green-200 rounded-lg p-3 md:p-4">
          <div className="w-6 h-6 md:w-8 md:h-8 bg-green-500 rounded-full flex items-center justify-center mb-2">
            <span className="text-white font-bold text-xs md:text-sm">✓</span>
          </div>
          <p className="text-2xl md:text-3xl font-bold text-green-700">{successCount}</p>
          <p className="text-xs md:text-sm text-gray-600">Success</p>
        </div>
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 md:p-4">
          <AlertTriangle className="w-6 h-6 md:w-8 md:h-8 text-yellow-600 mb-2" />
          <p className="text-2xl md:text-3xl font-bold text-yellow-700">{warningCount}</p>
          <p className="text-xs md:text-sm text-gray-600">Warnings</p>
        </div>
        <div className="bg-red-50 border border-red-200 rounded-lg p-3 md:p-4">
          <XCircle className="w-6 h-6 md:w-8 md:h-8 text-red-600 mb-2" />
          <p className="text-2xl md:text-3xl font-bold text-red-700">{errorCount}</p>
          <p className="text-xs md:text-sm text-gray-600">Errors</p>
        </div>
      </div>

      {/* Notifications - Desktop Table / Mobile Cards */}
      <div className="space-y-3">
        {/* Desktop Table - hidden on mobile */}
        <div className="hidden md:block bg-white rounded-lg border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Notification</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Related</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Time</th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Mark Read</th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Delete</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredNotifications.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                      {notifications.length === 0
                        ? 'No notifications yet. Route alerts will appear here.'
                        : 'No notifications found matching your filters'}
                    </td>
                  </tr>
                ) : (
                  filteredNotifications.map((notification) => (
                    <tr
                      key={notification.id}
                      className={`hover:bg-gray-50 ${notification.status === "unread" ? "bg-gray-100" : ""}`}
                    >
                      <td className="px-4 py-3">
                        {notification.status === "unread" ? (
                          <Mail className="h-5 w-5 text-black" />
                        ) : (
                          <MailOpen className="h-5 w-5 text-gray-400" />
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          {getTypeIcon(notification.type)}
                          <span className={`px-2 py-1 text-xs rounded-full ${getTypeBadge(notification.type)}`}>
                            {notification.type}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <div>
                          <div className={`font-medium text-sm ${notification.status === "unread" ? "text-black" : "text-gray-700"}`}>
                            {notification.title}
                          </div>
                          <div className="text-xs text-gray-600 line-clamp-1 max-w-md">
                            {notification.message}
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        {notification.relatedEntity && (
                          <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800 font-mono">
                            {notification.relatedEntity}
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {formatTimeAgo(notification.timestamp)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        {notification.status === "unread" && (
                          <button
                            onClick={() => handleMarkAsRead(notification.id)}
                            className="p-1 hover:bg-gray-100 rounded"
                            title="Mark as read"
                          >
                            <Check className="h-4 w-4" />
                          </button>
                        )}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <button
                          onClick={() => handleDelete(notification.id)}
                          className="p-1 hover:bg-gray-100 rounded"
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4 text-red-600" />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Mobile Cards - hidden on desktop */}
        <div className="md:hidden space-y-2">
          {filteredNotifications.length === 0 ? (
            <div className="bg-white rounded-lg p-8 text-center border border-gray-200">
              <Bell className="h-12 w-12 text-gray-400 mx-auto mb-3" />
              <p className="text-gray-500 text-sm">
                {notifications.length === 0
                  ? 'No notifications yet. Route alerts will appear here.'
                  : 'No notifications found matching your filters'}
              </p>
            </div>
          ) : (
            filteredNotifications.map((notification) => (
              <div
                key={notification.id}
                className={`bg-white rounded-lg border ${
                  notification.status === "unread"
                    ? "border-gray-800 bg-gray-100"
                    : "border-gray-200"
                } p-4`}
              >
                <div className="flex items-start gap-3">
                  <div className="flex-shrink-0 mt-1">
                    {notification.status === "unread" ? (
                      <Mail className="h-5 w-5 text-black" />
                    ) : (
                      <MailOpen className="h-5 w-5 text-gray-400" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2">
                        {getTypeIcon(notification.type)}
                        <span className={`px-2 py-0.5 text-xs rounded-full ${getTypeBadge(notification.type)}`}>
                          {notification.type}
                        </span>
                      </div>
                      <span className="text-xs text-gray-500 flex-shrink-0">
                        {formatTimeAgo(notification.timestamp)}
                      </span>
                    </div>

                    <h3 className={`text-sm font-semibold mb-1 ${
                      notification.status === "unread" ? "text-black" : "text-gray-700"
                    }`}>
                      {notification.title}
                    </h3>

                    <p className="text-sm text-gray-600 mb-2">
                      {notification.message}
                    </p>

                    {notification.relatedEntity && (
                      <span className="inline-block px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800 font-mono mb-3">
                        {notification.relatedEntity}
                      </span>
                    )}

                    <div className="flex items-center gap-3 pt-2 border-t border-gray-200">
                      {notification.status === "unread" && (
                        <button
                          onClick={() => handleMarkAsRead(notification.id)}
                          className="text-xs text-black font-semibold"
                        >
                          Mark as read
                        </button>
                      )}
                      {notification.actionUrl && (
                        <Link
                          href={notification.actionUrl}
                          className="text-xs text-black font-semibold underline"
                        >
                          View details
                        </Link>
                      )}
                      <button
                        onClick={() => handleDelete(notification.id)}
                        className="text-xs text-red-600 font-medium ml-auto"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
      </div>
    </div>
  )
}
