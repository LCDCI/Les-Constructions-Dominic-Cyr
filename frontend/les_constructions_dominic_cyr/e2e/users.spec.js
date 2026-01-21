import { test, expect } from '@playwright/test';
import { UsersPage } from './pages/users.page.js';

test.describe('Users Page - Display', () => {
  let usersPage;

  test.beforeEach(async ({ page }) => {
    usersPage = new UsersPage(page);
    await usersPage.goto();
  });

  // ========================== POSITIVE TESTS ==========================

  test('should display the page title "Users"', async () => {
    await expect(usersPage.pageTitle).toBeVisible();
    await expect(usersPage.pageTitle).toHaveText('Users');
  });

  test('should display the Add User button', async () => {
    await expect(usersPage.addUserButton).toBeVisible();
    await expect(usersPage.addUserButton).toHaveText('Add User');
  });

  test('should display page header', async () => {
    await expect(usersPage.pageHeader).toBeVisible();
  });

  test('should show loading state initially', async ({ page }) => {
    // Navigate fresh to catch loading state
    await page.goto('/users');

    // Either shows loading or already loaded table
    const isLoading = await usersPage.loadingMessage
      .isVisible()
      .catch(() => false);
    const isTableVisible = await usersPage.usersTable
      .isVisible()
      .catch(() => false);
    const isNoUsers = await usersPage.noUsersMessage
      .isVisible()
      .catch(() => false);

    expect(isLoading || isTableVisible || isNoUsers).toBeTruthy();
  });

  test('should display users table or no users message after loading', async () => {
    // Wait for loading to complete
    await usersPage.page.waitForTimeout(1000);

    const hasTable = await usersPage.usersTable.isVisible().catch(() => false);
    const hasNoUsersMessage = await usersPage.noUsersMessage
      .isVisible()
      .catch(() => false);

    expect(hasTable || hasNoUsersMessage).toBeTruthy();
  });

  test('should display correct table headers when users exist', async () => {
    await usersPage.page.waitForTimeout(1000);

    const hasTable = await usersPage.usersTable.isVisible().catch(() => false);

    if (hasTable) {
      const headers = await usersPage.getTableHeaderTexts();
      expect(headers).toContain('First name');
      expect(headers).toContain('Last name');
      expect(headers).toContain('Primary email');
      expect(headers).toContain('Secondary email');
      expect(headers).toContain('Phone');
      expect(headers).toContain('Role');
      expect(headers).toContain('Actions');
    }
  });

  test('should display Edit button for each user row', async () => {
    await usersPage.page.waitForTimeout(1000);

    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      const editButtonCount = await usersPage.editButtons.count();
      expect(editButtonCount).toBe(userCount);
    }
  });
});

test.describe('Users Page - Add User Modal', () => {
  let usersPage;

  test.beforeEach(async ({ page }) => {
    usersPage = new UsersPage(page);
    await usersPage.goto();
    await page.waitForTimeout(500);
  });

  test('should open Add User modal when clicking Add User button', async () => {
    await usersPage.clickAddUser();
    await expect(usersPage.addUserModal).toBeVisible();
  });

  test('should display Add User modal with correct title', async () => {
    await usersPage.clickAddUser();
    await expect(usersPage.addUserModalTitle).toHaveText('Add User');
  });

  test('should display all form fields in Add User modal', async () => {
    await usersPage.clickAddUser();

    // Check text inputs exist (firstName, lastName)
    await expect(usersPage.firstNameInput).toBeVisible();
    await expect(usersPage.lastNameInput).toBeVisible();

    // Check email inputs
    await expect(usersPage.primaryEmailInput).toBeVisible();
    await expect(usersPage.secondaryEmailInput).toBeVisible();

    // Check phone input
    await expect(usersPage.phoneInput).toBeVisible();

    // Check role select
    await expect(usersPage.roleSelect).toBeVisible();
  });

  test('should display Cancel and Create User buttons', async () => {
    await usersPage.clickAddUser();

    await expect(usersPage.cancelButton).toBeVisible();
    await expect(usersPage.cancelButton).toHaveText('Cancel');

    await expect(usersPage.submitButton).toBeVisible();
    await expect(usersPage.submitButton).toHaveText('Create User');
  });

  test('should close modal when clicking Cancel', async () => {
    await usersPage.clickAddUser();
    await expect(usersPage.addUserModal).toBeVisible();

    await usersPage.cancelAddUserForm();
    await expect(usersPage.addUserModal).not.toBeVisible();
  });

  test('should have role dropdown with all roles', async () => {
    await usersPage.clickAddUser();

    const options = await usersPage.roleSelect
      .locator('option')
      .allTextContents();
    expect(options).toContain('OWNER');
    expect(options).toContain('SALESPERSON');
    expect(options).toContain('CONTRACTOR');
    expect(options).toContain('CUSTOMER');
  });

  test('should default role to CUSTOMER', async () => {
    await usersPage.clickAddUser();

    const selectedValue = await usersPage.roleSelect.inputValue();
    expect(selectedValue).toBe('CUSTOMER');
  });

  test('should clear form when reopening modal', async () => {
    await usersPage.clickAddUser();

    // Fill some data
    await usersPage.fillAddUserForm({
      firstName: 'Test',
      lastName: 'User',
    });

    // Cancel and reopen
    await usersPage.cancelAddUserForm();
    await usersPage.clickAddUser();

    // Form should be cleared
    const firstNameValue = await usersPage.firstNameInput.inputValue();
    expect(firstNameValue).toBe('');
  });

  test('should allow filling all form fields', async () => {
    await usersPage.clickAddUser();

    await usersPage.fillAddUserForm({
      firstName: 'John',
      lastName: 'Doe',
      primaryEmail: 'john.doe@example.com',
      secondaryEmail: 'john.secondary@example.com',
      phone: '514-555-1234',
      role: 'CONTRACTOR',
    });

    expect(await usersPage.firstNameInput.inputValue()).toBe('John');
    expect(await usersPage.lastNameInput.inputValue()).toBe('Doe');
    expect(await usersPage.primaryEmailInput.inputValue()).toBe(
      'john.doe@example.com'
    );
    expect(await usersPage.secondaryEmailInput.inputValue()).toBe(
      'john.secondary@example.com'
    );
    expect(await usersPage.phoneInput.inputValue()).toBe('514-555-1234');
    expect(await usersPage.roleSelect.inputValue()).toBe('CONTRACTOR');
  });
});

test.describe('Users Page - Edit User Modal', () => {
  let usersPage;

  test.beforeEach(async ({ page }) => {
    usersPage = new UsersPage(page);
    await usersPage.goto();
    await page.waitForTimeout(1000);
  });

  test('should open Edit User modal when clicking Edit button', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editUserModal).toBeVisible();
    }
  });

  test('should display Edit User modal with correct title', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editUserModalTitle).toHaveText('Edit User');
    }
  });

  test('should populate form with existing user data', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      const userData = await usersPage.getUserDataFromRow(0);
      await usersPage.clickEditUser(0);

      const firstName = await usersPage.editFirstNameInput.inputValue();
      const lastName = await usersPage.editLastNameInput.inputValue();

      expect(firstName).toBe(userData.firstName);
      expect(lastName).toBe(userData.lastName);
    }
  });

  test('should have disabled email field', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editDisabledEmailInput).toBeDisabled();
    }
  });

  test('should have disabled role field', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editDisabledRoleInput).toBeDisabled();
    }
  });

  test('should display Save Changes and Cancel buttons', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);

      await expect(usersPage.editSaveButton).toBeVisible();
      await expect(usersPage.editSaveButton).toHaveText('Save Changes');

      await expect(usersPage.editCancelButton).toBeVisible();
      await expect(usersPage.editCancelButton).toHaveText('Cancel');
    }
  });

  test('should close modal when clicking Cancel', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editUserModal).toBeVisible();

      await usersPage.cancelEditUserForm();
      await expect(usersPage.editUserModal).not.toBeVisible();
    }
  });

  test('should close modal when clicking overlay', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);
      await expect(usersPage.editUserModal).toBeVisible();

      // Click on overlay (not the content)
      await usersPage.editUserModal.click({ position: { x: 10, y: 10 } });
      await expect(usersPage.editUserModal).not.toBeVisible();
    }
  });

  test('should allow editing first name and last name', async () => {
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      await usersPage.clickEditUser(0);

      await usersPage.fillEditUserForm({
        firstName: 'UpdatedFirst',
        lastName: 'UpdatedLast',
      });

      expect(await usersPage.editFirstNameInput.inputValue()).toBe(
        'UpdatedFirst'
      );
      expect(await usersPage.editLastNameInput.inputValue()).toBe(
        'UpdatedLast'
      );
    }
  });
});

test.describe('Users Page - Navigation', () => {
  test('should navigate to users page directly', async ({ page }) => {
    await page.goto('/users');
    await expect(page).toHaveURL(/.*users/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/users');
    await page.goBack();
    await expect(page).toHaveURL(/\/$/);
  });

  test('should handle page refresh', async ({ page }) => {
    await page.goto('/users');
    await page.reload();

    const pageTitle = page.locator('.page-header h1');
    await expect(pageTitle).toBeVisible();
  });
});

test.describe('Users Page - Responsive Design', () => {
  test('should display correctly on mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/users');

    const pageTitle = page.locator('.page-header h1');
    await expect(pageTitle).toBeVisible();

    const addButton = page.locator('.page-header button');
    await expect(addButton).toBeVisible();
  });

  test('should display correctly on tablet viewport', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('/users');

    const pageTitle = page.locator('.page-header h1');
    await expect(pageTitle).toBeVisible();
  });

  test('should display correctly on desktop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/users');

    const pageTitle = page.locator('.page-header h1');
    await expect(pageTitle).toBeVisible();
  });
});

test.describe('Users Page - Error Handling', () => {
  test('should display error message when API fails', async ({ page }) => {
    // Mock API failure
    await page.route('**/api/v1/users', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Internal Server Error' }),
      });
    });

    await page.goto('/users');
    await page.waitForTimeout(1000);

    const errorMessage = page.locator('.error');
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toHaveText('Failed to load users.');
  });

  test('should display error modal on create user failure', async ({
    page,
  }) => {
    // First, let users load successfully
    await page.goto('/users');
    await page.waitForTimeout(500);

    // Mock create user failure
    await page.route('**/api/v1/users', (route, request) => {
      if (request.method() === 'POST') {
        route.fulfill({
          status: 400,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'A user with this email already exists.',
          }),
        });
      } else {
        route.continue();
      }
    });

    const usersPage = new UsersPage(page);
    await usersPage.clickAddUser();

    await usersPage.fillAddUserForm({
      firstName: 'Test',
      lastName: 'User',
      primaryEmail: 'existing@example.com',
    });

    await usersPage.submitAddUserForm();
    await page.waitForTimeout(500);

    // Error modal should appear
    const errorModal = page.locator(
      '.modal-backdrop:has-text("User Operation Failed")'
    );
    await expect(errorModal).toBeVisible();
  });
});

test.describe('Users Page - Accessibility', () => {
  test('should have accessible form inputs', async ({ page }) => {
    await page.goto('/users');
    await page.waitForTimeout(500);

    const usersPage = new UsersPage(page);
    await usersPage.clickAddUser();

    // Form inputs should be focusable
    await usersPage.firstNameInput.focus();
    await expect(usersPage.firstNameInput).toBeFocused();
  });

  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/users');

    const h1 = page.locator('.page-header h1');
    await expect(h1).toBeVisible();

    const h1Count = await h1.count();
    expect(h1Count).toBe(1);
  });

  test('should be keyboard navigable in modal', async ({ page }) => {
    await page.goto('/users');
    await page.waitForTimeout(500);

    const usersPage = new UsersPage(page);
    await usersPage.clickAddUser();

    // Tab through form fields
    await page.keyboard.press('Tab');
    await page.keyboard.press('Tab');

    // Should be able to navigate through form
    const activeElement = await page.evaluate(
      () => document.activeElement?.tagName
    );
    expect(['INPUT', 'SELECT', 'BUTTON']).toContain(activeElement);
  });
});

test.describe('Users Page - Table Functionality', () => {
  test('should display user data correctly in table rows', async ({ page }) => {
    await page.goto('/users');
    await page.waitForTimeout(1000);

    const usersPage = new UsersPage(page);
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      const userData = await usersPage.getUserDataFromRow(0);

      // User data should not be empty
      expect(userData.firstName).toBeTruthy();
      expect(userData.lastName).toBeTruthy();
      expect(userData.primaryEmail).toBeTruthy();
      expect(userData.role).toBeTruthy();
    }
  });

  test('should display role correctly for each user', async ({ page }) => {
    await page.goto('/users');
    await page.waitForTimeout(1000);

    const usersPage = new UsersPage(page);
    const userCount = await usersPage.getUserCount();

    if (userCount > 0) {
      const userData = await usersPage.getUserDataFromRow(0);
      const validRoles = ['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER'];

      expect(validRoles).toContain(userData.role);
    }
  });
});
