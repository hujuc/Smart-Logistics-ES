/**
 * E2E Test Data Setup Utilities
 * 
 * This module provides helper functions to create test data via API calls
 * before running E2E tests. It authenticates as a logistics manager and
 * creates warehouses, orders, and other necessary entities.
 */

const BASE_URL = process.env.E2E_BASE_URL || "http://localhost";
const API_URL = `${BASE_URL}/api`;
// Keycloak is accessible through nginx reverse proxy
const KEYCLOAK_URL = `${BASE_URL}/realms/slms/protocol/openid-connect/token`;

// Test credentials for logistics manager (has permissions to create orders/warehouses)
const TEST_CREDENTIALS = {
  client_id: "slms-backend",
  client_secret: "GFHMOgIh9ZeWgpLlazG60Kb8FOR9semS",
  username: "logistics_manager",
  password: "manager",
  grant_type: "password",
};

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: string;
}

export interface WarehouseResponse {
  id: number;
  code: string;
  name: string;
  addressLine1: string;
  city: string;
  stateOrProvince: string;
  country: string;
  postalCode: string;
  active: boolean;
}

export interface OrderResponse {
  id: number;
  reference: string;
  description: string;
  status: string;
  shippingAddress: string;
  estimatedDeliveryDate: string;
  warehouseId: number;
  trackingNumber?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Get access token from Keycloak for API authentication
 */
export async function getAccessToken(): Promise<string> {
  const body = new URLSearchParams(TEST_CREDENTIALS);

  const response = await fetch(KEYCLOAK_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: body.toString(),
  });

  if (!response.ok) {
    throw new Error(
      `Failed to get access token: ${response.status} ${response.statusText}`
    );
  }

  const data: TokenResponse = await response.json();
  return data.access_token;
}

/**
 * Get list of existing orders (for E2E tests we'll reuse existing data)
 * Note: /api/admin/** endpoints are configured as permitAll() in SecurityConfig
 */
export async function getOrders(): Promise<OrderResponse[]> {
  const url = `${API_URL}/admin/orders`;
  console.log(`Fetching orders from: ${url}`);
  
  const response = await fetch(url, {
    method: "GET",
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to get orders: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  return await response.json();
}

/**
 * Create a test warehouse
 * Note: /api/admin/** endpoints are configured as permitAll() in SecurityConfig
 */
export async function createTestWarehouse(): Promise<WarehouseResponse> {
  const timestamp = Date.now();
  const warehouseData = {
    code: `WH-TEST-${timestamp}`,
    name: `Test Warehouse ${timestamp}`,
    addressLine1: "Rua de Teste 123",
    city: "Aveiro",
    stateOrProvince: "Aveiro",
    country: "Portugal",
    postalCode: "3800-123",
    active: true,
  };

  const response = await fetch(`${API_URL}/admin/warehouses`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      // No Authorization header needed for /api/admin/** endpoints
    },
    body: JSON.stringify(warehouseData),
  });

  if (!response.ok) {
    throw new Error(
      `Failed to create warehouse: ${response.status} ${response.statusText}`
    );
  }

  return await response.json();
}

/**
 * Get list of warehouses (to reuse existing ones if available)
 * Note: /api/admin/** endpoints are configured as permitAll() in SecurityConfig
 */
export async function getWarehouses(): Promise<WarehouseResponse[]> {
  const url = `${API_URL}/admin/warehouses`;
  console.log(`Fetching warehouses from: ${url}`);
  
  const response = await fetch(url, {
    method: "GET",
    // No Authorization header needed for /api/admin/** endpoints
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to get warehouses: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  return await response.json();
}

/**
 * Create a test order with specific properties
 * Note: /api/admin/** endpoints are configured as permitAll() in SecurityConfig
 */
export async function createTestOrder(
  warehouseId: number,
  options?: {
    reference?: string;
    description?: string;
    status?: string;
    shippingAddress?: string;
    trackingNumber?: string;
  }
): Promise<OrderResponse> {
  const timestamp = Date.now();
  const orderData = {
    reference: options?.reference || `ORD-TEST-${timestamp}`,
    description: options?.description || "Test order for E2E tests",
    status: options?.status || "CREATED",
    shippingAddress:
      options?.shippingAddress ||
      "Rua do Cliente 456, 4000-123 Porto, Portugal",
    estimatedDeliveryDate: "2025-12-31",
    warehouseId: warehouseId,
    trackingNumber: options?.trackingNumber,
  };

  const response = await fetch(`${API_URL}/admin/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(orderData),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to create order: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  return await response.json();
}

/**
 * Setup test data: uses existing warehouses and orders from database
 * Note: In a real scenario, we would create fresh test data, but due to fetch API
 * limitations with Spring Security, we're reusing existing data for E2E tests.
 */
export async function setupTestData(): Promise<{
  warehouse: WarehouseResponse;
  orders: OrderResponse[];
}> {
  console.log("Setting up test data...");

  // Get authentication token (verification purposes)
  const token = await getAccessToken();
  console.log("✓ Authentication token obtained");

  // Get existing warehouses
  let warehouses = await getWarehouses();
  if (warehouses.length === 0) {
    throw new Error("No warehouses found in database. Please seed data first.");
  }
  const warehouse = warehouses[0];
  console.log(`✓ Using existing warehouse: ${warehouse.name} (ID: ${warehouse.id})`);

  // Get existing orders
  let orders = await getOrders();
  if (orders.length < 3) {
    throw new Error(`Not enough orders in database (found ${orders.length}, need at least 3). Please seed data first.`);
  }
  
  // Use the first 3 orders for testing
  const testOrders = orders.slice(0, 3);
  console.log(`✓ Using ${testOrders.length} existing orders for tests:`);
  testOrders.forEach((order, index) => {
    console.log(`  Order ${index + 1}: ID=${order.id}, Ref=${order.reference}, Status=${order.status}`);
  });

  console.log(`Test data setup complete!`);

  return {
    warehouse,
    orders: testOrders,
  };
}

/**
 * Cleanup test data (optional - can be used in afterAll)
 * Note: /api/admin/** endpoints are configured as permitAll() in SecurityConfig
 */
export async function cleanupTestData(
  orderIds: number[]
): Promise<void> {
  console.log("Cleaning up test data...");

  for (const orderId of orderIds) {
    try {
      const response = await fetch(`${API_URL}/admin/orders/${orderId}`, {
        method: "DELETE",
        // No Authorization header needed for /api/admin/** endpoints
      });

      if (response.ok) {
        console.log(`✓ Deleted order ${orderId}`);
      } else {
        console.warn(`⚠ Could not delete order ${orderId}: ${response.status}`);
      }
    } catch (error) {
      console.warn(`⚠ Error deleting order ${orderId}:`, error);
    }
  }

  console.log("Cleanup complete!");
}
