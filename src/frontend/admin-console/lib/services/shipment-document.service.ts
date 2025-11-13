import { API_ENDPOINTS, createAuthHeaders } from "@/lib/api";
import type { ShipmentDocument } from "@/types";

async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    const message = (payload as { message?: string }).message ?? `HTTP ${response.status}: ${response.statusText}`;
    throw new Error(message);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const shipmentDocumentService = {
  async list(orderId: number): Promise<ShipmentDocument[]> {
    const headers = createAuthHeaders();
    const response = await fetch(API_ENDPOINTS.warehouse.orderShipmentDocuments(orderId), {
      method: "GET",
      headers,
      credentials: "include",
    });
    return handleJsonResponse<ShipmentDocument[]>(response);
  },

  async download(orderId: number, documentId: string): Promise<Blob> {
    const headers = createAuthHeaders("");
    const response = await fetch(
      API_ENDPOINTS.warehouse.orderShipmentDocumentDownload(orderId, documentId),
      {
        method: "GET",
        headers,
        credentials: "include",
      },
    );
    if (!response.ok) {
      const message = await response.text().catch(() => "Failed to download document");
      throw new Error(message);
    }
    return response.blob();
  },
};
