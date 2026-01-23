import React, { useState, useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import { FiTool, FiRefreshCw, FiShield } from 'react-icons/fi';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { fetchRenovations } from '../../features/renovations/api/renovations';
import RenovationCard from '../../features/renovations/components/RenovationCard';
import '../../styles/Public_Facing/home.css';
import '../../styles/Public_Facing/RenovationsPage.css';

const RenovationsPage = ({ resolveAssetUrl }) => {
  // Load translations from the root namespace since your JSON is flat
  const { t } = usePageTranslations('renovations');

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    (window.location.hostname.includes('lcdci-portal') ||
      window.location.hostname.includes('lcdci-frontend'))
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
      console.error('Failed to fetch renovations:', error);
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
      <section className="hero">
        <div className="hero-background">
          <div className="hero-image renovations-hero-bg" />
          <div className="hero-overlay" />
        </div>
        <div className="hero-container">
          <div className="hero-content" data-animate>
            <p className="hero-label">{t('hero.label', 'RENOVATIONS')}</p>
            <h1 className="hero-heading">
              {t('hero.heading', 'Custom renovations, delivered with care')}
            </h1>
            <p className="hero-description">
              {t(
                'hero.lede',
                'Kitchens, basements, open spaces or façades — we modernize every area with precision and creativity.'
              )}
            </p>
            <div className="hero-buttons">
              <Link to="/realizations" className="btn btn-primary">
                {t('cta.discover', 'Discover')}
              </Link>
              <Link to="/contact" className="btn btn-secondary">
                {t('cta.contact', 'Contact')}
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURED INTRO SECTION */}
      <section className="content-section featured-section single-column">
        <div className="section-text-wrapper" data-animate>
          <div className="section-header center renovations-feature-card">
            <span className="section-kicker">
              {t('kicker.renovations', 'Renovations')}
            </span>
            <h2 className="section-title">
              <em>{t('intro.emphasis', 'Modernize')}</em>{' '}
              {t('intro.rest', 'your space with style')}
            </h2>
            <p className="section-subtitle">
              {t(
                'intro.description',
                'Whether you want to refresh your kitchen, rethink your basement, open up spaces, or enhance your façade, we deliver with precision, rigor, and creativity.'
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
        <div className="renovations-page__status renovations-page__status--empty">
          <h2>{t('states.empty.title', 'Aucune rénovation pour le moment')}</h2>
          <p>
            {t(
              'states.empty.body',
              'Revenez bientôt pour voir nos derniers projets de rénovation !'
            )}
          </p>
        </div>
      )}

      {/* RENOVATIONS LIST */}
      {!isLoading && renovations.length > 0 && (
        <section className="portfolio-section">
          <div className="container">
            <div className="section-header center" data-animate>
              <span className="section-kicker">
                {t('grid.kicker', 'Our Work')}
              </span>
              <h2 className="section-title">
                {t('grid.title', 'Recent Renovations')}
              </h2>
            </div>
            <div className="renovations-page__grid">
              {renovations.map(
                ({
                  renovationId,
                  beforeImageIdentifier,
                  afterImageIdentifier,
                  description,
                }) => (
                  <RenovationCard
                    key={renovationId}
                    renovationIdentifier={renovationId}
                    beforeImageIdentifier={''}
                    afterImageIdentifier={''}
                    description={description}
                    resolveAssetUrl={() => ''}
                    showTitle={false}
                  />
                )
              )}
            </div>
          </div>
        </section>
      )}

      {/* CALL TO ACTION */}
      <section className="contact-cta">
        <div className="contact-wrapper single-column">
          <div className="contact-content" data-animate>
            <span className="section-kicker">
              {t('cta.kicker', 'Where to start?')}
            </span>
            <h2 className="contact-title">
              {t('cta.title', "Let's talk about your project")}
            </h2>
            <p className="contact-description">
              {t(
                'cta.subtitle',
                'Get tailored support to transform your space into a place that reflects you.'
              )}
            </p>
            <Link to="/contact" className="btn btn-primary">
              {t('cta.button', 'Contact Us')}
            </Link>
          </div>
          {/* Image removed intentionally */}
        </div>
      </section>
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
