export class UsersPage {
  constructor(page) {
    this.page = page;
    this.loadingMessage = page.getByText('Loading users...');
    this.tableRows = page.locator('tbody tr');

    this.editUserModal = page.locator('.modal-overlay');
    this.ownerEditModal = page.locator('.modal-overlay');
    this.statusModal = page.locator('.modal-overlay');

    this.editFirstNameInput = page.locator('input[name="firstName"]');
    this.editPhoneInput = page.locator('input[name="phone"]');
    this.saveButton = page.getByRole('button', { name: /Save Changes/i });

    this.deactivateOption = page.getByText('Deactivate User');
    this.setInactiveOption = page.getByText('Set as Inactive');
    this.reactivateOption = page.getByText('Reactivate User');
    this.confirmButton = page.getByRole('button', { name: 'Confirm' });
  }

  async goto() {
    const currentUrl = this.page.url();

    if (!currentUrl.includes('/users')) {
      await this.page.goto('/users');
      await this.page.waitForLoadState('domcontentloaded');

      await this.page.waitForTimeout(1000);

      const newUrl = this.page.url();

      if (!newUrl.includes('/users')) {
        throw new Error(
          `Failed to navigate to users page. Current URL: ${newUrl}`
        );
      }
    }

    await this.page.getByRole('heading', { name: 'Users' }).waitFor({
      state: 'visible',
      timeout: 10000,
    });

    const isLoading = await this.loadingMessage.isVisible().catch(() => false);
    if (isLoading) {
      await this.loadingMessage.waitFor({ state: 'hidden', timeout: 15000 });
    }

    await this.page.waitForSelector('tbody tr', {
      state: 'visible',
      timeout: 10000,
    });
  }

  getRowByEmail(email) {
    return this.page.locator('tr', { hasText: email });
  }

  async openEditModal(email) {
    const row = this.getRowByEmail(email);
    await row.waitFor({ state: 'visible', timeout: 5000 });

    const editButton = row.getByRole('button', { name: 'Edit' });
    await editButton.waitFor({ state: 'visible', timeout: 5000 });
    await editButton.click();

    // Wait a bit for modal animation
    await this.page.waitForTimeout(500);
  }

  async openStatusModal(email) {
    const row = this.getRowByEmail(email);
    await row.waitFor({ state: 'visible', timeout: 5000 });

    const statusButton = row.getByRole('button', { name: 'Manage Status' });
    await statusButton.waitFor({ state: 'visible', timeout: 5000 });
    await statusButton.click();

    // Wait a bit for modal animation
    await this.page.waitForTimeout(500);
  }
}
