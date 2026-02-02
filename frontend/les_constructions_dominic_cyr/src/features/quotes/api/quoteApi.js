const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

/**
 * Quote API integration layer.
 * Handles all communication with backend quote endpoints.
 */
export const quoteApi = {
  /**
   * Create a new quote under a project.
   *
   * @param {Object} quoteData - Quote data with line items
   * @param {string} quoteData.projectIdentifier - Project identifier
   * @param {Array} quoteData.lineItems - Array of line items
   * @param {string} token - Authorization token
   * @returns {Promise<Object>} Created quote with system-generated quote number
   * @throws {Error} If creation fails
   */
  createQuote: async (quoteData, token) => {
    try {
      const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      };

      const response = await fetch(`${API_BASE_URL}/quotes`, {
        method: 'POST',
        headers,
        body: JSON.stringify(quoteData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Failed to create quote (${response.status})`;

        if (errorText) {
          try {
            const errorData = JSON.parse(errorText);
            errorMessage = errorData.message || errorData.error || errorText;
          } catch (e) {
            errorMessage = errorText;
          }
        }

        const error = new Error(errorMessage);
        error.status = response.status;
        throw error;
      }

      return response.json();
    } catch (error) {
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        throw new Error('Network error: Unable to connect to API');
      }
      throw error;
    }
  },

  /**
   * Get all quotes for a specific project.
   *
   * @param {string} projectIdentifier - Project identifier
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of quotes
   * @throws {Error} If fetch fails
   */
  getQuotesByProject: async (projectIdentifier, token) => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(
        `${API_BASE_URL}/quotes/project/${projectIdentifier}`,
        { headers }
      );

      if (!response.ok) {
        throw new Error(`Failed to fetch quotes (${response.status})`);
      }

      return response.json();
    } catch (error) {
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        throw new Error('Network error: Unable to connect to API');
      }
      throw error;
    }
  },

  /**
   * Get a specific quote by its quote number.
   *
   * @param {string} quoteNumber - Quote number (e.g., QT-0000001)
   * @param {string} token - Authorization token
   * @returns {Promise<Object>} Quote details
   * @throws {Error} If fetch fails or quote not found
   */
  getQuoteByNumber: async (quoteNumber, token) => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(`${API_BASE_URL}/quotes/${quoteNumber}`, {
        headers,
      });

      if (!response.ok) {
        if (response.status === 404) {
          throw new Error(`Quote not found: ${quoteNumber}`);
        }
        throw new Error(`Failed to fetch quote (${response.status})`);
      }

      return response.json();
    } catch (error) {
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        throw new Error('Network error: Unable to connect to API');
      }
      throw error;
    }
  },

  /**
   * Get all quotes created by the current contractor.
   *
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of contractor's quotes
   * @throws {Error} If fetch fails
   */
  getMyQuotes: async token => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(`${API_BASE_URL}/quotes/my-quotes`, {
        headers,
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch quotes (${response.status})`);
      }

      return response.json();
    } catch (error) {
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        throw new Error('Network error: Unable to connect to API');
      }
      throw error;
    }
  },
};
