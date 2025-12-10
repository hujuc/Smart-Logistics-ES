/**
 * Chatbot API client for making requests to the backend chatbot endpoints
 */

import { getAccessToken } from './auth';
import type { ChatRequest, ChatResponse } from './chatbot.types';

/**
 * Send a chat message to the chatbot API
 *
 * @param endpoint The BFF endpoint URL (e.g., "/api/landing-page/chat")
 * @param request The chat request payload
 * @param skipAuth If true, skip sending Authorization header (for anonymous users)
 * @returns Promise resolving to the chatbot response
 * @throws Error if the request fails
 */
export async function sendChatMessage(
  endpoint: string,
  request: ChatRequest,
  skipAuth: boolean = false
): Promise<ChatResponse> {
  try {
    const token = skipAuth ? null : getAccessToken();

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify(request),
      credentials: 'include', // Include cookies for refresh token
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(
        `Chat request failed: ${response.status} ${response.statusText}. ${errorText}`
      );
    }

    const chatResponse: ChatResponse = await response.json();
    return chatResponse;
  } catch (error) {
    console.error('Error sending chat message:', error);
    throw error;
  }
}

/**
 * Check if the chatbot service is available
 *
 * @param endpoint The BFF endpoint URL
 * @returns Promise resolving to true if available, false otherwise
 */
export async function isChatbotAvailable(endpoint: string): Promise<boolean> {
  try {
    const response = await fetch(endpoint, {
      method: 'OPTIONS',
    });
    return response.ok;
  } catch (error) {
    console.error('Error checking chatbot availability:', error);
    return false;
  }
}
