import { test, expect } from '@playwright/test';

test.describe('Task Details Page - Delete Functionality', () => {
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

  const mockUnassignedTask = {
    ...mockTask,
    taskId: 'task-002',
    taskIdentifier: 'task-002',
    taskTitle: 'Unassigned Task',
    assignedToUserId: null,
  };

  const mockContractors = [
    {
      userId: 'contractor-123',
      userIdentifier: 'contractor-123',
      firstName: 'John',
      lastName: 'Contractor',
    },
  ];

  const mockProject = {
    projectIdentifier: 'proj-001',
    projectName: 'Test Project',
    contractorIds: ['contractor-123'],
  };

  test.beforeEach(async ({ page }) => {
    // Mock Auth0
    await page.route('**/oauth/token', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ access_token: 'mock-token' }),
      });
    });

    // Mock user as OWNER
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
  });

  test('should show Delete button only for OWNER role', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.goto('/tasks/task-001');

    // Check Delete button is visible for owner
    await expect(page.locator('button:has-text("Delete Task")')).toBeVisible();
  });

  test('should not show Delete button for CONTRACTOR role', async ({ page }) => {
    // Override user mock to be contractor
    await page.route('**/api/v1/users/me', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'contractor-123',
          userRole: 'CONTRACTOR',
        }),
      });
    });

    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
      });
    });

    await page.goto('/tasks/task-001');

    // Check Delete button is NOT visible for contractor
    await expect(page.locator('button:has-text("Delete Task")')).not.toBeVisible();
  });

  test('should open confirmation modal when Delete button is clicked', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
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

  test('should cancel deletion when Cancel button is clicked', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockTask),
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

  test('should delete task and navigate to schedule when confirmed', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-001', async route => {
      if (route.request().method() === 'DELETE') {
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

    // Wait for navigation to project schedule
    await page.waitForURL('**/projects/proj-001/schedule');
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

  test('should allow editing unassigned task', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-002', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ ...mockUnassignedTask, taskTitle: 'Updated Title' }),
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockUnassignedTask),
        });
      }
    });

    await page.goto('/tasks/task-002');

    // Verify Edit button is visible for unassigned task
    await expect(page.locator('button:has-text("Edit Task")')).toBeVisible();

    // Click Edit button
    await page.locator('button:has-text("Edit Task")').click();

    // Edit modal should appear
    await expect(page.locator('.schedule-modal')).toBeVisible();
  });

  test('should allow deleting unassigned task', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-002', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 204,
          body: '',
        });
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockUnassignedTask),
        });
      }
    });

    await page.goto('/tasks/task-002');

    // Verify Delete button is visible
    await expect(page.locator('button:has-text("Delete Task")')).toBeVisible();

    // Click Delete button
    await page.locator('button:has-text("Delete Task")').click();

    // Confirm deletion
    await page.locator('.btn-confirm-destructive').click();

    // Should navigate away
    await page.waitForURL('**/projects/proj-001/schedule');
  });

  test('should show "Unassigned" for tasks without contractor', async ({ page }) => {
    await page.route('**/api/v1/tasks/task-002', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockUnassignedTask),
      });
    });

    await page.goto('/tasks/task-002');

    // Check assignee shows "Unassigned"
    await expect(page.locator('.task-meta-block').filter({ hasText: 'Assignee' })).toContainText('Unassigned');
  });
});

