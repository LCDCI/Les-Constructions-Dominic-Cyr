import React, { useEffect, useState } from 'react';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { fetchPublicSiteContent } from '../../features/siteContent/api/siteContentApi';
import { mergeProjectManagementImageOverrides } from '../../features/siteContent/projectManagementGallery';
import { getFileUrl } from '../../utils/filesService';
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
  const [selectedProjectKey, setSelectedProjectKey] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [imageOverrides, setImageOverrides] = useState({});

  useEffect(() => {
    let active = true;
    setImageOverrides({});
    Promise.allSettled([
      fetchPublicSiteContent('en', 'project-management'),
      fetchPublicSiteContent('fr', 'project-management'),
    ]).then(([englishResult, frenchResult]) => {
      if (!active) return;

      const getItems = (result, language) => {
        if (result.status === 'fulfilled') return result.value;
        // eslint-disable-next-line no-console
        console.error(
          `Unable to load project-management gallery images for ${language}.`,
          result.reason
        );
        return [];
      };

      setImageOverrides(
        mergeProjectManagementImageOverrides(
          getItems(englishResult, 'en'),
          getItems(frenchResult, 'fr')
        )
      );
    });
    return () => {
      active = false;
    };
  }, []);

  const CDN_BASE_URL =
    'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16';

  const projectCards = [
    {
      key: 'bromont',
      imageSlots: ['galleryImage.1', 'galleryImage.6', 'galleryImage.7'],
      imageFallbacks: [`${CDN_BASE_URL}/BromontFrl.JPEG`, '', ''],
      alt: 'Bromont construction project',
      location: t('galleryItem.1.location', 'Bromont'),
      caption: t('galleryItem.1.caption', 'Bromont'),
    },
    {
      key: 'shefford',
      imageSlots: ['galleryImage.2', 'galleryImage.3', 'galleryImage.4'],
      imageFallbacks: [
        `${CDN_BASE_URL}/shefford1.JPG`,
        `${CDN_BASE_URL}/shefford2.JPG`,
        `${CDN_BASE_URL}/shefford3.JPG`,
      ],
      alt: 'Shefford construction project',
      location: t('galleryItem.2.location', 'Shefford'),
      caption: t('galleryItem.2.caption', 'Shefford'),
    },
    {
      key: 'st-bruno',
      imageSlots: ['galleryImage.5', 'galleryImage.8', 'galleryImage.9'],
      imageFallbacks: [`${CDN_BASE_URL}/st-hilaire.png`, '', ''],
      alt: 'St-Bruno construction project',
      location: t('galleryItem.3.location', 'St-Bruno'),
      caption: t('galleryItem.3.caption', 'St-Bruno'),
    },
  ];

  const imageFor = (slot, fallback = '') => {
    const imageIdentifier = imageOverrides[slot] || fallback;
    return imageIdentifier ? getFileUrl(imageIdentifier) : null;
  };
  const imagesFor = project =>
    project.imageSlots
      .map((slot, index) => imageFor(slot, project.imageFallbacks[index]))
      .filter(Boolean);

  const currentProject = projectCards.find(
    project => project.key === selectedProjectKey
  );
  const currentImageSet = currentProject ? imagesFor(currentProject) : [];
  const currentImageIndex =
    currentIndex < currentImageSet.length ? currentIndex : 0;
  const currentImage = currentImageSet[currentImageIndex] || null;

  const openModal = projectKey => {
    setSelectedProjectKey(projectKey);
    setCurrentIndex(0);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedProjectKey(null);
    setCurrentIndex(0);
  };

  const nextImage = () => {
    if (currentImageSet.length > 1) {
      setCurrentIndex(index => (index + 1) % currentImageSet.length);
    }
  };

  const prevImage = () => {
    if (currentImageSet.length > 1) {
      setCurrentIndex(
        index => (index - 1 + currentImageSet.length) % currentImageSet.length
      );
    }
  };

  return (
    <div className="public-page project-management-page">
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
              <p className="pm-title-subtitle">
                {t('intro.subtitle', "We've got you covered!")}
              </p>
              <p className="pm-tagline">
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
        </div>
      </section>

      <section className="pm-projects-section">
        <div className="pm-projects-container">
          <h2 className="pm-gallery-heading">
            {t(
              'gallery.heading',
              'Some project management achievements completed by Les Constructions Dominic Cyr Inc.'
            )}
          </h2>
          <div className="pm-images-grid">
            {projectCards.map(project => {
              const images = imagesFor(project);
              return (
                <div
                  key={project.key}
                  className="pm-image-card"
                  onClick={() => openModal(project.key)}
                  role="button"
                  tabIndex="0"
                  onKeyDown={event => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      openModal(project.key);
                    }
                  }}
                >
                  <div className="pm-image-container">
                    <img
                      src={images[0]}
                      alt={project.alt}
                      className="pm-image"
                    />
                    <span className="pm-image-badge">{project.location}</span>
                  </div>
                  <p className="pm-image-city">{project.caption}</p>
                </div>
              );
            })}
          </div>
          <div className="pm-cta-card pm-pricing-card">
            <p className="pm-pricing-text">
              {t(
                'advantages.pricing',
                'Depending on the scope and complexity of the project, pricing could be at a fixed cost or at a marked-up price. Contact us to discuss.'
              )}
            </p>
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
              type="button"
              className="pm-modal-close"
              onClick={closeModal}
              aria-label="Close modal"
            >
              <FaTimes />
            </button>

            {currentImageSet.length > 1 && (
              <>
                <button
                  type="button"
                  className="pm-modal-nav pm-modal-prev"
                  onClick={prevImage}
                  aria-label="Previous image"
                >
                  <FaChevronLeft />
                </button>
                <button
                  type="button"
                  className="pm-modal-nav pm-modal-next"
                  onClick={nextImage}
                  aria-label="Next image"
                >
                  <FaChevronRight />
                </button>
              </>
            )}
            {currentImage && (
              <img
                src={currentImage}
                alt="Project"
                className="pm-modal-image"
              />
            )}

            {currentImageSet.length > 1 && (
              <div className="pm-modal-indicators">
                <span className="pm-modal-index" aria-live="polite">
                  {currentImageIndex + 1} / {currentImageSet.length}
                </span>
                {currentImageSet.map((image, index) => (
                  <button
                    key={image}
                    type="button"
                    className={`pm-modal-indicator ${index === currentImageIndex ? 'active' : ''}`}
                    onClick={() => setCurrentIndex(index)}
                    aria-label={`${t('image', 'Image')} ${index + 1}`}
                    aria-pressed={index === currentImageIndex}
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
