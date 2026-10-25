import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MdArrowBackIos, MdArrowForwardIos } from 'react-icons/md';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { fetchPublicSiteContent } from '../../features/siteContent/api/siteContentApi';
import '../../styles/Public_Facing/realizations.css';
import '../../styles/Public_Facing/residential-projects.css';

// Image IDs from file storage (can be file ID or full CDN URL)
const REALIZATION_IMAGE_IDS = [
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/3262-1.jpeg',
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_1.jpeg',
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_2.jpeg',
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_3.jpeg',
  '1242c6c5-6a3d-4ca0-bac0-06d25bebb0bd',
  '1634e9ee-2680-41d1-b28a-47353f842d9c',
  '1659ff85-b160-4111-b419-84834eb4375a',
  '1681b3d5-8f0a-4daf-9590-53a1ce37cf20',
  '48f50cea-f368-41d6-91c3-ae55157bd868',
  '55378cf7-c0a0-48be-b5f9-9d2507eff177',
  '610354b2-8a9c-4e87-95a1-3cc63f494c6e',
  'bb6dd250-ed32-4041-8b4e-020e2ef45e2f',
];

const RealizationsPage = () => {
  const { t, currentLanguage } = usePageTranslations('realizations');
  const [currentIndex, setCurrentIndex] = useState(0);
  const [imageIds, setImageIds] = useState(REALIZATION_IMAGE_IDS.slice(0, 8));
  const [galleryItems, setGalleryItems] = useState([]);

  useEffect(() => {
    let active = true;
    fetchPublicSiteContent(currentLanguage, 'realizations')
      .then(items => {
        if (!active) return;
        const overrides = (items || []).reduce(
          (result, item) => {
            if (item.pageKey?.startsWith('galleryImage.')) {
              const index = Number(item.pageKey.split('.')[1]) - 1;
              if (item.imageIdentifier)
                result.images[index] = item.imageIdentifier;
            }
            if (item.pageKey?.startsWith('galleryItem.')) {
              const [, index, field] = item.pageKey.split('.');
              const itemIndex = Number(index) - 1;
              result.items[itemIndex] = result.items[itemIndex] || {};
              if (field === 'image' && item.imageIdentifier) {
                result.items[itemIndex].image = item.imageIdentifier;
              } else if (field === 'location' || field === 'description') {
                result.items[itemIndex][field] = item.contentText || '';
              }
            }
            return result;
          },
          { images: {}, items: {} }
        );
        setImageIds(
          REALIZATION_IMAGE_IDS.slice(0, 8).map(
            (image, index) => overrides.images[index] || image
          )
        );
        setGalleryItems(
          Array.from({ length: 9 }, (_, index) => ({
            ...(overrides.items[index] || {}),
            index,
          }))
        );
      })
      .catch(() => {
        if (active) {
          setImageIds(REALIZATION_IMAGE_IDS.slice(0, 8));
          setGalleryItems(Array.from({ length: 9 }, (_, index) => ({ index })));
        }
      });
    return () => {
      active = false;
    };
  }, [currentLanguage]);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : window.location.origin);

  const getImageUrl = imageIdentifier => {
    if (imageIdentifier.startsWith('http')) return imageIdentifier;
    return `${filesServiceUrl.replace(/\/files$/, '')}/files/${imageIdentifier}`;
  };

  const handlePrevious = () => {
    setCurrentIndex(prevIndex =>
      prevIndex === 0 ? imageIds.length - 1 : prevIndex - 1
    );
  };

  const handleNext = () => {
    setCurrentIndex(prevIndex =>
      prevIndex === imageIds.length - 1 ? 0 : prevIndex + 1
    );
  };

  const pad = n => String(n).padStart(2, '0');

  return (
    <div className="public-page realizations-page">
      {/* Hero Banner Section */}
      <section
        className="public-page__hero"
        aria-labelledby="realizations-title"
      >
        <div className="public-page__hero-content">
          <span className="public-page__kicker">
            {t('hero.subtitle', 'When vision becomes reality')}
          </span>
          <h1 className="public-page__title" id="realizations-title">
            {t('hero.title', 'Realizations')}
          </h1>
        </div>
      </section>

      {/* Intro Text Section */}
      <section
        className="public-page__section"
        aria-labelledby="realizations-intro-title"
      >
        <div className="public-page__intro">
          <h2 id="realizations-intro-title" className="public-page__heading">
            {t('intro.title', 'Notre savoir-faire en images')}
          </h2>
          <p className="public-page__copy">
            {t(
              'intro.description',
              'Our realizations reflect our expertise and commitment to quality. Each project is crafted with care to deliver lasting, well-designed spaces tailored to the needs of our clients.'
            )}
          </p>
        </div>
      </section>

      {/* Gallery Section */}
      <section
        className="public-page__section public-page__section--tint realizations-gallery-section"
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
            <MdArrowBackIos size={16} />
          </button>

          <div className="realizations-gallery" role="group" aria-live="polite">
            {imageIds.length > 0 ? (
              <div className="gallery-image-item">
                <img
                  src={getImageUrl(imageIds[currentIndex])}
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
            <MdArrowForwardIos size={16} />
          </button>
        </div>

        {imageIds.length > 0 && (
          <p className="gallery-counter" aria-hidden="true">
            {pad(currentIndex + 1)} / {pad(imageIds.length)}
          </p>
        )}
      </section>

      <section
        className="public-page__section realizations-gallery-grid-section"
        aria-labelledby="realizations-gallery-grid-title"
      >
        <div className="public-page__intro">
          <span className="public-page__kicker">
            {t('galleryGrid.kicker', 'Our Gallery')}
          </span>
          <h2
            id="realizations-gallery-grid-title"
            className="public-page__heading"
          >
            {t('galleryGrid.title', 'Our Gallery')}
          </h2>
        </div>
        <div className="realizations-gallery-grid">
          {galleryItems
            .filter(item => item.image)
            .map(item => (
              <article className="realizations-gallery-card" key={item.index}>
                <img
                  src={getImageUrl(item.image)}
                  alt={item.description || t('gallery.imageAlt', 'Realization')}
                  className="realizations-gallery-card-image"
                  loading="lazy"
                />
                {(item.location || item.description) && (
                  <div className="realizations-gallery-card-content">
                    {item.location && (
                      <p className="realizations-gallery-card-location">
                        {item.location}
                      </p>
                    )}
                    {item.description && <p>{item.description}</p>}
                  </div>
                )}
              </article>
            ))}
        </div>
        <Link
          to="/contact"
          className="public-page__button realizations-contact-link"
        >
          {t('gallery.contactLink', 'Contact us')}
        </Link>
      </section>
    </div>
  );
};

export default RealizationsPage;
