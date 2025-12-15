import { expect } from '@playwright/test';

export class ProjectOverviewPage {
  constructor(page) {
    this.page = page;
    this.main = page.locator('main');
  }

  async goto(identifier) {
    await this.page.goto(`/residential-projects/${identifier}`);
  }

  async expectLoaded() {
    await expect(this.main).toBeVisible();
  }

  async expectError() {
    await expect(this.main).toBeVisible();
  }

  async goBack() {
    await this.page.goBack();
  }
}
