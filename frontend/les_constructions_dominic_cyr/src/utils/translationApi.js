import api from '../client';

/**
 * Fetches all translations from the backend API
 * @param {string} language - Language code (e.g., 'en', 'fr')
 * @returns {Promise<Object>} Translations object organized by namespace
 */
export const fetchTranslations = async (language = 'en') => {
  try {
    const response = await api.get(`/translations/${language}`);

    const data = response.data;
    console.log(`[TranslationAPI] Fetched ${language} translations:`, data);

    const translations = data.translations || {};
    console.log(
      `[TranslationAPI] Extracted translations object:`,
      translations
    );
    console.log(
      `[TranslationAPI] Translation namespaces:`,
      Object.keys(translations)
    );

    return translations;
  } catch (error) {
    console.error(
      `[TranslationAPI] Error fetching ${language} translations:`,
      error
    );
    // Return empty object on error - i18next will use fallback
    return {};
  }
};

/**
 * Fetches translations for a specific page/namespace
 * @param {string} pageName - Page name (e.g., 'home', 'projects')
 * @param {string} language - Language code (e.g., 'en', 'fr')
 * @returns {Promise<Object>} Translations object for the page
 */
export const fetchPageTranslations = async (pageName, language = 'en') => {
  try {
    const response = await api.get(
      `/translations/${language}/page/${pageName}`
    );

    const data = response.data;
    console.log(
      `[TranslationAPI] Fetched ${pageName} translations (${language}):`,
      data
    );

    return data.translations || {};
  } catch (error) {
    if (error.response && error.response.status === 404) {
      console.warn(
        `[TranslationAPI] Page translations not found: ${pageName} (${language})`
      );
      return {};
    }
    console.error(
      `[TranslationAPI] Error fetching ${pageName} translations (${language}):`,
      error
    );
    // Return empty object on error - i18next will use fallback
    return {};
  }
};

/**
 * Fetches translations for a specific namespace
 * @param {string} namespace - Namespace (e.g., 'home', 'messages')
 * @param {string} language - Language code (e.g., 'en', 'fr')
 * @returns {Promise<Object>} Translations object for the namespace
 */
export const fetchNamespaceTranslations = async (
  namespace,
  language = 'en'
) => {
  try {
    const response = await api.get(
      `/translations/${language}/namespace/${namespace}`
    );

    const data = response.data;
    console.log(
      `[TranslationAPI] Fetched ${namespace} namespace translations (${language}):`,
      data
    );

    return data.translations || {};
  } catch (error) {
    if (error.response && error.response.status === 404) {
      console.warn(
        `[TranslationAPI] Namespace translations not found: ${namespace} (${language})`
      );
      return {};
    }
    console.error(
      `[TranslationAPI] Error fetching ${namespace} namespace translations (${language}):`,
      error
    );
    // Return empty object on error - i18next will use fallback
    return {};
  }
};
