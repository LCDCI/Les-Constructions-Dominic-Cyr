import { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import '../../styles/Project/projects.css';
import '../../styles/Project/create-project.css';
import '../../styles/Project/edit-project.css';
import '../../styles/Modals/ConfirmationModal.css';
import CreateProjectForm from '../../features/projects/components/CreateProjectForm';
import EditProjectForm from '../../features/projects/components/EditProjectForm';
import useBackendUser from '../../hooks/useBackendUser';
import { canCreateProjects, canEditProjects } from '../../utils/permissions';

const ProjectsPage = () => {
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('AVAILABLE'); // AVAILABLE | ARCHIVED | BOTH
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

  const { role } = useBackendUser();
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const canCreate = canCreateProjects(role);
  const canEdit = canEditProjects(role);

  const filterMenuContainerRef = useRef(null);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
  // Use relative path to leverage Vite proxy
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1';

  useEffect(() => {
    fetchProjects();
  }, [isAuthenticated, statusFilter]);

  useEffect(() => {
    filterProjects();
  }, [searchTerm, projects]);

  useEffect(() => {
    const handleClickOutside = (event) => {
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

      // Build URLs based on filter
      const baseUrl = `${apiBaseUrl}/projects`;

      const headers = {};
      // Include auth token if user is authenticated
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE || 'https://construction-api.loca',
            },
          });
          headers.Authorization = `Bearer ${token}`;
        } catch (tokenError) {
          console.warn('Could not get access token, proceeding without authentication:', tokenError);
        }
      }

      if (statusFilter === 'BOTH') {
        const [availableResp, archivedResp] = await Promise.all([
          fetch(baseUrl, { headers }),
          fetch(`${baseUrl}?status=ARCHIVED`, { headers }),
        ]);

        if (!availableResp.ok) {
          const errorText = await availableResp.text();
          throw new Error(`Failed to fetch available: ${availableResp.status} - ${errorText}`);
        }
        if (!archivedResp.ok) {
          const errorText = await archivedResp.text();
          throw new Error(`Failed to fetch archived: ${archivedResp.status} - ${errorText}`);
        }

        const availableAll = await availableResp.json();
        const archived = await archivedResp.json();
        const available = (availableAll || []).filter(p => p.status !== 'ARCHIVED');

        // Merge, keeping order: available first, then archived; de-duplicate by identifier
        const seen = new Set();
        const merged = [];
        for (const p of [...(available || []), ...(archived || [])]) {
          const id = p.projectIdentifier;
          if (id && !seen.has(id)) {
            seen.add(id);
            merged.push(p);
          }
        }

        setProjects(merged);
        setFilteredProjects(merged);
      } else {
        // AVAILABLE (default) or ARCHIVED only
        if (statusFilter === 'ARCHIVED') {
          const response = await fetch(`${baseUrl}?status=ARCHIVED`, { headers });
          if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to fetch: ${response.status} - ${errorText}`);
          }
          const data = await response.json();
          setProjects(data || []);
          setFilteredProjects(data || []);
        } else {
          // AVAILABLE: explicitly filter out archived in case backend returns both for owners
          const response = await fetch(baseUrl, { headers });
          if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to fetch: ${response.status} - ${errorText}`);
          }
          const data = await response.json();
          const nonArchived = (data || []).filter(p => p.status !== 'ARCHIVED');
          setProjects(nonArchived);
          setFilteredProjects(nonArchived);
        }
      }

      setLoading(false);
    } catch (error) {
      console.error('Error fetching projects:', error);
      setError(error.message || 'Failed to load projects. Please try again.');
      setProjects([]);
      setFilteredProjects([]);
      setLoading(false);
    }
  };

  const filterProjects = () => {
    if (!searchTerm.trim()) {
      setFilteredProjects(projects);
      return;
    }

    const filtered = projects.filter(project =>
      project.projectName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredProjects(filtered);
  };

  const getImageUrl = imageIdentifier => {
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handleViewProject = projectIdentifier => {
    window.location.href = `/projects/${projectIdentifier}/metadata`;
  };

  const handleEditProject = (project) => {
    setProjectToEdit(project);
    setIsEditOpen(true);
    setSubmitError(null);
  };

  const handleEditSuccess = () => {
    setIsEditOpen(false);
    setProjectToEdit(null);
    fetchProjects();
  };

  const handleArchiveProject = async (project) => {
    if (!project || project.status === 'ARCHIVED') return;
    const confirm = window.confirm(`Archive "${project.projectName}"? You can still view archived projects from the filter.`);
    if (!confirm) return;

    try {
      const headersToken = isAuthenticated
        ? await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE || 'https://construction-api.loca',
            },
          }).catch(() => null)
        : null;

      await (await import('../../features/projects/api/projectApi')).projectApi.updateProject(
        project.projectIdentifier,
        { status: 'ARCHIVED' },
        headersToken || null
      );

      await fetchProjects();
    } catch (e) {
      console.error('Failed to archive project:', e);
      setError(e.message || 'Failed to archive project.');
    }
  };

  const handleEditCancel = () => {
    setIsEditOpen(false);
    setProjectToEdit(null);
    setSubmitError(null);
  };

  const openArchiveModal = (project) => {
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
    <div className="projects-page">
      <div className="projects-content">
        <div className="projects-container">
          <div className="projects-header">
            <h1>Projects</h1>
            {canCreate && (
              <button
                className="create-project-button"
                onClick={() => setIsCreateOpen(true)}
              >
                Create New Project
              </button>
            )}
          </div>

          <div className="projects-filter">
            <div className="search-container">
              <input
                type="text"
                className="search-input"
                placeholder="Search projects by name..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
            {role === 'OWNER' && (
              <div ref={filterMenuContainerRef} style={{ position: 'relative', marginLeft: '20px' }}>
                <button
                  aria-haspopup="menu"
                  aria-expanded={isFilterMenuOpen}
                  className="filter-trigger"
                  onClick={() => setIsFilterMenuOpen(prev => !prev)}
                  title="Filter projects"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
                >
                  <span className="filter-icon" aria-hidden="true" style={{ display: 'inline-block', lineHeight: 1 }}>
                    <span style={{ display: 'block', width: '18px', height: '2px', background: 'currentColor', marginBottom: '3px' }}></span>
                    <span style={{ display: 'block', width: '18px', height: '2px', background: 'currentColor', marginBottom: '3px' }}></span>
                    <span style={{ display: 'block', width: '18px', height: '2px', background: 'currentColor' }}></span>
                  </span>
                  Filters
                </button>

                {isFilterMenuOpen && (
                  <div
                    role="menu"
                    aria-label="Project filters"
                    className="filter-menu"
                    style={{
                      position: 'absolute',
                      top: 'calc(100% + 8px)',
                      left: 0,
                      minWidth: '200px'
                    }}
                  >
                    <button
                      role="menuitemradio"
                      aria-checked={statusFilter === 'AVAILABLE'}
                      className={`filter-option ${statusFilter === 'AVAILABLE' ? 'active' : ''}`}
                      onClick={() => {
                        setStatusFilter('AVAILABLE');
                        setSearchTerm('');
                        setIsFilterMenuOpen(false);
                      }}
                    >
                      Available
                    </button>
                    <button
                      role="menuitemradio"
                      aria-checked={statusFilter === 'ARCHIVED'}
                      className={`filter-option ${statusFilter === 'ARCHIVED' ? 'active' : ''}`}
                      onClick={() => {
                        setStatusFilter('ARCHIVED');
                        setSearchTerm('');
                        setIsFilterMenuOpen(false);
                      }}
                    >
                      Archived
                    </button>
                    <button
                      role="menuitemradio"
                      aria-checked={statusFilter === 'BOTH'}
                      className={`filter-option ${statusFilter === 'BOTH' ? 'active' : ''}`}
                      onClick={() => {
                        setStatusFilter('BOTH');
                        setSearchTerm('');
                        setIsFilterMenuOpen(false);
                      }}
                    >
                      Both
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>

          {loading && (
            <div className="projects-loading">
              <p>Loading projects...</p>
            </div>
          )}

          {error && (
            <div className="projects-error">
              <p>{error}</p>
              <button onClick={fetchProjects} className="retry-button">
                Retry
              </button>
            </div>
          )}

          {!loading && !error && (
            <div className="projects-grid">
              {filteredProjects.length > 0 ? (
                filteredProjects.map(project => (
                  <div 
                    key={project.projectIdentifier} 
                    className={`project-card ${project.status === 'ARCHIVED' ? 'project-card-archived' : ''}`}
                  >
                    {project.status === 'ARCHIVED' && (
                      <div className="archived-badge">ARCHIVED</div>
                    )}
                    <div className="project-image-container">
                      <img
                        src={getImageUrl(project.imageIdentifier)}
                        alt={project.projectName}
                        className="project-image"
                      />
                    </div>
                    <h2 className="project-title">{project.projectName}</h2>
                    <p className="project-description">
                      {project.projectDescription}
                    </p>
                    <div className="project-actions">
                      <a
                        href={`/projects/${project.projectIdentifier}/metadata`}
                        className="project-button"
                      >
                        View this project
                      </a>
                      {canEdit && (
                        <button
                          onClick={() => handleEditProject(project)}
                          className="project-button edit-button"
                        >
                          Edit
                        </button>
                      )}
                      {canEdit && project.status !== 'ARCHIVED' && (
                        <button
                          onClick={() => openArchiveModal(project)}
                          className="project-button archive-button"
                        >
                          Archive
                        </button>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <div className="no-results">
                  <p>No projects found matching &quot;{searchTerm}&quot;</p>
                </div>
              )}
            </div>
          )}

          {!loading &&
            !error &&
            filteredProjects.length === 0 &&
            searchTerm === '' && (
              <div className="no-projects">
                <p>No projects available. Create your first project!</p>
              </div>
            )}

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
                {submitError && (
                  <div className="error-message">{submitError}</div>
                )}
                <CreateProjectForm
                  onCancel={() => setShowConfirmClose(true)}
                  onSuccess={projectIdentifier => {
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
                    className="btn-cancel"
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
                {submitError && (
                  <div className="error-message">{submitError}</div>
                )}
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
            <div className="confirmation-modal-overlay" role="dialog" aria-modal="true">
              <div className="confirmation-modal-content" onClick={e => e.stopPropagation()}>
                <div className="confirmation-modal-header">
                  <h2>Archive project</h2>
                </div>
                <div className="confirmation-modal-body">
                  <p>
                    Are you sure you want to archive “{projectToArchive.projectName}”?
                    You can still view archived projects by switching the filter.
                  </p>
                </div>
                <div className="confirmation-modal-footer">
                  <button type="button" className="btn-cancel" onClick={closeArchiveModal}>
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
                                audience: import.meta.env.VITE_AUTH0_AUDIENCE || 'https://construction-api.loca',
                              },
                            }).catch(() => null)
                          : null;

                        await (await import('../../features/projects/api/projectApi')).projectApi.updateProject(
                          projectToArchive.projectIdentifier,
                          { status: 'ARCHIVED' },
                          token || null
                        );

                        // Optimistic update: if viewing Available, remove it immediately
                        if (statusFilter === 'AVAILABLE') {
                          setProjects(prev => prev.filter(p => p.projectIdentifier !== projectToArchive.projectIdentifier));
                          setFilteredProjects(prev => prev.filter(p => p.projectIdentifier !== projectToArchive.projectIdentifier));
                        }

                        closeArchiveModal();
                        // Refresh list to reflect backend state for all filters
                        await fetchProjects();
                      } catch (e) {
                        console.error('Failed to archive project:', e);
                        setError(e.message || 'Failed to archive project.');
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
        </div>
      </div>
    </div>
  );
};

export default ProjectsPage;
