import { useState, useEffect, useCallback } from 'react';
import { fetchLots } from '../features/lots/api/lots';
import '../styles/lots.css';
import Footer from '../components/AppFooter';

const LotsPage = () => {
  const [lots, setLots] = useState([]);
  const [filteredLots, setFilteredLots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  const fetchAvailableLots = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchLots();
      const allLots = Array.isArray(data) ? data : [];
      // Filter to show only AVAILABLE lots
      const availableLots = allLots.filter(lot => lot.lotStatus === 'AVAILABLE');
      setLots(availableLots);
      setFilteredLots(availableLots);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error('Failed to fetch lots:', err);
      setError(err && err.message ? err.message : 'Failed to load lots');
      setLots([]);
      setFilteredLots([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAvailableLots();
  }, [fetchAvailableLots]);

  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredLots(lots);
      return;
    }

    const filtered = lots.filter(lot =>
      lot.location.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredLots(filtered);
  }, [searchTerm, lots]);

  const fetchAvailableLots = async () => {
    try {
      const data = await fetchLots();
      // Filter to show only AVAILABLE lots
      const availableLots = data.filter(lot => lot.lotStatus === 'AVAILABLE');
      setLots(availableLots);
      setFilteredLots(availableLots);
      setLoading(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('Failed to fetch lots:', error);
      setLoading(false);
    }
  };

  // filtering is handled inside the effect above

  const getImageUrl = imageIdentifier => {
    if (!imageIdentifier) {
      return '/placeholder-lot.png';
    }
    return `${filesServiceUrl}/files/${imageIdentifier}`;
  };

  const handleImageError = e => {
    e.target.src = '/placeholder-lot.png';
  };

  const formatPrice = price => {
    if (price === null || price === undefined) return '—';
    try {
      return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency: 'CAD',
      }).format(price);
    } catch (e) {
      return price.toString();
    }
  };

  if (loading) {
    return (
      <div className="lots-page">
        <div className="lots-content">
          <div className="lots-container">
            <p className="lots-loading">Loading lots...</p>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  if (error) {
    return (
      <div className="lots-page">
        <div className="lots-content">
          <div className="lots-container">
            <div className="lots-error">
              <p>Unable to load lots: {error}</p>
              <button className="retry-button" onClick={fetchAvailableLots}>
                Retry
              </button>
            </div>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="lots-page">
      <div className="lots-content">
        <div className="lots-container">
          <div className="lots-header">
            <h1>Available Lots</h1>
          </div>

          <div className="lots-filter">
            <div className="search-container">
              <input
                type="text"
                className="search-input"
                placeholder="Search lots by location..."
                aria-label="Search lots by location"
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div className="lots-grid">
            {filteredLots.length > 0 ? (
              filteredLots.map(lot => (
                <div key={lot.lotId} className="lot-card">
                  <div className="lot-image-container">
                    <img
                      src={getImageUrl(lot.imageIdentifier)}
                      alt={lot.location}
                      className="lot-image"
                      onError={handleImageError}
                    />
                  </div>
                  <h2 className="lot-title">{lot.location}</h2>
                  <div className="lot-details">
                    <div className="lot-detail-item">
                      <strong>Size:</strong> {lot.dimensions}
                    </div>
                    <div className="lot-detail-item">
                      <strong>Price:</strong> {formatPrice(lot.price)}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="no-results">
                <p>No lots found matching &quot;{searchTerm}&quot;</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default LotsPage;
