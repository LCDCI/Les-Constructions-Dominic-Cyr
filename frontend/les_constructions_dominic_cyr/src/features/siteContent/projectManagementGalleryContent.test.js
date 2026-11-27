// eslint-disable-next-line import/named
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  createSiteContent,
  fetchSiteContent,
  updateSiteContent,
} from './api/siteContentApi';
import { saveProjectManagementGalleryImage } from './projectManagementGalleryContent';

vi.mock('./api/siteContentApi', () => ({
  createSiteContent: vi.fn(),
  fetchSiteContent: vi.fn(),
  updateSiteContent: vi.fn(),
}));

describe('saveProjectManagementGalleryImage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('updates the existing locale and creates the missing locale with the same image', async () => {
    fetchSiteContent.mockImplementation(language =>
      Promise.resolve(
        language === 'en'
          ? [
              {
                id: 21,
                pageKey: 'galleryImage.6',
                contentText: '',
                imageIdentifier: 'old-image',
              },
            ]
          : []
      )
    );
    updateSiteContent.mockResolvedValue({
      id: 21,
      language: 'en',
      imageIdentifier: 'new-image',
    });
    createSiteContent.mockResolvedValue({
      id: 22,
      language: 'fr',
      imageIdentifier: 'new-image',
    });

    const result = await saveProjectManagementGalleryImage(
      { pageKey: 'galleryImage.6', contentText: '', sortOrder: 6 },
      'new-image',
      'token'
    );

    expect(updateSiteContent).toHaveBeenCalledWith(
      21,
      '',
      'new-image',
      'token'
    );
    expect(createSiteContent).toHaveBeenCalledWith(
      {
        pageGroup: 'project-management',
        pageKey: 'galleryImage.6',
        language: 'fr',
        contentText: '',
        imageIdentifier: 'new-image',
        sortOrder: 6,
      },
      'token'
    );
    expect(result.items).toHaveLength(2);
    expect(result.items.map(savedItem => savedItem.language)).toEqual([
      'en',
      'fr',
    ]);
    expect(result.previousImageIdentifiers).toEqual(['old-image']);
  });

  it('updates both existing locale rows while preserving their text', async () => {
    fetchSiteContent.mockImplementation(language =>
      Promise.resolve([
        {
          id: language === 'en' ? 41 : 42,
          pageKey: 'galleryImage.1',
          contentText: `${language} caption`,
          imageIdentifier: `${language}-old`,
        },
      ])
    );
    updateSiteContent.mockImplementation((id, contentText, imageIdentifier) =>
      Promise.resolve({ id, contentText, imageIdentifier })
    );

    await saveProjectManagementGalleryImage(
      { pageKey: 'galleryImage.1' },
      'shared-image',
      'token'
    );

    expect(updateSiteContent).toHaveBeenNthCalledWith(
      1,
      41,
      'en caption',
      'shared-image',
      'token'
    );
    expect(updateSiteContent).toHaveBeenNthCalledWith(
      2,
      42,
      'fr caption',
      'shared-image',
      'token'
    );
    expect(createSiteContent).not.toHaveBeenCalled();
  });

  it('clears both locale references without creating a missing row', async () => {
    fetchSiteContent.mockImplementation(language =>
      Promise.resolve(
        language === 'en'
          ? [{ id: 31, pageKey: 'galleryImage.7', imageIdentifier: 'en-old' }]
          : []
      )
    );
    updateSiteContent.mockResolvedValue({
      id: 31,
      language: 'en',
      imageIdentifier: null,
    });

    const result = await saveProjectManagementGalleryImage(
      { pageKey: 'galleryImage.7' },
      null,
      'token'
    );

    expect(updateSiteContent).toHaveBeenCalledWith(31, '', null, 'token');
    expect(createSiteContent).not.toHaveBeenCalled();
    expect(result.items).toHaveLength(1);
    expect(result.previousImageIdentifiers).toEqual(['en-old']);
  });
});
