export class RenovationsPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.renovations-page__title');
    this.renovationCards = page.locator('.renovation-card');
    this.renovationsGrid = page.locator('.renovations-page__grid');
    this.loadingSkeletons = page.locator('.renovations-page__skeleton');
    this.emptyMessage = page.locator('.renovations-page__status--empty');
    this.errorMessage = page.locator('.renovations-page__status--error');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.images = page.locator('.renovation-card__image');
    this.captions = page.locator('.renovation-card__caption');
    this.descriptions = page.locator('.renovation-card__description');
  }

  async goto() {
    await this.page.goto('/renovations');
  }

  async waitForRenovationsToLoad() {
    await this.page.waitForLoadState('networkidle');
    try {
      await this.loadingSkeletons
        .first()
        .waitFor({ state: 'hidden', timeout: 15000 });
    } catch (err) {
      // Log so CI doesn't silently swallow load problems
      // eslint-disable-next-line no-console
      console.error('Waiting for renovations loading indicator failed', err);
    }
  }

  async getRenovationCount() {
    return await this.renovationCards.count();
  }

  async getDescriptions() {
    return await this.descriptions.allTextContents();
  }

  async isLoaded() {
    await this.waitForRenovationsToLoad();
    return await this.pageTitle.isVisible();
  }
}
