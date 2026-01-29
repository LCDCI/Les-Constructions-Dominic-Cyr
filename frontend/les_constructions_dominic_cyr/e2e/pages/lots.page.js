export class LotsPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.lots-header h1');
    this.searchInput = page.locator('.search-input');
    this.lotCards = page.locator('.lot-card');
    this.lotsGrid = page.locator('.lots-grid');
    this.loadingIndicator = page.locator('text=Loading lots...');
    this.noResultsMessage = page.locator('.no-results');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.lotTitles = page.locator('.lot-title');
    this.lotImages = page.locator('.lot-image');

    // Lot creation form selectors
    this.createLotButton = page.locator('button:has-text("Create New Lot"), button:has-text("Créer un nouveau lot")');
    this.lotNumberInput = page.locator('#newLotNumber, input[placeholder*="LOT"]');
    this.lotLocationInput = page.locator('#newLotLocation, input[placeholder*="location"], input[placeholder*="emplacement"]');
    this.lotDimensionsInput = page.locator('#newLotDimensions, input[placeholder*="Dimensions"], input[placeholder*="dimensions"]');
    this.lotPriceInput = page.locator('#newLotPrice, input[type="number"]');
    this.lotStatusSelect = page.locator('#newLotStatus, select');
    this.lotPhotoInput = page.locator('#newLotPhoto, input[type="file"]');
    this.submitLotButton = page.locator('button:has-text("Create Lot"), button:has-text("Créer le lot")');
    this.cancelLotButton = page.locator('button:has-text("Cancel"), button:has-text("Annuler")');
    this.createLotForm = page.locator('.create-lot-form');
    this.createError = page.locator('.create-error, .error-message');

    // Customer assignment
    this.assignedCustomerSelect = page.locator('#newLotCustomer, select[name*="customer"]');
  }

  async goto() {
    await this.page.goto('/lots');
  }

  async gotoProjectLots(projectIdentifier) {
    await this.page.goto(`/projects/${projectIdentifier}/lots`);
  }

  async waitForLotsToLoad() {
    await this.page.waitForLoadState('networkidle');
    try {
      await this.loadingIndicator.waitFor({ state: 'hidden', timeout: 15000 });
    } catch (err) {
      // Log so CI doesn't silently swallow load problems
      // eslint-disable-next-line no-console
      console.error('Waiting for lots loading indicator failed', err);
    }
  }

  async searchLots(searchTerm) {
    await this.searchInput.fill(searchTerm);
    // Wait for either lot cards or no-results message to be visible after search
    await Promise.race([
      this.lotCards
        .first()
        .waitFor({ state: 'visible', timeout: 5000 })
        .catch(() => {}),
      this.noResultsMessage
        .waitFor({ state: 'visible', timeout: 5000 })
        .catch(() => {}),
    ]);
  }

  async clearSearch() {
    await this.searchInput.clear();
  }

  async getLotCount() {
    return await this.lotCards.count();
  }

  async getLotLocations() {
    return await this.lotTitles.allTextContents();
  }

  async isLoaded() {
    await this.waitForLotsToLoad();
    return await this.pageTitle.isVisible();
  }

  async clickCreateLot() {
    await this.createLotButton.click();
    await this.createLotForm.waitFor({ state: 'visible', timeout: 5000 });
  }

  async fillLotForm({ lotNumber, location, dimensions, price, status = 'AVAILABLE', customerIndex }) {
    if (lotNumber) await this.lotNumberInput.fill(lotNumber);
    if (location) await this.lotLocationInput.fill(location);
    if (dimensions) await this.lotDimensionsInput.fill(dimensions);
    if (price) await this.lotPriceInput.fill(price.toString());
    if (status) await this.lotStatusSelect.selectOption(status);
    if (customerIndex !== undefined) {
      await this.assignedCustomerSelect.selectOption({ index: customerIndex });
    }
  }

  async submitLot() {
    await this.submitLotButton.click();
  }

  async cancelLotCreation() {
    await this.cancelLotButton.click();
  }

  async createLot(lotData) {
    await this.clickCreateLot();
    await this.fillLotForm(lotData);
    await this.submitLot();
    // Wait for form to close or error to appear
    await Promise.race([
      this.createLotForm.waitFor({ state: 'hidden', timeout: 5000 }).catch(() => {}),
      this.createError.waitFor({ state: 'visible', timeout: 5000 }).catch(() => {}),
    ]);
  }

  async isCreateErrorVisible() {
    return await this.createError.isVisible();
  }

  async getCreateErrorText() {
    return await this.createError.textContent();
  }
}
