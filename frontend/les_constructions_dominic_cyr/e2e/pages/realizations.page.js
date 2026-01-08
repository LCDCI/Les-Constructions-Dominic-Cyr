export class RealizationsPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.realizations-header h1');
    this.searchInput = page.locator('.search-input');
    this.realizationCards = page.locator('.realization-card');
    this.realizationsGrid = page.locator('.realizations-grid');
    this.loadingIndicator = page.locator('text=Loading realizations...');
    this.noResultsMessage = page.locator('.no-results');
    this.viewRealizationButtons = page.locator('.realization-button');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.realizationTitles = page.locator('.realization-title');
    this.realizationLocations = page.locator('.realization-location');
    this.realizationDescriptions = page.locator('.realization-description');
    this.realizationImages = page.locator('.realization-image');
  }

  async goto() {
    await this.page.goto('/realizations');
  }

  async waitForRealizationsToLoad() {
    await this.page.waitForLoadState('networkidle');
    await this.loadingIndicator
      .waitFor({ state: 'hidden', timeout: 15000 })
      .catch(() => {});
  }

  async searchRealizations(searchTerm) {
    await this.searchInput.fill(searchTerm);
    await this.page.waitForTimeout(500);
  }

  async clearSearch() {
    await this.searchInput.clear();
  }

  async getRealizationCount() {
    return await this.realizationCards.count();
  }

  async clickViewRealization(index = 0) {
    await this.viewRealizationButtons.nth(index).click();
  }

  async getRealizationNames() {
    return await this.realizationTitles.allTextContents();
  }

  async isLoaded() {
    await this.waitForRealizationsToLoad();
    return await this.pageTitle.isVisible();
  }
}
