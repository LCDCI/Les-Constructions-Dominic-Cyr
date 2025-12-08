import { test, expect } from '@playwright/test';

test.describe('Project Documents Page', () => {
  const testProjectId = 'BILL-223067';

  test.beforeEach(async ({ page }) => {
    await page.goto(`/projects/${testProjectId}/files`);
  });

  test('should display the project documents page with correct title', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Project Documents');
    await expect(page.locator('h1')).toContainText(testProjectId);
  });

  test('should display upload document button', async ({ page }) => {
    const uploadButton = page.locator('button:has-text("Upload Document")');
    await expect(uploadButton).toBeVisible();
  });

  test('should display document table with headers', async ({ page }) => {
    const table = page.locator('.document-table table');
    await expect(table).toBeVisible();
    
    // Check for table headers
    await expect(table.locator('th:has-text("Document Name")')).toBeVisible();
    await expect(table.locator('th:has-text("Type")')).toBeVisible();
    await expect(table.locator('th:has-text("Uploaded By")')).toBeVisible();
    await expect(table.locator('th:has-text("Upload Date")')).toBeVisible();
    await expect(table.locator('th:has-text("Actions")')).toBeVisible();
  });

  test('should handle loading state', async ({ page }) => {
    // The page should eventually show either documents or "No documents found" message
    await expect(
      page.locator('text=Loading project documents').or(
        page.locator('text=No documents found').or(
          page.locator('.document-table tbody tr')
        )
      )
    ).toBeVisible({ timeout: 10000 });
  });

  test('should display document cards if documents exist', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    
    const documentRows = page.locator('.document-table tbody tr');
    const documentCount = await documentRows.count();
    
    if (documentCount > 0) {
      // First document should have required elements
      const firstRow = documentRows.first();
      await expect(firstRow.locator('td').nth(0)).toBeVisible(); // Document name
      await expect(firstRow.locator('td').nth(1)).toBeVisible(); // Type
      await expect(firstRow.locator('td').nth(2)).toBeVisible(); // Uploaded by
      await expect(firstRow.locator('td').nth(3)).toBeVisible(); // Upload date
      
      // Check for action buttons
      const viewButton = firstRow.locator('button:has-text("View")');
      const deleteButton = firstRow.locator('button:has-text("Delete")');
      
      await expect(viewButton).toBeVisible();
      await expect(deleteButton).toBeVisible();
    }
  });

  test('should show no documents message when empty', async ({ page }) => {
    await page.waitForLoadState('networkidle');
    
    const documentRows = page.locator('.document-table tbody tr');
    const documentCount = await documentRows.count();
    
    if (documentCount === 0) {
      await expect(
        page.locator('text=No documents found for this project')
      ).toBeVisible();
    }
  });
});

test.describe('Project Documents Page - Navigation', () => {
  test('should navigate to documents page directly', async ({ page }) => {
    const testProjectId = 'BILL-223067';
    await page.goto(`/projects/${testProjectId}/files`);
    await expect(page).toHaveURL(`/projects/${testProjectId}/files`);
  });

  test('should handle browser back navigation', async ({ page }) => {
    const testProjectId = 'BILL-223067';
    await page.goto('/');
    await page.goto(`/projects/${testProjectId}/files`);
    await page.goBack();
    await expect(page).toHaveURL('/');
  });
});
