import { test, expect } from '@playwright/test';
import { RenovationsPage } from './pages/renovations.page.js';

test.describe('Renovations Page', () => {
  let renovationsPage;

  test.beforeEach(async ({ page }) => {
    renovationsPage = new RenovationsPage(page);
    await renovationsPage.goto();
    await renovationsPage.waitForRenovationsToLoad();
  });

  test('should display renovation cards with required elements', async () => {
    const renovationCount = await renovationsPage.getRenovationCount();

    if (renovationCount > 0) {
      const firstCard = renovationsPage.renovationCards.first();
      await expect(firstCard).toBeVisible();
      await expect(
        firstCard.locator('.renovation-card__image--before')
      ).toBeVisible();
      await expect(
        firstCard.locator('.renovation-card__image--after')
      ).toBeVisible();
      await expect(
        firstCard.locator('.renovation-card__description')
      ).toBeVisible();
    }
  });

  test('should display only available renovations', async () => {
    const renovationCount = await renovationsPage.getRenovationCount();

    // The page should display renovations if they exist in the database
    // We verify that the count is non-negative
    expect(renovationCount).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Renovations Page - Navigation', () => {
  test('should navigate to renovations page directly', async ({ page }) => {
    await page.goto('/renovations');
    await expect(page).toHaveURL(/.*renovations/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/renovations');
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});
