import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Project/project-management.css';
import '../../styles/Public_Facing/residential-projects.css';
import {
  FaUserTie,
  FaChevronLeft,
  FaChevronRight,
  FaTimes,
  FaClipboardCheck,
} from 'react-icons/fa';
import { IoIosHammer } from 'react-icons/io';
import { AiOutlineStock } from 'react-icons/ai';
import { GrFormSchedule } from 'react-icons/gr';
import { MdAttachMoney } from 'react-icons/md';
import { CiStopwatch } from 'react-icons/ci';

export default function ProjectManagementPage() {
  const { t } = usePageTranslations('projectManagement');
  const [modalOpen, setModalOpen] = useState(false);
  const [currentImage, setCurrentImage] = useState(null);
  const [currentImageSet, setCurrentImageSet] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);

  const CDN_BASE_URL =
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16';

  const PM_IMAGES = {
    bromont: {
      url: `${CDN_BASE_URL}/BromontFrl.JPEG`,
      city: 'Bromont',
      alt: 'Bromont construction project',
    },
    shefford: {
      urls: [
        `${CDN_BASE_URL}/shefford1.JPG`,
        `${CDN_BASE_URL}/shefford2.JPG`,
        `${CDN_BASE_URL}/shefford3.JPG`,
      ],
      city: 'Shefford',
      alt: 'Shefford construction project',
    },
    stHilaire: {
      url: `${CDN_BASE_URL}/st-hilaire.png`,
      city: 'St-Hilaire',
      alt: 'St-Hilaire construction project',
    },
  };

  const openModal = (imageOrImages, index = 0) => {
    if (Array.isArray(imageOrImages)) {
      setCurrentImageSet(imageOrImages);
      setCurrentIndex(index);
      setCurrentImage(imageOrImages[index]);
    } else {
      setCurrentImageSet([imageOrImages]);
      setCurrentIndex(0);
      setCurrentImage(imageOrImages);
    }
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setCurrentImage(null);
    setCurrentImageSet([]);
    setCurrentIndex(0);
  };

  const nextImage = () => {
    if (currentImageSet.length > 1) {
      const newIndex = (currentIndex + 1) % currentImageSet.length;
      setCurrentIndex(newIndex);
      setCurrentImage(currentImageSet[newIndex]);
    }
  };

  const prevImage = () => {
    if (currentImageSet.length > 1) {
      const newIndex =
        (currentIndex - 1 + currentImageSet.length) % currentImageSet.length;
      setCurrentIndex(newIndex);
      setCurrentImage(currentImageSet[newIndex]);
    }
  };

  return (
    <div className="project-management-page">
      {/* Hero Banner Section */}
      <section className="projects-hero" aria-labelledby="pm-hero-title">
        <div className="projects-hero-content">
          <span className="section-kicker">
            {t('hero.kicker', 'Our Services')}
          </span>
          <h1 className="projects-title" id="pm-hero-title">
            {t('hero.title', 'Project Management')}
          </h1>
          <p className="projects-subtitle">
            {t('hero.subtitle', 'For Peace of Mind')}
          </p>
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
                  'Planning, organization, and work follow-up'
                )}
              </h2>
              <p className="pm-tagline">
                {t('intro.tagline', 'We handle it for you!')}
              </p>
            </div>

            <div className="pm-images-grid">
              <div
                className="pm-image-card"
                onClick={() => openModal(PM_IMAGES.bromont.url)}
                style={{ cursor: 'pointer' }}
              >
                <div className="pm-image-container">
                  <img
                    src={PM_IMAGES.bromont.url}
                    alt={PM_IMAGES.bromont.alt}
                    className="pm-image"
                  />
                </div>
                <p className="pm-image-city">{PM_IMAGES.bromont.city}</p>
              </div>
              <div
                className="pm-image-card"
                onClick={() => openModal(PM_IMAGES.shefford.urls, 0)}
                style={{ cursor: 'pointer' }}
              >
                <div className="pm-image-container">
                  <img
                    src={PM_IMAGES.shefford.urls[0]}
                    alt={PM_IMAGES.shefford.alt}
                    className="pm-image"
                  />
                </div>
                <p className="pm-image-city">{PM_IMAGES.shefford.city}</p>
              </div>
              <div
                className="pm-image-card"
                onClick={() => openModal(PM_IMAGES.stHilaire.url)}
                style={{ cursor: 'pointer' }}
              >
                <div className="pm-image-container">
                  <img
                    src={PM_IMAGES.stHilaire.url}
                    alt={PM_IMAGES.stHilaire.alt}
                    className="pm-image"
                  />
                </div>
                <p className="pm-image-city">{PM_IMAGES.stHilaire.city}</p>
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
            <li>
              <span className="pm-adv-emoji" aria-hidden="true">
                <FaClipboardCheck />
              </span>
              <span className="pm-adv-text">
                {t('advantages.item7', 'Compliance assurance')}
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

      {/* Image Modal */}
      {modalOpen && (
        <div className="pm-image-modal-overlay" onClick={closeModal}>
          <div
            className="pm-image-modal-content"
            onClick={e => e.stopPropagation()}
          >
            <button
              className="pm-modal-close"
              onClick={closeModal}
              aria-label="Close modal"
            >
              <FaTimes />
            </button>

            {currentImageSet.length > 1 && (
              <>
                <button
                  className="pm-modal-nav pm-modal-prev"
                  onClick={prevImage}
                  aria-label="Previous image"
                >
                  <FaChevronLeft />
                </button>
                <button
                  className="pm-modal-nav pm-modal-next"
                  onClick={nextImage}
                  aria-label="Next image"
                >
                  <FaChevronRight />
                </button>
              </>
            )}

            <img src={currentImage} alt="Project" className="pm-modal-image" />

            {currentImageSet.length > 1 && (
              <div className="pm-modal-indicators">
                {currentImageSet.map((_, index) => (
                  <span
                    key={index}
                    className={`pm-modal-indicator ${index === currentIndex ? 'active' : ''}`}
                    onClick={() => {
                      setCurrentIndex(index);
                      setCurrentImage(currentImageSet[index]);
                    }}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
