// src/features/users/components/InviteLinkModal.jsx
import React, { useState } from 'react';

export default function InviteLinkModal({ isOpen, inviteLink, onClose }) {
  const [copyStatus, setCopyStatus] = useState('');

  if (!isOpen) return null;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(inviteLink);
      setCopyStatus('Link copied to clipboard.');
    } catch (err) {
      console.error('Failed to copy:', err);
      setCopyStatus('Could not copy. Please copy manually.');
    }
  };

  const handleClose = () => {
    setCopyStatus('');
    onClose();
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>User Invite Link</h2>
        <p>
          Send this link to the client so they can set their password and access
          the portal.
        </p>
        <div className="invite-link-row">
          <input
            type="text"
            value={inviteLink}
            readOnly
            onFocus={e => e.target.select()}
          />
          <button type="button" onClick={handleCopy}>
            Copy
          </button>
        </div>

        {copyStatus && <p className="copy-status">{copyStatus}</p>}

        <div className="modal-actions">
          <button type="button" onClick={handleClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
