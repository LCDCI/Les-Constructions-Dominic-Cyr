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

  /**
   * Get all submitted (pending approval) quotes for owner.
   *
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of submitted quotes
   * @throws {Error} If fetch fails
   */
  getSubmittedQuotes: async token => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(`${API_BASE_URL}/quotes/admin/submitted`, {
        headers,
      });

      if (!response.ok) {
        throw new Error(
          `Failed to fetch submitted quotes (${response.status})`
        );
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
   * Get all quotes for owner (admin view).
   *
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of all quotes
   * @throws {Error} If fetch fails
   */
  getAllQuotes: async token => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(`${API_BASE_URL}/quotes/admin/all`, {
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

  /**
   * Get submitted quotes filtered by project.
   *
   * @param {string} projectIdentifier - Project identifier
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of submitted quotes for project
   * @throws {Error} If fetch fails
   */
  getSubmittedQuotesByProject: async (projectIdentifier, token) => {
    try {
      const headers = {};
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const response = await fetch(
        `${API_BASE_URL}/quotes/admin/submitted/project/${projectIdentifier}`,
        { headers }
      );

      if (!response.ok) {
        throw new Error(
          `Failed to fetch submitted quotes (${response.status})`
        );
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
   * Approve a quote.
   *
   * @param {string} quoteNumber - Quote number to approve
   * @param {string} token - Authorization token
   * @returns {Promise<Object>} Updated quote with APPROVED status
   * @throws {Error} If approval fails
   */
  approveQuote: async (quoteNumber, token) => {
    try {
      const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      };

      const response = await fetch(
        `${API_BASE_URL}/quotes/${quoteNumber}/approve`,
        {
          method: 'PUT',
          headers,
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Failed to approve quote (${response.status})`;

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
   * Reject a quote with a reason.
   *
   * @param {string} quoteNumber - Quote number to reject
   * @param {string} rejectionReason - Reason for rejection
   * @param {string} token - Authorization token
   * @returns {Promise<Object>} Updated quote with REJECTED status
   * @throws {Error} If rejection fails
   */
  rejectQuote: async (quoteNumber, rejectionReason, token) => {
    try {
      const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      };

      const response = await fetch(
        `${API_BASE_URL}/quotes/${quoteNumber}/reject`,
        {
          method: 'PUT',
          headers,
          body: JSON.stringify({
            action: 'REJECT',
            rejectionReason,
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Failed to reject quote (${response.status})`;

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
   * Get quotes pending customer approval.
   * Returns quotes that are OWNER_APPROVED for lots owned by the authenticated customer.
   *
   * @param {string} token - Authorization token
   * @returns {Promise<Array>} List of quotes pending customer approval
   * @throws {Error} If fetch fails
   */
  getCustomerPendingQuotes: async token => {
    try {
      const headers = {
        Authorization: `Bearer ${token}`,
      };

      const response = await fetch(`${API_BASE_URL}/quotes/customer/pending`, {
        headers,
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch pending quotes (${response.status})`);
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
   * Customer approves a quote.
   * Only customers who own the lot can approve the quote.
   * Quote must be in OWNER_APPROVED status.
   *
   * @param {string} quoteNumber - Quote number to approve
   * @param {string} token - Authorization token
   * @returns {Promise<Object>} Updated quote with CUSTOMER_APPROVED status
   * @throws {Error} If approval fails
   */
  customerApproveQuote: async (quoteNumber, token) => {
    try {
      const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      };

      const response = await fetch(
        `${API_BASE_URL}/quotes/${quoteNumber}/customer-approve`,
        {
          method: 'POST',
          headers,
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Failed to approve quote (${response.status})`;

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
};
