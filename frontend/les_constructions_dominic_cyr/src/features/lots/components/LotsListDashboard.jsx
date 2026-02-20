import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { fetchLotsByUserId } from '../api/lots';
import { projectApi } from '../../projects/api/projectApi';
import { useAuth0 } from '@auth0/auth0-react';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './LotsListDashboard.css';
import '../../../styles/Public_Facing/residential-projects.css';

/**
 * LotsListDashboard - Displays all projects with user's assigned lots grouped by project
 */
const LotsListDashboard = ({ userId, isCustomer = false }) => {
  const [allProjects, setAllProjects] = useState([]);
  const [lotsByProject, setLotsByProject] = useState({});
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { getAccessTokenSilently } = useAuth0();
  const { t } = usePageTranslations('lotsListDashboard');

  useEffect(() => {
    loadData();
  }, [userId]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = await getAccessTokenSilently();

      const [userLots, projects] = await Promise.all([
        fetchLotsByUserId(userId, token),
        projectApi.getAllProjects({}, token),
      ]);

      // Group lots by project identifier
      const grouped = (userLots || []).reduce((acc, lot) => {
        const projectId = lot.projectIdentifier || 'unknown';
        if (!acc[projectId]) acc[projectId] = [];
        acc[projectId].push(lot);
        return acc;
      }, {});

      setLotsByProject(grouped);
      setAllProjects(projects || []);
    } catch (err) {
      console.error('Failed to load data:', err);
      setError(t('error', 'Failed to load lots'));
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="lots-list-dashboard" data-testid="lots-list">
        <section className="lld-hero projects-hero">
          <div className="projects-hero-content">
            <span className="section-kicker">{t('heroKicker', 'Documents')}</span>
            <h1 className="projects-title">{t('pageTitle', 'My Lot Documents')}</h1>
          </div>
        </section>
        <div className="lld-content">
          <div className="loading-state">{t('loading', 'Loading your lots...')}</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="lots-list-dashboard" data-testid="lots-list">
        <section className="lld-hero projects-hero">
          <div className="projects-hero-content">
            <span className="section-kicker">{t('heroKicker', 'Documents')}</span>
            <h1 className="projects-title">{t('pageTitle', 'My Lot Documents')}</h1>
          </div>
        </section>
        <div className="lld-content">
          <div className="error-state" data-testid="error-state">{error}</div>
        </div>
      </div>
    );
  }

  const filteredProjects = selectedProjectId
    ? allProjects.filter(p => p.projectIdentifier === selectedProjectId)
    : allProjects;

  return (
    <div className="lots-list-dashboard" data-testid="lots-list">
      {/* Hero banner — same style as /projects page */}
      <section className="lld-hero projects-hero">
        <div className="projects-hero-content">
          <span className="section-kicker">{t('heroKicker', 'Documents')}</span>
          <h1 className="projects-title">{t('pageTitle', 'My Lot Documents')}</h1>
        </div>
      </section>

      <div className="lld-content">
        {/* Project filter */}
        {allProjects.length > 1 && (
          <div className="lld-filter-bar">
            <select
              className="lld-project-select"
              value={selectedProjectId}
              onChange={e => setSelectedProjectId(e.target.value)}
            >
              <option value="">{t('allProjects', 'All Projects')}</option>
              {allProjects.map(p => (
                <option key={p.projectIdentifier} value={p.projectIdentifier}>
                  {p.projectName}
                </option>
              ))}
            </select>
          </div>
        )}

        {filteredProjects.map(project => {
          const lots = lotsByProject[project.projectIdentifier] || [];
          const projectColor = project.primaryColor || '#aab2a6';

          return (
            <div key={project.projectIdentifier} className="project-section">
              <div
                className="project-header"
                style={{ background: projectColor }}
              >
                <h2 className="project-title">{project.projectName}</h2>
              </div>

              {lots.length === 0 ? (
                <div className="project-no-lots">
                  <p>{t('noLotsForProject', 'No lots assigned for this project.')}</p>
                </div>
              ) : (
                <div className="lots-table">
                  <div className="lots-table-header">
                    <div className="lots-cell">{t('columns.identifier', 'Identifier')}</div>
                    <div className="lots-cell">{t('columns.lotNumber', 'Lot #')}</div>
                    <div className="lots-cell">{t('columns.civicAddress', 'Civic Address')}</div>
                    <div className="lots-cell">{t('columns.areaSquareFeet', 'Area (sqft)')}</div>
                    <div className="lots-cell">{t('columns.areaSquareMeters', 'Area (m²)')}</div>
                    <div className="lots-cell">{t('columns.price', 'Price')}</div>
                    <div className="lots-cell">{t('columns.status', 'Status')}</div>
                    <div className="lots-cell lots-cell-actions">{t('columns.actions', 'Actions')}</div>
                  </div>

                  {lots.map((lot, lotIndex) => (
                    <div
                      key={lot.lotId}
                      className="lots-table-row"
                      data-testid={`lot-card-${lot.lotId}`}
                    >
                      <div className="lots-cell">{lot.lotNumber || '—'}</div>
                      <div className="lots-cell">{`Lot ${lotIndex + 1}`}</div>
                      <div className="lots-cell">{lot.civicAddress || '—'}</div>
                      <div className="lots-cell">{lot.dimensionsSquareFeet || '—'}</div>
                      <div className="lots-cell">{lot.dimensionsSquareMeters || '—'}</div>
                      <div className="lots-cell">
                        {lot.price ? `$${lot.price.toLocaleString()}` : '—'}
                      </div>
                      <div className="lots-cell">
                        {lot.lotStatus ? (
                          <span className={`lot-status lot-status-${lot.lotStatus?.toLowerCase()}`}>
                            {lot.lotStatus}
                          </span>
                        ) : '—'}
                      </div>
                      <div className="lots-cell lots-cell-actions">
                        <Link
                          to={`/projects/${project.projectIdentifier}/lots/${lot.lotId}/documents`}
                          className="btn btn-primary lots-action-button"
                          data-testid={`lot-documents-link-${lot.lotId}`}
                        >
                          {t('viewDocuments', 'View Documents')}
                        </Link>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

LotsListDashboard.propTypes = {
  userId: PropTypes.string.isRequired,
  isCustomer: PropTypes.bool,
};

export default LotsListDashboard;
