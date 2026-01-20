import { test, expect } from '@playwright/test';
import { ReportsPage } from './pages/reports.page.js';

async function loginAsOwner(page) {
  await page.goto('/portal/login');
  await page.waitForLoadState('networkidle');

  let currentUrl = page.url();

  const continueToLoginBtn = page.getByRole('button', {
    name: /Continue to Login/i,
  });

  try {
    await continueToLoginBtn.waitFor({ state: 'visible', timeout: 5000 });
    await continueToLoginBtn.click();
    await page.waitForURL(url => url.toString().includes('auth0.com'), {
      timeout: 10000,
    });
  } catch (e) {
    // Fallback if button is bypassed
  }

  await page.waitForLoadState('networkidle');
  currentUrl = page.url();

  if (currentUrl.includes('auth0.com')) {
    await page.getByLabel('Email address').fill('owner@test.com');
    await page.locator('input[type="password"]').fill('Password123!');

    await Promise.all([
      page.waitForURL(
        url => {
          const urlStr = url.toString();
          return (
            urlStr.includes('localhost:3000') && !urlStr.includes('auth0.com')
          );
        },
        { timeout: 15000 }
      ),
      page.getByRole('button', { name: /Continue/i }).click(),
    ]);

    await page.waitForLoadState('networkidle');
  }

  currentUrl = page.url();

  if (!currentUrl.includes('localhost:3000') || currentUrl.includes('login')) {
    throw new Error(
      `Login failed. Expected to be at localhost:3000/, but at: ${currentUrl}.`
    );
  }

  await page.waitForTimeout(1000);
}

test.describe('Reports Page Transitions', () => {
  let reportsPage;

  test.beforeEach(async ({ page }) => {
    await loginAsOwner(page);
    await page.goto('/reports');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();
    if (currentUrl.includes('login')) {
      throw new Error(
        `Cannot access /reports - redirected to login. URL: ${currentUrl}`
      );
    }

    reportsPage = new ReportsPage(page);
    await page.waitForSelector('h1', { state: 'visible', timeout: 10000 });
  });

  test('should switch tabs and update active styling', async ({ page }) => {
    await expect(reportsPage.generateTab).toHaveClass(/active/);
    await expect(reportsPage.myReportsTab).not.toHaveClass(/active/);
    await reportsPage.switchToMyReports();
    await page.waitForTimeout(500);
    await expect(reportsPage.myReportsTab).toHaveClass(/active/);
    await expect(reportsPage.generateTab).not.toHaveClass(/active/);
    await expect(reportsPage.header).toBeVisible();
  });
});
