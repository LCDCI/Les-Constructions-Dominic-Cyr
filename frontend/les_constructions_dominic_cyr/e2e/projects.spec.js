import { test, expect } from '@playwright/test';
import { ProjectsPage } from './pages/projects.page.js';

test.describe('Projects Page', () => {
  let projectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
    await expect(projectsPage.pageTitle).toBeVisible();
  });

  test('should filter projects when searching', async () => {
    const searchPresent = await projectsPage.searchInput.count();
    const initialCount = await projectsPage.getProjectCount();

    if (searchPresent > 0 && initialCount > 0) {
      await projectsPage.searchInput.fill('nonexistent12345');
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
      await expect(firstCard.locator('.project-title')).toBeVisible();

      const imgCount = await firstCard.locator('.project-image').count();
      if (imgCount > 0) {
        await expect(firstCard.locator('.project-image')).toBeVisible();
      }

      const descCount = await firstCard.locator('.project-description').count();
      if (descCount > 0) {
        await expect(firstCard.locator('.project-description')).toBeVisible();
      }

      const btnCount = await firstCard.locator('.project-button').count();
      if (btnCount > 0) {
        await expect(firstCard.locator('.project-button').first()).toBeVisible();
      }
    }
  });

  test('should have clickable view project buttons', async () => {
    const buttonCount = await projectsPage.viewProjectButtons.count();

    if (buttonCount > 0) {
      await expect(projectsPage.viewProjectButtons.first()).toBeEnabled();
      await expect(projectsPage.viewProjectButtons.first()).toHaveText(/view/i);
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
