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
  }

  async goto() {
    await this.page.goto('/lots');
  }

  async waitForLotsToLoad() {
    await this.page.waitForLoadState('networkidle');
    await this.loadingIndicator
      .waitFor({ state: 'hidden', timeout: 15000 })
      .catch(() => {});
  }

  async searchLots(searchTerm) {
    await this.searchInput.fill(searchTerm);
    await this.page.waitForTimeout(500);
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
}
