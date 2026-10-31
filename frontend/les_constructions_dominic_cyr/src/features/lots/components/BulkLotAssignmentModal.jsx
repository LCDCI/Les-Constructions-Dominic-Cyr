import React, { useState } from 'react';
import PropTypes from 'prop-types';
import MultiUserSelector from './MultiUserSelector';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './LotFormModal.css';

const BulkLotAssignmentModal = ({
  isOpen,
  onClose,
  onSubmit,
  token,
  isSubmitting = false,
}) => {
  const { t } = usePageTranslations('ownerLots');
  const [selectedUserIds, setSelectedUserIds] = useState([]);

  if (!isOpen) return null;

  const handleSubmit = event => {
    event.preventDefault();
    if (selectedUserIds.length === 1) {
      onSubmit(selectedUserIds[0]);
    }
  };

  return (
    <div
      className="lot-modal-overlay"
      onClick={isSubmitting ? undefined : onClose}
    >
      <div
        className="lot-modal"
        onClick={event => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        <div className="lot-modal-header">
          <h2>{t('bulkAssignment.title')}</h2>
          <button
            className="lot-modal-close"
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            aria-label="Close"
          >
            ×
          </button>
        </div>
        <form className="lot-form" onSubmit={handleSubmit}>
          <p>{t('bulkAssignment.description')}</p>
          <MultiUserSelector
            selectedUserIds={selectedUserIds}
            onChange={value => setSelectedUserIds(value.slice(-1))}
            token={token}
            allowedRoles={['CONTRACTOR', 'SALESPERSON']}
            placeholder={t('bulkAssignment.placeholder')}
          />
          <div className="modal-actions">
            <button type="button" onClick={onClose} disabled={isSubmitting}>
              {t('modal.cancel')}
            </button>
            <button
              type="submit"
              disabled={isSubmitting || selectedUserIds.length !== 1}
            >
              {isSubmitting
                ? t('bulkAssignment.assigning')
                : t('bulkAssignment.submit')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

BulkLotAssignmentModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  token: PropTypes.string,
  isSubmitting: PropTypes.bool,
};

export default BulkLotAssignmentModal;
