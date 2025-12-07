export class ContactPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.contact-hero h1');
    this.nameInput = page.getByPlaceholder('Your name');
    this.emailInput = page.getByPlaceholder('your.email@example.com');
    this.phoneInput = page.getByPlaceholder('(555) 123-4567');
    this.messageInput = page.getByPlaceholder('Tell us about your project...');
    this.submitButton = page.getByRole('button', { name: /submit inquiry/i });
    this.sendingButton = page.getByRole('button', { name: /sending/i });
    this.statusMessage = page.locator('.status-message');
    this.contactWrapper = page.locator('.contact-form');
  }

  async goto() {
    await this.page.goto('/contact');
  }

  async waitForReady() {
    await this.page.waitForLoadState('networkidle');
    await this.contactWrapper.waitFor({ state: 'visible' });
  }

  async fillForm({ name, email, phone = '', message }) {
    if (name !== undefined) await this.nameInput.fill(name);
    if (email !== undefined) await this.emailInput.fill(email);
    if (phone !== undefined) await this.phoneInput.fill(phone);
    if (message !== undefined) await this.messageInput.fill(message);
  }

  async submit() {
    await this.submitButton.click();
  }

  async getStatusText() {
    if (this.statusMessage.count()) {
      return (await this.statusMessage.innerText()).trim();
    }
    return '';
  }
}
