import { expect } from '@playwright/test';

export class LotMetadataPage {
  constructor(page) {
    this.page = page;
    this.main = page.locator('main');
    this.headerTitle = page.locator('.metadata-hero .hero-content .project-title');
    this.statusBadge = page.locator('.metadata-hero .status-badge');
    this.civicAddress = page.locator('.metadata-grid .metadata-item .metadata-value').first();
  }

  async goto(projectId, lotId) {
    await this.page.goto(`/projects/${projectId}/lots/${lotId}/metadata`);
    await this.page.waitForLoadState('networkidle');
    // wait for main content to be visible
    await Promise.race([
      this.headerTitle.waitFor({ state: 'visible', timeout: 5000 }).catch(() => null),
      this.main.waitFor({ state: 'visible', timeout: 5000 }).catch(() => null),
    ]);
  }

  async expectLoaded() {
    await expect(this.headerTitle).toBeVisible();
  }

  async getHeaderText() {
    return await this.headerTitle.textContent();
  }

  async getStatusText() {
    return await this.statusBadge.textContent();
  }

  async expectError() {
    await expect(this.main).toBeVisible();
  }
}

export default LotMetadataPage;
