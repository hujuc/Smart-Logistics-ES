/**
 * useChatbot Hook
 *
 * Manages chatbot state, multiple conversations, and API interactions.
 * Provides conversation management with real backend integration.
 */

'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { sendChatMessage } from '../lib/chatbot-api';
import type { ChatMessage, ChatRequest, Conversation } from '../lib/chatbot.types';

const STORAGE_KEY_PREFIX = 'slms_chatbot_state_';
const MAX_CONVERSATIONS = 50;

interface UseChatbotOptions {
  apiEndpoint: string;
  locale?: string;
  persistHistory?: boolean;
  skipAuth?: boolean;
}

interface UseChatbotReturn {
  // State
  isOpen: boolean;
  isTyping: boolean;
  hasNewMessage: boolean;
  conversations: Conversation[];
  currentConversationId: number;
  showHistory: boolean;
  error: string | null;

  // Actions
  setIsOpen: (open: boolean) => void;
  setShowHistory: (show: boolean) => void;
  sendMessage: (message: string) => Promise<void>;
  createNewConversation: () => void;
  deleteConversation: (id: number) => void;
  switchConversation: (id: number) => void;
}

const getInitialMessages = (): ChatMessage[] => [
  {
    role: 'assistant',
    content: 'Welcome to Smart Logistics Management System! 👋 I\'m here to help you with your logistics needs.',
    timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
  }
];

export function useChatbot({
  apiEndpoint,
  locale = 'en-US',
  persistHistory = true,
  skipAuth = false,
}: UseChatbotOptions): UseChatbotReturn {

  const storageKey = `${STORAGE_KEY_PREFIX}${apiEndpoint.replace(/\//g, '_')}`;
  const isInitialized = useRef(false);

  // State
  const [isOpen, setIsOpen] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [hasNewMessage, setHasNewMessage] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [conversations, setConversations] = useState<Conversation[]>([
    {
      id: Date.now(),
      title: 'New Conversation',
      messages: getInitialMessages(),
      createdAt: new Date().toISOString(),
      lastMessageAt: new Date().toISOString()
    }
  ]);
  const [currentConversationId, setCurrentConversationId] = useState(Date.now());

  // Load state from localStorage on mount
  useEffect(() => {
    if (!persistHistory || isInitialized.current || typeof window === 'undefined') return;
    isInitialized.current = true;

    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.conversations && Array.isArray(parsed.conversations)) {
          setConversations(parsed.conversations);
          setCurrentConversationId(parsed.currentConversationId || parsed.conversations[0]?.id);
          setIsOpen(parsed.isOpen || false);
        }
      }
    } catch (err) {
      console.error('Failed to load chatbot state:', err);
    }
  }, [storageKey, persistHistory]);

  // Save state to localStorage
  useEffect(() => {
    if (!persistHistory || typeof window === 'undefined') return;

    try {
      const state = {
        isOpen,
        conversations: conversations.slice(0, MAX_CONVERSATIONS),
        currentConversationId
      };
      localStorage.setItem(storageKey, JSON.stringify(state));
    } catch (err) {
      console.error('Failed to save chatbot state:', err);
    }
  }, [isOpen, conversations, currentConversationId, storageKey, persistHistory]);

  // New message notification (show after 5 seconds if chat is closed)
  useEffect(() => {
    const timer = setTimeout(() => {
      if (!isOpen) {
        setHasNewMessage(true);
      }
    }, 5000);

    return () => clearTimeout(timer);
  }, [isOpen]);

  /**
   * Send a message to the chatbot and receive a response
   */
  const sendMessage = useCallback(
    async (message: string) => {
      if (!message.trim() || isTyping) return;

      setError(null);
      setIsTyping(true);

      const timestamp = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

      // Add user message
      const userMessage: ChatMessage = {
        role: 'user',
        content: message.trim(),
        timestamp
      };

      setConversations((prev) =>
        prev.map((conv) =>
          conv.id === currentConversationId
            ? {
                ...conv,
                messages: [...conv.messages, userMessage],
                lastMessageAt: new Date().toISOString(),
                title: conv.messages.length <= 1
                  ? message.substring(0, 30) + (message.length > 30 ? '...' : '')
                  : conv.title
              }
            : conv
        )
      );

      try {
        // Get current conversation history (without the user message we just added)
        const currentConv = conversations.find(c => c.id === currentConversationId);
        const historyWithoutCurrent = (currentConv?.messages || []).filter(m => m.timestamp !== timestamp);

        // Prepare request
        const request: ChatRequest = {
          message: message.trim(),
          conversationHistory: historyWithoutCurrent.map(m => ({
            role: m.role,
            content: m.content
          })),
          locale,
        };

        // Call real API
        const response = await sendChatMessage(apiEndpoint, request, skipAuth);

        // Add assistant response
        const assistantMessage: ChatMessage = {
          role: 'assistant',
          content: response.response,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };

        setConversations((prev) =>
          prev.map((conv) =>
            conv.id === currentConversationId
              ? {
                  ...conv,
                  messages: [...conv.messages, assistantMessage],
                  lastMessageAt: new Date().toISOString()
                }
              : conv
          )
        );
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred';
        setError(errorMessage);

        // Add error message to conversation
        const errorAssistantMessage: ChatMessage = {
          role: 'assistant',
          content: `Sorry, I encountered an error: ${errorMessage}. Please try again.`,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };

        setConversations((prev) =>
          prev.map((conv) =>
            conv.id === currentConversationId
              ? { ...conv, messages: [...conv.messages, errorAssistantMessage] }
              : conv
          )
        );
      } finally {
        setIsTyping(false);
      }
    },
    [conversations, currentConversationId, apiEndpoint, locale, isTyping]
  );

  /**
   * Create a new conversation
   */
  const createNewConversation = useCallback(() => {
    const newConv: Conversation = {
      id: Date.now(),
      title: 'New Conversation',
      messages: getInitialMessages(),
      createdAt: new Date().toISOString(),
      lastMessageAt: new Date().toISOString()
    };
    setConversations((prev) => [newConv, ...prev]);
    setCurrentConversationId(newConv.id);
    setShowHistory(false);
  }, []);

  /**
   * Delete a conversation
   */
  const deleteConversation = useCallback(
    (id: number) => {
      if (conversations.length === 1) {
        // If deleting the last conversation, create a new one
        const newId = Date.now();
        setConversations([
          {
            id: newId,
            title: 'New Conversation',
            messages: getInitialMessages(),
            createdAt: new Date().toISOString(),
            lastMessageAt: new Date().toISOString()
          }
        ]);
        setCurrentConversationId(newId);
      } else {
        setConversations((prev) => prev.filter((c) => c.id !== id));
        if (currentConversationId === id) {
          const remaining = conversations.filter((c) => c.id !== id);
          setCurrentConversationId(remaining[0].id);
        }
      }
    },
    [conversations, currentConversationId]
  );

  /**
   * Switch to a different conversation
   */
  const switchConversation = useCallback((id: number) => {
    setCurrentConversationId(id);
    setShowHistory(false);
  }, []);

  return {
    isOpen,
    isTyping,
    hasNewMessage,
    conversations,
    currentConversationId,
    showHistory,
    error,
    setIsOpen: (open: boolean) => {
      setIsOpen(open);
      if (open) {
        setHasNewMessage(false);
      }
    },
    setShowHistory,
    sendMessage,
    createNewConversation,
    deleteConversation,
    switchConversation,
  };
}
