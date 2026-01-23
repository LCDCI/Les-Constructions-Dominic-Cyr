import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { cookieUtils } from './cookieUtils';
import { fetchTranslations, fetchPageTranslations } from './translationApi';

const normalizeLang = lng => {
  if (!lng) return 'en';
  const lower = String(lng).toLowerCase();
  if (lower.startsWith('fr')) return 'fr';
  if (lower.startsWith('en')) return 'en';
  return 'en';
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
  fallbackLng: 'en',
  lng: initialLanguage,
  ns: ['translation', 'lots', 'projectoverview'],
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
    const [allTranslations, lotsTranslations] = await Promise.all([
      fetchTranslations(lang),
      fetchPageTranslations('lots', lang),
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
