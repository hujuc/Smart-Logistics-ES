"use client"

import { useState, useEffect, useRef } from "react"
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
  Warehouse,
  QrCode
} from "lucide-react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { useUser } from "../../../../shared/hooks/use-user"
import { useNotificationsStore } from "../../../../shared/hooks/use-notifications"

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

export default function WarehouseNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [searchTerm, setSearchTerm] = useState("")
  const [filterType, setFilterType] = useState<string>("All")
  const [filterStatus, setFilterStatus] = useState<string>("All")
  const user = useUser()
  const stompClientRef = useRef<Client | null>(null)
  const { setUnreadCount, incrementUnread, decrementUnread } = useNotificationsStore()

  // Fetch initial notifications
  useEffect(() => {
    if (user?.username) {
      fetch(`/api/notifications/${user.username}`)
        .then(async res => {
          if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`)
          }
          const text = await res.text()
          if (!text) {
            return []
          }
          return JSON.parse(text)
        })
        .then(data => {
          if (!Array.isArray(data)) {
            console.warn("Invalid data format, expected array:", data)
            return
          }
          const mappedNotifications: Notification[] = data.map((n: any) => ({
            id: n.id.toString(),
            type: mapEventTypeToType(n.eventType),
            title: formatTitle(n.eventType),
            message: n.message,
            timestamp: new Date(n.createdAt),
            status: (n.read ? "read" : "unread") as NotificationStatus,
            relatedEntity: n.aggregateId,
            actionUrl: getActionUrl(n.eventType, n.aggregateId)
          }))
          setNotifications(mappedNotifications)
          const unread = mappedNotifications.filter(n => n.status === "unread").length
          setUnreadCount(unread)
        })
        .catch(err => {
          console.error("Failed to fetch notifications:", err)
          setNotifications([])
        })
    }
  }, [user?.username, setUnreadCount])

  // WebSocket connection for real-time notifications
  useEffect(() => {
    if (!user?.username) return

    const socket = new SockJS("/ws-notifications")
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        username: user.username
      },
      onConnect: () => {
        console.log("✅ WebSocket connected for Warehouse notifications")
        client.subscribe("/user/queue/notifications", (message) => {
          console.log("📬 Received real-time notification:", message.body)
          const n = JSON.parse(message.body)
          const newNotification: Notification = {
            id: n.id.toString(),
            type: mapEventTypeToType(n.eventType),
            title: formatTitle(n.eventType),
            message: n.message,
            timestamp: new Date(n.createdAt),
            status: "unread",
            relatedEntity: n.aggregateId,
            actionUrl: getActionUrl(n.eventType, n.aggregateId)
          }
          setNotifications(prev => [newNotification, ...prev])
          incrementUnread()
        })
      },
      onStompError: (frame) => {
        console.error("❌ WebSocket error:", frame.headers["message"])
      }
    })

    client.activate()
    stompClientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [user?.username, incrementUnread])

  const mapEventTypeToType = (eventType: string): NotificationType => {
    switch (eventType) {
      case "SHIPMENT_PREPARED":
      case "INVENTORY_COUNT_COMPLETE":
      case "NEW_PACKAGE_RECEIVED":
        return "success"
      case "SCANNER_MALFUNCTION":
      case "PACKAGE_DAMAGED":
        return "error"
      case "LOW_STOCK_ALERT":
      case "MAINTENANCE_DUE":
        return "warning"
      case "NEW_CARRIER_RATE":
      default:
        return "info"
    }
  }

  const formatTitle = (eventType: string) => {
    return eventType.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
  }

  const getActionUrl = (eventType: string, aggregateId: string): string => {
    // Map event types to appropriate URLs
    if (eventType.includes("SCANNER")) {
      return `/interfaces/warehouse/qr-scanner`
    }
    if (eventType.includes("CARRIER")) {
      return `/interfaces/warehouse/carrier-comparison`
    }
    if (eventType.includes("PACKAGE")) {
      return `/interfaces/warehouse/package/${aggregateId}`
    }
    return `/interfaces/warehouse`
  }

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

  // Handler functions
  const handleMarkAsRead = (id: string) => {
    fetch(`/api/notifications/${id}/read`, { method: "PUT" })
      .then(() => {
        setNotifications(notifications.map(n =>
          n.id === id ? { ...n, status: "read" as NotificationStatus } : n
        ))
        decrementUnread()
      })
      .catch(err => console.error("Failed to mark as read:", err))
  }

  const handleMarkAllAsRead = () => {
    setNotifications(notifications.map(n => ({ ...n, status: "read" as NotificationStatus })))
    setUnreadCount(0)
  }

  const handleDelete = (id: string) => {
    fetch(`/api/notifications/${id}`, { method: "DELETE" })
      .then(() => {
        const notification = notifications.find(n => n.id === id)
        if (notification?.status === "unread") {
          decrementUnread()
        }
        setNotifications(notifications.filter(n => n.id !== id))
      })
      .catch(err => console.error("Failed to delete notification:", err))
  }

  const handleDeleteAll = () => {
    if (confirm("Are you sure you want to delete all notifications?")) {
      if (user?.username) {
        fetch(`/api/notifications/${user.username}`, { method: "DELETE" })
          .then(() => {
            setNotifications([])
            setUnreadCount(0)
          })
          .catch(err => console.error("Failed to delete all notifications:", err))
      }
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Warehouse className="h-8 w-8 text-purple-600" />
            <h1 className="text-3xl font-bold text-black">Warehouse Notifications</h1>
          </div>
          <p className="text-gray-600 mt-1">Alerts and updates for warehouse operations</p>
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
                    ? 'No notifications yet. Warehouse alerts will appear here.'
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
                      <span className="px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800 font-mono">
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
