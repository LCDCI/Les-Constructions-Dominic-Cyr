import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLotById } from '../../features/lots/api/lots';
import '../../styles/Project/ProjectMetadata.css';

const LotMetadata = () => {
  const { projectId, lotId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading: authLoading, getAccessTokenSilently } = useAuth0();
  const [lot, setLot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;
        const data = await fetchLotById({ projectIdentifier: projectId, lotId, token });
        if (!cancelled) setLot(data);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to load lot');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (!authLoading) load();
    return () => {
      cancelled = true;
    };
  }, [projectId, lotId, isAuthenticated, authLoading, getAccessTokenSilently]);

  const formatPrice = p => {
    if (p == null) return '—';
    const n = typeof p === 'number' ? p : Number(p);
    if (Number.isNaN(n)) return String(p);
    return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(n);
  };

  if (loading) return <div className="page">Loading...</div>;
  if (error) return <div className="page">{error}</div>;
  if (!lot) return null;

  return (
    <div
      className="project-metadata"
      style={{ ['--project-primary']: lot.primaryColor || '#2c7be5', ['--project-buyer']: lot.buyerColor || '#27ae60' }}
    >
      <div className="metadata-hero" style={{ backgroundColor: lot.primaryColor || '#ddd' }}>
        <div className="hero-content">
          <h1 className="project-title">{lot.lotNumber || `Lot ${lot.lotId}`}</h1>
          <span className={`status-badge status-${(lot.lotStatus || '').toLowerCase()}`}>{lot.lotStatus}</span>
        </div>
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: lot.primaryColor }}>Lot Overview</h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">Civic Address</span>
              <span className="metadata-value">{lot.civicAddress || 'Not set'}</span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Area (sqft)</span>
              <span className="metadata-value">{lot.dimensionsSquareFeet || '—'}</span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Area (sqm)</span>
              <span className="metadata-value">{lot.dimensionsSquareMeters || '—'}</span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Price</span>
              <span className="metadata-value">{formatPrice(lot.price)}</span>
            </div>
          </div>
          {lot.lotDescription && (
            <div className="project-description">
              <p>{lot.lotDescription}</p>
            </div>
          )}
        </section>

        {lot.assignedUsers && (
          <section className="metadata-section">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ color: lot.primaryColor, margin: 0 }}>Assigned Team</h2>
            </div>
            <div className="team-grid">
              {lot.assignedUsers.map(u => (
                <div key={u.userId || u.userIdentifier} className="team-member">
                  <h3>{u.fullName || `${u.firstName || ''} ${u.lastName || ''}`}</h3>
                  <p className="member-contact">{u.primaryEmail || u.email}</p>
                </div>
              ))}
            </div>
          </section>
        )}
      </div>

      <div className="button-container">
        <button
          className="btn-secondary project-metadata-back"
          onClick={() => {
            try {
              if (window.history.length > 1) {
                navigate(-1);
                return;
              }
            } catch (e) {
              // ignore
            }
            navigate(`/projects/${projectId}/lots/select`);
          }}
        >
          Back to lot selection
        </button>

        <button
          className="btn-primary project-metadata-schedule"
          onClick={() => {
            try {
              if (window.history.length > 1) {
                navigate(-1);
                return;
              }
            } catch (e) {
              // ignore
            }
            navigate('/projects');
          }}
        >
          Back to projects
        </button>
      </div>
    </div>
  );
};

export default LotMetadata;
