import React, { useState } from 'react';
import '../../../styles/users.css';

export default function UserStatusModal({ isOpen, user, onClose, onConfirm, isSubmitting, currentUser }) {
  const [action, setAction] = useState('');

  if (!isOpen || !user) return null;

  const isOwner = currentUser?.userRole === 'OWNER';
  const canDeactivate = user.userStatus === 'ACTIVE' || user.userStatus === 'INACTIVE';
  const canSetInactive = user.userStatus === 'ACTIVE';
  const canReactivate = user.userStatus === 'INACTIVE' || user.userStatus === 'DEACTIVATED';

  const handleConfirm = () => {
    if (action) {
      onConfirm(action);
    }
  };

  const getActionText = () => {
    switch (action) {
      case 'deactivate':
        return 'Deactivating this user will prevent them from logging in and they will not appear in active assignment lists.  Historical data will remain available. ';
      case 'inactive': 
        return 'Setting this user as inactive will keep them visible in the dashboard but signal the end of their current project.  They can still log in. ';
      case 'reactivate':
        return 'Reactivating this user will restore their full access to the system. ';
      default:
        return '';
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Manage User Status</h2>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="user-status-info">
            <p>
              <strong>User:</strong> {user.firstName} {user.lastName}
            </p>
            <p>
              <strong>Email:</strong> {user.primaryEmail}
            </p>
            <p>
              <strong>Current Status:</strong>{' '}
              <span className={`status-badge status-${user.userStatus?. toLowerCase()}`}>
                {user.userStatus || 'ACTIVE'}
              </span>
            </p>
          </div>

          {isOwner && (
            <div className="action-selection">
              <label>Select Action:</label>
              <div className="action-buttons">
                {canSetInactive && (
                  <button
                    className={`action-btn ${action === 'inactive' ? 'selected' : ''}`}
                    onClick={() => setAction('inactive')}
                    disabled={isSubmitting}
                  >
                    Set as Inactive
                  </button>
                )}
                {canDeactivate && (
                  <button
                    className={`action-btn danger ${action === 'deactivate' ? 'selected' : ''}`}
                    onClick={() => setAction('deactivate')}
                    disabled={isSubmitting}
                  >
                    Deactivate User
                  </button>
                )}
                {canReactivate && (
                  <button
                    className={`action-btn success ${action === 'reactivate' ? 'selected' : ''}`}
                    onClick={() => setAction('reactivate')}
                    disabled={isSubmitting}
                  >
                    Reactivate User
                  </button>
                )}
              </div>

              {action && (
                <div className="action-description">
                  <p>{getActionText()}</p>
                </div>
              )}
            </div>
          )}

          {! isOwner && (
            <p className="error-text">You do not have permission to change user status.</p>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </button>
          {isOwner && (
            <button
              className="btn-primary"
              onClick={handleConfirm}
              disabled={isSubmitting || !action}
            >
              {isSubmitting ? 'Processing...' : 'Confirm'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}