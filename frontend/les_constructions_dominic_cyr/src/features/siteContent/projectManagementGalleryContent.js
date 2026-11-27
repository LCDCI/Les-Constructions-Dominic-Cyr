import {
  createSiteContent,
  fetchSiteContent,
  updateSiteContent,
} from './api/siteContentApi';

const GALLERY_LANGUAGES = ['en', 'fr'];

export async function saveProjectManagementGalleryImage(
  item,
  imageIdentifier,
  token
) {
  if (!item.pageKey?.startsWith('galleryImage.')) {
    throw new Error('A project-management gallery image slot is required.');
  }

  const localizedContent = await Promise.all(
    GALLERY_LANGUAGES.map(async language => {
      const items = await fetchSiteContent(
        language,
        token,
        'project-management'
      );
      if (!Array.isArray(items)) {
        throw new Error(
          `Invalid project-management content response for ${language}.`
        );
      }
      return { language, items };
    })
  );

  const previousImageIdentifiers = [
    ...new Set(
      localizedContent
        .flatMap(({ items }) => items)
        .filter(content => content.pageKey === item.pageKey)
        .map(content => content.imageIdentifier)
        .filter(Boolean)
    ),
  ];

  const savedItems = await Promise.all(
    localizedContent.map(({ language, items }) => {
      const existing = items.find(content => content.pageKey === item.pageKey);
      if (existing) {
        return updateSiteContent(
          existing.id,
          existing.contentText ?? '',
          imageIdentifier,
          token
        );
      }
      if (!imageIdentifier) return null;

      return createSiteContent(
        {
          pageGroup: 'project-management',
          pageKey: item.pageKey,
          language,
          contentText: item.contentText ?? '',
          imageIdentifier,
          sortOrder: item.sortOrder ?? 0,
        },
        token
      );
    })
  );
  const savedItemsWithLanguage = savedItems.flatMap((savedItem, index) =>
    savedItem
      ? [{ ...savedItem, language: localizedContent[index].language }]
      : []
  );

  return {
    items: savedItemsWithLanguage,
    previousImageIdentifiers,
  };
}
