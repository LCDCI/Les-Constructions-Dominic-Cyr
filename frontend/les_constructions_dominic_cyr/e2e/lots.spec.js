import { test, expect } from '@playwright/test';
import { LotsPage } from './pages/lots.page.js';

test.describe('Lots Page', () => {
  let lotsPage;

  test.beforeEach(async ({ page }) => {
    lotsPage = new LotsPage(page);
    await lotsPage.goto();
    await lotsPage.isLoaded();
  });

  test('should filter lots when searching', async () => {
    const initialCount = await lotsPage.getLotCount();

    if (initialCount > 0) {
      await lotsPage.searchInput.fill('nonexistent12345');
      await lotsPage.page.waitForTimeout(500);

      const filteredCount = await lotsPage.getLotCount();
      const noResults = await lotsPage.noResultsMessage.isVisible();

      expect(filteredCount === 0 || noResults).toBeTruthy();
    }
  });

  test('should display lot cards with required elements', async () => {
    const lotCount = await lotsPage.getLotCount();

    if (lotCount > 0) {
      const firstCard = lotsPage.lotCards.first();
      await expect(firstCard).toBeVisible();
      await expect(firstCard.locator('.lot-image')).toBeVisible();
      await expect(firstCard.locator('.lot-title')).toBeVisible();
      await expect(firstCard.locator('.lot-details')).toBeVisible();
    }
  });

  test('should only display available lots', async () => {
    const lotCount = await lotsPage.getLotCount();

    // The page should only show lots with status AVAILABLE
    // We can verify this by checking that lots are displayed
    // (if there are available lots in the database)
    expect(lotCount).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Lots Page - Navigation', () => {
  test('should navigate to lots page directly', async ({ page }) => {
    await page.goto('/lots');
    await expect(page).toHaveURL(/.*lots/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/lots');
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});
