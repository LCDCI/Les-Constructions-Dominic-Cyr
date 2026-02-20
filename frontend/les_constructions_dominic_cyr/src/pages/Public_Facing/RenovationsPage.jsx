import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { FiRefreshCw } from 'react-icons/fi';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { fetchRenovations } from '../../features/renovations/api/renovations';
import '../../styles/Public_Facing/home.css';
import '../../styles/Public_Facing/RenovationsPage.css';
import '../../styles/Public_Facing/residential-projects.css';

const RenovationsPage = ({ resolveAssetUrl }) => {
  // Load translations from the root namespace since your JSON is flat
  const { t } = usePageTranslations('renovations');

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : `${window.location.origin}/files`);

  const getImageUrl = identifier => {
    if (!identifier) return '';
    // If a custom resolveAssetUrl is provided, use it
    if (resolveAssetUrl) {
      return resolveAssetUrl(identifier);
    }
    // Otherwise, use the default file service URL
    return `${filesServiceUrl}/files/${identifier}`;
  };

  const [renovations, setRenovations] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);

  useEffect(() => {
    loadRenovations();
  }, []);

  // Animate on scroll (reuse pattern from Home page)
  useEffect(() => {
    const elements = Array.from(document.querySelectorAll('[data-animate]'));
    if (!elements.length) return;

    const prefersReduced =
      typeof window !== 'undefined' &&
      window.matchMedia &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReduced) {
      elements.forEach(el => el.classList.add('animated'));
      return;
    }

    const io = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animated');
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -80px 0px' }
    );

    elements.forEach(el => io.observe(el));
    return () => io.disconnect();
  }, [renovations]);

  const loadRenovations = async () => {
    try {
      setIsLoading(true);
      setIsError(false);
      const data = await fetchRenovations();
      setRenovations(data);
    } catch (error) {
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  const refetch = () => {
    loadRenovations();
  };

  // Images are removed site-wide for this page; use decorative gradients instead

  return (
    <div className="renovations-page" aria-live="polite">
      {/* HERO */}
      <section
        className="projects-hero"
        aria-labelledby="renovations-hero-title"
        aria-describedby="renovations-hero-subtitle"
      >
        <div className="hero-background">
          <div className="hero-image renovations-hero-bg" />
          <div className="hero-overlay" />
        </div>
        <div className="hero-container projects-hero-content">
          <div className="hero-content" data-animate>
            <p className="hero-label">{t('hero.label', 'RENOVATIONS')}</p>
            <h1
              className="hero-heading projects-title"
              id="renovations-hero-title"
            >
              {t('hero.heading', 'Custom renovations, delivered with care')}
            </h1>
            <p
              className="hero-description projects-subtitle"
              id="renovations-hero-subtitle"
            >
              {t(
                'hero.lede',
                'Kitchens, basements, open spaces or façades — we modernize every area with precision and creativity.'
              )}
            </p>
          </div>
        </div>
      </section>

      {/* FEATURED INTRO SECTION */}
      <section
        className="content-section featured-section single-column"
        aria-labelledby="renovations-intro-title"
      >
        <div className="section-text-wrapper" data-animate>
          <div className="section-header center renovations-feature-card">
            <h2 className="section-title" id="renovations-intro-title">
              <em>{t('intro.emphasis', 'Transform')}</em>{' '}
              {t(
                'intro.rest',
                'your home into the living space of your dreams!'
              )}
            </h2>
            <p className="section-subtitle" style={{ whiteSpace: 'pre-line' }}>
              {t(
                'intro.description',
                "Whether it's modernizing your kitchen, completely rethinking your basement, opening up spaces, or increasing your property value, our team delivers your project with precision, style, and efficiency.\n\n\nWe offer a turnkey service: planning, design, site management, and high-end finishing. Every detail is crafted to reflect your personality and improve your daily comfort.\n\n\nEntrust your renovation to passionate professionals!"
              )}
            </p>
            <Link to="/contact" className="link-arrow">
              {t('intro.cta', 'Talk to an expert')}
            </Link>
          </div>
        </div>
      </section>

      {/* ERROR */}
      {isError && (
        <div
          className="renovations-page__status renovations-page__status--error"
          role="alert"
        >
          <p>
            {t(
              'states.error.message',
              'Impossible de charger les rénovations. Veuillez réessayer.'
            )}
          </p>
          <button
            type="button"
            onClick={refetch}
            className="renovations-page__button"
          >
            <FiRefreshCw aria-hidden="true" />
            {t('states.error.retry', 'Réessayer')}
          </button>
        </div>
      )}

      {/* LOADING */}
      {isLoading && (
        <div className="container renovations-page__grid">
          {[0, 1, 2].map(skeleton => (
            <div
              key={skeleton}
              className="renovations-page__skeleton"
              aria-hidden="true"
            />
          ))}
        </div>
      )}

      {/* EMPTY */}
      {!isLoading && !isError && renovations.length === 0 && (
        <div
          className="renovations-page__status renovations-page__status--empty"
          role="status"
          aria-live="polite"
        >
          <h2>{t('states.empty.title', 'Aucune rénovation pour le moment')}</h2>
          <p>
            {t(
              'states.empty.body',
              'Revenez bientôt pour voir nos derniers projets de rénovation !'
            )}
          </p>
        </div>
      )}
    </div>
  );
};

RenovationsPage.propTypes = {
  resolveAssetUrl: PropTypes.func,
};

RenovationsPage.defaultProps = {
  resolveAssetUrl: null,
};

export default RenovationsPage;
