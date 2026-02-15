import React, { useState, useEffect } from 'react';

export default function OwnerEditUserModal({
  isOpen,
  user,
  onClose,
  onSave,
  isSaving,
}) {
  const [formValues, setFormValues] = useState({
    firstName: '',
    lastName: '',
    primaryEmail: '',
    phone: '',
    secondaryEmail: '',
  });

  useEffect(() => {
    if (user) {
      setFormValues({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        primaryEmail: user.primaryEmail || '',
        phone: user.phone || '',
        secondaryEmail: user.secondaryEmail || '',
      });
    }
  }, [user]);

  const handleChange = e => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = e => {
    e.preventDefault();
    onSave(formValues);
  };

  if (!isOpen || !user) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal owner-edit-modal"
        onClick={e => e.stopPropagation()}
      >
        <h2>Edit User Details (Owner)</h2>
        <p>As an owner, you can edit all fields for this user.</p>

        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <label>
              <span>
                First Name<span className="required">*</span>
              </span>
              <input
                type="text"
                name="firstName"
                value={formValues.firstName}
                onChange={handleChange}
                required
              />
            </label>

            <label>
              <span>
                Last Name<span className="required">*</span>
              </span>
              <input
                type="text"
                name="lastName"
                value={formValues.lastName}
                onChange={handleChange}
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <span>
                Primary Email (Login)
                <span className="required">*</span>
              </span>
              <input
                type="email"
                name="primaryEmail"
                value={formValues.primaryEmail}
                onChange={handleChange}
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <span>Secondary Email</span>
              <input
                type="email"
                name="secondaryEmail"
                value={formValues.secondaryEmail}
                onChange={handleChange}
                placeholder="example@email.com"
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <span>Phone Number</span>
              <input
                type="tel"
                name="phone"
                value={formValues.phone}
                onChange={handleChange}
                placeholder="(123) 456-7890"
              />
            </label>
          </div>

          <div className="user-info-section">
            <h3>Read-Only Information</h3>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">Role:</span>
                <span className="info-value role-badge">{user.userRole}</span>
              </div>
              <div className="info-item">
                <span className="info-label">User ID:</span>
                <span className="info-value">{user.userIdentifier}</span>
              </div>
            </div>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              disabled={isSaving}
              className="btn-cancel"
            >
              Cancel
            </button>
            <button type="submit" disabled={isSaving} className="btn-submit">
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
