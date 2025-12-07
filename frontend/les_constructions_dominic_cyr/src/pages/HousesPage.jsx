import { useState, useEffect } from 'react';
import '../styles/houses.css';
import Footer from '../components/AppFooter';

const HousesPage = () => {
  const [houses, setHouses] = useState([]);
  const [filteredHouses, setFilteredHouses] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

  useEffect(() => {
    fetchHouses();
  }, []);

  useEffect(() => {
    const filterHouses = () => {
      if (!searchTerm.trim()) {
        setFilteredHouses(houses);
        return;
      }

      const filtered = houses.filter(
        house =>
          house.houseName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          house.location.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredHouses(filtered);
    };

    filterHouses();
  }, [searchTerm, houses]);

  const fetchHouses = async () => {
    try {
      const response = await fetch(`${apiBaseUrl}/houses`);
      const data = await response.json();
      setHouses(data);
      setFilteredHouses(data);
      setLoading(false);
    } catch (error) {
      setLoading(false);
    }
  };

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) {
      return '/placeholder-house.png';
    }
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handleImageError = e => {
    e.target.src = '/placeholder-house.png';
  };

  const handleViewHouse = houseId => {
    window.location.href = `/houses/${houseId}`;
  };

  if (loading) {
    return (
      <div className="houses-page">
        <div className="houses-content">
          <div className="houses-container">
            <p
              style={{ textAlign: 'center', padding: '5%', fontSize: '1.2rem' }}
            >
              Loading houses...
            </p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="houses-page">
      <div className="houses-content">
        <div className="houses-container">
          <div className="houses-header">
            <h1>Houses</h1>
          </div>

          <div className="houses-filter">
            <div className="search-container">
              <input
                type="text"
                className="search-input"
                placeholder="Search houses by name or location..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div className="houses-grid">
            {filteredHouses.length > 0 ? (
              filteredHouses.map(house => (
                <div key={house.houseId} className="house-card">
                  <div className="house-image-container">
                    <img
                      src={getImageUrl(house.imageIdentifier)}
                      alt={house.houseName}
                      className="house-image"
                      onError={handleImageError}
                    />
                  </div>
                  <h2 className="house-title">{house.houseName}</h2>
                  <p className="house-location">{house.location}</p>
                  <p className="house-description">{house.description}</p>
                  <div className="house-details">
                    <span className="house-detail-item">
                      🛏️ {house.numberOfBedrooms} Bedrooms
                    </span>
                    <span className="house-detail-item">
                      🛁 {house.numberOfBathrooms} Bathrooms
                    </span>
                    <span className="house-detail-item">
                      🚪 {house.numberOfRooms} Rooms
                    </span>
                    <span className="house-detail-item">
                      📅 Built in {house.constructionYear}
                    </span>
                  </div>
                  <button
                    className="house-button"
                    onClick={() => handleViewHouse(house.houseId)}
                  >
                    View this house
                  </button>
                </div>
              ))
            ) : (
              <div className="no-results">
                <p>No houses found matching &quot;{searchTerm}&quot;</p>
              </div>
            )}
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default HousesPage;
