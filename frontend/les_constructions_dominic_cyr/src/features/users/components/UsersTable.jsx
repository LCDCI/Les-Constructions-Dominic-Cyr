import React from 'react';

export default function UsersTable({ users }) {
  if (!users || users.length === 0) {
    return <p>No users found.</p>;
  }

  return (
    <table className="users-table">
      <thead>
        <tr>
          <th>First name</th>
          <th>Last name</th>
          <th>Primary email</th>
          <th>Secondary email</th>
          <th>Phone</th>
          <th>Role</th>
        </tr>
      </thead>
      <tbody>
        {users.map((u) => (
          <tr key={u.userIdentifier}>
            <td>{u.firstName}</td>
            <td>{u.lastName}</td>
            <td>{u.primaryEmail}</td>
            <td>{u.secondaryEmail}</td>
            <td>{u.phone}</td>
            <td>{u.userRole}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
