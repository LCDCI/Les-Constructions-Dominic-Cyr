import api from '../../../client';

/**
 * Fetches all notifications for the current user
 * @param {string} token - Auth0 access token (optional)
 * @returns {Promise<Array>} Array of notification objects
 */
export const fetchNotifications = async (token = null) => {
  try {
    const config = {};
    if (token) {
      config.headers = { Authorization: `Bearer ${token}` };
    }
    const response = await api.get('/notifications', config);
    return response.data;
  } catch (error) {
    console.error('[NotificationsAPI] Error fetching notifications:', error);
    throw error;
  }
};

/**
 * Fetches unread notifications for the current user
 * @param {string} token - Auth0 access token (optional)
 * @returns {Promise<Array>} Array of unread notification objects
 */
export const fetchUnreadNotifications = async (token = null) => {
  try {
    const config = {};
    if (token) {
      config.headers = { Authorization: `Bearer ${token}` };
    }
    const response = await api.get('/notifications/unread', config);
    return response.data;
  } catch (error) {
    console.error('[NotificationsAPI] Error fetching unread notifications:', error);
    throw error;
  }
};

/**
 * Fetches the unread notification count for the current user
 * @param {string} token - Auth0 access token (optional)
 * @returns {Promise<number>} Unread notification count
 */
export const fetchUnreadCount = async (token = null) => {
  try {
    const config = {};
    if (token) {
      config.headers = { Authorization: `Bearer ${token}` };
    }
    const response = await api.get('/notifications/unread-count', config);
    return response.data.count || 0;
  } catch (error) {
    console.error('[NotificationsAPI] Error fetching unread count:', error);
    return 0;
  }
};

/**
 * Marks a notification as read
 * @param {string} notificationId - The notification ID
 * @param {string} token - Auth0 access token (optional)
 * @returns {Promise<void>}
 */
export const markNotificationAsRead = async (notificationId, token = null) => {
  try {
    const config = {};
    if (token) {
      config.headers = { Authorization: `Bearer ${token}` };
    }
    await api.put(`/notifications/${notificationId}/read`, {}, config);
  } catch (error) {
    console.error('[NotificationsAPI] Error marking notification as read:', error);
    throw error;
  }
};

/**
 * Marks all notifications as read for the current user
 * @param {string} token - Auth0 access token (optional)
 * @returns {Promise<void>}
 */
export const markAllNotificationsAsRead = async (token = null) => {
  try {
    const config = {};
    if (token) {
      config.headers = { Authorization: `Bearer ${token}` };
    }
    await api.put('/notifications/read-all', {}, config);
  } catch (error) {
    console.error('[NotificationsAPI] Error marking all notifications as read:', error);
    throw error;
  }
};
