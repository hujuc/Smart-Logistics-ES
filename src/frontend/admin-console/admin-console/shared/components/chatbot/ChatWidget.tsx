/**
 * ChatWidget Component
 *
 * Main chatbot widget component with floating button, chat window, and conversation history.
 * Matches the design from the original ChatBot component with black theme and lucide-react icons.
 */

'use client';

import React from 'react';
import { MessageCircle, X, Clock, Plus, Trash2 } from 'lucide-react';
import { useChatbot } from '../../hooks/use-chatbot';
import { ChatWindow } from './ChatWindow';
import type { Conversation } from '../../lib/chatbot.types';

interface ChatWidgetProps {
  apiEndpoint: string;
  locale?: string;
  persistHistory?: boolean;
}

export function ChatWidget({ apiEndpoint, locale = 'en-US', persistHistory = true }: ChatWidgetProps) {
  const {
    isOpen,
    isTyping,
    hasNewMessage,
    conversations,
    currentConversationId,
    showHistory,
    error,
    setIsOpen,
    setShowHistory,
    sendMessage,
    createNewConversation,
    deleteConversation,
    switchConversation,
  } = useChatbot({
    apiEndpoint,
    locale,
    persistHistory,
  });

  const currentConversation = conversations.find((c) => c.id === currentConversationId);
  const messages = currentConversation?.messages || [];

  /**
   * Format the conversation time for display (e.g., "5m ago", "2h ago")
   */
  const formatConversationTime = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <>
      {/* Chat Panel */}
      {isOpen && (
        <div className="fixed bottom-6 right-6 z-50">
          {/* Conversation History Sidebar */}
          {showHistory && (
            <div className="absolute bottom-0 right-96 w-64 h-[600px] border-r border-gray-200 flex flex-col bg-gray-50 rounded-tl-2xl rounded-bl-2xl border-t border-l border-b">
              {/* New Chat Button */}
              <div className="p-4 border-b border-gray-200">
                <button
                  onClick={createNewConversation}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors text-sm font-medium"
                  aria-label="Create new conversation"
                >
                  <Plus className="h-4 w-4" />
                  New Chat
                </button>
              </div>

              {/* Conversation List */}
              <div className="flex-1 overflow-y-auto p-2">
                <div className="text-xs font-semibold text-gray-500 px-2 py-1 mb-1">
                  Chat History
                </div>
                {conversations.map((conv: Conversation) => (
                  <div
                    key={conv.id}
                    className={`group relative p-3 rounded-lg mb-1 cursor-pointer transition-colors ${
                      conv.id === currentConversationId
                        ? 'bg-white border border-gray-300'
                        : 'hover:bg-gray-100'
                    }`}
                    onClick={() => switchConversation(conv.id)}
                    role="button"
                    aria-label={`Switch to conversation: ${conv.title}`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        <div className="text-sm font-medium text-gray-900 truncate">
                          {conv.title}
                        </div>
                        <div className="flex items-center gap-1 text-xs text-gray-500 mt-1">
                          <Clock className="h-3 w-3" />
                          {formatConversationTime(conv.lastMessageAt)}
                        </div>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteConversation(conv.id);
                        }}
                        className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-50 rounded transition-opacity"
                        aria-label={`Delete conversation: ${conv.title}`}
                      >
                        <Trash2 className="h-3.5 w-3.5 text-red-500" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Main Chat Window */}
          <ChatWindow
            messages={messages}
            isLoading={isTyping}
            error={error}
            onSendMessage={sendMessage}
            onClose={() => setIsOpen(false)}
            onToggleHistory={() => setShowHistory(!showHistory)}
            onNewConversation={createNewConversation}
            showHistory={showHistory}
          />
        </div>
      )}

      {/* Floating Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 h-14 w-14 bg-black text-white rounded-full shadow-2xl hover:bg-gray-800 transition-all duration-300 flex items-center justify-center z-40 hover:scale-110"
        aria-label={isOpen ? 'Close chat' : 'Open chat'}
      >
        {hasNewMessage && !isOpen && (
          <div className="absolute -top-1 -right-1 h-5 w-5 bg-red-500 rounded-full border-2 border-white flex items-center justify-center">
            <span className="text-xs font-bold">1</span>
          </div>
        )}
        {isOpen ? <X className="h-6 w-6" /> : <MessageCircle className="h-6 w-6" />}
      </button>
    </>
  );
}
