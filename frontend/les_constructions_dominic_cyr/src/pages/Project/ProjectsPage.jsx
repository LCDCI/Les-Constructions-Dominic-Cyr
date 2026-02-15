import { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import '../../styles/Project/projects.css';
import '../../styles/Project/projects.mobile.css';
import '../../styles/Project/create-project.css';
import '../../styles/Project/edit-project.css';
import '../../styles/Modals/ConfirmationModal.css';
import '../../styles/Public_Facing/residential-projects.css';
import CreateProjectForm from '../../features/projects/components/CreateProjectForm';
import EditProjectForm from '../../features/projects/components/EditProjectForm';
import { fetchLots } from '../../features/lots/api/lots';
import useBackendUser from '../../hooks/useBackendUser';
import { canCreateProjects, canEditProjects } from '../../utils/permissions';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const ProjectsPage = () => {
  const { t } = usePageTranslations('projects');
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter] = useState('ACTIVE'); // ACTIVE | ARCHIVED | ALL
  const [isFilterMenuOpen, setIsFilterMenuOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [projectToEdit, setProjectToEdit] = useState(null);
  const [submitError, setSubmitError] = useState(null);
  const [showConfirmClose, setShowConfirmClose] = useState(false);
  const [isArchiveOpen, setIsArchiveOpen] = useState(false);
  const [projectToArchive, setProjectToArchive] = useState(null);
  const [isLotSelectionOpen, setIsLotSelectionOpen] = useState(false);
  const [projectForLotSelection, setProjectForLotSelection] = useState(null);
  const [userLotsForProject, setUserLotsForProject] = useState([]);

  const { role } = useBackendUser();
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  // const canCreate = canCreateProjects(role); // Removed unused variable
  const canEdit = canEditProjects(role);

  const filterMenuContainerRef = useRef(null);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : `${window.location.origin}/files`);
  // Use relative path to leverage Vite proxy
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1';

  const navigate = useNavigate();

  // Helper for error redirection
  const redirectToError = (status = 500) => {
    if (status === 404) {
      navigate('/404', { replace: true });
    } else {
      navigate('/error', { replace: true });
    }
  };

  useEffect(() => {
    fetchProjects();
  }, [isAuthenticated, statusFilter]);

  useEffect(() => {
    filterProjects();
  }, [searchTerm, projects]);

  useEffect(() => {
    const handleClickOutside = event => {
      if (
        isFilterMenuOpen &&
        filterMenuContainerRef.current &&
        !filterMenuContainerRef.current.contains(event.target)
      ) {
        setIsFilterMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isFilterMenuOpen]);

  const fetchProjects = async () => {
    try {
      setError(null);
      setLoading(true);

      const baseUrl = `${apiBaseUrl}/projects`;

      const headers = {};
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently({
            authorizationParams: {
              audience:
                import.meta.env.VITE_AUTH0_AUDIENCE ||
                'https://construction-api.loca',
            },
          });
          headers.Authorization = `Bearer ${token}`;
        } catch (tokenError) {
          // Could not get access token, proceeding without authentication
        }
      }

      if (statusFilter === 'ALL') {
        const [availableResp, archivedResp] = await Promise.all([
          fetch(baseUrl, { headers }),
          fetch(`${baseUrl}?status=ARCHIVED`, { headers }),
        ]);

        if (!availableResp.ok) {
          const errorText = await availableResp.text();
          throw new Error(
            `Failed to fetch available: ${availableResp.status} - ${errorText}`
          );
        }
        if (!archivedResp.ok) {
          const errorText = await archivedResp.text();
          throw new Error(
            `Failed to fetch archived: ${archivedResp.status} - ${errorText}`
          );
        }

        const availableAll = await availableResp.json();
        const archived = await archivedResp.json();
        const active = (availableAll || []).filter(
          p => p.status !== 'ARCHIVED'
        );

        // Merge, keeping order: active first, then archived; de-duplicate by identifier
        const seen = new Set();
        const merged = [];
        for (const p of [...(active || []), ...(archived || [])]) {
          const id = p.projectIdentifier;
          if (id && !seen.has(id)) {
            seen.add(id);
            merged.push(p);
          }
        }

        setProjects(merged);
        setFilteredProjects(merged);
      } else if (statusFilter === 'ARCHIVED') {
        const response = await fetch(`${baseUrl}?status=ARCHIVED`, { headers });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to fetch: ${response.status} - ${errorText}`);
        }
        const data = await response.json();
        setProjects(data || []);
        setFilteredProjects(data || []);
      } else {
        const response = await fetch(baseUrl, { headers });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to fetch: ${response.status} - ${errorText}`);
        }
        const data = await response.json();
        const active = (data || []).filter(p => p.status !== 'ARCHIVED');
        setProjects(active);
        setFilteredProjects(active);
      }

      setLoading(false);
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const filterProjects = () => {
    try {
      if (!searchTerm.trim()) {
        setFilteredProjects(projects);
        return;
      }

      const filtered = projects.filter(project => {
        const name = project?.projectName || '';
        return name.toLowerCase().includes(searchTerm.toLowerCase());
      });
      setFilteredProjects(filtered);
    } catch (err) {
      setFilteredProjects([]);
    }
  };

  const getImageUrl = imageIdentifier => {
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handleViewProject = async project => {
    // Owners always go to project metadata page
    if (role === 'OWNER') {
      navigate(`/projects/${project.projectIdentifier}/metadata`);
      return;
    }

    try {
      // Get user's lots for this project
      let token = null;
      if (isAuthenticated) {
        try {
          token = await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            },
          });
        } catch (tokenErr) {
          console.warn('Could not get token for lots fetch');
        }
      }

      const lotsData = await fetchLots({
        projectIdentifier: project.projectIdentifier,
        token,
      });

      // Filter lots to only those assigned to current user
      // If there are multiple lots, show selection modal
      if (lotsData && lotsData.length > 1) {
        setProjectForLotSelection(project);
        setUserLotsForProject(lotsData);
        setIsLotSelectionOpen(true);
      } else if (lotsData && lotsData.length === 1) {
        // Go directly to the lot's metadata page
        navigate(
          `/projects/${project.projectIdentifier}/lots/${lotsData[0].lotId}/metadata`
        );
      } else {
        navigate(`/projects/${project.projectIdentifier}/metadata`);
      }
    } catch (error) {
      navigate(`/projects/${project.projectIdentifier}/metadata`);
    }
  };

  const handleLotSelection = lot => {
    setIsLotSelectionOpen(false);
    setProjectForLotSelection(null);
    setUserLotsForProject([]);
    navigate(`/projects/${lot.projectIdentifier}/lots/${lot.lotId}/metadata`);
  };

  const closeLotSelectionModal = () => {
    setIsLotSelectionOpen(false);
    setProjectForLotSelection(null);
    setUserLotsForProject([]);
  };

  const handleEditProject = project => {
    setProjectToEdit(project);
    setIsEditOpen(true);
    setSubmitError(null);
  };

  const handleEditSuccess = () => {
    setIsEditOpen(false);
    setProjectToEdit(null);
    fetchProjects();
  };

  // Removed unused handleArchiveProject

  const handleEditCancel = () => {
    setIsEditOpen(false);
    setProjectToEdit(null);
    setSubmitError(null);
  };

  const openArchiveModal = project => {
    setProjectToArchive(project);
    setIsArchiveOpen(true);
  };

  const closeArchiveModal = () => {
    setIsArchiveOpen(false);
    setProjectToArchive(null);
  };

  const overlayStyle = {
    position: 'fixed',
    inset: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.45)',
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'center',
    padding: '3rem 1rem',
    zIndex: 1000,
    overflowY: 'auto',
  };

  const modalStyle = {
    width: '100%',
    maxWidth: '900px',
    background: '#fff',
    borderRadius: '8px',
    boxShadow: '0 10px 30px rgba(0, 0, 0, 0.2)',
    padding: '2rem',
  };

  const confirmOverlayStyle = {
    position: 'fixed',
    inset: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '1rem',
    zIndex: 1100,
  };

  const confirmBoxStyle = {
    width: '100%',
    maxWidth: '520px',
    background: '#fff',
    borderRadius: '8px',
    boxShadow: '0 10px 30px rgba(0, 0, 0, 0.25)',
    padding: '1.5rem',
  };

  return (
    <div className="admin-projects-page projects-page">
      <section className="projects-hero">
        <div className="projects-hero-content">
          <span className="section-kicker">{t('hero.kicker', 'Projects')}</span>
          <h1 className="projects-title">
            {role === 'OWNER'
              ? t('hero.titleOwner', 'Manage Projects')
              : t('hero.title', 'Our Projects')}
          </h1>
          <p className="projects-subtitle">
            {role === 'OWNER'
              ? t(
                  'hero.subtitleOwner',
                  'View, create, and manage your construction projects.'
                )
              : t(
                  'hero.subtitle',
                  'Explore our portfolio of residential projects showcasing quality construction and innovative design.'
                )}
          </p>
        </div>
      </section>
      <div className="projects-search-container">
        <div className="container">
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '1.5rem',
            }}
          >
            <div />
            {/* Restore Create Project button for users with permission */}
            {canCreateProjects(role) && (
              <button
                className="admin-create-project-button"
                onClick={() => setIsCreateOpen(true)}
              >
                {t('form.buttons.createProject', 'Create New Project')}
              </button>
            )}
          </div>
          <div className="search-box">
            <input
              type="text"
              className="search-input admin-search-input"
              placeholder={t(
                'search.placeholder',
                'Search projects by name...'
              )}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </div>
      <section className="portfolio-section">
        <div className="container">
          {loading && (
            <div className="projects-page loading-state">
              <div className="page-loader">
                <div className="loader-spinner"></div>
              </div>
            </div>
          )}
          {error && (
            <div className="admin-projects-error">
              <p>{error}</p>
              <button onClick={fetchProjects} className="admin-retry-button">
                Retry
              </button>
            </div>
          )}
          {!loading && !error && (
            <div className="portfolio-grid">
              {filteredProjects.length > 0 ? (
                filteredProjects.map(project => (
                  <div className="project-item" key={project.projectIdentifier}>
                    <div
                      className={`portfolio-card${project.status === 'ARCHIVED' ? ' project-card-archived' : ''} project-hover-card`}
                      data-animate
                    >
                      <img
                        src={getImageUrl(project.imageIdentifier)}
                        alt={project.projectName}
                        loading="lazy"
                        className="card-image-bg project-hover-image"
                      />
                      <div className="card-overlay project-hover-overlay" />
                      <div className="card-content project-hover-content">
                        <h2 className="card-title">{project.projectName}</h2>
                        <div className="admin-project-actions">
                          <button
                            onClick={() => handleViewProject(project)}
                            className="admin-project-button"
                          >
                            {t('buttons.view', 'View this project')}
                          </button>

                          {role === 'OWNER' && canEdit && (
                            <>
                              <button
                                onClick={() => handleEditProject(project)}
                                className="admin-project-button admin-edit-button"
                              >
                                {t('buttons.edit', 'Edit')}
                              </button>
                              <a
                                href={`/projects/${project.projectIdentifier}/manage-lots`}
                                className="admin-project-button admin-lots-button"
                              >
                                {t('buttons.lots', 'View Project Lots')}
                              </a>
                            </>
                          )}

                          {role === 'OWNER' &&
                            canEdit &&
                            project.status !== 'ARCHIVED' && (
                              <button
                                onClick={() => openArchiveModal(project)}
                                className="admin-project-button archive-button"
                              >
                                {t('buttons.archive', 'Archive')}
                              </button>
                            )}
                        </div>
                      </div>
                    </div>
                    <div className="mobile-project-actions" aria-hidden={false}>
                      <details className="mobile-actions-dropdown">
                        <summary className="mobile-actions-btn admin-project-button">
                          {t('buttons.actions', 'Actions')}
                        </summary>
                        <div className="mobile-actions-dropdown-list">
                          <button
                            onClick={() => handleViewProject(project)}
                            className="admin-project-button"
                          >
                            {t('buttons.view', 'View this project')}
                          </button>
                          {role === 'OWNER' && canEdit && (
                            <>
                              <button
                                onClick={() => handleEditProject(project)}
                                className="admin-project-button admin-edit-button"
                              >
                                {t('buttons.edit', 'Edit')}
                              </button>
                              <a
                                href={`/projects/${project.projectIdentifier}/manage-lots`}
                                className="admin-project-button admin-lots-button"
                              >
                                {t('buttons.lots', 'View Project Lots')}
                              </a>
                            </>
                          )}
                          {role === 'OWNER' &&
                            canEdit &&
                            project.status !== 'ARCHIVED' && (
                              <button
                                onClick={() => openArchiveModal(project)}
                                className="admin-project-button archive-button"
                              >
                                {t('buttons.archive', 'Archive')}
                              </button>
                            )}
                        </div>
                      </details>
                    </div>
                  </div>
                ))
              ) : (
                <div className="no-results">
                  <p>
                    {t(
                      'noResults',
                      `No projects found matching "${searchTerm}"`
                    )}
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </section>
      {isCreateOpen && (
        <div
          style={overlayStyle}
          role="dialog"
          aria-modal="true"
          onClick={e => {
            if (showConfirmClose) return;
            if (e.target === e.currentTarget) setShowConfirmClose(true);
          }}
        >
          <div style={modalStyle}>
            <div className="create-project-header">
              <h1>Create New Project</h1>
            </div>
            {submitError && <div className="error-message">{submitError}</div>}
            <CreateProjectForm
              onCancel={() => setShowConfirmClose(true)}
              onSuccess={() => {
                setIsCreateOpen(false);
                fetchProjects();
              }}
              onError={setSubmitError}
            />
          </div>
        </div>
      )}
      {isCreateOpen && showConfirmClose && (
        <div style={confirmOverlayStyle} role="dialog" aria-modal="true">
          <div style={confirmBoxStyle} onClick={e => e.stopPropagation()}>
            <h2 style={{ marginTop: 0 }}>Leave form?</h2>
            <p style={{ margin: '0.5rem 0 1rem 0' }}>
              If you exit now, any information you’ve entered will be lost.
            </p>
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                gap: '0.5rem',
              }}
            >
              <button
                type="button"
                className="btn-cancel btn-stay-on-form"
                onClick={() => setShowConfirmClose(false)}
              >
                Stay on form
              </button>
              <button
                type="button"
                className="btn-submit"
                onClick={() => {
                  setShowConfirmClose(false);
                  setIsCreateOpen(false);
                  setSubmitError(null);
                }}
              >
                Leave form
              </button>
            </div>
          </div>
        </div>
      )}
      {isEditOpen && projectToEdit && (
        <div
          style={overlayStyle}
          role="dialog"
          aria-modal="true"
          onClick={e => {
            if (e.target === e.currentTarget) handleEditCancel();
          }}
        >
          <div style={modalStyle}>
            {submitError && <div className="error-message">{submitError}</div>}
            <EditProjectForm
              project={projectToEdit}
              onCancel={handleEditCancel}
              onSuccess={handleEditSuccess}
              onError={setSubmitError}
            />
          </div>
        </div>
      )}
      {isArchiveOpen && projectToArchive && (
        <div
          className="confirmation-modal-overlay"
          role="dialog"
          aria-modal="true"
        >
          <div
            className="confirmation-modal-content"
            onClick={e => e.stopPropagation()}
          >
            <div className="confirmation-modal-header">
              <h2>Archive project</h2>
            </div>
            <div className="confirmation-modal-body">
              <p>
                Are you sure you want to archive “{projectToArchive.projectName}
                ”? You can still view archived projects by switching the filter.
              </p>
            </div>
            <div className="confirmation-modal-footer">
              <button
                type="button"
                className="btn-cancel"
                onClick={closeArchiveModal}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn-confirm-destructive"
                onClick={async () => {
                  try {
                    const token = isAuthenticated
                      ? await getAccessTokenSilently({
                          authorizationParams: {
                            audience:
                              import.meta.env.VITE_AUTH0_AUDIENCE ||
                              'https://construction-api.loca',
                          },
                        }).catch(() => null)
                      : null;

                    await (
                      await import('../../features/projects/api/projectApi')
                    ).projectApi.updateProject(
                      projectToArchive.projectIdentifier,
                      { status: 'ARCHIVED' },
                      token || null
                    );

                    // Optimistic update: if viewing Active, remove it immediately
                    if (statusFilter === 'ACTIVE') {
                      setProjects(prev =>
                        prev.filter(
                          p =>
                            p.projectIdentifier !==
                            projectToArchive.projectIdentifier
                        )
                      );
                      setFilteredProjects(prev =>
                        prev.filter(
                          p =>
                            p.projectIdentifier !==
                            projectToArchive.projectIdentifier
                        )
                      );
                    }

                    closeArchiveModal();
                    // Refresh list to reflect backend state for all filters
                    await fetchProjects();
                  } catch (e) {
                    if (e?.response?.status === 404) {
                      redirectToError(404);
                    } else {
                      redirectToError();
                    }
                    closeArchiveModal();
                  }
                }}
              >
                Archive
              </button>
            </div>
          </div>
        </div>
      )}
      {isLotSelectionOpen && projectForLotSelection && (
        <div
          className="confirmation-modal-overlay"
          role="dialog"
          aria-modal="true"
        >
          <div
            className="confirmation-modal-content"
            onClick={e => e.stopPropagation()}
          >
            <div className="confirmation-modal-header">
              <h2>Select Lot</h2>
            </div>
            <div className="confirmation-modal-body">
              <p>
                You are assigned to multiple lots in "
                {projectForLotSelection.projectName}". Please select which lot
                you want to view:
              </p>
              <div style={{ marginTop: '1rem' }}>
                {userLotsForProject.map(lot => (
                  <button
                    key={lot.lotId}
                    onClick={() => handleLotSelection(lot)}
                    className="admin-project-button"
                    style={{
                      display: 'block',
                      marginBottom: '0.5rem',
                      width: '100%',
                    }}
                  >
                    Lot {lot.id}
                  </button>
                ))}
              </div>
            </div>
            <div className="confirmation-modal-footer">
              <button
                type="button"
                className="btn-cancel"
                onClick={closeLotSelectionModal}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProjectsPage;
