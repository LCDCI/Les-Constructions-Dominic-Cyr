/* eslint-disable no-console */
import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { FiPlus } from 'react-icons/fi';
import {
  fetchLots,
  resolveProjectIdentifier,
  createLot,
  updateLot,
  deleteLot,
} from '../../features/lots/api/lots';
import { projectApi } from '../../features/projects/api/projectApi';
import LotList from '../../features/lots/components/LotList';
import LotFormModal from '../../features/lots/components/LotFormModal';
import ConfirmationModal from '../../features/lots/components/ConfirmationModal';
import '../../styles/lots.css';

const LotsPage = () => {
  const { projectIdentifier: urlProjectIdentifier } = useParams();
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

        // --- 2. Fetch Lots (Using the token!) ---
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

    if (!authLoading) resolveAndFetch();
    return () => {
      cancelled = true;
    };
  }, [
    urlProjectIdentifier,
    isAuthenticated,
    authLoading,
    getAccessTokenSilently,
  ]);

  // Lot management functions
  const handleAddLot = async () => {
    setCurrentLot(null);
    // Get fresh token for modal
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
    // Get fresh token for modal
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
        throw new Error(
          'Project identifier is missing. Please reopen this page from a project.'
        );
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await createLot({
        projectIdentifier: resolved,
        lotData,
        token,
      });

      // Refresh lots list
      const updatedLots = await fetchLots({
        projectIdentifier: resolved,
        token,
      });
      setLots(updatedLots);
      setIsAddModalOpen(false);
    } catch (err) {
      alert('Error creating lot: ' + (err.message || 'Unknown error'));
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
        throw new Error(
          'Project identifier is missing. Please reopen this page from a project.'
        );
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await updateLot({
        projectIdentifier: resolved,
        lotId: currentLot.lotId,
        lotData,
        token,
      });

      // Refresh lots list
      const updatedLots = await fetchLots({
        projectIdentifier: resolved,
        token,
      });
      setLots(updatedLots);
      setIsEditModalOpen(false);
      setCurrentLot(null);
    } catch (err) {
      alert('Error updating lot: ' + (err.message || 'Unknown error'));
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
        throw new Error(
          'Project identifier is missing. Please reopen this page from a project.'
        );
      }
      const token = isAuthenticated ? await getAccessTokenSilently() : null;

      await deleteLot({
        projectIdentifier: resolved,
        lotId: currentLot.lotId,
        token,
      });

      // Refresh lots list
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
    if (!isOwner) {
      result = result.filter(
        lot => lot.lotStatus?.toUpperCase() === 'AVAILABLE'
      );
    } else if (statusFilter !== 'all') {
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
  }, [searchTerm, statusFilter, sortConfig, lots, isOwner]);

  if (loading)
    return (
      <div className="lots-page">
        <div className="lots-content">Loading Foresta project data...</div>
      </div>
    );

  return (
    <div className="lots-page">
      <div className="lots-content">
        <div className="lots-header-section">
          <div className="header-content">
            <h1>{projectName ? `${projectName}'s Lots` : 'Project Lots'}</h1>
            {isOwner && (
              <button
                className="add-lot-btn"
                onClick={handleAddLot}
                disabled={loading}
              >
                <FiPlus size={18} />
                Add Lot
              </button>
            )}
          </div>
        </div>

        <div className="toolbar-section">
          <div className="search-box">
            <input
              type="text"
              className="search-input"
              placeholder="Search by address or lot..."
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
                <option value="all">All Statuses</option>
                <option value="available">Available</option>
                <option value="sold">Sold</option>
                <option value="pending">Pending</option>
              </select>
            )}
            <select
              className="filter-select"
              onChange={e => {
                const [key, dir] = e.target.value.split('-');
                setSortConfig({ key, direction: dir });
              }}
            >
              <option value="none-asc">Sort By</option>
              {isOwner && <option value="price-asc">Price: Low to High</option>}
              {isOwner && (
                <option value="price-desc">Price: High to Low</option>
              )}
              <option value="dimensionsSquareFeet-asc">Size: Smallest</option>
              <option value="dimensionsSquareFeet-desc">Size: Largest</option>
            </select>
          </div>
        </div>

        <div className="list-section">
          {error ? (
            <div className="no-results">{error}</div>
          ) : (
            <LotList
              lots={filteredLots}
              isOwner={isOwner}
              onEdit={isOwner ? handleEditLot : undefined}
              onDelete={isOwner ? handleDeleteLot : undefined}
            />
          )}
        </div>
      </div>

      {/* Modals */}
      {isOwner && (
        <>
          <LotFormModal
            isOpen={isAddModalOpen}
            onClose={handleCloseModals}
            onSubmit={handleCreateLot}
            token={currentToken}
            isSubmitting={isSubmitting}
            title="Add New Lot"
          />

          <LotFormModal
            isOpen={isEditModalOpen}
            onClose={handleCloseModals}
            onSubmit={handleUpdateLot}
            lot={currentLot}
            token={currentToken}
            isSubmitting={isSubmitting}
            title="Edit Lot"
          />

          <ConfirmationModal
            isOpen={isDeleteModalOpen}
            onClose={handleCloseModals}
            onConfirm={handleConfirmDelete}
            title="Delete Lot"
            message={`Are you sure you want to delete "${currentLot?.lotNumber}"? This action cannot be undone.`}
            confirmText="Delete"
            cancelText="Cancel"
            isDestructive={true}
            isSubmitting={isSubmitting}
          />
        </>
      )}
    </div>
  );
};

export default LotsPage;
