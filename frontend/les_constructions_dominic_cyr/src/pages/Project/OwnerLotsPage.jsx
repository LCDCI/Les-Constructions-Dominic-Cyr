/* eslint-disable no-console */
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FiPlus, FiArrowLeft } from 'react-icons/fi';
import {
  fetchLots,
  resolveProjectIdentifier,
  createLot,
  updateLot,
  deleteLot,
} from '../../features/lots/api/lots';
import { projectApi } from '../../features/projects/api/projectApi';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import OwnerLotList from '../../features/lots/components/OwnerLotList';
import OwnerLotFormModal from '../../features/lots/components/OwnerLotFormModal';
import ConfirmationModal from '../../features/lots/components/ConfirmationModal';
import '../../styles/lots.css';

const OwnerLotsPage = () => {
  const { projectIdentifier: urlProjectIdentifier } = useParams();
  const navigate = useNavigate();
  const { t, isLoading: translationsLoading } =
    usePageTranslations('ownerLots');

  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
    user,
  } = useAuth0();

  const [lots, setLots] = useState([]);
  const [filteredLots, setFilteredLots] = useState([]);
  const [projectName, setProjectName] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [sortConfig, setSortConfig] = useState({
    key: 'none',
    direction: 'asc',
  });

  // Modal states
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [currentLot, setCurrentLot] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentToken, setCurrentToken] = useState(null);

  const roles = user?.['https://construction-api.loca/roles'] || [];
  const isOwner =
    isAuthenticated && roles.some(role => role.toUpperCase() === 'OWNER');

  // Redirect non-owners
  useEffect(() => {
    if (!authLoading && !isOwner) {
      navigate('/unauthorized');
    }
  }, [authLoading, isOwner, navigate]);

  useEffect(() => {
    let cancelled = false;

    const resolveAndFetch = async () => {
      setLoading(true);
      try {
        const resolved = urlProjectIdentifier || resolveProjectIdentifier();
        if (!resolved) return;

        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        setCurrentToken(token);

        try {
          const projectData = await projectApi.getProjectById(resolved, token);
          if (!cancelled) setProjectName(projectData.projectName);
        } catch (e) {
          //
        }

        const data = await fetchLots({ projectIdentifier: resolved, token });

        if (!cancelled) {
          setLots(data);
          setFilteredLots(data);
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to fetch');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (!authLoading && isOwner) resolveAndFetch();
    return () => {
      cancelled = true;
    };
  }, [
    urlProjectIdentifier,
    isAuthenticated,
    authLoading,
    getAccessTokenSilently,
    isOwner,
  ]);

  // Lot management functions
  const handleAddLot = async () => {
    setCurrentLot(null);
    if (isAuthenticated) {
      try {
        const token = await getAccessTokenSilently();
        setCurrentToken(token);
      } catch (err) {
        console.error('Failed to get token:', err);
      }
    }
    setIsAddModalOpen(true);
  };

  const handleEditLot = async lot => {
    setCurrentLot(lot);
    if (isAuthenticated) {
      try {
        const token = await getAccessTokenSilently();
        setCurrentToken(token);
      } catch (err) {
        console.error('Failed to get token:', err);
      }
    }
    setIsEditModalOpen(true);
  };

  const handleDeleteLot = lot => {
    setCurrentLot(lot);
    setIsDeleteModalOpen(true);
  };

  const handleCreateLot = async lotData => {
    setIsSubmitting(true);
    setError('');
    try {
      const resolved = urlProjectIdentifier || resolveProjectIdentifier();
      if (!resolved) {
        throw new Error(t('errors.projectMissing'));
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await createLot({
        projectIdentifier: resolved,
        lotData,
        token,
      });

      const updatedLots = await fetchLots({
        projectIdentifier: resolved,
        token,
      });
      setLots(updatedLots);
      setIsAddModalOpen(false);
    } catch (err) {
      alert(
        t('errors.errorCreating') + (err.message || t('errors.unknownError'))
      );
      setError(err.message || 'Failed to create lot');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdateLot = async lotData => {
    if (!currentLot) return;

    setIsSubmitting(true);
    setError('');
    try {
      const resolved = urlProjectIdentifier || resolveProjectIdentifier();
      if (!resolved) {
        throw new Error(t('errors.projectMissing'));
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await updateLot({
        projectIdentifier: resolved,
        lotId: currentLot.lotId,
        lotData,
        token,
      });

      const updatedLots = await fetchLots({
        projectIdentifier: resolved,
        token,
      });
      setLots(updatedLots);
      setIsEditModalOpen(false);
      setCurrentLot(null);
    } catch (err) {
      alert(
        t('errors.errorUpdating') + (err.message || t('errors.unknownError'))
      );
      setError(err.message || 'Failed to update lot');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!currentLot) return;

    setIsSubmitting(true);
    setError('');
    try {
      const resolved = urlProjectIdentifier || resolveProjectIdentifier();
      if (!resolved) {
        throw new Error(t('errors.projectMissing'));
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await deleteLot({
        projectIdentifier: resolved,
        lotId: currentLot.lotId,
        token,
      });

      const updatedLots = await fetchLots({
        projectIdentifier: resolved,
        token,
      });
      setLots(updatedLots);
      setIsDeleteModalOpen(false);
      setCurrentLot(null);
    } catch (err) {
      setError(err.message || 'Failed to delete lot');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseModals = () => {
    if (!isSubmitting) {
      setIsAddModalOpen(false);
      setIsEditModalOpen(false);
      setIsDeleteModalOpen(false);
      setCurrentLot(null);
    }
  };

  // Filtering and Sorting Logic
  useEffect(() => {
    let result = [...lots];

    if (statusFilter !== 'all') {
      result = result.filter(
        lot => lot.lotStatus?.toLowerCase() === statusFilter
      );
    }
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
  }, [searchTerm, statusFilter, sortConfig, lots]);

  if (authLoading || translationsLoading || (!isOwner && !authLoading)) {
    return (
      <div className="lots-page">
        <div className="lots-content">{t('loading')}</div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="lots-page">
        <div className="lots-content">{t('loadingLots')}</div>
      </div>
    );
  }

  return (
    <div className="lots-page">
      <div className="lots-content">
        <div className="lots-header-section">
          <div className="header-content">
            <button
              className="back-btn"
              onClick={() => navigate('/projects')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                marginRight: '16px',
                padding: '8px 16px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                background: 'white',
                cursor: 'pointer',
              }}
            >
              <FiArrowLeft size={18} />
              {t('backToProjects')}
            </button>
            <h1>
              {projectName
                ? `${projectName} - ${t('pageTitle')}`
                : t('pageTitle')}
            </h1>
            <button
              className="add-lot-btn"
              onClick={handleAddLot}
              disabled={loading}
            >
              <FiPlus size={18} />
              {t('addLot')}
            </button>
          </div>
        </div>

        <div className="toolbar-section">
          <div className="search-box">
            <input
              type="text"
              className="search-input"
              placeholder={t('searchPlaceholder')}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <select
              className="filter-select"
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
            >
              <option value="all">{t('filters.allStatuses')}</option>
              <option value="available">{t('filters.available')}</option>
              <option value="reserved">{t('filters.reserved')}</option>
              <option value="sold">{t('filters.sold')}</option>
              <option value="pending">{t('filters.pending')}</option>
            </select>
            <select
              className="filter-select"
              onChange={e => {
                const [key, dir] = e.target.value.split('-');
                setSortConfig({ key, direction: dir });
              }}
            >
              <option value="none-asc">{t('sort.sortBy')}</option>
              <option value="price-asc">{t('sort.priceLowHigh')}</option>
              <option value="price-desc">{t('sort.priceHighLow')}</option>
              <option value="dimensionsSquareFeet-asc">
                {t('sort.sizeSmallest')}
              </option>
              <option value="dimensionsSquareFeet-desc">
                {t('sort.sizeLargest')}
              </option>
            </select>
          </div>
        </div>

        <div className="list-section">
          {error ? (
            <div className="no-results">{error}</div>
          ) : (
            <OwnerLotList
              lots={filteredLots}
              onEdit={handleEditLot}
              onDelete={handleDeleteLot}
            />
          )}
        </div>
      </div>

      {/* Modals */}
      <OwnerLotFormModal
        isOpen={isAddModalOpen}
        onClose={handleCloseModals}
        onSubmit={handleCreateLot}
        token={currentToken}
        isSubmitting={isSubmitting}
      />

      <OwnerLotFormModal
        isOpen={isEditModalOpen}
        onClose={handleCloseModals}
        onSubmit={handleUpdateLot}
        lot={currentLot}
        token={currentToken}
        isSubmitting={isSubmitting}
      />

      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        onClose={handleCloseModals}
        onConfirm={handleConfirmDelete}
        title={t('modal.deleteLotTitle')}
        message={t('modal.deleteLotMessage').replace(
          '{{lotNumber}}',
          currentLot?.lotNumber || ''
        )}
        confirmText={t('modal.delete')}
        cancelText={t('modal.cancel')}
        isDestructive={true}
        isSubmitting={isSubmitting}
      />
    </div>
  );
};

export default OwnerLotsPage;
