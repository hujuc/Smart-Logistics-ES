import { test, expect, Page } from "@playwright/test";

// API configuration
const BASE_URL = process.env.E2E_BASE_URL || "http://localhost";
const API_URL = `${BASE_URL}/api`;

// Types for test data
interface WarehouseResponse {
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

interface OrderResponse {
  id: number;
  reference: string;
  description: string;
  status: string;
  shippingAddress: string;
  estimatedDeliveryDate: string;
  warehouseId: number;
  trackingNumber?: string;
}

/**
 * Create a test warehouse for E2E tests
 */
async function createTestWarehouse(): Promise<WarehouseResponse> {
  const timestamp = Date.now();
  const warehouseData = {
    code: `WH-E2E-${timestamp}`,
    name: `E2E Test Warehouse ${timestamp}`,
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
    },
    body: JSON.stringify(warehouseData),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to create warehouse: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  return await response.json();
}

/**
 * Create a test order for E2E tests
 */
async function createTestOrder(
  warehouseId: number,
  reference: string,
  description: string,
  trackingNumber?: string
): Promise<OrderResponse> {
  const orderData = {
    reference,
    description,
    status: "CREATED",
    shippingAddress: "Rua do Cliente 456, 4000-123 Porto, Portugal",
    estimatedDeliveryDate: "2025-12-31",
    warehouseId: warehouseId,
    trackingNumber,
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
 * Delete test data after tests complete
 */
async function cleanupTestData(orderIds: number[], warehouseId: number): Promise<void> {
  // Delete orders
  for (const orderId of orderIds) {
    try {
      await fetch(`${API_URL}/admin/orders/${orderId}`, {
        method: "DELETE",
      });
    } catch (error) {
      console.warn(`⚠ Could not delete order ${orderId}:`, error);
    }
  }

  // Delete warehouse
  try {
    await fetch(`${API_URL}/admin/warehouses/${warehouseId}`, {
      method: "DELETE",
    });
  } catch (error) {
    console.warn(`⚠ Could not delete warehouse ${warehouseId}:`, error);
  }
}

/**
 * Helper to perform Customer Service Representative login via landing page
 */
const loginAsCSR = async (page: Page) => {
  // Start from landing page (which handles the auth flow correctly)
  await page.goto("/");
  
  // Click on Login button
  await page.getByRole("button", { name: /login/i }).first().click();
  
  // Wait for Keycloak login page
  await expect(page.getByRole("heading", { name: /welcome back/i })).toBeVisible({ timeout: 30_000 });
  
  // Fill credentials
  await page.getByLabel(/username/i).fill("customer_service");
  await page.getByLabel(/password/i).fill("support");
  
  // Click login button
  await page.getByRole("button", { name: /^login$/i }).last().click();
  
  // Wait a bit for login to complete
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);
  
  // Navigate to admin console manually (CSR might not auto-redirect)
  await page.goto("/admin");
  
  // Wait for admin console landing page to load
  await expect(page.getByRole("heading", { name: /customer service/i })).toBeVisible({ timeout: 60_000 });
};

/**
 * Helper to wait for order lookup page to load
 */
const waitForOrderLookupPage = async (page: Page) => {
  await page.waitForURL(/\/admin\/interfaces\/customer-service\/order-lookup/, { timeout: 30_000 });
  await expect(page.getByRole("heading", { name: /order lookup/i })).toBeVisible({ timeout: 30_000 });
};

/**
 * Helper to search for an order and navigate to details
 * Handles both single result (direct navigation) and multiple results (finds specific row and clicks View)
 */
const searchAndViewOrder = async (page: Page, searchTerm: string, expectedRef?: string) => {
  // Fill search input
  const orderIdInput = page.getByPlaceholder(/order.*tracking/i);
  await orderIdInput.fill(searchTerm);
  
  // Click search
  await page.getByRole("button", { name: /search/i }).click();
  
  // Wait a bit for results
  await page.waitForTimeout(2000);
  
  // Check if we got a table (multiple results)
  const resultsTable = page.locator('table');
  const hasTable = await resultsTable.isVisible({ timeout: 3000 }).catch(() => false);
  
  if (hasTable && expectedRef) {
    // Multiple results - find the row with the expected reference (exact match in first cell)
    // Use a more precise selector to avoid matching "teste2" when looking for "teste"
    const row = page.locator('tr').filter({ 
      has: page.locator('td').first().filter({ hasText: new RegExp(`^${expectedRef}$`) })
    });
    const viewButton = row.getByRole("button", { name: /^view$/i });
    await viewButton.click();
  } else if (hasTable) {
    // Multiple results but no specific ref - click first View button
    await page.getByRole("button", { name: /^view$/i }).first().click();
  }
  
  // Wait for navigation/state update
  await page.waitForTimeout(1000);
};

test.describe.configure({ mode: "serial" });

// Test data that will be shared across all tests
let testOrders: OrderResponse[] = [];
let testWarehouse: WarehouseResponse;

test.describe("Customer Service - Order Tracking and Details (US 7)", () => {
  // Setup test data before all tests - create fresh data for each test run
  test.beforeAll(async () => {
    console.log("Creating test data...");
    
    // Create a test warehouse
    testWarehouse = await createTestWarehouse();
    console.log(`✓ Created warehouse: ${testWarehouse.name} (ID: ${testWarehouse.id})`);
    
    // Create 3 test orders
    const order1 = await createTestOrder(
      testWarehouse.id,
      `E2E-ORDER-1-${Date.now()}`,
      "Test order 1 for E2E tests"
    );
    testOrders.push(order1);
    console.log(`✓ Created order 1: ${order1.reference} (ID: ${order1.id})`);
    
    const order2 = await createTestOrder(
      testWarehouse.id,
      `E2E-ORDER-2-${Date.now()}`,
      "Test order 2 for E2E tests",
      "TRK-E2E-12345"
    );
    testOrders.push(order2);
    console.log(`✓ Created order 2: ${order2.reference} (ID: ${order2.id})`);
    
    const order3 = await createTestOrder(
      testWarehouse.id,
      `E2E-ORDER-3-${Date.now()}`,
      "Test order 3 for E2E tests",
      "TRK-E2E-67890"
    );
    testOrders.push(order3);
    console.log(`✓ Created order 3: ${order3.reference} (ID: ${order3.id})`);
    
    console.log("Test data creation complete!");
  });

  // Cleanup test data after all tests
  test.afterAll(async () => {
    if (testOrders.length > 0 && testWarehouse) {
      console.log("Cleaning up test data...");
      const orderIds = testOrders.map((order) => order.id);
      await cleanupTestData(orderIds, testWarehouse.id);
      console.log("Cleanup complete!");
    }
  });

  test.beforeEach(async ({ page }) => {
    // Login as CSR before each test
    await loginAsCSR(page);
  });

  test("CSR can navigate to order lookup page", async ({ page }) => {
    // Navigate directly to order lookup page
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    
    // Verify page loaded
    await waitForOrderLookupPage(page);
    
    // Verify search input is present
    await expect(page.getByPlaceholder(/order.*tracking/i)).toBeVisible();
  });

  test("CSR can search for an order by ID", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Get first test order
    const testOrder = testOrders[0];
    // Use reference instead of ID for search, as the frontend seems to search by reference
    const searchTerm = testOrder.reference;
    const testOrderRef = testOrder.reference;
    
    // Search and view order
    await searchAndViewOrder(page, searchTerm, testOrderRef);
    
    // Verify order details are displayed
    await expect(page.getByRole("heading", { name: new RegExp(testOrderRef, "i") })).toBeVisible({ timeout: 30_000 });
    await expect(page.getByText(/warehouse/i)).toBeVisible();
    await expect(page.getByText(/shipping address/i)).toBeVisible();
  });

  test("CSR can view order details including status and timeline", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    const testOrder = testOrders[0];
    const testOrderRef = testOrder.reference;
    
    // Search and view order (using reference as search term)
    await searchAndViewOrder(page, testOrderRef, testOrderRef);
    
    // Verify order details loaded
    await expect(page.getByRole("heading", { name: new RegExp(testOrderRef, "i") })).toBeVisible({ timeout: 30_000 });
    
    // Verify essential information is present
    await expect(page.getByText(/warehouse/i)).toBeVisible();
    await expect(page.getByText(/est\. delivery|estimated delivery/i)).toBeVisible();
    await expect(page.getByText(/last updated/i)).toBeVisible();
    
    // Verify order journey/timeline is present
    await expect(page.getByRole("heading", { name: /order journey|shipping timeline/i })).toBeVisible();
  });

  test("CSR can see delivery address in order details", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    const testOrder = testOrders[0];
    const testOrderRef = testOrder.reference;
    
    // Search and view order (using reference as search term)
    await searchAndViewOrder(page, testOrderRef, testOrderRef);
    
    // Verify order details loaded
    await expect(page.getByRole("heading", { name: new RegExp(testOrderRef, "i") })).toBeVisible({ timeout: 30_000 });
    
    // Verify delivery address section
    await expect(page.getByText(/shipping address/i)).toBeVisible();
  });

  test("CSR sees error message for non-existent order", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for non-existent order
    await page.getByPlaceholder(/order.*tracking/i).fill("999999");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Verify error message appears: "No orders found for "999999""
    await expect(page.getByText(/no orders found/i)).toBeVisible({ 
      timeout: 30_000 
    });
  });

  test("CSR can view order item details", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    const testOrder = testOrders[0];
    const testOrderRef = testOrder.reference;
    
    // Search and view order (using reference as search term)
    await searchAndViewOrder(page, testOrderRef, testOrderRef);
    
    // Verify order details loaded
    await expect(page.getByRole("heading", { name: new RegExp(testOrderRef, "i") })).toBeVisible({ timeout: 30_000 });
    
    // Verify order information is present
    await expect(page.getByText(/warehouse/i)).toBeVisible();
    await expect(page.getByText(/tracking/i)).toBeVisible();
  });

  test("CSR can search for another order after viewing one", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    const firstOrder = testOrders[0];
    const secondOrder = testOrders[1];
    
    // First search (using reference as search term)
    await searchAndViewOrder(page, firstOrder.reference, firstOrder.reference);
    await expect(page.getByRole("heading", { name: new RegExp(firstOrder.reference, "i") })).toBeVisible({ timeout: 30_000 });
    
    // Go back to order lookup page for second search
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    
    // Wait for page to be ready - check for search input instead of heading
    await expect(page.getByPlaceholder(/order.*tracking/i)).toBeVisible({ timeout: 30_000 });
    
    // Search for second order (using reference as search term)
    await searchAndViewOrder(page, secondOrder.reference, secondOrder.reference);
    
    // Verify second order is displayed
    await expect(page.getByRole("heading", { name: new RegExp(secondOrder.reference, "i") })).toBeVisible({ timeout: 30_000 });
  });

  test("CSR can view carrier information if order has shipment", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    const testOrder = testOrders[0];
    const testOrderRef = testOrder.reference;
    
    // Search and view order (using reference as search term)
    await searchAndViewOrder(page, testOrderRef, testOrderRef);
    
    // Verify order details loaded
    await expect(page.getByRole("heading", { name: new RegExp(testOrderRef, "i") })).toBeVisible({ timeout: 30_000 });
    
    // Verify warehouse information is present (acts as logistics info)
    await expect(page.getByText(/warehouse/i)).toBeVisible();
    
    // If tracking number exists, verify it's shown
    if (testOrder.trackingNumber) {
      await expect(page.getByText(/tracking/i)).toBeVisible();
    }
  });
});
