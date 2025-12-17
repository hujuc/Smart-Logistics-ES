"use client"

import { useEffect } from "react"
import { useUser } from "@shared/hooks/use-user"
import { useNotificationsStore } from "@shared/hooks/use-notifications"
import { useWebSocket } from "@shared/hooks/use-websocket"
import { getAccessToken } from "@shared/lib/auth"

export function NotificationListener() {
    const user = useUser()
    const { setUnreadCount, incrementUnread } = useNotificationsStore()

    // Fetch initial unread count
    useEffect(() => {
        async function fetchUnreadCount() {
            if (!user?.username) return;
            try {
                const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/notifications/${user.username}/unread`, {
                    headers: {
                        Authorization: `Bearer ${getAccessToken()}`
                    }
                });
                if (response.ok) {
                    const data = await response.json();
                    // The API /unread returns a list of unread notifications, so length is the count
                    setUnreadCount(data.length);
                }
            } catch (error) {
                console.error("Failed to fetch unread notifications:", error);
            }
        }
        fetchUnreadCount();
    }, [user?.username, setUnreadCount]);

    // WebSocket for real-time updates
    useWebSocket({
        onMessage: () => {
            incrementUnread();
        }
    });

    return null
}
