export class HousesPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.houses-header h1');
    this.searchInput = page.locator('.search-input');
    this.houseCards = page.locator('.house-card');
    this.housesGrid = page.locator('.houses-grid');
    this.loadingIndicator = page.locator('text=Loading houses...');
    this.noResultsMessage = page.locator('.no-results');
    this.viewHouseButtons = page.locator('.house-button');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.houseTitles = page.locator('.house-title');
    this.houseLocations = page.locator('.house-location');
    this.houseDescriptions = page.locator('.house-description');
    this.houseImages = page.locator('.house-image');
  }

  async goto() {
    await this.page.goto('/houses');
  }

  async waitForHousesToLoad() {
    await this.page.waitForLoadState('networkidle');
    await this.loadingIndicator
      .waitFor({ state: 'hidden', timeout: 15000 })
      .catch(() => {});
  }

  async searchHouses(searchTerm) {
    await this.searchInput.fill(searchTerm);
    await this.page.waitForTimeout(500);
  }

  async clearSearch() {
    await this.searchInput.clear();
  }

  async getHouseCount() {
    return await this.houseCards.count();
  }

  async clickViewHouse(index = 0) {
    await this.viewHouseButtons.nth(index).click();
  }

  async getHouseNames() {
    return await this.houseTitles.allTextContents();
  }

  async isLoaded() {
    await this.waitForHousesToLoad();
    return await this.pageTitle.isVisible();
  }
}
