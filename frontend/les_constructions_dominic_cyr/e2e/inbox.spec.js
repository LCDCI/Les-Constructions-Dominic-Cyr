import { test, expect } from '@playwright/test';
import { InboxPage } from './pages/inbox.page.js';

// Helper function for owner login (reused from existing tests)
async function loginAsOwner(page) {
  // Wait for server to be ready by trying to access the base URL
  let retries = 5;
  let lastError;
  while (retries > 0) {
    try {
      const response = await page.goto('/', { waitUntil: 'domcontentloaded', timeout: 10000 });
      if (response && response.status() < 500) {
        break; // Server is ready
      }
    } catch (e) {
      lastError = e;
      await page.waitForTimeout(2000);
      retries--;
    }
  }
  
  if (retries === 0) {
    throw new Error(`Server not ready after multiple attempts. Last error: ${lastError?.message || 'Unknown'}`);
  }

  // Try to navigate to login page with error handling
  try {
    const response = await page.goto('/portal/login', { waitUntil: 'domcontentloaded', timeout: 15000 });
    if (!response || response.status() >= 500) {
      throw new Error(`Failed to load login page. Status: ${response?.status() || 'unknown'}`);
    }
    // Check if we got an error page
    const url = page.url();
    if (url.startsWith('chrome-error://') || url.startsWith('about:')) {
      throw new Error(`Navigation failed. Got error page: ${url}`);
    }
  } catch (e) {
    // If navigation fails, try again after a short wait
    await page.waitForTimeout(2000);
    try {
      const response = await page.goto('/portal/login', { waitUntil: 'domcontentloaded', timeout: 15000 });
      if (!response || response.status() >= 500) {
        throw new Error(`Failed to load login page after retry. Status: ${response?.status() || 'unknown'}`);
      }
      const url = page.url();
      if (url.startsWith('chrome-error://') || url.startsWith('about:')) {
        throw new Error(`Navigation failed after retry. Got error page: ${url}`);
      }
    } catch (retryError) {
      throw new Error(`Failed to navigate to login page: ${retryError.message}`);
    }
  }
  
  await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});

  let currentUrl = page.url();

  const continueToLoginBtn = page.getByRole('button', {
    name: /Continue to Login/i,
  });

  try {
    await continueToLoginBtn.waitFor({ state: 'visible', timeout: 5000 });
    await continueToLoginBtn.click();
    // Wait for Auth0 redirect with shorter timeout - if it doesn't happen, that's okay
    await page.waitForURL(url => url.toString().includes('auth0.com'), {
      timeout: 3000,
    }).catch(() => {
      // Auth0 not available in test environment - that's fine, we'll handle it below
    });
  } catch (e) {
    // If button not visible or Auth0 redirect fails, might already be logged in or mocked
  }

  await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => {});
  currentUrl = page.url();

  if (currentUrl.includes('auth0.com')) {
    await page.getByLabel('Email address').fill('owner@test.com');
    await page.locator('input[type="password"]').fill('Password123');
    await page.getByRole('button', { name: /Continue/i }).click();

    await page.waitForURL(
      url => {
        const urlStr = url.toString();
        return (
          urlStr.includes('localhost:3000') && !urlStr.includes('auth0.com')
        );
      },
      { timeout: 15000 }
    );
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => {});
  } else if (currentUrl.includes('login')) {
    // If we're still on login page and Auth0 didn't redirect, try navigating directly
    // This handles the case where Auth0 is mocked or not available
    await page.goto('/');
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => {});
  }

  currentUrl = page.url();
  // Allow being on any localhost:3000 page, not just the root
  if (!currentUrl.includes('localhost:3000')) {
    throw new Error(
      `Login failed. Expected to be at localhost:3000, but at: ${currentUrl}.`
    );
  }
  await page.waitForTimeout(1000);
}

test.describe('Inbox Page - Display and Filtering', () => {
  let inboxPage;

  const mockNotifications = [
    {
      notificationId: 'notif-001',
      title: 'New Task Assigned',
      message: 'You have been assigned to a new task: Install Electrical Wiring',
      category: 'TASK_ASSIGNED',
      isRead: false,
      link: '/tasks/task-001',
      createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(), // 5 minutes ago
    },
    {
      notificationId: 'notif-002',
      title: 'Project Updated',
      message: 'The project "Panorama Heights" has been updated',
      category: 'PROJECT_UPDATED',
      isRead: false,
      link: '/projects/proj-001',
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30 minutes ago
    },
    {
      notificationId: 'notif-003',
      title: 'Inquiry Received',
      message: 'A new inquiry has been received from John Doe',
      category: 'INQUIRY_RECEIVED',
      isRead: true,
      link: '/inquiries/inq-001',
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), // 2 hours ago
    },
    {
      notificationId: 'notif-004',
      title: 'Schedule Created',
      message: 'A new schedule has been created for your project',
      category: 'SCHEDULE_CREATED',
      isRead: true,
      link: '/projects/proj-001/schedule',
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 1 day ago
    },
  ];

  test.beforeEach(async ({ page }) => {
    // Mock translations and other API endpoints first
    await page.route('**/api/v1/translations/**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });

    await loginAsOwner(page);

    // Mock notifications API
    await page.route('**/api/v1/notifications', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockNotifications),
      });
    });

    await page.route('**/api/v1/notifications/unread-count', async route => {
      const unread = mockNotifications.filter(n => !n.isRead).length;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ count: unread }),
      });
    });

    inboxPage = new InboxPage(page);
  });

  test('should display inbox page with title and unread count', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    await expect(inboxPage.pageTitle).toBeVisible();
    await expect(inboxPage.pageTitle).toHaveText('Inbox');
    await expect(inboxPage.unreadBadge).toBeVisible();
    await expect(inboxPage.unreadBadge).toHaveText(/2 unread/);
  });

  test('should display all notifications when loaded', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    const count = await inboxPage.getNotificationCount();
    expect(count).toBe(4);

    const firstTitle = await inboxPage.getNotificationTitle(0);
    expect(firstTitle).toContain('New Task Assigned');
  });

  test('should filter notifications by unread only', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Select "Unread Only" filter
    await inboxPage.selectFilter('unread');
    await page.waitForTimeout(500);

    const count = await inboxPage.getNotificationCount();
    expect(count).toBe(2); // Only unread notifications

    // Verify all displayed notifications are unread
    const firstIsRead = await inboxPage.isNotificationRead(0);
    expect(firstIsRead).toBe(false);
  });

  test('should filter notifications by read only', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Select "Read Only" filter
    await inboxPage.selectFilter('read');
    await page.waitForTimeout(500);

    const count = await inboxPage.getNotificationCount();
    expect(count).toBe(2); // Only read notifications

    // Verify all displayed notifications are read
    const firstIsRead = await inboxPage.isNotificationRead(0);
    expect(firstIsRead).toBe(true);
  });

  test('should search notifications by title', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Search for "Task"
    await inboxPage.searchNotifications('Task');
    await page.waitForTimeout(500);

    const count = await inboxPage.getNotificationCount();
    expect(count).toBe(1);

    const title = await inboxPage.getNotificationTitle(0);
    expect(title).toContain('Task');
  });

  test('should search notifications by message content', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Search for "Panorama"
    await inboxPage.searchNotifications('Panorama');
    await page.waitForTimeout(500);

    const count = await inboxPage.getNotificationCount();
    expect(count).toBe(1);

    const message = await inboxPage.getNotificationMessage(0);
    expect(message).toContain('Panorama');
  });

  test('should display empty state when no notifications match filters', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Search for something that doesn't exist
    await inboxPage.searchNotifications('NonExistentTerm');
    await page.waitForTimeout(500);

    const emptyMessage = await inboxPage.getEmptyStateMessage();
    expect(emptyMessage).toContain('No notifications match your filters');
  });

  test('should display empty state when no notifications exist', async ({ page }) => {
    // Override mock for this test to return empty array
    await page.route('**/api/v1/notifications', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.route('**/api/v1/notifications/unread-count', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ count: 0 }),
      });
    });

    await inboxPage.goto();
    await inboxPage.waitForReady();

    const emptyMessage = await inboxPage.getEmptyStateMessage();
    expect(emptyMessage).toContain('No notifications yet');
  });

  test('should sort notifications by newest first', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Verify newest first (default)
    await inboxPage.selectSort('newest');
    await page.waitForTimeout(500);

    const firstTitle = await inboxPage.getNotificationTitle(0);
    expect(firstTitle).toContain('New Task Assigned'); // Most recent
  });

  test('should sort notifications by oldest first', async ({ page }) => {
    await inboxPage.goto();
    await inboxPage.waitForReady();

    // Sort by oldest first
    await inboxPage.selectSort('oldest');
    await page.waitForTimeout(500);

    const firstTitle = await inboxPage.getNotificationTitle(0);
    expect(firstTitle).toContain('Schedule Created'); // Oldest
  });

  test('should show loading state while fetching notifications', async ({ page }) => {
    let resolveNotifications;
    const notificationsPromise = new Promise(resolve => {
      resolveNotifications = resolve;
    });

    await page.route('**/api/v1/notifications', async route => {
      await notificationsPromise;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockNotifications),
      });
    });

    await inboxPage.goto();

    // Check loading state appears
    const loadingVisible = await inboxPage.loadingMessage.isVisible().catch(() => false);
    expect(loadingVisible).toBe(true);

    // Resolve the promise to continue
    resolveNotifications();

    await inboxPage.waitForReady();

    // Loading should be gone
    const loadingStillVisible = await inboxPage.loadingMessage.isVisible().catch(() => false);
    expect(loadingStillVisible).toBe(false);
  });

  test('should display error message when API fails', async ({ page }) => {
    // Override mock for this test to return error
    await page.route('**/api/v1/notifications', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Internal Server Error' }),
      });
    });

    await inboxPage.goto();
    await inboxPage.waitForReady();

    const hasError = await inboxPage.hasError();
    expect(hasError).toBe(true);

    const errorText = await inboxPage.getErrorText();
    expect(errorText).toContain('Error loading notifications');
  });
});

test.describe('Inbox Page - Navigation', () => {
  test('should navigate to inbox page directly', async ({ page }) => {
    // Mock translations first
    await page.route('**/api/v1/translations/**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });

    await loginAsOwner(page);

    await page.route('**/api/v1/notifications', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.route('**/api/v1/notifications/unread-count', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ count: 0 }),
      });
    });

    await page.goto('/inbox');
    await expect(page).toHaveURL(/.*inbox/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    // Mock translations first
    await page.route('**/api/v1/translations/**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });

    await loginAsOwner(page);

    await page.route('**/api/v1/notifications', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    await page.route('**/api/v1/notifications/unread-count', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ count: 0 }),
      });
    });

    await page.goto('/');
    await page.goto('/inbox');
    await page.goBack();
    await expect(page).toHaveURL(/\/$/);
  });
});
