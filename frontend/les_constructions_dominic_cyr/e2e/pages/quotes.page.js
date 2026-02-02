export class QuotesPage {
  constructor(page) {
    this.page = page;
  }

  // Quote List Page elements
  async goto() {
    await this.page.goto('/quotes');
  }

  async getPageTitle() {
    return await this.page.locator('h1').first().textContent();
  }

  async getQuotesByStatus(status) {
    const statusElements = await this.page.locator(`[class*="status-pill"]`).filter({ hasText: status }).all();
    return statusElements.length;
  }

  async clickQuote(quoteNumber) {
    await this.page.click(`text=${quoteNumber}`);
  }

  async getQuoteCount() {
    const quotes = await this.page.locator('[role="row"]').all();
    return quotes.length;
  }

  // Quote Create Page elements
  async gotoCreateQuote(projectId, lotId) {
    await this.page.goto(`/quotes/create?projectId=${projectId}&lotId=${lotId}`);
  }

  async fillQuoteForm(formData) {
    // Category
    if (formData.category) {
      await this.page.selectOption('select[name="category"]', formData.category);
    }

    // Description
    if (formData.description) {
      await this.page.fill('textarea[name="description"]', formData.description);
    }

    // Payment Terms
    if (formData.paymentTerms) {
      await this.page.selectOption('select[name="paymentTerms"]', formData.paymentTerms);
    }

    // Delivery Date
    if (formData.deliveryDate) {
      await this.page.fill('input[name="deliveryDate"]', formData.deliveryDate);
    }

    // Notes
    if (formData.notes) {
      await this.page.fill('textarea[name="notes"]', formData.notes);
    }
  }

  async addLineItem(itemData) {
    // Click "Add Item" button
    await this.page.click('button:has-text("Add Item")');

    // Wait for new row to appear
    const lastRow = this.page.locator('tbody tr').last();
    await lastRow.waitFor();

    // Fill in the item details
    const inputs = await lastRow.locator('input').all();
    if (inputs.length >= 3) {
      await inputs[0].fill(itemData.description);
      await inputs[1].fill(itemData.hours.toString());
      await inputs[2].fill(itemData.rate.toString());
    }
  }

  async getLineItemCount() {
    const items = await this.page.locator('tbody tr').all();
    return items.length;
  }

  async getSubtotal() {
    const subtotalText = await this.page.locator('text=Subtotal').locator('..').locator('strong').textContent();
    return parseFloat(subtotalText.replace(/[^0-9.]/g, ''));
  }

  async getTotal() {
    const totalText = await this.page.locator('.totals-total .totals-value').textContent();
    return parseFloat(totalText.replace(/[^0-9.]/g, ''));
  }

  async submitQuote() {
    await this.page.click('button[type="submit"]:has-text("Create Quote")');
  }

  async getErrorMessage() {
    return await this.page.locator('[role="dialog"] p').textContent();
  }

  async closeErrorModal() {
    await this.page.click('[role="dialog"] button');
  }

  // Quote Detail Page elements
  async gotoQuoteDetail(quoteNumber) {
    await this.page.goto(`/quotes/${quoteNumber}`);
  }

  async getQuoteNumber() {
    return await this.page.locator('.quote-number').textContent();
  }

  async getContractorName() {
    const fromCard = this.page.locator('.quote-party-section .party-card').first();
    return await fromCard.locator('h3').textContent();
  }

  async getCustomerName() {
    const toCard = this.page.locator('.quote-party-section .party-card').nth(1);
    return await toCard.locator('h3').textContent();
  }

  async getProjectName() {
    const projectCard = this.page.locator('.quote-party-section .party-card').nth(2);
    return await projectCard.locator('h3').textContent();
  }

  async getQuoteStatus() {
    return await this.page.locator('.status-pill').textContent();
  }

  async getLineItems() {
    const rows = await this.page.locator('table tbody tr').all();
    return rows.length;
  }

  async clickPrintButton() {
    await this.page.click('button:has-text("Print")');
  }

  async clickBackButton() {
    await this.page.click('button:has-text("Back")');
  }

  async isLoading() {
    const spinner = this.page.locator('.spinner');
    return await spinner.isVisible().catch(() => false);
  }

  async waitForQuoteLoaded() {
    await this.page.waitForLoadState('networkidle');
    await this.page.waitForSelector('.quote-detail-page', { timeout: 5000 });
  }
}
