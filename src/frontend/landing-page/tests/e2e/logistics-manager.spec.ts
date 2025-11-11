import { test, expect, Page } from "@playwright/test";

const randomDriverData = () => {
  const timestamp = Date.now();
  return {
    username: `driver_${timestamp}`,
    email: `driver_${timestamp}@slms.test`,
    password: `Driver#${timestamp}`,
    firstName: "Alex",
    lastName: `Courier${timestamp.toString().slice(-4)}`,
  };
};

async function loginThroughLandingPage(page: Page, username: string, password: string) {
  await page.goto("/");
  await page.getByRole("button", { name: "Login" }).first().click();
  await expect(page.getByRole("heading", { name: "Welcome back" })).toBeVisible();
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await page.getByRole("button", { name: /^Login$/ }).last().click();
}

async function waitForLogisticsDashboard(page: Page) {
  await page.waitForURL(/\/admin\/interfaces\/logistics\/?$/, { timeout: 120_000 });
  await expect(
    page.getByRole("heading", { name: "Logistics Operations Dashboard" })
  ).toBeVisible({ timeout: 60_000 });
}

test.describe.configure({ mode: "serial" });

test.describe("Logistics manager driver onboarding", () => {
  test("can create a new driver and confirm details", async ({ page }) => {
    const driver = randomDriverData();

    await loginThroughLandingPage(page, "logistics_manager", "manager");
    await waitForLogisticsDashboard(page);

    const usersNavButton = page.locator('nav button[aria-label="Users"]').first();
    await usersNavButton.waitFor({ state: "visible" });
    await usersNavButton.click();

    await page.waitForURL(/\/admin\/interfaces\/logistics\/users\/?$/, { timeout: 60_000 });
    await expect(page.getByRole("heading", { name: "User Management" })).toBeVisible();

    await page.getByRole("button", { name: "Add New User" }).click();
    await expect(page.getByRole("heading", { name: "Create New User" })).toBeVisible();

    await page.getByLabel("Username").fill(driver.username);
    await page.getByLabel("Email").fill(driver.email);
    await page.getByLabel("Password", { exact: true }).fill(driver.password);
    await page.getByLabel("First Name").fill(driver.firstName);
    await page.getByLabel("Last Name").fill(driver.lastName);
    await page.getByLabel("Role *").selectOption("DRIVER");

    await page.getByRole("button", { name: "Create User" }).click();

    await expect(page.getByText("User created successfully.")).toBeVisible();

    const driverRow = page
      .getByRole("row")
      .filter({ hasText: driver.username })
      .filter({ hasText: driver.email });

    await expect(driverRow).toBeVisible({ timeout: 60_000 });
    await expect(driverRow.getByText("Drivers")).toBeVisible();
  });
});
