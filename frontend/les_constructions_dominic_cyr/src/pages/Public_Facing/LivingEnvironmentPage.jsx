import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import '../../styles/Public_Facing/home.css';
import '../../styles/Public_Facing/living-environment.css';
import {
  FaSkiing,
  FaGolfBall,
  FaBicycle,
  FaMountain,
  FaTree,
  FaWineGlass,
  FaUtensils,
  FaShoppingCart,
  FaShoppingBag,
  FaHospital,
  FaSchool,
  FaSpa,
  FaCrosshairs,
  FaHotel,
} from 'react-icons/fa';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

const LivingEnvironmentPage = () => {
  const { t: tLivingEnv } = usePageTranslations('livingEnvironment');
  const { i18n } = useTranslation();
  const t = (key, defaultValue) => tLivingEnv(key, defaultValue);
  const navigate = useNavigate();
  const { projectIdentifier } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch living environment data from the dedicated endpoint
  useEffect(() => {
    const fetchData = async () => {
      try {
        setError(null);
        setLoading(true);

        if (!projectIdentifier) {
          setError('No project identifier provided');
          setLoading(false);
          return;
        }

        const lang = i18n.language || 'en';
        const url = `${API_BASE_URL}/projects/${projectIdentifier}/living-environment?lang=${lang}`;

        const response = await fetch(url, {
          headers: { Accept: 'application/json' },
          signal: AbortSignal.timeout(10000),
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(
            `Server error (${response.status}): ${errorText || 'Failed to load content'}`
          );
        }

        const responseData = await response.json();

        setData(responseData);

        // Apply project colors to CSS variables
        if (responseData) {
          const colors = {
            '--primary-color': responseData.primaryColor || '#4c4d4f',
            '--tertiary-color': responseData.tertiaryColor || '#628db5',
            '--buyer-color': responseData.buyerColor || '#aab2a6',
          };

          Object.keys(colors).forEach(key => {
            document.documentElement.style.setProperty(key, colors[key]);
          });
        }
      } catch (err) {
        if (err.name === 'TimeoutError' || err.name === 'AbortError') {
          setError(
            'Request timed out. Please check if the backend server is running.'
          );
        } else if (
          err.message.includes('Failed to fetch') ||
          err.message.includes('NetworkError')
        ) {
          setError(
            'Unable to connect to the backend. Please ensure the backend server is running.'
          );
        } else {
          setError(err.message || 'Failed to load page content.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();

    return () => {
      // Clean up CSS variables
      document.documentElement.style.removeProperty('--primary-color');
      document.documentElement.style.removeProperty('--tertiary-color');
      document.documentElement.style.removeProperty('--buyer-color');
    };
  }, [projectIdentifier, i18n.language]);

  // Log a warning if key header fields are missing to help diagnose empty banners
  useEffect(() => {
    if (!loading && data) {
      const missing = [];
      if (!data.headerTitle) missing.push('headerTitle');
      if (!data.headerSubtitle) missing.push('headerSubtitle');
      if (!data.headerSubtitleLast) missing.push('headerSubtitleLast');
      if (!data.headerTagline) missing.push('headerTagline');
      if (missing.length) {
        // eslint-disable-next-line no-console
        console.warn(
          `LivingEnvironmentPage: missing fields for project ${projectIdentifier}: ${missing.join(', ')}`
        );
      }
    }
  }, [loading, data, projectIdentifier]);

  // Map amenity keys to icons (all from react-icons/fa)
  const getAmenityIcon = key => {
    const iconMap = {
      ski: <FaSkiing />,
      golf: <FaGolfBall />,
      bike: <FaBicycle />,
      cycling: <FaBicycle />,
      bromont: <FaMountain />,
      sutton: <FaMountain />,
      yamaska: <FaTree />,
      vineyards: <FaWineGlass />,
      restaurants: <FaUtensils />,
      groceries: <FaShoppingCart />,
      stores: <FaShoppingBag />,
      hospitals: <FaHospital />,
      schools: <FaSchool />,
      spas: <FaSpa />,
      crosscountry: <FaCrosshairs />,
      hiking: <FaMountain />,
      lodging: <FaHotel />,
    };
    return iconMap[key] || <FaTree />;
  };

  // Convert strings to sentence case (only first letter uppercase)
  const toSentenceCase = raw => {
    if (!raw && raw !== 0) return '';
    const s = String(raw).trim().replace(/\s+/g, ' ');
    if (!s) return '';
    const lower = s.toLowerCase();
    return lower.charAt(0).toUpperCase() + lower.slice(1);
  };

  if (loading) {
    return (
      <div className="living-environment-page">
        <div className="container">
          <div className="loading-spinner">
            <p>Loading...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="living-environment-page">
        <div className="container">
          <div className="error-message">
            <h2>Unable to Load Content</h2>
            <p>{error || 'No content available for this project.'}</p>
            <div className="error-details">
              <p>
                <strong>Troubleshooting:</strong>
              </p>
              <ul>
                <li>Check if the backend server is running</li>
                <li>
                  Verify Docker containers are up:{' '}
                  <code>docker-compose -f docker-compose.local.yml up -d</code>
                </li>
                <li>
                  Make sure the database has been seeded with living environment
                  data
                </li>
                <li>
                  API Endpoint:{' '}
                  <code>
                    {API_BASE_URL}/projects/{projectIdentifier}
                    /living-environment
                  </code>
                </li>
              </ul>
            </div>
            <button
              onClick={() => window.history.back()}
              className="btn btn-secondary"
              style={{ marginTop: '2rem' }}
            >
              Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="living-environment-page">
      {/* Header Section: full-width with 5% side gutters */}
      <section className="le-header-section full-width-le">
        <h1 className="le-main-title" style={{ color: '#fff' }}>
          {toSentenceCase(data.headerTitle)}
        </h1>
        <h2 className="le-subtitle" style={{ color: '#fff' }}>
          {toSentenceCase(data.headerSubtitle)}
        </h2>
        <h3 className="le-subtitle-last" style={{ color: '#fff' }}>
          {toSentenceCase(data.headerSubtitleLast)}
        </h3>
        <p
          className="le-tagline"
          style={{ color: 'var(--tertiary-color, #aab2a6)' }}
        >
          {toSentenceCase(data.headerTagline)}
        </p>
      </section>

      <div className="container">
        {/* Description Section */}
        <section className="le-description-section">
          <p className="le-description-text">{data.descriptionText}</p>
        </section>

        {/* Proximity Section */}
        <section className="le-proximity-section">
          <h2 className="le-proximity-title">{data.proximityTitle}</h2>

          <div className="le-amenities-grid">
            {data.amenities &&
              data.amenities.map(amenity => (
                <div key={amenity.key} className="le-amenity-box">
                  <div
                    className="le-amenity-icon"
                    style={{ color: 'var(--primary-color, #4c4d4f)' }}
                  >
                    {getAmenityIcon(amenity.key)}
                  </div>
                  <p
                    className="le-amenity-label"
                    style={{ color: 'var(--primary-color, #4c4d4f)' }}
                  >
                    {amenity.label}
                  </p>
                </div>
              ))}
          </div>
        </section>

        {/* Footer Section */}
        <section className="le-footer-section">
          <p className="le-footer-text">{data.footerText}</p>
        </section>
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            marginTop: '2.5rem',
          }}
        >
          <button
            className="btn btn-secondary"
            style={{ maxWidth: '300px' }}
            onClick={() => navigate(`/projects/${projectIdentifier}/overview`)}
          >
            {t('backToProject', 'Back to Residential Project')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default LivingEnvironmentPage;
