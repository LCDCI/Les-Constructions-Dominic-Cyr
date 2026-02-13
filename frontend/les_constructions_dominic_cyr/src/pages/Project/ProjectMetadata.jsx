import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import { fetchLots } from '../../features/lots/api/lots';
import useBackendUser from '../../hooks/useBackendUser';
import usePageTranslations from '../../hooks/usePageTranslations';
import { FiUsers } from 'react-icons/fi';
import '../../styles/Public_Facing/home.css';
import '../../styles/Project/ProjectMetadata.css';

const ProjectMetadata = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const { profile, role } = useBackendUser();
  const [metadata, setMetadata] = useState(null);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { t } = usePageTranslations('projectMetadata');

  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        // Wait for Auth0 to finish loading before deciding on token
        if (isLoading) return;

        setLoading(true);

        // Get token if authenticated, otherwise fetch without token
        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {
            console.warn('Could not get token, proceeding without auth');
          }
        }

        const data = await getProjectMetadata(projectId, token);
        setMetadata(data);

        // Fetch lots for this project (used to show lot list)
        try {
          const lotsData = await fetchLots({
            projectIdentifier: projectId,
            token,
          });
          const list = Array.isArray(lotsData) ? lotsData : [];
          setLots(list);
        } catch (e) {
          console.warn('Failed to load project lots:', e?.message || e);
          setLots([]);
        }

        document.documentElement.style.setProperty(
          '--project-primary',
          data.primaryColor
        );
        document.documentElement.style.setProperty(
          '--project-tertiary',
          data.tertiaryColor
        );
        document.documentElement.style.setProperty(
          '--project-buyer',
          data.buyerColor
        );
      } catch (err) {
        const message =
          err.response?.data?.message || 'Failed to load project metadata';
        setError(message);
        if (err.response?.status === 403) {
          navigate('/unauthorized', { replace: true });
        }
      } finally {
        setLoading(false);
      }
    };

    fetchMetadata();

    // Refresh data every 30 seconds
    const interval = setInterval(fetchMetadata, 30000);

    // Refresh when page becomes visible (tab focus)
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        fetchMetadata();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      clearInterval(interval);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      document.documentElement.style.removeProperty('--project-primary');
      document.documentElement.style.removeProperty('--project-tertiary');
      document.documentElement.style.removeProperty('--project-buyer');
    };
  }, [projectId, isAuthenticated, isLoading, getAccessTokenSilently, navigate]);

  if (loading) {
    return (
      <div className="metadata-loading">
        <div className="spinner"></div>
        <p>{t('loadingProject') || 'Loading project information...'}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="metadata-error">
        <h2>{t('accessDeniedTitle') || 'Access Denied'}</h2>
        <p>{error}</p>
      </div>
    );
  }

  if (!metadata) {
    return null;
  }

  const myId = profile?.userId || profile?.userIdentifier || null;

  const formatDate = dateString => {
    if (!dateString) return t('notSet') || 'Not set';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // Status badge removed for compact mobile view

  return (
    <div className="project-metadata">
      <div
        className="metadata-hero"
        style={{ backgroundColor: metadata.primaryColor }}
      >
        <div className="hero-content">
          <h1 className="project-title">{metadata.projectName}</h1>
        </div>
        {metadata.imageIdentifier && (
          <div className="hero-image">
            <img
              src={`${import.meta.env.VITE_FILES_SERVICE_URL || (typeof window !== 'undefined' && window.location.hostname.includes('constructions-dominiccyr') ? 'https://files-service-app-xubs2.ondigitalocean.app' : `${window.location.origin}/files`)}/files/${metadata.imageIdentifier}`}
              alt={metadata.projectName}
            />
          </div>
        )}
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: metadata.primaryColor }}>
            {t('projectOverview') || 'Project Overview'}
          </h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">
                {t('location') || 'Location'}
              </span>
              <span className="metadata-value">{metadata.location}</span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">
                {t('startDate') || 'Start Date'}
              </span>
              <span className="metadata-value">
                {formatDate(metadata.startDate)}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">
                {t('endDate') || 'End Date'}
              </span>
              <span className="metadata-value">
                {formatDate(metadata.endDate)}
              </span>
            </div>
            {metadata.completionDate && (
              <div className="metadata-item">
                <span className="metadata-label">
                  {t('completionDate') || 'Completion Date'}
                </span>
                <span className="metadata-value">
                  {formatDate(metadata.completionDate)}
                </span>
              </div>
            )}
            {metadata.progressPercentage !== null && (
              <div className="metadata-item full-width">
                <span className="metadata-label">
                  {t('progress') || 'Progress'}
                </span>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${metadata.progressPercentage}%`,
                      backgroundColor: metadata.buyerColor,
                    }}
                  ></div>
                  <span className="progress-text">
                    {metadata.progressPercentage}%
                  </span>
                </div>
              </div>
            )}
          </div>
          <div
            className="schedule-button-container"
            style={{ marginTop: '20px' }}
          >
            <a
              href={`/projects/${projectId}/schedule`}
              className="project-metadata-schedule"
            >
              {t('viewProjectSchedule') || 'View Project Schedule'}
            </a>
          </div>
        </section>

        {role === 'OWNER' && lots.length > 0 && (
          <section className="metadata-section">
            <h2 style={{ color: metadata.primaryColor }}>
              {t('projectLots') || 'Project Lots'}
            </h2>
            <div className="lots-grid">
              {[...lots]
                .sort((a, b) => {
                  const idA = String(a.id ?? a.lotId ?? '');
                  const idB = String(b.id ?? b.lotId ?? '');
                  return idA.localeCompare(idB, undefined, { numeric: true });
                })
                .map(lot => {
                  const lotId = lot.lotId ?? lot.id;
                  const displayId = lot.id ?? lot.lotId ?? '—';
                  return (
                  <div
                    key={lotId}
                    className="lot-card"
                    style={{ borderColor: metadata.primaryColor }}
                    onClick={() =>
                      navigate(
                        `/projects/${projectId}/lots/${lotId}/metadata`
                      )
                    }
                  >
                    <h3>{`${t('lot') || 'Lot'} ${displayId}`}</h3>
                    {lot.civicAddress && (
                      <p className="lot-address">{lot.civicAddress}</p>
                    )}
                    {lot.lotStatus && (
                      <div className="lot-status-inline">
                        <span
                          className={`status-dot status-${lot.lotStatus.toLowerCase()}`}
                          aria-hidden="true"
                        ></span>
                        <span className="status-label">
                          {lot.lotStatus.replace('_', ' ')}
                        </span>
                      </div>
                    )}
                  </div>
                  );
                })}
            </div>
          </section>
        )}
      </div>

      <div className="button-container">
        <a href={`/projects`} className="project-metadata-back">
          {t('backToProjects') || 'Back to projects'}
        </a>
      </div>
    </div>
  );
};

export default ProjectMetadata;
