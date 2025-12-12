import { test, expect } from '@playwright/test';

test.describe('Owner Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/owner/dashboard');
  });

  test('should display dashboard title', async ({ page }) => {
    const title = page.locator('h1.dashboard-title');
    await expect(title).toBeVisible();
    await expect(title).toHaveText('Owner Dashboard');
  });

  test('should display all 8 dashboard cards', async ({ page }) => {
    const cards = page.locator('.dashboard-card');
    await expect(cards).toHaveCount(8);
  });

  test('should display buttons on all cards', async ({ page }) => {
    const buttons = page.locator('.card-button');
    await expect(buttons).toHaveCount(8);
  });

  test('should display schedule section', async ({ page }) => {
    const scheduleSection = page.locator('.schedule-section');
    await expect(scheduleSection).toBeVisible();

    const scheduleTitle = page.locator('.schedule-section h2');
    await expect(scheduleTitle).toHaveText('This week:');
  });

  test('should display see more button', async ({ page }) => {
    const seeMoreButton = page.locator('.see-more-button');
    await expect(seeMoreButton).toBeVisible();
    await expect(seeMoreButton).toHaveText('See more');
  });

  test('should navigate to projects when clicking projects card button', async ({
    page,
  }) => {
    const projectsButton = page
      .locator('.dashboard-card', { hasText: 'Projects' })
      .locator('.card-button');
    await projectsButton.click();
    await expect(page).toHaveURL('/projects');
  });

  test('should navigate to uploads when clicking uploads card button', async ({
    page,
  }) => {
    const uploadsButton = page
      .locator('.dashboard-card', { hasText: 'Uploads' })
      .locator('.card-button');
    await uploadsButton.click();
    await expect(page).toHaveURL('/owner/uploads');
  });

  test('should have hover effect on cards', async ({ page }) => {
    const card = page.locator('.dashboard-card').first();

    const boxShadowBefore = await card.evaluate(el => {
      return window.getComputedStyle(el).boxShadow;
    });

    await card.hover();

    await page.waitForTimeout(500);

    const boxShadowAfter = await card.evaluate(el => {
      return window.getComputedStyle(el).boxShadow;
    });

    expect(boxShadowBefore).not.toBe(boxShadowAfter);
  });

  test('should be responsive on tablet', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });

    const cards = page.locator('.dashboard-card');
    await expect(cards).toHaveCount(8);

    const dashboardGrid = page.locator('.dashboard-grid');
    await expect(dashboardGrid).toBeVisible();
  });
});
