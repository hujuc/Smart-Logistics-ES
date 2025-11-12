import { test, expect, Page } from "@playwright/test";

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

test.describe.configure({ mode: "serial" });

test.describe("Customer Service - Order Tracking and Details (US 7)", () => {
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
    
    // Enter order ID (assuming order 1 exists in test data)
    const orderIdInput = page.getByPlaceholder(/order.*tracking/i);
    await orderIdInput.fill("1");
    
    // Click search button
    await page.getByRole("button", { name: /search/i }).click();
    
    // Wait for order details to appear
    await expect(page.getByText(/order details/i)).toBeVisible({ timeout: 30_000 });
    
    // Verify order ID is displayed
    await expect(page.getByText(/order #1/i)).toBeVisible();
  });

  test("CSR can view order details including status and timeline", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for order
    await page.getByPlaceholder(/order.*tracking/i).fill("1");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Wait for details to load
    await expect(page.getByText(/order details/i)).toBeVisible({ timeout: 30_000 });
    
    // Verify essential information is present
    await expect(page.getByText(/status/i)).toBeVisible();
    await expect(page.getByText(/tracking number/i)).toBeVisible();
    await expect(page.getByText(/customer/i)).toBeVisible();
    
    // Verify shipping timeline is present
    await expect(page.getByText(/shipping timeline/i)).toBeVisible();
  });

  test("CSR can see delivery address in order details", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for order
    await page.getByPlaceholder(/order.*tracking/i).fill("1");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Wait for details
    await expect(page.getByText(/order details/i)).toBeVisible({ timeout: 30_000 });
    
    // Verify delivery address section
    await expect(page.getByText(/delivery address/i)).toBeVisible();
  });

  test("CSR sees error message for non-existent order", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for non-existent order
    await page.getByPlaceholder(/order.*tracking/i).fill("999999");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Verify error message appears
    await expect(page.getByText(/not found|no order found|order does not exist/i)).toBeVisible({ 
      timeout: 30_000 
    });
  });

  test("CSR can view order item details", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for order
    await page.getByPlaceholder(/order.*tracking/i).fill("1");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Wait for details
    await expect(page.getByText(/order details/i)).toBeVisible({ timeout: 30_000 });
    
    // Verify order items section
    await expect(page.getByText(/items|products|order items/i)).toBeVisible();
    
    // Verify item information is present (weight, dimensions, etc.)
    await expect(page.getByText(/weight|dimensions|quantity/i)).toBeVisible();
  });

  test("CSR can search for another order after viewing one", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // First search
    await page.getByPlaceholder(/order.*tracking/i).fill("1");
    await page.getByRole("button", { name: /search/i }).click();
    await expect(page.getByText(/order #1/i)).toBeVisible({ timeout: 30_000 });
    
    // Clear and search for another order
    const orderIdInput = page.getByPlaceholder(/order.*tracking/i);
    await orderIdInput.clear();
    await orderIdInput.fill("2");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Verify second order is displayed
    await expect(page.getByText(/order #2/i)).toBeVisible({ timeout: 30_000 });
  });

  test("CSR can view carrier information if order has shipment", async ({ page }) => {
    await page.goto("/admin/interfaces/customer-service/order-lookup");
    await waitForOrderLookupPage(page);
    
    // Search for order with shipment
    await page.getByPlaceholder(/order.*tracking/i).fill("1");
    await page.getByRole("button", { name: /search/i }).click();
    
    // Wait for details
    await expect(page.getByText(/order details/i)).toBeVisible({ timeout: 30_000 });
    
    // Check if carrier information is present (may not always be available)
    const carrierInfo = page.getByText(/carrier|shipping provider/i);
    
    // If carrier info exists, verify it's visible
    if (await carrierInfo.count() > 0) {
      await expect(carrierInfo.first()).toBeVisible();
    }
  });
});
