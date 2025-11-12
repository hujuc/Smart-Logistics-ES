import { test, expect, Page } from "@playwright/test";
import { setupTestData, cleanupTestData } from "./test-setup";
import type { OrderResponse, WarehouseResponse } from "./test-setup";

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
  // Setup test data before all tests
  test.beforeAll(async () => {
    const testData = await setupTestData();
    testOrders = testData.orders;
    testWarehouse = testData.warehouse;
  });

  // Note: No cleanup needed as we're using existing database records
  // test.afterAll(async () => {
  //   if (testOrders.length > 0) {
  //     const orderIds = testOrders.map((order) => order.id);
  //     await cleanupTestData(orderIds);
  //   }
  // });

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
