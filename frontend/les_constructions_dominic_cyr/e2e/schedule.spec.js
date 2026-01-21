import { test, expect } from '@playwright/test';

test.describe('Schedule Section', () => {
  test('should load and display schedules from API', async ({ page }) => {
    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            scheduleIdentifier: 'SCH-001',
            scheduleStartDate: '2024-11-26',
            scheduleEndDate: '2024-11-26',
            scheduleDescription: 'Begin Excavation',
            lotNumber: 'Lot 53',
          },
          {
            scheduleIdentifier: 'SCH-002',
            scheduleStartDate: '2024-11-26',
            scheduleEndDate: '2024-11-26',
            scheduleDescription: 'Plumbing',
            lotNumber: 'Lot 57',
          },
          {
            scheduleIdentifier: 'SCH-003',
            scheduleStartDate: '2024-11-27',
            scheduleEndDate: '2024-11-27',
            scheduleDescription: 'Electrical',
            lotNumber: 'Lot 54',
          },
        ]),
      });
    });

    await page.goto('/owner/dashboard');

    const scheduleItems = page.locator('.schedule-item');
    await expect(scheduleItems).toHaveCount(3);

    await expect(scheduleItems.first()).toContainText('Begin Excavation');
    await expect(scheduleItems.first()).toContainText('Lot 53');
  });

  test('should display loading state', async ({ page }) => {
    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.goto('/owner/dashboard');

    const loadingText = page.locator('text=Loading schedules...');
    await expect(loadingText).toBeVisible();
  });

  test('should display error message when API fails', async ({ page }) => {
    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Internal Server Error' }),
      });
    });

    await page.goto('/owner/dashboard');

    const errorText = page.locator('.error');
    await expect(errorText).toBeVisible();
  });

  test('should display empty state when no schedules', async ({ page }) => {
    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.goto('/owner/dashboard');

    const emptyText = page.locator('text=No schedules for this week');
    await expect(emptyText).toBeVisible();
  });

  test('should format dates correctly', async ({ page }) => {
    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            scheduleIdentifier: 'SCH-001',
            scheduleStartDate: '2024-11-26',
            scheduleEndDate: '2024-11-26',
            scheduleDescription: 'Begin Excavation',
            lotNumber: 'Lot 53',
          },
        ]),
      });
    });

    await page.goto('/owner/dashboard');

    const scheduleItem = page.locator('.schedule-item').first();
    const dateText = await scheduleItem.locator('.schedule-date').textContent();

    expect(dateText).toMatch(/\w+, \w+ \d+/);
  });

  test('should display multiple schedule items correctly', async ({ page }) => {
    const mockSchedules = Array.from({ length: 5 }, (_, i) => ({
      scheduleIdentifier: `SCH-${i + 1}`,
      scheduleStartDate: '2024-11-26',
      scheduleEndDate: '2024-11-26',
      scheduleDescription: `Task ${i + 1}`,
      lotNumber: `Lot ${i + 50}`,
    }));

    await page.route('**/api/v1/owners/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockSchedules),
      });
    });

    await page.goto('/owner/dashboard');

    const scheduleItems = page.locator('.schedule-item');
    await expect(scheduleItems).toHaveCount(5);

    for (let i = 0; i < 5; i++) {
      await expect(scheduleItems.nth(i)).toContainText(`Task ${i + 1}`);
      await expect(scheduleItems.nth(i)).toContainText(`Lot ${i + 50}`);
    }
  });
});
