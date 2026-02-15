import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import MultiUserSelector from './MultiUserSelector';
import './LotFormModal.css';

const OwnerLotFormModal = ({
  isOpen,
  onClose,
  onSubmit,
  lot = null,
  token,
  isSubmitting = false,
}) => {
  const { t } = usePageTranslations('ownerLots');

  const LOT_STATUSES = [
    { value: 'AVAILABLE', label: t('filters.available') },
    { value: 'RESERVED', label: t('filters.reserved') },
    { value: 'SOLD', label: t('filters.sold') },
    { value: 'PENDING', label: t('filters.pending') },
  ];

  const [formData, setFormData] = useState({
    lotNumber: '',
    civicAddress: '',
    price: '',
    dimensionsSquareFeet: '',
    dimensionsSquareMeters: '',
    lotStatus: 'AVAILABLE',
    assignedUserIds: [],
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (lot) {
      setFormData({
        lotNumber: lot.lotNumber || '',
        civicAddress: lot.civicAddress || '',
        price: lot.price?.toString() || '',
        dimensionsSquareFeet: lot.dimensionsSquareFeet || '',
        dimensionsSquareMeters: lot.dimensionsSquareMeters || '',
        lotStatus: lot.lotStatus || 'AVAILABLE',
        assignedUserIds: lot.assignedUsers?.map(u => u.userId) || [],
      });
    } else {
      setFormData({
        lotNumber: '',
        civicAddress: '',
        price: '',
        dimensionsSquareFeet: '',
        dimensionsSquareMeters: '',
        lotStatus: 'AVAILABLE',
        assignedUserIds: [],
      });
    }
    setErrors({});
  }, [lot, isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = e => {
      if (e.key === 'Escape' || e.key === 'Esc') {
        if (!isSubmitting) onClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, isSubmitting, onClose]);

  const handleChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));

    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: '',
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.lotNumber.trim()) {
      newErrors.lotNumber = t('form.lotNumberRequired');
    }

    if (!formData.civicAddress.trim()) {
      newErrors.civicAddress = t('form.civicAddressRequired');
    }

    if (formData.price && parseFloat(formData.price) < 0) {
      newErrors.price = t('form.priceNegativeError');
    }

    if (!formData.dimensionsSquareFeet.trim()) {
      newErrors.dimensionsSquareFeet = t('form.dimensionsSqFtRequired');
    }

    if (!formData.dimensionsSquareMeters.trim()) {
      newErrors.dimensionsSquareMeters = t('form.dimensionsSqMRequired');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = e => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const submitData = {
      lotNumber: formData.lotNumber,
      civicAddress: formData.civicAddress,
      price: formData.price ? parseFloat(formData.price) : null,
      dimensionsSquareFeet: formData.dimensionsSquareFeet,
      dimensionsSquareMeters: formData.dimensionsSquareMeters,
      lotStatus: formData.lotStatus,
      assignedUserIds: formData.assignedUserIds,
    };

    onSubmit(submitData);
  };

  const handleClose = () => {
    if (!isSubmitting) {
      onClose();
    }
  };

  if (!isOpen) return null;

  const modalTitle = lot ? t('modal.editLot') : t('modal.addLot');

  return (
    <div className="lot-modal-overlay" onClick={handleClose}>
      <div
        className="lot-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="owner-lot-modal-title"
        aria-describedby="owner-lot-modal-description"
      >
        <div className="lot-modal-header">
          <h2 id="owner-lot-modal-title">{modalTitle}</h2>
          <button
            className="lot-modal-close"
            onClick={handleClose}
            disabled={isSubmitting}
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <p
          id="owner-lot-modal-description"
          style={{
            border: 0,
            clip: 'rect(0 0 0 0)',
            height: '1px',
            margin: '-1px',
            overflow: 'hidden',
            padding: 0,
            position: 'absolute',
            width: '1px',
            whiteSpace: 'nowrap',
          }}
        >
          Update lot details and assigned users, then submit the form.
        </p>

        <form onSubmit={handleSubmit} className="lot-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="lotNumber">
                {t('form.lotNumber')}{' '}
                <span className="required">{t('form.required')}</span>
              </label>
              <input
                id="lotNumber"
                type="text"
                value={formData.lotNumber}
                onChange={e => handleChange('lotNumber', e.target.value)}
                placeholder={t('form.lotNumberPlaceholder')}
                disabled={isSubmitting}
                className={errors.lotNumber ? 'error' : ''}
              />
              {errors.lotNumber && (
                <span className="error-text">{errors.lotNumber}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="lotStatus">
                {t('form.status')}{' '}
                <span className="required">{t('form.required')}</span>
              </label>
              <select
                id="lotStatus"
                value={formData.lotStatus}
                onChange={e => handleChange('lotStatus', e.target.value)}
                disabled={isSubmitting}
              >
                {LOT_STATUSES.map(status => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="civicAddress">
              {t('form.civicAddress')}{' '}
              <span className="required">{t('form.required')}</span>
            </label>
            <input
              id="civicAddress"
              type="text"
              value={formData.civicAddress}
              onChange={e => handleChange('civicAddress', e.target.value)}
              placeholder={t('form.civicAddressPlaceholder')}
              disabled={isSubmitting}
              className={errors.civicAddress ? 'error' : ''}
            />
            {errors.civicAddress && (
              <span className="error-text">{errors.civicAddress}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="assignedUsers">{t('form.assignedUsers')}</label>
            <MultiUserSelector
              selectedUserIds={formData.assignedUserIds}
              onChange={value => handleChange('assignedUserIds', value)}
              token={token}
              placeholder={t('form.assignedUsersPlaceholder')}
            />
            {formData.assignedUserIds.length > 0 &&
              formData.lotStatus === 'AVAILABLE' && (
                <div className="info-message">
                  ℹ️ {t('form.assignedUsersInfo')}
                </div>
              )}
          </div>

          <div className="form-group">
            <label htmlFor="price">{t('form.price')}</label>
            <input
              id="price"
              type="number"
              step="0.01"
              min="0"
              value={formData.price}
              onChange={e => handleChange('price', e.target.value)}
              placeholder={t('form.pricePlaceholder')}
              disabled={isSubmitting}
              className={errors.price ? 'error' : ''}
            />
            {errors.price && <span className="error-text">{errors.price}</span>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="dimensionsSquareFeet">
                {t('form.dimensionsSqFt')}{' '}
                <span className="required">{t('form.required')}</span>
              </label>
              <input
                id="dimensionsSquareFeet"
                type="text"
                value={formData.dimensionsSquareFeet}
                onChange={e =>
                  handleChange('dimensionsSquareFeet', e.target.value)
                }
                placeholder={t('form.dimensionsSqFtPlaceholder')}
                disabled={isSubmitting}
                className={errors.dimensionsSquareFeet ? 'error' : ''}
              />
              {errors.dimensionsSquareFeet && (
                <span className="error-text">
                  {errors.dimensionsSquareFeet}
                </span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="dimensionsSquareMeters">
                {t('form.dimensionsSqM')}{' '}
                <span className="required">{t('form.required')}</span>
              </label>
              <input
                id="dimensionsSquareMeters"
                type="text"
                value={formData.dimensionsSquareMeters}
                onChange={e =>
                  handleChange('dimensionsSquareMeters', e.target.value)
                }
                placeholder={t('form.dimensionsSqMPlaceholder')}
                disabled={isSubmitting}
                className={errors.dimensionsSquareMeters ? 'error' : ''}
              />
              {errors.dimensionsSquareMeters && (
                <span className="error-text">
                  {errors.dimensionsSquareMeters}
                </span>
              )}
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              onClick={handleClose}
              disabled={isSubmitting}
              className="btn-secondary"
            >
              {t('modal.cancel')}
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary"
            >
              {isSubmitting
                ? t('modal.saving')
                : lot
                  ? t('modal.updateLot')
                  : t('modal.createLot')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

OwnerLotFormModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  lot: PropTypes.object,
  token: PropTypes.string,
  isSubmitting: PropTypes.bool,
};

export default OwnerLotFormModal;
