import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/residential-projects.css';

const ResidentialProjectsPage = () => {
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    filterProjects();
  }, [searchTerm, projects]);

  const fetchProjects = async () => {
    try {
      const response = await fetch(`${apiBaseUrl}/projects`);
      const data = await response.json();
      setProjects(data);
      setFilteredProjects(data);
      setLoading(false);
    } catch (error) {
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
    navigate(`/projects/${projectIdentifier}/overview`);
  };

  if (loading) {
    return (
      <div className="projects-page loading-state">
        <p>Loading projects...</p>
      </div>
    );
  }

  return (
    <div className="projects-page">
      <div className="projects-content">
        <div className="projects-container">
          <div className="projects-header">
            <h1>Residential Projects</h1>
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
          </div>

          <div className="projects-grid">
            {filteredProjects.length > 0 ? (
              filteredProjects.map(project => (
                <div key={project.projectIdentifier} className="project-card">
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
                  <button
                    className="project-button"
                    onClick={() => handleViewProject(project.projectIdentifier)}
                  >
                    View this project
                  </button>
                </div>
              ))
            ) : (
              <div className="no-results">
                <p>No projects found matching &quot;{searchTerm}&quot;</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResidentialProjectsPage;
