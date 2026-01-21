import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { cookieUtils } from './cookieUtils';
import { fetchTranslations } from './translationApi';

// Get language from cookie or default to 'en'
const getInitialLanguage = () => {
  return cookieUtils.getLanguage() || 'en';
};

// Initialize i18n synchronously first with empty resources
const initialLanguage = getInitialLanguage();

i18n.use(initReactI18next).init({
  supportedLngs: ['en', 'fr'],

  // Default language - English

  fallbackLng: 'en',
  lng: initialLanguage,

  // Namespace configuration
  // Namespaces will be loaded dynamically from the backend
  ns: ['translation'],
  defaultNS: 'translation',

  // Allow loading namespaces dynamically
  load: 'languageOnly',

  // Start with empty resources - will be loaded asynchronously
  resources: {
    en: {
      translation: {},
    },
    fr: {
      translation: {},
    },
  },

  // React i18next options
  react: {
    useSuspense: false,
  },

  // Interpolation options
  interpolation: {
    escapeValue: false, // React already escapes values
  },
});

// Load translations from backend asynchronously and add them
// Supports multiple namespaces (home, projects, nav, footer, etc.)
const loadTranslations = async (language = null) => {
  try {
    const languagesToLoad = language ? [language] : ['en', 'fr'];

    for (const lang of languagesToLoad) {
      const allTranslations = await fetchTranslations(lang);

      if (allTranslations && Object.keys(allTranslations).length > 0) {
        console.log(
          `[i18n] Loaded ${lang} translations with namespaces:`,
          Object.keys(allTranslations)
        );

        // Add each namespace separately
        for (const [namespace, translations] of Object.entries(
          allTranslations
        )) {
          if (translations && typeof translations === 'object') {
            // Add translations as a separate namespace
            i18n.addResourceBundle(lang, namespace, translations, true, true);
            console.log(`[i18n] Added namespace '${namespace}' for ${lang}`);

            // Add nav and footer to 'translation' namespace for any namespace that has them
            // so they're globally available (for AppNavBar, AppFooter, etc.)
            // This ensures navbar and footer work on all pages (home, notfound, servererror, etc.)
            const globalTranslations = {};
            if (translations.nav) {
              globalTranslations.nav = translations.nav;
            }
            if (translations.footer) {
              globalTranslations.footer = translations.footer;
            }
            if (Object.keys(globalTranslations).length > 0) {
              i18n.addResourceBundle(
                lang,
                'translation',
                globalTranslations,
                true,
                true
              );
              console.log(
                `[i18n] Added nav/footer to 'translation' namespace for ${lang} (from ${namespace})`
              );
            }
          }
        }

        // For backward compatibility: merge 'messages' namespace content into 'translation' namespace
        // This allows existing code using t('app.title') to continue working
        // The 'messages' namespace contains the global translations (app, common, nav, etc.)
        if (
          allTranslations.messages &&
          typeof allTranslations.messages === 'object'
        ) {
          i18n.addResourceBundle(
            lang,
            'translation',
            allTranslations.messages,
            true,
            true
          );
          console.log(
            `[i18n] Added 'messages' content to 'translation' namespace for ${lang} (backward compatibility)`
          );
        }

        // Emit event to trigger re-render
        i18n.emit('loaded', { lng: lang });
      } else {
        console.warn(`[i18n] No translations loaded for ${lang}`);
      }
    }
  } catch (error) {
    console.error('[i18n] Failed to load translations from backend:', error);
  }
};

// Load all translations in the background
loadTranslations();

// Reload translations when language changes
const originalChangeLanguage = i18n.changeLanguage.bind(i18n);
i18n.changeLanguage = async (lng, callback) => {
  cookieUtils.setLanguage(lng);

  // Load translations for the new language if not already loaded
  const hasResources = i18n.hasResourceBundle(lng, 'translation');
  if (
    !hasResources ||
    Object.keys(i18n.getResourceBundle(lng, 'translation') || {}).length === 0
  ) {
    await loadTranslations(lng);
  }

  return originalChangeLanguage(lng, callback);
};

export default i18n;
