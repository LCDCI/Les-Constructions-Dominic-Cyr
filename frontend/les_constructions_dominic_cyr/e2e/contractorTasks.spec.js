import { test, expect } from '@playwright/test';

test.describe('Contractor Tasks Page', () => {
  const mockTasks = [
    {
      taskId: 'task-001',
      taskIdentifier: 'task-001',
      taskTitle: 'Site Preparation',
      taskDescription: 'Prepare the site for construction',
      taskStatus: 'COMPLETED',
      taskPriority: 'HIGH',
      periodStart: '2025-11-27',
      periodEnd: '2025-11-29',
      estimatedHours: 16,
      hoursSpent: 16,
      taskProgress: 100,
      assignedToUserId: 'contractor-123',
      assignedToUserName: 'Jane Contractor',
      projectIdentifier: 'foresta',
      projectName: 'Fôresta',
      lotId: '53',
      lotNumber: 'Lot 53',
      scheduleId: 'schedule-001',
    },
    {
      taskId: 'task-002',
      taskIdentifier: 'task-002',
      taskTitle: 'Foundation Work',
      taskDescription: 'Pour foundation for the building',
      taskStatus: 'IN_PROGRESS',
      taskPriority: 'VERY_HIGH',
      periodStart: '2025-11-30',
      periodEnd: '2025-12-05',
      estimatedHours: 40,
      hoursSpent: 20,
      taskProgress: 50,
      assignedToUserId: 'contractor-123',
      assignedToUserName: 'Jane Contractor',
      projectIdentifier: 'foresta',
      projectName: 'Fôresta',
      lotId: '53',
      lotNumber: 'Lot 53',
      scheduleId: 'schedule-002',
    },
    {
      taskId: 'task-003',
      taskIdentifier: 'task-003',
      taskTitle: 'Framing',
      taskDescription: 'Frame the structure',
      taskStatus: 'TO_DO',
      taskPriority: 'MEDIUM',
      periodStart: '2025-12-06',
      periodEnd: '2025-12-12',
      estimatedHours: 60,
      hoursSpent: 0,
      taskProgress: 0,
      assignedToUserId: 'contractor-123',
      assignedToUserName: 'Jane Contractor',
      projectIdentifier: 'foresta',
      projectName: 'Fôresta',
      lotId: '54',
      lotNumber: 'Lot 54',
      scheduleId: 'schedule-003',
    },
    {
      taskId: 'task-004',
      taskIdentifier: 'task-004',
      taskTitle: 'Electrical Installation',
      taskDescription: 'Install electrical systems',
      taskStatus: 'TO_DO',
      taskPriority: 'LOW',
      periodStart: '2025-12-13',
      periodEnd: '2025-12-18',
      estimatedHours: 32,
      hoursSpent: 0,
      taskProgress: 0,
      assignedToUserId: 'contractor-123',
      assignedToUserName: 'Jane Contractor',
      projectIdentifier: 'maple-hills',
      projectName: 'Maple Hills',
      lotId: '10',
      lotNumber: 'Lot 10',
      scheduleId: 'schedule-004',
    },
  ];

  const mockProjects = [
    {
      projectIdentifier: 'foresta',
      projectName: 'Fôresta',
    },
    {
      projectIdentifier: 'maple-hills',
      projectName: 'Maple Hills',
    },
  ];

  test.beforeEach(async ({ page }) => {
    // Mock Auth0
    await page.route('**/oauth/token', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ access_token: 'mock-token' }),
      });
    });

    // Mock user profile
    await page.route('**/api/v1/users/me', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'contractor-123',
          userIdentifier: 'contractor-123',
          firstName: 'Jane',
          lastName: 'Contractor',
          userRole: 'CONTRACTOR',
        }),
      });
    });

    // Mock tasks API
    await page.route('**/api/v1/tasks/contractor-view', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTasks),
      });
    });

    // Mock projects API
    await page.route('**/api/v1/projects', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProjects),
      });
    });

    // Mock translations
    await page.route('**/api/v1/translations/**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          contractorTasks: {
            title: 'My Assigned Tasks',
            loading: 'Loading tasks...',
            retry: 'Retry',
            tasksFound: '{{count}} tasks found',
            noTasks: 'No tasks found matching your filters.',
          },
        }),
      });
    });
  });

  test('should load and display contractor tasks page', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');

    // Wait for tasks to load
    await page.waitForSelector('.contractor-tasks-page');

    // Check page title
    await expect(page.locator('h1')).toContainText('My Assigned Tasks');

    // Check task count
    await expect(page.locator('.subtitle')).toContainText('4');

    // Check that tasks are grouped by project
    await expect(page.locator('.project-header h2')).toHaveCount(2);
    await expect(page.locator('.project-header h2').first()).toContainText('Fôresta');
  });

  test('should display all filters', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.filters-container');

    // Check all filters are present
    await expect(page.locator('#project-filter')).toBeVisible();
    await expect(page.locator('#lot-filter')).toBeVisible();
    await expect(page.locator('#status-filter')).toBeVisible();
    await expect(page.locator('#priority-filter')).toBeVisible();
  });

  test('should filter tasks by project', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select Fôresta project
    await page.selectOption('#project-filter', 'foresta');

    // Should show only 3 tasks from Fôresta
    await page.waitForTimeout(500); // Wait for filter to apply
    const taskRows = page.locator('.task-row');
    await expect(taskRows).toHaveCount(3);
  });

  test('should filter tasks by lot', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select a specific lot
    await page.selectOption('#lot-filter', '53');

    // Should show only tasks for Lot 53
    await page.waitForTimeout(500);
    const taskRows = page.locator('.task-row');
    await expect(taskRows).toHaveCount(2);
  });

  test('should filter tasks by status', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select IN_PROGRESS status
    await page.selectOption('#status-filter', 'IN_PROGRESS');

    // Should show only 1 task with IN_PROGRESS status
    await page.waitForTimeout(500);
    const taskRows = page.locator('.task-row');
    await expect(taskRows).toHaveCount(1);
    await expect(taskRows.first().locator('.task-title')).toContainText('Foundation Work');
  });

  test('should filter tasks by priority', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select HIGH priority
    await page.selectOption('#priority-filter', 'HIGH');

    // Should show only 1 task with HIGH priority
    await page.waitForTimeout(500);
    const taskRows = page.locator('.task-row');
    await expect(taskRows).toHaveCount(1);
    await expect(taskRows.first().locator('.task-title')).toContainText('Site Preparation');
  });

  test('should combine multiple filters', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select project and status
    await page.selectOption('#project-filter', 'foresta');
    await page.selectOption('#status-filter', 'TO_DO');

    // Should show only TO_DO tasks from Fôresta project
    await page.waitForTimeout(500);
    const taskRows = page.locator('.task-row');
    await expect(taskRows).toHaveCount(1);
    await expect(taskRows.first().locator('.task-title')).toContainText('Framing');
  });

  test('should display task details correctly', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Check first task details
    const firstTask = page.locator('.task-row').first();
    await expect(firstTask.locator('.task-title')).toContainText('Site Preparation');
    await expect(firstTask.locator('.status-badge')).toBeVisible();
    await expect(firstTask.locator('.priority-badge')).toBeVisible();
    await expect(firstTask.locator('.progress-bar-fill')).toBeVisible();
  });

  test('should navigate to task details when clicking a task', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Click on the first task
    const firstTask = page.locator('.task-row').first();
    await firstTask.click();

    // Should navigate to task details page
    await page.waitForURL('**/tasks/task-001');
  });

  test('should show "no tasks" message when no tasks match filters', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select a status that no task has
    await page.selectOption('#status-filter', 'ON_HOLD');

    // Should show no tasks message
    await page.waitForTimeout(500);
    await expect(page.locator('.no-tasks-message')).toBeVisible();
    await expect(page.locator('.no-tasks-message')).toContainText('No tasks found');
  });

  test('should display lot headers with task counts', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Check lot headers
    const lotHeaders = page.locator('.lot-header h3');
    await expect(lotHeaders.first()).toContainText('Lot');

    // Check task count badges
    const taskCountBadges = page.locator('.task-count-badge');
    await expect(taskCountBadges.first()).toBeVisible();
  });

  test('should display progress bars correctly', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Check that progress bars exist
    const progressBars = page.locator('.progress-bar-fill');
    await expect(progressBars).toHaveCount(4);

    // First task should have 100% progress
    const firstProgress = progressBars.first();
    await expect(firstProgress).toHaveCSS('width', /%/);
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Override route to return error
    await page.route('**/api/v1/tasks/contractor-view', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'Internal Server Error' }),
      });
    });

    await page.goto('http://localhost:3000/contractor/tasks');

    // Should show error message
    await page.waitForSelector('.error-container');
    await expect(page.locator('.error-message')).toBeVisible();
    await expect(page.locator('button')).toContainText('Retry');
  });

  test('should show loading state', async ({ page }) => {
    // Delay the API response to see loading state
    await page.route('**/api/v1/tasks/contractor-view', async route => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTasks),
      });
    });

    await page.goto('http://localhost:3000/contractor/tasks');

    // Should show loading spinner
    await expect(page.locator('.loading-spinner')).toBeVisible();
    await expect(page.locator('text=Loading tasks...')).toBeVisible();

    // Wait for tasks to load
    await page.waitForSelector('.contractor-tasks-page .tasks-grouped-container');
  });

  test('should reset lot filter when project changes', async ({ page }) => {
    await page.goto('http://localhost:3000/contractor/tasks');
    await page.waitForSelector('.contractor-tasks-page');

    // Select a lot
    await page.selectOption('#lot-filter', '53');

    // Change project
    await page.selectOption('#project-filter', 'maple-hills');

    // Lot filter should reset to "All Lots"
    await page.waitForTimeout(300);
    const lotSelect = page.locator('#lot-filter');
    await expect(lotSelect).toHaveValue('all');
  });
});

