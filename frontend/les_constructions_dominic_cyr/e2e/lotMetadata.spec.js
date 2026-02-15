import { test, expect } from '@playwright/test';
import { LotMetadataPage } from './pages/lotMetadata.page';

test.describe('Lot Metadata E2E', () => {
  test('loads lot metadata and shows lot id in header and translated status', async ({
    page,
  }) => {
    // Mock project metadata
    await page.route('**/api/v1/projects/sample-project', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          projectName: 'Sample Project',
          primaryColor: '#123456',
          tertiaryColor: '#654321',
          buyerColor: '#00ff00',
          imageIdentifier: null,
        }),
      })
    );

    // Mock lot metadata
    await page.route('**/api/v1/projects/sample-project/lots/3', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 3,
          lotId: '6690973',
          lotNumber: '6690 973',
          lotStatus: 'AVAILABLE',
          civicAddress: '123 Test Ave',
          primaryColor: '#123456',
          buyerColor: '#00ff00',
          progressPercentage: 10,
        }),
      })
    );

    // Mock translations for lotMetadata namespace (English)
    await page.route('**/api/v1/translations/en/page/lotMetadata', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          language: 'en',
          translations: {
            lot: 'Lot',
            lotStatus: { available: 'Available-e2e' },
            loadingLot: 'Loading...',
          },
        }),
      })
    );

    const pageObj = new LotMetadataPage(page);
    await pageObj.goto('sample-project', '3');
    await pageObj.expectLoaded();

    const header = await pageObj.getHeaderText();
    expect(header).toMatch(/Lot\s*3/);

    const status = await pageObj.getStatusText();
    expect(status).toContain('Available-e2e');
  });

  test('displays completed and remaining tasks for lot', async ({ page }) => {
    // Mock project metadata
    await page.route('**/api/v1/projects/sample-project', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          projectName: 'Sample Project',
          primaryColor: '#123456',
          tertiaryColor: '#654321',
          buyerColor: '#00ff00',
          imageIdentifier: null,
        }),
      })
    );

    // Mock lot metadata
    await page.route('**/api/v1/projects/sample-project/lots/3', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 3,
          lotId: '6690973',
          lotNumber: '6690 973',
          lotStatus: 'AVAILABLE',
          civicAddress: '123 Test Ave',
          primaryColor: '#123456',
          buyerColor: '#00ff00',
          progressPercentage: 50,
        }),
      })
    );

    // Mock schedules
    await page.route('**/api/v1/projects/sample-project/schedules', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            scheduleIdentifier: 'SCH-001',
            lotId: '6690973',
            scheduleDescription: 'Construction Schedule',
          },
        ]),
      })
    );

    // Mock tasks for schedule
    await page.route('**/api/v1/schedules/SCH-001/tasks', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            taskId: 'TASK-001',
            taskTitle: 'Foundation Work',
            taskDescription: 'Complete foundation',
            taskStatus: 'COMPLETED',
            taskProgress: 100,
          },
          {
            taskId: 'TASK-002',
            taskTitle: 'Framing',
            taskDescription: 'Frame structure',
            taskStatus: 'IN_PROGRESS',
            taskProgress: 45,
          },
          {
            taskId: 'TASK-003',
            taskTitle: 'Roofing',
            taskDescription: 'Install roof',
            taskStatus: 'TO_DO',
            taskProgress: 0,
          },
        ]),
      })
    );

    // Mock translations
    await page.route('**/api/v1/translations/en/page/lotMetadata', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          language: 'en',
          translations: {
            lot: 'Lot',
            lotStatus: { available: 'Available' },
            completedTasks: 'Completed Tasks',
            remainingTasks: 'Remaining Tasks',
            taskStatus: {
              completed: 'Completed',
              in_progress: 'In Progress',
              to_do: 'To Do',
            },
          },
        }),
      })
    );

    const pageObj = new LotMetadataPage(page);
    await pageObj.goto('sample-project', '3');
    await pageObj.expectLoaded();

    // Check for completed tasks section
    const completedTasksHeading = page.locator('h2:has-text("Completed Tasks")');
    await expect(completedTasksHeading).toBeVisible();
    await expect(completedTasksHeading).toContainText('(1)');

    // Check completed task is displayed
    const foundationTask = page.locator('.lot-card:has-text("Foundation Work")');
    await expect(foundationTask).toBeVisible();

    // Check for remaining tasks section
    const remainingTasksHeading = page.locator('h2:has-text("Remaining Tasks")');
    await expect(remainingTasksHeading).toBeVisible();
    await expect(remainingTasksHeading).toContainText('(2)');

    // Check remaining tasks are displayed
    const framingTask = page.locator('.lot-card:has-text("Framing")');
    await expect(framingTask).toBeVisible();

    const roofingTask = page.locator('.lot-card:has-text("Roofing")');
    await expect(roofingTask).toBeVisible();
  });

  test('navigates to task detail page when clicking on task', async ({ page }) => {
    // Mock all the same routes as previous test
    await page.route('**/api/v1/projects/sample-project', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          projectName: 'Sample Project',
          primaryColor: '#123456',
          tertiaryColor: '#654321',
          buyerColor: '#00ff00',
          imageIdentifier: null,
        }),
      })
    );

    await page.route('**/api/v1/projects/sample-project/lots/3', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 3,
          lotId: '6690973',
          lotNumber: '6690 973',
          lotStatus: 'AVAILABLE',
          civicAddress: '123 Test Ave',
          primaryColor: '#123456',
          buyerColor: '#00ff00',
          progressPercentage: 50,
        }),
      })
    );

    await page.route('**/api/v1/projects/sample-project/schedules', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            scheduleIdentifier: 'SCH-001',
            lotId: '6690973',
            scheduleDescription: 'Construction Schedule',
          },
        ]),
      })
    );

    await page.route('**/api/v1/schedules/SCH-001/tasks', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            taskId: 'TASK-001',
            taskTitle: 'Foundation Work',
            taskDescription: 'Complete foundation',
            taskStatus: 'COMPLETED',
            taskProgress: 100,
          },
        ]),
      })
    );

    await page.route('**/api/v1/translations/en/page/lotMetadata', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          language: 'en',
          translations: {
            lot: 'Lot',
            lotStatus: { available: 'Available' },
            completedTasks: 'Completed Tasks',
            remainingTasks: 'Remaining Tasks',
          },
        }),
      })
    );

    const pageObj = new LotMetadataPage(page);
    await pageObj.goto('sample-project', '3');
    await pageObj.expectLoaded();

    // Click on the task
    const foundationTask = page.locator('.lot-card:has-text("Foundation Work")');
    await foundationTask.click();

    // Verify navigation to task detail page
    await page.waitForURL('**/tasks/TASK-001');
    expect(page.url()).toContain('/tasks/TASK-001');
  });
});