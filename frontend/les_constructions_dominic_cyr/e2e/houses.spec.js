import { test, expect } from '@playwright/test';
import { HousesPage } from './pages/houses.page.js';

test.describe('Houses Page', () => {
  let housesPage;

  test.beforeEach(async ({ page }) => {
    housesPage = new HousesPage(page);
    await housesPage.goto();
    await housesPage.waitForHousesToLoad();
  });

  test('should filter houses when searching', async () => {
    const initialCount = await housesPage.getHouseCount();

    if (initialCount > 0) {
      await housesPage.searchInput.fill('nonexistent12345');
      await housesPage.page.waitForTimeout(500);

      const filteredCount = await housesPage.getHouseCount();
      const noResults = await housesPage.noResultsMessage.isVisible();

      expect(filteredCount === 0 || noResults).toBeTruthy();
    }
  });

  test('should display house cards with required elements', async () => {
    const houseCount = await housesPage.getHouseCount();

    if (houseCount > 0) {
      const firstCard = housesPage.houseCards.first();
      await expect(firstCard).toBeVisible();
      await expect(firstCard.locator('.house-image')).toBeVisible();
      await expect(firstCard.locator('.house-title')).toBeVisible();
      await expect(firstCard.locator('.house-description')).toBeVisible();
      await expect(firstCard.locator('.house-button')).toBeVisible();
    }
  });

  test('should have clickable view house buttons', async () => {
    const buttonCount = await housesPage.viewHouseButtons.count();

    if (buttonCount > 0) {
      await expect(housesPage.viewHouseButtons.first()).toBeEnabled();
      await expect(housesPage.viewHouseButtons.first()).toHaveText(
        'View this house'
      );
    }
  });
});

test.describe('Houses Page - Navigation', () => {
  test('should navigate to houses page directly', async ({ page }) => {
    await page.goto('/houses');
    await expect(page).toHaveURL(/.*houses/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/houses');
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});
