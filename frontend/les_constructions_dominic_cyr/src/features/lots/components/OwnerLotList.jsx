/* eslint-disable react/prop-types */
import React from 'react';
import { FiEdit2, FiTrash2 } from 'react-icons/fi';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './LotList.css';

function getRoleBadgeClass(role) {
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
}

export default function OwnerLotList({ lots = [], onEdit, onDelete }) {
  const { t } = usePageTranslations('ownerLots');

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

  const getTranslatedStatus = status => {
    switch (status?.toUpperCase()) {
      case 'AVAILABLE':
        return t('status.available');
      case 'RESERVED':
        return t('status.reserved');
      case 'SOLD':
        return t('status.sold');
      case 'PENDING':
        return t('status.pending');
      default:
        return status || 'UNKNOWN';
    }
  };

  if (!lots || lots.length === 0) {
    return (
      <div className="no-results">
        <p>{t('table.noLotsFound')}</p>
      </div>
    );
  }

  return (
    <div className="table-container">
      <table className="fixed-table">
        <thead>
          <tr>
            <th>{t('table.assignedUsers')}</th>
            <th>{t('table.lotNumber')}</th>
            <th>{t('table.buyer')}</th>
            <th>{t('table.buyerEmail')}</th>
            <th>{t('table.civicAddress')}</th>
            <th>{t('table.status')}</th>
            <th>{t('table.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {lots.map(l => {
            const buyer = l.assignedUsers?.find(
              user => (user.role || user.userRole)?.toUpperCase() === 'CUSTOMER'
            );
            return (
              <tr key={l.lotId}>
                <td className="users-cell">
                  {l.assignedUsers && l.assignedUsers.length > 0 ? (
                    <div className="assigned-users-list">
                      {l.assignedUsers.map((user, idx) => (
                        <div
                          key={user.userIdentifier || user.userId || idx}
                          className="assigned-user-item"
                        >
                          <span className="user-name">
                            {user.fullName ||
                              `${user.firstName || ''} ${user.lastName || ''}`.trim()}
                          </span>
                          <span
                            className={getRoleBadgeClass(
                              user.role || user.userRole
                            )}
                          >
                            {getTranslatedRole(user.role || user.userRole)}
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <span className="no-users">
                      {t('table.noUsersAssigned')}
                    </span>
                  )}
                </td>

                <td className="bold-cell">{l.id ?? '—'}</td>
                <td className="customer-cell">
                  {buyer?.fullName ||
                    `${buyer?.firstName || ''} ${buyer?.lastName || ''}`.trim() ||
                    '—'}
                </td>
                <td className="customer-cell">
                  {buyer?.primaryEmail || buyer?.email || '—'}
                </td>
                <td className="address-cell">{l.civicAddress || '—'}</td>
                <td>
                  <span
                    className={`status-pill ${String(l.lotStatus || '').toLowerCase()}`}
                  >
                    {getTranslatedStatus(l.lotStatus)}
                  </span>
                </td>
                <td className="actions-cell">
                  <div className="action-buttons">
                    <button
                      className="action-btn edit-btn"
                      onClick={() => onEdit?.(l)}
                      title={t('table.editLot')}
                    >
                      <FiEdit2 size={16} />
                    </button>
                    <button
                      className="action-btn delete-btn"
                      onClick={() => onDelete?.(l)}
                      title={t('table.deleteLot')}
                    >
                      <FiTrash2 size={16} />
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
