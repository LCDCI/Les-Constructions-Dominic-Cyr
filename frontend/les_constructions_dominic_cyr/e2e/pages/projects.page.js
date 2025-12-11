export class ProjectsPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.projects-header h1');
    this.searchInput = page.locator('.search-input');
    this.projectCards = page.locator('.project-card');
    this.projectGrid = page.locator('.projects-grid');
    this.loadingIndicator = page.locator('text=Loading projects...');
    this.noResultsMessage = page.locator('.no-results');
    this.viewProjectButtons = page.locator('.project-button');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.projectTitles = page.locator('.project-title');
    this.projectDescriptions = page.locator('.project-description');
    this.projectImages = page.locator('.project-image');
  }

  async goto() {
    await this.page.goto('/projects');
  }

  async clearSearch() {
    await this.searchInput.clear();
  }

  async getProjectCount() {
    return await this.projectCards.count();
  }

  async clickViewProject(index = 0) {
    await this.viewProjectButtons.nth(index).click();
  }

  async getProjectNames() {
    return await this.projectTitles.allTextContents();
  }

  async isLoaded() {
    return await this.pageTitle.isVisible();
  }
}
