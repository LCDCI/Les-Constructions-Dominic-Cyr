import React, { useEffect, useState } from 'react';
import { fetchUsers, updateUser } from '../features/users/api/usersApi';
import '../styles/profile.css';

export default function ProfilePage() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    secondaryEmail: '',
  });

  useEffect(() => {
    const loadUser = async () => {
      try {
        setLoading(true);
        setError(null);
        // For now, we'll get the first user as a placeholder
        // In a real app, this would use auth context to get the current user
        const users = await fetchUsers();
        if (users && users.length > 0) {
          const currentUser = users[0]; // Placeholder - should get from auth context
          setUser(currentUser);
          setFormData({
            firstName: currentUser.firstName || '',
            lastName: currentUser.lastName || '',
            phone: currentUser.phone || '',
            secondaryEmail: currentUser.secondaryEmail || '',
          });
        }
      } catch (err) {
        console.error('Failed to load user:', err);
        setError('Failed to load profile information.');
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSave = async () => {
    if (!user) return;

    try {
      setIsSaving(true);
      const updatedUser = await updateUser(user.userIdentifier, formData);
      setUser(updatedUser);
      setIsEditing(false);
    } catch (err) {
      console.error('Failed to update user:', err);
      alert('Failed to update profile. Please try again.');
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
    setIsEditing(false);
  };

  if (loading) {
    return (
      <div className="profile-page">
        <p>Loading profile...</p>
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
        <p>No user information available.</p>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="profile-header">
        <h1>My Profile</h1>
        {!isEditing && (
          <button onClick={() => setIsEditing(true)} className="edit-button">
            Edit Profile
          </button>
        )}
      </div>

      <div className="profile-content">
        <div className="profile-section">
          <h2>Personal Information</h2>
          
          <div className="profile-field">
            <label>First Name</label>
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
            <label>Last Name</label>
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
            <label>Email</label>
            <div className="profile-value profile-value-readonly">
              {user.primaryEmail}
            </div>
            <small className="field-note">Email cannot be changed</small>
          </div>

          <div className="profile-field">
            <label>Secondary Email</label>
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
                {user.secondaryEmail || 'Not provided'}
              </div>
            )}
          </div>

          <div className="profile-field">
            <label>Phone</label>
            {isEditing ? (
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleInputChange}
                className="profile-input"
              />
            ) : (
              <div className="profile-value">{user.phone || 'Not provided'}</div>
            )}
          </div>

          <div className="profile-field">
            <label>Role</label>
            <div className="profile-value profile-value-readonly">
              {user.userRole}
            </div>
            <small className="field-note">Role cannot be changed</small>
          </div>
        </div>

        {isEditing && (
          <div className="profile-actions">
            <button
              onClick={handleSave}
              disabled={isSaving}
              className="save-button"
            >
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              onClick={handleCancel}
              disabled={isSaving}
              className="cancel-button"
            >
              Cancel
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
