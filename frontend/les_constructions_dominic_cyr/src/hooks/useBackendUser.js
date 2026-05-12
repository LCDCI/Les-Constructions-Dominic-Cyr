import { useAuth0 } from '@auth0/auth0-react';
import { useEffect, useState, useCallback } from 'react';
import { fetchUserByAuth0Id } from '../features/users/api/usersApi';

/**
 * Fetches the backend user profile (including role) using the Auth0 access token.
 */
export function useBackendUser() {
  const {
    user: auth0User,
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
  } = useAuth0();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadProfile = useCallback(async () => {
    if (!isAuthenticated || !auth0User) {
      setProfile(null);
      setLoading(false);
      return;
    }

    if (isLoading) {
      // Keep loading true while Auth0 is initializing
      setLoading(true);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const { getAuthAudience } = await import('../utils/authConfig');

      // Try to obtain an access token for the API audience, fall back to a default token
      let token = null;
      let attemptedAudience = getAuthAudience();
      try {
        token = await getAccessTokenSilently({
          authorizationParams: { audience: attemptedAudience },
        });
      } catch (audErr) {
        // Log and attempt fallback without audience to help debugging local setups
        console.warn(
          '[useBackendUser] token request with audience failed:',
          audErr?.message || audErr
        );
        try {
          token = await getAccessTokenSilently();
          attemptedAudience = '(none)';
        } catch (noAudErr) {
          console.error(
            '[useBackendUser] token request fallback failed:',
            noAudErr?.message || noAudErr
          );
          throw noAudErr;
        }
      }

      // Debug: log token audience claims to help diagnose 401s (will not log token body in production)
      try {
        const parts = String(token || '').split('.');
        if (parts.length >= 2) {
          const payload = JSON.parse(
            atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
          );
          console.info(
            '[useBackendUser] obtained token for audience(s):',
            payload.aud || payload.scope || '(no aud)'
          );
        } else {
          console.info(
            '[useBackendUser] obtained non-JWT token or empty token'
          );
        }
      } catch (decErr) {
        console.warn(
          '[useBackendUser] failed to decode token payload:',
          decErr?.message || decErr
        );
      }

      const data = await fetchUserByAuth0Id(auth0User.sub, token);
      setProfile(data);
    } catch (err) {
      console.error('[useBackendUser] Error fetching user:', err);
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [auth0User, getAccessTokenSilently, isAuthenticated, isLoading]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  return {
    profile,
    role: profile?.userRole,
    loading,
    error,
    reload: loadProfile,
  };
}

export default useBackendUser;
