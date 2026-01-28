import { test, expect } from '@playwright/test';

test.describe('Task Details Page', () => {
  const mockTask = {
    taskId: 'task-001',
    taskIdentifier: 'task-001',
    taskTitle: 'Install Electrical Wiring',
    taskDescription: 'Install electrical wiring in the main floor',
    taskStatus: 'IN_PROGRESS',
    taskPriority: 'HIGH',
    periodStart: '2026-01-20',
    periodEnd: '2026-01-25',
    estimatedHours: 40,
    hoursSpent: 20,
    taskProgress: 50,
    assignedToUserId: 'contractor-123',
    projectId: 'proj-001',
    scheduleId: 'schedule-001',
  };

  const mockContractors = [
    {
      userId: 'contractor-123',
      userIdentifier: 'contractor-123',
      firstName: 'John',
      lastName: 'Contractor',
    },
    {
      userId: 'contractor-456',
      userIdentifier: 'contractor-456',
      firstName: 'Jane',
      lastName: 'Builder',
    },
  ];

  const mockProject = {
    projectIdentifier: 'proj-001',
    projectName: 'Test Project',
    contractorIds: ['contractor-123', 'contractor-456'],
  };

  test.beforeEach(async ({ page }) => {
    // Mock Auth0 token
    await page.route('**/oauth/token', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ access_token: 'mock-token' }),
      });
    });

    // Mock user endpoint
    await page.route('**/api/v1/users/me', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'owner-001',
          userRole: 'OWNER',
        }),
      });
    });
  });

  test('should display task details correctly', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Check task title
    await expect(page.locator('h1')).toContainText('Install Electrical Wiring');

    // Check task ID
    await expect(page.locator('.task-subtle')).toContainText('task-001');

    // Check status
    await expect(page.locator('.task-meta-block').filter({ hasText: 'Status' })).toContainText('IN_PROGRESS');

    // Check priority
    await expect(page.locator('.task-meta-block').filter({ hasText: 'Priority' })).toContainText('HIGH');

    // Check assignee
    await expect(page.locator('.task-meta-block').filter({ hasText: 'Assignee' })).toContainText('John Contractor');

    // Check description
    await expect(page.locator('.task-body-text')).toContainText('Install electrical wiring in the main floor');

    // Check time stats
    await expect(page.locator('.task-stat').filter({ hasText: 'Estimated' })).toContainText('40');
    await expect(page.locator('.task-stat').filter({ hasText: 'Spent' })).toContainText('20');
    await expect(page.locator('.task-stat').filter({ hasText: 'Progress' })).toContainText('50%');
  });

  test('should display unassigned task correctly', async ({ page }) => {
    const unassignedTask = { ...mockTask, assignedToUserId: null };

    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(unassignedTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Check assignee shows "Unassigned"
    await expect(page.locator('.task-meta-block').filter({ hasText: 'Assignee' })).toContainText('Unassigned');
  });

  test('should show Edit button for owner', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Check Edit button is visible
    await expect(page.locator('button:has-text("Edit Task")')).toBeVisible();
  });

  test('should show Delete button only for owner', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Check Delete button is visible for owner
    await expect(page.locator('button:has-text("Delete Task")')).toBeVisible();
  });

  test('should open confirmation modal when Delete is clicked', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Click Delete button
    await page.locator('button:has-text("Delete Task")').click();

    // Check confirmation modal appears
    await expect(page.locator('.confirmation-modal-overlay')).toBeVisible();
    await expect(page.locator('#modal-title')).toContainText('Delete Task');
    await expect(page.locator('#modal-description')).toContainText('Are you sure you want to delete this task?');
  });

  test('should cancel deletion when Cancel is clicked in modal', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Click Delete button
    await page.locator('button:has-text("Delete Task")').click();

    // Click Cancel in modal
    await page.locator('.btn-cancel').click();

    // Modal should close
    await expect(page.locator('.confirmation-modal-overlay')).not.toBeVisible();

    // Task should still be visible
    await expect(page.locator('h1')).toContainText('Install Electrical Wiring');
  });

  test('should delete task and navigate back when confirmed', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    let deleteRequested = false;
    await page.route('**/api/v1/tasks/task-001', async route => {
      if (route.request().method() === 'DELETE') {
        deleteRequested = true;
        await route.fulfill({
          status: 204,
          body: '',
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockTask),
        });
      }
    });

    await page.goto('/tasks/task-001');

    // Click Delete button
    await page.locator('button:has-text("Delete Task")').click();

    // Confirm deletion
    await page.locator('.btn-confirm-destructive').click();

    // Wait for navigation
    await page.waitForURL('**/projects/proj-001/schedule');

    // Verify delete was called
    expect(deleteRequested).toBe(true);
  });

  test('should show error message if deletion fails', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ message: 'Internal Server Error' }),
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockTask),
        });
      }
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Click Delete button
    await page.locator('button:has-text("Delete Task")').click();

    // Confirm deletion
    await page.locator('.btn-confirm-destructive').click();

    // Wait a moment for the error to appear
    await page.waitForTimeout(500);

    // Error should be displayed in the modal
    await expect(page.locator('#modal-description')).toContainText('Error:');
  });

  test('should open edit modal when Edit Task is clicked', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Click Edit button
    await page.locator('button:has-text("Edit Task")').click();

    // Check edit modal appears
    await expect(page.locator('.schedule-modal')).toBeVisible();
    await expect(page.locator('.schedule-modal-title')).toContainText('Edit Task');
  });

  test('should allow editing unassigned task', async ({ page }) => {
    const unassignedTask = { ...mockTask, assignedToUserId: null };

    await page.route('**/api/v1/tasks/task-001', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...unassignedTask, taskTitle: 'Updated Title' }),
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(unassignedTask),
        });
      }
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    await page.goto('/tasks/task-001');

    // Verify Edit button is visible for unassigned task
    await expect(page.locator('button:has-text("Edit Task")')).toBeVisible();

    // Click Edit button
    await page.locator('button:has-text("Edit Task")').click();

    // Edit modal should appear
    await expect(page.locator('.schedule-modal')).toBeVisible();
  });

  test('should display loading state correctly', async ({ page }) => {
    // Delay the API response
    await page.route('**/api/v1/tasks/task-001', async route => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.goto('/tasks/task-001');

    // Check loading state
    await expect(page.locator('.schedule-loading')).toBeVisible();
    await expect(page.locator('text=Loading task...')).toBeVisible();
  });

  test('should display error state when task not found', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Task not found' }),
      });
    });

    await page.goto('/tasks/task-001');

    // Check error state
    await expect(page.locator('.schedule-error')).toBeVisible();
    await expect(page.locator('.schedule-error')).toContainText('Task not found');
  });

  test('should navigate back when Back button is clicked', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.route('**/api/v1/projects/proj-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockProject),
      });
    });

    await page.route('**/api/v1/users?role=CONTRACTOR', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockContractors),
      });
    });

    // Navigate from a specific page
    await page.goto('/projects/proj-001/schedule');
    await page.goto('/tasks/task-001');

    // Click Back button
    await page.locator('button:has-text("← Back")').click();

    // Should navigate back
    await page.waitForURL('**/projects/proj-001/schedule');
  });
});

