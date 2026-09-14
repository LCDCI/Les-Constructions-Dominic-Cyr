import localTranslations from 'virtual:local-translations';

const getLocalTranslations = (language, pageName) => {
  const languageTranslations = localTranslations[language.toLowerCase()] || {};
  return pageName
    ? languageTranslations[pageName.toLowerCase()] || {}
    : languageTranslations;
};

export const fetchTranslations = async (language = 'en') => {
  return getLocalTranslations(language);
};

export const fetchPageTranslations = async (pageName, language = 'en') => {
  return getLocalTranslations(language, pageName);
};

export const fetchNamespaceTranslations = async (
  namespace,
  language = 'en'
) => {
  return getLocalTranslations(language, namespace);
};
