import { test, expect } from '@playwright/test';

// Living Environment Page Object
class LivingEnvironmentPage {
  constructor(page) {
    this.page = page;
    this.header = page.locator('.le-header-section');
    this.mainTitle = page.locator('.le-main-title');
    this.subtitle = page.locator('.le-subtitle');
    this.amenitiesGrid = page.locator('.le-amenities-grid');
    this.amenityBoxes = page.locator('.le-amenity-box');
    this.backButton = page.locator(
      'button:has-text("Back to Residential Project")'
    );
    this.footer = page.locator('.le-footer-section');
  }

  async goto(projectId) {
    await this.page.goto(`/projects/${projectId}/living-environment`);
    await this.page.waitForLoadState('networkidle');
  }
}

export { LivingEnvironmentPage };
