import React from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../../../hooks/usePageTranslations';

export default function ErrorModal({
  isOpen,
  title = 'Error',
  message,
  onClose,
}) {
  const { t } = usePageTranslations('home');
  if (!isOpen) return null;

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>{title}</h2>
        <p>{message}</p>

        <div className="modal-actions">
          <button type="button" onClick={onClose}>
            {t('modals.close', 'Close')}
          </button>
        </div>
      </div>
    </div>
  );
}

ErrorModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  title: PropTypes.string,
  message: PropTypes.node.isRequired,
  onClose: PropTypes.func.isRequired,
};
