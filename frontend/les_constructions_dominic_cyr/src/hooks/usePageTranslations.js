import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { fetchPageTranslations } from '../utils/translationApi';
import { fetchPublicSiteContent } from '../features/siteContent/api/siteContentApi';

const PUBLIC_PAGE_GROUPS = {
  home: 'home',
  contact: 'contact',
  renovations: 'renovations',
  projectManagement: 'project-management',
  realizations: 'realizations',
  residentialProjects: 'residential-projects',
  houses: 'houses',
};

const mergeObjects = (target, source) => {
  Object.entries(source || {}).forEach(([key, value]) => {
    if (
      value &&
      typeof value === 'object' &&
      !Array.isArray(value) &&
      typeof target[key] === 'object'
    ) {
      mergeObjects(target[key], value);
    } else {
      target[key] = value;
    }
  });
  return target;
};

const setNestedValue = (target, path, value) => {
  const parts = path.split('.');
  let current = target;
  parts.forEach((part, index) => {
    if (index === parts.length - 1) {
      current[part] = value;
    } else {
      current[part] = current[part] || {};
      current = current[part];
    }
  });
};

const loadTranslationsForPage = async (pageName, language) => {
  const localTranslations = await fetchPageTranslations(pageName, language);
  const pageGroup = PUBLIC_PAGE_GROUPS[pageName];
  if (!pageGroup) return localTranslations;

  const siteItems = await fetchPublicSiteContent(language, pageGroup);
  const overrides = {};
  (siteItems || []).forEach(item => {
    try {
      if (item.contentText?.trim().startsWith('{')) {
        mergeObjects(overrides, JSON.parse(item.contentText));
      } else if (item.pageKey && item.contentText != null) {
        setNestedValue(overrides, item.pageKey, item.contentText);
      }
    } catch {
      // Preserve the local fallback when an editorial JSON value is malformed.
    }
  });
  return mergeObjects({ ...(localTranslations || {}) }, overrides);
};

/**
 * Custom hook for page-specific translations.
 * Automatically loads and manages translations for a specific page/namespace.
 *
 * @param {string} pageName - The page name (e.g., 'home', 'projects')
 * @returns {Object} Object containing the translation function and loading state
 *
 * @example
 * const { t, isLoading } = usePageTranslations('home');
 * return <h1>{t('title')}</h1>;
 */
export const usePageTranslations = pageName => {
  const { i18n: i18nInstance } = useTranslation();
  const [isLoading, setIsLoading] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const currentLanguage = i18nInstance.language || 'en';

  useEffect(() => {
    if (!pageName) {
      return;
    }

    const loadPageTranslations = async () => {
      const namespace = pageName;

      setIsLoading(true);
      try {
        // Always fetch fresh translations to ensure nav/footer are up-to-date
        const translations = await loadTranslationsForPage(
          pageName,
          currentLanguage
        );

        if (translations && Object.keys(translations).length > 0) {
          // Add/update translations as a new namespace
          i18nInstance.addResourceBundle(
            currentLanguage,
            namespace,
            translations,
            true,
            true
          );

          // Always update nav and footer in 'translation' namespace when page loads
          // This ensures navbar shows the correct translations for the current page
          const globalTranslations = {};
          if (translations.nav) {
            globalTranslations.nav = translations.nav;
          }
          if (translations.footer) {
            globalTranslations.footer = translations.footer;
          }
          if (Object.keys(globalTranslations).length > 0) {
            // Use merge: true, deep: true to properly overwrite existing nav/footer values
            i18nInstance.addResourceBundle(
              currentLanguage,
              'translation',
              globalTranslations,
              true,
              true
            );
            // Force a re-render by emitting a language change event
            // This ensures the navbar updates with the latest translations
            i18nInstance.emit('languageChanged', currentLanguage);
          }
        }

        setIsInitialized(true);
      } catch (error) {
        // Keep local fallback translations available when the content service is unavailable.
      } finally {
        setIsLoading(false);
      }
    };

    loadPageTranslations();
  }, [pageName, currentLanguage, i18nInstance]);

  // Reload translations when language changes
  useEffect(() => {
    if (isInitialized && pageName) {
      const loadForNewLanguage = async () => {
        setIsLoading(true);
        try {
          const translations = await loadTranslationsForPage(
            pageName,
            currentLanguage
          );
          if (translations && Object.keys(translations).length > 0) {
            i18nInstance.addResourceBundle(
              currentLanguage,
              pageName,
              translations,
              true,
              true
            );

            // Always update nav and footer in 'translation' namespace when language changes
            const globalTranslations = {};
            if (translations.nav) {
              globalTranslations.nav = translations.nav;
            }
            if (translations.footer) {
              globalTranslations.footer = translations.footer;
            }
            if (Object.keys(globalTranslations).length > 0) {
              i18nInstance.addResourceBundle(
                currentLanguage,
                'translation',
                globalTranslations,
                true,
                true
              );
              // Force a re-render to ensure navbar updates
              i18nInstance.emit('languageChanged', currentLanguage);
            }
          }
        } catch (error) {
          // Keep the last loaded translations when the refresh fails.
        } finally {
          setIsLoading(false);
        }
      };

      loadForNewLanguage();
    }
  }, [currentLanguage, pageName, isInitialized, i18nInstance]);

  // Create a translation function that uses the page namespace
  const t = (key, options) => {
    return i18nInstance.t(`${pageName}:${key}`, options);
  };

  return {
    t,
    isLoading,
    isInitialized,
    currentLanguage,
  };
};

export default usePageTranslations;
