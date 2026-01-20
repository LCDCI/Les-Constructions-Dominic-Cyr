import { test, expect } from '@playwright/test';
import { UsersPage } from './pages/users.page.js';

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
  } catch (e) {}

  await page.waitForLoadState('networkidle');
  currentUrl = page.url();

  if (currentUrl.includes('auth0.com')) {
    await page.getByLabel('Email address').fill('owner@test.com');
    await page.locator('input[type="password"]').fill('Password123');
    await page.getByRole('button', { name: /Continue/i }).click();

    await page.waitForURL(
      url => {
        const urlStr = url.toString();
        return (
          urlStr.includes('localhost:3000') && !urlStr.includes('auth0.com')
        );
      },
      { timeout: 15000 }
    );

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

test.describe('Users - Edit and Status Management', () => {
  let usersPage;
  let targetEmail;

  test.beforeEach(async ({ page }) => {
    await loginAsOwner(page);

    await page.goto('/users');
    await page.waitForLoadState('networkidle');

    const currentUrl = page.url();

    if (currentUrl.includes('login')) {
      throw new Error(
        `Cannot access /users - redirected to login. URL: ${currentUrl}.`
      );
    }

    usersPage = new UsersPage(page);

    const isLoading = await usersPage.loadingMessage
      .isVisible()
      .catch(() => false);
    if (isLoading) {
      await usersPage.loadingMessage.waitFor({
        state: 'hidden',
        timeout: 15000,
      });
    }

    await page.waitForSelector('tbody tr', {
      state: 'visible',
      timeout: 10000,
    });

    const rows = await page.locator('tbody tr').all();
    for (const row of rows) {
      const text = await row.textContent();
      if (!text.includes('OWNER') && text.includes('@test.com')) {
        const emailMatch = text.match(
          /([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)/
        );
        if (emailMatch) {
          targetEmail = emailMatch[0];

          break;
        }
      }
    }

    if (!targetEmail) {
      throw new Error('No suitable test user found in the table');
    }
  });

  test('should deactivate a user', async ({ page }) => {
    await usersPage.openStatusModal(targetEmail);
    await page.waitForTimeout(500);
    await page.getByText('Deactivate User').click();
    await page.getByRole('button', { name: 'Confirm' }).click();
    await page.waitForTimeout(1000);
    const row = usersPage.getRowByEmail(targetEmail);
    await expect(row).not.toBeVisible();
  });
});
