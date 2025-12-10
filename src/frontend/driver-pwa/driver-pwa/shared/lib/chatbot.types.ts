/**
 * Shared TypeScript types for chatbot functionality
 * These types match the backend DTOs for consistency
 */

/**
 * Represents a single chat message in the conversation
 */
export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string; // Time when message was sent
}

/**
 * Request payload sent to the chatbot API
 */
export interface ChatRequest {
  message: string;
  conversationHistory: ChatMessage[];
  locale?: string; // Defaults to "en-US" if not provided
}

/**
 * Response payload received from the chatbot API
 */
export interface ChatResponse {
  response: string;
  sourceDocuments: string[]; // IDs of chunks used for RAG context
}

/**
 * Represents a conversation with multiple messages
 */
export interface Conversation {
  id: number;
  title: string;
  messages: ChatMessage[];
  createdAt: string;
  lastMessageAt: string;
}

/**
 * Chatbot UI state
 */
export interface ChatbotState {
  isOpen: boolean;
  isTyping: boolean;
  hasNewMessage: boolean;
  conversations: Conversation[];
  currentConversationId: number;
  showHistory: boolean;
}
