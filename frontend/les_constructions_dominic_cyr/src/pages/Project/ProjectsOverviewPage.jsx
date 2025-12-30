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
import { LuMapPinned } from 'react-icons/lu';
import '../../styles/Project/projectOverview.css';
import '../../styles/Public_Facing/overviewMap.css';

const DEFAULT_COORDS = [45.31941496688032, -72.79945127353109];

const API_BASE_URL =
  import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

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
      map.setView(mapCoords, map.getZoom());
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
    if (mapCoords && mapCoords !== DEFAULT_COORDS) {
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
          backgroundColor: 'var(--background-color-tertiary)',
        }}
      >
        Map address not specified.
      </div>
    );
  }

  const mapUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
  const attribution =
    '© <a href="http://osm.org/copyright">OpenStreetMap</a> contributors';

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
      <MapController mapCoords={centerCoords} />
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
            &times;
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
            <MapController mapCoords={centerCoords} />
            <TileLayer
              attribution='© <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
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
    const fetchOverview = async () => {
      try {
        setLoading(true);
        const data =
          await projectOverviewApi.getProjectOverview(projectIdentifier);
        setOverview(data);
        setError(null);

        if (data.locationLatitude && data.locationLongitude) {
          const lat = parseFloat(data.locationLatitude);
          const lng = parseFloat(data.locationLongitude);

          if (!isNaN(lat) && !isNaN(lng)) {
            setMapCoords([lat, lng]);
          } else {
            setMapCoords(null);
          }
        } else {
          setMapCoords(null);
        }
      } catch (err) {
        setError('Failed to load project details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchOverview();
  }, [projectIdentifier]);

  useEffect(() => {
    if (overview) {
      const colors = {
        '--buyer-color': overview.buyerColor || '#000000',
        '--primary-color': overview.primaryColor || '#2c3e50',
        '--tertiary-color': overview.tertiaryColor || '#3498db',

        '--background-color-white': '#ffffff',
        '--background-color-tertiary': overview.tertiaryColor || '#ffffff',
        '--text-primary': overview.primaryColor || '#2c3e50',
        '--text-secondary': overview.tertiaryColor || '#7f8c8d',
      };

      Object.keys(colors).forEach(key => {
        document.documentElement.style.setProperty(key, colors[key]);
      });
    }

    return () => {
      if (overview) {
        document.documentElement.style.removeProperty('--buyer-color');
        document.documentElement.style.removeProperty('--primary-color');
        document.documentElement.style.removeProperty('--tertiary-color');
        document.documentElement.style.removeProperty('--text-primary');
        document.documentElement.style.removeProperty('--text-secondary');
      }
    };
  }, [overview]);

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) return '/placeholder-project.png';
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const getFeatureIcon = featureTitle => {
    const title = featureTitle?.toLowerCase() || '';

    switch (title) {
      case 'living environment':
        return IoLeafOutline;
      case 'new houses':
        return HiOutlineHomeModern;
      case 'lots':
        return LuMapPinned;
      default:
    }
    return IoLeafOutline;
  };

  const getFeaturePath = featureTitle => {
    const title = featureTitle?.toLowerCase() || '';

    switch (title) {
      case 'living environment':
        return 'living-environment';
      case 'new houses':
        return 'houses';
      case 'lots':
        return 'lots';
      default:
        return null;
    }
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

  const finalMapCoords = mapCoords || DEFAULT_COORDS;

  const isMapCoordsValid =
    Array.isArray(finalMapCoords) &&
    finalMapCoords.length === 2 &&
    !isNaN(finalMapCoords[0]) &&
    !isNaN(finalMapCoords[1]);

  return (
    <div className={`project-overview-page`}>
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
                const path = getFeaturePath(feature.featureTitle);
                const isClickable = !!path;

                const handleClick = () => {
                  if (isClickable) {
                    //Ideal solution, but we do not have houses or living environment per project for the pages
                    //navigate(`/${projectIdentifier}/${path}`);
                    navigate(`/${path}`);
                  }
                };

                return (
                  <div
                    key={index}
                    className={`feature-card ${isClickable ? 'clickable-card' : ''}`}
                    onClick={handleClick}
                    role={isClickable ? 'button' : undefined}
                    tabIndex={isClickable ? 0 : undefined}
                    onKeyDown={e => {
                      if (isClickable && (e.key === 'Enter' || e.key === ' ')) {
                        handleClick();
                      }
                    }}
                  >
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
      {overview.lots && overview.lots.length > 0 && (
        <section className="project-section lots-section">
          <div className="section-container">
            {overview.lotsSectionTitle && (
              <h2 className="section-title">{overview.lotsSectionTitle}</h2>
            )}
            <div className="lots-grid">
              {overview.lots.map((lot, index) => (
                <div key={index} className="lot-card">
                  <div className="lot-image-container">
                    <img
                      src={getImageUrl(lot.lotImageIdentifier)}
                      alt={`Lot ${lot.lotLocation}`}
                      className="lot-image"
                    />
                  </div>
                  <div className="lot-details">
                    <h3 className="lot-location">{lot.lotLocation}</h3>
                    <div className="lot-info">
                      <span className="lot-dimensions">
                        {lot.lotDimensions}
                      </span>
                      <span className="lot-price">
                        ${lot.lotPrice.toLocaleString()}
                      </span>
                    </div>
                    <span
                      className={`lot-status status-${lot.lotStatus.toLowerCase()}`}
                    >
                      {lot.lotStatus}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}
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
              {overview.locationAddress && isMapCoordsValid && (
                <div className="location-map">
                  <LocationMap
                    locationAddress={overview.locationAddress}
                    mapCoords={finalMapCoords}
                    setShowModal={handleShowModal}
                  />
                </div>
              )}
            </div>
          </div>
        </section>
      )}
      {isMapCoordsValid && (
        <LocationModal
          show={showModal}
          handleClose={handleCloseModal}
          mapCoords={finalMapCoords}
          locationAddress={overview?.locationAddress}
        />
      )}

      <div className="back-button-container">
        <button
          className="back-button"
          onClick={() => navigate('/residential-projects')}
        >
          &larr; Back to Residential Projects
        </button>
      </div>
    </div>
  );
};

export default ProjectOverviewPage;

const NOMINATIM_URL = 'https://nominatim.openstreetmap.org/search';

export const projectOverviewApiMethods = {
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
