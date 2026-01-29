import { useState, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchUnreadCount } from '../api/notificationsApi';

/**
 * Hook to fetch and maintain unread notification count
 * Useful for navbar badge display
 */
export const useUnreadCount = (refreshInterval = 30000) => {
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const loadUnreadCount = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: import.meta.env.VITE_AUTH0_AUDIENCE,
        },
      });
      const count = await fetchUnreadCount(token);
      setUnreadCount(count);
    } catch (err) {
      console.error('Error loading unread count:', err);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  useEffect(() => {
    if (isAuthenticated) {
      loadUnreadCount();

      // Set up periodic refresh
      const interval = setInterval(() => {
        loadUnreadCount();
      }, refreshInterval);

      return () => clearInterval(interval);
    } else {
      setUnreadCount(0);
      setLoading(false);
    }
  }, [isAuthenticated, loadUnreadCount, refreshInterval]);

  return { unreadCount, loading, refresh: loadUnreadCount };
};
