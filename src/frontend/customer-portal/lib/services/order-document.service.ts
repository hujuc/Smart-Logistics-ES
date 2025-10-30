/**
 * Order document service - handles MinIO-backed document interactions.
 */

import { getSession } from "next-auth/react";
import type { OrderDocument } from "@/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "/api";

async function getAuthHeaders(contentType?: string): Promise<HeadersInit> {
  const session = await getSession();
  const headers: HeadersInit = contentType ? { "Content-Type": contentType } : {};

  if (session?.accessToken) {
    headers["Authorization"] = `Bearer ${session.accessToken}`;
  } else {
    console.warn("No access token found in session");
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
    const headers = await getAuthHeaders("application/json");
    const response = await fetch(
      `${API_BASE_URL}/admin/orders/${orderId}/documents`,
      {
        method: "GET",
        headers,
      },
    );
    return handleJsonResponse<OrderDocument[]>(response);
  },

  async download(orderId: number, documentId: string): Promise<Blob> {
    const headers = await getAuthHeaders();
    const response = await fetch(
      `${API_BASE_URL}/admin/orders/${orderId}/documents/${encodeURIComponent(documentId)}/download`,
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
