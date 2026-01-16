import React from 'react';
import '../../styles/Reports/DeleteModal.css';

const DeleteConfirmationModal = ({ isOpen, onClose, onConfirm, reportDate }) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h3>Confirm Permanent Deletion</h3>
                </div>
                <div className="modal-body">
                    <p>You are about to delete the report from <strong>{reportDate}</strong>.</p>
                    <p className="warning-text">This action cannot be undone and will remove the file from Digital Ocean storage.</p>
                </div>
                <div className="modal-footer">
                    <button className="secondary-btn" onClick={onClose}>Cancel</button>
                    <button className="danger-btn" onClick={onConfirm}>Delete Permanently</button>
                </div>
            </div>
        </div>
    );
};

export default DeleteConfirmationModal;