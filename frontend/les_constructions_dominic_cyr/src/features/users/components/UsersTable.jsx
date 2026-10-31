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

  const getRoleLabel = role =>
    ({
      OWNER: t('roleOptions.owner', 'Owner'),
      CONTRACTOR: t('roleOptions.supplier', 'Supplier'),
      SALESPERSON: t('roleOptions.salesperson', 'Salesperson'),
      CUSTOMER: t('roleOptions.customer', 'Customer'),
    })[role] || role;

  if (!users || users.length === 0) {
    return <p>{t('table.noUsersFound', 'No users found.')}</p>;
  }

  return (
    <div className="users-table-wrapper">
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
          {Object.entries(
            users.reduce((groups, user) => {
              const role = user.userRole || 'UNKNOWN';
              groups[role] = [...(groups[role] || []), user];
              return groups;
            }, {})
          ).map(([role, roleUsers]) => (
            <React.Fragment key={role}>
              <tr className="users-role-divider">
                <th colSpan="8">{getRoleLabel(role)}</th>
              </tr>
              {roleUsers.map(u => (
                <tr key={u.userIdentifier}>
              <td>{u.firstName}</td>
              <td>{u.lastName}</td>
              <td>{u.primaryEmail}</td>
              <td>{u.secondaryEmail || t('table.notAvailable', 'N/A')}</td>
              <td>{u.phone || t('table.notAvailable', 'N/A')}</td>
              <td>{getRoleLabel(u.userRole)}</td>
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
            </React.Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
}
