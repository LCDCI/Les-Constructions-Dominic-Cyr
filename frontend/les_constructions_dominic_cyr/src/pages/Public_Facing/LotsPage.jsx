import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useTranslation } from 'react-i18next';
import {
  fetchLots,
  resolveProjectIdentifier,
} from '../../features/lots/api/lots';
import { projectApi } from '../../features/projects/api/projectApi';
import LotList from '../../features/lots/components/LotList';
import ProjectsFooter from '../../components/Footers/ProjectsFooter';
import '../../styles/lots.css';
import '../../styles/Public_Facing/foresta_LotsMap_Buttons.css';

const LotsPage = () => {
  const { t } = useTranslation(['lots', 'translation']);
  const { projectIdentifier: urlProjectIdentifier } = useParams();
  const navigate = useNavigate();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
    user,
  } = useAuth0();
  const resolvedProjectId = urlProjectIdentifier || resolveProjectIdentifier();

  const [lots, setLots] = useState([]);
  const [filteredLots, setFilteredLots] = useState([]);
  const [projectName, setProjectName] = useState('');
  const [projectColors, setProjectColors] = useState({
    primary: '#737373',
    secondary: '#F6F4F1',
    accent: '#545454',
  });

  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [sortConfig, setSortConfig] = useState({
    key: 'none',
    direction: 'asc',
  });
  const [selectedLot, setSelectedLot] = useState(null);

  const mapButtons = {
    1: { top: '28.5%', left: '24%' },
    2: { top: '36.5%', left: '24%' },
    3: { top: '44.5%', left: '24%' },
    4: { top: '51.5%', left: '24%' },
    5: { top: '59.5%', left: '24%' },
    6: { top: '67%', left: '24%' },
    7: { top: '74.5%', left: '24%' },
    8: { top: '82.5%', left: '24%' },
    9: { top: '90.5%', left: '24%' },
    33: { top: '88%', left: '41%' },
    34: { top: '88%', left: '51%' },
    35: { top: '87.5%', left: '61%' },
    36: { top: '82%', left: '70%' },
    37: { top: '74%', left: '70%' },
    38: { top: '66%', left: '70%' },
    39: { top: '60%', left: '72.5%' },
    40: { top: '48.5%', left: '72.5%' },
    41: { top: '45.5%', left: '65.5%' },
    42: { top: '65.5%', left: '52.5%' },
    43: { top: '73%', left: '53%' },
    45: { top: '47%', left: '41.5%' },
    46: { top: '39%', left: '41.5%' },
    47: { top: '30.5%', left: '41.5%' },
    48: { top: '22%', left: '41.5%' },
    49: { top: '17%', left: '36%' },
    50: { top: '15.5%', left: '27%' },
  };

  const roles = user?.['https://construction-api.loca/roles'] || [];
  const isOwner =
    isAuthenticated && roles.some(role => role.toUpperCase() === 'OWNER');
  const isPubliclyVisibleId = num =>
    (num >= 1 && num <= 9) ||
    (num >= 33 && num <= 43) ||
    (num >= 45 && num <= 50);

  useEffect(() => {
    let cancelled = false;
    const resolveAndFetch = async () => {
      setLoading(true);
      try {
        if (!resolvedProjectId) return;
        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;
        const projectData = await projectApi.getProjectById(
          resolvedProjectId,
          token
        );
        if (!cancelled && projectData) {
          setProjectName(projectData.projectName);
          setProjectColors({
            primary: projectData.primaryColor || '#737373',
            secondary: projectData.secondaryColor || '#F6F4F1',
            accent: projectData.accentColor || '#545454',
          });
        }
        const data = await fetchLots({
          projectIdentifier: resolvedProjectId,
          token,
        });
        if (!cancelled) setLots(data || []);
      } catch (err) {
        if (!cancelled) setError(err.message || t('errors.fetchFailed'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    if (!authLoading) resolveAndFetch();
    return () => {
      cancelled = true;
    };
  }, [
    resolvedProjectId,
    isAuthenticated,
    authLoading,
    getAccessTokenSilently,
    t,
  ]);

  useEffect(() => {
    if (!selectedLot) return;
    const handleKeyDown = e => {
      if (e.key === 'Escape' || e.key === 'Esc') {
        setSelectedLot(null);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [selectedLot]);

  useEffect(() => {
    let result = [...lots];
    if (!isOwner) {
      result = result.filter(lot => {
        const isAvailable = lot.lotStatus?.toUpperCase() === 'AVAILABLE';
        const numericId = Number(lot.id);
        const logicalId = !isNaN(numericId) ? numericId : lots.indexOf(lot) + 1;
        return isAvailable && isPubliclyVisibleId(logicalId);
      });
    } else if (statusFilter !== 'all') {
      result = result.filter(
        lot => lot.lotStatus?.toLowerCase() === statusFilter
      );
    }
    result = result.map(lot => {
      const numericId = Number(lot.id);
      const displayId = !isNaN(numericId) ? numericId : lots.indexOf(lot) + 1;
      return { ...lot, lotNumber: displayId.toString() };
    });
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        lot =>
          lot.civicAddress?.toLowerCase().includes(term) ||
          lot.lotNumber?.toLowerCase().includes(term)
      );
    }
    if (sortConfig.key !== 'none') {
      result.sort((a, b) => {
        const valA = a[sortConfig.key] || 0;
        const valB = b[sortConfig.key] || 0;
        return sortConfig.direction === 'asc'
          ? valA > valB
            ? 1
            : -1
          : valA < valB
            ? 1
            : -1;
      });
    }
    setFilteredLots(result);
  }, [searchTerm, statusFilter, sortConfig, lots, isOwner]);

  if (loading)
    return (
      <div className="lots-page">
        <div className="lots-content">{t('common.loading')}</div>
      </div>
    );

  return (
    <div className="lots-page">
      <div className="lots-content">
        <div
          className="lots-header-section"
          style={{ backgroundColor: projectColors.primary }}
        >
          {resolvedProjectId === 'proj-001-foresta' && (
            <p className="exclusive-tag">{t('foresta.exclusiveCount')}</p>
          )}
          {resolvedProjectId === 'proj-002-panorama' && (
            <p className="exclusive-tag" style={{ whiteSpace: 'pre-line' }}>{t('panorama.preTitle')}</p>
          )}
          <h1 style={{ color: projectColors.secondary }}>
            {resolvedProjectId === 'proj-002-panorama'
              ? t('panorama.headerTitle', 'Unités de PANØRAMA')
              : projectName
                ? t('header.projectLots', { name: projectName })
                : t('header.defaultTitle')}
            <span
              className="header-underline"
              style={{ backgroundColor: projectColors.accent }}
            ></span>
          </h1>
          {resolvedProjectId === 'proj-001-foresta' && (
            <p className="peace-tag">{t('foresta.peaceTagline')}</p>
          )}
          {resolvedProjectId === 'proj-002-panorama' && (
            <p className="peace-tag">{t('panorama.tagline')}</p>
          )}
          {/* Button moved below all content */}
        </div>

        {resolvedProjectId === 'proj-002-panorama' && (
          <div className="lots-image-section">
            <div className="map-wrapper">
              <img
                src="https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/panorama.png"
                alt="Panorama Map"
                className="phase-map-image"
                style={{ cursor: 'default' }}
              />
            </div>
            <p
              style={{
                textAlign: 'center',
                fontSize: '1.1rem',
                margin: '1.5rem auto',
                color: '#6b7280',
                maxWidth: '75%',
              }}
            >
              {t('comingSoon')}
            </p>
          </div>
        )}

        {resolvedProjectId === 'proj-001-foresta' && (
          <div className="lots-image-section">
            <div className="map-wrapper">
              <img
                src="https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-01-20/phase1.png"
                alt="Map"
                className="phase-map-image"
              />
              {filteredLots.map(lot => {
                const coords = mapButtons[lot.lotNumber];
                if (!coords) return null;
                return (
                  <button
                    key={lot.id}
                    className={`lot-map-button status-${lot.lotStatus?.toLowerCase()}`}
                    style={{ top: coords.top, left: coords.left }}
                    onClick={() => setSelectedLot(lot)}
                    title={`${t('common.lot')} ${lot.lotNumber}`}
                    aria-label={
                      lot.lotStatus
                        ? `${t('common.lot')} ${lot.lotNumber} (${t(
                            `status.${lot.lotStatus.toLowerCase()}`,
                            lot.lotStatus
                          )})`
                        : `${t('common.lot')} ${lot.lotNumber}`
                    }
                  />
                );
              })}
            </div>
            <div className="map-legend">
              <div className="legend-item">
                <span className="legend-dot status-available"></span>
                <span>{t('status.available')}</span>
              </div>
              <div className="legend-item">
                <span className="legend-dot status-reserved"></span>
                <span>{t('status.reserved')}</span>
              </div>
              <div className="legend-item">
                <span className="legend-dot status-sold"></span>
                <span>{t('status.sold')}</span>
              </div>
            </div>
          </div>
        )}

        {selectedLot && (
          <div
            className="lot-modal-overlay"
            onClick={() => setSelectedLot(null)}
          >
            <div
              className="lot-modal-content"
              onClick={e => e.stopPropagation()}
              style={{ borderTop: `5px solid ${projectColors.accent}` }}
              role="dialog"
              aria-modal="true"
              aria-labelledby="lot-details-title"
            >
              <button
                className="close-modal"
                onClick={() => setSelectedLot(null)}
                aria-label={t('common.close', 'Close')}
              >
                &times;
              </button>
              <h2 id="lot-details-title">
                {t('common.lot')} {selectedLot.lotNumber}
              </h2>
              <hr />
              <p>
                <strong>{t('modal.address')}:</strong>{' '}
                {selectedLot.civicAddress}
              </p>
              <p>
                <strong>{t('modal.area')}:</strong>{' '}
                {selectedLot.dimensionsSquareFeet} sqft (
                {selectedLot.dimensionsSquareMeters} m²)
              </p>
              <p>
                <strong>{t('modal.status')}:</strong>{' '}
                {t(`status.${selectedLot.lotStatus?.toLowerCase()}`)}
              </p>
              {isOwner && (
                <p>
                  <strong>{t('modal.price')}:</strong> $
                  {selectedLot.price?.toLocaleString()}
                </p>
              )}
            </div>
          </div>
        )}

        <div className="toolbar-section">
          <div className="search-box">
            <input
              type="text"
              className="search-input"
              placeholder={t('toolbar.searchPlaceholder')}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="filter-group">
            {isOwner && (
              <select
                className="filter-select"
                value={statusFilter}
                onChange={e => setStatusFilter(e.target.value)}
              >
                <option value="all">{t('filters.allStatuses')}</option>
                <option value="available">{t('status.available')}</option>
                <option value="sold">{t('status.sold')}</option>
                <option value="reserved">{t('status.reserved')}</option>
              </select>
            )}
            <select
              className="filter-select"
              onChange={e => {
                const [key, dir] = e.target.value.split('-');
                setSortConfig({ key, direction: dir });
              }}
            >
              <option value="none-asc">{t('filters.sortBy')}</option>
              {isOwner && (
                <option value="price-asc">{t('filters.priceLow')}</option>
              )}
              <option value="dimensionsSquareFeet-asc">
                {t('filters.sizeSmall')}
              </option>
              <option value="dimensionsSquareFeet-desc">
                {t('filters.sizeLarge')}
              </option>
            </select>
          </div>
        </div>

        <div className="list-section">
          {error ? (
            <div className="no-results">{error}</div>
          ) : filteredLots.length === 0 ? (
            <div className="no-results">{t('common.noResults')}</div>
          ) : (
            <LotList lots={filteredLots} isOwner={isOwner} />
          )}
        </div>
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            margin: '2.5rem 0',
          }}
        >
          <button
            className="btn btn-secondary"
            style={{ maxWidth: '300px' }}
            onClick={() => navigate(`/projects/${resolvedProjectId}/overview`)}
          >
            {t('backToProject', 'Back to Residential Project')}
          </button>
        </div>
      </div>
      <ProjectsFooter projectId={resolvedProjectId} />
    </div>
  );
};

export default LotsPage;
