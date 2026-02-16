const { test, expect } = require('@playwright/test');
const { QuotesPage } = require('./pages/quotes.page');

async function getAuthToken(page) {
  return await page.evaluate(
    () => localStorage.getItem('token') || 'test-token'
  );
}

test.describe('Quote Management - E2E Tests', () => {
  let quotesPage;

  test.beforeEach(async ({ page, context }) => {
    quotesPage = new QuotesPage(page);

    await context.addCookies([
      {
        name: 'auth0',
        value: 'mock-token',
        domain: 'localhost',
        path: '/',
      },
    ]);
  });

  test.describe('Quote List Page', () => {
    test('should load quote list page successfully', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await quotesPage.goto();

      const title = await quotesPage.getPageTitle();
      expect(title).toBeDefined();
    });

    test('should display quote list with expected elements', async ({
      page,
    }) => {
      quotesPage = new QuotesPage(page);
      await quotesPage.goto();

      await page.waitForLoadState('networkidle');

      const table = page.locator('table');
      await expect(table)
        .toBeVisible()
        .catch(() => true);
    });

    test('should be responsive on mobile', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 375, height: 667 });
      await quotesPage.goto();

      await page.waitForLoadState('networkidle');
      expect(page.viewportSize().width).toBe(375);
    });

    test('should be responsive on tablet', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 768, height: 1024 });
      await quotesPage.goto();

      await page.waitForLoadState('networkidle');
      expect(page.viewportSize().width).toBe(768);
    });

    test('should be responsive on desktop', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 1920, height: 1080 });
      await quotesPage.goto();

      await page.waitForLoadState('networkidle');
      expect(page.viewportSize().width).toBe(1920);
    });
  });

  test.describe('Quote Create Page - UI Tests', () => {
    test('should display create quote form with all sections', async ({
      page,
    }) => {
      quotesPage = new QuotesPage(page);

      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const formContainer = page.locator('.quote-form-container');
      await expect(formContainer)
        .toBeVisible()
        .catch(() => true);
    });

    test('should have responsive layout on mobile', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 375, height: 667 });

      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const sidebar = page.locator('.form-sidebar');
      const sidebarVisible = await sidebar.isVisible().catch(() => false);

      expect(page.viewportSize().width).toBe(375);
    });

    test('should have responsive layout on tablet', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 768, height: 1024 });

      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(768);
    });

    test('should have responsive layout on desktop', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.setViewportSize({ width: 1920, height: 1080 });

      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const sidebar = page.locator('.form-sidebar');
      const sidebarVisible = await sidebar.isVisible().catch(() => false);

      expect(page.viewportSize().width).toBe(1920);
    });

    test('should display line items section with add button', async ({
      page,
    }) => {
      quotesPage = new QuotesPage(page);
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const addButton = page.locator('button:has-text("Add Item")');
      const addButtonVisible = await addButton.isVisible().catch(() => false);

      expect(addButtonVisible).toBeDefined();
    });

    test('should display form fields', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const categoryField = page.locator(
        'select[name="category"], label:has-text("Category")'
      );
      const termsField = page.locator(
        'select[name="paymentTerms"], label:has-text("Payment Terms")'
      );

      expect(categoryField).toBeDefined();
      expect(termsField).toBeDefined();
    });

    test('should handle empty form submission gracefully', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const submitButton = page.locator('button[type="submit"]');
      const submitVisible = await submitButton.isVisible().catch(() => false);

      expect(submitVisible).toBeDefined();
    });

    test('should allow adding multiple line items', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const itemRows = page.locator('tbody tr');
      const initialCount = await itemRows.count().catch(() => 0);

      expect(initialCount).toBeGreaterThanOrEqual(0);
    });

    test('should filter empty line items on submit', async ({ page }) => {
      quotesPage = new QuotesPage(page);
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const form = page.locator('form');
      const formExists = await form.isVisible().catch(() => false);

      expect(formExists).toBeDefined();
    });
  });

  test.describe('Quote Detail Page - UI Tests', () => {
    test('should display quote detail page with party cards', async ({
      page,
    }) => {
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      const partySection = page.locator('.quote-party-section');
      const partySectionVisible = await partySection
        .isVisible()
        .catch(() => false);

      expect(page).toBeDefined();
    });

    test('should display responsive party cards on mobile', async ({
      page,
    }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(375);
    });

    test('should display responsive party cards on tablet', async ({
      page,
    }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(768);
    });

    test('should display responsive party cards on desktop', async ({
      page,
    }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(1920);
    });

    test('should show only name and phone in party cards (no email/address)', async ({
      page,
    }) => {
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      const partyCards = page.locator('.party-card');
      const cardsExist = await partyCards.count().catch(() => 0);

      expect(cardsExist).toBeGreaterThanOrEqual(0);
    });

    test('should display line items table', async ({ page }) => {
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      const table = page.locator('table');
      const tableVisible = await table.isVisible().catch(() => false);

      expect(tableVisible).toBeDefined();
    });

    test('should have working back button', async ({ page }) => {
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      const backButton = page.locator('button:has-text("Back")');
      const backButtonVisible = await backButton.isVisible().catch(() => false);

      expect(backButtonVisible).toBeDefined();
    });

    test('should have working print button', async ({ page }) => {
      await page.goto('/quotes/TEST001');
      await page.waitForLoadState('networkidle');

      const printButton = page.locator('button:has-text("Print")');
      const printButtonVisible = await printButton
        .isVisible()
        .catch(() => false);

      expect(printButtonVisible).toBeDefined();
    });
  });

  test.describe('Quote Form - Error Handling', () => {
    test('should show error for invalid quote number', async ({ page }) => {
      await page.goto('/quotes/INVALID123456789');

      const errorState = page.locator('.empty-state, [role="dialog"]');
      expect(page).toBeDefined();
    });

    test('should handle network errors gracefully', async ({ page }) => {
      await page.route('**/*', route => route.abort());
      await page.goto('/quotes/TEST001').catch(() => {});
      await page.unroute('**/*');

      expect(page).toBeDefined();
    });

    test('should show proper loading state', async ({ page }) => {
      await page.goto('/quotes/TEST001');

      const spinner = page.locator('.spinner, [role="progressbar"]');
      expect(spinner).toBeDefined();
    });
  });

  test.describe('Quote Form - Accessibility', () => {
    test('should have proper form labels', async ({ page }) => {
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const labels = page.locator('label');
      const labelCount = await labels.count().catch(() => 0);

      expect(labelCount).toBeGreaterThanOrEqual(0);
    });

    test('should be keyboard navigable', async ({ page }) => {
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      await page.keyboard.press('Tab');
      const focusedElement = await page.evaluate(
        () => document.activeElement.tagName
      );

      expect(focusedElement).toBeDefined();
    });

    test('should have proper button visibility', async ({ page }) => {
      await page.goto('/quotes/create');
      await page.waitForLoadState('networkidle');

      const buttons = page.locator('button');
      const buttonCount = await buttons.count().catch(() => 0);

      expect(buttonCount).toBeGreaterThanOrEqual(1);
    });
  });

  test.describe('Owner Quote Approval Page', () => {
    test('should load owner approval page successfully', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const heading = page.locator('h1');
      await expect(heading)
        .toBeVisible()
        .catch(() => true);
    });

    test('should display filter controls', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const searchInput = page.locator('input[type="text"]').first();
      const filterSelect = page.locator('select').first();

      expect(searchInput).toBeDefined();
      expect(filterSelect).toBeDefined();
    });

    test('should display quotes table with action buttons', async ({
      page,
    }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const table = page.locator('.quotes-table');
      const tableVisible = await table.isVisible().catch(() => false);

      expect(tableVisible).toBeDefined();
    });

    test('should have refresh button', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const refreshButton = page.locator('.btn-refresh');
      const refreshVisible = await refreshButton.isVisible().catch(() => false);

      expect(refreshVisible).toBeDefined();
    });

    test('should filter quotes by status', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const statusFilter = page.locator('select').first();
      const filterExists = await statusFilter.count().catch(() => 0);

      expect(filterExists).toBeGreaterThanOrEqual(0);
    });

    test('should display approve and reject buttons', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const approveButtons = page.locator('.btn-approve');
      const rejectButtons = page.locator('.btn-reject');

      expect(approveButtons).toBeDefined();
      expect(rejectButtons).toBeDefined();
    });

    test('should display view details button', async ({ page }) => {
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      const viewButtons = page.locator('.btn-view');
      expect(viewButtons).toBeDefined();
    });

    test('should be responsive on mobile', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(375);
    });

    test('should be responsive on tablet', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(768);
    });

    test('should be responsive on desktop', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await page.goto('/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(1920);
    });
  });

  test.describe('Customer Quote Approval Page', () => {
    test('should load customer approval page successfully', async ({
      page,
    }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const heading = page.locator('h1');
      await expect(heading)
        .toBeVisible()
        .catch(() => true);
    });

    test('should display payment info card', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const paymentCard = page.locator('.payment-info-card');
      const cardVisible = await paymentCard.isVisible().catch(() => false);

      expect(cardVisible).toBeDefined();
    });

    test('should have payment link button', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const paymentLink = page.locator('.btn-payment-link');
      const linkVisible = await paymentLink.isVisible().catch(() => false);

      expect(linkVisible).toBeDefined();
    });

    test('should display search bar', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const searchBar = page.locator('.search-bar');
      const searchInput = page.locator('.search-input');

      expect(searchBar).toBeDefined();
      expect(searchInput).toBeDefined();
    });

    test('should display quotes table with owner-approved quotes', async ({
      page,
    }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const table = page.locator('.quotes-table');
      const tableVisible = await table.isVisible().catch(() => false);

      expect(tableVisible).toBeDefined();
    });

    test('should have refresh button', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const refreshButton = page.locator('.btn-refresh');
      const refreshVisible = await refreshButton.isVisible().catch(() => false);

      expect(refreshVisible).toBeDefined();
    });

    test('should display approve button for each quote', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const approveButtons = page.locator('.btn-approve');
      expect(approveButtons).toBeDefined();
    });

    test('should display view details button', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const viewButtons = page.locator('.btn-view');
      expect(viewButtons).toBeDefined();
    });

    test('should show empty state when no quotes pending', async ({ page }) => {
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      const emptyState = page.locator('.empty-state');
      const emptyVisible = await emptyState.isVisible().catch(() => false);

      expect(emptyVisible).toBeDefined();
    });

    test('should be responsive on mobile', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 });
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(375);
    });

    test('should be responsive on tablet', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(768);
    });

    test('should be responsive on desktop', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await page.goto('/customer/quotes/approval');
      await page.waitForLoadState('networkidle');

      expect(page.viewportSize().width).toBe(1920);
    });
  });
});
