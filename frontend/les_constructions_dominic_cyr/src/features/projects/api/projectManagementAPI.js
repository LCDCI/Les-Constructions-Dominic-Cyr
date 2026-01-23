import api from '../../../client';

/**
 * Fetches project management page content from the backend API
 * @param {string} language - Language code (e.g., 'en', 'fr')
 * @returns {Promise<Object>} Content object with all page text
 */
export const fetchProjectManagementContent = async (language = 'en') => {
  try {
    const response = await api.get(`/project-management/content/${language}`);
    return response.data;
  } catch (error) {
    console.error(
      `[ProjectManagementAPI] Error fetching content (${language}):`,
      error
    );
    // Fallback to English if requested language fails
    if (language !== 'en') {
      try {
        const fallbackResponse = await api.get(
          '/project-management/content/en'
        );
        return fallbackResponse.data;
      } catch (fallbackError) {
        console.error(
          '[ProjectManagementAPI] Error fetching fallback content:',
          fallbackError
        );
        throw fallbackError;
      }
    }
    throw error;
  }
};
