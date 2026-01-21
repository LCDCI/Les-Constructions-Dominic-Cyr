import React from 'react';
import '../../../styles/users.css';

export default function UsersTable({
  users,
  onEditUser,
  onManageStatus,
  currentUser,
}) {
  const isOwner = currentUser?.userRole === 'OWNER';

  const getStatusBadge = status => {
    const statusClass = status ? status.toLowerCase() : 'active';
    const statusText = status || 'ACTIVE';
    return (
      <span className={`status-badge status-${statusClass}`}>{statusText}</span>
    );
  };

  if (!users || users.length === 0) {
    return <p>No users found.</p>;
  }

  return (
    <table className="users-table">
      <thead>
        <tr>
          <th>First Name</th>
          <th>Last Name</th>
          <th>Primary Email</th>
          <th>Secondary Email</th>
          <th>Phone</th>
          <th>Role</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {users.map(u => (
          <tr key={u.userIdentifier}>
            <td>{u.firstName}</td>
            <td>{u.lastName}</td>
            <td>{u.primaryEmail}</td>
            <td>{u.secondaryEmail || 'N/A'}</td>
            <td>{u.phone || 'N/A'}</td>
            <td>{u.userRole}</td>
            <td>{getStatusBadge(u.userStatus)}</td>
            <td>
              <button className="btn-edit" onClick={() => onEditUser(u)}>
                Edit
              </button>
              {isOwner && u.userRole !== 'OWNER' && (
                <button
                  className="btn-manage-status"
                  onClick={() => onManageStatus(u)}
                >
                  Manage Status
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
