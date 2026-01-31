import { test, expect } from '@playwright/test';

test.describe('Contractor Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/contractor/dashboard');
  });

  test('should display dashboard title', async ({ page }) => {
    const title = page.locator('h1.dashboard-title');
    await expect(title).toBeVisible();
    await expect(title).toHaveText('Contractor Dashboard');
  });

  test('should display all 5 dashboard cards', async ({ page }) => {
    const cards = page.locator('.dashboard-card');
    await expect(cards).toHaveCount(5);
  });

  test('should display buttons on all cards', async ({ page }) => {
    const buttons = page.locator('.card-button');
    await expect(buttons).toHaveCount(5);
  });

  test('should display schedule section', async ({ page }) => {
    const scheduleSection = page.locator('.schedule-section');
    await expect(scheduleSection).toBeVisible();

    const scheduleTitle = page.locator('.schedule-section h2');
    await expect(scheduleTitle).toHaveText('Here are your upcoming tasks:');
  });

  test('should display see more button', async ({ page }) => {
    const seeMoreButton = page.locator('.see-more-button');
    await expect(seeMoreButton).toBeVisible();
    await expect(seeMoreButton).toHaveText('See more');
  });

  test('should navigate to all tasks page when clicking see more button', async ({
    page,
  }) => {
    const seeMoreButton = page.locator('.see-more-button');
    await seeMoreButton.click();
    await expect(page).toHaveURL('/contractor/tasks');
  });

  test('should navigate to all tasks when clicking All Tasks card button', async ({
    page,
  }) => {
    const allTasksButton = page
      .locator('.dashboard-card', { hasText: 'All Tasks' })
      .locator('.card-button');
    await allTasksButton.click();
    await expect(page).toHaveURL('/contractor/tasks');
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
    await expect(cards).toHaveCount(4);

    const dashboardGrid = page.locator('.dashboard-grid');
    await expect(dashboardGrid).toBeVisible();
  });
});
