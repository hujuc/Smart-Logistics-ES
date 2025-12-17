"use client"

import { useEffect, useRef } from "react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { useUser } from "../shared/hooks/use-user"
import { useNotificationsStore } from "../shared/hooks/use-notifications"

export function NotificationListener() {
    const user = useUser()
    const { setUnreadCount, incrementUnread } = useNotificationsStore()
    const stompClientRef = useRef<Client | null>(null)

    // Fetch initial notifications count
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
                    // Calculate unread count
                    const unread = data.filter((n: any) => !n.read).length
                    setUnreadCount(unread)
                })
                .catch(err => {
                    console.error("Failed to fetch notifications:", err)
                })
        }
    }, [user?.username, setUnreadCount])

    // WebSocket connection
    useEffect(() => {
        if (!user?.username) return

        const socket = new SockJS("/ws-notifications")
        const client = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                username: user.username
            },
            onConnect: () => {
                client.subscribe("/user/queue/notifications", (message) => {
                    // We just need to increment the count, we don't need to parse the message content
                    // unless we want to show a toast notification in the future
                    incrementUnread()
                })
            },
            onStompError: (frame) => {
                console.error("WebSocket error:", frame.headers["message"])
            }
        })

        client.activate()
        stompClientRef.current = client

        return () => {
            client.deactivate()
        }
    }, [user?.username, incrementUnread])

    return null // This component doesn't render anything
}
