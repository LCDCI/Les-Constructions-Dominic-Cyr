/**
 * Page object for the Create New Project use case.
 * Use case: Create New Project.
 * Works for both standalone page (/projects/create) and modal on Projects page.
 * All locators target the create-project form (.create-project-form).
 */
export class CreateProjectPage {
  constructor(page) {
    this.page = page;

    // --- Use case step 0 & 1: Form container and title ---
    this.form = page.locator('.create-project-form');
    this.formTitle = page.locator('.create-project-header h1');

    // --- Step 0 (French) / Step 1 (English): Basic fields (same ids in both steps) ---
    this.projectNameInput = page.locator('#projectName');
    this.projectDescriptionInput = page.locator('#projectDescription');
    this.locationInput = page.locator('#location');
    this.statusSelect = page.locator('#status');
    this.startDateInput = page.locator('#startDate');
    this.endDateInput = page.locator('#endDate');

    // --- Buttons (EN + FR text) — use case steps: Cancel, Fill out English, Back, Continue to lots, Create Project ---
    this.cancelButton = page.locator(
      '.form-actions button.btn-cancel:has-text("Cancel"), .form-actions button.btn-cancel:has-text("Annuler")'
    ).first();
    this.fillOutEnglishButton = page.locator(
      'button:has-text("Fill out English"), button:has-text("Remplir l\'anglais")'
    );
    this.backButton = page.locator(
      'button:has-text("Back"), button:has-text("Retour")'
    ).first();
    this.continueToLotsButton = page.locator(
      'button:has-text("Continue to add lots"), button:has-text("Continuer vers les lots")'
    );
    this.createProjectSubmitButton = page.locator(
      '.create-project-form button[type="submit"], button:has-text("Create Project"), button:has-text("Créer le projet")'
    ).first();

    // --- Validation / errors ---
    this.validationAlert = page.locator('.validation-alert');
    this.errorMessages = page.locator('.create-project-form .error-message');

    // --- Step 2: Lots section (optional) ---
    this.lotsSection = page.locator('.form-section').filter({ has: page.locator('h2') });
  }

  // ---------------------------------------------------------------------------
  // Navigation — use case: open create form (standalone or modal)
  // ---------------------------------------------------------------------------

  /** Navigate to standalone create project page. Use case step: Open Create Project form. */
  async gotoCreatePage() {
    await this.page.goto('/projects/create');
    await this.page.waitForLoadState('networkidle');
    await this.form.waitFor({ state: 'visible', timeout: 10000 });
  }

  /** Get the form title text (for U1/U2: title in EN/FR). */
  async getFormTitle() {
    return await this.formTitle.textContent();
  }

  /** Whether the create form is visible. */
  async isFormVisible() {
    return await this.form.isVisible();
  }

  /** Wait for the create form to disappear (after cancel or successful submit). */
  async waitForFormToClose() {
    await this.form.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
  }

  // ---------------------------------------------------------------------------
  // Step 0 (French): Fill required fields and proceed
  // ---------------------------------------------------------------------------

  /** Use case step 0: Fill French basic info. */
  async fillStep0French(projectName, projectDescription, location, options = {}) {
    await this.projectNameInput.first().fill(projectName);
    await this.projectDescriptionInput.first().fill(projectDescription);
    await this.locationInput.first().fill(location);
    if (options.status) await this.statusSelect.first().selectOption(options.status);
    if (options.startDate) await this.startDateInput.first().fill(options.startDate);
    if (options.endDate) await this.endDateInput.first().fill(options.endDate);
  }

  /** Use case step 0: Click "Fill out English" to go to step 1. */
  async clickFillOutEnglish() {
    await this.fillOutEnglishButton.click();
  }

  // ---------------------------------------------------------------------------
  // Step 1 (English): Same fields, then continue to lots
  // ---------------------------------------------------------------------------

  /** Use case step 1: Fill English basic info (same fields, now in EN step). */
  async fillStep1English(projectName, projectDescription, location, options = {}) {
    await this.projectNameInput.first().fill(projectName);
    await this.projectDescriptionInput.first().fill(projectDescription);
    await this.locationInput.first().fill(location);
    if (options.status) await this.statusSelect.first().selectOption(options.status);
    if (options.startDate) await this.startDateInput.first().fill(options.startDate);
    if (options.endDate) await this.endDateInput.first().fill(options.endDate);
  }

  /** Use case step 1: Click "Continue to add lots" to go to step 2. */
  async clickContinueToLots() {
    await this.continueToLotsButton.click();
  }

  // ---------------------------------------------------------------------------
  // Step 2: Lots — submit or go back
  // ---------------------------------------------------------------------------

  /** Use case step 2: Submit "Create Project" (no lots added = valid). */
  async clickCreateProject() {
    await this.createProjectSubmitButton.click();
  }

  /** Use case: Cancel from current step. */
  async clickCancel() {
    await this.cancelButton.click();
  }

  /** Use case: Back from step 1 to 0 or step 2 to 1. */
  async clickBack() {
    await this.backButton.click();
  }

  // ---------------------------------------------------------------------------
  // Helpers: minimal valid data for success paths
  // ---------------------------------------------------------------------------

  /** Returns minimal valid data for step 0 (French). */
  getMinimalStep0Data(uniqueSuffix = Date.now()) {
    const today = new Date();
    const startDate = today.toISOString().slice(0, 10);
    const endDate = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
    return {
      projectName: `E2E Project ${uniqueSuffix}`,
      projectDescription: `E2E description ${uniqueSuffix}`,
      location: `E2E Location ${uniqueSuffix}`,
      status: 'IN_PROGRESS',
      startDate,
      endDate,
    };
  }

  /** Full flow: fill step 0, go to step 1, fill step 1, go to step 2. No lots, no cover. */
  async fillStep0AndStep1ToReachLots(uniqueSuffix = Date.now()) {
    const data = this.getMinimalStep0Data(uniqueSuffix);
    await this.fillStep0French(data.projectName, data.projectDescription, data.location, {
      status: data.status,
      startDate: data.startDate,
      endDate: data.endDate,
    });
    await this.clickFillOutEnglish();
    await this.page.waitForTimeout(300);
    await this.fillStep1English(data.projectName, data.projectDescription, data.location, {
      status: data.status,
      startDate: data.startDate,
      endDate: data.endDate,
    });
    await this.clickContinueToLots();
    await this.page.waitForTimeout(300);
    return data;
  }

  /** Check if we are on step 2 (Lots) by presence of submit button. */
  async isOnStep2() {
    return await this.createProjectSubmitButton.isVisible();
  }

  /** Get visible validation/error text. */
  async getValidationOrErrorText() {
    const alert = await this.validationAlert.textContent().catch(() => null);
    if (alert?.trim()) return alert.trim();
    const errors = await this.errorMessages.allTextContents();
    return errors.filter(Boolean).join(' ').trim() || null;
  }

  /** Check if Leave form dialog is visible (modal path). */
  isLeaveFormDialogVisible() {
    return this.page.locator('text=Leave form?, text=If you exit now').first();
  }

  /** Click "Stay on form" in Leave form dialog. */
  async clickStayOnForm() {
    await this.page.locator('button:has-text("Stay on form")').click();
  }

  /** Click "Leave" in Leave form dialog. */
  async clickLeaveForm() {
    await this.page.getByRole('button', { name: /Leave|Quitter/ }).first().click();
  }
}
