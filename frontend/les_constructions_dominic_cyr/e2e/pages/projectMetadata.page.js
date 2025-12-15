import { expect } from '@playwright/test';

export class ProjectMetadataPage {
  constructor(page) {
    this.page = page;
    this.main = page.locator('main');
  }

  async goto(projectId) {
    await this.page.goto(`/projects/${projectId}`);
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
