import { useAuth0 } from '@auth0/auth0-react';
import { useEffect, useState, useCallback } from 'react';
import { fetchUserByAuth0Id } from '../features/users/api/usersApi';

/**
 * Fetches the backend user profile (including role) using the Auth0 access token.
 */
export function useBackendUser() {
  const { user: auth0User, isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadProfile = useCallback(async () => {
    if (!isAuthenticated || !auth0User || isLoading) {
      setProfile(null);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: import.meta.env.VITE_AUTH0_AUDIENCE },
      });
      const data = await fetchUserByAuth0Id(auth0User.sub, token);
      setProfile(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [auth0User, getAccessTokenSilently, isAuthenticated, isLoading]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  return { profile, role: profile?.userRole, loading, error, reload: loadProfile };
}

export default useBackendUser;
