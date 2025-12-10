import { useEffect, useRef, useState } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useUserStore } from '../lib/user-store';
import { getAccessToken } from '../lib/auth';

interface WebSocketOptions {
    onMessage: (message: any) => void;
    onConnect?: () => void;
    onDisconnect?: () => void;
}

export function useWebSocket({ onMessage, onConnect, onDisconnect }: WebSocketOptions) {
    const clientRef = useRef<Client | null>(null);
    const subscriptionRef = useRef<StompSubscription | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const user = useUserStore((state) => state.user);

    useEffect(() => {
        if (!user?.username) return;

        const socketUrl = process.env.NEXT_PUBLIC_API_URL
            ? `${process.env.NEXT_PUBLIC_API_URL}/ws-notifications`
            : 'http://localhost:8080/ws-notifications';

        const client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            connectHeaders: {
                Authorization: `Bearer ${getAccessToken()}`,
                username: user.username,
            },
            debug: (str) => {
                console.log('[WebSocket]', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = (frame) => {
            console.log('Connected to WebSocket');
            setIsConnected(true);
            onConnect?.();

            // Subscribe to user-specific notifications
            // The backend configures /user/queue/notifications
            subscriptionRef.current = client.subscribe('/user/queue/notifications', (message) => {
                try {
                    const body = JSON.parse(message.body);
                    onMessage(body);
                } catch (e) {
                    console.error('Failed to parse notification:', e);
                }
            });
        };

        client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };

        client.onWebSocketClose = () => {
            console.log('WebSocket connection closed');
            setIsConnected(false);
            onDisconnect?.();
        };

        client.activate();
        clientRef.current = client;

        return () => {
            if (subscriptionRef.current) {
                subscriptionRef.current.unsubscribe();
            }
            if (clientRef.current) {
                clientRef.current.deactivate();
            }
        };
    }, [user?.username]);

    return { isConnected };
}
