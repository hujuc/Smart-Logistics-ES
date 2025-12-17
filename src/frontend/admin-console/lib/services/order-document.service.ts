/**
 * Order document service - handles MinIO-backed document interactions.
 * Uses API_ENDPOINTS from api.ts for proper BFF endpoint configuration
 */

import { API_ENDPOINTS, createAuthHeaders } from "@/lib/api";
import { getUserRole } from "@/lib/shared-auth";
import type { OrderDocument } from "@/types";

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
    const headers = createAuthHeaders();
    const response = await fetch(
      API_ENDPOINTS.warehouse.orderDocuments(orderId),
      {
        method: "GET",
        headers,
        credentials: "include",
      },
    );
    return handleJsonResponse<OrderDocument[]>(response);
  },

  async download(orderId: number, documentId: string): Promise<Blob> {
    const headers = createAuthHeaders(''); // No content-type for blob download
    const response = await fetch(
      API_ENDPOINTS.warehouse.orderDocumentDownload(orderId, documentId),
      {
        method: "GET",
        headers,
        credentials: "include",
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
