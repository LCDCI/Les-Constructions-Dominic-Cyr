import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLotById } from '../../features/lots/api/lots';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import useBackendUser from '../../hooks/useBackendUser';
import '../../styles/Public_Facing/home.css';
import '../../styles/Project/ProjectMetadata.css';

const LotMetadata = () => {
  const { projectId, lotId } = useParams();
  const navigate = useNavigate();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
  } = useAuth0();
  const [lot, setLot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { profile, role } = useBackendUser();
  const [project, setProject] = useState(null);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        // Fetch project metadata to set colors and logo
        const projectData = await getProjectMetadata(projectId, token);
        setProject(projectData);
        document.documentElement.style.setProperty('--project-primary', projectData.primaryColor);
        document.documentElement.style.setProperty('--project-tertiary', projectData.tertiaryColor);
        document.documentElement.style.setProperty('--project-buyer', projectData.buyerColor);

        const data = await fetchLotById({
          projectIdentifier: projectId,
          lotId,
          token,
        });
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
      // Clean up project colors
      document.documentElement.style.removeProperty('--project-primary');
      document.documentElement.style.removeProperty('--project-tertiary');
      document.documentElement.style.removeProperty('--project-buyer');
    };
  }, [projectId, lotId, isAuthenticated, authLoading, getAccessTokenSilently]);

  const formatPrice = p => {
    if (p == null) return '—';
    const n = typeof p === 'number' ? p : Number(p);
    if (Number.isNaN(n)) return String(p);
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: 'CAD',
    }).format(n);
  };

  if (loading) return <div className="page">Loading...</div>;
  if (error) return <div className="page">{error}</div>;
  if (!lot) return null;

  return (
    <div
      className="project-metadata"
      style={{
        ['--project-primary']: lot.primaryColor || '#2c7be5',
        ['--project-buyer']: lot.buyerColor || '#27ae60',
      }}
    >
      <div
        className="metadata-hero"
        style={{ backgroundColor: project?.primaryColor || lot.primaryColor || '#ddd' }}
      >
        <div className="hero-content">
          <h1 className="project-title">
            {lot.lotNumber || `Lot ${lot.lotId}`}
          </h1>
          <span className={`status-badge status-${(lot.lotStatus || '').toLowerCase()}`}>{lot.lotStatus}</span>
        </div>
        {project?.imageIdentifier && (
          <div className="hero-image">
            <img
              src={`${import.meta.env.VITE_FILES_SERVICE_URL || (typeof window !== 'undefined' && window.location.hostname.includes('constructions-dominiccyr') ? 'https://files-service-app-xubs2.ondigitalocean.app' : `${window.location.origin}/files`)}/files/${project.imageIdentifier}`}
              alt={project.projectName}
            />
          </div>
        )}
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: lot.primaryColor }}>Lot Overview</h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">Civic Address</span>
              <span className="metadata-value">
                {lot.civicAddress || 'Not set'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Area (sqft)</span>
              <span className="metadata-value">
                {lot.dimensionsSquareFeet || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Area (sqm)</span>
              <span className="metadata-value">
                {lot.dimensionsSquareMeters || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Price</span>
              <span className="metadata-value">{formatPrice(lot.price)}</span>
            </div>
            {lot.progressPercentage !== null && (
              <div className="metadata-item full-width">
                <span className="metadata-label">Progress</span>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${lot.progressPercentage}%`,
                      backgroundColor: lot.primaryColor || '#27ae60',
                    }}
                  ></div>
                  <span className="progress-text">
                    {lot.progressPercentage}%
                  </span>
                </div>
              </div>
            )}
          </div>
          {lot.lotDescription && (
            <div className="project-description">
              <p>{lot.lotDescription}</p>
            </div>
          )}
        </section>

        {lot.assignedUsers && (
          <section className="metadata-section">
            <h2 style={{ color: lot.primaryColor }}>Assigned Users</h2>
            <div className="lots-grid">
              {lot.assignedUsers
                .filter(user => {
                  // OWNER sees all except themselves and other owners; others see all except OWNER
                  if (role === 'OWNER') {
                    return user.role !== 'OWNER' && user.userId !== profile?.userId;
                  }
                  return user.role !== 'OWNER';
                })
                .map(user => (
                  <div key={user.userId || user.id} className="lot-card" style={{ borderColor: lot.primaryColor }}>
                    <h3>{user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Unnamed User'}</h3>
                    <p className="lot-address">{user.email || 'No email'}</p>
                    <div className="lot-status-inline">
                      <span className="status-label">{user.role}</span>
                    </div>
                  </div>
                ))}
            </div>
          </section>
        )}
      </div>

      <div className="button-container">
        <button
          className="project-metadata-back"
          style={{ backgroundColor: lot.primaryColor, color: '#fff' }}
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
          onMouseOver={e => (e.currentTarget.style.filter = 'brightness(0.9)')}
          onMouseOut={e => (e.currentTarget.style.filter = '')}
        >
          Back to lot selection
        </button>

        <button
          className="project-metadata-schedule"
          style={{ backgroundColor: lot.primaryColor, color: '#fff' }}
          onClick={() => navigate('/projects')}
          onMouseOver={e => (e.currentTarget.style.filter = 'brightness(0.9)')}
          onMouseOut={e => (e.currentTarget.style.filter = '')}
        >
          Back to projects
        </button>
      </div>
    </div>
  );
};

export default LotMetadata;
