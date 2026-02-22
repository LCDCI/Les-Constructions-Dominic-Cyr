import React, { useEffect, useState } from 'react';
import '../../../styles/users.css';
import { usePageTranslations } from '../../../hooks/usePageTranslations';

export default function UserStatusModal({
  isOpen,
  user,
  onClose,
  onConfirm,
  isSubmitting,
  currentUser,
}) {
  const { t } = usePageTranslations('usersPage');
  const [action, setAction] = useState('');

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = e => {
      if (e.key === 'Escape' || e.key === 'Esc') {
        onClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen || !user) return null;

  const isOwner = currentUser?.userRole === 'OWNER';
  const canDeactivate =
    user.userStatus === 'ACTIVE' || user.userStatus === 'INACTIVE';
  const canSetInactive = user.userStatus === 'ACTIVE';
  const canReactivate =
    user.userStatus === 'INACTIVE' || user.userStatus === 'DEACTIVATED';

  const handleConfirm = () => {
    if (action) {
      onConfirm(action);
    }
  };

  const getActionText = () => {
    switch (action) {
      case 'deactivate':
        return t(
          'statusModal.deactivateDescription',
          'Deactivating this user will prevent them from logging in and they will not appear in active assignment lists. Historical data will remain available.'
        );
      case 'inactive':
        return t(
          'statusModal.inactiveDescription',
          'Setting this user as inactive will keep them visible in the dashboard but signal the end of their current project. They can still log in.'
        );
      case 'reactivate':
        return t(
          'statusModal.reactivateDescription',
          'Reactivating this user will restore their full access to the system.'
        );
      default:
        return '';
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content status-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-status-modal-title"
        aria-describedby="user-status-modal-description"
      >
        <div className="modal-header">
          <h2 id="user-status-modal-title">
            {t('statusModal.title', 'Manage User Status')}
          </h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <p id="user-status-modal-description" className="sr-only">
          {t(
            'statusModal.description',
            'Review the current status and select an action to continue.'
          )}
        </p>

        <div className="modal-body">
          <div className="user-status-info">
            <p>
              <strong>{t('statusModal.userLabel', 'User:')}</strong>{' '}
              {user.firstName} {user.lastName}
            </p>
            <p>
              <strong>{t('statusModal.emailLabel', 'Email:')}</strong>{' '}
              {user.primaryEmail}
            </p>
            <p>
              <strong>
                {t('statusModal.currentStatusLabel', 'Current Status:')}
              </strong>{' '}
              <span
                className={`status-badge status-${user.userStatus?.toLowerCase()}`}
              >
                {user.userStatus || 'ACTIVE'}
              </span>
            </p>
          </div>

          {isOwner && (
            <div className="action-selection">
              <label>{t('statusModal.selectAction', 'Select Action:')}</label>
              <div className="action-buttons">
                {canSetInactive && (
                  <button
                    className={`action-btn ${
                      action === 'inactive' ? 'selected' : ''
                    }`}
                    onClick={() => setAction('inactive')}
                    disabled={isSubmitting}
                  >
                    {t('statusModal.setInactive', 'Set as Inactive')}
                  </button>
                )}
                {canDeactivate && (
                  <button
                    className={`action-btn danger ${
                      action === 'deactivate' ? 'selected' : ''
                    }`}
                    onClick={() => setAction('deactivate')}
                    disabled={isSubmitting}
                  >
                    {t('statusModal.deactivateUser', 'Deactivate User')}
                  </button>
                )}
                {canReactivate && (
                  <button
                    className={`action-btn success ${
                      action === 'reactivate' ? 'selected' : ''
                    }`}
                    onClick={() => setAction('reactivate')}
                    disabled={isSubmitting}
                  >
                    {t('statusModal.reactivateUser', 'Reactivate User')}
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

          {!isOwner && (
            <p className="error-text">
              {t(
                'statusModal.noPermission',
                'You do not have permission to change user status.'
              )}
            </p>
          )}
        </div>

        <div className="modal-footer">
          <button
            className="btn-secondary"
            onClick={onClose}
            disabled={isSubmitting}
          >
            {t('statusModal.cancel', 'Cancel')}
          </button>
          {isOwner && (
            <button
              className="btn-primary"
              onClick={handleConfirm}
              disabled={isSubmitting || !action}
            >
              {isSubmitting
                ? t('statusModal.processing', 'Processing...')
                : t('statusModal.confirm', 'Confirm')}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
