import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import '../../styles/Modals/ConfirmationModal.css';

export default function ConfirmationModal({ isOpen, onCancel, config = {} }) {
  const {
    title = 'Confirm Action',
    message = 'Are you sure?',
    onConfirm = () => {},
    confirmText = 'Delete',
    cancelText = 'Cancel',
    isDestructive = true,
  } = config;

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = e => {
      if (e.key === 'Escape' || e.key === 'Esc') {
        onCancel();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onCancel]);
  if (!isOpen) return null;

  return (
    <div className="confirmation-modal-overlay" onClick={onCancel}>
      <div
        className="confirmation-modal-content"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        aria-describedby="modal-description"
      >
        <div className="confirmation-modal-header">
          <h2 id="modal-title">{title}</h2>
        </div>
        <div className="confirmation-modal-body">
          <p id="modal-description">{message}</p>
        </div>
        <div className="confirmation-modal-footer">
          <button className="btn-cancel" onClick={onCancel}>
            {cancelText}
          </button>
          <button
            className={
              isDestructive ? 'btn-confirm-destructive' : 'btn-confirm'
            }
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

ConfirmationModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onCancel: PropTypes.func.isRequired,
  config: PropTypes.shape({
    title: PropTypes.string,
    message: PropTypes.string,
    onConfirm: PropTypes.func,
    confirmText: PropTypes.string,
    cancelText: PropTypes.string,
    isDestructive: PropTypes.bool,
  }),
};
