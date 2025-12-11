import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectOverviewApi } from '../features/projects/api/projectOverviewApi';
import '../styles/projectOverview.css';
import '../styles/projectColors.css';

const ProjectOverviewPage = () => {
  const { projectIdentifier } = useParams();
  const navigate = useNavigate();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  useEffect(() => {
    const fetchOverview = async () => {
      try {
        setLoading(true);
        const data =
          await projectOverviewApi.getProjectOverview(projectIdentifier);
        setOverview(data);
        setError(null);
      } catch (err) {
        setError('Failed to load project details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchOverview();
  }, [projectIdentifier]);

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) return '/placeholder-project.png';
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const getProjectThemeClass = () => {
    const projectName = overview?.projectName?.toLowerCase() || '';
    if (projectName.includes('foresta')) return 'project-theme-foresta';
    if (projectName.includes('panorama')) return 'project-theme-panorama';
    return '';
  };

  if (loading) {
    return (
      <div className="project-overview-loading">
        <p>Loading project details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="project-overview-error">
        <p>{error}</p>
        <button onClick={() => navigate('/projects')}>Back to Projects</button>
      </div>
    );
  }

  if (!overview) {
    return (
      <div className="project-overview-error">
        <p>Project not found</p>
        <button onClick={() => navigate('/projects')}>Back to Projects</button>
      </div>
    );
  }

  const themeClass = getProjectThemeClass();

  return (
    <div className={`project-overview-page ${themeClass}`}>
      <section className="project-hero">
        <div className="hero-image-container">
          <img
            src={getImageUrl(overview.imageIdentifier)}
            alt={overview.projectName}
            className="hero-image"
            onError={e => {
              e.target.src = '/placeholder-project.png';
            }}
          />
          <div className="hero-overlay">
            <div className="hero-content">
              <h1 className="hero-title">
                {overview.heroTitle || overview.projectName}
              </h1>
              {overview.heroSubtitle && (
                <p className="hero-subtitle">{overview.heroSubtitle}</p>
              )}
            </div>
          </div>
        </div>
      </section>

      {overview.overviewSectionContent && (
        <section className="project-section overview-section">
          <div className="section-container">
            {overview.overviewSectionTitle && (
              <h2 className="section-title">{overview.overviewSectionTitle}</h2>
            )}
            <p className="section-content">{overview.overviewSectionContent}</p>
            {overview.heroDescription && (
              <p className="section-description">{overview.heroDescription}</p>
            )}
          </div>
        </section>
      )}

      {overview.features && overview.features.length > 0 && (
        <section className="project-section features-section">
          <div className="section-container">
            {overview.featuresSectionTitle && (
              <h2 className="section-title">{overview.featuresSectionTitle}</h2>
            )}
            <div className="features-grid">
              {overview.features.map((feature, index) => (
                <div key={index} className="feature-card">
                  {feature.featureIcon && (
                    <div className="feature-icon">{feature.featureIcon}</div>
                  )}
                  <h3 className="feature-title">{feature.featureTitle}</h3>
                  {feature.featureDescription && (
                    <p className="feature-description">
                      {feature.featureDescription}
                    </p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {overview.lots && overview.lots.length > 0 && (
        <section className="project-section lots-section">
          <div className="section-container">
            <h2 className="section-title">Available Lots</h2>
            <div className="lots-grid">
              {overview.lots.map(lot => (
                <div key={lot.lotId} className="lot-card">
                  <div className="lot-image-container">
                    <img
                      src={getImageUrl(lot.imageIdentifier)}
                      alt={lot.location}
                      className="lot-image"
                      onError={e => {
                        e.target.src = '/placeholder-lot.png';
                      }}
                    />
                  </div>
                  <div className="lot-details">
                    <h3 className="lot-location">{lot.location}</h3>
                    <div className="lot-info">
                      <span className="lot-dimensions">{lot.dimensions}</span>
                      <span className="lot-price">
                        {new Intl.NumberFormat('en-CA', {
                          style: 'currency',
                          currency: 'CAD',
                        }).format(lot.price)}
                      </span>
                    </div>
                    <span
                      className={`lot-status status-${lot.lotStatus.toLowerCase()}`}
                    >
                      {lot.lotStatus}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {overview.locationDescription && (
        <section className="project-section location-section">
          <div className="section-container">
            {overview.locationSectionTitle && (
              <h2 className="section-title">{overview.locationSectionTitle}</h2>
            )}
            <div className="location-content">
              <div className="location-text">
                <p className="section-content">
                  {overview.locationDescription}
                </p>
                {overview.locationAddress && (
                  <div className="location-address">
                    <strong>Address:</strong>
                    <p>{overview.locationAddress}</p>
                  </div>
                )}
              </div>
              {overview.locationMapEmbedUrl && (
                <div className="location-map">
                  <iframe
                    src={overview.locationMapEmbedUrl}
                    width="100%"
                    height="400"
                    style={{ border: 0 }}
                    allowFullScreen=""
                    loading="lazy"
                    referrerPolicy="no-referrer-when-downgrade"
                    title="Project Location Map"
                  ></iframe>
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {overview.galleryImages && overview.galleryImages.length > 0 && (
        <section className="project-section gallery-section">
          <div className="section-container">
            {overview.gallerySectionTitle && (
              <h2 className="section-title">{overview.gallerySectionTitle}</h2>
            )}
            <div className="gallery-grid">
              {overview.galleryImages.map((image, index) => (
                <div key={index} className="gallery-item">
                  <img
                    src={getImageUrl(image.imageIdentifier)}
                    alt={image.imageCaption || `Gallery image ${index + 1}`}
                    className="gallery-image"
                    onError={e => {
                      e.target.src = '/placeholder-project.png';
                    }}
                  />
                  {image.imageCaption && (
                    <p className="gallery-caption">{image.imageCaption}</p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      <div className="back-button-container">
        <button className="back-button" onClick={() => navigate('/projects')}>
          ← Back to All Projects
        </button>
      </div>
    </div>
  );
};

export default ProjectOverviewPage;
