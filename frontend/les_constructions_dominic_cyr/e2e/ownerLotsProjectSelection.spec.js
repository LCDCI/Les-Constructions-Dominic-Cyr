import { test, expect } from '@playwright/test';

/**
 * E2E Tests for Owner Lots Project Selection Feature
 *
 * Focused test suite covering core modal functionality:
 * - Opening modal from navbar
 * - Displaying projects
 * - Closing modal
 * - Project selection and navigation
 * - Error handling
 */

// Mock project data
const mockProjects = [
  {
    projectIdentifier: 'proj-001-test',
    projectName: 'Test Project Alpha',
    imageIdentifier: 'test-image-1',
  },
  {
    projectIdentifier: 'proj-002-test',
    projectName: 'Test Project Beta',
    imageIdentifier: 'test-image-2',
  },
];

test.describe('Owner Lots - Project Selection', () => {
  test.beforeEach(async ({ page }) => {
    // Mock authentication
    await page.route('**/oauth/token', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          access_token: 'mock-token',
          token_type: 'Bearer',
          expires_in: 3600,
        }),
      })
    );

    // Mock projects API
    await page.route('**/api/v1/projects', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProjects),
      })
    );

    // Mock file service (for images)
    await page.route('**/files/**', route =>
      route.fulfill({
        status: 200,
        contentType: 'image/jpeg',
        body: Buffer.from('fake-image-data'),
      })
    );
  });

  test('should open project selection modal from navbar', async ({ page }) => {
    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open hamburger menu if on mobile
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }

    // Click Lots in navbar
    const lotsButton = page.locator('.navbar-link', { hasText: /Lots/i });
    await lotsButton.click();

    // Verify modal is visible
    const modal = page.locator('.project-selection-modal');
    await expect(modal).toBeVisible({ timeout: 10000 });
    
    const modalHeader = modal.locator('h2');
    await expect(modalHeader).toBeVisible();
  });

  test('should display projects in list layout', async ({ page }) => {
    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Wait for projects to load
    const projectButtons = page.locator('.project-button');
    await expect(projectButtons).toHaveCount(mockProjects.length, { timeout: 10000 });

    // Verify first project is visible
    await expect(projectButtons.first()).toBeVisible();
  });

  test('should close modal when clicking X button', async ({ page }) => {
    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Wait for modal
    const modal = page.locator('.project-selection-modal');
    await expect(modal).toBeVisible({ timeout: 10000 });

    // Click close button
    const closeButton = page.locator('.modal-close-button');
    await closeButton.click();

    // Verify modal is hidden
    await expect(modal).not.toBeVisible();
  });

  test('should close modal when clicking backdrop', async ({ page }) => {
    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Wait for modal
    const modal = page.locator('.project-selection-modal');
    await expect(modal).toBeVisible({ timeout: 10000 });

    // Click backdrop (outside modal)
    await page.locator('.modal-backdrop').click({ position: { x: 5, y: 5 } });

    // Verify modal is hidden
    await expect(modal).not.toBeVisible();
  });

  test('should navigate to lots page when clicking project', async ({ page }) => {
    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Mock the lots endpoint
    await page.route('**/api/v1/projects/proj-001-test/lots', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      })
    );

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Wait for projects to load
    await page.waitForSelector('.project-button', { timeout: 10000 });

    // Click first project
    const firstProject = page.locator('.project-button').first();
    await firstProject.click();

    // Verify navigation to lots page
    await page.waitForURL('**/projects/proj-001-test/manage-lots', { timeout: 15000 });
    expect(page.url()).toContain('/projects/proj-001-test/manage-lots');
  });

  test('should show error state when API fails', async ({ page }) => {
    // Override projects API to return error
    await page.route('**/api/v1/projects', route =>
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' }),
      })
    );

    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Verify error state is displayed
    const errorState = page.locator('.error-state');
    await expect(errorState).toBeVisible({ timeout: 10000 });
    
    // Verify retry button exists
    const retryButton = page.locator('.retry-button');
    await expect(retryButton).toBeVisible();
  });

  test('should show empty state when no projects exist', async ({ page }) => {
    // Override projects API to return empty array
    await page.route('**/api/v1/projects', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      })
    );

    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Verify empty state is displayed
    const emptyState = page.locator('.empty-state');
    await expect(emptyState).toBeVisible({ timeout: 10000 });
  });

  test('should handle many projects with scrolling', async ({ page }) => {
    // Create many mock projects
    const manyProjects = Array.from({ length: 20 }, (_, i) => ({
      projectIdentifier: `proj-${String(i + 1).padStart(3, '0')}-test`,
      projectName: `Test Project ${i + 1}`,
      imageIdentifier: `test-image-${i + 1}`,
    }));

    // Override projects API with many projects
    await page.route('**/api/v1/projects', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(manyProjects),
      })
    );

    await page.goto('/owner/dashboard');
    await page.waitForLoadState('networkidle');

    // Open modal
    const hamburger = page.locator('.hamburger-menu');
    if (await hamburger.isVisible()) {
      await hamburger.click();
      await page.waitForTimeout(300);
    }
    await page.locator('.navbar-link', { hasText: /Lots/i }).click();

    // Wait for projects to load
    const projectButtons = page.locator('.project-button');
    await expect(projectButtons).toHaveCount(20, { timeout: 10000 });

    // Verify modal content is scrollable
    const modalContent = page.locator('.modal-content');
    await expect(modalContent).toBeVisible();

    // Scroll down to check if more projects are accessible
    await modalContent.evaluate(el => el.scrollTop = el.scrollHeight);
    await page.waitForTimeout(500);

    // Verify last project is now visible after scroll
    const lastProject = projectButtons.last();
    await expect(lastProject).toBeVisible();
  });
});
