/**
 * ChatMessage Component
 *
 * Displays a single chat message (user or assistant).
 * Matches the design from the original ChatBot component.
 */

'use client';

import React from 'react';
import type { ChatMessage as ChatMessageType } from '../../lib/chatbot.types';

interface ChatMessageProps {
  message: ChatMessageType;
}

export function ChatMessage({ message }: ChatMessageProps) {
  const isBot = message.role === 'assistant';

  return (
    <div
      className={`flex ${isBot ? 'justify-start' : 'justify-end'} mb-4`}
      role="article"
      aria-label={`${isBot ? 'Assistant message' : 'Your message'}`}
    >
      <div
        className={`max-w-[80%] rounded-lg px-4 py-2 ${
          isBot
            ? 'bg-gray-100 text-gray-900'
            : 'bg-black text-white'
        }`}
      >
        <p className="text-sm leading-relaxed whitespace-pre-line">{message.content}</p>
        {message.timestamp && (
          <span className="text-xs opacity-60 mt-1 block">
            {message.timestamp}
          </span>
        )}
      </div>
    </div>
  );
}
