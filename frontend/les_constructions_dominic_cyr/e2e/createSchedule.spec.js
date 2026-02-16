/**
 * E2E tests — Owner Creates Project Schedule.
 *
 * Every test mocks the backend APIs so it runs against the real React UI
 * without needing a running server or Auth0 session.
 *
 * Route: /projects/:projectId/schedule  →  ProjectSchedulePage.jsx
 * Modals: ScheduleFormModal.jsx (create)  ·  TaskModal.jsx (add tasks)
 *
 * Scenario index:
 *   S1  — Success: create schedule (no tasks), task modal opens
 *   S2  — Success: create schedule, then close task modal
 *   A1  — Alternate: cancel the create‑schedule form
 *   A2  — Alternate: close modal via × button
 *   V1  — Validation: missing description (client‑side)
 *   V2  — Validation: missing lot (client‑side)
 *   V3  — Validation: end date before start date (client‑side)
 *   E1  — Exception: POST schedule returns 404 (project not found)
 *   E2  — Exception: POST schedule returns 500 (server error)
 */

import { test, expect } from '@playwright/test';
import { CreateSchedulePage } from './pages/createSchedule.page.js';

const PROJECT_ID = 'PRJ-001';

/** Two mock lots the form can pick from. */
const MOCK_LOTS = [
  {
    lotIdentifier: { lotId: 'LOT-A' },
    lotNumber: '11',
    civicAddress: '123 Elm St',
  },
  {
    lotIdentifier: { lotId: 'LOT-B' },
    lotNumber: '12',
    civicAddress: '456 Oak Ave',
  },
];

// ---------------------------------------------------------------------------
// Owner Creates Project Schedule
// ---------------------------------------------------------------------------
test.describe('Owner Creates Project Schedule', () => {
  // -----------------------------------------------------------------------
  // Success paths
  // -----------------------------------------------------------------------
  test.describe('Success paths', () => {
    test('S1 — Create schedule with valid fields; task modal opens', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);
      await sp.mockRoutes(PROJECT_ID, { lots: MOCK_LOTS });
      await sp.goto(PROJECT_ID);

      // Open create modal via "+ New Work"
      await sp.openCreateModal();
      await expect(sp.createModal).toBeVisible();

      // Fill required fields (matches ScheduleFormModal.jsx inputs)
      await sp.fillForm({
        description: 'Foundation pour',
        lot: 'LOT-A',
        startDate: '2025-03-01',
        endDate: '2025-03-15',
      });

      // Submit — should succeed, create modal closes, task modal opens
      await sp.submitForm();
      await sp.waitForTaskModal();
      await expect(sp.taskModal).toBeVisible();
      await expect(sp.createModal).not.toBeVisible();
    });

    test('S2 — After schedule created, close task modal; schedule card appears', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);
      await sp.mockRoutes(PROJECT_ID, { lots: MOCK_LOTS });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.fillForm({
        description: 'Framing',
        lot: 'LOT-B',
        startDate: '2025-04-01',
        endDate: '2025-04-30',
      });
      await sp.submitForm();
      await sp.waitForTaskModal();

      // Close tree modal with × button
      await sp.closeTaskModal();
      await expect(sp.taskModal).not.toBeVisible();

      // A schedule card should now be visible on the page
      await expect(sp.scheduleCards.first()).toBeVisible();
    });
  });

  // -----------------------------------------------------------------------
  // Alternate paths
  // -----------------------------------------------------------------------
  test.describe('Alternate paths', () => {
    test('A1 — Cancel the create form; modal closes, no schedule created', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);

      let postCalled = false;
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          postCalled = true;
          await route.fulfill({ status: 201, body: '{}' });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();

      // Fill something, then cancel
      await sp.fillForm({ description: 'Will cancel this' });
      await sp.cancelForm();

      // Modal should close
      await expect(sp.createModal).not.toBeVisible();
      // API should NOT have been called
      expect(postCalled).toBe(false);
    });

    test('A2 — Close create modal via × button; modal closes', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);
      await sp.mockRoutes(PROJECT_ID, { lots: MOCK_LOTS });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.closeCreate();
      await expect(sp.createModal).not.toBeVisible();
    });
  });

  // -----------------------------------------------------------------------
  // Validation paths (client‑side — no API call made)
  // -----------------------------------------------------------------------
  test.describe('Validation errors', () => {
    test('V1 — Missing description prevents form submission; no API call', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);

      let postCalled = false;
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          postCalled = true;
          await route.fulfill({ status: 201, body: '{}' });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();

      // Fill everything EXCEPT description (left empty by buildEmptyScheduleForm)
      await sp.fillForm({
        lot: 'LOT-A',
        startDate: '2025-03-01',
        endDate: '2025-03-15',
      });
      await sp.submitForm();

      // Browser required‑field validation prevents form submit
      await expect(sp.createModal).toBeVisible();
      const isInvalid = await sp.descriptionInput.evaluate(
        el => !el.validity.valid
      );
      expect(isInvalid).toBe(true);
      expect(postCalled).toBe(false);
    });

    test('V2 — Missing lot shows error; no API call', async ({ page }) => {
      const sp = new CreateSchedulePage(page);

      let postCalled = false;
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          postCalled = true;
          await route.fulfill({ status: 201, body: '{}' });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.fillForm({
        description: 'Plumbing rough‑in',
        // lot: not selected (default empty)
        startDate: '2025-03-01',
        endDate: '2025-03-15',
      });
      await sp.submitForm();

      await expect(sp.formError).toBeVisible();
      const errorText = await sp.formError.textContent();
      expect(errorText.toLowerCase()).toContain('lot');
      expect(postCalled).toBe(false);
    });

    test('V3 — End date before start date shows error; no API call', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);

      let postCalled = false;
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          postCalled = true;
          await route.fulfill({ status: 201, body: '{}' });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.fillForm({
        description: 'Electrical',
        lot: 'LOT-A',
        startDate: '2025-06-15',
        endDate: '2025-06-01', // before start
      });
      await sp.submitForm();

      await expect(sp.formError).toBeVisible();
      const errorText = await sp.formError.textContent();
      expect(errorText.toLowerCase()).toMatch(/end.*before|date/);
      expect(postCalled).toBe(false);
    });
  });

  // -----------------------------------------------------------------------
  // Exception paths (API errors)
  // -----------------------------------------------------------------------
  test.describe('Exception handling', () => {
    test('E1 — POST returns 404 (project not found); error shown in modal', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          await route.fulfill({
            status: 404,
            contentType: 'application/json',
            body: JSON.stringify({
              message: 'Project not found with identifier: ' + PROJECT_ID,
            }),
          });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.fillForm({
        description: 'Roofing',
        lot: 'LOT-A',
        startDate: '2025-05-01',
        endDate: '2025-05-31',
      });
      await sp.submitForm();

      // Error should appear inside the create modal
      await expect(sp.formError).toBeVisible();
      const errorText = await sp.formError.textContent();
      expect(errorText.toLowerCase()).toMatch(/failed|not found|error/);
      // Modal stays open so user can retry
      await expect(sp.createModal).toBeVisible();
    });

    test('E2 — POST returns 500 (server error); error shown in modal', async ({
      page,
    }) => {
      const sp = new CreateSchedulePage(page);
      await sp.mockRoutes(PROJECT_ID, {
        lots: MOCK_LOTS,
        onPostSchedule: async route => {
          await route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify({ message: 'Internal Server Error' }),
          });
        },
      });
      await sp.goto(PROJECT_ID);

      await sp.openCreateModal();
      await sp.fillForm({
        description: 'Siding',
        lot: 'LOT-B',
        startDate: '2025-07-01',
        endDate: '2025-07-15',
      });
      await sp.submitForm();

      await expect(sp.formError).toBeVisible();
      const errorText = await sp.formError.textContent();
      expect(errorText.toLowerCase()).toMatch(/failed|error|server/);
      await expect(sp.createModal).toBeVisible();
    });
  });
});
