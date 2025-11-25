/**
 * ChatWindow Component
 *
 * Displays the chat conversation window with messages and input field.
 * Matches the original design with black header and SLMS branding.
 */

'use client';

import React, { useEffect, useRef } from 'react';
import { X, Package, Clock, Plus } from 'lucide-react';
import { ChatMessage } from './ChatMessage';
import { ChatInput } from './ChatInput';
import type { ChatMessage as ChatMessageType } from '../../lib/chatbot.types';

interface ChatWindowProps {
  messages: ChatMessageType[];
  isLoading: boolean;
  error: string | null;
  onSendMessage: (message: string) => void;
  onClose: () => void;
  onToggleHistory: () => void;
  onNewConversation: () => void;
  showHistory: boolean;
}

export function ChatWindow({
  messages,
  isLoading,
  error,
  onSendMessage,
  onClose,
  onToggleHistory,
  onNewConversation,
  showHistory,
}: ChatWindowProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  return (
    <div
      className="w-96 h-[600px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col"
      role="dialog"
      aria-label="Chat window"
    >
      {/* Header */}
      <div
        className={`bg-black text-white px-6 py-4 flex items-center justify-between ${
          showHistory ? 'rounded-tr-2xl' : 'rounded-t-2xl'
        }`}
      >
        <div className="flex items-center gap-3">
          <button
            onClick={onToggleHistory}
            className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
            title="Conversation history"
            aria-label="Toggle conversation history"
          >
            <Clock className="h-5 w-5" />
          </button>
          <div className="h-10 w-10 bg-white rounded-full flex items-center justify-center">
            <Package className="h-6 w-6 text-black" />
          </div>
          <div>
            <h3 className="font-semibold">SLMS Support</h3>
            <div className="flex items-center gap-1">
              <div className="h-2 w-2 bg-green-400 rounded-full"></div>
              <span className="text-xs text-gray-300">Online</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={onNewConversation}
            className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
            title="New conversation"
            aria-label="Start new conversation"
          >
            <Plus className="h-5 w-5" />
          </button>
          <button
            onClick={onClose}
            className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
            aria-label="Close chat"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
      </div>

      {/* Messages Area */}
      <div
        className="flex-1 overflow-y-auto p-4 bg-gray-50"
        style={{ minHeight: 0 }}
      >
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-center text-gray-500">
            <Package className="w-12 h-12 mb-4 text-gray-400" />
            <p className="text-sm">
              Hello! I'm your AI assistant.
              <br />
              How can I help you today?
            </p>
          </div>
        ) : (
          <>
            {messages.map((msg, index) => (
              <ChatMessage key={index} message={msg} />
            ))}
            {error && (
              <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
                {error}
              </div>
            )}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Input Area */}
      <ChatInput onSendMessage={onSendMessage} isLoading={isLoading} />
    </div>
  );
}
