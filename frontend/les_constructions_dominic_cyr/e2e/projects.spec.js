import { test, expect } from '@playwright/test';
import { ProjectsPage } from './pages/projects.page.js';

test.describe('Projects Page', () => {
  let projectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
    const titleVisible = await projectsPage.pageTitle
      .isVisible()
      .catch(() => false);
    if (!titleVisible) {
      await Promise.allSettled([
        projectsPage.projectCards
          .first()
          .waitFor({ state: 'visible', timeout: 10000 }),
        projectsPage.noResultsMessage.waitFor({
          state: 'visible',
          timeout: 10000,
        }),
      ]);
    }
  });

  test('should filter projects when searching', async () => {
    const searchPresent = await projectsPage.searchInput.count();
    const initialCount = await projectsPage.getProjectCount();

    if (searchPresent > 0 && initialCount > 0) {
      await projectsPage.searchInput.fill('panora');
      await projectsPage.page.waitForTimeout(700);

      const filteredCount = await projectsPage.getProjectCount();
      const noResults = await projectsPage.noResultsMessage.isVisible();

      expect(filteredCount === 0 || noResults).toBeTruthy();
    }
  });

  test('should display project cards with required elements', async () => {
    const projectCount = await projectsPage.getProjectCount();

    if (projectCount > 0) {
      const firstCard = projectsPage.projectCards.first();
      await expect(firstCard).toBeVisible();
      await firstCard.hover().catch(() => null);
      await projectsPage.page.waitForTimeout(300);
      await expect(firstCard.locator('.card-title')).toBeVisible();

      const imgCount = await firstCard
        .locator('.card-image-bg, .project-image')
        .count();
      if (imgCount > 0) {
        await expect(
          firstCard.locator('.card-image-bg, .project-image')
        ).toBeVisible();
      }

      const descCount = await firstCard
        .locator('.admin-project-description, .project-description')
        .count();
      if (descCount > 0) {
        await expect(
          firstCard.locator('.admin-project-description, .project-description')
        ).toBeVisible();
      }

      const btnCount = await firstCard
        .locator('.admin-project-button, .project-button')
        .count();
      if (btnCount > 0) {
        const firstBtn = firstCard
          .locator('.admin-project-button, .project-button')
          .first();
        const btnVisible = await firstBtn.isVisible().catch(() => false);
        if (!btnVisible) {
          await firstCard.hover().catch(() => null);
          await projectsPage.page.waitForTimeout(300);
        }
        await expect(firstBtn).toBeVisible();
      }
    }
  });

  test('should have clickable view project buttons', async () => {
    const buttonCount = await projectsPage.viewProjectButtons.count();

    if (buttonCount > 0) {
      const firstView = projectsPage.viewProjectButtons.first();
      // If not visible, try revealing via hover on the first project card
      const visible = await firstView.isVisible().catch(() => false);
      if (!visible) {
        const firstCard = projectsPage.projectCards.first();
        await firstCard.hover().catch(() => null);
        await projectsPage.page.waitForTimeout(300);
      }

      let nowVisible = await firstView.isVisible().catch(() => false);
      // If still not visible, maybe mobile combined actions are used — open dropdown
      if (!nowVisible) {
        const firstCard = projectsPage.projectCards.first();
        const detailsSummary = firstCard.locator(
          'xpath=following-sibling::div[contains(@class, "mobile-project-actions")]//summary'
        );
        const detailsCount = await detailsSummary.count();
        if (detailsCount > 0) {
          await detailsSummary.first().click();
          await projectsPage.page.waitForTimeout(200);
        }
      }

      await expect(firstView).toBeVisible();
      await expect(firstView).toHaveText(/view/i);
    }
  });
});

test.describe('Projects Page - Navigation', () => {
  test('should navigate to projects page directly', async ({ page }) => {
    await page.goto('/projects');
    await expect(page).toHaveURL(/.*projects/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/projects');
    await page.goBack();
    await expect(page).toHaveURL(/\/$/);
  });
});
