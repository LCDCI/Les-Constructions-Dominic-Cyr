import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/Public_Facing/realizations.css';
import Footer from '../../components/Footers/ProjectsFooter';

const RealizationsPage = () => {
  const navigate = useNavigate();
  const [realizations, setRealizations] = useState([]);
  const [filteredRealizations, setFilteredRealizations] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

  useEffect(() => {
    fetchRealizations();
  }, []);

  useEffect(() => {
    const filterRealizations = () => {
      if (!searchTerm.trim()) {
        setFilteredRealizations(realizations);
        return;
      }

      const filtered = realizations.filter(
        realization =>
          realization.realizationName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          realization.location.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredRealizations(filtered);
    };

    filterRealizations();
  }, [searchTerm, realizations]);

  const fetchRealizations = async () => {
    try {
      const response = await fetch(`${apiBaseUrl}/realizations`);
      const data = await response.json();
      setRealizations(data);
      setFilteredRealizations(data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to fetch realizations:', error);
      setLoading(false);
    }
  };

  const getImageUrl = imageIdentifier => {
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handleViewRealization = realizationId => {
    navigate(`/realizations/${realizationId}`);
  };

  if (loading) {
    return (
      <div className="realizations-page">
        <div className="realizations-content">
          <div className="realizations-container">
            <p
              style={{ textAlign: 'center', padding: '5%', fontSize: '1.2rem' }}
            >
              Loading realizations...
            </p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="realizations-page">
      <div className="realizations-content">
        <div className="realizations-container">
          <div className="realizations-header">
            <h1>Realizations</h1>
          </div>

          <div className="realizations-filter">
            <div className="search-container">
              <input
                type="text"
                className="search-input"
                placeholder="Search realizations by name or location..."
                aria-label="Search realizations by name or location"
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div className="realizations-grid">
            {filteredRealizations.length > 0 ? (
              filteredRealizations.map(realization => (
                <div key={realization.realizationId} className="realization-card">
                  <div className="realization-image-container">
                    <img
                      src={getImageUrl(realization.imageIdentifier)}
                      alt={realization.realizationName}
                      className="realization-image"
                    />
                  </div>
                  <h2 className="realization-title">{realization.realizationName}</h2>
                  <p className="realization-location">{realization.location}</p>
                  <p className="realization-description">{realization.description}</p>
                  <div className="realization-details">
                    <span className="realization-detail-item">
                      🛏️ {realization.numberOfBedrooms} Bedrooms
                    </span>
                    <span className="realization-detail-item">
                      🛁 {realization.numberOfBathrooms} Bathrooms
                    </span>
                    <span className="realization-detail-item">
                      🚪 {realization.numberOfRooms} Rooms
                    </span>
                    <span className="realization-detail-item">
                      📅 Built in {realization.constructionYear}
                    </span>
                  </div>
                  <button
                    className="realization-button"
                    onClick={() => handleViewRealization(realization.realizationId)}
                  >
                    View this realization
                  </button>
                </div>
              ))
            ) : (
              <div className="no-results">
                <p>No realizations found matching &quot;{searchTerm}&quot;</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RealizationsPage;
