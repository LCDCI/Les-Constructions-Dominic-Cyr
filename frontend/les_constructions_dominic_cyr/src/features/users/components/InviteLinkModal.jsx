// src/features/users/components/InviteLinkModal.jsx
import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../../../hooks/usePageTranslations';

export default function InviteLinkModal({ isOpen, inviteLink, onClose }) {
  const [copyStatus, setCopyStatus] = useState('');
  const { t } = usePageTranslations('usersPage');

  if (!isOpen) return null;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(inviteLink);
      setCopyStatus(t('inviteLink.copied', 'Link copied to clipboard.'));
    } catch {
      setCopyStatus(
        t('inviteLink.copyFailed', 'Could not copy. Please copy manually.')
      );
    }
  };

  const handleClose = () => {
    setCopyStatus('');
    onClose();
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>{t('inviteLink.title', 'User Invite Link')}</h2>
        <p>
          {t(
            'inviteLink.description',
            'Send this link to the user so they can set their password and access the portal.'
          )}
        </p>
        <div className="invite-link-row">
          <input
            type="text"
            value={inviteLink}
            readOnly
            onFocus={e => e.target.select()}
          />
          <button type="button" onClick={handleCopy}>
            {t('inviteLink.copy', 'Copy')}
          </button>
        </div>

        {copyStatus && <p className="copy-status">{copyStatus}</p>}

        <div className="modal-actions">
          <button type="button" onClick={handleClose}>
            {t('inviteLink.close', 'Close')}
          </button>
        </div>
      </div>
    </div>
  );
}

InviteLinkModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  inviteLink: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
};
