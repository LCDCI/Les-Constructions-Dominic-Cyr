import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLots, createLot } from '../../lots/api/lots';
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
    priceCannotBeNegative: 'Price cannot be negative',
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
    draftLotsTitle: 'New lots (to be created with project)',
    newLabel: 'New',
    // Lot detail labels
    lotNumberLabel: 'Lot #:',
    dimensionsLabel: 'Dimensions:',
    sqFt: 'sq ft',
    sqM: 'sq m',
    priceLabel: 'Price:',
    statusLabel: 'Status:',
    unknownLocation: 'Unknown Location',
    na: 'N/A',
    retry: 'Retry',
    isRequired: 'is required',
    errorPrefix: 'Error:',
    removeDraftLot: 'Remove draft lot',
    // Status option labels
    statusAvailable: 'Available',
    statusSold: 'Sold',
    statusPending: 'Pending',
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
    priceCannotBeNegative: 'Le prix ne peut pas être négatif',
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
    draftLotsTitle: 'Nouveaux lots (créés avec le projet)',
    newLabel: 'Nouveau',
    // Lot detail labels
    lotNumberLabel: 'Lot n° :',
    dimensionsLabel: 'Dimensions :',
    sqFt: 'pi²',
    sqM: 'm²',
    priceLabel: 'Prix :',
    statusLabel: 'Statut :',
    unknownLocation: 'Emplacement inconnu',
    na: 'N/D',
    retry: 'Réessayer',
    isRequired: 'est requis',
    errorPrefix: 'Erreur :',
    removeDraftLot: 'Supprimer le lot brouillon',
    // Status option labels
    statusAvailable: 'Disponible',
    statusSold: 'Vendu',
    statusPending: 'En attente',
  },
};

const LotSelector = ({
  currentLanguage,
  selectedLots,
  onChange,
  onLotCreated,
  projectIdentifier,
  // Draft lots (create-project flow: no project yet; lots created after project is saved)
  draftLots = [],
  onDraftLotAdded,
  onDraftLotRemoved,
  // Props to persist lot form state across language switches
  lotFormData,
  onLotFormDataChange,
  showLotCreateForm,
  onShowLotCreateFormChange,
}) => {
  const isDraftMode =
    !projectIdentifier && typeof onDraftLotAdded === 'function';
  const t = key => lotTranslations[currentLanguage]?.[key] || key;
  const { user, getAccessTokenSilently } = useAuth0();
  const [availableLots, setAvailableLots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(!isDraftMode);
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
  const [localShowCreateForm, setLocalShowCreateForm] = useState(false);

  // Use props if provided, otherwise use local state
  const newLotData = lotFormData !== undefined ? lotFormData : localLotFormData;
  const setNewLotData = onLotFormDataChange || setLocalLotFormData;

  const showCreateForm =
    showLotCreateForm !== undefined ? showLotCreateForm : localShowCreateForm;
  const setShowCreateForm = onShowLotCreateFormChange || setLocalShowCreateForm;

  useEffect(() => {
    loadCustomersAndRole();
    if (isDraftMode) {
      setAvailableLots([]);
      setIsLoading(false);
    } else {
      loadLots();
    }
  }, [isDraftMode]);

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
      const token = await getAccessTokenSilently();
      const lots = await fetchLots({ projectIdentifier, token });
      setAvailableLots(lots || []);
    } catch (err) {
      setError(err.message || 'Failed to load lots');
      setAvailableLots([]);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredLots = availableLots.filter(lot => {
    const searchTermLower = searchTerm.toLowerCase();
    return (
      lot.civicAddress?.toLowerCase().includes(searchTermLower) ||
      lot.lotId?.toLowerCase().includes(searchTermLower)
    );
  });

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
      setCreateError(t('lotNumber') + ' ' + t('isRequired'));
      return;
    }
    if (!newLotData.civicAddress.trim()) {
      setCreateError(t('civicAddress') + ' ' + t('isRequired'));
      return;
    }
    if (!newLotData.dimensionsSquareFeet.trim()) {
      setCreateError(t('dimensionsSquareFeet') + ' ' + t('isRequired'));
      return;
    }
    if (!newLotData.dimensionsSquareMeters.trim()) {
      setCreateError(t('dimensionsSquareMeters') + ' ' + t('isRequired'));
      return;
    }
    const priceNum = parseFloat(newLotData.price);
    if (
      newLotData.price === '' ||
      newLotData.price == null ||
      isNaN(priceNum)
    ) {
      setCreateError(t('price') + ' ' + t('isRequired'));
      return;
    }
    if (priceNum < 0) {
      setCreateError(t('priceCannotBeNegative'));
      return;
    }

    const lotData = {
      lotNumber: newLotData.lotNumber.trim(),
      civicAddress: newLotData.civicAddress.trim(),
      dimensionsSquareFeet: newLotData.dimensionsSquareFeet.trim(),
      dimensionsSquareMeters: newLotData.dimensionsSquareMeters.trim(),
      price: priceNum,
      lotStatus: newLotData.lotStatus,
      assignedCustomerId: newLotData.assignedCustomerId || null,
    };

    // Draft mode: no project yet (create-project step 3); add to draft list and create after project is saved
    if (isDraftMode) {
      onDraftLotAdded(lotData);
      setNewLotData({
        lotNumber: '',
        civicAddress: '',
        dimensionsSquareFeet: '',
        dimensionsSquareMeters: '',
        price: '',
        lotStatus: 'AVAILABLE',
        assignedCustomerId: '',
      });
      setShowCreateForm(false);
      setCreateError(null);
      return;
    }

    if (!projectIdentifier) {
      setCreateError(
        'Project identifier is missing. Save or select a project before creating lots.'
      );
      return;
    }

    setIsCreating(true);
    try {
      const token = await getAccessTokenSilently();

      const createdLot = await createLot({
        projectIdentifier,
        lotData,
        token,
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
      setNewLotData({
        lotNumber: '',
        civicAddress: '',
        dimensionsSquareFeet: '',
        dimensionsSquareMeters: '',
        price: '',
        lotStatus: 'AVAILABLE',
        assignedCustomerId: '',
      });
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

  // Show error and block UI only if not in draft mode (draft mode: no project yet, so loading lots may fail; still show form to add draft lots)
  if (error && availableLots.length === 0 && !isDraftMode) {
    return (
      <div className="lot-selector">
        <div className="lot-selector-error">
          {error}
          <button
            type="button"
            onClick={loadLots}
            style={{ marginTop: '10px', padding: '5px 10px' }}
          >
            {t('retry')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="lot-selector">
      <div className="create-lot-form create-lot-form-always-open">
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
          />
        </div>

        <div className="form-group">
          <label htmlFor="newLotDimensionsSquareFeet">
            {t('dimensionsSquareFeet')} *
          </label>
          <input
            type="text"
            id="newLotDimensionsSquareFeet"
            value={newLotData.dimensionsSquareFeet}
            onChange={e =>
              setNewLotData({
                ...newLotData,
                dimensionsSquareFeet: e.target.value,
              })
            }
            placeholder={t('dimensionsSquareFeetPlaceholder')}
          />
        </div>

        <div className="form-group">
          <label htmlFor="newLotDimensionsSquareMeters">
            {t('dimensionsSquareMeters')} *
          </label>
          <input
            type="text"
            id="newLotDimensionsSquareMeters"
            value={newLotData.dimensionsSquareMeters}
            onChange={e =>
              setNewLotData({
                ...newLotData,
                dimensionsSquareMeters: e.target.value,
              })
            }
            placeholder={t('dimensionsSquareMetersPlaceholder')}
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
          >
            <option value="AVAILABLE">{t('statusAvailable')}</option>
            <option value="SOLD">{t('statusSold')}</option>
            <option value="PENDING">{t('statusPending')}</option>
          </select>
        </div>

        {userRole === 'OWNER' && (
          <div className="form-group">
            <label htmlFor="newLotAssignedCustomer">
              {t('assignedCustomer')}
            </label>
            {isLoadingCustomers ? (
              <div>{t('loadingCustomers')}</div>
            ) : (
              <select
                id="newLotAssignedCustomer"
                value={newLotData.assignedCustomerId}
                onChange={e =>
                  setNewLotData({
                    ...newLotData,
                    assignedCustomerId: e.target.value,
                  })
                }
              >
                <option value="">{t('assignedCustomerPlaceholder')}</option>
                {customers.map(customer => (
                  <option key={customer.userId} value={customer.userId}>
                    {customer.firstName} {customer.lastName} (
                    {customer.primaryEmail})
                  </option>
                ))}
              </select>
            )}
          </div>
        )}

        {/* Image upload removed - not supported by backend Lot entity */}

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
            <strong>{t('errorPrefix')}</strong> {createError}
          </div>
        )}

        <div className="create-lot-actions">
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

      {!isDraftMode && (
        <>
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
                        {lot.civicAddress || t('unknownLocation')}
                      </div>
                      <div className="lot-item-info">
                        <span>{t('lotNumberLabel')} {lot.lotNumber || t('na')}</span>
                        <span>
                          {t('dimensionsLabel')} {lot.dimensionsSquareFeet || t('na')} {t('sqFt')}
                          {' / '}{lot.dimensionsSquareMeters || t('na')} {t('sqM')}
                        </span>
                        {lot.price && (
                          <span>{t('priceLabel')} ${lot.price.toLocaleString()}</span>
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
        </>
      )}

      {draftLots.length > 0 && (
        <div className="draft-lots-section">
          <div className="selected-lots-header">
            {t('draftLotsTitle')} ({draftLots.length}):
          </div>
          <div className="lot-selector-list">
            {draftLots.map((draft, index) => (
              <div key={`draft-${index}`} className="lot-item lot-item-draft">
                <div className="lot-item-details">
                  <div className="lot-item-location">
                    {draft.civicAddress || t('unknownLocation')}
                  </div>
                  <div className="lot-item-info">
                    <span>{t('lotNumberLabel')} {draft.lotNumber || t('na')}</span>
                    <span>
                      {t('dimensionsLabel')} {draft.dimensionsSquareFeet || t('na')} {t('sqFt')}
                      {' / '}{draft.dimensionsSquareMeters || t('na')} {t('sqM')}
                    </span>
                    {draft.price != null && draft.price !== '' && (
                      <span>
                        {t('priceLabel')} $
                        {typeof draft.price === 'number'
                          ? draft.price.toLocaleString()
                          : Number(draft.price).toLocaleString()}
                      </span>
                    )}
                    {draft.lotStatus && <span>{t('statusLabel')} {
                      draft.lotStatus === 'AVAILABLE' ? t('statusAvailable')
                        : draft.lotStatus === 'SOLD' ? t('statusSold')
                        : draft.lotStatus === 'PENDING' ? t('statusPending')
                        : draft.lotStatus
                    }</span>}
                  </div>
                </div>
                {typeof onDraftLotRemoved === 'function' && (
                  <button
                    type="button"
                    onClick={() => onDraftLotRemoved(index)}
                    className="lot-item-draft-remove"
                    aria-label={t('removeDraftLot')}
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {!isDraftMode && selectedLots.length > 0 && (
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
  draftLots: PropTypes.arrayOf(PropTypes.object),
  onDraftLotAdded: PropTypes.func,
  onDraftLotRemoved: PropTypes.func,
  // Optional props for persisting lot form state across language switches
  lotFormData: PropTypes.object,
  onLotFormDataChange: PropTypes.func,
  showLotCreateForm: PropTypes.bool,
  onShowLotCreateFormChange: PropTypes.func,
};

export default LotSelector;
