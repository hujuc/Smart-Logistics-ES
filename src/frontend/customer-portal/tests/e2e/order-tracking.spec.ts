import { test, expect, type Page } from "@playwright/test";

/**
 * E2E Tests for Customer Portal - Order Tracking and Details (US)
 * 
 * User Story: As a customer, I want to track my orders so that I can see their current status and delivery information.
 * 
 * These tests cover:
 * 1. Viewing order history
 * 2. Searching for orders
 * 3. Viewing order details
 * 4. Viewing order timeline/events
 * 5. Error handling
 */

// Test data will be created fresh for each test run
let testWarehouseId: number;
let testOrderIds: number[] = [];
let testOrders: Array<{
  id: number;
  reference: string;
  trackingNumber: string;
  description: string;
}> = [];

// Helper function to create a test warehouse
async function createTestWarehouse(): Promise<number> {
  const timestamp = Date.now();
  const response = await fetch("http://localhost/api/admin/warehouses", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name: `E2E Test Warehouse ${timestamp}`,
      code: `E2E-WH-${timestamp}`,
      addressLine1: "Test Street 123",
      city: "Porto",
      stateOrProvince: "Porto",
      postalCode: "4000-001",
      country: "Portugal",
      active: true,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to create warehouse: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  const warehouse = await response.json();
  console.log(`✓ Created warehouse: ${warehouse.name} (ID: ${warehouse.id})`);
  return warehouse.id;
}

// Helper function to create a test order
async function createTestOrder(
  warehouseId: number,
  reference: string,
  description: string,
  customerId: string,
  trackingNumber?: string
): Promise<{ id: number; reference: string; trackingNumber: string; description: string }> {
  const response = await fetch("http://localhost/api/admin/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reference,
      description,
      customerId, // Add customerId to associate order with customer
      trackingNumber: trackingNumber || `TRK-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      status: "CREATED",
      warehouseId,
      shippingAddress: "Customer Street 456, Porto, 4100-001, Portugal",
      recipientName: "Test Customer",
      recipientPhone: "+351912345678",
      recipientEmail: "customer@test.com",
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to create order: ${response.status} ${response.statusText} - ${errorText}`
    );
  }

  const order = await response.json();
  console.log(`✓ Created order: ${order.reference} (ID: ${order.id}, Tracking: ${order.trackingNumber})`);
  return {
    id: order.id,
    reference: order.reference,
    trackingNumber: order.trackingNumber,
    description: order.description,
  };
}

// Helper function to cleanup test data
async function cleanupTestData(orderIds: number[], warehouseId: number) {
  console.log("Cleaning up test data...");
  
  // Delete orders first (due to foreign key constraints)
  for (const orderId of orderIds) {
    try {
      // First, delete any order events
      await fetch(`http://localhost/api/admin/orders/${orderId}/events`, {
        method: "DELETE",
      });
      
      // Then delete the order
      await fetch(`http://localhost/api/admin/orders/${orderId}`, {
        method: "DELETE",
      });
    } catch (error) {
      console.warn(`Failed to delete order ${orderId}:`, error);
    }
  }
  
  // Delete warehouse
  try {
    await fetch(`http://localhost/api/admin/warehouses/${warehouseId}`, {
      method: "DELETE",
    });
  } catch (error) {
    console.warn(`Failed to delete warehouse ${warehouseId}:`, error);
  }
  
  console.log("Cleanup complete!");
}

// Helper function to login as customer
async function loginAsCustomer(page: Page) {
  // Navigate to landing page
  await page.goto("/");
  await page.waitForLoadState("networkidle");

  // Click on the first Login button (in nav) to open modal
  await page.getByRole("button", { name: /^login$/i }).first().click();
  
  // Wait for modal to appear
  await page.waitForSelector('input[id="username"]', { timeout: 10000 });

  // Fill login form
  await page.locator('input[id="username"]').fill("customer");
  await page.locator('input[id="password"]').fill("customer");
  
  // Submit login - click the button inside the modal
  await page.locator('form').getByRole("button", { name: /^login$/i }).click();
  
  // Wait for redirect to customer portal
  await page.waitForURL(/\/customer/, { timeout: 10000 });
}

// Helper function to extract customerId from JWT token
async function getCustomerIdFromToken(page: Page): Promise<string> {
  // Get the access token from localStorage (set by middleware after successful login)
  const accessToken = await page.evaluate(() => {
    return localStorage.getItem("access_token");
  });

  if (!accessToken) {
    throw new Error("No access token found in localStorage");
  }

  // Decode JWT to get the subject (customerId)
  const payload = JSON.parse(atob(accessToken.split('.')[1]));
  return payload.sub; // 'sub' claim contains the Keycloak user UUID
}

test.describe.configure({ mode: "serial" });

test.describe("Customer Portal - Order Tracking and Details (US)", () => {
  test.beforeAll(async ({ browser }) => {
    console.log("Creating test data...");
    const timestamp = Date.now();
    
    // Create a temporary page to login and get customerId
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // Login to get the customerId from JWT
    await loginAsCustomer(page);
    const customerId = await getCustomerIdFromToken(page);
    console.log(`✓ Got customerId from JWT: ${customerId}`);
    
    // Close temporary page
    await context.close();
    
    // Create warehouse
    testWarehouseId = await createTestWarehouse();
    
    // Create 3 test orders with different statuses
    const order1 = await createTestOrder(
      testWarehouseId,
      `E2E-CUST-1-${timestamp}`,
      "Test Package 1 - Electronics",
      customerId,
      `TRACK-CUST-1-${timestamp}`
    );
    
    const order2 = await createTestOrder(
      testWarehouseId,
      `E2E-CUST-2-${timestamp}`,
      "Test Package 2 - Books",
      customerId,
      `TRACK-CUST-2-${timestamp}`
    );
    
    const order3 = await createTestOrder(
      testWarehouseId,
      `E2E-CUST-3-${timestamp}`,
      "Test Package 3 - Clothing",
      customerId,
      `TRACK-CUST-3-${timestamp}`
    );
    
    testOrders = [order1, order2, order3];
    testOrderIds = testOrders.map(o => o.id);
    
    console.log("Test data creation complete!");
  });

  test("Customer can navigate to order history page", async ({ page }) => {
    await loginAsCustomer(page);
    
    // Navigate to order history
    await page.getByRole("link", { name: /order.*history/i }).click();
    
    // Verify we're on the order history page
    await expect(page).toHaveURL(/\/interfaces\/support\/history/);
    await expect(page.getByRole("heading", { name: /order history/i })).toBeVisible();
  });

  test("Customer can view their order history", async ({ page }) => {
    await loginAsCustomer(page);
    await page.goto("/customer/interfaces/support/history");
    
    // Wait for orders to load
    await page.waitForSelector("table", { timeout: 10000 });
    
    // Check if at least one of our test orders is visible
    const orderVisible = await page.locator("table").locator(`text=${testOrders[0].reference}`).isVisible()
      .catch(() => false);
    
    expect(orderVisible).toBeTruthy();
  });

  test("Customer can search for an order by tracking number", async ({ page }) => {
    await loginAsCustomer(page);
    await page.goto("/customer/interfaces/support/history");
    
    // Wait for page to load completely
    await page.waitForLoadState('networkidle');
    
    // Wait for orders to load
    await page.waitForSelector("table", { timeout: 10000 });
    
    const searchOrder = testOrders[0];
    
    // Search by tracking number
    await page.getByPlaceholder(/search/i).fill(searchOrder.trackingNumber);
    
    // Wait a bit for the filter to apply
    await page.waitForTimeout(1000);
    
    // Verify the searched order is visible (check by tracking number which is in the table)
    await expect(page.locator("table").getByText(searchOrder.trackingNumber)).toBeVisible();
  });

  test("Customer can view order details by clicking on an order", async ({ page }) => {
    await loginAsCustomer(page);
    await page.goto("/customer/interfaces/support/history");
    
    const targetOrder = testOrders[0];
    
    // Wait for table to load
    await page.waitForSelector("table");
    
    // Click on the "Track" button for the order
    const row = page.locator("table tbody tr").filter({ hasText: targetOrder.trackingNumber });
    await row.getByRole("button", { name: /track/i }).click();
    
    // Wait for navigation to order details page
    await page.waitForURL(new RegExp(`/interfaces/support/history/${targetOrder.trackingNumber}`), { timeout: 10000 });
    
    // Verify order details are displayed
    await expect(page.getByText(targetOrder.trackingNumber)).toBeVisible();
    await expect(page.getByText(targetOrder.reference)).toBeVisible();
  });

  test("Customer can see order status and timeline", async ({ page }) => {
    await loginAsCustomer(page);
    const targetOrder = testOrders[0];
    
    // Navigate directly to order details
    await page.goto(`/customer/interfaces/support/history/${targetOrder.trackingNumber}`);
    
    // Wait for order to load
    await page.waitForSelector("text=Package Information", { timeout: 10000 });
    
    // Verify key sections are displayed
    await expect(page.getByText("Package Information")).toBeVisible();
    await expect(page.getByText("Shipping Timeline")).toBeVisible();
    
    // Verify tracking number is visible
    await expect(page.getByText(targetOrder.trackingNumber)).toBeVisible();
  });

  test("Customer can see shipping address in order details", async ({ page }) => {
    await loginAsCustomer(page);
    const targetOrder = testOrders[0];
    
    await page.goto(`/customer/interfaces/support/history/${targetOrder.trackingNumber}`);
    await page.waitForSelector("text=Package Information");
    
    // Verify shipping address is displayed
    await expect(page.getByText(/shipping address/i)).toBeVisible();
    await expect(page.getByText(/Portugal/i)).toBeVisible();
  });

  test("Customer sees error message for non-existent tracking number", async ({ page }) => {
    await loginAsCustomer(page);
    
    // Navigate to a non-existent tracking number
    await page.goto("/customer/interfaces/support/history/NONEXISTENT-123");
    
    // Wait for error message
    await page.waitForSelector("text=/Tracking Number Not Found|Error/i", { timeout: 10000 });
    
    // Verify error heading is displayed
    await expect(page.getByRole("heading", { name: /error/i })).toBeVisible();
  });

  test("Customer can navigate back to order history from order details", async ({ page }) => {
    await loginAsCustomer(page);
    
    // Start from order history page
    await page.goto("/customer/interfaces/support/history");
    await page.waitForSelector("table");
    
    const targetOrder = testOrders[0];
    
    // Go to order details by clicking Track button
    const row = page.locator("table tbody tr").filter({ hasText: targetOrder.trackingNumber });
    await row.getByRole("button", { name: /track/i }).click();
    await page.waitForURL(new RegExp(`/interfaces/support/history/${targetOrder.trackingNumber}`));
    
    // Click back button
    await page.getByRole("button", { name: /back/i }).click();
    
    // Verify we're back on order history (allow trailing slash)
    await expect(page).toHaveURL(/\/interfaces\/support\/history\/?$/);
    await expect(page.getByRole("heading", { name: /order history/i })).toBeVisible();
  });

  // Cleanup test - must be last
  test("Cleanup test data", async () => {
    await cleanupTestData(testOrderIds, testWarehouseId);
  });
});
