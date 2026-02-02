import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import QuoteDisplay from '../features/quotes/components/QuoteDisplay';
import '../features/quotes/styles/QuotesPage.css';

/**
 * QuotesPage Component
 * Allows contractors and owners to view quotes for projects
 * - Contractors can view their own quotes
 * - Owners can view all quotes for all projects
 */
const QuotesPage = () => {
  const { t } = useTranslation();
  const { getAccessTokenSilently, user } = useAuth0();
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loadingProjects, setLoadingProjects] = useState(false);
  const [token, setToken] = useState(null);

  // Get user role from decoded token
  const [userRole, setUserRole] = useState(null);

  useEffect(() => {
    const initializePage = async () => {
      try {
        setLoading(true);
        setError(null);

        // Get access token and extract role
        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);

        // Decode JWT to get role
        const tokenParts = accessToken.split('.');
        if (tokenParts.length === 3) {
          const decoded = JSON.parse(atob(tokenParts[1]));
          const roles = decoded['https://les-constructions.com/roles'] || [];
          setUserRole(roles[0] || null);
        }

        // Fetch projects based on user role
        setLoadingProjects(true);
        const projectsResponse = await axios.get('/api/v1/projects', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (projectsResponse.data && Array.isArray(projectsResponse.data)) {
          setProjects(projectsResponse.data);
          if (projectsResponse.data.length > 0) {
            setSelectedProject(projectsResponse.data[0]);
          }
        }
      } catch (err) {
        console.error('Error initializing quotes page:', err);
        setError(
          err.response?.data?.message ||
            err.message ||
            t('common.errorOccurred') ||
            'An error occurred while loading projects'
        );
      } finally {
        setLoading(false);
        setLoadingProjects(false);
      }
    };

    initializePage();
  }, [getAccessTokenSilently, t]);

  const handleProjectChange = e => {
    const projectId = e.target.value;
    const project = projects.find(p => p.projectIdentifier === projectId);
    setSelectedProject(project);
  };

  if (loading) {
    return (
      <div className="quotes-page">
        <div className="quotes-container">
          <div className="loading-state">
            <div className="spinner"></div>
            <p>{t('common.loading') || 'Loading...'}</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="quotes-page">
        <div className="quotes-container">
          <div className="error-state">
            <div className="error-icon">⚠️</div>
            <h2>{t('common.error') || 'Error'}</h2>
            <p>{error}</p>
            <button
              className="btn btn-primary"
              onClick={() => window.location.reload()}
            >
              {t('common.tryAgain') || 'Try Again'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (projects.length === 0) {
    return (
      <div className="quotes-page">
        <div className="quotes-container">
          <div className="empty-state">
            <div className="empty-icon">📋</div>
            <h2>{t('quote.noProjects') || 'No Projects Found'}</h2>
            <p>
              {userRole === 'CONTRACTOR'
                ? t('quote.noProjectsContractor') ||
                  'You are not assigned to any projects yet.'
                : t('quote.noProjectsOwner') || 'No projects available.'}
            </p>
            {userRole === 'OWNER' && (
              <button
                className="btn btn-primary"
                onClick={() => navigate('/projects/create')}
              >
                {t('project.createNew') || 'Create Project'}
              </button>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="quotes-page">
      <div className="quotes-container">
        {/* Page Header */}
        <div className="quotes-header">
          <div className="header-content">
            <h1>{t('quote.viewQuotes') || 'Quotes'}</h1>
            <p className="header-subtitle">
              {userRole === 'CONTRACTOR'
                ? t('quote.subtitleContractor') || 'View and manage your quotes'
                : t('quote.subtitleOwner') ||
                  'View all quotes for your projects'}
            </p>
          </div>
        </div>

        {/* Project Selector */}
        <div className="project-selector-card">
          <label htmlFor="project-select" className="selector-label">
            {t('quote.selectProject') || 'Select Project'}
          </label>
          <select
            id="project-select"
            value={selectedProject?.projectIdentifier || ''}
            onChange={handleProjectChange}
            className="project-select"
          >
            <option value="">-- Choose a project --</option>
            {projects.map(project => (
              <option
                key={project.projectIdentifier}
                value={project.projectIdentifier}
              >
                {project.projectName || project.projectIdentifier}
              </option>
            ))}
          </select>
        </div>

        {/* Quote Display */}
        {selectedProject && (
          <div className="quote-display-wrapper">
            <QuoteDisplay
              projectIdentifier={selectedProject.projectIdentifier}
              token={token}
            />
          </div>
        )}

        {!selectedProject && (
          <div className="no-project-selected">
            <p>
              {t('quote.selectProjectToView') ||
                'Select a project to view its quotes'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default QuotesPage;
