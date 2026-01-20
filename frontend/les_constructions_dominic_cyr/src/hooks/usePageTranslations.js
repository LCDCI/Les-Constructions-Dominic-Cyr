import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { fetchPageTranslations } from '../utils/translationApi';

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
      console.warn('[usePageTranslations] Page name is required');
      return;
    }

    const loadPageTranslations = async () => {
      // Check if translations are already loaded for this namespace
      const namespace = pageName;
      const hasResources = i18nInstance.hasResourceBundle(
        currentLanguage,
        namespace
      );

      setIsLoading(true);
      try {
        // Always fetch fresh translations to ensure nav/footer are up-to-date
        const translations = await fetchPageTranslations(
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
          console.log(
            `[usePageTranslations] Loaded translations for ${pageName} (${currentLanguage})`
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
            console.log(
              `[usePageTranslations] Updated nav/footer in 'translation' namespace for ${currentLanguage}`
            );
          }
        } else {
          console.warn(
            `[usePageTranslations] No translations found for ${pageName} (${currentLanguage})`
          );
        }

        setIsInitialized(true);
      } catch (error) {
        console.error(
          `[usePageTranslations] Error loading translations for ${pageName}:`,
          error
        );
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
          const translations = await fetchPageTranslations(
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
              console.log(
                `[usePageTranslations] Updated nav/footer in 'translation' namespace for ${currentLanguage} (language change)`
              );
            }
          }
        } catch (error) {
          console.error(
            `[usePageTranslations] Error reloading translations:`,
            error
          );
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
