import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchUserByAuth0Id, updateUser } from '../features/users/api/usersApi';
import '../styles/profile.css';
import { usePageTranslations } from '../hooks/usePageTranslations';

export default function ProfilePage() {
  const { t } = usePageTranslations('profilePage');
  const {
    user: auth0User,
    isLoading: auth0Loading,
    getAccessTokenSilently,
  } = useAuth0();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    secondaryEmail: '',
  });

  useEffect(() => {
    const loadUser = async () => {
      // Wait for Auth0 to finish loading
      if (auth0Loading) {
        return;
      }

      // Check if user is authenticated
      if (!auth0User) {
        setError(t('errors.notLoggedIn', 'You must be logged in to view your profile.'));
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        // Fetch user from backend using Auth0 user ID
        const auth0UserId = auth0User.sub;
        if (!auth0UserId || auth0UserId.trim() === '') {
          setError(t('errors.invalidAuth', 'Invalid authentication data. Please log in again.'));
          setLoading(false);
          return;
        }
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
          },
        });
        const currentUser = await fetchUserByAuth0Id(auth0UserId, token);
        setUser(currentUser);
        setFormData({
          firstName: currentUser.firstName || '',
          lastName: currentUser.lastName || '',
          phone: currentUser.phone || '',
          secondaryEmail: currentUser.secondaryEmail || '',
        });
      } catch (err) {
        console.error('Failed to load user:', err);
        if (err.response?.status === 404) {
          setError(t('errors.notFound', 'Your user profile was not found. Please contact support.'));
        } else {
          setError(t('errors.loadFailed', 'Failed to load profile information. Please try again later.'));
        }
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, [auth0User, auth0Loading]);

  const handleInputChange = e => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSave = async () => {
    if (!user) return;

    try {
      setIsSaving(true);
      setSaveError(null);
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: import.meta.env.VITE_AUTH0_AUDIENCE },
      });
      const updatedUser = await updateUser(
        user.userIdentifier,
        formData,
        token
      );
      setUser(updatedUser);
      setIsEditing(false);
    } catch (err) {
      console.error('Failed to update user:', err);
      setSaveError(t('errors.updateFailed', 'Failed to update profile. Please try again.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phone: user.phone || '',
        secondaryEmail: user.secondaryEmail || '',
      });
    }
    setSaveError(null);
    setIsEditing(false);
  };

  if (loading) {
    return (
      <div className="profile-page">
        <p>{t('loading', 'Loading profile...')}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profile-page">
        <p className="error">{error}</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="profile-page">
        <p>{t('noUserInfo', 'No user information available.')}</p>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="profile-header">
        <h1>{t('title', 'My Profile')}</h1>
        {!isEditing && (
          <button onClick={() => setIsEditing(true)} className="edit-button">
            {t('editProfile', 'Edit Profile')}
          </button>
        )}
      </div>

      <div className="profile-content">
        {saveError && <div className="save-error-message">{saveError}</div>}

        <div className="profile-section">
          <h2>{t('personalInfo', 'Personal Information')}</h2>

          <div className="profile-field">
            <label>{t('fields.firstName', 'First Name')}</label>
            {isEditing ? (
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                className="profile-input"
              />
            ) : (
              <div className="profile-value">{user.firstName}</div>
            )}
          </div>

          <div className="profile-field">
            <label>{t('fields.lastName', 'Last Name')}</label>
            {isEditing ? (
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                className="profile-input"
              />
            ) : (
              <div className="profile-value">{user.lastName}</div>
            )}
          </div>

          <div className="profile-field">
            <label>{t('fields.email', 'Email')}</label>
            <div className="profile-value profile-value-readonly">
              {user.primaryEmail}
            </div>
            <small className="field-note">{t('emailCannotChange', 'Email cannot be changed')}</small>
          </div>

          <div className="profile-field">
            <label>{t('fields.secondaryEmail', 'Secondary Email')}</label>
            {isEditing ? (
              <input
                type="email"
                name="secondaryEmail"
                value={formData.secondaryEmail}
                onChange={handleInputChange}
                className="profile-input"
              />
            ) : (
              <div className="profile-value">
                {user.secondaryEmail || t('notProvided', 'Not provided')}
              </div>
            )}
          </div>

          <div className="profile-field">
            <label>{t('fields.phone', 'Phone')}</label>
            {isEditing ? (
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                className="profile-input"
              />
            ) : (
              <div className="profile-value">
                {user.phone || t('notProvided', 'Not provided')}
              </div>
            )}
          </div>

          <div className="profile-field">
            <label>{t('fields.role', 'Role')}</label>
            <div className="profile-value profile-value-readonly">
              {user.userRole}
            </div>
            <small className="field-note">{t('roleCannotChange', 'Role cannot be changed')}</small>
          </div>
        </div>

        {isEditing && (
          <div className="profile-actions">
            <button
              onClick={handleSave}
              disabled={isSaving}
              className="save-button"
            >
              {isSaving ? t('saving', 'Saving...') : t('saveChanges', 'Save Changes')}
            </button>
            <button
              onClick={handleCancel}
              disabled={isSaving}
              className="cancel-button"
            >
              {t('cancel', 'Cancel')}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
