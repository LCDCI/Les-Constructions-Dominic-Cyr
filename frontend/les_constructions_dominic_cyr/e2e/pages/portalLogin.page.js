export class PortalLoginPage {
  constructor(page) {
    this.page = page;
    this.portalCard = page.locator('.portal-card');
    this.pageTitle = page.locator('.portal-card h1');
    this.description = page.locator('.portal-card p');
    this.loginButton = page.locator('.portal-card button');
    this.loadingMessage = page.locator('text=Loading...');
    this.alreadyLoggedInMessage = page.locator('text=You are already logged in');
  }

  async goto() {
    await this.page.goto('/portal/login');
  }

  async clickLogin() {
    await this.loginButton.click();
  }

  async isLoaded() {
    return await this.portalCard.isVisible();
  }

  async isLoading() {
    return await this.loadingMessage.isVisible();
  }

  async isAlreadyLoggedIn() {
    return await this.alreadyLoggedInMessage.isVisible();
  }

  async getTitle() {
    return await this.pageTitle.textContent();
  }

  async getDescription() {
    return await this.description.textContent();
  }

  async getLoginButtonText() {
    return await this.loginButton.textContent();
  }
}
