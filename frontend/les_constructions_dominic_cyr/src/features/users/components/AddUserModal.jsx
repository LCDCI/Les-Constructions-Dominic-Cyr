// src/features/users/components/AddUserModal.jsx
import React, { useState, useEffect } from 'react';

const USER_ROLES = ['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER'];
const USER_ROLE_LABELS = {
  OWNER: 'Owner',
  SALESPERSON: 'Salesperson',
  CONTRACTOR: 'Supplier',
  CUSTOMER: 'Customer',
};
const SUPPLIER_TYPES = [
  'Arpenteur',
  'Excavateur',
  'Fondation',
  'Charpentier',
  'Toiture',
  'Plombier',
  'Électricien',
  'Cimentier',
  'Porte de garage',
  'Ventilation',
  'Foyer',
  'Alarme & balayeuse',
  'Isolation',
  'Poseur de gypse',
  'Peintre',
  'Poseur de céramique',
  'Poseur de boiserie',
  'Cuisiniste',
  "Poseur d'escalier",
  'Poseur de plancher',
  'Aménagement garde-robe',
  'Vitrier',
  'Ménage',
  'Briqueteur',
  'Poseur revêtement extérieur',
  'Crépi',
  'Gouttières',
];

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
  const [supplierType, setSupplierType] = useState('');

  // Reset form when modal opens/closes
  useEffect(() => {
    if (isOpen) {
      setFirstName('');
      setLastName('');
      setPrimaryEmail('');
      setSecondaryEmail('');
      setPhone('');
      setUserRole('CUSTOMER');
      setSupplierType('');
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
      supplierType: userRole === 'CONTRACTOR' ? supplierType : null,
    });
  };

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>Add User</h2>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <label>
              <span>{userRole === 'CONTRACTOR' ? 'Company Name' : 'First Name'}<span className="required">*</span></span>
              <input
                type="text"
                value={firstName}
                onChange={e => setFirstName(e.target.value)}
                required
              />
            </label>
            <label>
              <span>
                {userRole === 'CONTRACTOR'
                  ? 'Representative Full Name'
                  : 'Last Name'}
                {userRole !== 'CONTRACTOR' && (
                  <span className="required">*</span>
                )}
              </span>
              <input
                type="text"
                value={lastName}
                onChange={e => setLastName(e.target.value)}
                required={userRole !== 'CONTRACTOR'}
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <span>
                Primary Email<span className="required">*</span>
              </span>
              <input
                type="email"
                value={primaryEmail}
                onChange={e => setPrimaryEmail(e.target.value)}
                required
              />
            </label>
            <label>
              <span>Secondary Email</span>
              <input
                type="email"
                value={secondaryEmail}
                onChange={e => setSecondaryEmail(e.target.value)}
              />
            </label>
          </div>

          {userRole === 'CONTRACTOR' && (
            <div className="form-row">
              <label>
                <span>
                  Supplier Type<span className="required">*</span>
                </span>
                <select
                  value={supplierType}
                  onChange={e => setSupplierType(e.target.value)}
                  required
                >
                  <option value="">Select a supplier type</option>
                  {SUPPLIER_TYPES.map(type => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}

          <div className="form-row">
            <label>
              <span>
                Phone<span className="required">*</span>
              </span>
              <input
                type="tel"
                value={phone}
                onChange={e => setPhone(e.target.value)}
                required
              />
            </label>

            <label>
              <span>
                Role<span className="required">*</span>
              </span>
              <select
                value={userRole}
                onChange={e => setUserRole(e.target.value)}
                required
              >
                {USER_ROLES.map(role => (
                  <option key={role} value={role}>
                    {USER_ROLE_LABELS[role]}
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
