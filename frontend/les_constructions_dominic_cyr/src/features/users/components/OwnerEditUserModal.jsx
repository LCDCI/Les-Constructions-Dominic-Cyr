import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../../../hooks/usePageTranslations';

export default function OwnerEditUserModal({
  isOpen,
  user,
  onClose,
  onSave,
  isSaving,
}) {
  const { t } = usePageTranslations('usersPage');
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
        <h2>{t('ownerEdit.title', 'Edit User Details (Owner)')}</h2>
        <p>
          {t(
            'ownerEdit.description',
            'As an owner, you can edit all fields for this user.'
          )}
        </p>

        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-row">
            <label>
              <span>
                {t('ownerEdit.firstName', 'First Name')}
                <span className="required">*</span>
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
                {t('ownerEdit.lastName', 'Last Name')}
                <span className="required">*</span>
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
                {t('ownerEdit.primaryEmail', 'Primary Email (Login)')}
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
              <span>{t('ownerEdit.secondaryEmail', 'Secondary Email')}</span>
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
              <span>{t('ownerEdit.phone', 'Phone Number')}</span>
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
            <h3>{t('ownerEdit.readOnly', 'Read-Only Information')}</h3>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">
                  {t('ownerEdit.role', 'Role:')}
                </span>
                <span className="info-value role-badge">{user.userRole}</span>
              </div>
              <div className="info-item">
                <span className="info-label">
                  {t('ownerEdit.userId', 'User ID:')}
                </span>
                <span className="info-value">{user.userIdentifier}</span>
              </div>
              <div className="info-item">
                <span className="info-label">
                  {t('ownerEdit.activationKey', 'Activation Key:')}
                </span>
                <span className="info-value">{user.inviteLink || ''}</span>
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
              {t('ownerEdit.cancel', 'Cancel')}
            </button>
            <button type="submit" disabled={isSaving} className="btn-submit">
              {isSaving
                ? t('ownerEdit.saving', 'Saving...')
                : t('ownerEdit.save', 'Save Changes')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

OwnerEditUserModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  user: PropTypes.shape({
    firstName: PropTypes.string,
    lastName: PropTypes.string,
    primaryEmail: PropTypes.string,
    phone: PropTypes.string,
    secondaryEmail: PropTypes.string,
    userRole: PropTypes.string,
    userIdentifier: PropTypes.string,
    inviteLink: PropTypes.string,
  }),
  onClose: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  isSaving: PropTypes.bool,
};
