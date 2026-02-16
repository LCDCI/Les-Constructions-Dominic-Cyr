import React from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Project/project-management.css';
import '../../styles/Public_Facing/residential-projects.css';
import { FaUserTie } from 'react-icons/fa';
import { IoIosHammer } from 'react-icons/io';
import { AiOutlineStock } from 'react-icons/ai';
import { GrFormSchedule } from 'react-icons/gr';
import { MdAttachMoney } from 'react-icons/md';
import { CiStopwatch } from 'react-icons/ci';

export default function ProjectManagementPage() {
  const { t } = usePageTranslations('projectManagement');

  const PM_IMAGE_IDS = {
    professionals: '1659ff85-b160-4111-b419-84834eb4375a',
    floorPlan: '48f50cea-f368-41d6-91c3-ae55157bd868',
    tools: 'bb6dd250-ed32-4041-8b4e-020e2ef45e2f',
  };

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : typeof window !== 'undefined' &&
          window.location.hostname === 'localhost'
        ? 'http://localhost:8082'
        : `${window.location.origin}/files`);

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) return '';
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const pillars = [
    t('intro.heading.line1', 'Planning'),
    t('intro.heading.line2', 'Organization'),
    t('intro.heading.line3', 'Site follow-up'),
  ];

  return (
    <div className="project-management-page">
      {/* Hero Banner Section */}
      <section className="pm-hero-banner" aria-labelledby="pm-hero-title">
        <div className="pm-hero-content">
          <h1 className="pm-hero-title" id="pm-hero-title">
            {`${t('hero.line1', 'PROJECT MANAGEMENT,')} ${t('hero.line2', 'FOR PEACE OF MIND')}`}
          </h1>
        </div>
      </section>

      {/* Introduction Section with Images */}
      <section className="pm-intro-section" aria-labelledby="pm-intro-title">
        <div className="pm-intro-container">
          <div className="pm-intro-content">
            {/* Heading Section */}
            <div className="pm-heading-section">
              <h2 className="pm-main-heading" id="pm-intro-title">
                {t(
                  'intro.heading.title',
                  'Planning, organization, and site follow-up without the friction'
                )}
              </h2>
              <div className="pm-pill-row" role="list">
                {pillars.map((label, index) => (
                  <span key={index} className="pm-pill" role="listitem">
                    {label}
                  </span>
                ))}
              </div>
              <p className="pm-tagline">
                {t('intro.tagline', 'We handle it for you!')}
              </p>
            </div>

            <div className="pm-images-grid">
              <div className="pm-image-card">
                <div className="pm-image-container">
                  <img
                    src={getImageUrl(PM_IMAGE_IDS.professionals)}
                    alt={t(
                      'intro.image1.alt',
                      'Professionals collaborating on project'
                    )}
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
                    alt={t(
                      'intro.image3.alt',
                      'Construction tools and materials'
                    )}
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
                  'You own land and want to entrust project management to a qualified builder? Les Constructions Dominic Cyr Inc. is here for you, whether for specific construction stages or a turnkey project.'
                )}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Advantages Section */}
      <section
        className="pm-advantages-section"
        aria-labelledby="pm-advantages-title"
      >
        <div className="pm-advantages-container">
          <h2 className="pm-advantages-heading" id="pm-advantages-title">
            {t('advantages.heading', 'Our project management strengths')}
          </h2>

          <ul className="pm-advantages-list">
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <FaUserTie />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item1', 'Advisory services')}
              </span>
            </li>
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <IoIosHammer />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item2', 'Compliance with building codes')}
              </span>
            </li>
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <AiOutlineStock />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item3', 'Planning with all stakeholders')}
              </span>
            </li>
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <GrFormSchedule />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item4', 'Work schedule development')}
              </span>
            </li>
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <MdAttachMoney />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item5', 'Cost control')}
              </span>
            </li>
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <CiStopwatch />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item6', 'Rigorous follow-up')}
              </span>
            </li>
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
              'Depending on project scope and complexity, pricing may be fixed-fee or cost-plus. Contact us to discuss.'
            )}
          </p>

          {/* Contact Link */}
          <div className="pm-contact-link-wrapper">
            <Link to="/contact" className="pm-contact-link">
              {t('advantages.contactLink', 'Contact us')}
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
