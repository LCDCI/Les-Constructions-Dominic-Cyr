import React from 'react';
import PropTypes from 'prop-types';
import '../../styles/Modals/DeleteScheduleConfirmationModal.css';

const DeleteScheduleConfirmationModal = ({
  isOpen,
  onCancel,
  onConfirm,
  isDeleting = false,
}) => {
  if (!isOpen) return null;

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={onCancel}
    >
      <div
        className="schedule-modal delete-schedule-modal"
        onMouseDown={e => e.stopPropagation()}
        role="document"
      >
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">Delete work?</div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onCancel}
            disabled={isDeleting}
          >
            ×
          </button>
        </div>

        <div className="delete-schedule-body">
          This will delete the work and all associated tasks. This action be
          undone.
        </div>

        <div className="form-actions with-danger">
          <button
            type="button"
            className="modal-secondary"
            onClick={onCancel}
            disabled={isDeleting}
          >
            Cancel
          </button>
          <button
            type="button"
            className="modal-danger"
            onClick={onConfirm}
            disabled={isDeleting}
          >
            {isDeleting ? 'Deleting…' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  );
};

DeleteScheduleConfirmationModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onCancel: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  isDeleting: PropTypes.bool,
};

export default DeleteScheduleConfirmationModal;
