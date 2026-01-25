import React, { useState, useEffect, useRef } from 'react';
import { fetchAllCustomers } from '../../users/api/usersApi';
import PropTypes from 'prop-types';
import './UserSelector.css';

const UserSelector = ({ value, onChange, token, placeholder = "Select a customer..." }) => {
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
        const customersData = await fetchAllCustomers(token);
        setUsers(customersData || []);
      } catch (err) {
        setUsers([]);
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, [token]);

  useEffect(() => {
    const handleClickOutside = (event) => {
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
    const search = searchTerm.toLowerCase();
    return fullName.includes(search) || email.includes(search);
  });

  const selectedUser = value ? users.find(user => user.userIdentifier === value) : null;

  const handleSelectUser = (userId) => {
    onChange(userId);
    setIsOpen(false);
    setSearchTerm('');
  };

  const handleClearSelection = () => {
    onChange('');
    setSearchTerm('');
  };

  return (
    <div className="user-selector" ref={dropdownRef}>
      <div
        className={`user-selector-input ${isOpen ? 'open' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        {selectedUser ? (
          <div className="selected-user">
            <span className="user-name">
              {selectedUser.firstName} {selectedUser.lastName}
            </span>
            <span className="user-email">({selectedUser.primaryEmail})</span>
            <button
              type="button"
              className="clear-button"
              onClick={(e) => {
                e.stopPropagation();
                handleClearSelection();
              }}
            >
              ×
            </button>
          </div>
        ) : (
          <span className="placeholder">{placeholder}</span>
        )}
        <span className={`dropdown-arrow ${isOpen ? 'up' : 'down'}`}>▼</span>
      </div>

      {isOpen && (
        <div className="user-selector-dropdown">
          <div className="search-box">
            <input
              type="text"
              placeholder="Search by name or email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
              autoFocus
            />
          </div>

          <div className="users-list">
            {loading ? (
              <div className="loading">Loading customers...</div>
            ) : filteredUsers.length === 0 ? (
              <div className="no-results">
                {searchTerm ? 'No customers found matching search' : 'No customers available'}
              </div>
            ) : (
              filteredUsers.map(user => (
                <div
                  key={user.userIdentifier}
                  className={`user-option ${value === user.userIdentifier ? 'selected' : ''}`}
                  onClick={() => handleSelectUser(user.userIdentifier)}
                >
                  <div className="user-info">
                    <div className="user-name">
                      {user.firstName} {user.lastName}
                    </div>
                    <div className="user-email">
                      {user.primaryEmail}
                    </div>
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

UserSelector.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func.isRequired,
  token: PropTypes.string,
  placeholder: PropTypes.string,
};

export default UserSelector;
