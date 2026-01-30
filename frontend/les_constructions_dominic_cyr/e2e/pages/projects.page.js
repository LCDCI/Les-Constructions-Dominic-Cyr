export class ProjectsPage {
  constructor(page) {
    this.page = page;
    // Use the hero h1 as the page title for e2e tests (current design)
    this.pageTitle = page.locator('.projects-hero h1.projects-title');
    this.searchInput = page.locator(
      'input.search-input, input[placeholder*="Search"]'
    );
    this.projectCards = page.locator('.portfolio-card, .project-hover-card');
    this.projectGrid = page.locator('.portfolio-grid, .projects-grid');
    this.loadingIndicator = page.locator('text=Loading projects...');
    this.noResultsMessage = page.locator('.no-results');
    this.viewProjectButtons = page.locator('a:has-text("View"), button:has-text("View")');
    this.editProjectButtons = page.locator('button:has-text("Edit")');
    this.navbar = page.locator('.navbar');
    this.footer = page.locator('.footer');
    this.projectTitles = page.locator('.card-title, .project-title');
    this.projectDescriptions = page.locator('.admin-project-description, .project-description');
    this.projectImages = page.locator('.card-image-bg, .project-image');
  }

  async goto() {
    await this.page.goto('/projects');
    // Wait for projects to load
    await this.page.waitForLoadState('networkidle');

    // Be tolerant: wait for either the hero title, a project card, or the no-results message
    try {
      await this.page.locator('.projects-hero h1.projects-title').waitFor({ state: 'visible', timeout: 5000 });
    } catch (e) {
      await Promise.allSettled([
        this.projectCards.first().waitFor({ state: 'visible', timeout: 10000 }).catch(() => null),
        this.noResultsMessage.waitFor({ state: 'visible', timeout: 10000 }).catch(() => null),
      ]);
    }
  }

  async clearSearch() {
    const searchInput = this.page.locator(
      'input.search-input, input[placeholder*="Search"]'
    );
    await searchInput.clear();
  }

  async getProjectCount() {
    return await this.projectCards.count();
  }

  async clickViewProject(index = 0) {
    const viewButtons = this.page.locator('button:has-text("View")');
    await viewButtons.nth(index).click();
  }

  async clickEditProject(index = 0) {
    const editButtons = this.page.locator('button:has-text("Edit")');
    await editButtons.nth(index).click();
  }

  async getProjectNames() {
    return await this.projectTitles.allTextContents();
  }

  async isLoaded() {
    return await this.pageTitle.isVisible();
  }

  // Edit Form Methods
  async getEditFormTitle() {
    return await this.page.locator('.edit-project-form h2').textContent();
  }

  async fillProjectName(name) {
    const input = this.page.locator('input[id="projectName"]').first();
    await input.fill(name);
  }

  async fillProjectDescription(description) {
    const input = this.page
      .locator('textarea[id="projectDescription"]')
      .first();
    await input.fill(description);
  }

  async fillLocation(location) {
    const locationInput = this.page.locator('input[id="location"]').first();
    await locationInput.fill(location);
  }

  async selectStatus(status) {
    const select = this.page.locator('select[id="status"]').first();
    await select.selectOption(status);
  }

  async fillStartDate(date) {
    const input = this.page.locator('input[id="startDate"]').first();
    await input.fill(date);
  }

  async fillEndDate(date) {
    const input = this.page.locator('input[id="endDate"]').first();
    await input.fill(date);
  }

  async fillProgressPercentage(percentage) {
    const input = this.page.locator('input[id="progressPercentage"]').first();
    await input.fill(percentage.toString());
  }

  async submitEditForm() {
    await this.page.locator('button:has-text("Save Changes")').click();
  }

  async cancelEditForm() {
    await this.page.locator('button:has-text("Cancel")').first().click();
  }

  async getEditFormError() {
    const errorElement = this.page.locator('.error-message').first();
    return await errorElement.textContent();
  }

  async isEditFormVisible() {
    return await this.page.locator('.edit-project-form').isVisible();
  }

  async waitForEditFormToClose() {
    await this.page.locator('.edit-project-form').waitFor({ state: 'hidden' });
  }
}
