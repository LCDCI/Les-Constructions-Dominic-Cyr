export class ProjectFilesPage {
  constructor(page) {
    this.page = page;
    this.pageTitle = page.locator('.documents-header h1');
    this.uploadButton = page.locator('.btn-upload');
    this.documentTable = page.locator('.document-table');
    this.documentRows = page.locator('.document-table tbody tr');
    this.loadingMessage = page.locator('text=Loading documents...');
    this.errorMessage = page.locator('text=Failed to load documents');
    this.noDocumentsMessage = page.locator('text=No documents found');
    this.viewButtons = page.locator('.btn-view');
    this.deleteButtons = page.locator('.btn-delete');
  }

  async goto() {
    await this.page.goto('/project-files');
  }

  async waitForDocumentsToLoad() {
    await this.page.waitForLoadState('networkidle');
    try {
      await this.loadingMessage.waitFor({ state: 'hidden', timeout: 15000 });
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error('Waiting for documents loading indicator failed', err);
    }
  }

  async getDocumentCount() {
    return await this.documentRows.count();
  }

  async getDocumentNames() {
    const rows = await this.documentRows.all();
    const names = [];
    for (const row of rows) {
      const nameCell = row.locator('.file-name-cell');
      const text = await nameCell.textContent();
      names.push(text.trim());
    }
    return names;
  }

  async clickViewDocument(index = 0) {
    await this.viewButtons.nth(index).click();
  }

  async clickDeleteDocument(index = 0) {
    await this.deleteButtons.nth(index).click();
  }

  async clickUploadButton() {
    await this.uploadButton.click();
  }

  async isLoaded() {
    await this.waitForDocumentsToLoad();
    return await this.pageTitle.isVisible();
  }

  async isTableVisible() {
    return await this.documentTable.isVisible();
  }

  async hasDocuments() {
    const count = await this.getDocumentCount();
    return count > 0;
  }
}
