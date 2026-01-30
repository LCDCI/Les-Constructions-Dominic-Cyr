import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLots, resolveProjectIdentifier } from '../../features/lots/api/lots';
import useBackendUser from '../../hooks/useBackendUser';
import '../../styles/Project/ProjectMetadata.css';

export default function LotSelectPage() {
  const { projectIdentifier } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();
  const { profile } = useBackendUser();
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const resolved = resolveProjectIdentifier(projectIdentifier);
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;
        const all = await fetchLots({ projectIdentifier: resolved, token });
        const myId = profile?.userId || profile?.userIdentifier || null;
        const assigned = (all || []).filter(l =>
          l.assignedUsers?.some(u => u.userId === myId || u.userIdentifier === myId)
        );
        if (!cancelled) setLots(assigned);
      } catch (err) {
        // fallback: empty
        if (!cancelled) setLots([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [projectIdentifier, isAuthenticated, getAccessTokenSilently, profile]);

  if (loading) return <div className="page">Loading...</div>;

  const handleBack = () => {
    try {
      if (window.history.length > 1) {
        navigate(-1);
        return;
      }
    } catch (e) {
      // ignore
    }
    navigate('/projects');
  };

  return (
    <div className="page project-metadata">
      <div className="metadata-hero">
        <div className="hero-content">
          <h1>Choose a lot</h1>
          <p>Select a lot to view its information.</p>
        </div>
      </div>

      <div className="metadata-content">
        {lots.length === 0 ? (
          <div className="no-results">
            <p>No lots assigned to you for this project.</p>
            <div style={{ marginTop: '1rem' }}>
              <button className="btn-secondary" onClick={() => navigate('/projects')}>Back to projects</button>
            </div>
          </div>
        ) : (
          <div className="lot-selector-list">
            {lots.map(l => (
              <div key={l.lotId} className="lot-card">
                <div>
                  <h3>{l.lotNumber || `Lot ${l.lotId}`}</h3>
                  <p>{l.civicAddress || '—'}</p>
                </div>
                <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                  <button className="btn-primary" onClick={() => navigate(`/projects/${projectIdentifier}/lots/${l.lotId}/metadata`)}>View lot information</button>
                  <button className="btn-secondary" onClick={handleBack}>Back to projects</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
