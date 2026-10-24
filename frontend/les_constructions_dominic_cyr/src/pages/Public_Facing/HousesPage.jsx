import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import ProjectsFooter from '../../components/Footers/ProjectsFooter';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/houses.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

const getFilesServiceUrl = () =>
  import.meta.env.VITE_FILES_SERVICE_URL ||
  (typeof window !== 'undefined' &&
  window.location.hostname.includes('constructions-dominiccyr')
    ? 'https://files-service-app-xubs2.ondigitalocean.app'
    : `${window.location.origin}/files`);

export default function HousesPage() {
  const { t } = usePageTranslations('houses');
  const { i18n } = useTranslation();
  const { projectIdentifier, houseId } = useParams();
  const [houses, setHouses] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedHouse, setSelectedHouse] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchHouses = async () => {
      try {
        setLoading(true);
        const query = projectIdentifier
          ? `?projectIdentifier=${encodeURIComponent(projectIdentifier)}`
          : '';
        const response = await fetch(`${API_BASE_URL}/houses${query}`);
        if (!response.ok) throw new Error('fetchFailed');
        const data = await response.json();
        if (!cancelled) setHouses(Array.isArray(data) ? data : []);
      } catch (fetchError) {
        if (!cancelled) setError(fetchError.message || 'fetchFailed');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchHouses();
    return () => {
      cancelled = true;
    };
  }, [projectIdentifier]);

  useEffect(() => {
    if (!houseId) return;
    const house = houses.find(item => item.houseIdentifier === houseId);
    if (house) setSelectedHouse(house);
  }, [houseId, houses]);

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredHouses = houses.filter(house => {
    if (!normalizedSearch) return true;
    return [house.houseName, house.location, getHouseDescription(house)]
      .filter(Boolean)
      .some(value => String(value).toLowerCase().includes(normalizedSearch));
  });

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) return '/fallback.jpg';
    if (String(imageIdentifier).startsWith('http')) return imageIdentifier;
    return `${getFilesServiceUrl()}/files/${imageIdentifier}`;
  };

  const getHouseDescription = house => {
    const isFrench = i18n.language?.startsWith('fr');
    return (
      (isFrench ? house.descriptionFr : house.descriptionEn) ||
      house.description ||
      (isFrench ? house.descriptionEn : house.descriptionFr) ||
      ''
    );
  };

  const hasHouseValue = value =>
    value !== null && value !== undefined && String(value).trim() !== '';

  if (loading) {
    return (
      <div className="houses-page">
        <div className="houses-state">{t('loading', 'Loading houses...')}</div>
      </div>
    );
  }

  return (
    <div className="houses-page">
      <section className="projects-hero houses-hero">
        <div className="projects-hero-content">
          <span className="section-kicker">{t('kicker', 'Homes')}</span>
          <h1 className="projects-title">{t('title', 'Houses')}</h1>
          <p className="projects-subtitle">
            {t('subtitle', 'Explore available home designs for this project.')}
          </p>
        </div>
      </section>

      <main className="houses-content">
        <div className="houses-container">
          <label className="houses-search-label" htmlFor="houses-search">
            {t('searchLabel', 'Search houses')}
          </label>
          <input
            id="houses-search"
            className="houses-search-input"
            type="search"
            placeholder={t('searchPlaceholder', 'Search by name or location')}
            value={searchTerm}
            onChange={event => setSearchTerm(event.target.value)}
          />

          {error ? (
            <p className="houses-state houses-error">
              {t(error, t('fetchFailed', 'Failed to load houses.'))}
            </p>
          ) : filteredHouses.length === 0 ? (
            <p className="houses-state">{t('empty', 'No houses found.')}</p>
          ) : (
            <div className="houses-grid">
              {filteredHouses.map(house => (
                <article key={house.houseIdentifier} className="house-card">
                  <div className="house-image-container">
                    <img
                      src={getImageUrl(house.imageIdentifier)}
                      alt={house.houseName || t('imageAlt', 'House')}
                      className="house-image"
                      onError={event => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = '/fallback.jpg';
                      }}
                    />
                  </div>
                  <h2 className="house-title">{house.houseName}</h2>
                  {house.location && (
                    <p className="house-location">{house.location}</p>
                  )}
                  {getHouseDescription(house) && (
                    <p className="house-description">
                      {getHouseDescription(house)}
                    </p>
                  )}
                  <div className="house-details">
                    {hasHouseValue(house.numberOfBedrooms) && (
                      <span>
                        {t('bedrooms', 'Bedrooms')}: {house.numberOfBedrooms}
                      </span>
                    )}
                    {hasHouseValue(house.numberOfBathrooms) && (
                      <span>
                        {t('bathrooms', 'Bathrooms')}: {house.numberOfBathrooms}
                      </span>
                    )}
                    {hasHouseValue(house.numberOfRooms) && (
                      <span>
                        {t('rooms', 'Rooms')}: {house.numberOfRooms}
                      </span>
                    )}
                  </div>
                  <button
                    type="button"
                    className="house-button"
                    onClick={() => setSelectedHouse(house)}
                  >
                    {t('viewHouse', 'View this house')}
                  </button>
                </article>
              ))}
            </div>
          )}
        </div>
      </main>
      {selectedHouse && (
        <div
          className="house-image-modal-overlay"
          role="presentation"
          onClick={() => setSelectedHouse(null)}
        >
          <div
            className="house-image-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="house-image-modal-title"
            onClick={event => event.stopPropagation()}
          >
            <button
              type="button"
              className="house-image-modal-close"
              onClick={() => setSelectedHouse(null)}
              aria-label={t('close', 'Close')}
            >
              ×
            </button>
            <img
              src={getImageUrl(selectedHouse.imageIdentifier)}
              alt={selectedHouse.houseName || t('imageAlt', 'House')}
              className="house-image-modal-photo"
            />
            <div className="house-image-modal-caption">
              <h2 id="house-image-modal-title">{selectedHouse.houseName}</h2>
              {selectedHouse.location && <p>{selectedHouse.location}</p>}
              {getHouseDescription(selectedHouse) && (
                <p>{getHouseDescription(selectedHouse)}</p>
              )}
            </div>
          </div>
        </div>
      )}
      <ProjectsFooter projectId={projectIdentifier} />
    </div>
  );
}
