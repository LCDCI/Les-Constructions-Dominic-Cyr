export class InboxPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.inbox-title');
    this.unreadBadge = page.locator('.inbox-unread-badge');
    this.markAllAsReadButton = page.locator('.btn-mark-all-read');
    this.searchInput = page.locator('.search-input, input[placeholder*="Search notifications"]');
    this.filterSelect = page.locator('.inbox-filters select, .filter-group select').first();
    this.sortSelect = page.locator('.inbox-filters select, .filter-group select').last();
    this.notificationsList = page.locator('.notifications-list');
    this.notificationItems = page.locator('.notification-item');
    this.emptyState = page.locator('.inbox-empty');
    this.emptyMessage = page.locator('.inbox-empty p');
    this.loadingMessage = page.locator('text=Loading notifications...');
    this.errorMessage = page.locator('.inbox-error');
    this.inboxContainer = page.locator('.inbox-container');
  }

  async goto() {
    await this.page.goto('/inbox');
    await this.page.waitForLoadState('networkidle');
  }

  async waitForReady() {
    // Wait for either loading to finish or content to appear
    const isLoading = await this.loadingMessage.isVisible().catch(() => false);
    if (isLoading) {
      await this.loadingMessage.waitFor({ state: 'hidden', timeout: 10000 });
    }

    // Wait for either notifications list or empty state
    await Promise.allSettled([
      this.notificationsList.waitFor({ state: 'visible', timeout: 10000 }).catch(() => null),
      this.emptyState.waitFor({ state: 'visible', timeout: 10000 }).catch(() => null),
      this.pageTitle.waitFor({ state: 'visible', timeout: 10000 }).catch(() => null),
    ]);
  }

  async getNotificationCount() {
    return await this.notificationItems.count();
  }

  async getUnreadCount() {
    const badgeVisible = await this.unreadBadge.isVisible().catch(() => false);
    if (badgeVisible) {
      const text = await this.unreadBadge.textContent();
      const match = text.match(/(\d+)\s*unread/);
      return match ? parseInt(match[1], 10) : 0;
    }
    return 0;
  }

  async searchNotifications(query) {
    if (await this.searchInput.count() > 0) {
      await this.searchInput.fill(query);
      await this.page.waitForTimeout(300); // Wait for debounce
    }
  }

  async clearSearch() {
    if (await this.searchInput.count() > 0) {
      await this.searchInput.clear();
      await this.page.waitForTimeout(300);
    }
  }

  async selectFilter(filterValue) {
    // Try to find filter select by value or label
    const filterSelect = this.page.locator('select').filter({ hasText: filterValue }).first();
    const count = await filterSelect.count();
    if (count > 0) {
      await filterSelect.selectOption({ value: filterValue });
    } else {
      // Fallback: try to click on option text
      const option = this.page.locator(`option:has-text("${filterValue}")`).first();
      if (await option.count() > 0) {
        await this.filterSelect.selectOption({ label: filterValue });
      }
    }
    await this.page.waitForTimeout(300);
  }

  async selectSort(sortValue) {
    const sortSelect = this.page.locator('select').last();
    const count = await sortSelect.count();
    if (count > 0) {
      await sortSelect.selectOption({ value: sortValue });
      await this.page.waitForTimeout(300);
    }
  }

  async clickNotification(index = 0) {
    const notifications = await this.notificationItems.all();
    if (notifications.length > index) {
      await notifications[index].click();
    }
  }

  async clickMarkAllAsRead() {
    const buttonVisible = await this.markAllAsReadButton.isVisible().catch(() => false);
    if (buttonVisible) {
      await this.markAllAsReadButton.click();
      await this.page.waitForTimeout(500);
    }
  }

  async getNotificationTitle(index = 0) {
    const notifications = await this.notificationItems.all();
    if (notifications.length > index) {
      const title = notifications[index].locator('.notification-title');
      return await title.textContent();
    }
    return null;
  }

  async getNotificationMessage(index = 0) {
    const notifications = await this.notificationItems.all();
    if (notifications.length > index) {
      const message = notifications[index].locator('.notification-message');
      return await message.textContent();
    }
    return null;
  }

  async isNotificationRead(index = 0) {
    const notifications = await this.notificationItems.all();
    if (notifications.length > index) {
      const classes = await notifications[index].getAttribute('class');
      return classes ? classes.includes('read') : false;
    }
    return false;
  }

  async getEmptyStateMessage() {
    const emptyVisible = await this.emptyState.isVisible().catch(() => false);
    if (emptyVisible) {
      return await this.emptyMessage.textContent();
    }
    return null;
  }

  async isLoaded() {
    return await this.inboxContainer.isVisible();
  }

  async hasError() {
    return await this.errorMessage.isVisible();
  }

  async getErrorText() {
    if (await this.hasError()) {
      return await this.errorMessage.textContent();
    }
    return null;
  }
}
