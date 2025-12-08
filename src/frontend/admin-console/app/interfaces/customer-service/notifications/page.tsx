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
  MessageSquare,
  Users,
  FileText
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

// Mock data for static UI - Customer Service-specific notifications
const mockNotifications: Notification[] = [
  {
    id: "1",
    type: "warning",
    title: "High Priority Ticket",
    message: "Ticket #TKT-789 marked as high priority. Customer requesting immediate assistance.",
    timestamp: new Date(Date.now() - 1000 * 60 * 10),
    status: "unread",
    relatedEntity: "TKT-789",
    actionUrl: "/interfaces/customer-service/tickets"
  },
  {
    id: "2",
    type: "error",
    title: "Escalated Complaint",
    message: "Customer complaint #CMP-456 has been escalated to management. Requires immediate attention.",
    timestamp: new Date(Date.now() - 1000 * 60 * 30),
    status: "unread",
    relatedEntity: "CMP-456",
    actionUrl: "/interfaces/customer-service/tickets"
  },
  {
    id: "3",
    type: "success",
    title: "Ticket Resolved",
    message: "Support ticket #TKT-745 has been successfully resolved. Customer satisfaction: 5/5.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60),
    status: "read",
    relatedEntity: "TKT-745",
    actionUrl: "/interfaces/customer-service/tickets"
  },
  {
    id: "4",
    type: "info",
    title: "New Ticket Assigned",
    message: "Support ticket #TKT-892 has been automatically assigned to your queue.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
    status: "unread",
    relatedEntity: "TKT-892",
    actionUrl: "/interfaces/customer-service/tickets"
  },
  {
    id: "5",
    type: "warning",
    title: "SLA Warning",
    message: "Ticket #TKT-623 is approaching SLA deadline. Response required within 2 hours.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 3),
    status: "read",
    relatedEntity: "TKT-623",
    actionUrl: "/interfaces/customer-service/tickets"
  },
  {
    id: "6",
    type: "info",
    title: "Customer Feedback Received",
    message: "New feedback received for order #ORD-2024-1234. Rating: 4/5 stars.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4),
    status: "read",
    relatedEntity: "ORD-2024-1234",
    actionUrl: "/interfaces/customer-service/order-lookup"
  },
  {
    id: "7",
    type: "success",
    title: "Refund Processed",
    message: "Refund request for order #ORD-2024-0987 has been successfully processed.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 6),
    status: "read",
    relatedEntity: "ORD-2024-0987",
    actionUrl: "/interfaces/customer-service/order-lookup"
  },
  {
    id: "8",
    type: "error",
    title: "Failed Delivery Follow-up",
    message: "Customer for failed delivery #DEL-2024-555 has not responded to contact attempts.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 8),
    status: "read",
    relatedEntity: "DEL-2024-555",
    actionUrl: "/interfaces/customer-service/order-lookup"
  },
  {
    id: "9",
    type: "info",
    title: "Weekly Report Available",
    message: "Customer service weekly performance report is now available for review.",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
    status: "read",
    actionUrl: "/interfaces/customer-service/reports"
  },
  {
    id: "10",
    type: "success",
    title: "Customer Satisfaction Goal Met",
    message: "Team achieved 95% customer satisfaction rate this week. Great job!",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 48),
    status: "read",
    actionUrl: "/interfaces/customer-service/reports"
  },
]

export default function CustomerServiceNotificationsPage() {
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
            <MessageSquare className="h-8 w-8 text-cyan-600" />
            <h1 className="text-3xl font-bold text-black">Customer Service Notifications</h1>
          </div>
          <p className="text-gray-600 mt-1">Support tickets, customer feedback, and service alerts</p>
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
                    ? 'No notifications yet. Customer service alerts will appear here.'
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
                      <span className="px-2 py-1 text-xs rounded-full bg-cyan-100 text-cyan-800 font-mono">
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
