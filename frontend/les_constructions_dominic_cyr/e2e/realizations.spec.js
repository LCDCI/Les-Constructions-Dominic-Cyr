import { test, expect } from '@playwright/test';
import { RealizationsPage } from './pages/realizations.page.js';

test.describe('Realizations Page', () => {
  let realizationsPage;

  test.beforeEach(async ({ page }) => {
    realizationsPage = new RealizationsPage(page);
    await realizationsPage.goto();
    await realizationsPage.waitForRealizationsToLoad();
  });

  test('should filter realizations when searching', async () => {
    const initialCount = await realizationsPage.getRealizationCount();

    if (initialCount > 0) {
      await realizationsPage.searchInput.fill('nonexistent12345');
      await realizationsPage.page.waitForTimeout(500);

      const filteredCount = await realizationsPage.getRealizationCount();
      const noResults = await realizationsPage.noResultsMessage.isVisible();

      expect(filteredCount === 0 || noResults).toBeTruthy();
    }
  });

  test('should display realization cards with required elements', async () => {
    const realizationCount = await realizationsPage.getRealizationCount();

    if (realizationCount > 0) {
      const firstCard = realizationsPage.realizationCards.first();
      await expect(firstCard).toBeVisible();
      await expect(firstCard.locator('.realization-image')).toBeVisible();
      await expect(firstCard.locator('.realization-title')).toBeVisible();
      await expect(firstCard.locator('.realization-description')).toBeVisible();
      await expect(firstCard.locator('.realization-button')).toBeVisible();
    }
  });

  test('should have clickable view realization buttons', async () => {
    const buttonCount = await realizationsPage.viewRealizationButtons.count();

    if (buttonCount > 0) {
      await expect(
        realizationsPage.viewRealizationButtons.first()
      ).toBeEnabled();
      await expect(realizationsPage.viewRealizationButtons.first()).toHaveText(
        'View this realization'
      );
    }
  });
});

test.describe('Realizations Page - Navigation', () => {
  test('should navigate to realizations page directly', async ({ page }) => {
    await page.goto('/realizations');
    await expect(page).toHaveURL(/.*realizations/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/realizations');
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});
