import React from 'react';
import '../../../styles/users.css';
import { usePageTranslations } from '../../../hooks/usePageTranslations';

export default function UsersTable({
  users,
  onEditUser,
  onManageStatus,
  currentUser,
}) {
  const { t } = usePageTranslations('usersPage');
  const isOwner = currentUser?.userRole === 'OWNER';

  const getStatusBadge = status => {
    const statusClass = status ? status.toLowerCase() : 'active';
    const statusText = status || 'ACTIVE';
    return (
      <span className={`status-badge status-${statusClass}`}>{statusText}</span>
    );
  };

  if (!users || users.length === 0) {
    return <p>{t('table.noUsersFound', 'No users found.')}</p>;
  }

  return (
    <table className="users-table">
      <thead>
        <tr>
          <th>{t('table.firstName', 'First Name')}</th>
          <th>{t('table.lastName', 'Last Name')}</th>
          <th>{t('table.primaryEmail', 'Primary Email')}</th>
          <th>{t('table.secondaryEmail', 'Secondary Email')}</th>
          <th>{t('table.phone', 'Phone')}</th>
          <th>{t('table.role', 'Role')}</th>
          <th>{t('table.status', 'Status')}</th>
          <th>{t('table.actions', 'Actions')}</th>
        </tr>
      </thead>
      <tbody>
        {users.map(u => (
          <tr key={u.userIdentifier}>
            <td>{u.firstName}</td>
            <td>{u.lastName}</td>
            <td>{u.primaryEmail}</td>
            <td>{u.secondaryEmail || t('table.notAvailable', 'N/A')}</td>
            <td>{u.phone || t('table.notAvailable', 'N/A')}</td>
            <td>{u.userRole}</td>
            <td>{getStatusBadge(u.userStatus)}</td>
            <td>
              <button className="btn-edit" onClick={() => onEditUser(u)}>
                {t('table.edit', 'Edit')}
              </button>
              {isOwner && u.userRole !== 'OWNER' && (
                <button
                  className="btn-manage-status"
                  onClick={() => onManageStatus(u)}
                >
                  {t('table.manageStatus', 'Manage Status')}
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
