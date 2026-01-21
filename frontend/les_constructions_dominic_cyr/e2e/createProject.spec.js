import { test, expect } from '@playwright/test';
import { ProjectsPage } from './pages/projects.page';

test.describe('Create Project - E2E', () => {
  test('should create a project via API and list it in the UI', async ({
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
      headers: {
        'Content-Type': 'application/json',
      },
    });

    expect(createResponse.status(), 'createProject should return 201').toBe(
      201
    );
    const created = await createResponse.json();
    expect(created.projectIdentifier).toBeTruthy();
    expect(created.projectName).toBe(projectName);
    expect(created.status).toBe('IN_PROGRESS');

    const projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
    await expect(projectsPage.pageTitle).toBeVisible();

    await expect(projectsPage.projectGrid).toBeVisible();

    const names = await projectsPage.getProjectNames();
    expect(
      names.some(n => (n || '').trim() === projectName),
      `Expected project "${projectName}" to appear in project list`
    ).toBeTruthy();
  });
});
