import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
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

const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

// Map project identifiers to living environment keys
const projectKeyMap = {
  foresta: 'foresta',
  panorama: 'panorama',
};

const LivingEnvironmentPage = () => {
  const { t, i18n } = useTranslation('livingEnvironment');
  const { projectIdentifier } = useParams();
  const [projectData, setProjectData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Get the living environment key based on project identifier
  const getProjectKey = () => {
    const lowerProjectId = projectIdentifier?.toLowerCase() || 'foresta';
    return projectKeyMap[lowerProjectId] || 'foresta';
  };

  const projectKey = getProjectKey();

  // Fetch project data from API
  useEffect(() => {
    const fetchProjectData = async () => {
      try {
        if (!projectIdentifier) {
          setLoading(false);
          return;
        }
        
        const response = await fetch(
          `${API_BASE_URL}/projects/${projectIdentifier}/overview`
        );
        if (response.ok) {
          const data = await response.json();
          setProjectData(data);
          
          // Apply project colors to CSS variables
          const colors = {
            '--primary-color': data.primaryColor || '#4c4d4f',
            '--tertiary-color': data.tertiaryColor || '#628db5',
            '--buyer-color': data.buyerColor || '#aab2a6',
          };
          
          Object.keys(colors).forEach(key => {
            document.documentElement.style.setProperty(key, colors[key]);
          });
        }
      } catch (error) {
        console.error('Failed to fetch project data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProjectData();

    return () => {
      // Clean up CSS variables
      document.documentElement.style.removeProperty('--primary-color');
      document.documentElement.style.removeProperty('--tertiary-color');
      document.documentElement.style.removeProperty('--buyer-color');
    };
  }, [projectIdentifier]);

  // Map amenity keys to icons
  const getAmenityIcon = (key) => {
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

  const buildAmenities = () => {
    try {
      const amenitiesPath = `amenities.${projectKey}`;
      const amenitiesData = t(amenitiesPath, {}, { returnObjects: true });
      
      if (!amenitiesData || typeof amenitiesData === 'string') {
        return [];
      }

      return Object.keys(amenitiesData).map(key => ({
        key,
        label: amenitiesData[key],
        icon: getAmenityIcon(key),
      }));
    } catch (error) {
      console.error('Error building amenities:', error);
      return [];
    }
  };

  if (loading) {
    return (
      <div className="living-environment-page">
        <div className="container">
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  const amenities = buildAmenities();

  // Get translations using the project key
  const headerData = t(`header.${projectKey}`, {}, { returnObjects: true });
  const proximityTitle = t(`proximity.${projectKey}`);
  const descriptionText = t(`description.${projectKey}`);
  const footerText = t(`footer.${projectKey}`);

  return (
    <div className="living-environment-page">
      <div className="container">
        {/* Header Section */}
        <section className="le-header-section">
          <h1 className="le-main-title">{headerData?.title || ''}</h1>
          <h2 className="le-subtitle">
            {headerData?.subtitle || ''}
          </h2>
          <h3 className="le-subtitle-last">
            {headerData?.subtitleLast || ''}
          </h3>
          <p className="le-tagline">
            {headerData?.tagline || ''}
          </p>
        </section>

        {/* Description Section */}
        <section className="le-description-section">
          <p className="le-description-text">
            {descriptionText}
          </p>
        </section>

        {/* Proximity Section */}
        <section className="le-proximity-section">
          <h2 className="le-proximity-title">
            {proximityTitle}
          </h2>

          <div className="le-amenities-grid">
            {amenities.map((amenity) => (
              <div key={amenity.key} className="le-amenity-box">
                <div className="le-amenity-icon">{amenity.icon}</div>
                <p className="le-amenity-label">{amenity.label}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Footer Section */}
        <section className="le-footer-section">
          <p className="le-footer-text">
            {footerText}
          </p>
        </section>
      </div>
    </div>
  );
};

export default LivingEnvironmentPage;
