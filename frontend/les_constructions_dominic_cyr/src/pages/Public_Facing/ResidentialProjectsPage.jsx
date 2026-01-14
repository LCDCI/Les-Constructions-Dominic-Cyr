import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/residential-projects.css';

const ResidentialProjectsPage = () => {
    const { t } = usePageTranslations('residentialProjects');
    const navigate = useNavigate();
    const [projects, setProjects] = useState([]);
    const [filteredProjects, setFilteredProjects] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);

    const pageSize = 6; // two rows of three cards or three rows of two cards

    const filesServiceUrl =
        import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
    const apiBaseUrl =
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

    const FALLBACK_IMAGE = '/fallback.jpg';

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
        if (!searchTerm.trim()) {
            setFilteredProjects(projects);
            setCurrentPage(1);
            return;
        }

        const filtered = projects.filter(project =>
            project.projectName.toLowerCase().includes(searchTerm.toLowerCase())
        );
        setFilteredProjects(filtered);
        setCurrentPage(1);
    };

    const getImageUrl = imageIdentifier => {
        if (!imageIdentifier) return FALLBACK_IMAGE;
        return `${filesServiceUrl}/files/${imageIdentifier}`;
    };

    const handleImageError = (e) => {
        e.target.onerror = null;
        e.target.src = FALLBACK_IMAGE;
    };

    const handleViewProject = projectIdentifier => {
        navigate(`/projects/${projectIdentifier}/overview`);
    };

    if (loading) {
        return (
            <div className="projects-page loading-state">
                <div className="page-loader">
                    <div className="loader-spinner"></div>
                </div>
            </div>
        );
    }

    const totalPages = Math.ceil(filteredProjects.length / pageSize) || 1;
    const startIndex = (currentPage - 1) * pageSize;
    const paginatedProjects = filteredProjects.slice(startIndex, startIndex + pageSize);

    const handlePageChange = (page) => {
        if (page < 1 || page > totalPages) return;
        setCurrentPage(page);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return (
        <div className="projects-page">
            {/* HEADER SECTION */}
            <section className="projects-hero">
                <div className="projects-hero-content">
                    <span className="section-kicker">{t('hero.kicker', 'Our Work')}</span>
                    <h1 className="projects-title">{t('hero.title', 'Residential Projects')}</h1>
                    <p className="projects-subtitle">
                        {t('hero.subtitle', 'Explore our portfolio of residential projects showcasing quality construction and innovative design.')}
                    </p>
                </div>
            </section>

            {/* SEARCH SECTION */}
            <div className="projects-search-container">
                <div className="container">
                    <div className="search-box">
                        <input
                            type="text"
                            className="search-input"
                            placeholder={t('search.placeholder', 'Search projects by name...')}
                            value={searchTerm}
                            onChange={e => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            {/* PORTFOLIO GRID */}
            <section className="portfolio-section">
                <div className="container">
                    {filteredProjects.length > 0 ? (
                        <div className="portfolio-grid">
                            {paginatedProjects.map((project) => (
                                <Link
                                    key={project.projectIdentifier}
                                    to={`/projects/${project.projectIdentifier}/overview`}
                                    className="portfolio-card"
                                    data-animate
                                >
                                    <img
                                        src={getImageUrl(project.imageIdentifier)}
                                        alt={project.projectName}
                                        loading="lazy"
                                        onError={handleImageError}
                                        className="card-image-bg"
                                    />
                                    <div className="card-overlay" />
                                </Link>
                            ))}
                        </div>
                    ) : (
                        <div className="no-results">
                            <p>{t('noResults', `No projects found matching "${searchTerm}"`)}</p>
                        </div>
                    )}

                    {filteredProjects.length > 0 && totalPages > 1 && (
                        <div className="projects-pagination">
                            <button
                                className="page-btn"
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 1}
                            >
                                {t('pagination.prev', 'Prev')}
                            </button>
                            {Array.from({ length: totalPages }, (_, idx) => {
                                const page = idx + 1;
                                return (
                                    <button
                                        key={page}
                                        className={`page-btn ${currentPage === page ? 'active' : ''}`}
                                        onClick={() => handlePageChange(page)}
                                    >
                                        {page}
                                    </button>
                                );
                            })}
                            <button
                                className="page-btn"
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage === totalPages}
                            >
                                {t('pagination.next', 'Next')}
                            </button>
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
};

export default ResidentialProjectsPage;
