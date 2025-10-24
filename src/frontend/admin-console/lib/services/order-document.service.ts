/**
 * Order document service - handles MinIO-backed document interactions.
 */

import { getAccessToken } from "@/lib/shared-auth";
import type { OrderDocument } from "@/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "/api";

function getAuthHeaders(contentType?: string): HeadersInit {
  const headers: HeadersInit = contentType ? { "Content-Type": contentType } : {};

  const accessToken = getAccessToken();
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  } else {
    console.warn("No access token found in cookies or localStorage");
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
      `${API_BASE_URL}/warehouse/orders/${orderId}/documents`,
      {
        method: "GET",
        headers,
      },
    );
    return handleJsonResponse<OrderDocument[]>(response);
  },

  async download(orderId: number, documentId: string): Promise<Blob> {
    const headers = getAuthHeaders();
    const response = await fetch(
      `${API_BASE_URL}/warehouse/orders/${orderId}/documents/${encodeURIComponent(documentId)}/download`,
      {
        method: "GET",
        headers,
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
