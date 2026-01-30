import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../../hooks/useBackendUser';
import { fetchLots, resolveProjectIdentifier } from '../../features/lots/api/lots';

export default function ProjectEntryRouter() {
  const { projectIdentifier } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading: authLoading, getAccessTokenSilently } = useAuth0();
  const { profile, role, loading: profileLoading } = useBackendUser();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const route = async () => {
      setLoading(true);
      try {
        const resolved = resolveProjectIdentifier(projectIdentifier);
        // Owners see project metadata
        if (role === 'OWNER') {
          navigate(`/projects/${resolved}/metadata`, { replace: true });
          return;
        }

        // For other roles, fetch lots and find assigned ones
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        const lots = await fetchLots({ projectIdentifier: resolved, token });

        const myId = profile?.userId || profile?.userIdentifier || null;

        const assigned = (lots || []).filter(l => {
          if (!l.assignedUsers || l.assignedUsers.length === 0) return false;
          return l.assignedUsers.some(u => u.userId === myId || u.userIdentifier === myId);
        });

        if (assigned.length === 1) {
          navigate(`/projects/${resolved}/lots/${assigned[0].lotId}/metadata`, { replace: true });
        } else if (assigned.length > 1) {
          navigate(`/projects/${resolved}/lots/select`, { replace: true });
        } else {
          // Not assigned to any lots - show lots list page (public) as fallback
          navigate(`/projects/${resolved}/lots`, { replace: true });
        }
      } catch (err) {
        // fallback to overview
        try {
          navigate(`/projects/${projectIdentifier}/overview`, { replace: true });
        } catch (e) {
          // noop
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (!authLoading && !profileLoading) route();

    return () => {
      cancelled = true;
    };
  }, [projectIdentifier, role, profile, authLoading, profileLoading, isAuthenticated, getAccessTokenSilently, navigate]);

  return <div className="page">Redirecting...</div>;
}
