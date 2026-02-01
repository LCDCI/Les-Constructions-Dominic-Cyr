import { test, expect } from '@playwright/test';
import { LotMetadataPage } from './pages/lotMetadata.page';

test.describe('Lot Metadata E2E', () => {
  test('loads lot metadata and shows lot id in header and translated status', async ({
    page,
  }) => {
    // Mock project metadata
    await page.route('**/api/v1/projects/sample-project', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          projectName: 'Sample Project',
          primaryColor: '#123456',
          tertiaryColor: '#654321',
          buyerColor: '#00ff00',
          imageIdentifier: null,
        }),
      })
    );

    // Mock lot metadata
    await page.route('**/api/v1/projects/sample-project/lots/3', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 3,
          lotId: '6690973',
          lotNumber: '6690 973',
          lotStatus: 'AVAILABLE',
          civicAddress: '123 Test Ave',
          primaryColor: '#123456',
          buyerColor: '#00ff00',
          progressPercentage: 10,
        }),
      })
    );

    // Mock translations for lotMetadata namespace (English)
    await page.route('**/api/v1/translations/en/page/lotMetadata', route =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          language: 'en',
          translations: {
            lot: 'Lot',
            lotStatus: { available: 'Available-e2e' },
            loadingLot: 'Loading...',
          },
        }),
      })
    );

    const pageObj = new LotMetadataPage(page);
    await pageObj.goto('sample-project', '3');
    await pageObj.expectLoaded();

    const header = await pageObj.getHeaderText();
    expect(header).toMatch(/Lot\s*3/);

    const status = await pageObj.getStatusText();
    expect(status).toContain('Available-e2e');
  });
});
