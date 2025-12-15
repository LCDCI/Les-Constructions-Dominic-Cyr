export class UsersPage {
  constructor(page) {
    this.page = page;
    this.pageHeader = page.locator('.page-header');
    this.pageTitle = page.locator('.page-header h1');
    this.addUserButton = page.locator('.page-header button');
    this.usersTable = page.locator('.users-table');
    this.tableRows = page.locator('.users-table tbody tr');
    this.tableHeaders = page.locator('.users-table thead th');
    this.editButtons = page.locator('.edit-user-button');
    this.noUsersMessage = page.locator('text=No users found.');
    this.loadingMessage = page.locator('text=Loading users...');
    this.errorMessage = page.locator('.error');
    
    // Add User Modal
    this.addUserModal = page.locator('.modal-backdrop');
    this.addUserModalTitle = page.locator('.modal h2');
    this.firstNameInput = page.locator('input[type="text"]').first();
    this.lastNameInput = page.locator('input[type="text"]').nth(1);
    this.primaryEmailInput = page.locator('input[type="email"]').first();
    this.secondaryEmailInput = page.locator('input[type="email"]').nth(1);
    this.phoneInput = page.locator('input[type="tel"]');
    this.roleSelect = page.locator('select');
    this.cancelButton = page.locator('.modal-actions button[type="button"]');
    this.submitButton = page.locator('.modal-actions button[type="submit"]');
    
    // Edit User Modal
    this.editUserModal = page.locator('.modal-overlay');
    this.editUserModalTitle = page.locator('.modal-content h2');
    this.editFirstNameInput = page.locator('.modal-content input[name="firstName"]');
    this.editLastNameInput = page.locator('.modal-content input[name="lastName"]');
    this.editPhoneInput = page.locator('.modal-content input[name="phone"]');
    this.editSecondaryEmailInput = page.locator('.modal-content input[name="secondaryEmail"]');
    this.editDisabledEmailInput = page.locator('.modal-content .disabled-input').first();
    this.editDisabledRoleInput = page.locator('.modal-content .disabled-input').nth(1);
    this.editSaveButton = page.locator('.modal-content .btn-primary');
    this.editCancelButton = page.locator('.modal-content .btn-secondary');
    
    // Invite Link Modal
    this.inviteModal = page.locator('.modal-backdrop:has-text("User Invite Link")');
    this.inviteLinkInput = page.locator('.invite-link-row input');
    this.copyButton = page.locator('.invite-link-row button');
    this.copyStatus = page.locator('.copy-status');
    
    // Error Modal
    this.errorModal = page.locator('.modal-backdrop:has-text("User Operation Failed")');
    this.errorModalMessage = page.locator('.modal p');
    this.errorModalCloseButton = page.locator('.modal-actions button:has-text("Close")');
  }

  async goto() {
    await this.page.goto('/users');
  }

  async isLoaded() {
    return await this.pageTitle.isVisible();
  }

  async isLoading() {
    return await this.loadingMessage.isVisible();
  }

  async getUserCount() {
    return await this.tableRows.count();
  }

  async clickAddUser() {
    await this.addUserButton.click();
  }

  async clickEditUser(index = 0) {
    await this.editButtons.nth(index).click();
  }

  async fillAddUserForm({ firstName, lastName, primaryEmail, secondaryEmail, phone, role }) {
    if (firstName) await this.firstNameInput.fill(firstName);
    if (lastName) await this.lastNameInput.fill(lastName);
    if (primaryEmail) await this.primaryEmailInput.fill(primaryEmail);
    if (secondaryEmail) await this.secondaryEmailInput.fill(secondaryEmail);
    if (phone) await this.phoneInput.fill(phone);
    if (role) await this.roleSelect.selectOption(role);
  }

  async fillEditUserForm({ firstName, lastName, phone, secondaryEmail }) {
    if (firstName) {
      await this.editFirstNameInput.clear();
      await this.editFirstNameInput.fill(firstName);
    }
    if (lastName) {
      await this.editLastNameInput.clear();
      await this.editLastNameInput.fill(lastName);
    }
    if (phone) {
      await this.editPhoneInput.clear();
      await this.editPhoneInput.fill(phone);
    }
    if (secondaryEmail) {
      await this.editSecondaryEmailInput.clear();
      await this.editSecondaryEmailInput.fill(secondaryEmail);
    }
  }

  async submitAddUserForm() {
    await this.submitButton.click();
  }

  async submitEditUserForm() {
    await this.editSaveButton.click();
  }

  async cancelAddUserForm() {
    await this.cancelButton.click();
  }

  async cancelEditUserForm() {
    await this.editCancelButton.click();
  }

  async closeInviteModal() {
    await this.inviteModal.locator('button:has-text("Close")').click();
  }

  async closeErrorModal() {
    await this.errorModalCloseButton.click();
  }

  async getTableHeaderTexts() {
    return await this.tableHeaders.allTextContents();
  }

  async getUserDataFromRow(index = 0) {
    const row = this.tableRows.nth(index);
    const cells = row.locator('td');
    return {
      firstName: await cells.nth(0).textContent(),
      lastName: await cells.nth(1).textContent(),
      primaryEmail: await cells.nth(2).textContent(),
      secondaryEmail: await cells.nth(3).textContent(),
      phone: await cells.nth(4).textContent(),
      role: await cells.nth(5).textContent(),
    };
  }

  async copyInviteLink() {
    await this.copyButton.click();
  }
}
