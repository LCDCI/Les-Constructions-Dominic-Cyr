import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLotById } from '../../features/lots/api/lots';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/home.css';
import '../../styles/Project/ProjectMetadata.css';

const LotMetadata = () => {
  const { t } = usePageTranslations('lotMetadata');
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

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        // Fetch project metadata to set colors
        const projectData = await getProjectMetadata(projectId, token);
        document.documentElement.style.setProperty(
          '--project-primary',
          projectData.primaryColor
        );
        document.documentElement.style.setProperty(
          '--project-tertiary',
          projectData.tertiaryColor
        );
        document.documentElement.style.setProperty(
          '--project-buyer',
          projectData.buyerColor
        );

        const data = await fetchLotById({
          projectIdentifier: projectId,
          lotId,
          token,
        });
        if (!cancelled) setLot(data);
      } catch (err) {
        if (!cancelled) setError(err.message || t('errors.loadFailed', 'Failed to load lot'));
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

  if (loading) return <div className="page">{t('loading', 'Loading...')}</div>;
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
        style={{ backgroundColor: lot.primaryColor || '#ddd' }}
      >
        <div className="hero-content">
          <h1 className="project-title">
            {lot.lotNumber || t('lot', 'Lot') + ' ' + lot.lotId}
          </h1>
          <span
            className={`status-badge status-${(lot.lotStatus || '').toLowerCase()}`}
          >
            {lot.lotStatus}
          </span>
        </div>
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: lot.primaryColor }}>{t('lotOverview', 'Lot Overview')}</h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">{t('civicAddress', 'Civic Address')}</span>
              <span className="metadata-value">
                {lot.civicAddress || t('notSet', 'Not set')}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('areaSqft', 'Area (sqft)')}</span>
              <span className="metadata-value">
                {lot.dimensionsSquareFeet || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('areaSqm', 'Area (sqm)')}</span>
              <span className="metadata-value">
                {lot.dimensionsSquareMeters || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('price', 'Price')}</span>
              <span className="metadata-value">{formatPrice(lot.price)}</span>
            </div>
            {lot.progressPercentage !== null && (
              <div className="metadata-item full-width">
                <span className="metadata-label">{t('progress', 'Progress')}</span>
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

        {lot.assignedUsers &&
          (() => {
            const customer = lot.assignedUsers.find(u => u.role === 'CUSTOMER');
            return customer ? (
              <section className="metadata-section">
                <h2 style={{ color: lot.primaryColor }}>{t('buyerInformation', 'Buyer Information')}</h2>
                <div
                  className="buyer-info"
                  style={{ backgroundColor: lot.buyerColor || '#27ae60' }}
                >
                  <p className="buyer-name">
                    {customer.fullName ||
                      `${customer.firstName || ''} ${customer.lastName || ''}`.trim()}
                  </p>
                </div>
              </section>
            ) : null;
          })()}
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
          {t('backToLotSelection', 'Back to lot selection')}
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
          {t('backToProjects', 'Back to projects')}
        </button>
      </div>
    </div>
  );
};

export default LotMetadata;
