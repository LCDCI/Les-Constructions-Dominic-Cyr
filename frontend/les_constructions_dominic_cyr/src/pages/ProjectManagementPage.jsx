import React from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../hooks/usePageTranslations';
import '../styles/project-management.css';

export default function ProjectManagementPage() {
  const { t, isLoading } = usePageTranslations('projectManagement');

  // Image UUIDs from MinIO file service
  const PM_IMAGE_IDS = {
    professionals: '36b7e01d-f6a2-4c65-93e6-ce768806bed0',
    floorPlan: '24fd5054-1739-464a-9494-220be7718775',
    tools: 'b25db4c6-0161-4057-bd55-d3a35ab330fb',
    gallery1: 'e4b5281c-68eb-4c6d-b1cb-c534b725c737',
    gallery2: '5907733d-8774-4a3e-bab2-c4e9cdc48677',
    gallery3: '7e224338-adaf-4923-804c-0e803944a9af',
  };

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) return '';
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  if (isLoading) {
    return <div className="project-management-loading">Loading...</div>;
  }

  return (
    <div className="project-management-page">
      {/* Hero Banner Section */}
      <section className="pm-hero-banner">
        <div className="pm-hero-content">
          <h1 className="pm-hero-title">
            {t('hero.line1', 'LA GESTION DE PROJET,')}
            <br />
            {t('hero.line2', 'POUR UNE TRANQUILLITÉ D\'ESPRIT')}
          </h1>
        </div>
      </section>

      {/* Introduction Section with Images */}
      <section className="pm-intro-section">
        <div className="pm-intro-container">
          <div className="pm-intro-content">
            {/* Heading Section */}
            <div className="pm-heading-section">
              <h2 className="pm-main-heading">
                {t('intro.heading.line1', 'PLANIFICATION')}
                <br />
                {t('intro.heading.line2', 'ORGANISATION')}
                <br />
                {t('intro.heading.line3', 'SUIVI DES TRAVAUX')}
              </h2>
              <p className="pm-tagline">{t('intro.tagline', 'On s\'en occupe !')}</p>
            </div>

            {/* Image Grid - Three Images in Better Arrangement */}
            <div className="pm-images-grid">
              <div className="pm-image-card">
                <div className="pm-image-container">
                  <img
                    src={getImageUrl(PM_IMAGE_IDS.professionals)}
                    alt={t('intro.image1.alt', 'Professionals collaborating on project')}
                    className="pm-image"
                  />
                </div>
              </div>
              <div className="pm-image-card">
                <div className="pm-image-container">
                  <img
                    src={getImageUrl(PM_IMAGE_IDS.floorPlan)}
                    alt={t('intro.image2.alt', '3D floor plan rendering')}
                    className="pm-image"
                  />
                </div>
              </div>
              <div className="pm-image-card">
                <div className="pm-image-container">
                  <img
                    src={getImageUrl(PM_IMAGE_IDS.tools)}
                    alt={t('intro.image3.alt', 'Construction tools and materials')}
                    className="pm-image"
                  />
                </div>
              </div>
            </div>

            {/* Bottom Paragraph - Centered */}
            <div className="pm-intro-paragraph">
              <p>
                {t(
                  'intro.paragraph',
                  'Vous êtes propriétaire d\'un terrain et vous souhaitez confier la gestion de projet à un entrepreneur qualifié? Les Constructions Dominic Cyr Inc. est là pour vous, que ce soit pour certaines étapes spécifiques de la construction ou pour un projet clé en main.'
                )}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Advantages Section */}
      <section className="pm-advantages-section">
        <div className="pm-advantages-container">
          <h2 className="pm-advantages-heading">
            {t(
              'advantages.heading',
              'Il y a de nombreux avantages à nous confier la gestion de votre projet :'
            )}
          </h2>

          <ul className="pm-advantages-list">
            <li>{t('advantages.item1', 'Service conseil')}</li>
            <li>{t('advantages.item2', 'Conformité avec les règles du bâtiment')}</li>
            <li>{t('advantages.item3', 'Planification avec tous les intervenants')}</li>
            <li>{t('advantages.item4', 'Établissement de l\'échéancier des travaux')}</li>
            <li>{t('advantages.item5', 'Contrôle des coûts')}</li>
            <li>{t('advantages.item6', 'Suivi rigoureux')}</li>
            <li>{t('advantages.item7', 'Assurance conformité')}</li>
          </ul>

          {/* Separator Lines */}
          <div className="pm-separator">
            <div className="pm-separator-line"></div>
            <div className="pm-separator-line"></div>
          </div>

          {/* Pricing/Contact Paragraph */}
          <p className="pm-pricing-text">
            {t(
              'advantages.pricing',
              'Selon l\'ampleur et la complexité du projet, la tarification pourrait être à coût fixe ou à prix majoré. Contactez-nous pour en discuter.'
            )}
          </p>

          {/* Contact Link */}
          <div className="pm-contact-link-wrapper">
            <Link to="/contact" className="pm-contact-link">
              {t('advantages.contactLink', 'Contactez-nous')}
            </Link>
          </div>
        </div>
      </section>

      {/* Gallery Section */}
      <section className="pm-gallery-section">
        <div className="pm-gallery-container">
          <h2 className="pm-gallery-heading">
            {t(
              'gallery.heading',
              'Quelques réalisations en gestion de projet par Les Constructions Dominic Cyr Inc.'
            )}
          </h2>

          <div className="pm-gallery-grid">
            <div className="pm-gallery-card">
              <div className="pm-gallery-image-container">
                <img
                  src={getImageUrl(PM_IMAGE_IDS.gallery1)}
                  alt={t('gallery.image1.alt', 'Résidence')}
                  className="pm-gallery-image"
                />
              </div>
              <p className="pm-gallery-caption">{t('gallery.caption', 'Résidence')}</p>
            </div>

            <div className="pm-gallery-card">
              <div className="pm-gallery-image-container">
                <img
                  src={getImageUrl(PM_IMAGE_IDS.gallery2)}
                  alt={t('gallery.image2.alt', 'Résidence')}
                  className="pm-gallery-image"
                />
              </div>
              <p className="pm-gallery-caption">{t('gallery.caption', 'Résidence')}</p>
            </div>

            <div className="pm-gallery-card">
              <div className="pm-gallery-image-container">
                <img
                  src={getImageUrl(PM_IMAGE_IDS.gallery3)}
                  alt={t('gallery.image3.alt', 'Résidence')}
                  className="pm-gallery-image"
                />
              </div>
              <p className="pm-gallery-caption">{t('gallery.caption', 'Résidence')}</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
