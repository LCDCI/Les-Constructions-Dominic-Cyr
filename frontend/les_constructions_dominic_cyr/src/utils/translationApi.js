import api from '../client';

export const fetchTranslations = async (language = 'en') => {
  try {
    const response = await api.get(`/translations/${language}`);
    return response.data?.translations || {};
  } catch (error) {
    return {};
  }
};

export const fetchPageTranslations = async (pageName, language = 'en') => {
  try {
    const response = await api.get(
      `/translations/${language}/page/${pageName}`
    );
    const translations = response.data?.translations || {};

    if (translations[pageName]) {
      return translations[pageName];
    }

    return translations;
  } catch (error) {
    return {};
  }
};

export const fetchNamespaceTranslations = async (
  namespace,
  language = 'en'
) => {
  try {
    const response = await api.get(
      `/translations/${language}/namespace/${namespace}`
    );
    const translations = response.data?.translations || {};

    // Unwrap the namespace if nested
    if (translations[namespace]) {
      return translations[namespace];
    }

    return translations;
  } catch (error) {
    return {};
  }
};
