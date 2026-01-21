export class SalespersonDashboardPage {
  constructor(page) {
    this.page = page;
    this.dashboardTitle = page.locator('h1.dashboard-title');
    this.dashboardCards = page.locator('.dashboard-card');
    this.scheduleSection = page.locator('.schedule-section');
    this.scheduleItems = page.locator('.schedule-item');
    this.seeMoreButton = page.locator('.see-more-button');
    this.errorMessage = page.locator('.error');
    this.loadingMessage = page.locator('text=Loading schedules...');
  }

  async goto() {
    await this.page.goto('/salesperson/dashboard');
  }

  async getCardByTitle(title) {
    return this.page.locator('.dashboard-card', { hasText: title });
  }

  async clickCardButton(title) {
    const card = await this.getCardByTitle(title);
    await card.locator('.card-button').click();
  }

  async getScheduleCount() {
    return await this.scheduleItems.count();
  }

  async waitForSchedulesToLoad() {
    await this.page.waitForSelector(
      '.schedule-item, .error, text=No schedules for this week'
    );
  }

  async mockScheduleAPI(schedules) {
    await this.page.route('**/api/v1/salesperson/schedules', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(schedules),
      });
    });
  }

  async mockScheduleAPIError() {
    await this.page.route('**/api/v1/salesperson/schedules', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Internal Server Error' }),
      });
    });
  }
}
