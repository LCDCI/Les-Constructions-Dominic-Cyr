/* eslint-disable react/prop-types */
import React from 'react';
import { FiEdit2, FiTrash2 } from 'react-icons/fi';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './LotList.css';

function formatPrice(p, lang) {
  if (!p) return '—';
  const n = typeof p === 'number' ? p : Number(p);
  return !Number.isNaN(n)
    ? new Intl.NumberFormat(lang === 'fr' ? 'fr-CA' : 'en-CA', {
        style: 'currency',
        currency: 'CAD',
      }).format(n)
    : String(p);
}

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
  const { t, i18n } = usePageTranslations('ownerLots');
  const lang = i18n?.language?.startsWith('fr') ? 'fr' : 'en';

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
            <th style={{ width: '22%' }}>{t('table.assignedUsers')}</th>
            <th style={{ width: '10%' }}>{t('table.lotNumber')}</th>
            <th style={{ width: '20%' }}>{t('table.civicAddress')}</th>
            <th style={{ width: '10%' }}>{t('table.areaSqft')}</th>
            <th style={{ width: '10%' }}>{t('table.areaSqm')}</th>
            <th style={{ width: '12%' }}>{t('table.price')}</th>
            <th style={{ width: '10%' }}>{t('table.status')}</th>
            <th style={{ width: '8%' }}>{t('table.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {lots.map(l => (
            <tr key={l.lotId}>
              <td className="users-cell">
                {l.assignedUsers && l.assignedUsers.length > 0 ? (
                  <div className="assigned-users-list">
                    {l.assignedUsers.map((user, idx) => (
                      <div
                        key={user.userId || idx}
                        className="assigned-user-item"
                      >
                        <span className="user-name">{user.fullName}</span>
                        <span className={getRoleBadgeClass(user.role)}>
                          {getTranslatedRole(user.role)}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className="no-users">{t('table.noUsersAssigned')}</span>
                )}
              </td>

              <td className="bold-cell">{l.lotNumber || '—'}</td>
              <td className="address-cell">{l.civicAddress || '—'}</td>
              <td>{l.dimensionsSquareFeet || '—'}</td>
              <td>{l.dimensionsSquareMeters || '—'}</td>
              <td className="price-cell">{formatPrice(l.price, lang)}</td>
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
          ))}
        </tbody>
      </table>
    </div>
  );
}
