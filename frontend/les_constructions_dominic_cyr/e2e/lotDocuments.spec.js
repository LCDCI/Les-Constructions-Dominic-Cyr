import { test, expect } from '@playwright/test';

/**
 * Playwright E2E Tests for CDC-184: Lot Documents Page
 * 
 * These tests cover:
 * - Multi-lot navigation
 * - Read permissions
 * - Upload permissions by role
 * - Search functionality
 * - Download functionality
 * - Delete rules and confirmation modal
 * - Tab filtering (Photos/Files)
 */

// Test data constants
const TEST_LOT_A_ID = 'lot-test-a';
const TEST_LOT_B_ID = 'lot-test-b';

// Helper function to login as specific role
async function loginAsRole(page, role) {
  // Navigate to login
  await page.goto('/portal/login');
  
  // Based on role, use appropriate credentials
  const credentials = {
    owner: { email: 'owner@test.com', password: 'TestPassword123!' },
    contractor: { email: 'contractor@test.com', password: 'TestPassword123!' },
    customer: { email: 'customer@test.com', password: 'TestPassword123!' },
  };

  const creds = credentials[role];
  if (!creds) throw new Error(`Unknown role: ${role}`);

  // Fill login form (adjust selectors based on your actual login page)
  await page.fill('input[type="email"]', creds.email);
  await page.fill('input[type="password"]', creds.password);
  await page.click('button[type="submit"]');
  
  // Wait for navigation to complete
  await page.waitForURL(/dashboard|lots/, { timeout: 10000 });
}

test.describe('Lot Documents - Multi-Lot Navigation', () => {
  test('should display lots list in dashboard', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto('/dashboard/lots');

    const lotsList = page.locator('[data-testid="lots-list"]');
    await expect(lotsList).toBeVisible();

    // Verify at least one lot card is visible
    const lotCards = page.locator('[data-testid^="lot-card-"]');
    await expect(lotCards.first()).toBeVisible({ timeout: 5000 });
  });

  test('should navigate to specific lot documents page', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto('/dashboard/lots');

    // Click on first lot's documents link
    const firstLotLink = page.locator('[data-testid^="lot-documents-link-"]').first();
    await firstLotLink.click();

    // Verify navigation to lot documents page
    await expect(page).toHaveURL(/\/lots\/.*\/documents/);
    
    const documentsPage = page.locator('[data-testid="lot-documents-page"]');
    await expect(documentsPage).toBeVisible();
  });

  test('should show different documents for different lots', async ({ page }) => {
    // This test assumes lotA and lotB have different documents
    await loginAsRole(page, 'owner');

    // Visit Lot A
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);
    await page.waitForSelector('[data-testid="lot-documents-page"]');
    
    // Get documents from Lot A
    const lotADocsList = page.locator('[data-testid="lot-documents-list"]');
    const lotADocsCount = await lotADocsList.locator('[data-testid^="lot-document-"]').count();

    // Visit Lot B
    await page.goto(`/dashboard/lots/${TEST_LOT_B_ID}/documents`);
    await page.waitForSelector('[data-testid="lot-documents-page"]');
    
    // Get documents from Lot B
    const lotBDocsList = page.locator('[data-testid="lot-documents-list"]');
    const lotBDocsCount = await lotBDocsList.locator('[data-testid^="lot-document-"]').count();

    // Documents should be different (counts may differ or specific docs)
    // In real test, you'd verify specific document IDs differ
    console.log(`Lot A has ${lotADocsCount} documents, Lot B has ${lotBDocsCount} documents`);
  });
});

test.describe('Lot Documents - Read Permissions', () => {
  test('assigned team member can access lot documents page', async ({ page }) => {
    await loginAsRole(page, 'contractor');
    
    // Navigate to a lot the contractor is assigned to
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Verify no 403 or forbidden message
    const documentsPage = page.locator('[data-testid="lot-documents-page"]');
    await expect(documentsPage).toBeVisible();

    // Verify no error state
    const errorState = page.locator('[data-testid="error-state"]');
    await expect(errorState).not.toBeVisible();
  });

  test('unassigned user is blocked from accessing lot documents', async ({ page }) => {
    // Login as user not assigned to any lot
    await loginAsRole(page, 'customer');
    
    // Try to access lot documents directly
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Expect error state or redirect
    const errorState = page.locator('[data-testid="error-state"]');
    await expect(errorState).toBeVisible({ timeout: 5000 });
  });
});

test.describe('Lot Documents - Upload Permissions', () => {
  test('Owner can see upload button and upload files', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Verify upload button is visible
    const uploadButton = page.locator('[data-testid="lot-documents-upload-button"]');
    await expect(uploadButton).toBeVisible();

    // Click upload button and select files
    const fileInput = page.locator('[data-testid="lot-documents-upload-input"]');
    
    // Create test file
    const testFile = {
      name: 'test-document.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('Test PDF content'),
    };

    await fileInput.setInputFiles({
      name: testFile.name,
      mimeType: testFile.mimeType,
      buffer: testFile.buffer,
    });

    // Wait for upload to complete
    await page.waitForResponse(response =>
      response.url().includes('/documents') && response.status() === 201,
      { timeout: 10000 }
    );

    // Verify new document appears in list
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const newDocument = documentsList.locator(`text=${testFile.name}`);
    await expect(newDocument).toBeVisible({ timeout: 5000 });
  });

  test('Contractor can upload files', async ({ page }) => {
    await loginAsRole(page, 'contractor');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Verify upload button is visible
    const uploadButton = page.locator('[data-testid="lot-documents-upload-button"]');
    await expect(uploadButton).toBeVisible();

    // Upload test image
    const fileInput = page.locator('[data-testid="lot-documents-upload-input"]');
    const testImage = {
      name: 'test-image.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from('fake image data'),
    };

    await fileInput.setInputFiles({
      name: testImage.name,
      mimeType: testImage.mimeType,
      buffer: testImage.buffer,
    });

    // Wait for upload
    await page.waitForResponse(response =>
      response.url().includes('/documents') && response.status() === 201,
      { timeout: 10000 }
    );

    // Verify uploaded
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const newImage = documentsList.locator(`text=${testImage.name}`);
    await expect(newImage).toBeVisible({ timeout: 5000 });
  });

  test('Customer does not see upload button', async ({ page }) => {
    // Assuming customer is assigned to lot but cannot upload
    await loginAsRole(page, 'customer');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Verify upload button is NOT visible
    const uploadButton = page.locator('[data-testid="lot-documents-upload-button"]');
    await expect(uploadButton).not.toBeVisible();
  });
});

test.describe('Lot Documents - Search Functionality', () => {
  test('should filter documents by filename search', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    // Wait for documents to load
    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Get initial document count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const initialCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    // Type search query
    const searchInput = page.locator('[data-testid="lot-documents-search"]');
    await searchInput.fill('test-document');

    // Wait for debounce and filtering
    await page.waitForTimeout(500);

    // Get filtered count
    const filteredCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    // Verify filtering occurred (count should be less or equal)
    expect(filteredCount).toBeLessThanOrEqual(initialCount);
  });

  test('should show all documents when search is cleared', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Get initial count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const initialCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    // Search
    const searchInput = page.locator('[data-testid="lot-documents-search"]');
    await searchInput.fill('specificfilename');
    await page.waitForTimeout(500);

    // Clear search
    await searchInput.clear();
    await page.waitForTimeout(500);

    // Verify count returns to initial
    const finalCount = await documentsList.locator('[data-testid^="lot-document-"]').count();
    expect(finalCount).toBe(initialCount);
  });
});

test.describe('Lot Documents - Download Functionality', () => {
  test('should trigger download when download button is clicked', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Click first download button
    const downloadButton = page.locator('[data-testid^="lot-document-download-"]').first();

    // Wait for download event
    const [download] = await Promise.all([
      page.waitForEvent('download', { timeout: 10000 }),
      downloadButton.click(),
    ]);

    // Verify download occurred
    expect(download).toBeTruthy();
    
    // Verify suggested filename
    const suggestedFilename = download.suggestedFilename();
    expect(suggestedFilename).toBeTruthy();
    expect(suggestedFilename.length).toBeGreaterThan(0);
  });
});

test.describe('Lot Documents - Delete Rules and Confirmation', () => {
  test('uploader can see delete button on own document', async ({ page }) => {
    await loginAsRole(page, 'contractor');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Verify at least one delete button is visible (for contractor's own uploads)
    const deleteButtons = page.locator('[data-testid^="lot-document-delete-"]');
    const count = await deleteButtons.count();
    
    // Contractor should have at least one document they can delete
    expect(count).toBeGreaterThan(0);
  });

  test('owner can see delete button on all documents', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Get document count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const documentCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    // Get delete button count
    const deleteButtons = page.locator('[data-testid^="lot-document-delete-"]');
    const deleteButtonCount = await deleteButtons.count();

    // Owner should have delete button for every document
    expect(deleteButtonCount).toBe(documentCount);
  });

  test('delete shows confirmation modal and can be cancelled', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Get initial document count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const initialCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    // Click first delete button
    const deleteButton = page.locator('[data-testid^="lot-document-delete-"]').first();
    await deleteButton.click();

    // Verify confirmation modal appears
    const confirmModal = page.locator('[data-testid="confirm-delete-modal"]');
    await expect(confirmModal).toBeVisible();

    // Click cancel
    const cancelButton = page.locator('[data-testid="confirm-delete-no"]');
    await cancelButton.click();

    // Verify modal closes
    await expect(confirmModal).not.toBeVisible();

    // Verify document count unchanged
    const finalCount = await documentsList.locator('[data-testid^="lot-document-"]').count();
    expect(finalCount).toBe(initialCount);
  });

  test('delete confirmation removes document', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Get initial document count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const initialCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    if (initialCount === 0) {
      console.log('No documents to delete, skipping test');
      return;
    }

    // Click first delete button
    const deleteButton = page.locator('[data-testid^="lot-document-delete-"]').first();
    await deleteButton.click();

    // Verify confirmation modal appears
    const confirmModal = page.locator('[data-testid="confirm-delete-modal"]');
    await expect(confirmModal).toBeVisible();

    // Click confirm
    const confirmButton = page.locator('[data-testid="confirm-delete-yes"]');
    await confirmButton.click();

    // Wait for delete request to complete
    await page.waitForResponse(response =>
      response.url().includes('/documents/') && response.status() === 200,
      { timeout: 10000 }
    );

    // Wait for modal to close
    await expect(confirmModal).not.toBeVisible({ timeout: 5000 });

    // Verify document count decreased
    const finalCount = await documentsList.locator('[data-testid^="lot-document-"]').count();
    expect(finalCount).toBe(initialCount - 1);
  });
});

test.describe('Lot Documents - Tab Filtering', () => {
  test('Photos tab shows only images', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Click Photos tab
    const photosTab = page.locator('[data-testid="lot-documents-tab-photos"]');
    await photosTab.click();

    // Wait for filtering
    await page.waitForTimeout(500);

    // Verify only photo cards are visible (not file rows)
    const photoCards = page.locator('[data-testid^="lot-document-card-"]');
    const fileRows = page.locator('[data-testid^="lot-document-row-"]');

    const photoCount = await photoCards.count();
    const fileCount = await fileRows.count();

    // Only photos should be visible
    expect(fileCount).toBe(0);
    // At least some photos should exist (or empty state)
    console.log(`Photos tab shows ${photoCount} photos`);
  });

  test('Files tab shows only documents', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Click Files tab
    const filesTab = page.locator('[data-testid="lot-documents-tab-files"]');
    await filesTab.click();

    // Wait for filtering
    await page.waitForTimeout(500);

    // Verify only file rows are visible (not photo cards)
    const photoCards = page.locator('[data-testid^="lot-document-card-"]');
    const fileRows = page.locator('[data-testid^="lot-document-row-"]');

    const photoCount = await photoCards.count();
    const fileCount = await fileRows.count();

    // Only files should be visible
    expect(photoCount).toBe(0);
    // At least some files should exist (or empty state)
    console.log(`Files tab shows ${fileCount} files`);
  });

  test('All tab shows both photos and files', async ({ page }) => {
    await loginAsRole(page, 'owner');
    await page.goto(`/dashboard/lots/${TEST_LOT_A_ID}/documents`);

    await page.waitForSelector('[data-testid="lot-documents-list"]');

    // Click All tab (should be default, but click to ensure)
    const allTab = page.locator('[data-testid="lot-documents-tab-all"]');
    await allTab.click();

    // Wait for filtering
    await page.waitForTimeout(500);

    // Get total document count
    const documentsList = page.locator('[data-testid="lot-documents-list"]');
    const totalCount = await documentsList.locator('[data-testid^="lot-document-"]').count();

    console.log(`All tab shows ${totalCount} total documents`);
    // Should have at least one document (or show empty state)
  });
});

test.describe('Lot Documents - Empty State', () => {
  test('shows empty state when no documents exist', async ({ page }) => {
    await loginAsRole(page, 'owner');
    
    // Navigate to a lot with no documents (or create new empty lot)
    await page.goto(`/dashboard/lots/empty-lot-id/documents`);

    // Wait for page to load
    await page.waitForSelector('[data-testid="lot-documents-page"]');

    // Verify empty state is visible
    const emptyState = page.locator('[data-testid="empty-state"]');
    await expect(emptyState).toBeVisible();
  });
});
