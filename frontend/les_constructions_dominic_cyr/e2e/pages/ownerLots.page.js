export class OwnerLotsPage {
  constructor(page) {
    this.page = page;
    
    // Dashboard elements
    this.dashboardLotsCard = page.locator('.dashboard-card', { hasText: /Lots|lots/i });
    this.dashboardLotsButton = this.dashboardLotsCard.locator('.card-button');
    
    // Navbar elements
    this.hamburgerToggle = page.locator('.navbar-toggle');
    this.navbarLotsButton = page.locator('.navbar-link', { hasText: /Lots|lots/i });
    
    // Project Selection Modal
    this.projectModal = page.locator('.project-selection-modal');
    this.modalTitle = this.projectModal.locator('h2');
    this.modalCloseButton = this.projectModal.locator('.modal-close-button');
    this.projectCards = this.projectModal.locator('.project-card');
    this.loadingState = this.projectModal.locator('.loading-state');
    this.errorState = this.projectModal.locator('.error-state');
    this.emptyState = this.projectModal.locator('.empty-state');
    this.retryButton = this.errorState.locator('.retry-button');
    
    // Owner Lots Management Page
    this.pageTitle = page.locator('.lots-content h1, .lots-header h1');
    this.addLotButton = page.locator('button:has-text("Add Lot"), button:has-text("Ajouter un lot")');
    this.backButton = page.locator('button:has-text("Back"), a:has-text("Retour")');
    this.lotsTable = page.locator('table.fixed-table, .table-container table');
    this.lotRows = this.lotsTable.locator('tbody tr');
    this.searchInput = page.locator('input[placeholder*="Search"], input[placeholder*="Rechercher"]');
    
    // Lot actions
    this.editButtons = page.locator('button[aria-label*="Edit"], button[title*="Edit"]');
    this.deleteButtons = page.locator('button[aria-label*="Delete"], button[title*="Delete"]');
    
    // Lot Form Modal
    this.lotFormModal = page.locator('.modal, .lot-form-modal');
    this.lotNumberInput = this.lotFormModal.locator('input[placeholder*="Lot"], input[name*="lotNumber"]');
    this.civicAddressInput = this.lotFormModal.locator('input[placeholder*="address"], input[placeholder*="adresse"]');
    this.priceInput = this.lotFormModal.locator('input[placeholder*="price"], input[placeholder*="prix"]');
    this.statusSelect = this.lotFormModal.locator('select');
    this.submitButton = this.lotFormModal.locator('button[type="submit"]');
    this.cancelButton = this.lotFormModal.locator('button:has-text("Cancel"), button:has-text("Annuler")');
    
    // Delete Confirmation Modal
    this.confirmDeleteModal = page.locator('.confirmation-modal, .modal');
    this.confirmDeleteButton = this.confirmDeleteModal.locator('button:has-text("Delete"), button:has-text("Supprimer")');
    this.cancelDeleteButton = this.confirmDeleteModal.locator('button:has-text("Cancel"), button:has-text("Annuler")');
  }

  async gotoOwnerDashboard() {
    await this.page.goto('/owner/dashboard');
    await this.page.waitForLoadState('networkidle');
  }

  async openNavbar() {
    // Only click if navbar is not already open
    const isOpen = await this.navbarLotsButton.isVisible();
    if (!isOpen) {
      await this.hamburgerToggle.click();
      await this.navbarLotsButton.waitFor({ state: 'visible', timeout: 3000 });
    }
  }

  async clickLotsFromDashboard() {
    await this.dashboardLotsButton.click();
    await this.waitForProjectModal();
  }

  async clickLotsFromNavbar() {
    await this.openNavbar();
    await this.navbarLotsButton.click();
    await this.waitForProjectModal();
  }

  async waitForProjectModal() {
    await this.projectModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async getProjectModalTitle() {
    return await this.modalTitle.textContent();
  }

  async getProjectCardsCount() {
    // Wait for either loading to finish or cards to appear
    try {
      await this.loadingState.waitFor({ state: 'hidden', timeout: 5000 });
    } catch (e) {
      // Loading might not appear for fast loads
    }
    return await this.projectCards.count();
  }

  async selectProjectByIndex(index) {
    const card = this.projectCards.nth(index);
    await card.waitFor({ state: 'visible' });
    await card.click();
    // Wait for navigation to lots page
    await this.page.waitForURL(/.*\/projects\/.*\/manage-lots/);
  }

  async selectProjectByName(projectName) {
    const card = this.projectCards.filter({ hasText: projectName });
    await card.click();
    await this.page.waitForURL(/.*\/projects\/.*\/manage-lots/);
  }

  async closeProjectModal() {
    await this.modalCloseButton.click();
    await this.projectModal.waitFor({ state: 'hidden', timeout: 3000 });
  }

  async closeProjectModalByBackdrop() {
    // Click outside the modal
    await this.page.locator('.modal-backdrop').click({ position: { x: 10, y: 10 } });
    await this.projectModal.waitFor({ state: 'hidden', timeout: 3000 });
  }

  async waitForLotsPage() {
    await this.pageTitle.waitFor({ state: 'visible', timeout: 5000 });
  }

  async getLotsCount() {
    return await this.lotRows.count();
  }

  async clickAddLot() {
    await this.addLotButton.click();
    await this.lotFormModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async fillLotForm(lotData) {
    if (lotData.lotNumber) {
      await this.lotNumberInput.fill(lotData.lotNumber);
    }
    if (lotData.civicAddress) {
      await this.civicAddressInput.fill(lotData.civicAddress);
    }
    if (lotData.price) {
      await this.priceInput.fill(lotData.price.toString());
    }
    if (lotData.status) {
      await this.statusSelect.selectOption(lotData.status);
    }
  }

  async submitLotForm() {
    await this.submitButton.click();
    await this.lotFormModal.waitFor({ state: 'hidden', timeout: 5000 });
  }

  async cancelLotForm() {
    await this.cancelButton.click();
    await this.lotFormModal.waitFor({ state: 'hidden', timeout: 5000 });
  }

  async clickEditLot(index = 0) {
    const editButton = this.editButtons.nth(index);
    await editButton.click();
    await this.lotFormModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async clickDeleteLot(index = 0) {
    const deleteButton = this.deleteButtons.nth(index);
    await deleteButton.click();
    await this.confirmDeleteModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async confirmDelete() {
    await this.confirmDeleteButton.click();
    await this.confirmDeleteModal.waitFor({ state: 'hidden', timeout: 5000 });
  }

  async cancelDelete() {
    await this.cancelDeleteButton.click();
    await this.confirmDeleteModal.waitFor({ state: 'hidden', timeout: 5000 });
  }

  async searchLots(searchTerm) {
    await this.searchInput.fill(searchTerm);
    await this.page.waitForTimeout(500); // Wait for debounce
  }

  async isProjectModalVisible() {
    return await this.projectModal.isVisible();
  }

  async hasLoadingState() {
    return await this.loadingState.isVisible();
  }

  async hasErrorState() {
    return await this.errorState.isVisible();
  }

  async hasEmptyState() {
    return await this.emptyState.isVisible();
  }

  async clickRetryButton() {
    await this.retryButton.click();
  }
}
