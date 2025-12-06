import { useState, useEffect } from 'react';
import '../styles/projects.css';

const ProjectsPage = () => {
    const [projects, setProjects] = useState([]);
    const [filteredProjects, setFilteredProjects] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);

    const filesServiceUrl = import.meta.env. VITE_FILES_SERVICE_URL || 'http://localhost:8082';
    const apiBaseUrl = import.meta.env. VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

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
            console.error('Error fetching projects:', error);
            setLoading(false);
        }
    };

    const filterProjects = () => {
        if (!searchTerm. trim()) {
            setFilteredProjects(projects);
            return;
        }

        const filtered = projects.filter(project =>
            project.projectName.toLowerCase().includes(searchTerm.toLowerCase())
        );
        setFilteredProjects(filtered);
    };

    const getImageUrl = (imageIdentifier) => {
        if (! imageIdentifier) {
            return '/placeholder-project.png';
        }
        return `${filesServiceUrl}/files/${imageIdentifier}`;
    };

    const handleImageError = (e) => {
        e.target.src = '/placeholder-project.png';
    };

    const handleViewProject = (projectIdentifier) => {
        window.location.href = `/projects/${projectIdentifier}`;
    };

    if (loading) {
        return (
            <div className="projects-content">
                <div className="projects-container">
                    <p style={{ textAlign: 'center', padding: '5%', fontSize: '1.2rem' }}>Loading projects...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="projects-content">
            <div className="projects-container">
                <div className="projects-header">
                    <h1>Projects</h1>
                </div>

                <div className="projects-filter">
                    <div className="search-container">
                        <input
                            type="text"
                            className="search-input"
                            placeholder="Search projects by name..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>

                <div className="projects-grid">
                    {filteredProjects.length > 0 ? (
                        filteredProjects.map((project) => (
                            <div key={project.projectIdentifier} className="project-card">
                                <div className="project-image-container">
                                    <img
                                        src={getImageUrl(project.imageIdentifier)}
                                        alt={project. projectName}
                                        className="project-image"
                                        onError={handleImageError}
                                    />
                                </div>
                                <h2 className="project-title">{project.projectName}</h2>
                                <p className="project-description">{project.projectDescription}</p>
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
                            <p>No projects found matching "{searchTerm}"</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProjectsPage;