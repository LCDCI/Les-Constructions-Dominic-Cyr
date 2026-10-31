import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../../hooks/useBackendUser';
import {
  fetchLots,
  resolveProjectIdentifier,
} from '../../features/lots/api/lots';

export default function ProjectEntryRouter() {
  const { projectIdentifier } = useParams();
  const navigate = useNavigate();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
  } = useAuth0();
  const { profile, loading: profileLoading } = useBackendUser();
  useEffect(() => {
    const route = async () => {
      try {
        const resolved = resolveProjectIdentifier(projectIdentifier);
        // For other roles, fetch lots and find assigned ones
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        const lots = await fetchLots({ projectIdentifier: resolved, token });

        const myId = profile?.userId || profile?.userIdentifier || null;

        const assigned = (lots || []).filter(l => {
          if (!l.assignedUsers || l.assignedUsers.length === 0) return false;
          return l.assignedUsers.some(
            u => u.userId === myId || u.userIdentifier === myId
          );
        });

        if (assigned.length === 1) {
          navigate(`/projects/${resolved}/overview`, { replace: true });
        } else if (assigned.length > 1) {
          navigate(`/projects/${resolved}/lots/select`, { replace: true });
        } else {
          // Not assigned to any lots - show lots list page (public) as fallback
          navigate(`/projects/${resolved}/overview`, { replace: true });
        }
      } catch (err) {
        navigate(`/projects/${projectIdentifier}/overview`, {
          replace: true,
        });
      }
    };

    if (!authLoading && !profileLoading) route();
  }, [
    projectIdentifier,
    profile,
    authLoading,
    profileLoading,
    isAuthenticated,
    getAccessTokenSilently,
    navigate,
  ]);

  return <div className="page">Redirecting...</div>;
}
