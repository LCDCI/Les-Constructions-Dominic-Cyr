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
}
