/**
 * Order document service - handles MinIO-backed document interactions.
 */

import type { OrderDocument } from "@/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost";

/**
 * Get access token from cookies
 */
function getAccessToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  const cookies = document.cookie.split(';');
  const tokenCookie = cookies.find(cookie => cookie.trim().startsWith('access_token='));

  if (tokenCookie) {
    return tokenCookie.split('=')[1];
  }

  return null;
}

/**
 * Create auth headers using cookie-based token
 */
function getAuthHeaders(contentType?: string): HeadersInit {
  const token = getAccessToken();
  const headers: HeadersInit = contentType ? { "Content-Type": contentType } : {};

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  return headers;
}

async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message =
      (errorData as { message?: string }).message ??
      `HTTP ${response.status}: ${response.statusText}`;
    throw new Error(message);
  }

  // Handle 204 No Content gracefully
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const orderDocumentService = {
  async list(orderId: number): Promise<OrderDocument[]> {
    const headers = getAuthHeaders("application/json");
    const response = await fetch(
      `${API_BASE_URL}/api/customer/orders/${orderId}/documents`,
      {
        method: "GET",
        headers,
        credentials: 'include',
      },
    );
    return handleJsonResponse<OrderDocument[]>(response);
  },

  async download(orderId: number, documentId: string): Promise<Blob> {
    const headers = getAuthHeaders();
    const response = await fetch(
      `${API_BASE_URL}/api/customer/orders/${orderId}/documents/${encodeURIComponent(documentId)}/download`,
      {
        method: "GET",
        headers,
        credentials: 'include',
      },
    );
    if (!response.ok) {
      const text = await response.text().catch(() => "");
      const message = text || `HTTP ${response.status}: ${response.statusText}`;
      throw new Error(message);
    }
    return response.blob();
  },
};
