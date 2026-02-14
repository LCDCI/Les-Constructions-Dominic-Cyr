import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import UserSelector from './UserSelector';
import './LotFormModal.css';

const LOT_STATUSES = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'SOLD', label: 'Sold' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'RESERVED', label: 'Reserved' },
];

const LotFormModal = ({
  isOpen,
  onClose,
  onSubmit,
  lot = null,
  token,
  isSubmitting = false,
  title = 'Add Lot',
}) => {
  const [formData, setFormData] = useState({
    lotNumber: '',
    civicAddress: '',
    price: '',
    dimensionsSquareFeet: '',
    dimensionsSquareMeters: '',
    lotStatus: 'AVAILABLE',
    assignedCustomerId: '',
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
        assignedCustomerId: lot.assignedCustomerId || '',
      });
    } else {
      setFormData({
        lotNumber: '',
        civicAddress: '',
        price: '',
        dimensionsSquareFeet: '',
        dimensionsSquareMeters: '',
        lotStatus: 'AVAILABLE',
        assignedCustomerId: '',
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

    // Clear error for this field when user starts typing
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
      newErrors.lotNumber = 'Lot number is required';
    }

    if (!formData.civicAddress.trim()) {
      newErrors.civicAddress = 'Civic address is required';
    }

    // Price is optional - only validate if provided and must be non-negative
    if (formData.price && parseFloat(formData.price) < 0) {
      newErrors.price = 'Price cannot be negative';
    }

    if (!formData.dimensionsSquareFeet.trim()) {
      newErrors.dimensionsSquareFeet = 'Square feet dimension is required';
    }

    if (!formData.dimensionsSquareMeters.trim()) {
      newErrors.dimensionsSquareMeters = 'Square meters dimension is required';
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
      assignedCustomerId: formData.assignedCustomerId || null,
    };

    onSubmit(submitData);
  };

  const handleClose = () => {
    if (!isSubmitting) {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="lot-modal-overlay" onClick={handleClose}>
      <div
        className="lot-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="lot-modal-title"
        aria-describedby="lot-modal-description"
      >
        <div className="lot-modal-header">
          <h2 id="lot-modal-title">{title}</h2>
          <button
            className="lot-modal-close"
            onClick={handleClose}
            disabled={isSubmitting}
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <p id="lot-modal-description" className="sr-only">
          Fill out the lot details and submit the form.
        </p>

        <form onSubmit={handleSubmit} className="lot-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="lotNumber">
                Lot Number <span className="required">*</span>
              </label>
              <input
                id="lotNumber"
                type="text"
                value={formData.lotNumber}
                onChange={e => handleChange('lotNumber', e.target.value)}
                placeholder="e.g., Lot 53"
                disabled={isSubmitting}
                className={errors.lotNumber ? 'error' : ''}
              />
              {errors.lotNumber && (
                <span className="error-text">{errors.lotNumber}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="lotStatus">
                Status <span className="required">*</span>
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
              Civic Address <span className="required">*</span>
            </label>
            <input
              id="civicAddress"
              type="text"
              value={formData.civicAddress}
              onChange={e => handleChange('civicAddress', e.target.value)}
              placeholder="e.g., 123 Main Street, City, QC"
              disabled={isSubmitting}
              className={errors.civicAddress ? 'error' : ''}
            />
            {errors.civicAddress && (
              <span className="error-text">{errors.civicAddress}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="assignedCustomer">Assigned Customer</label>
            <UserSelector
              value={formData.assignedCustomerId}
              onChange={value => handleChange('assignedCustomerId', value)}
              token={token}
              placeholder="Select a customer (optional)..."
            />
            {formData.assignedCustomerId && formData.lotStatus !== 'SOLD' && (
              <div className="info-message">
                ℹ️ Lot status will automatically be set to "Reserved" when a
                customer is assigned
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="price">Price (CAD)</label>
            <input
              id="price"
              type="number"
              step="0.01"
              min="0"
              value={formData.price}
              onChange={e => handleChange('price', e.target.value)}
              placeholder="e.g., 150000"
              disabled={isSubmitting}
              className={errors.price ? 'error' : ''}
            />
            {errors.price && <span className="error-text">{errors.price}</span>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="dimensionsSquareFeet">
                Dimensions (sq ft) <span className="required">*</span>
              </label>
              <input
                id="dimensionsSquareFeet"
                type="text"
                value={formData.dimensionsSquareFeet}
                onChange={e =>
                  handleChange('dimensionsSquareFeet', e.target.value)
                }
                placeholder="e.g., 5000"
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
                Dimensions (sq m) <span className="required">*</span>
              </label>
              <input
                id="dimensionsSquareMeters"
                type="text"
                value={formData.dimensionsSquareMeters}
                onChange={e =>
                  handleChange('dimensionsSquareMeters', e.target.value)
                }
                placeholder="e.g., 464.5"
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
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary"
            >
              {isSubmitting ? 'Saving...' : lot ? 'Update Lot' : 'Create Lot'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

LotFormModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  lot: PropTypes.object,
  token: PropTypes.string,
  isSubmitting: PropTypes.bool,
  title: PropTypes.string,
};

export default LotFormModal;
