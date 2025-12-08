import { test, expect } from '@playwright/test';
import { ProjectFilesPage } from './pages/projectFiles.page.js';

test.describe('Project Files Page', () => {
  let projectFilesPage;

  test.beforeEach(async ({ page }) => {
    projectFilesPage = new ProjectFilesPage(page);
    await projectFilesPage.goto();
    await projectFilesPage.waitForDocumentsToLoad();
  });

  test('should display the page title', async () => {
    await expect(projectFilesPage.pageTitle).toBeVisible();
    await expect(projectFilesPage.pageTitle).toContainText('Project Documents');
  });

  test('should display upload button', async () => {
    await expect(projectFilesPage.uploadButton).toBeVisible();
    await expect(projectFilesPage.uploadButton).toContainText('Upload Document');
  });

  test('should display document table when documents exist', async () => {
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (hasDocuments) {
      await expect(projectFilesPage.documentTable).toBeVisible();
      
      const count = await projectFilesPage.getDocumentCount();
      expect(count).toBeGreaterThan(0);
    }
  });

  test('should display no documents message when list is empty', async () => {
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (!hasDocuments) {
      await expect(projectFilesPage.noDocumentsMessage).toBeVisible();
    }
  });

  test('should display document information in table rows', async () => {
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (hasDocuments) {
      const firstRow = projectFilesPage.documentRows.first();
      await expect(firstRow).toBeVisible();
      
      // Check that table has the expected columns
      const headers = projectFilesPage.page.locator('.document-table thead th');
      const headerCount = await headers.count();
      expect(headerCount).toBe(4); // Name, Type, Uploaded By, Actions
      
      await expect(headers.nth(0)).toContainText('Document Name');
      await expect(headers.nth(1)).toContainText('Type');
      await expect(headers.nth(2)).toContainText('Uploaded By');
      await expect(headers.nth(3)).toContainText('Actions');
    }
  });

  test('should have view and delete buttons for each document', async () => {
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (hasDocuments) {
      const viewButtonCount = await projectFilesPage.viewButtons.count();
      const deleteButtonCount = await projectFilesPage.deleteButtons.count();
      
      expect(viewButtonCount).toBeGreaterThan(0);
      expect(deleteButtonCount).toBeGreaterThan(0);
      expect(viewButtonCount).toBe(deleteButtonCount);
      
      await expect(projectFilesPage.viewButtons.first()).toBeEnabled();
      await expect(projectFilesPage.deleteButtons.first()).toBeEnabled();
    }
  });

  test('should open view button in new tab', async ({ page, context }) => {
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (hasDocuments) {
      const firstViewButton = projectFilesPage.viewButtons.first();
      const link = await firstViewButton.locator('..').getAttribute('href');
      
      expect(link).toBeTruthy();
      expect(link).toContain('/files/');
      
      // Check that link has target="_blank"
      const target = await firstViewButton.locator('..').getAttribute('target');
      expect(target).toBe('_blank');
    }
  });
});

test.describe('Project Files Page - Navigation', () => {
  test('should navigate to project files page directly', async ({ page }) => {
    await page.goto('/project-files');
    await expect(page).toHaveURL(/.*project-files/);
  });

  test('should handle browser back navigation', async ({ page }) => {
    await page.goto('/');
    await page.goto('/project-files');
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});

test.describe('Project Files Page - Print Functionality', () => {
  test('should have print styles defined', async ({ page }) => {
    await page.goto('/project-files');
    
    // Emulate print media
    await page.emulateMedia({ media: 'print' });
    
    const projectFilesPage = new ProjectFilesPage(page);
    await projectFilesPage.waitForDocumentsToLoad();
    
    // Check that action buttons are hidden in print view
    const uploadButton = await projectFilesPage.uploadButton.isVisible();
    expect(uploadButton).toBe(false);
    
    // Document table should still be visible
    const hasDocuments = await projectFilesPage.hasDocuments();
    if (hasDocuments) {
      await expect(projectFilesPage.documentTable).toBeVisible();
    }
    
    // Reset media emulation
    await page.emulateMedia({ media: 'screen' });
  });

  test('should display document list correctly in print preview', async ({ page }) => {
    await page.goto('/project-files');
    
    const projectFilesPage = new ProjectFilesPage(page);
    await projectFilesPage.waitForDocumentsToLoad();
    
    const hasDocuments = await projectFilesPage.hasDocuments();
    
    if (hasDocuments) {
      // Switch to print media
      await page.emulateMedia({ media: 'print' });
      
      // Verify table is still visible
      await expect(projectFilesPage.documentTable).toBeVisible();
      
      // Verify rows are still visible
      const count = await projectFilesPage.getDocumentCount();
      expect(count).toBeGreaterThan(0);
      
      // Reset
      await page.emulateMedia({ media: 'screen' });
    }
  });
});
