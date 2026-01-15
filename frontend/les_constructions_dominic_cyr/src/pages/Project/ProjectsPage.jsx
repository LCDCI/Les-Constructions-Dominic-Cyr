import { useState, useEffect } from 'react';
import '../../styles/Project/projects.css';
import '../../styles/Project/create-project.css';
import '../../styles/Project/edit-project.css';
import CreateProjectForm from '../../features/projects/components/CreateProjectForm';
import EditProjectForm from '../../features/projects/components/EditProjectForm';
import useBackendUser from '../../hooks/useBackendUser';
import { canCreateProjects, canEditProjects } from '../../utils/permissions';

const ProjectsPage = () => {
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [projectToEdit, setProjectToEdit] = useState(null);
  const [submitError, setSubmitError] = useState(null);
  const [showConfirmClose, setShowConfirmClose] = useState(false);

  const { role } = useBackendUser();
  const canCreate = canCreateProjects(role);
  const canEdit = canEditProjects(role);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
  // Use relative path to leverage Vite proxy
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1';

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    filterProjects();
  }, [searchTerm, projects]);

  const fetchProjects = async () => {
    try {
      setError(null);
      setLoading(true);
      const url = `${apiBaseUrl}/projects`;
      console.log('Fetching projects from:', url);
      const response = await fetch(url);
      console.log('Response status:', response.status, response.statusText);
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to fetch: ${response.status} - ${errorText}`);
      }
      const data = await response.json();
      console.log('Projects data received:', data);
      setProjects(data || []);
      setFilteredProjects(data || []);
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

  const handleEditCancel = () => {
    setIsEditOpen(false);
    setProjectToEdit(null);
    setSubmitError(null);
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
    <div className="admin-projects-page">
      <div className="admin-projects-content">
        <div className="admin-projects-container">
          <div className="admin-projects-header">
            <h1>Projects</h1>
            {canCreate && (
              <button
                className="admin-create-project-button"
                onClick={() => setIsCreateOpen(true)}
              >
                Create New Project
              </button>
            )}
          </div>

          <div className="admin-projects-filter">
            <div className="admin-search-container">
              <input
                type="text"
                className="admin-search-input"
                placeholder="Search projects by name..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          {loading && (
            <div className="admin-projects-loading">
              <p>Loading projects...</p>
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
            <div className="admin-projects-grid">
              {filteredProjects.length > 0 ? (
                filteredProjects.map(project => (
                  <div key={project.projectIdentifier} className="admin-project-card">
                    <div className="admin-project-image-container">
                      <img
                        src={getImageUrl(project.imageIdentifier)}
                        alt={project.projectName}
                        className="admin-project-image"
                      />
                    </div>
                    <h2 className="admin-project-title">{project.projectName}</h2>
                    <p className="admin-project-description">
                      {project.projectDescription}
                    </p>
                    <div className="admin-project-actions">
                      <a
                        href={`/projects/${project.projectIdentifier}/metadata`}
                        className="admin-project-button"
                      >
                        View this project
                      </a>
                      {canEdit && (
                        <>
                          <button
                            onClick={() => handleEditProject(project)}
                            className="admin-project-button admin-edit-button"
                          >
                            Edit
                          </button>
                          <a
                            href={`/projects/${project.projectIdentifier}/team-management`}
                            className="admin-project-button admin-team-button"
                          >
                            Manage Team
                          </a>
                        </>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <div className="admin-no-results">
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
        </div>
      </div>
    </div>
  );
};

export default ProjectsPage;
