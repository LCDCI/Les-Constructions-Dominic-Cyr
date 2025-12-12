import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useParams, useNavigate } from 'react-router-dom';
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  useMap,
  useMapEvents,
} from 'react-leaflet';
import { IoLeafOutline } from 'react-icons/io5';
import { HiOutlineHomeModern } from 'react-icons/hi2';
import '../styles/projectOverview.css';
import '../styles/projectColors.css';
import '../styles/overviewMap.css';

const DEFAULT_COORDS = [45.31941496688032, -72.79945127353109];

const API_BASE_URL =
  import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';
const NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

const LOTS_DATA = [
  {
    id: 16,
    x: 27,
    y: 39,
    status: 'Vendu',
    price: 120000,
    description: 'Lot 16 - Côté ouest',
  },
  {
    id: 17,
    x: 37,
    y: 43,
    status: 'Disponible',
    price: 135000,
    description: 'Lot 17 - Près du ruisseau',
  },
  {
    id: 18,
    x: 27,
    y: 46,
    status: 'Réservé',
    price: 125000,
    description: 'Lot 18 - Bonne exposition',
  },
  {
    id: 19,
    x: 37,
    y: 50,
    status: 'Disponible',
    price: 140000,
    description: 'Lot 19 - Grand terrain',
  },
  {
    id: 20,
    x: 27,
    y: 53,
    status: 'Vendu',
    price: 130000,
    description: 'Lot 20 - Vendu rapidement',
  },
  {
    id: 21,
    x: 37,
    y: 57,
    status: 'Disponible',
    price: 130000,
    description: 'Lot 21 - Dernier disponible',
  },
  {
    id: 22,
    x: 58,
    y: 69,
    status: 'Disponible',
    price: 110000,
    description: 'Lot 22 - Petit prix',
  },
  {
    id: 23,
    x: 67,
    y: 64,
    status: 'Vendu',
    price: 115000,
    description: 'Lot 23 - Vendu',
  },
  {
    id: 24,
    x: 58,
    y: 77,
    status: 'Réservé',
    price: 120000,
    description: 'Lot 24 - Réservé',
  },
  {
    id: 25,
    x: 67,
    y: 72,
    status: 'Disponible',
    price: 125000,
    description: 'Lot 25 - Belle vue',
  },
];

const LotMapInteractive = ({ lotImageSrc, lotsData }) => {
  const [selectedLot, setSelectedLot] = useState(null);

  const getLotColor = status => {
    switch (status) {
      case 'Vendu':
        return 'var(--color-danger)';
      case 'Réservé':
        return 'var(--color-warning)';
      case 'Disponible':
        return 'var(--color-success)';
      default:
        return 'var(--color-primary)';
    }
  };

  const handleClick = lot => {
    setSelectedLot(lot);
  };

  const handleClosePopup = () => {
    setSelectedLot(null);
  };

  return (
    <div className="lot-map-container">
      <div className="lot-map-wrapper">
        <img
          src={lotImageSrc}
          alt="Plan de lotissement"
          className="lot-map-image"
        />

        {lotsData.map(lot => (
          <div
            key={lot.id}
            onClick={() => handleClick(lot)}
            className="lot-marker"
            style={{
              left: `${lot.x}%`,
              top: `${lot.y}%`,
              backgroundColor: getLotColor(lot.status),
            }}
            title={`Lot ${lot.id}: ${lot.status}`}
            role="button"
            tabIndex="0"
          >
            <span className="lot-marker-label">{lot.id}</span>
          </div>
        ))}

        {selectedLot && (
          <div
            className="lot-popup"
            style={{
              left: `${selectedLot.x + 3}%`,
              top: `${selectedLot.y - 15}%`,
            }}
          >
            <div className="lot-popup-header">
              <h4 className="lot-popup-title">Lot #{selectedLot.id}</h4>
              <button
                className="lot-popup-close-btn"
                onClick={handleClosePopup}
              >
                ×
              </button>
            </div>
            <p>
              <strong>Statut:</strong>{' '}
              <span style={{ color: getLotColor(selectedLot.status) }}>
                {selectedLot.status}
              </span>
            </p>
            <p>
              <strong>Prix:</strong>{' '}
              {selectedLot.price ? `${selectedLot.price}$` : 'N/A'}
            </p>
            <p className="lot-popup-description">{selectedLot.description}</p>
          </div>
        )}
      </div>
    </div>
  );
};

LotMapInteractive.propTypes = {
  lotImageSrc: PropTypes.string.isRequired,
  lotsData: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.number.isRequired,
      x: PropTypes.number.isRequired,
      y: PropTypes.number.isRequired,
      status: PropTypes.string.isRequired,
      price: PropTypes.number,
      description: PropTypes.string,
    })
  ).isRequired,
};

export const projectOverviewApi = {
  getProjectOverview: async projectIdentifier => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}/overview`
    );
    if (!response.ok) {
      throw new Error('Failed to fetch project overview');
    }
    return response.json();
  },

  geocodeAddress: async address => {
    if (!address) return null;

    const params = new URLSearchParams({
      q: address,
      format: 'json',
      limit: 1,
    });

    const response = await fetch(`${NOMINATIM_URL}?${params.toString()}`);

    if (!response.ok) {
      return null;
    }

    const data = await response.json();

    if (data && data.length > 0) {
      const { lat, lon } = data[0];
      return [parseFloat(lat), parseFloat(lon)];
    }

    return null;
  },
};

const MapController = ({ mapCoords }) => {
  const map = useMap();

  useEffect(() => {
    const timeouts = [
      setTimeout(() => map.invalidateSize(), 0),
      setTimeout(() => map.invalidateSize(), 100),
      setTimeout(() => map.invalidateSize(), 250),
      setTimeout(() => map.invalidateSize(), 500),
    ];
    if (mapCoords) {
      const newCenter = mapCoords || DEFAULT_COORDS;
      map.setView(newCenter, map.getZoom());
    }

    return () => timeouts.forEach(clearTimeout);
  }, [map, mapCoords]);

  return null;
};

MapController.propTypes = {
  mapCoords: PropTypes.arrayOf(PropTypes.number),
};

const MapClickHandler = ({ onClick }) => {
  useMapEvents({
    click: onClick,
  });
  return null;
};

MapClickHandler.propTypes = {
  onClick: PropTypes.func.isRequired,
};

const LocationMap = ({ locationAddress, mapCoords, setShowModal }) => {
  const centerCoords = mapCoords || DEFAULT_COORDS;
  const defaultZoom = 13;
  const [mapKey, setMapKey] = useState(0);

  useEffect(() => {
    if (mapCoords) {
      setMapKey(prev => prev + 1);
    }
  }, [mapCoords]);

  if (!locationAddress) {
    return (
      <div
        style={{
          height: '400px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: '#f0f0f0',
        }}
      >
        Map address not specified.
      </div>
    );
  }

  const mapUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
  const attribution =
    '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors';

  const usingDefault = mapCoords === null;
  const popupText = usingDefault
    ? `Default location near ${locationAddress.split(',').pop()}`
    : locationAddress;

  const handleMapClick = () => {
    setShowModal(true);
  };

  return (
    <MapContainer
      center={centerCoords}
      zoom={defaultZoom}
      scrollWheelZoom={false}
      style={{ height: '100%', width: '100%', cursor: 'pointer' }}
      key={`map-${mapKey}`}
      whenCreated={mapInstance => {
        setTimeout(() => mapInstance.invalidateSize(), 100);
      }}
    >
      <MapController mapCoords={mapCoords} />
      <MapClickHandler onClick={handleMapClick} />
      <TileLayer attribution={attribution} url={mapUrl} />
      <Marker position={centerCoords}>
        <Popup>{popupText}</Popup>
      </Marker>
    </MapContainer>
  );
};

LocationMap.propTypes = {
  locationAddress: PropTypes.string,
  mapCoords: PropTypes.arrayOf(PropTypes.number),
  setShowModal: PropTypes.func.isRequired,
};

const LocationModal = ({ show, handleClose, mapCoords, locationAddress }) => {
  const centerCoords = mapCoords || DEFAULT_COORDS;
  const modalZoom = 15;

  if (!show) return null;

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-container" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Location: {locationAddress}</h2>
          <button
            className="modal-close-btn"
            onClick={handleClose}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>
        <div className="modal-body">
          <MapContainer
            center={centerCoords}
            zoom={modalZoom}
            scrollWheelZoom={true}
            style={{ height: '100%', width: '100%' }}
            key={`modal-map-${locationAddress}`}
          >
            <MapController mapCoords={mapCoords} />
            <TileLayer
              attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <Marker position={centerCoords}>
              <Popup>{locationAddress}</Popup>
            </Marker>
          </MapContainer>
        </div>
        <div className="modal-footer">
          <button className="btn-secondary" onClick={handleClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

LocationModal.propTypes = {
  show: PropTypes.bool.isRequired,
  handleClose: PropTypes.func.isRequired,
  mapCoords: PropTypes.arrayOf(PropTypes.number),
  locationAddress: PropTypes.string,
};

const ProjectOverviewPage = () => {
  const { projectIdentifier } = useParams();
  const navigate = useNavigate();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [mapCoords, setMapCoords] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const handleCloseModal = () => setShowModal(false);
  const handleShowModal = () => setShowModal(true);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  useEffect(() => {
    const fetchOverviewAndGeocode = async () => {
      try {
        setLoading(true);
        const data =
          await projectOverviewApi.getProjectOverview(projectIdentifier);
        setOverview(data);
        setError(null);

        if (data.locationAddress) {
          const coords = await projectOverviewApi.geocodeAddress(
            data.locationAddress
          );
          setMapCoords(coords);
        } else {
          setMapCoords(null);
        }
      } catch (err) {
        setError('Failed to load project details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchOverviewAndGeocode();
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

  const getFeatureIcon = featureTitle => {
    const title = featureTitle?.toLowerCase() || '';

    switch (title) {
      case 'landscape':
      case 'living environment':
        return IoLeafOutline;
      case 'new houses':
      case 'lots':
        return HiOutlineHomeModern;
      case 'energy efficiency':
        return IoLeafOutline;
      case 'smart home technology':
        return IoLeafOutline;
      default:
    }
    return IoLeafOutline;
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
        <button onClick={() => navigate('/residential-projects')}>
          Back to Residential Projects
        </button>
      </div>
    );
  }

  if (!overview) {
    return (
      <div className="project-overview-error">
        <p>Project not found</p>
        <button onClick={() => navigate('/residential-projects')}>
          Back to Residential Projects
        </button>
      </div>
    );
  }

  const themeClass = getProjectThemeClass();
  const lotissementImage =
    'frontend\\les_constructions_dominic_cyr\\public\\phase1_transparent.png';

  return (
    <div className={`project-overview-page ${themeClass}`}>
      <section className="project-hero">
        <div className="hero-image-container">
          <img
            src={getImageUrl(overview.imageIdentifier)}
            alt={overview.projectName}
            className="hero-image"
            onError={e => {
              e.target.src = '/public/fallback.jpg';
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
              {overview.features.map((feature, index) => {
                const IconComponent = getFeatureIcon(feature.featureTitle);

                return (
                  <div key={index} className="feature-card">
                    <IconComponent className="feature-icon" />
                    <h3 className="feature-title">{feature.featureTitle}</h3>
                    {feature.featureDescription && (
                      <p className="feature-description">
                        {feature.featureDescription}
                      </p>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </section>
      )}

      {/* NOUVELLE SECTION POUR LA CARTE DE LOTISSEMENT */}
      <section className="project-section lotissement-section">
        <div className="section-container">
          <h2 className="section-title">Plan des Lots Disponibles</h2>
          <div className="lot-map-section">
            <LotMapInteractive
              lotImageSrc={lotissementImage}
              lotsData={LOTS_DATA}
            />
          </div>
        </div>
      </section>
      {/* FIN NOUVELLE SECTION */}

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
              {overview.locationAddress && (
                <div className="location-map">
                  <LocationMap
                    locationAddress={overview.locationAddress}
                    mapCoords={mapCoords}
                    setShowModal={handleShowModal}
                  />
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      <LocationModal
        show={showModal}
        handleClose={handleCloseModal}
        mapCoords={mapCoords}
        locationAddress={overview?.locationAddress}
      />

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
                      e.target.src = '/public/fallback.jpg';
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
        <button
          className="back-button"
          onClick={() => navigate('/residential-projects')}
        >
          ← Back to All Projects
        </button>
      </div>
    </div>
  );
};

export default ProjectOverviewPage;
