import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLotById } from '../../features/lots/api/lots';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import useBackendUser from '../../hooks/useBackendUser';
import usePageTranslations from '../../hooks/usePageTranslations';
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
  const { t } = usePageTranslations('lotMetadata');

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

  const normalizeStatusKey = raw => {
    if (!raw) return '';
    const key = String(raw).toLowerCase().replace(/[^a-z0-9]/g, '');
    // Map variants to canonical translation keys
    if (key.includes('contract')) return 'contract';
    if (key === 'inprogress' || key === 'inprogress') return 'inprogress';
    if (key === 'available') return 'available';
    if (key === 'reserved') return 'reserved';
    if (key === 'sold') return 'sold';
    if (key === 'pending') return 'pending';
    return key;
  };

  if (loading) return <div className="page">{t('loadingLot') || 'Loading...'}</div>;
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
            {lot.id ? `${t('lot')} ${lot.id}` : (lot.lotNumber || `${t('lot')} ${lot.lotId}`)}
          </h1>
          {(() => {
            const statusKey = normalizeStatusKey(lot.lotStatus);
            const statusLabel = t(`lotStatus.${statusKey}`) || (lot.lotStatus || '');
            return (
              <span className={`status-badge status-${statusKey}`}>{statusLabel}</span>
            );
          })()}
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
          <h2 style={{ color: lot.primaryColor }}>{t('lotOverview') || 'Lot Overview'}</h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">{t('civicAddress') || 'Civic Address'}</span>
              <span className="metadata-value">
                {lot.civicAddress || t('notSet')}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('areaSqft') || 'Area (sqft)'}</span>
              <span className="metadata-value">
                {lot.dimensionsSquareFeet || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('areaSqm') || 'Area (sqm)'}</span>
              <span className="metadata-value">
                {lot.dimensionsSquareMeters || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('price') || 'Price'}</span>
              <span className="metadata-value">{formatPrice(lot.price)}</span>
            </div>
            {lot.progressPercentage !== null && (
              <div className="metadata-item full-width">
                <span className="metadata-label">{t('progress') || 'Progress'}</span>
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
            <h2 style={{ color: lot.primaryColor }}>{t('assignedUsers') || 'Assigned Users'}</h2>
            <div className="lots-grid">
              {lot.assignedUsers
                .filter(user => {
                  if (role === 'OWNER') {
                    return user.role !== 'OWNER' && user.userId !== profile?.userId;
                  }
                  return user.role !== 'OWNER';
                })
                .map(user => (
                  <div key={user.userId || user.id} className="lot-card" style={{ borderColor: lot.primaryColor }}>
                    <h3>{user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim() || t('unnamedUser')}</h3>
                    <p className="lot-address">{user.email || t('noEmail')}</p>
                    <div className="lot-status-inline">
                      <span className="status-label">{t(`userRole.${(user.role || '').toLowerCase()}`) || user.role}</span>
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
          {t('backToLotSelection') || 'Back to lot selection'}
        </button>

        <button
          className="project-metadata-schedule"
          style={{ backgroundColor: lot.primaryColor, color: '#fff' }}
          onClick={() => navigate('/projects')}
          onMouseOver={e => (e.currentTarget.style.filter = 'brightness(0.9)')}
          onMouseOut={e => (e.currentTarget.style.filter = '')}
        >
          {t('backToProjects') || 'Back to projects'}
        </button>
      </div>
    </div>
  );
};

export default LotMetadata;
