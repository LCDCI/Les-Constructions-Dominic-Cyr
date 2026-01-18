export class ReportsPage {
  /**
   * @param {import('@playwright/test').Page} page
   */
  constructor(page) {
    this.page = page;
    this.generateTab = page.locator('button.tab', { hasText: 'Generate New' });
    this.myReportsTab = page.locator('button.tab', { hasText: 'My Reports' });
    this.header = page.getByRole('heading', { name: 'Analytics Reports' });
    this.tabContent = page.locator('.tab-content');
    this.reportGeneratorForm = page.locator('.report-generator');
    this.reportListTable = page.locator('.report-list');
  }

  async goTo() {
    await this.page.goto('/reports');
    await this.header.waitFor({ state: 'visible' });
  }

  async switchToMyReports() {
    await this.myReportsTab.click();
  }

  async switchToGenerateNew() {
    await this.generateTab.click();
  }
}
