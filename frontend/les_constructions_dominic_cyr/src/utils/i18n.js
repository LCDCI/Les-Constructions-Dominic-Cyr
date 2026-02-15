import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { cookieUtils } from './cookieUtils';
import { fetchTranslations, fetchPageTranslations } from './translationApi';

const normalizeLang = lng => {
  if (!lng) return 'fr';
  const lower = String(lng).toLowerCase();
  if (lower.startsWith('fr')) return 'fr';
  if (lower.startsWith('en')) return 'en';
  return 'fr';
};

const getInitialLanguage = () => {
  const fromCookie = cookieUtils.getLanguage();
  const normalized = normalizeLang(fromCookie);
  if (fromCookie && fromCookie !== normalized) {
    cookieUtils.setLanguage(normalized);
  }
  return normalized;
};

const initialLanguage = getInitialLanguage();

i18n.use(initReactI18next).init({
  supportedLngs: ['en', 'fr'],
  fallbackLng: 'fr',
  lng: initialLanguage,
  ns: ['translation', 'lots', 'projectoverview', 'livingenvironment'],
  ns: [
    'translation',
    'lots',
    'projectoverview',
    'livingenvironment',
    'lotMetadata',
    'projectMetadata',
    'quotes',
  ],
  defaultNS: 'translation',
  load: 'languageOnly',
  resources: {
    en: { translation: {} },
    fr: { translation: {} },
  },
  react: {
    useSuspense: false,
    bindI18n: 'languageChanged loaded',
  },
  interpolation: {
    escapeValue: false,
  },
});

const loadTranslations = async (language = null) => {
  try {
    const lang = normalizeLang(language || getInitialLanguage());
    const [
      allTranslations,
      lotsTranslations,
      livingEnvironmentTranslations,
      homeTranslations,
      navbarTranslations,
      lotMetadataTranslations,
      projectMetadataTranslations,
      quotesTranslations,
    ] = await Promise.all([
      fetchTranslations(lang),
      fetchPageTranslations('lots', lang),
      fetchPageTranslations('livingenvironment', lang),
      fetchPageTranslations('home', lang),
      fetchPageTranslations('navbar', lang),
      fetchPageTranslations('lotMetadata', lang),
      fetchPageTranslations('projectMetadata', lang),
      fetchPageTranslations('quotes', lang),
    ]);

    // Add general translations
    if (allTranslations) {
      Object.entries(allTranslations).forEach(([ns, bundle]) => {
        i18n.addResourceBundle(lang, ns, bundle, true, true);
      });
    }

    // Explicitly add/overwrite the 'lots' namespace
    if (lotsTranslations) {
      i18n.addResourceBundle(lang, 'lots', lotsTranslations, true, true);
    }

    // Explicitly add/overwrite the 'livingenvironment' namespace
    if (livingEnvironmentTranslations) {
      i18n.addResourceBundle(
        lang,
        'livingenvironment',
        livingEnvironmentTranslations,
        true,
        true
      );
    }

    // Always load home translations for nav/footer in global 'translation' namespace
    // This ensures navbar and footer are always available regardless of current page
    if (homeTranslations) {
      i18n.addResourceBundle(lang, 'home', homeTranslations, true, true);

      // Extract nav and footer from home translations and add to global 'translation' namespace
      const globalTranslations = {};
      if (homeTranslations.nav) {
        globalTranslations.nav = homeTranslations.nav;
      }
      if (homeTranslations.footer) {
        globalTranslations.footer = homeTranslations.footer;
      }
      if (Object.keys(globalTranslations).length > 0) {
        i18n.addResourceBundle(
          lang,
          'translation',
          globalTranslations,
          true,
          true
        );
      }
    }

    // Load navbar translations into global 'translation' namespace
    // This ensures role-specific navbar items are always available
    if (navbarTranslations) {
      i18n.addResourceBundle(lang, 'navbar', navbarTranslations, true, true);
      // Also add to global 'translation' namespace for easy access
      i18n.addResourceBundle(
        lang,
        'translation',
        { navbar: navbarTranslations },
        true,
        true
      );
    }

    // Explicitly add/overwrite the 'quotes' namespace
    if (quotesTranslations) {
      i18n.addResourceBundle(lang, 'quotes', quotesTranslations, true, true);
    }

    // Tell i18next we are done so the UI refreshes
    i18n.emit('loaded');
  } catch (error) {
    //fuck this
  }
};

loadTranslations();

const originalChangeLanguage = i18n.changeLanguage.bind(i18n);
i18n.changeLanguage = async (lng, callback) => {
  const lang = normalizeLang(lng);
  cookieUtils.setLanguage(lang);

  const hasResources = i18n.hasResourceBundle(lang, 'translation');
  const bundle = i18n.getResourceBundle(lang, 'translation');

  if (!hasResources || !bundle || Object.keys(bundle).length === 0) {
    await loadTranslations(lang);
  }

  return originalChangeLanguage(lang, callback);
};

export default i18n;
