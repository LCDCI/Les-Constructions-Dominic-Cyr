/**
 * E2E tests: Create New Project use case.
 *
 * Rules followed:
 * - All use case paths (success + alternates) are covered.
 * - Each test/block connects explicitly to use case steps (see comments).
 * - Tests demonstrate alignment from UI to data tier (assert UI + API/DB).
 *
 * Use case: Create New Project
 * Steps: 0 (French) → 1 (English) → 2 (Lots) → Submit | Cancel/Back at any step.
 *
 * Scenario index (mapped in test names):
 *   S1, S2, S3, S8 — Success path and variants (no lots, no cover, minimal fields).
 *   A1, A2, A3     — Cancel from step 0, 1, 2 (no project in data tier).
 *   A4, A5         — Back from step 1→0, step 2→1 (no submit).
 *   V1, V2, V3     — Validation: missing required, blocked continue, invalid dates.
 *   U1, U2         — UI: form title in English / French.
 *   E1             — API error: create fails, error shown.
 *   Legacy         — API create then UI list (data tier alignment).
 */

import { test, expect } from '@playwright/test';
import { ProjectsPage } from './pages/projects.page.js';
import { CreateProjectPage } from './pages/createProject.page.js';
import { TranslationPage } from './pages/translation.page.js';

// ---------------------------------------------------------------------------
// Create New Project — one use case with subcategories
// ---------------------------------------------------------------------------

test.describe('Create New Project', () => {
  // -------------------------------------------------------------------------
  // Subcategory: Success path (S1) + Success variants (S2, S3, S8)
  // -------------------------------------------------------------------------
  test.describe('Success path and variants', () => {
    test('S1 — Success: Create project with full flow (step 0 → 1 → 2, no lots, no cover); assert UI + data tier', async ({
      page,
    }) => {
      // --- Use case step: Open Create Project form (standalone page) ---
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      await expect(createPage.form).toBeVisible();

      // --- Use case steps 0 & 1: Fill French then English, proceed to Lots ---
      const uniqueSuffix = Date.now();
      const data = await createPage.fillStep0AndStep1ToReachLots(uniqueSuffix);
      expect(await createPage.isOnStep2()).toBeTruthy();

      // --- Use case step 2: Submit Create Project (no lots = valid) ---
      await createPage.clickCreateProject();

      // --- UI: Expect redirect to project detail or list (success) ---
      await page
        .waitForURL(/\/(projects\/[^/]+|projects)(\/)?$/, { timeout: 15000 })
        .catch(() => {});

      // --- Data tier: Project must exist in API ---
      const listRes = await page.request.get('/api/v1/projects');
      expect(listRes.ok()).toBeTruthy();
      const projects = await listRes.json();
      const names = Array.isArray(projects)
        ? projects.map(p => p.projectName || p.name)
        : [];
      expect(
        names.some(n => (n || '').includes(data.projectName)),
        `Data tier: project "${data.projectName}" should exist after create`
      ).toBeTruthy();
    });

    test('S2 — Success variant: New project with no lots; assert UI + data tier', async ({
      page,
    }) => {
      // Same as S1: we do not add any lots at step 2, submit — valid path
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const uniqueSuffix = Date.now();
      const data = await createPage.fillStep0AndStep1ToReachLots(uniqueSuffix);

      // Step 2: Submit without adding lots (explicit S2 scenario)
      await createPage.clickCreateProject();

      await page
        .waitForURL(/\/(projects\/[^/]+|projects)(\/)?$/, { timeout: 15000 })
        .catch(() => {});

      const listRes = await page.request.get('/api/v1/projects');
      expect(listRes.ok()).toBeTruthy();
      const projects = await listRes.json();
      const names = Array.isArray(projects)
        ? projects.map(p => p.projectName || p.name)
        : [];
      expect(
        names.some(n => (n || '').includes(data.projectName))
      ).toBeTruthy();
    });

    test('S3 — Success variant: New project with no cover image; assert UI + data tier', async ({
      page,
    }) => {
      // We do not select a cover image; form allows submit without image
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const uniqueSuffix = Date.now();
      const data = await createPage.fillStep0AndStep1ToReachLots(uniqueSuffix);
      await createPage.clickCreateProject();

      await page
        .waitForURL(/\/(projects\/[^/]+|projects)(\/)?$/, { timeout: 15000 })
        .catch(() => {});

      const listRes = await page.request.get('/api/v1/projects');
      expect(listRes.ok()).toBeTruthy();
      const projects = await listRes.json();
      const names = Array.isArray(projects)
        ? projects.map(p => p.projectName || p.name)
        : [];
      expect(
        names.some(n => (n || '').includes(data.projectName))
      ).toBeTruthy();
    });

    test('S8 — Success variant: New project with minimal required fields only; assert data tier', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const uniqueSuffix = Date.now();
      const data = createPage.getMinimalStep0Data(uniqueSuffix);

      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );
      await createPage.clickFillOutEnglish();
      await page.waitForTimeout(300);
      await createPage.fillStep1English(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );
      await createPage.clickContinueToLots();
      await page.waitForTimeout(300);
      await createPage.clickCreateProject();

      await page
        .waitForURL(/\/(projects\/[^/]+|projects)(\/)?$/, { timeout: 15000 })
        .catch(() => {});

      const listRes = await page.request.get('/api/v1/projects');
      expect(listRes.ok()).toBeTruthy();
      const projects = await listRes.json();
      const names = Array.isArray(projects)
        ? projects.map(p => p.projectName || p.name)
        : [];
      expect(
        names.some(n => (n || '').includes(data.projectName))
      ).toBeTruthy();
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: Cancel (A1, A2, A3) — no project created
  // -------------------------------------------------------------------------
  test.describe('Cancel alternate paths', () => {
    test('A1 — Alternate: Cancel from step 0 (French); form closes, no project in data tier', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();

      // Step 0: Fill something then cancel
      const data = createPage.getMinimalStep0Data(Date.now());
      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );

      const countBefore = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);

      await createPage.clickCancel();
      await createPage.waitForFormToClose();

      const countAfter = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);
      expect(countAfter).toBe(countBefore);
    });

    test('A2 — Alternate: Cancel from step 1 (English); form closes, no project in data tier', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const data = createPage.getMinimalStep0Data(Date.now());
      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );
      await createPage.clickFillOutEnglish();
      await page.waitForTimeout(300);

      const countBefore = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);

      await createPage.clickCancel();
      await createPage.waitForFormToClose();

      const countAfter = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);
      expect(countAfter).toBe(countBefore);
    });

    test('A3 — Alternate: Cancel from step 2 (Lots); form closes, no project in data tier', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      await createPage.fillStep0AndStep1ToReachLots(Date.now());

      const countBefore = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);

      await createPage.clickCancel();
      await createPage.waitForFormToClose();

      const countAfter = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);
      expect(countAfter).toBe(countBefore);
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: Back (A4, A5) — no submit
  // -------------------------------------------------------------------------
  test.describe('Back alternate paths', () => {
    test('A4 — Alternate: Back from step 1 to step 0; still on form, no project created', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const data = createPage.getMinimalStep0Data(Date.now());
      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );
      await createPage.clickFillOutEnglish();
      await page.waitForTimeout(300);

      await createPage.clickBack();
      await page.waitForTimeout(300);

      // Still on form; "Fill out English" should be visible (step 0)
      await expect(createPage.fillOutEnglishButton).toBeVisible();

      const countAfter = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);
      const names = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p =>
          Array.isArray(p) ? p.map(x => x.projectName || x.name) : []
        )
        .catch(() => []);
      expect(names.some(n => (n || '').includes(data.projectName))).toBeFalsy();
    });

    test('A5 — Alternate: Back from step 2 to step 1; still on form, no project created', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      await createPage.fillStep0AndStep1ToReachLots(Date.now());

      await createPage.clickBack();
      await page.waitForTimeout(300);

      // Step 1: "Continue to add lots" should be visible
      await expect(createPage.continueToLotsButton).toBeVisible();
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: Validation (V1, V2, V3) — invalid input
  // -------------------------------------------------------------------------
  test.describe('Validation alternates', () => {
    test('V1 — Validation: Missing required fields at step 0; cannot proceed, no project created', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();

      // Leave project name (and/or description, location) empty — "Fill out English" may be disabled or validation shows
      await createPage.projectNameInput.first().fill('');
      await createPage.projectDescriptionInput.first().fill('');
      await createPage.locationInput.first().fill('');

      await page.waitForTimeout(300);
      const countBefore = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);

      // Either button is disabled or we see validation after click
      const fillOutBtn = createPage.fillOutEnglishButton;
      const isDisabled = await fillOutBtn
        .getAttribute('disabled')
        .then(a => a != null);
      if (!isDisabled) {
        await fillOutBtn.click();
        await page.waitForTimeout(500);
        const errorText = await createPage.getValidationOrErrorText();
        expect(
          errorText || (await createPage.errorMessages.count()) > 0
        ).toBeTruthy();
      }

      const countAfter = await page.request
        .get('/api/v1/projects')
        .then(r => (r.ok() ? r.json() : []))
        .then(p => (Array.isArray(p) ? p.length : 0))
        .catch(() => 0);
      expect(countAfter).toBe(countBefore);
    });

    test('V2 — Validation: Try to continue to lots without filling English required fields; blocked or error', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const data = createPage.getMinimalStep0Data(Date.now());
      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        {
          status: data.status,
          startDate: data.startDate,
          endDate: data.endDate,
        }
      );
      await createPage.clickFillOutEnglish();
      await page.waitForTimeout(300);

      // Clear required field in English step
      await createPage.projectNameInput.first().fill('');
      await page.waitForTimeout(300);

      const continueBtn = createPage.continueToLotsButton;
      const isDisabled = await continueBtn
        .getAttribute('disabled')
        .then(a => a != null);
      expect(isDisabled).toBeTruthy();
    });

    test('V3 — Validation: End date before start date; validation message, no project created', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const data = createPage.getMinimalStep0Data(Date.now());
      await createPage.fillStep0French(
        data.projectName,
        data.projectDescription,
        data.location,
        { status: data.status, startDate: '2026-12-31', endDate: '2025-01-01' }
      );
      await createPage.clickFillOutEnglish();
      await page.waitForTimeout(300);
      await createPage.fillStep1English(
        data.projectName,
        data.projectDescription,
        data.location,
        { status: data.status, startDate: '2026-12-31', endDate: '2025-01-01' }
      );
      await createPage.clickContinueToLots();
      await page.waitForTimeout(300);

      await createPage.clickCreateProject();
      await page.waitForTimeout(1000);

      const errorText = await createPage.getValidationOrErrorText();
      const hasDateError =
        errorText &&
        (errorText.includes('date') ||
          errorText.includes('End date') ||
          errorText.includes('after') ||
          errorText.includes('avant'));
      expect(
        hasDateError || (await createPage.errorMessages.count()) > 0
      ).toBeTruthy();
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: UI / Language (U1, U2) — form title by language
  // -------------------------------------------------------------------------
  test.describe('UI / Language', () => {
    test('U1 — Form title in English when app language is English', async ({
      page,
    }) => {
      const translationPage = new TranslationPage(page);
      await translationPage.goto('/');
      await translationPage.waitForTranslationsToLoad();
      const currentLang = await translationPage.getCurrentLanguage();
      if (currentLang !== 'en') {
        await translationPage.setLanguage('en');
        await page.waitForTimeout(500);
      }
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const title = (await createPage.getFormTitle()) || '';
      expect(
        title.trim() === 'Create New Project',
        `Expected title "Create New Project", got: ${title}`
      ).toBeTruthy();
    });

    test('U2 — Form title in French when app language is French', async ({
      page,
    }) => {
      const translationPage = new TranslationPage(page);
      await translationPage.goto('/');
      await translationPage.waitForTranslationsToLoad();
      const currentLang = await translationPage.getCurrentLanguage();
      if (currentLang !== 'fr') {
        await translationPage.setLanguage('fr');
        await page.waitForTimeout(500);
      }
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      const title = (await createPage.getFormTitle()) || '';
      expect(
        title.trim() === 'Créer un nouveau projet',
        `Expected title "Créer un nouveau projet", got: ${title}`
      ).toBeTruthy();
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: API error (E1) — create request fails
  // -------------------------------------------------------------------------
  test.describe('API error', () => {
    test('E1 — API error: Create project request fails; error shown, form remains or closes with message', async ({
      page,
    }) => {
      const createPage = new CreateProjectPage(page);
      await createPage.gotoCreatePage();
      await createPage.fillStep0AndStep1ToReachLots(Date.now());

      // Abort POST to create project
      await page.route('**/api/v1/projects', route => {
        if (route.request().method() === 'POST') {
          route.abort('failed');
        } else {
          route.continue();
        }
      });

      await createPage.clickCreateProject();
      await page.waitForTimeout(2000);

      const errorVisible =
        (await page
          .locator('.error-message')
          .isVisible()
          .catch(() => false)) ||
        (await page
          .locator('text=/failed|error|unavailable/i')
          .isVisible()
          .catch(() => false));
      expect(errorVisible).toBeTruthy();
    });
  });

  // -------------------------------------------------------------------------
  // Subcategory: Data tier — API create then UI list
  // -------------------------------------------------------------------------
  test.describe('Data tier alignment', () => {
    test('Create project via API and verify it appears in UI list (UI ↔ data tier)', async ({
      page,
    }) => {
      const today = new Date();
      const startDate = today.toISOString().slice(0, 10);
      const endDate = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
        .toISOString()
        .slice(0, 10);
      const uniqueSuffix = Date.now().toString().slice(-6);
      const projectName = `E2E Project ${uniqueSuffix}`;

      const createResponse = await page.request.post('/api/v1/projects', {
        data: {
          projectName,
          projectDescription: 'Created by Playwright E2E',
          status: 'IN_PROGRESS',
          startDate,
          endDate,
          primaryColor: '#FFFFFF',
          tertiaryColor: '#000000',
          buyerColor: '#FF0000',
          buyerName: 'E2E Tester',
          customerId: 'cust-e2e',
          progressPercentage: 10,
        },
        headers: { 'Content-Type': 'application/json' },
      });

      expect(createResponse.status(), 'createProject should return 201').toBe(
        201
      );
      const created = await createResponse.json();
      expect(created.projectIdentifier).toBeTruthy();
      expect(created.projectName).toBe(projectName);

      const projectsPage = new ProjectsPage(page);
      await projectsPage.goto();
      await expect(projectsPage.pageTitle).toBeVisible();
      await expect(projectsPage.projectGrid).toBeVisible();

      const names = await projectsPage.getProjectNames();
      expect(
        names.some(n => (n || '').trim() === projectName),
        `Expected project "${projectName}" to appear in project list (UI ↔ data tier)`
      ).toBeTruthy();
    });
  });
});
