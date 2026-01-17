import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLots, resolveProjectIdentifier } from '../features/lots/api/lots';
import { projectApi } from '../features/projects/api/projectApi';
import '../styles/lots.css';
import Footer from '../components/Footers/ProjectsFooter';

const LotsPage = () => {
  const { projectIdentifier: urlProjectIdentifier } = useParams();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
    useAuth0();
  const [lots, setLots] = useState([]);
  const [filteredLots, setFilteredLots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [projectIdentifier, setProjectIdentifier] = useState(null);

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  useEffect(() => {
    let cancelled = false;

    const resolveProjectAndFetch = async () => {
      setLoading(true);
      setError('');
      try {
        // Prefer URL param, then env, then auto-discover a project
        let resolved = urlProjectIdentifier || resolveProjectIdentifier();

        if (!resolved) {
          const projects = await projectApi.getAllProjects();
          const first = Array.isArray(projects) ? projects[0] : null;
          // Prefer numeric projectId when available to satisfy backends that expect a bigint path variable
          resolved =
            first?.projectId || first?.id || first?.projectIdentifier || null;
        }

        if (!resolved) {
          if (!cancelled) {
            setError(
              'No project found. Open via /projects/{projectIdentifier}/lots or create a project first.'
            );
          }
          return;
        }

        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {
            console.warn('Could not get token for lots, continuing without', tokenErr);
          }
        }

        if (!cancelled) {
          setProjectIdentifier(resolved);
        }

        await fetchAvailableLots(resolved, token, () => cancelled);
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Failed to fetch lots');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    resolveProjectAndFetch();

    return () => {
      cancelled = true;
    };
  }, [urlProjectIdentifier, isAuthenticated, authLoading]);

  useEffect(() => {
    filterLots();
  }, [searchTerm, lots]);

  const fetchAvailableLots = async (projectId, token, shouldCancel) => {
    try {
      setLoading(true);
      setError('');
      const data = await fetchLots({ projectIdentifier: projectId, token });
      const availableLots = data.filter(lot => lot.lotStatus === 'AVAILABLE');
      if (shouldCancel?.()) return;
      setLots(availableLots);
      setFilteredLots(availableLots);
    } catch (err) {
      console.error('Failed to fetch lots:', err);
      if (!shouldCancel?.()) {
        const message = err.message || 'Failed to fetch lots';
        if (message.includes('status 401')) {
          setError('Unauthorized. Please sign in to view lots.');
        } else {
          setError(message);
        }
      }
    } finally {
      if (!shouldCancel?.()) {
        setLoading(false);
      }
    }
  };

  const filterLots = () => {
    if (!searchTerm.trim()) {
      setFilteredLots(lots);
      return;
    }

    const term = searchTerm.toLowerCase();
    const filtered = lots.filter(lot =>
      Object.values(lot || {}).some(value => {
        if (value === null || value === undefined) return false;
        if (typeof value === 'string') return value.toLowerCase().includes(term);
        if (typeof value === 'number')
          return value.toString().toLowerCase().includes(term);
        return false;
      })
    );
    setFilteredLots(filtered);
  };

  const getImageUrl = imageIdentifier => {
    return `${filesServiceUrl}/files/${imageIdentifier}`;
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

  const formatLabel = key => {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/[_-]/g, ' ')
      .replace(/\s+/g, ' ')
      .trim()
      .replace(/^./, c => c.toUpperCase());
  };

  const formatValue = value => {
    if (value === null || value === undefined || value === '') return '—';
    if (Array.isArray(value)) return value.length ? value.join(', ') : '—';
    if (typeof value === 'object') return JSON.stringify(value);
    return value;
  };

  const renderField = (label, value, key) => (
    <div className="lot-field" key={key || label}>
      <div className="lot-field-label">{label}</div>
      <div className="lot-field-value">{value ?? '—'}</div>
    </div>
  );

  if (loading) {
    return (
      <div className="lots-page">
        <div className="lots-content">
          <div className="lots-container">
            <p
              style={{ textAlign: 'center', padding: '5%', fontSize: '1.2rem' }}
            >
              Loading lots...
            </p>
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
            {error ? (
              <div className="no-results">
                <p>{error}</p>
              </div>
            ) : filteredLots.length > 0 ? (
              <div className="lots-list">
                {filteredLots.map(lot => {
                  const extraFields = Object.entries(lot || {}).filter(
                    ([key]) =>
                      ![
                        'lotId',
                        'location',
                        'dimensions',
                        'price',
                        'lotStatus',
                        'imageIdentifier',
                        'projectIdentifier',
                      ].includes(key)
                  );

                  const statusClass = `status-${String(lot?.lotStatus || 'unknown').toLowerCase()}`;

                  return (
                    <div key={lot.lotId || lot.location} className="lot-row">
                      <div className="lot-row-header">
                        <div className="lot-row-titles">
                          <h2 className="lot-title">{lot.location || 'Unnamed lot'}</h2>
                          <div className="lot-subtitle">
                            <span>Lot ID: {lot.lotId || '—'}</span>
                            <span>
                              Project: {lot.projectIdentifier || projectIdentifier || '—'}
                            </span>
                          </div>
                        </div>
                        <span className={`lot-status-badge ${statusClass}`}>
                          {lot.lotStatus || 'UNKNOWN'}
                        </span>
                      </div>

                      <div className="lot-row-body">
                        {renderField('Location', lot.location, 'location')}
                        {renderField('Dimensions', lot.dimensions, 'dimensions')}
                        {renderField('Price', formatPrice(lot.price), 'price')}
                        {renderField('Status', lot.lotStatus || '—', 'lotStatus')}
                        {renderField(
                          'Project Identifier',
                          lot.projectIdentifier || projectIdentifier || '—',
                          'projectIdentifier'
                        )}
                        {renderField(
                          'Image',
                          lot.imageIdentifier ? (
                            <img
                              src={getImageUrl(lot.imageIdentifier)}
                              alt={lot.location || 'Lot photo'}
                              className="lot-thumb"
                            />
                          ) : (
                            '—'
                          ),
                          'image'
                        )}

                        {extraFields.map(([key, value]) =>
                          renderField(formatLabel(key), formatValue(value), key)
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="no-results">
                <p>
                  {searchTerm
                    ? <>No lots found matching &quot;{searchTerm}&quot;</>
                    : lots.length === 0
                      ? <>No available lots at the moment</>
                      : <>No lots found</>}
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default LotsPage;
