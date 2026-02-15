import { test, expect } from '@playwright/test';

/**
 * E2E tests for Lot Documents Page - Forms functionality
 * Tests form visibility, viewing submitted forms, and read-only enforcement
 */
test.describe('Lot Documents Page - Forms Functionality', () => {
  const testLotId = 'test-lot-123';
  const testProjectId = 'test-project-456';
  
  // Mock form data
  const mockForms = {
    assigned: {
      formId: 'form-assigned-1',
      formType: 'WINDOWS',
      formStatus: 'ASSIGNED',
      lotIdentifier: testLotId,
      assignedAt: new Date().toISOString(),
      formData: {}
    },
    inProgress: {
      formId: 'form-progress-2',
      formType: 'EXTERIOR_DOORS',
      formStatus: 'IN_PROGRESS',
      lotIdentifier: testLotId,
      assignedAt: new Date().toISOString(),
      formData: { doorModel: 'Model A' }
    },
    submitted: {
      formId: 'form-submitted-3',
      formType: 'GARAGE_DOORS',
      formStatus: 'SUBMITTED',
      lotIdentifier: testLotId,
      assignedAt: new Date().toISOString(),
      submittedAt: new Date().toISOString(),
      formData: {
        pdfFile: { fileId: 'file-123', fileName: 'garage-selection.pdf' },
        additionalNotes: 'Customer selected model X with special finish'
      }
    },
    completed: {
      formId: 'form-completed-4',
      formType: 'PAINT',
      formStatus: 'COMPLETED',
      lotIdentifier: testLotId,
      assignedAt: new Date().toISOString(),
      submittedAt: new Date(Date.now() - 86400000).toISOString(),
      completedAt: new Date().toISOString(),
      formData: {
        exteriorColor: 'Navy Blue',
        interiorColor: 'Warm White'
      }
    }
  };

  test.describe('Customer - Form Visibility and Actions', () => {
    test.beforeEach(async ({ page }) => {
      // Mock API responses
      await page.route('**/api/v1/forms', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(Object.values(mockForms))
        });
      });

      await page.route(`**/api/v1/lots/${testLotId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            lotId: testLotId,
            lotNumber: '101',
            civicAddress: '123 Main St',
            lotStatus: 'RESERVED',
            projectIdentifier: testProjectId
          })
        });
      });

      // Navigate to lot documents page as customer
      await page.goto(`/projects/${testProjectId}/lots/${testLotId}/documents`);
      
      // Switch to forms view
      await page.click('[data-testid="view-mode-forms"]');
      await page.waitForSelector('[data-testid="lot-forms-view"]');
    });

    test('should display all forms for the lot', async ({ page }) => {
      const formCards = page.locator('.lot-form-card');
      await expect(formCards).toHaveCount(4);
    });

    test('should show Fill Form button for ASSIGNED forms (customer only)', async ({ page }) => {
      const assignedForm = page.locator(`[data-testid="lot-form-card-${mockForms.assigned.formId}"]`);
      const fillButton = assignedForm.locator('[data-testid^="lot-form-fill-"]');
      
      await expect(fillButton).toBeVisible();
      await expect(fillButton).toHaveText('Fill Form');
    });

    test('should show Fill Form button for IN_PROGRESS forms (customer only)', async ({ page }) => {
      const inProgressForm = page.locator(`[data-testid="lot-form-card-${mockForms.inProgress.formId}"]`);
      const fillButton = inProgressForm.locator('[data-testid^="lot-form-fill-"]');
      
      await expect(fillButton).toBeVisible();
      await expect(fillButton).toHaveText('Fill Form');
    });

    test('should show View Form button ONLY for SUBMITTED forms', async ({ page }) => {
      const submittedForm = page.locator(`[data-testid="lot-form-card-${mockForms.submitted.formId}"]`);
      const viewButton = submittedForm.locator('[data-testid^="lot-form-view-"]');
      
      await expect(viewButton).toBeVisible();
      await expect(viewButton).toHaveText('View Form');
    });

    test('should NOT show View Form button for COMPLETED forms', async ({ page }) => {
      const completedForm = page.locator(`[data-testid="lot-form-card-${mockForms.completed.formId}"]`);
      const viewButton = completedForm.locator('[data-testid^="lot-form-view-"]');
      
      await expect(viewButton).not.toBeVisible();
    });

    test('should NOT show Fill Form button for SUBMITTED forms', async ({ page }) => {
      const submittedForm = page.locator(`[data-testid="lot-form-card-${mockForms.submitted.formId}"]`);
      const fillButton = submittedForm.locator('[data-testid^="lot-form-fill-"]');
      
      await expect(fillButton).not.toBeVisible();
    });
  });

  test.describe('View Submitted Forms - Read-Only Enforcement', () => {
    test.beforeEach(async ({ page }) => {
      // Mock API responses
      await page.route('**/api/v1/forms', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([mockForms.submitted])
        });
      });

      await page.route(`**/api/v1/forms/${mockForms.submitted.formId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockForms.submitted)
        });
      });

      await page.route(`**/api/v1/lots/${testLotId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            lotId: testLotId,
            lotNumber: '101',
            civicAddress: '123 Main St',
            lotStatus: 'RESERVED',
            projectIdentifier: testProjectId
          })
        });
      });

      await page.goto(`/projects/${testProjectId}/lots/${testLotId}/documents`);
      await page.click('[data-testid="view-mode-forms"]');
      await page.waitForSelector('[data-testid="lot-forms-view"]');
    });

    test('should open modal when clicking View Form button', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      const modal = page.locator('.forms-modal-overlay');
      await expect(modal).toBeVisible();
      
      const modalTitle = page.locator('.forms-modal-header h2');
      await expect(modalTitle).toContainText('GARAGE DOORS');
    });

    test('should display customer form data in modal', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      // Check that the form data is displayed
      const modalBody = page.locator('.forms-modal-body');
      await expect(modalBody).toBeVisible();
      
      // Check for file upload display
      const fileDisplay = page.locator('.forms-file-uploaded');
      await expect(fileDisplay).toContainText('garage-selection.pdf');
      
      // Check for additional notes
      const notesField = page.locator('textarea');
      await expect(notesField).toHaveValue('Customer selected model X with special finish');
    });

    test('should make all input fields read-only in submitted form', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      // Check textarea is read-only
      const textarea = page.locator('textarea');
      await expect(textarea).toHaveAttribute('readonly');
      await expect(textarea).toBeDisabled();
    });

    test('should NOT show file upload/remove buttons in submitted form', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      // Should not have file input or remove button
      const fileInput = page.locator('input[type="file"]');
      await expect(fileInput).not.toBeVisible();
      
      const removeButton = page.locator('.forms-file-remove');
      await expect(removeButton).not.toBeVisible();
    });

    test('should show ONLY Close button in modal footer for submitted forms', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      const modalFooter = page.locator('.forms-modal-footer');
      
      // Should have Close button
      const closeButton = modalFooter.locator('button', { hasText: 'Close' });
      await expect(closeButton).toBeVisible();
      
      // Should NOT have Save Draft or Submit buttons
      const saveDraftButton = modalFooter.locator('button', { hasText: 'Save Draft' });
      await expect(saveDraftButton).not.toBeVisible();
      
      const submitButton = modalFooter.locator('button', { hasText: 'Submit Form' });
      await expect(submitButton).not.toBeVisible();
    });

    test('should close modal when clicking Close button', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      const modal = page.locator('.forms-modal-overlay');
      await expect(modal).toBeVisible();
      
      const closeButton = page.locator('.forms-modal-footer button', { hasText: 'Close' });
      await closeButton.click();
      
      await expect(modal).not.toBeVisible();
    });
  });

  test.describe('Salesperson - View Submitted Forms', () => {
    test.beforeEach(async ({ page }) => {
      // Mock API responses for salesperson
      await page.route('**/api/v1/forms', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([mockForms.submitted])
        });
      });

      await page.route(`**/api/v1/forms/${mockForms.submitted.formId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockForms.submitted)
        });
      });

      await page.route(`**/api/v1/lots/${testLotId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            lotId: testLotId,
            lotNumber: '101',
            civicAddress: '123 Main St',
            lotStatus: 'RESERVED',
            projectIdentifier: testProjectId
          })
        });
      });

      // Navigate as salesperson
      await page.goto(`/salesperson/documents`);
      await page.goto(`/projects/${testProjectId}/lots/${testLotId}/documents`);
      await page.click('[data-testid="view-mode-forms"]');
      await page.waitForSelector('[data-testid="lot-forms-view"]');
    });

    test('should see submitted forms', async ({ page }) => {
      const submittedForm = page.locator(`[data-testid="lot-form-card-${mockForms.submitted.formId}"]`);
      await expect(submittedForm).toBeVisible();
    });

    test('should see View Form button for submitted forms', async ({ page }) => {
      const viewButton = page.locator(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      await expect(viewButton).toBeVisible();
    });

    test('should NOT see Fill Form button (salesperson cannot fill)', async ({ page }) => {
      const fillButton = page.locator('[data-testid^="lot-form-fill-"]');
      await expect(fillButton).not.toBeVisible();
    });

    test('salesperson should see read-only modal with Close button only', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      const modal = page.locator('.forms-modal-overlay');
      await expect(modal).toBeVisible();
      
      // Should only have Close button
      const closeButton = page.locator('.forms-modal-footer button', { hasText: 'Close' });
      await expect(closeButton).toBeVisible();
      
      // Should NOT have edit buttons
      const saveDraftButton = page.locator('.forms-modal-footer button', { hasText: 'Save Draft' });
      await expect(saveDraftButton).not.toBeVisible();
    });
  });

  test.describe('Contractor - View Submitted Forms', () => {
    test.beforeEach(async ({ page }) => {
      // Mock API responses for contractor
      await page.route('**/api/v1/forms', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([mockForms.submitted])
        });
      });

      await page.route(`**/api/v1/forms/${mockForms.submitted.formId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockForms.submitted)
        });
      });

      await page.route(`**/api/v1/lots/${testLotId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            lotId: testLotId,
            lotNumber: '101',
            civicAddress: '123 Main St',
            lotStatus: 'RESERVED',
            projectIdentifier: testProjectId
          })
        });
      });

      // Navigate as contractor
      await page.goto(`/contractors/documents`);
      await page.goto(`/projects/${testProjectId}/lots/${testLotId}/documents`);
      await page.click('[data-testid="view-mode-forms"]');
      await page.waitForSelector('[data-testid="lot-forms-view"]');
    });

    test('contractor should see submitted forms', async ({ page }) => {
      const submittedForm = page.locator(`[data-testid="lot-form-card-${mockForms.submitted.formId}"]`);
      await expect(submittedForm).toBeVisible();
    });

    test('contractor should see View Form button', async ({ page }) => {
      const viewButton = page.locator(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      await expect(viewButton).toBeVisible();
    });

    test('contractor should see read-only modal (cannot edit)', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      // All fields should be read-only
      const textarea = page.locator('textarea');
      await expect(textarea).toHaveAttribute('readonly');
      await expect(textarea).toBeDisabled();
    });
  });

  test.describe('Owner - View Submitted Forms', () => {
    test.beforeEach(async ({ page }) => {
      // Mock API responses for owner
      await page.route('**/api/v1/forms', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([mockForms.submitted])
        });
      });

      await page.route(`**/api/v1/forms/${mockForms.submitted.formId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockForms.submitted)
        });
      });

      await page.route(`**/api/v1/lots/${testLotId}`, async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            lotId: testLotId,
            lotNumber: '101',
            civicAddress: '123 Main St',
            lotStatus: 'RESERVED',
            projectIdentifier: testProjectId
          })
        });
      });

      // Navigate as owner
      await page.goto(`/owner/documents`);
      await page.goto(`/projects/${testProjectId}/lots/${testLotId}/documents`);
      await page.click('[data-testid="view-mode-forms"]');
      await page.waitForSelector('[data-testid="lot-forms-view"]');
    });

    test('owner should see all submitted forms', async ({ page }) => {
      const submittedForm = page.locator(`[data-testid="lot-form-card-${mockForms.submitted.formId}"]`);
      await expect(submittedForm).toBeVisible();
    });

    test('owner should see View Form button', async ({ page }) => {
      const viewButton = page.locator(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      await expect(viewButton).toBeVisible();
    });

    test('owner should see read-only modal with customer data', async ({ page }) => {
      await page.click(`[data-testid="lot-form-view-${mockForms.submitted.formId}"]`);
      
      const modal = page.locator('.forms-modal-overlay');
      await expect(modal).toBeVisible();
      
      // Should display customer's form data
      const textarea = page.locator('textarea');
      await expect(textarea).toHaveValue('Customer selected model X with special finish');
      await expect(textarea).toBeDisabled();
    });
  });
});
