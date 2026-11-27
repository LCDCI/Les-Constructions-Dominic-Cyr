// eslint-disable-next-line import/named
import { describe, expect, it } from 'vitest';
import { mergeProjectManagementImageOverrides } from './projectManagementGallery';

describe('mergeProjectManagementImageOverrides', () => {
  it('uses the most recently changed image and fills slots from either language', () => {
    const overrides = mergeProjectManagementImageOverrides(
      [
        {
          pageKey: 'galleryImage.1',
          imageIdentifier: 'english-main',
          updatedAt: '2026-01-01T10:00:00Z',
        },
        { pageKey: 'galleryImage.6', imageIdentifier: 'english-second' },
        { pageKey: 'galleryItem.1.caption', imageIdentifier: 'not-an-image' },
        { pageKey: 'galleryImage.8', imageIdentifier: '' },
      ],
      [
        {
          pageKey: 'galleryImage.1',
          imageIdentifier: 'french-main',
          updatedAt: '2026-01-02T10:00:00Z',
        },
        { pageKey: 'galleryImage.7', imageIdentifier: 'french-third' },
      ]
    );

    expect(overrides).toEqual({
      'galleryImage.1': 'french-main',
      'galleryImage.6': 'english-second',
      'galleryImage.7': 'french-third',
    });
  });
});
