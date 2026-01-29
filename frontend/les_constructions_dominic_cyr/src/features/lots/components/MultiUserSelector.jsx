/* eslint-disable no-console */
import React, { useState, useEffect, useRef } from 'react';
import { fetchUsers } from '../../users/api/usersApi';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import PropTypes from 'prop-types';
import './MultiUserSelector.css';

const MultiUserSelector = ({
  selectedUserIds = [],
  onChange,
  token,
  placeholder = 'Select users...',
}) => {
  const { t } = usePageTranslations('ownerLots');

  const [users, setUsers] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const loadUsers = async () => {
      if (!token) return;

      setLoading(true);
      try {
        const allUsers = await fetchUsers(token);
        // Filter to only include contractors, customers, and salespersons
        const eligibleUsers = (allUsers || []).filter(user =>
          ['CONTRACTOR', 'CUSTOMER', 'SALESPERSON'].includes(
            user.userRole?.toUpperCase()
          )
        );
        setUsers(eligibleUsers);
      } catch (err) {
        console.error('Failed to load users:', err);
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, [token]);

  useEffect(() => {
    const handleClickOutside = event => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const filteredUsers = users.filter(user => {
    const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
    const email = (user.primaryEmail || '').toLowerCase();
    const role = (user.userRole || '').toLowerCase();
    const search = searchTerm.toLowerCase();
    return (
      fullName.includes(search) ||
      email.includes(search) ||
      role.includes(search)
    );
  });

  const selectedUsers = users.filter(user =>
    selectedUserIds.includes(user.userIdentifier)
  );

  const handleToggleUser = userId => {
    if (selectedUserIds.includes(userId)) {
      onChange(selectedUserIds.filter(id => id !== userId));
    } else {
      onChange([...selectedUserIds, userId]);
    }
  };

  const handleRemoveUser = (e, userId) => {
    e.stopPropagation();
    onChange(selectedUserIds.filter(id => id !== userId));
  };

  const getRoleBadgeClass = role => {
    switch (role?.toUpperCase()) {
      case 'CUSTOMER':
        return 'role-badge customer';
      case 'CONTRACTOR':
        return 'role-badge contractor';
      case 'SALESPERSON':
        return 'role-badge salesperson';
      default:
        return 'role-badge';
    }
  };

  const getTranslatedRole = role => {
    switch (role?.toUpperCase()) {
      case 'CUSTOMER':
        return t('roles.customer');
      case 'CONTRACTOR':
        return t('roles.contractor');
      case 'SALESPERSON':
        return t('roles.salesperson');
      default:
        return role;
    }
  };

  return (
    <div className="multi-user-selector" ref={dropdownRef}>
      <div
        className={`multi-user-selector-input ${isOpen ? 'open' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        {selectedUsers.length > 0 ? (
          <div className="selected-users-chips">
            {selectedUsers.map(user => (
              <div key={user.userIdentifier} className="user-chip">
                <span className="chip-name">
                  {user.firstName} {user.lastName}
                </span>
                <span className={getRoleBadgeClass(user.userRole)}>
                  {getTranslatedRole(user.userRole)}
                </span>
                <button
                  type="button"
                  className="chip-remove"
                  onClick={e => handleRemoveUser(e, user.userIdentifier)}
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        ) : (
          <span className="placeholder">{placeholder}</span>
        )}
        <span className={`dropdown-arrow ${isOpen ? 'up' : 'down'}`}>▼</span>
      </div>

      {isOpen && (
        <div className="multi-user-selector-dropdown">
          <div className="search-box">
            <input
              type="text"
              placeholder={t('userSelector.searchPlaceholder')}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              className="search-input"
              autoFocus
            />
          </div>

          <div className="users-list">
            {loading ? (
              <div className="loading">{t('userSelector.loadingUsers')}</div>
            ) : filteredUsers.length === 0 ? (
              <div className="no-results">
                {searchTerm
                  ? t('userSelector.noUsersFound')
                  : t('userSelector.noUsersAvailable')}
              </div>
            ) : (
              filteredUsers.map(user => (
                <div
                  key={user.userIdentifier}
                  className={`user-option ${selectedUserIds.includes(user.userIdentifier) ? 'selected' : ''}`}
                  onClick={() => handleToggleUser(user.userIdentifier)}
                >
                  <div className="user-checkbox">
                    <input
                      type="checkbox"
                      checked={selectedUserIds.includes(user.userIdentifier)}
                      onChange={() => {}}
                      onClick={e => e.stopPropagation()}
                    />
                  </div>
                  <div className="user-info">
                    <div className="user-name-row">
                      <span className="user-name">
                        {user.firstName} {user.lastName}
                      </span>
                      <span className={getRoleBadgeClass(user.userRole)}>
                        {getTranslatedRole(user.userRole)}
                      </span>
                    </div>
                    <div className="user-email">{user.primaryEmail}</div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};

MultiUserSelector.propTypes = {
  selectedUserIds: PropTypes.arrayOf(PropTypes.string),
  onChange: PropTypes.func.isRequired,
  token: PropTypes.string,
  placeholder: PropTypes.string,
};

export default MultiUserSelector;
