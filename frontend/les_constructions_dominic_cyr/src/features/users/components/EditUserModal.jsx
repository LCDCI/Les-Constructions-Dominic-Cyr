import React, { useState, useEffect } from 'react';

export default function EditUserModal({
  isOpen,
  user,
  onClose,
  onSave,
  isSaving,
}) {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    secondaryEmail: '',
  });

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phone: user.phone || '',
        secondaryEmail: user.secondaryEmail || '',
      });
    }
  }, [user]);

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

  if (!isOpen) return null;

  const handleChange = e => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = e => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="edit-user-modal-title"
        aria-describedby="edit-user-modal-description"
      >
        <h2 id="edit-user-modal-title">Edit User</h2>
        <p id="edit-user-modal-description">
          Update user profile details and save changes.
        </p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="edit-user-first-name">First Name</label>
            <input
              id="edit-user-first-name"
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="edit-user-last-name">Last Name</label>
            <input
              id="edit-user-last-name"
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="edit-user-primary-email">Primary Email</label>
            <input
              id="edit-user-primary-email"
              type="email"
              value={user?.primaryEmail || ''}
              disabled
              className="disabled-input"
            />
            <small>Email cannot be changed</small>
          </div>

          <div className="form-group">
            <label htmlFor="edit-user-secondary-email">Secondary Email</label>
            <input
              id="edit-user-secondary-email"
              type="email"
              name="secondaryEmail"
              value={formData.secondaryEmail}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="edit-user-phone">Phone</label>
            <input
              id="edit-user-phone"
              type="tel"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="edit-user-role">Role</label>
            <input
              id="edit-user-role"
              type="text"
              value={user?.userRole || ''}
              disabled
              className="disabled-input"
            />
            <small>Role cannot be changed</small>
          </div>

          <div className="modal-actions">
            <button type="submit" disabled={isSaving} className="btn-primary">
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              type="button"
              onClick={onClose}
              disabled={isSaving}
              className="btn-secondary"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
