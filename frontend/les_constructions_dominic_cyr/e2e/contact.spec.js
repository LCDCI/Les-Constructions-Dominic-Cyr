import { test, expect } from '@playwright/test';
import { ContactPage } from './pages/contact.page.js';

test.describe('Contact Page - Inquiry Form', () => {
  let contactPage;

  test.beforeEach(async ({ page }) => {
    contactPage = new ContactPage(page);
    await contactPage.goto();
    await contactPage.waitForReady();
  });

  test('submits inquiry with all fields', async ({ page }) => {
    const requests = [];

    await page.route('**/api/inquiries', async (route) => {
      requests.push(route.request().postDataJSON());
      await route.fulfill({ status: 200, body: 'Thank you! Your inquiry has been received.' });
    });

    await contactPage.fillForm({
      name: 'Jane Doe',
      email: 'jane@example.com',
      phone: '555-1234',
      message: 'I am interested in building a custom home.',
    });

    await contactPage.submit();

    await expect(contactPage.statusMessage).toHaveText(/thank you! your inquiry has been received/i);
    await expect(contactPage.nameInput).toHaveValue('');
    await expect(contactPage.emailInput).toHaveValue('');
    await expect(contactPage.phoneInput).toHaveValue('');
    await expect(contactPage.messageInput).toHaveValue('');

    expect(requests).toHaveLength(1);
    expect(requests[0]).toEqual({
      name: 'Jane Doe',
      email: 'jane@example.com',
      phone: '555-1234',
      message: 'I am interested in building a custom home.',
    });
  });

  test('submits inquiry without optional phone field', async ({ page }) => {
    const requests = [];

    await page.route('**/api/inquiries', async (route) => {
      requests.push(route.request().postDataJSON());
      await route.fulfill({ status: 200, body: 'Thank you! Your inquiry has been received.' });
    });

    await contactPage.fillForm({
      name: 'John Smith',
      email: 'john@test.com',
      message: 'Need a renovation quote.',
    });

    await contactPage.submit();

    await expect(contactPage.statusMessage).toHaveText(/thank you! your inquiry has been received/i);
    expect(requests).toHaveLength(1);
    expect(requests[0]).toEqual({
      name: 'John Smith',
      email: 'john@test.com',
      phone: '',
      message: 'Need a renovation quote.',
    });
  });

  test('shows validation error when required fields are missing', async ({ page }) => {
    const requests = [];

    await page.route('**/api/inquiries', async (route) => {
      requests.push(route.request().postDataJSON());
      await route.fulfill({ status: 200, body: 'noop' });
    });

    // Disable native browser validation so the component's client-side validation can run
    await page.locator('form').evaluate((form) => form.setAttribute('novalidate', ''));

    await contactPage.submit();

    await expect(contactPage.statusMessage).toHaveText(/please fill out all required fields/i);
    expect(requests).toHaveLength(0);
  });

  test('shows server error message when submission fails', async ({ page }) => {
    await page.route('**/api/inquiries', async (route) => {
      await route.fulfill({ status: 500, body: 'Server error' });
    });

    await contactPage.fillForm({
      name: 'Alice',
      email: 'alice@test.com',
      message: 'Test message',
    });

    await contactPage.submit();

    await expect(contactPage.statusMessage).toHaveText(/server error/i);
  });

  test('shows network error when request fails', async ({ page }) => {
    await page.route('**/api/inquiries', (route) => route.abort());

    await contactPage.fillForm({
      name: 'Bob',
      email: 'bob@test.com',
      message: 'Inquiry message',
    });

    await contactPage.submit();

    await expect(contactPage.statusMessage).toHaveText(/network error/i);
  });

  test('submit button shows loading state during submission', async ({ page }) => {
    let releaseResponse;

    await page.route('**/api/inquiries', async (route) => {
      await new Promise((resolve) => {
        releaseResponse = resolve;
      });
      await route.fulfill({ status: 200, body: 'Thank you! Your inquiry has been received.' });
    });

    await contactPage.fillForm({
      name: 'Charlie',
      email: 'charlie@test.com',
      message: 'Test inquiry',
    });

    await contactPage.submit();

    await expect(contactPage.sendingButton).toBeDisabled();

    releaseResponse();

    await expect(contactPage.submitButton).toBeEnabled();
  });
});
