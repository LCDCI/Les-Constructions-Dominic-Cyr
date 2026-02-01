import { test, expect } from '@playwright/test';
import { LivingEnvironmentPage } from './pages/livingEnvironment.page.js';

const TEST_PROJECT_ID = 'proj-001-foresta'; // Use a known test project ID

test.describe('Living Environment Page', () => {
  let lePage;

  test.beforeEach(async ({ page }) => {
    lePage = new LivingEnvironmentPage(page);
    await lePage.goto(TEST_PROJECT_ID);
  });

  test('should display the header and main title', async () => {
    await expect(lePage.header).toBeVisible();
    await expect(lePage.mainTitle).toBeVisible();
    await expect(lePage.subtitle).toBeVisible();
  });

  test('should display amenities grid and at least one amenity', async () => {
    await expect(lePage.amenitiesGrid).toBeVisible();
    const count = await lePage.amenityBoxes.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should display the back to project button', async () => {
    await expect(lePage.backButton).toBeVisible();
    await lePage.backButton.click();
    await expect(lePage.page).toHaveURL(
      new RegExp(`/projects/${TEST_PROJECT_ID}/overview`)
    );
  });

  test('should display the footer section', async () => {
    await expect(lePage.footer).toBeVisible();
  });
});
