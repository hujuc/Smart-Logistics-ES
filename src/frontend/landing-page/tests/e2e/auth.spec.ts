import { test, expect, Page } from "@playwright/test";

const randomCredentials = () => {
  const timestamp = Date.now();
  const username = `e2e_customer_${timestamp}`;
  return {
    username,
    email: `${username}@slms.test`,
    password: `Passw0rd!${timestamp}`,
    fullName: "E2E Customer",
  };
};

const waitForCustomerPortal = async (page: Page) => {
  await page.waitForURL(/\/customer(\/.*)?$/, { timeout: 120_000 });
  await expect(page.getByRole("heading", { name: "My Support Tickets" })).toBeVisible({
    timeout: 60_000,
  });
};

test.describe.configure({ mode: "serial" });

test.describe("Landing page authentication flows", () => {
  test("customer can register via landing page", async ({ page }) => {
    const creds = randomCredentials();

    await page.goto("/");
    await page.getByRole("button", { name: "Sign Up" }).first().click();
    await expect(page.getByRole("heading", { name: "Create your account" })).toBeVisible();

    await page.getByLabel("Full Name").fill(`${creds.fullName} ${creds.username}`);
    await page.getByLabel("Username").fill(creds.username);
    await page.getByLabel("Email").fill(creds.email);
    await page.getByLabel("Password", { exact: true }).fill(creds.password);
    await page.getByLabel("Confirm Password", { exact: true }).fill(creds.password);

    await page.getByRole("button", { name: "Continue" }).click();

    await expect(page.getByRole("button", { name: "Create Account" })).toBeVisible();
    await page.locator('input[name="role"][value="customer"]').check();
    await page.getByRole("button", { name: "Create Account" }).click();

    await waitForCustomerPortal(page);
  });

  test("existing customer can log in through landing page", async ({ page }) => {
    await page.goto("/");
    await page.getByRole("button", { name: "Login" }).first().click();
    await expect(page.getByRole("heading", { name: "Welcome back" })).toBeVisible();

    await page.getByLabel("Username").fill("customer");
    await page.getByLabel("Password").fill("customer");
    await page.getByRole("button", { name: /^Login$/ }).last().click();

    await waitForCustomerPortal(page);
  });
});
