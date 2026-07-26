import { test, expect } from '@playwright/test';

/**
 * End-to-end integration tests tracing portal redirects, dashboard navigation, and organization detail views.
 */
test.describe('ProjectMind AI E2E Verification', () => {
  test('should load login page and authenticate successfully', async ({ page }) => {
    await page.goto('/auth/login');
    await expect(page.locator('h2')).toContainText('Sign In');
    
    await page.click('button:has-text("Login Mock Admin")');
    await expect(page).toHaveURL(/.*dashboard/);
  });

  test('should navigate across dashboard widgets', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.locator('h1')).toContainText('Welcome to ProjectMind AI Admin Console');
  });

  test('should navigate to organizations directory', async ({ page }) => {
    await page.goto('/organizations');
    await expect(page.locator('h2')).toContainText('Organizations');
  });
});
