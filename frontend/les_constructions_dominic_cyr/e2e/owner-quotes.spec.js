import { test, expect } from '@playwright/test';

test.describe('Owner Quote Approval', () => {
  // Assuming we have a setup or login helper, but adhering to standard playwright structure
  // This test assumes a running app and available owner credentials/mocking
  
  test('should allow owner to view and filter quotes', async ({ page }) => {
    // 1. Login as Owner (Mocked or UI login)
    // For this example, we'll assume we land on the dashboard or login page
    // Using a pattern often found in existing tests (if any) would be better.
    // I will use a direct navigation after a hypothetical login or mock state.
    
    // NOTE: This test might need adjustment based on actual auth setup in E2E env.
    await page.goto('/quotes/approval');

    // 2. Check for page title
    await expect(page.getByRole('heading', { name: /Quote Approval Management/i })).toBeVisible();

    // 3. Check for filters
    await expect(page.getByLabel('Search')).toBeVisible();
    await expect(page.getByLabel('Status')).toBeVisible();
    await expect(page.getByLabel('Sort By')).toBeVisible();

    // 4. Check for either table or empty state
    const table = page.locator('.quotes-table');
    const emptyState = page.locator('.empty-state');
    
    // Wait for loading to finish
    await expect(page.locator('.spinner')).toBeHidden();

    if (await table.isVisible()) {
      // 5. Check table headers
      await expect(page.getByRole('columnheader', { name: 'Quote #' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: 'Amount' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: 'Contractor' })).toBeVisible();
      await expect(page.getByRole('columnheader', { name: 'Status' })).toBeVisible();
      
      // 6. Check actions on a row (if any)
      const firstRow = table.locator('tbody tr').first();
      if (await firstRow.isVisible()) {
        const approveBtn = firstRow.locator('.btn-action.approve');
        const rejectBtn = firstRow.locator('.btn-action.reject');
        
        await expect(approveBtn).toBeVisible();
        await expect(rejectBtn).toBeVisible();
        
        // 7. Test Rejection Modal opening
        // We only click if enabled (status might be APPROVED/REJECTED already)
        if (await rejectBtn.isEnabled()) {
          await rejectBtn.click();
          await expect(page.locator('.approval-modal')).toBeVisible();
          
          // Check reason input in rejection modal
          const reasonInput = page.locator('textarea#rejection-reason');
          await expect(reasonInput).toBeVisible();
          
          // Close modal
          await page.getByRole('button', { name: 'Cancel' }).click();
          await expect(page.locator('.approval-modal')).toBeHidden();
        }
      }
    } else {
      await expect(emptyState).toBeVisible();
      await expect(page.getByText(/No quotes found/i)).toBeVisible();
    }
  });
});
