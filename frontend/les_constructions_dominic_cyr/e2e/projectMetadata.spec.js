import { test, expect } from '@playwright/test';
import { ProjectMetadataPage } from '../e2e/pages/projectMetadata.page';
import { ProjectOverviewPage } from '../e2e/pages/projectOverview.page';

test.describe('Project Metadata E2E', () => {
  test('handles metadata API failure', async ({ page }) => {
    await page.route('**/projects/*/metadata', route =>
      route.fulfill({ status: 403 })
    );

    const metadata = new ProjectMetadataPage(page);
    await metadata.goto('restricted-project');
    await metadata.expectError();
  });
});

test.describe('Project Overview E2E', () => {
  test('loads overview page', async ({ page }) => {
    const overview = new ProjectOverviewPage(page);

    await overview.goto('proj-001-foresta');
    await overview.expectLoaded();

    const primaryColor = await page.evaluate(() =>
      getComputedStyle(document.documentElement)
        .getPropertyValue('--primary-color')
        .trim()
    );

    expect(primaryColor).not.toBe('');
  });

  test('handles overview API failure', async ({ page }) => {
    await page.route('**/projects/*/overview', route =>
      route.fulfill({ status: 500 })
    );

    const overview = new ProjectOverviewPage(page);
    await overview.goto('broken-project');
    await overview.expectError();
  });
});
