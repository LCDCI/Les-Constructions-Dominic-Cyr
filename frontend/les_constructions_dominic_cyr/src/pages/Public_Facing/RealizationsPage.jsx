import { useState, useEffect } from 'react';
import { MdArrowBackIos, MdArrowForwardIos } from 'react-icons/md';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/realizations.css';
import '../../styles/Public_Facing/residential-projects.css';
import Footer from '../../components/Footers/ProjectsFooter';

const RealizationsPage = () => {
  const { t } = usePageTranslations('realizations');
  const [loading, setLoading] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);

  // Image IDs from file storage (can be file ID or full CDN URL)
  const REALIZATION_IMAGE_IDS = [
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/3262-1.jpeg',
    '1242c6c5-6a3d-4ca0-bac0-06d25bebb0bd',
    '1634e9ee-2680-41d1-b28a-47353f842d9c',
    '1659ff85-b160-4111-b419-84834eb4375a',
    '1681b3d5-8f0a-4daf-9590-53a1ce37cf20',
    '48f50cea-f368-41d6-91c3-ae55157bd868',
    '55378cf7-c0a0-48be-b5f9-9d2507eff177',
    '610354b2-8a9c-4e87-95a1-3cc63f494c6e',
    'bb6dd250-ed32-4041-8b4e-020e2ef45e2f',
  ];

  const PORTFOLIO_IMAGES = [
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/seigneurie.png',
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/fourche.jpg',
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/niverville.jpg',
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/ruisseau.jpg',
  ];

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : `${window.location.origin}/files`);

  const getImageUrl = imageIdentifier => {
    if (imageIdentifier.startsWith('http')) return imageIdentifier;
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handlePrevious = () => {
    setCurrentIndex(prevIndex =>
      prevIndex === 0 ? REALIZATION_IMAGE_IDS.length - 1 : prevIndex - 1
    );
  };

  const handleNext = () => {
    setCurrentIndex(prevIndex =>
      prevIndex === REALIZATION_IMAGE_IDS.length - 1 ? 0 : prevIndex + 1
    );
  };

  if (loading) {
    return (
      <div className="realizations-page">
        <p style={{ textAlign: 'center', padding: '5%', fontSize: '1.2rem' }}>
          Loading realizations...
        </p>
        <Footer />
      </div>
    );
  }

  return (
    <div className="realizations-page">
      {/* Hero Banner Section */}
      <section className="projects-hero" aria-labelledby="realizations-title">
        <div className="projects-hero-content">
          <h1 className="projects-title" id="realizations-title">
            {t('hero.title', 'Realizations')}
          </h1>
          <p className="realizations-hero-subtitle">
            {t('hero.subtitle', 'When vision becomes reality')}
          </p>
        </div>
      </section>

      {/* Intro Text Section */}
      <section
        className="realizations-intro"
        aria-labelledby="realizations-intro-title"
      >
        <div className="realizations-intro-content">
          <h2 id="realizations-intro-title" className="sr-only">
            {t('intro.title', 'Realizations overview')}
          </h2>
          <p className="realizations-intro-text">
            {t(
              'intro.description',
              'Our realizations reflect our expertise and commitment to quality. Each project is crafted with care to deliver lasting, well-designed spaces tailored to the needs of our clients.'
            )}
          </p>
        </div>
      </section>

      {/* Gallery Section */}
      <section
        className="realizations-gallery-section"
        aria-labelledby="realizations-gallery-title"
      >
        <h2 id="realizations-gallery-title" className="sr-only">
          {t('gallery.title', 'Realizations gallery')}
        </h2>
        <div className="realizations-gallery-container">
          <button
            className="gallery-arrow gallery-arrow-left"
            onClick={handlePrevious}
            aria-label={t('gallery.previousAriaLabel', 'Previous image')}
          >
            <MdArrowBackIos size={32} />
          </button>

          <div className="realizations-gallery" role="group" aria-live="polite">
            {REALIZATION_IMAGE_IDS.length > 0 ? (
              <div className="gallery-image-item">
                <img
                  src={getImageUrl(REALIZATION_IMAGE_IDS[currentIndex])}
                  alt={t('gallery.imageAlt', `Realization ${currentIndex + 1}`)}
                  className="gallery-image"
                />
              </div>
            ) : (
              <p style={{ textAlign: 'center', padding: '5%' }} role="status">
                {t('gallery.noResults', 'No realizations found')}
              </p>
            )}
          </div>

          <button
            className="gallery-arrow gallery-arrow-right"
            onClick={handleNext}
            aria-label={t('gallery.nextAriaLabel', 'Next image')}
          >
            <MdArrowForwardIos size={32} />
          </button>
        </div>

        {REALIZATION_IMAGE_IDS.length > 0 && (
          <div className="gallery-counter-hidden" aria-hidden="true" />
        )}
      </section>

      {/* Portfolio Text */}
      <section className="realizations-portfolio-text-section">
        <p className="realizations-portfolio-text">
          {t(
            'intro.portfolioText',
            'Here is a portfolio of projects completed in recent years.'
          )}
        </p>
      </section>

      {/* Portfolio 2x2 Grid */}
      <section
        className="realizations-portfolio-grid-section"
        aria-label="Portfolio"
      >
        <div className="realizations-portfolio-grid">
          {PORTFOLIO_IMAGES.map((url, idx) => (
            <div key={idx} className="realizations-portfolio-grid-item">
              <img
                src={url}
                alt={`Portfolio ${idx + 1}`}
                className="realizations-portfolio-grid-img"
              />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default RealizationsPage;
