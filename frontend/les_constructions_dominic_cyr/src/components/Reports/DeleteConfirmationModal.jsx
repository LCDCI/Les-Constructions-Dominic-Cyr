/* eslint-disable react/prop-types */
import React from 'react';
import '../../styles/Reports/DeleteModal.css';

const DeleteConfirmationModal = ({
  isOpen,
  onClose,
  onConfirm,
  reportDate,
}) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <h3>Confirm Permanent Deletion</h3>
        </div>
        <div className="modal-body">
          <p>
            You are about to delete this report. Are you sure you want to?
            <strong>{reportDate}</strong>
          </p>
          <br></br>
          <p className="warning-text">
            This action cannot be undone and will remove the file from the
            online storage.
          </p>
          <br></br>
          <br></br>
          <br></br>
          <button className="secondary-btn" onClick={onClose}>
            Cancel
          </button>
          <button className="danger-btn" onClick={onConfirm}>
            Delete Permanently
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeleteConfirmationModal;
