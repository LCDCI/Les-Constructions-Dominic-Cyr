export const mergeProjectManagementImageOverrides = (
  englishItems = [],
  frenchItems = []
) => {
  const overrides = new Map();

  [...englishItems, ...frenchItems].forEach(item => {
    if (!item.pageKey?.startsWith('galleryImage.') || !item.imageIdentifier) {
      return;
    }

    const updatedAt = Date.parse(item.updatedAt || item.createdAt || '');
    const timestamp = Number.isFinite(updatedAt)
      ? updatedAt
      : Number.NEGATIVE_INFINITY;
    const current = overrides.get(item.pageKey);

    if (!current || timestamp > current.timestamp) {
      overrides.set(item.pageKey, {
        imageIdentifier: item.imageIdentifier,
        timestamp,
      });
    }
  });

  return Object.fromEntries(
    [...overrides].map(([pageKey, { imageIdentifier }]) => [
      pageKey,
      imageIdentifier,
    ])
  );
};
