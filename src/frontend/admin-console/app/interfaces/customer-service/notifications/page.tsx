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
  MessageSquare
} from "lucide-react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { useUser } from "../../../../shared/hooks/use-user"
import { useNotificationsStore } from "../../../../shared/hooks/use-notifications"
import { useNotificationAnimation } from "../../../../shared/hooks/use-notification-animation"

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

export default function CustomerServiceNotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [searchTerm, setSearchTerm] = useState("")
  const [filterType, setFilterType] = useState<string>("All")
  const [filterStatus, setFilterStatus] = useState<string>("All")
  const user = useUser()
  const stompClientRef = useRef<Client | null>(null)
  const { setUnreadCount, incrementUnread, decrementUnread } = useNotificationsStore()

  // Calculate unread count for display and animation
  const unreadCount = notifications.filter(n => n.status === "unread").length
  const shouldAnimate = useNotificationAnimation(unreadCount)

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
          // Unread count is handled by global listener
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
        console.log("✅ WebSocket connected for Customer Service notifications")
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
          // Unread count is handled by global listener
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
      case "TICKET_RESOLVED":
      case "TICKET_CLOSED":
      case "CUSTOMER_FEEDBACK":
        return "success"
      case "SLA_BREACH":
      case "TICKET_ESCALATED":
      case "NEW_HIGH_PRIORITY_TICKET":
        return "error"
      case "SLA_WARNING":
        return "warning"
      case "TICKET_ASSIGNED":
      case "NEW_TICKET":
      default:
        return "info"
    }
  }

  const formatTitle = (eventType: string) => {
    return eventType.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
  }

  const getActionUrl = (eventType: string, aggregateId: string): string => {
    // For ticket-related notifications, link to the ticket details page
    if (eventType.includes("TICKET") || eventType.includes("SLA")) {
      return `/interfaces/customer-service/tickets`
    }
    return `/interfaces/customer-service/tickets`
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
  // Calculate stats
  // const unreadCount = notifications.filter(n => n.status === "unread").length
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
    if (user?.username) {
      fetch(`/api/notifications/customer/${user.username}/read`, { method: "PUT" })
        .then(() => {
          setNotifications(notifications.map(n => ({ ...n, status: "read" as NotificationStatus })))
          setUnreadCount(0)
        })
        .catch(err => console.error("Failed to mark all as read:", err))
    }
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
        fetch(`/api/notifications/customer/${user.username}`, { method: "DELETE" })
          .then(() => {
            setNotifications([])
            setUnreadCount(0)
          })
          .catch(err => console.error("Failed to delete all notifications:", err))
      }
    }
  }



  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center gap-2 mb-2 relative">
          <div className="relative">
            <MessageSquare className="h-6 w-6 text-blue-600" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white animate-in fade-in zoom-in duration-300">
                {unreadCount > 9 ? '9+' : unreadCount}
                {shouldAnimate && (
                  <span className="absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75 animate-ping"></span>
                )}
              </span>
            )}
          </div>
          <h1 className="text-2xl font-bold text-black">Customer Service Notifications</h1>
        </div>
        <p className="text-gray-600">Support tickets, customer feedback, and service alerts</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white border rounded-lg p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Unread</p>
              <p className="text-2xl font-bold text-black">{unreadCount}</p>
            </div>
            <Bell className="h-8 w-8 text-blue-600" />
          </div>
        </div>

        <div className="bg-white border rounded-lg p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Success</p>
              <p className="text-2xl font-bold text-green-600">{successCount}</p>
            </div>
            <CheckCircle className="h-8 w-8 text-green-600" />
          </div>
        </div>

        <div className="bg-white border rounded-lg p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Warnings</p>
              <p className="text-2xl font-bold text-yellow-600">{warningCount}</p>
            </div>
            <AlertTriangle className="h-8 w-8 text-yellow-600" />
          </div>
        </div>

        <div className="bg-white border rounded-lg p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Errors</p>
              <p className="text-2xl font-bold text-red-600">{errorCount}</p>
            </div>
            <XCircle className="h-8 w-8 text-red-600" />
          </div>
        </div>
      </div>

      {/* Filters and Actions */}
      <div className="bg-white border rounded-lg shadow-sm mb-6">
        <div className="p-4 border-b">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
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
              className="px-3 py-2 border rounded-md bg-white text-black"
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
              className="px-3 py-2 border rounded-md bg-white text-black"
            >
              <option value="All">All Status</option>
              <option value="unread">Unread</option>
              <option value="read">Read</option>
            </select>

            <Button onClick={handleMarkAllAsRead} variant="outline" className="whitespace-nowrap">
              <Check className="h-4 w-4 mr-2" />
              Mark All as Read
            </Button>

            <Button onClick={handleDeleteAll} variant="outline" className="whitespace-nowrap text-red-600 hover:text-red-700">
              <Trash2 className="h-4 w-4 mr-2" />
              Delete All
            </Button>
          </div>
        </div>

        {/* Notifications Table */}
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-12">Status</TableHead>
              <TableHead className="w-12">Type</TableHead>
              <TableHead>Notification</TableHead>
              <TableHead className="w-32">Related</TableHead>
              <TableHead className="w-40">Time</TableHead>
              <TableHead className="w-32">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredNotifications.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-gray-500">
                  {notifications.length === 0 ? "No notifications yet" : "No notifications match your filters"}
                </TableCell>
              </TableRow>
            ) : (
              filteredNotifications.map((notification) => (
                <TableRow
                  key={notification.id}
                  className={notification.status === "unread" ? "bg-blue-50" : ""}
                >
                  <TableCell>
                    {notification.status === "unread" ? (
                      <Mail className="h-5 w-5 text-blue-600" />
                    ) : (
                      <MailOpen className="h-5 w-5 text-gray-400" />
                    )}
                  </TableCell>
                  <TableCell>{getTypeIcon(notification.type)}</TableCell>
                  <TableCell>
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className={`text-xs px-2 py-1 rounded-full font-medium ${getTypeBadge(notification.type)}`}>
                          {notification.title}
                        </span>
                        {notification.status === "unread" && (
                          <span className="h-2 w-2 bg-blue-600 rounded-full"></span>
                        )}
                      </div>
                      <p className="text-sm text-gray-700">{notification.message}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    {notification.relatedEntity && (
                      <span className="text-xs text-gray-600 bg-gray-100 px-2 py-1 rounded">
                        {notification.relatedEntity.substring(0, 8)}
                      </span>
                    )}
                  </TableCell>
                  <TableCell className="text-sm text-gray-600">
                    {formatTimeAgo(notification.timestamp)}
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      {notification.status === "unread" && (
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleMarkAsRead(notification.id)}
                          title="Mark as read"
                        >
                          <Check className="h-4 w-4" />
                        </Button>
                      )}
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleDelete(notification.id)}
                        className="text-red-600 hover:text-red-700"
                        title="Delete"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Legend */}
      <div className="bg-gray-50 border rounded-lg p-4">
        <h3 className="text-sm font-semibold text-black mb-2">Notification Types</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
          <div className="flex items-center gap-2">
            <Info className="h-4 w-4 text-blue-600" />
            <span className="text-gray-700">New assignments, general info</span>
          </div>
          <div className="flex items-center gap-2">
            <CheckCircle className="h-4 w-4 text-green-600" />
            <span className="text-gray-700">Resolved tickets, feedback</span>
          </div>
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-yellow-600" />
            <span className="text-gray-700">SLA warnings, escalations</span>
          </div>
          <div className="flex items-center gap-2">
            <XCircle className="h-4 w-4 text-red-600" />
            <span className="text-gray-700">High priority, SLA breach</span>
          </div>
        </div>
      </div>
    </div>
  )
}
