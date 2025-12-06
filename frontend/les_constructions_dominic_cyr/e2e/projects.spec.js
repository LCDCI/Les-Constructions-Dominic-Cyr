import { test, expect } from '@playwright/test';
import { ProjectsPage } from './pages/projects.page.js';

test.describe('Projects Page', () => {
  let projectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
    await projectsPage.waitForProjectsToLoad();
  });

  test('should filter projects when searching', async () => {
    const initialCount = await projectsPage.getProjectCount();

    if (initialCount > 0) {
      await projectsPage.searchInput.fill('nonexistent12345');
      await projectsPage.page.waitForTimeout(500);

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
      await expect(firstCard.locator('.project-image')).toBeVisible();
      await expect(firstCard.locator('.project-title')).toBeVisible();
      await expect(firstCard.locator('.project-description')).toBeVisible();
      await expect(firstCard.locator('.project-button')).toBeVisible();
    }
  });

  test('should have clickable view project buttons', async () => {
    const buttonCount = await projectsPage.viewProjectButtons.count();

    if (buttonCount > 0) {
      await expect(projectsPage.viewProjectButtons.first()).toBeEnabled();
      await expect(projectsPage.viewProjectButtons.first()).toHaveText(
        'View this project'
      );
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
    await expect(page).toHaveURL('/');
  });
});
