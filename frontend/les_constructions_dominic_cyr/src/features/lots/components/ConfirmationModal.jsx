import React from 'react';
import PropTypes from 'prop-types';
import './ConfirmationModal.css';

const ConfirmationModal = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  isDestructive = false,
  isSubmitting = false,
}) => {
  const handleConfirm = () => {
    if (!isSubmitting) {
      onConfirm();
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="confirmation-modal-overlay" onClick={handleClose}>
      <div className="confirmation-modal" onClick={e => e.stopPropagation()}>
        <div className="confirmation-modal-header">
          <h3>{title}</h3>
          <button
            className="confirmation-modal-close"
            onClick={handleClose}
            disabled={isSubmitting}
          >
            ×
          </button>
        </div>

        <div className="confirmation-modal-body">
          <p>{message}</p>
        </div>

        <div className="confirmation-modal-actions">
          <button
            type="button"
            onClick={handleClose}
            disabled={isSubmitting}
            className="btn-cancel"
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={isSubmitting}
            className={`btn-confirm ${isDestructive ? 'destructive' : ''}`}
          >
            {isSubmitting ? 'Please wait...' : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

ConfirmationModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  title: PropTypes.string,
  message: PropTypes.string,
  confirmText: PropTypes.string,
  cancelText: PropTypes.string,
  isDestructive: PropTypes.bool,
  isSubmitting: PropTypes.bool,
};

export default ConfirmationModal;
