import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLots, createLot } from '../../lots/api/lots';
import { uploadFile } from '../../files/api/filesApi';
import { fetchActiveCustomers } from '../../users/api/usersApi';
import PropTypes from 'prop-types';
import '../../../styles/Project/create-project.css';

// Hardcoded translations for LotSelector
const lotTranslations = {
  en: {
    searchPlaceholder: 'Search lots by location...',
    selectedCount: 'Selected Lots',
    createNew: '+ Create New Lot',
    photo: 'Photo',
    cancelCreate: 'Cancel',
    createLotTitle: 'Create New Lot',
    lotNumber: 'Lot Number',
    lotNumberPlaceholder: 'e.g., Lot-001',
    civicAddress: 'Civic Address',
    civicAddressPlaceholder: 'Enter civic address',
    dimensionsSquareFeet: 'Dimensions (sq ft)',
    dimensionsSquareFeetPlaceholder: 'e.g., 5000',
    dimensionsSquareMeters: 'Dimensions (sq m)',
    dimensionsSquareMetersPlaceholder: 'e.g., 465',
    price: 'Price',
    pricePlaceholder: 'Enter price',
    status: 'Status',
    assignedCustomer: 'Assigned Customer',
    assignedCustomerPlaceholder: 'Select a customer (optional)',
    createButton: 'Create Lot',
    creating: 'Creating...',
    cancel: 'Cancel',
    loading: 'Loading lots...',
    noLotsFound: 'No lots found matching your search',
    noLotsAvailable: 'No lots available',
    removeImage: 'Remove Image',
    invalidImageType: 'Invalid image type. Allowed: PNG, JPG, JPEG, WEBP',
    loadingCustomers: 'Loading customers...',
  },
  fr: {
    searchPlaceholder: 'Rechercher des lots par emplacement...',
    selectedCount: 'Lots sélectionnés',
    createNew: '+ Créer un nouveau lot',
    photo: 'Photo',
    cancelCreate: 'Annuler',
    createLotTitle: 'Créer un nouveau lot',
    lotNumber: 'Numéro de lot',
    lotNumberPlaceholder: 'ex: Lot-001',
    civicAddress: 'Adresse civique',
    civicAddressPlaceholder: "Entrez l'adresse civique",
    dimensionsSquareFeet: 'Dimensions (pi²)',
    dimensionsSquareFeetPlaceholder: 'ex: 5000',
    dimensionsSquareMeters: 'Dimensions (m²)',
    dimensionsSquareMetersPlaceholder: 'ex: 465',
    price: 'Prix',
    pricePlaceholder: 'Entrez le prix',
    status: 'Statut',
    assignedCustomer: 'Client assigné',
    assignedCustomerPlaceholder: 'Sélectionner un client (optionnel)',
    createButton: 'Créer le lot',
    creating: 'Création en cours...',
    cancel: 'Annuler',
    loading: 'Chargement des lots...',
    noLotsFound: 'Aucun lot trouvé correspondant à votre recherche',
    noLotsAvailable: 'Aucun lot disponible',
    removeImage: "Supprimer l'image",
    invalidImageType: "Type d'image invalide. Autorisés: PNG, JPG, JPEG, WEBP",
    loadingCustomers: 'Chargement des clients...',
  },
};

const LotSelector = ({
  currentLanguage,
  selectedLots,
  onChange,
  onLotCreated,
  projectIdentifier,
  // Props to persist lot form state across language switches
  lotFormData,
  onLotFormDataChange,
  lotFormImageFile,
  onLotFormImageFileChange,
  lotFormImagePreviewUrl,
  onLotFormImagePreviewUrlChange,
  showLotCreateForm,
  onShowLotCreateFormChange,
}) => {
  const t = key => lotTranslations[currentLanguage]?.[key] || key;
  const { user, getAccessTokenSilently } = useAuth0();
  const [availableLots, setAvailableLots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [customers, setCustomers] = useState([]);
  const [isLoadingCustomers, setIsLoadingCustomers] = useState(false);
  const [userRole, setUserRole] = useState(null);

  // Local state fallback (for backward compatibility when props not provided)
  const [localLotFormData, setLocalLotFormData] = useState({
    lotNumber: '',
    civicAddress: '',
    dimensionsSquareFeet: '',
    dimensionsSquareMeters: '',
    price: '',
    lotStatus: 'AVAILABLE',
    assignedCustomerId: '',
  });
  const [localLotImageFile, setLocalLotImageFile] = useState(null);
  const [localLotImagePreviewUrl, setLocalLotImagePreviewUrl] = useState(null);
  const [localShowCreateForm, setLocalShowCreateForm] = useState(false);

  // Use props if provided, otherwise use local state
  const newLotData = lotFormData !== undefined ? lotFormData : localLotFormData;
  const setNewLotData = onLotFormDataChange || setLocalLotFormData;

  const newLotImageFile =
    lotFormImageFile !== undefined ? lotFormImageFile : localLotImageFile;
  const setNewLotImageFile = onLotFormImageFileChange || setLocalLotImageFile;

  const newLotImagePreviewUrl =
    lotFormImagePreviewUrl !== undefined
      ? lotFormImagePreviewUrl
      : localLotImagePreviewUrl;
  const setNewLotImagePreviewUrl =
    onLotFormImagePreviewUrlChange || setLocalLotImagePreviewUrl;

  const showCreateForm =
    showLotCreateForm !== undefined ? showLotCreateForm : localShowCreateForm;
  const setShowCreateForm = onShowLotCreateFormChange || setLocalShowCreateForm;

  useEffect(() => {
    loadLots();
    loadCustomersAndRole();
  }, []);

  const loadCustomersAndRole = async () => {
    try {
      // Get user role from Auth0 user object
      const roles = user?.['https://app.lcdci.ca/roles'] || [];
      const isOwner = roles.includes('OWNER');
      setUserRole(isOwner ? 'OWNER' : null);

      // Only load customers if user is OWNER
      if (isOwner) {
        setIsLoadingCustomers(true);
        const token = await getAccessTokenSilently();
        const customerList = await fetchActiveCustomers(token);
        setCustomers(customerList || []);
      }
    } catch (err) {
      console.error('Failed to load customers:', err);
      // Don't block lot creation if customer loading fails
    } finally {
      setIsLoadingCustomers(false);
    }
  };

  const loadLots = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const lots = await fetchLots({ projectIdentifier });
      setAvailableLots(lots || []);
    } catch (err) {
      setError(err.message || 'Failed to load lots');
      setAvailableLots([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLotImageChange = file => {
    if (!file) {
      // Revoke object URL before clearing
      if (newLotImagePreviewUrl && newLotImagePreviewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newLotImagePreviewUrl);
      }
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
      return;
    }
    const validTypes = ['image/png', 'image/jpeg', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setCreateError(t('invalidImageType') || 'Invalid image type');
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
      return;
    }
    // Additional validation: check file is a real image by loading with FileReader and Image
    const reader = new FileReader();
    reader.onload = e => {
      const img = new window.Image();
      img.onload = () => {
        setCreateError(null);
        // Revoke previous object URL if it exists
        if (
          newLotImagePreviewUrl &&
          newLotImagePreviewUrl.startsWith('blob:')
        ) {
          URL.revokeObjectURL(newLotImagePreviewUrl);
        }
        setNewLotImageFile(file);
        const url = URL.createObjectURL(file);
        setNewLotImagePreviewUrl(url);
      };
      img.onerror = () => {
        setCreateError('Uploaded file is not a valid image.');
        // Revoke object URL if it was created
        if (
          newLotImagePreviewUrl &&
          newLotImagePreviewUrl.startsWith('blob:')
        ) {
          URL.revokeObjectURL(newLotImagePreviewUrl);
        }
        setNewLotImageFile(null);
        setNewLotImagePreviewUrl(null);
      };
      img.src = e.target.result;
    };
    reader.onerror = () => {
      setCreateError('Failed to read image file.');
      // Revoke object URL if it was created
      if (newLotImagePreviewUrl && newLotImagePreviewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newLotImagePreviewUrl);
      }
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
    };
    reader.readAsDataURL(file);
  };

  const filteredLots = availableLots.filter(
    lot =>
      lot.civicAddress?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      lot.lotId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleToggleLot = lotId => {
    if (!lotId) {
      return;
    }

    if (selectedLots.includes(lotId)) {
      // Remove lot
      onChange(selectedLots.filter(id => id !== lotId));
    } else {
      // Add lot
      onChange([...selectedLots, lotId]);
    }
  };

  const handleRemoveLot = lotId => {
    onChange(selectedLots.filter(id => id !== lotId));
  };

  const getSelectedLotDetails = lotId => {
    return availableLots.find(lot => lot.lotId === lotId);
  };

  const handleCreateLot = async e => {
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault();
    }
    setCreateError(null);

    // Validate
    if (!newLotData.lotNumber.trim()) {
      setCreateError(t('lotNumber') + ' is required');
      return;
    }
    if (!newLotData.civicAddress.trim()) {
      setCreateError(t('civicAddress') + ' is required');
      return;
    }
    if (!newLotData.dimensionsSquareFeet.trim()) {
      setCreateError(t('dimensionsSquareFeet') + ' is required');
      return;
    }
    if (!newLotData.dimensionsSquareMeters.trim()) {
      setCreateError(t('dimensionsSquareMeters') + ' is required');
      return;
    }
    if (!newLotData.price || parseFloat(newLotData.price) <= 0) {
      setCreateError(t('price') + ' must be greater than 0');
      return;
    }

    setIsCreating(true);
    try {
      const lotData = {
        lotNumber: newLotData.lotNumber.trim(),
        civicAddress: newLotData.civicAddress.trim(),
        dimensionsSquareFeet: newLotData.dimensionsSquareFeet.trim(),
        dimensionsSquareMeters: newLotData.dimensionsSquareMeters.trim(),
        price: parseFloat(newLotData.price),
        lotStatus: newLotData.lotStatus,
        assignedCustomerId: newLotData.assignedCustomerId || null,
      };

      const createdLot = await createLot({
        projectIdentifier,
        lotData,
      });

      if (!createdLot || !createdLot.lotId) {
        throw new Error('Lot was created but no lotId was returned');
      }

      const newLotId = createdLot.lotId;

      // Add to available lots
      setAvailableLots(prev => {
        if (prev.some(lot => lot.lotId === newLotId)) {
          return prev; // Already exists
        }
        return [...prev, createdLot];
      });

      // Add to selected lots
      if (!selectedLots.includes(newLotId)) {
        onChange([...selectedLots, newLotId]);
      }

      // Notify parent component
      if (onLotCreated) {
        onLotCreated(newLotId);
      }

      // Reset form and clear search so new lot is visible
      // Revoke object URL before clearing
      if (newLotImagePreviewUrl && newLotImagePreviewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(newLotImagePreviewUrl);
      }
      setNewLotData({
        lotNumber: '',
        civicAddress: '',
        dimensionsSquareFeet: '',
        dimensionsSquareMeters: '',
        price: '',
        lotStatus: 'AVAILABLE',
        assignedCustomerId: '',
      });
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
      setShowCreateForm(false);
      setCreateError(null);
      setSearchTerm(''); // Clear search so newly created lot is visible
    } catch (err) {
      const errorMessage =
        err.message ||
        err.toString() ||
        'Failed to create lot. Please try again.';
      setCreateError(errorMessage);
      // Keep form open so user can see the error and try again
    } finally {
      setIsCreating(false);
    }
  };

  if (isLoading) {
    return <div className="lot-selector-loading">{t('loading')}</div>;
  }

  // Show error if there's an error and no lots loaded
  if (error && availableLots.length === 0) {
    return (
      <div className="lot-selector">
        <div className="lot-selector-error">
          {error}
          <button
            type="button"
            onClick={loadLots}
            style={{ marginTop: '10px', padding: '5px 10px' }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="lot-selector">
      <div className="lot-selector-header">
        <button
          type="button"
          className="btn-create-lot"
          onClick={() => setShowCreateForm(!showCreateForm)}
        >
          {showCreateForm ? t('cancelCreate') : t('createNew')}
        </button>
      </div>

      {showCreateForm && (
        <div className="create-lot-form">
          <h3>{t('createLotTitle')}</h3>
          <div className="form-group">
            <label htmlFor="newLotNumber">{t('lotNumber')} *</label>
            <input
              type="text"
              id="newLotNumber"
              value={newLotData.lotNumber}
              onChange={e =>
                setNewLotData({ ...newLotData, lotNumber: e.target.value })
              }
              placeholder={t('lotNumberPlaceholder')}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newLotCivicAddress">{t('civicAddress')} *</label>
            <input
              type="text"
              id="newLotCivicAddress"
              value={newLotData.civicAddress}
              onChange={e =>
                setNewLotData({ ...newLotData, civicAddress: e.target.value })
              }
              placeholder={t('civicAddressPlaceholder')}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newLotDimensionsSquareFeet">{t('dimensionsSquareFeet')} *</label>
            <input
              type="text"
              id="newLotDimensionsSquareFeet"
              value={newLotData.dimensionsSquareFeet}
              onChange={e =>
                setNewLotData({ ...newLotData, dimensionsSquareFeet: e.target.value })
              }
              placeholder={t('dimensionsSquareFeetPlaceholder')}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newLotDimensionsSquareMeters">{t('dimensionsSquareMeters')} *</label>
            <input
              type="text"
              id="newLotDimensionsSquareMeters"
              value={newLotData.dimensionsSquareMeters}
              onChange={e =>
                setNewLotData({ ...newLotData, dimensionsSquareMeters: e.target.value })
              }
              placeholder={t('dimensionsSquareMetersPlaceholder')}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newLotPrice">{t('price')} *</label>
            <input
              type="number"
              id="newLotPrice"
              value={newLotData.price}
              onChange={e =>
                setNewLotData({ ...newLotData, price: e.target.value })
              }
              placeholder={t('pricePlaceholder')}
              min="0"
              step="0.01"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="newLotStatus">{t('status')} *</label>
            <select
              id="newLotStatus"
              value={newLotData.lotStatus}
              onChange={e =>
                setNewLotData({ ...newLotData, lotStatus: e.target.value })
              }
              required
            >
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="SOLD">SOLD</option>
              <option value="PENDING">PENDING</option>
            </select>
          </div>

          {userRole === 'OWNER' && (
            <div className="form-group">
              <label htmlFor="newLotAssignedCustomer">{t('assignedCustomer')}</label>
              {isLoadingCustomers ? (
                <div>{t('loadingCustomers')}</div>
              ) : (
                <select
                  id="newLotAssignedCustomer"
                  value={newLotData.assignedCustomerId}
                  onChange={e =>
                    setNewLotData({ ...newLotData, assignedCustomerId: e.target.value })
                  }
                >
                  <option value="">{t('assignedCustomerPlaceholder')}</option>
                  {customers.map(customer => (
                    <option key={customer.userId} value={customer.userId}>
                      {customer.firstName} {customer.lastName} ({customer.primaryEmail})
                    </option>
                  ))}
                </select>
              )}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="newLotPhoto">{t('photo') || 'Photo'}</label>
            <input
              type="file"
              id="newLotPhoto"
              accept="image/png, image/jpeg, image/webp"
              onChange={e => handleLotImageChange(e.target.files?.[0])}
            />
          </div>

          {newLotImagePreviewUrl && (
            <div className="form-group">
              <img
                src={newLotImagePreviewUrl}
                alt="Lot preview"
                style={{
                  width: '100%',
                  maxWidth: '400px',
                  borderRadius: '6px',
                  border: '1px solid #e0e0e0',
                }}
              />
              <div style={{ marginTop: '0.5rem' }}>
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => {
                    // Revoke object URL before clearing
                    if (
                      newLotImagePreviewUrl &&
                      newLotImagePreviewUrl.startsWith('blob:')
                    ) {
                      URL.revokeObjectURL(newLotImagePreviewUrl);
                    }
                    setNewLotImageFile(null);
                    setNewLotImagePreviewUrl(null);
                  }}
                  disabled={isCreating}
                >
                  {t('removeImage') || 'Remove Image'}
                </button>
              </div>
            </div>
          )}

          {createError && (
            <div
              className="error-message"
              style={{
                color: '#d32f2f',
                backgroundColor: '#ffebee',
                padding: '10px',
                borderRadius: '4px',
                marginBottom: '10px',
                border: '1px solid #d32f2f',
              }}
            >
              <strong>Error:</strong> {createError}
            </div>
          )}

          <div className="create-lot-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={() => {
                // Revoke object URL before clearing
                if (
                  newLotImagePreviewUrl &&
                  newLotImagePreviewUrl.startsWith('blob:')
                ) {
                  URL.revokeObjectURL(newLotImagePreviewUrl);
                }
                setShowCreateForm(false);
                setCreateError(null);
                setNewLotData({
                  lotNumber: '',
                  civicAddress: '',
                  dimensionsSquareFeet: '',
                  dimensionsSquareMeters: '',
                  price: '',
                  lotStatus: 'AVAILABLE',
                  assignedCustomerId: '',
                });
                setNewLotImageFile(null);
                setNewLotImagePreviewUrl(null);
              }}
              disabled={isCreating}
            >
              {t('cancel')}
            </button>
            <button
              type="button"
              className="btn-submit"
              disabled={isCreating}
              onClick={handleCreateLot}
            >
              {isCreating ? t('creating') : t('createButton')}
            </button>
          </div>
        </div>
      )}

      <div className="lot-selector-search">
        <input
          type="text"
          placeholder={t('searchPlaceholder')}
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="lot-search-input"
        />
      </div>

      <div className="lot-selector-list">
        {filteredLots.length > 0 ? (
          filteredLots.map(lot => {
            if (!lot || !lot.lotId) {
              return null;
            }
            const isSelected = (selectedLots || []).includes(lot.lotId);
            return (
              <div
                key={lot.lotId}
                className={`lot-item ${isSelected ? 'selected' : ''}`}
                onClick={() => handleToggleLot(lot.lotId)}
              >
                <input
                  type="checkbox"
                  checked={isSelected}
                  onChange={() => handleToggleLot(lot.lotId)}
                  onClick={e => e.stopPropagation()}
                />
                <div className="lot-item-details">
                  <div className="lot-item-location">
                    {lot.civicAddress || 'Unknown Location'}
                  </div>
                  <div className="lot-item-info">
                    <span>Lot #: {lot.lotNumber || 'N/A'}</span>
                    <span>Dimensions: {lot.dimensionsSquareFeet || 'N/A'} sq ft / {lot.dimensionsSquareMeters || 'N/A'} sq m</span>
                    {lot.price && (
                      <span>Price: ${lot.price.toLocaleString()}</span>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        ) : (
          <div className="lot-selector-empty">
            {searchTerm ? t('noLotsFound') : t('noLotsAvailable')}
          </div>
        )}
      </div>

      {selectedLots.length > 0 && (
        <div className="selected-lots">
          <div className="selected-lots-header">
            {t('selectedCount')} ({selectedLots.length}):
          </div>
          <div className="selected-lots-chips">
            {selectedLots.map(lotId => {
              const lot = getSelectedLotDetails(lotId);
              return (
                <div key={lotId} className="lot-chip">
                  <span>{lot?.civicAddress || lotId}</span>
                  <button
                    type="button"
                    onClick={() => handleRemoveLot(lotId)}
                    className="lot-chip-remove"
                    aria-label="Remove lot"
                  >
                    ×
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

LotSelector.propTypes = {
  currentLanguage: PropTypes.oneOf(['en', 'fr']).isRequired,
  selectedLots: PropTypes.arrayOf(PropTypes.string).isRequired,
  onChange: PropTypes.func.isRequired,
  onLotCreated: PropTypes.func,
  projectIdentifier: PropTypes.string,
  // Optional props for persisting lot form state across language switches
  lotFormData: PropTypes.object,
  onLotFormDataChange: PropTypes.func,
  lotFormImageFile: PropTypes.object,
  onLotFormImageFileChange: PropTypes.func,
  lotFormImagePreviewUrl: PropTypes.string,
  onLotFormImagePreviewUrlChange: PropTypes.func,
  showLotCreateForm: PropTypes.bool,
  onShowLotCreateFormChange: PropTypes.func,
};

export default LotSelector;
