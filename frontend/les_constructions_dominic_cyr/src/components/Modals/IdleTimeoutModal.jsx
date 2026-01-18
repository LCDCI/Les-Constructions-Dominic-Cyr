import React from 'react';
import PropTypes from 'prop-types';
import '../../styles/users.css';

export default function IdleTimeoutModal({ remainingSeconds, onStay, onLogout }) {
  const minutes = Math.floor(remainingSeconds / 60);
  const seconds = remainingSeconds % 60;

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ color: '#111827' }}>Still there?</h2>
          </div>

        <p>
          For your security, we'll log you out soon due to inactivity. Click "Stay signed in" to keep
          your session active.
        </p>

        <p><strong>Auto logout in: {minutes}:{seconds.toString().padStart(2, '0')}</strong></p>

        <div className="modal-actions">
          <button type="button" className="modal-secondary" onClick={onStay}>
            Stay signed in
          </button>
          <button type="button" className="modal-primary" onClick={onLogout}>
            Logout now
          </button>
        </div>
      </div>
    </div>
  );
}

IdleTimeoutModal.propTypes = {
  remainingSeconds: PropTypes.number.isRequired,
  onStay: PropTypes.func.isRequired,
  onLogout: PropTypes.func.isRequired,
};
