// src/features/users/components/AddUserModal.jsx
import React, { useState, useEffect } from 'react';

const USER_ROLES = ['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER'];

export default function AddUserModal({
  isOpen,
  onClose,
  onCreate,
  isSubmitting,
}) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [primaryEmail, setPrimaryEmail] = useState('');
  const [secondaryEmail, setSecondaryEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [userRole, setUserRole] = useState('CUSTOMER');

  // Reset form when modal opens/closes
  useEffect(() => {
    if (isOpen) {
      setFirstName('');
      setLastName('');
      setPrimaryEmail('');
      setSecondaryEmail('');
      setPhone('');
      setUserRole('CUSTOMER');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = e => {
    e.preventDefault();
    onCreate({
      firstName,
      lastName,
      primaryEmail,
      secondaryEmail,
      phone,
      userRole,
    });
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>Add User</h2>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <label>
              <div className="label-row">
                <span>First Name</span>
                <span className="required">*</span>
              </div>
              <input
                type="text"
                value={firstName}
                onChange={e => setFirstName(e.target.value)}
                required
              />
            </label>
            <label>
              <div className="label-row">
                <span>Last Name</span>
                <span className="required">*</span>
              </div>
              <input
                type="text"
                value={lastName}
                onChange={e => setLastName(e.target.value)}
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <div className="label-row">
                <span>Primary Email</span>
                <span className="required">*</span>
              </div>
              <input
                type="email"
                value={primaryEmail}
                onChange={e => setPrimaryEmail(e.target.value)}
                required
              />
            </label>
            <label>
              <div className="label-row">
                <span>Secondary Email</span>
              </div>
              <input
                type="email"
                value={secondaryEmail}
                onChange={e => setSecondaryEmail(e.target.value)}
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <div className="label-row">
                <span>Phone</span>
                <span className="required">*</span>
              </div>
              <input
                type="tel"
                value={phone}
                onChange={e => setPhone(e.target.value)}
                required
              />
            </label>

            <label>
              <div className="label-row">
                <span>Role</span>
                <span className="required">*</span>
              </div>
              <select
                value={userRole}
                onChange={e => setUserRole(e.target.value)}
                required
              >
                {USER_ROLES.map(role => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="btn-cancel"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-submit"
            >
              {isSubmitting ? 'Creating...' : 'Create User'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
