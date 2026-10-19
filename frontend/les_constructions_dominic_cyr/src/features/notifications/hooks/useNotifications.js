/* eslint-disable no-console */
import { useState, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import {
  fetchNotifications,
  fetchUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
} from '../api/notificationsApi';

export const useNotifications = () => {
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadNotifications = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const { getAuthAudience } = await import('../../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const data = await fetchNotifications(token);
      setNotifications(data);
    } catch (err) {
      setError(err.message || 'Failed to load notifications');
      console.error('Error loading notifications:', err);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  const loadUnreadCount = useCallback(async () => {
    if (!isAuthenticated) {
      return;
    }

    try {
      const { getAuthAudience } = await import('../../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const count = await fetchUnreadCount(token);
      setUnreadCount(count);
    } catch (err) {
      console.error('Error loading unread count:', err);
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  const markAsRead = useCallback(
    async notificationId => {
      if (!isAuthenticated) {
        return;
      }

      try {
        const { getAuthAudience } = await import('../../../utils/authConfig');
        const token = await getAccessTokenSilently({
          authorizationParams: { audience: getAuthAudience() },
        });
        await markNotificationAsRead(notificationId, token);
        // Update local state
        setNotifications(prev =>
          prev.map(notif =>
            notif.notificationId === notificationId
              ? { ...notif, isRead: true }
              : notif
          )
        );
        // Update unread count
        setUnreadCount(prev => Math.max(0, prev - 1));
      } catch (err) {
        console.error('Error marking notification as read:', err);
        throw err;
      }
    },
    [isAuthenticated, getAccessTokenSilently]
  );

  const markAllAsRead = useCallback(async () => {
    if (!isAuthenticated) {
      return;
    }

    try {
      const { getAuthAudience } = await import('../../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      await markAllNotificationsAsRead(token);
      // Update local state
      setNotifications(prev => prev.map(notif => ({ ...notif, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Error marking all notifications as read:', err);
      throw err;
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  const removeNotification = useCallback(
    async notificationId => {
      const { getAuthAudience } = await import('../../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      await deleteNotification(notificationId, token);
      setNotifications(prev =>
        prev.filter(item => item.notificationId !== notificationId)
      );
      setUnreadCount(prev =>
        notifications.find(
          item => item.notificationId === notificationId && !item.isRead
        )
          ? Math.max(0, prev - 1)
          : prev
      );
    },
    [getAccessTokenSilently, notifications]
  );

  const removeAllNotifications = useCallback(async () => {
    const { getAuthAudience } = await import('../../../utils/authConfig');
    const token = await getAccessTokenSilently({
      authorizationParams: { audience: getAuthAudience() },
    });
    await deleteAllNotifications(token);
    setNotifications([]);
    setUnreadCount(0);
  }, [getAccessTokenSilently]);

  useEffect(() => {
    if (isAuthenticated) {
      loadNotifications();
      loadUnreadCount();
    }
  }, [isAuthenticated, loadNotifications, loadUnreadCount]);

  return {
    notifications,
    unreadCount,
    loading,
    error,
    refresh: loadNotifications,
    refreshUnreadCount: loadUnreadCount,
    markAsRead,
    markAllAsRead,
    removeNotification,
    removeAllNotifications,
  };
};
