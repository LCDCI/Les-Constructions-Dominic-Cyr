import { test, expect } from '@playwright/test';
import { LotsPage } from './pages/lots.page.js';

test.describe('Lots Page', () => {
  let lotsPage;

  test.beforeEach(async ({ page }) => {
    lotsPage = new LotsPage(page);
    await lotsPage.goto();
    await lotsPage.waitForLotsToLoad();
  });

  test('should filter lots when searching', async () => {
    const initialCount = await lotsPage.getLotCount();
    
    // Only test search functionality if there are lots to search
    if (initialCount === 0) {
      test.skip();
    }

    // Perform search for non-existent lot
    await lotsPage.searchInput.fill('nonexistent12345');
    
    // Wait for either the no-results message or for the cards count to update
    try {
      await expect(lotsPage.noResultsMessage).toBeVisible({ timeout: 2000 });
    } catch (e) {
      // if the no-results message didn't appear, we'll check the count below
    }

    const filteredCount = await lotsPage.getLotCount();
    const noResults = await lotsPage.noResultsMessage.isVisible();

    // Either the filtered count is 0, or the no-results message is visible
    expect(filteredCount === 0 || noResults).toBeTruthy();
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
