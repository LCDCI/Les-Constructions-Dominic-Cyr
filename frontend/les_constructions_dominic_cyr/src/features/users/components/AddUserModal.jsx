// src/features/users/components/AddUserModal.jsx
import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

const USER_ROLES = ['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER'];
const SUPPLIER_TYPES = [
  ['arpenteur', 'Arpenteur'],
  ['excavateur', 'Excavateur'],
  ['fondation', 'Fondation'],
  ['charpentier', 'Charpentier'],
  ['toiture', 'Toiture'],
  ['plombier', 'Plombier'],
  ['electricien', 'Électricien'],
  ['cimentier', 'Cimentier'],
  ['porteDeGarage', 'Porte de garage'],
  ['ventilation', 'Ventilation'],
  ['foyer', 'Foyer'],
  ['alarmeBalayeuse', 'Alarme & balayeuse'],
  ['isolation', 'Isolation'],
  ['poseurDeGypse', 'Poseur de gypse'],
  ['peintre', 'Peintre'],
  ['poseurDeCeramique', 'Poseur de céramique'],
  ['poseurDeBoiserie', 'Poseur de boiserie'],
  ['cuisiniste', 'Cuisiniste'],
  ['poseurDEscalier', "Poseur d'escalier"],
  ['poseurDePlancher', 'Poseur de plancher'],
  ['amenagementGardeRobe', 'Aménagement garde-robe'],
  ['vitrier', 'Vitrier'],
  ['menage', 'Ménage'],
  ['briqueteur', 'Briqueteur'],
  ['poseurRevetementExterieur', 'Poseur revêtement extérieur'],
  ['crepi', 'Crépi'],
  ['gouttieres', 'Gouttières'],
].sort((a, b) => a[1].localeCompare(b[1], 'fr'));

export default function AddUserModal({
  isOpen,
  onClose,
  onCreate,
  isSubmitting,
}) {
  const { t } = useTranslation('usersPage');
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
        <h2>{t('addUserModal.title')}</h2>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-row">
            <label>
              <span>
                {userRole === 'CONTRACTOR'
                  ? t('addUserModal.companyName')
                  : t('addUserModal.firstName')}
                <span className="required">*</span>
              </span>
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
                  ? t('addUserModal.representativeName')
                  : t('addUserModal.lastName')}
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
                {t('addUserModal.primaryEmail')}
                <span className="required">*</span>
              </span>
              <input
                type="email"
                value={primaryEmail}
                onChange={e => setPrimaryEmail(e.target.value)}
                required
              />
            </label>
            <label>
              <span>{t('addUserModal.secondaryEmail')}</span>
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
                  {t('addUserModal.supplierType')}
                  <span className="required">*</span>
                </span>
                <select
                  value={supplierType}
                  onChange={e => setSupplierType(e.target.value)}
                  required
                >
                  <option value="">
                    {t('addUserModal.selectSupplierType')}
                  </option>
                  {SUPPLIER_TYPES.map(([key, value]) => (
                    <option key={key} value={value}>
                      {t(`addUserModal.supplierTypes.${key}`)}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}

          <div className="form-row">
            <label>
              <span>
                {t('addUserModal.phone')}
                <span className="required">*</span>
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
                {t('addUserModal.role')}
                <span className="required">*</span>
              </span>
              <select
                value={userRole}
                onChange={e => setUserRole(e.target.value)}
                required
              >
                {USER_ROLES.map(role => (
                  <option key={role} value={role}>
                    {t(
                      `roleOptions.${role === 'CONTRACTOR' ? 'supplier' : role.toLowerCase()}`
                    )}
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
              {t('addUserModal.cancel')}
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-submit"
            >
              {isSubmitting
                ? t('addUserModal.creating')
                : t('addUserModal.create')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
