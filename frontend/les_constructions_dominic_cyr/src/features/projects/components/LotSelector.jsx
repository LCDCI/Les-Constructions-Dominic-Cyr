import React, { useState, useEffect } from 'react';
import { fetchLots, createLot } from '../../lots/api/lots';
import { uploadFile } from '../../files/api/filesApi';
import PropTypes from 'prop-types';
import '../../../styles/create-project.css';

// Hardcoded translations for LotSelector
const lotTranslations = {
  en: {
    searchPlaceholder: 'Search lots by location...',
    selectedCount: 'Selected Lots',
    createNew: '+ Create New Lot',
    photo: 'Photo',
    cancelCreate: 'Cancel',
    createLotTitle: 'Create New Lot',
    location: 'Location',
    locationPlaceholder: 'Enter lot location',
    dimensions: 'Dimensions',
    dimensionsPlaceholder: 'e.g., 50x100ft',
    price: 'Price',
    pricePlaceholder: 'Enter price',
    status: 'Status',
    createButton: 'Create Lot',
    creating: 'Creating...',
    cancel: 'Cancel',
    loading: 'Loading lots...',
    noLotsFound: 'No lots found matching your search',
    noLotsAvailable: 'No lots available',
    removeImage: 'Remove Image',
    invalidImageType: 'Invalid image type. Allowed: PNG, JPG, JPEG, WEBP',
  },
  fr: {
    searchPlaceholder: 'Rechercher des lots par emplacement...',
    selectedCount: 'Lots sélectionnés',
    createNew: '+ Créer un nouveau lot',
    photo: 'Photo',
    cancelCreate: 'Annuler',
    createLotTitle: 'Créer un nouveau lot',
    location: 'Emplacement',
    locationPlaceholder: "Entrez l'emplacement du lot",
    dimensions: 'Dimensions',
    dimensionsPlaceholder: 'ex: 50x100pi',
    price: 'Prix',
    pricePlaceholder: 'Entrez le prix',
    status: 'Statut',
    createButton: 'Créer le lot',
    creating: 'Création en cours...',
    cancel: 'Annuler',
    loading: 'Chargement des lots...',
    noLotsFound: 'Aucun lot trouvé correspondant à votre recherche',
    noLotsAvailable: 'Aucun lot disponible',
    removeImage: "Supprimer l'image",
    invalidImageType: "Type d'image invalide. Autorisés: PNG, JPG, JPEG, WEBP",
  },
};

const LotSelector = ({ currentLanguage, selectedLots, onChange, onLotCreated }) => {
  const t = (key) => lotTranslations[currentLanguage]?.[key] || key;
  const [availableLots, setAvailableLots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newLotData, setNewLotData] = useState({
    location: '',
    dimensions: '',
    price: '',
    lotStatus: 'AVAILABLE',
    imageIdentifier: null,
  });
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [newLotImageFile, setNewLotImageFile] = useState(null);
  const [newLotImagePreviewUrl, setNewLotImagePreviewUrl] = useState(null);

  useEffect(() => {
    loadLots();
  }, []);

  const loadLots = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const lots = await fetchLots();
      setAvailableLots(lots || []);
    } catch (err) {
      setError(err.message || 'Failed to load lots');
      setAvailableLots([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLotImageChange = (file) => {
    if (!file) {
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
    reader.onload = (e) => {
      const img = new window.Image();
      img.onload = () => {
        setCreateError(null);
        setNewLotImageFile(file);
        const url = URL.createObjectURL(file);
        setNewLotImagePreviewUrl(url);
      };
      img.onerror = () => {
        setCreateError('Uploaded file is not a valid image.');
        setNewLotImageFile(null);
        setNewLotImagePreviewUrl(null);
      };
      img.src = e.target.result;
    };
    reader.onerror = () => {
      setCreateError('Failed to read image file.');
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
    };
    reader.readAsDataURL(file);
  };

  const filteredLots = availableLots.filter(lot =>
    lot.location?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    lot.lotId?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleToggleLot = (lotId) => {
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

  const handleRemoveLot = (lotId) => {
    onChange(selectedLots.filter(id => id !== lotId));
  };

  const getSelectedLotDetails = (lotId) => {
    return availableLots.find(lot => lot.lotId === lotId);
  };

  const handleCreateLot = async (e) => {
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault();
    }
    setCreateError(null);

    // Validate
    if (!newLotData.location.trim()) {
      setCreateError(t('location') + ' is required');
      return;
    }
    if (!newLotData.dimensions.trim()) {
      setCreateError(t('dimensions') + ' is required');
      return;
    }
    if (!newLotData.price || parseFloat(newLotData.price) <= 0) {
      setCreateError(t('price') + ' must be greater than 0');
      return;
    }

    setIsCreating(true);
    try {
      // Upload image first if provided to obtain imageIdentifier
      let imageIdentifier = null;
      if (newLotImageFile) {
        const formData = new FormData();
        formData.append('file', newLotImageFile);
        formData.append('category', 'PHOTO');
        // No projectId for lots; stored as global photo
        const uploadResp = await uploadFile(formData);
        imageIdentifier = uploadResp.fileId || uploadResp.id || null;
      }

      const lotData = {
        location: newLotData.location.trim(),
        dimensions: newLotData.dimensions.trim(),
        price: parseFloat(newLotData.price),
        lotStatus: newLotData.lotStatus,
        imageIdentifier: imageIdentifier || newLotData.imageIdentifier || null,
      };

      const createdLot = await createLot(lotData);
      
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
        location: '',
        dimensions: '',
        price: '',
        lotStatus: 'AVAILABLE',
        imageIdentifier: null,
      });
      setNewLotImageFile(null);
      setNewLotImagePreviewUrl(null);
      setShowCreateForm(false);
      setCreateError(null);
      setSearchTerm(''); // Clear search so newly created lot is visible
    } catch (err) {
      const errorMessage = err.message || err.toString() || 'Failed to create lot. Please try again.';
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
          {showCreateForm 
            ? t('cancelCreate') 
            : t('createNew')}
        </button>
      </div>

      {showCreateForm && (
        <div className="create-lot-form">
          <h3>{t('createLotTitle')}</h3>
            <div className="form-group">
              <label htmlFor="newLotLocation">
                {t('location')} *
              </label>
              <input
                type="text"
                id="newLotLocation"
                value={newLotData.location}
                onChange={(e) => setNewLotData({ ...newLotData, location: e.target.value })}
                placeholder={t('locationPlaceholder')}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="newLotDimensions">
                {t('dimensions')} *
              </label>
              <input
                type="text"
                id="newLotDimensions"
                value={newLotData.dimensions}
                onChange={(e) => setNewLotData({ ...newLotData, dimensions: e.target.value })}
                placeholder={t('dimensionsPlaceholder')}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="newLotPrice">
                {t('price')} *
              </label>
              <input
                type="number"
                id="newLotPrice"
                value={newLotData.price}
                onChange={(e) => setNewLotData({ ...newLotData, price: e.target.value })}
                placeholder={t('pricePlaceholder')}
                min="0"
                step="0.01"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="newLotStatus">
                {t('status')} *
              </label>
              <select
                id="newLotStatus"
                value={newLotData.lotStatus}
                onChange={(e) => setNewLotData({ ...newLotData, lotStatus: e.target.value })}
                required
              >
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="SOLD">SOLD</option>
                <option value="PENDING">PENDING</option>
              </select>
            </div>

          <div className="form-group">
            <label htmlFor="newLotPhoto">
              {t('photo') || 'Photo'}
            </label>
            <input
              type="file"
              id="newLotPhoto"
              accept="image/png, image/jpeg, image/webp"
              onChange={(e) => handleLotImageChange(e.target.files?.[0])}
            />
          </div>

          {newLotImagePreviewUrl && (
            <div className="form-group">
              <img
                src={newLotImagePreviewUrl}
                alt="Lot preview"
                style={{ width: '100%', maxWidth: '400px', borderRadius: '6px', border: '1px solid #e0e0e0' }}
              />
              <div style={{ marginTop: '0.5rem' }}>
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => {
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
              <div className="error-message" style={{ 
                color: '#d32f2f', 
                backgroundColor: '#ffebee', 
                padding: '10px', 
                borderRadius: '4px', 
                marginBottom: '10px',
                border: '1px solid #d32f2f'
              }}>
                <strong>Error:</strong> {createError}
              </div>
            )}

            <div className="create-lot-actions">
              <button
                type="button"
                className="btn-cancel"
                onClick={() => {
                  setShowCreateForm(false);
                  setCreateError(null);
                  setNewLotData({
                    location: '',
                    dimensions: '',
                    price: '',
                    lotStatus: 'AVAILABLE',
                    imageIdentifier: null,
                  });
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
                {isCreating 
                  ? t('creating') 
                  : t('createButton')}
              </button>
            </div>
        </div>
      )}

      <div className="lot-selector-search">
        <input
          type="text"
          placeholder={t('searchPlaceholder')}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
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
                  onClick={(e) => e.stopPropagation()}
                />
                <div className="lot-item-details">
                  <div className="lot-item-location">{lot.location || 'Unknown Location'}</div>
                  <div className="lot-item-info">
                    <span>Dimensions: {lot.dimensions || 'N/A'}</span>
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
                  <span>{lot?.location || lotId}</span>
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
};

export default LotSelector;
