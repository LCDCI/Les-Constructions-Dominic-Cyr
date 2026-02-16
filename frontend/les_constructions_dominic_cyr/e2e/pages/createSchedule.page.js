/**
 * Page object for the Owner Creates Project Schedule use case.
 *
 * Based on real components:
 *   - ProjectSchedulePage.jsx  → route /projects/:projectId/schedule
 *   - ScheduleFormModal.jsx    → .create-schedule-modal
 *   - TaskModal.jsx            → .tasks-modal
 *
 * All locators match the actual JSX class names & element structure.
 *
 * Auth strategy:
 *   Auth0 SPA SDK v2 with cacheLocation:"localstorage" and useRefreshTokens.
 *   We seed localStorage with three cache entries (token, user, manifest)
 *   and set the `auth0.<clientId>.is.authenticated` cookie so that
 *   checkSession() → getTokenSilently() hits the cache and returns
 *   immediately, making isAuthenticated === true.
 */
export class CreateSchedulePage {
  constructor(page) {
    this.page = page;

    // --- ProjectSchedulePage layout ---
    this.container = page.locator('.project-schedule-page');
    this.loadingSpinner = page.locator('.schedule-loading');
    this.errorBlock = page.locator('.schedule-error');
    this.newWorkButton = page.locator('.schedule-actions button.primary');
    this.scheduleCards = page.locator('.schedule-card');
    this.emptyState = page.locator('.empty');

    // --- ScheduleFormModal (create) ---
    this.createModal = page.locator('.create-schedule-modal');
    this.descriptionInput = page.locator(
      '.create-schedule-form input[type="text"]'
    );
    this.lotSelect = page.locator('.create-schedule-form select');
    this.startDateInput = page
      .locator('.create-schedule-form input[type="date"]')
      .first();
    this.endDateInput = page
      .locator('.create-schedule-form input[type="date"]')
      .last();
    this.saveWorkButton = page.locator(
      '.create-schedule-form button.modal-primary'
    );
    this.cancelButton = page.locator(
      '.create-schedule-form button.modal-secondary'
    );
    this.formError = page.locator('.create-schedule-modal .form-error');
    this.closeModalX = page.locator(
      '.create-schedule-modal button.modal-close'
    );

    // --- TaskModal (opens after successful create) ---
    this.taskModal = page.locator('.tasks-modal');
    this.taskRows = page.locator('.tasks-modal .task-row');
    this.saveTasksButton = page.locator('.tasks-modal button.modal-primary');
    this.closeTaskModalX = page.locator('.tasks-modal button.modal-close');
    this.taskFormError = page.locator('.tasks-modal .form-error');
  }

  /* ------------------------------------------------------------------ */
  /*  Auth‑mocking (Auth0 SPA SDK localStorage + cookie)                 */
  /* ------------------------------------------------------------------ */

  /**
   * Populate localStorage with Auth0 token/user cache entries and set
   * the authentication cookie so ProtectedRoute lets the user through.
   *
   * Must be called **before** page.goto().
   */
  async mockAuth() {
    const CLIENT_ID = 'DEV_CLIENT_ID';
    const AUDIENCE = 'https://construction-api.local';
    const SCOPE = 'openid profile email';
    const USER_SUB = 'auth0|test-user-123';

    const user = {
      sub: USER_SUB,
      email: 'test@example.com',
      name: 'Test Owner',
      'https://construction-api.loca/roles': ['OWNER'],
    };

    const claims = {
      ...user,
      iss: 'https://dev-8ytrd4o4t5c5bw04.us.auth0.com/',
      aud: CLIENT_ID,
      exp: 9999999999,
      iat: Math.floor(Date.now() / 1000),
    };

    /* ---- 1. Token cache ---- */
    const tokenKey = `@@auth0spajs@@::${CLIENT_ID}::${AUDIENCE}::${SCOPE}`;
    const tokenVal = JSON.stringify({
      body: {
        client_id: CLIENT_ID,
        access_token: 'fake-access-token',
        token_type: 'Bearer',
        scope: SCOPE,
        expires_in: 86400,
        audience: AUDIENCE,
      },
      expiresAt: Math.floor(Date.now() / 1000) + 86400,
    });

    /* ---- 2. User / id‑token cache ---- */
    const userKey = `@@auth0spajs@@::${CLIENT_ID}::@@user@@`;
    const userVal = JSON.stringify({
      id_token: 'fake.jwt.token',
      decodedToken: { claims, user },
    });

    /* ---- 3. Key manifest ---- */
    const manifestKey = `@@auth0spajs@@::${CLIENT_ID}`;
    const manifestVal = JSON.stringify({ keys: [tokenKey] });

    // Seed localStorage before the app bundle executes
    await this.page.addInitScript(
      args => {
        localStorage.setItem(args.tokenKey, args.tokenVal);
        localStorage.setItem(args.userKey, args.userVal);
        localStorage.setItem(args.manifestKey, args.manifestVal);
      },
      { tokenKey, tokenVal, userKey, userVal, manifestKey, manifestVal }
    );

    // Auth cookie checked by checkSession() before it calls getTokenSilently()
    await this.page.context().addCookies([
      {
        name: `auth0.${CLIENT_ID}.is.authenticated`,
        value: 'true',
        domain: 'localhost',
        path: '/',
      },
    ]);

    // Safety‑net: intercept any calls to the real Auth0 domain
    await this.page.route(
      'https://dev-8ytrd4o4t5c5bw04.us.auth0.com/**',
      async route => {
        if (route.request().url().includes('/oauth/token')) {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              access_token: 'fake-access-token',
              id_token: 'fake.jwt.token',
              scope: SCOPE,
              expires_in: 86400,
              token_type: 'Bearer',
            }),
          });
        } else {
          // /authorize, /.well-known/*, etc.
          await route.abort('blockedbyclient');
        }
      }
    );
  }

  /* ------------------------------------------------------------------ */
  /*  Route‑mocking helpers                                              */
  /* ------------------------------------------------------------------ */

  /**
   * Intercept every API call the schedule page makes on load and during
   * create‑schedule / create‑task flows.
   *
   * Also calls mockAuth() so the Auth0 ProtectedRoute passes.
   *
   * Call **before** navigating to the page.
   */
  async mockRoutes(
    projectId,
    {
      schedules = [],
      lots = [],
      project = null,
      onPostSchedule = null,
      onPostTask = null,
    } = {}
  ) {
    // ---- Auth (must come first) ----
    await this.mockAuth();

    const projectData = project || {
      projectIdentifier: projectId,
      projectName: 'Test Project',
      contractorIds: [],
    };

    // Backend user profile → OWNER role (used by useBackendUser hook)
    await this.page.route('**/api/v1/users/auth0*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'user-001',
          auth0UserId: 'auth0|test-user-123',
          userRole: 'OWNER',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'Owner',
        }),
      });
    });

    // GET project
    await this.page.route(`**/api/v1/projects/${projectId}`, async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(projectData),
        });
      } else {
        await route.continue();
      }
    });

    // GET + POST schedules
    await this.page.route(
      `**/api/v1/projects/${projectId}/schedules`,
      async route => {
        const method = route.request().method();
        if (method === 'GET') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(schedules),
          });
        } else if (method === 'POST') {
          if (onPostSchedule) {
            await onPostSchedule(route);
          } else {
            const body = route.request().postDataJSON();
            await route.fulfill({
              status: 201,
              contentType: 'application/json',
              body: JSON.stringify({
                scheduleIdentifier: `SCH-${Date.now()}`,
                scheduleDescription: body.scheduleDescription,
                scheduleStartDate: body.scheduleStartDate,
                scheduleEndDate: body.scheduleEndDate,
                lotId: body.lotId,
                lotNumber: body.lotId,
                tasks: [],
              }),
            });
          }
        } else {
          await route.continue();
        }
      }
    );

    // LOTS (fetchLots uses native fetch → /api/v1/projects/{id}/lots)
    await this.page.route(
      `**/api/v1/projects/${projectId}/lots*`,
      async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(lots),
        });
      }
    );

    // fallback /lots (in case no project id in URL)
    await this.page.route('**/api/v1/lots*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(lots),
      });
    });

    // Contractors
    await this.page.route('**/api/v1/users/contractors*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });
    await this.page.route('**/api/v1/contractors*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    // Tasks
    await this.page.route('**/api/v1/tasks*', async route => {
      if (route.request().method() === 'POST') {
        if (onPostTask) {
          await onPostTask(route);
        } else {
          const body = route.request().postDataJSON();
          await route.fulfill({
            status: 201,
            contentType: 'application/json',
            body: JSON.stringify({ ...body, taskId: `TSK-${Date.now()}` }),
          });
        }
      } else {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify([]),
        });
      }
    });

    // Owner-specific task endpoints
    await this.page.route('**/api/v1/owners/tasks*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    // Schedule tasks
    await this.page.route('**/api/v1/schedules/*/tasks*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });

    // Translations (prevent proxy errors)
    await this.page.route('**/api/v1/translations/**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({}),
      });
    });

    // Users list (fetched for contractors dropdown)
    await this.page.route('**/api/v1/users', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
    });
  }

  /* ------------------------------------------------------------------ */
  /*  Navigation                                                         */
  /* ------------------------------------------------------------------ */

  async goto(projectId) {
    await this.page.goto(`/projects/${projectId}/schedule`);
    await Promise.race([
      this.container.waitFor({ state: 'visible', timeout: 15000 }),
      this.errorBlock.waitFor({ state: 'visible', timeout: 15000 }),
    ]).catch(() => {});
  }

  /* ------------------------------------------------------------------ */
  /*  Create‑schedule modal                                              */
  /* ------------------------------------------------------------------ */

  async openCreateModal() {
    await this.newWorkButton.click();
    await this.createModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async fillForm({ description, lot, startDate, endDate } = {}) {
    if (description !== undefined)
      await this.descriptionInput.fill(description);
    if (lot) await this.lotSelect.selectOption(lot);
    if (startDate) await this.startDateInput.fill(startDate);
    if (endDate) await this.endDateInput.fill(endDate);
  }

  async submitForm() {
    await this.saveWorkButton.click();
  }

  async cancelForm() {
    await this.cancelButton.click();
  }

  async closeCreate() {
    await this.closeModalX.click();
  }

  /* ------------------------------------------------------------------ */
  /*  Task modal                                                         */
  /* ------------------------------------------------------------------ */

  async waitForTaskModal() {
    await this.taskModal.waitFor({ state: 'visible', timeout: 5000 });
  }

  async closeTaskModal() {
    await this.closeTaskModalX.click();
    await this.taskModal
      .waitFor({ state: 'hidden', timeout: 5000 })
      .catch(() => {});
  }
}
