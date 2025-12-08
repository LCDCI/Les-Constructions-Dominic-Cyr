import Cookies from 'js-cookie';

const LANGUAGE_COOKIE_NAME = 'i18nextLng';
const COOKIE_EXPIRY_DAYS = 365; // 1 year

/**
 * Cookie utility functions for managing language preference
 */
export const cookieUtils = {
  /**
   * Get language from cookie
   * @returns {string|null} Language code or null if not set
   */
  getLanguage: () => {
    return Cookies.get(LANGUAGE_COOKIE_NAME) || null;
  },

  /**
   * Set language in cookie
   * @param {string} language - Language code (e.g., 'en', 'fr')
   */
  setLanguage: (language) => {
    Cookies.set(LANGUAGE_COOKIE_NAME, language, { 
      expires: COOKIE_EXPIRY_DAYS,
      sameSite: 'lax'
    });
  },

  /**
   * Remove language cookie
   */
  removeLanguage: () => {
    Cookies.remove(LANGUAGE_COOKIE_NAME);
  }
};

