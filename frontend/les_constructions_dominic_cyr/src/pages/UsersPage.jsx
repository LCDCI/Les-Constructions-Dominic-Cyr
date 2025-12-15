// src/pages/UsersPage.jsx
import React, { useEffect, useState } from 'react';
import { fetchUsers, createUser, updateUser } from '../features/users/api/usersApi';
import UsersTable from '../features/users/components/UsersTable';
import AddUserModal from '../features/users/components/AddUserModal';
import EditUserModal from '../features/users/components/EditUserModal';
import InviteLinkModal from '../features/users/components/InviteLinkModal';
import ErrorModal from '../features/users/components/ErrorModal';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingError, setLoadingError] = useState(null);

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [inviteLink, setInviteLink] = useState('');
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

  const [errorMessage, setErrorMessage] = useState('');
  const [isErrorModalOpen, setIsErrorModalOpen] = useState(false);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [isEditSubmitting, setIsEditSubmitting] = useState(false);

  // Load users on mount
  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setLoadingError(null);
        const data = await fetchUsers();
        setUsers(data);
      } catch (err) {
        console.error(err);
        setLoadingError('Failed to load users.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  const openErrorModal = (message) => {
    setErrorMessage(message);
    setIsErrorModalOpen(true);
  };

  const handleCreateUser = async (formValues) => {
    try {
      setIsSubmitting(true);

      const createdUser = await createUser(formValues);

      // Add new user to list
      setUsers((prev) => [...prev, createdUser]);

      // Show invite link modal
      if (createdUser.inviteLink) {
        setInviteLink(createdUser.inviteLink);
        setIsInviteModalOpen(true);
      }

      setIsAddModalOpen(false);
    } catch (err) {
      console.error(err);

      let niceMessage = 'Failed to create user. Please try again.';
      if (err.response && err.response.data && err.response.data.message) {
        niceMessage = err.response.data.message;
      }

      openErrorModal(niceMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditUser = (user) => {
    setEditingUser(user);
    setIsEditModalOpen(true);
  };

  const handleUpdateUser = async (formValues) => {
    try {
      setIsEditSubmitting(true);

      const updatedUser = await updateUser(editingUser.userIdentifier, formValues);

      // Update user in list
      setUsers((prev) =>
        prev.map((u) =>
          u.userIdentifier === updatedUser.userIdentifier ? updatedUser : u
        )
      );

      setIsEditModalOpen(false);
      setEditingUser(null);
    } catch (err) {
      console.error(err);

      let niceMessage = 'Failed to update user. Please try again.';
      if (err.response && err.response.data && err.response.data.message) {
        niceMessage = err.response.data.message;
      }

      openErrorModal(niceMessage);
    } finally {
      setIsEditSubmitting(false);
    }
  };

  return (
    <div className="page users-page">
      <div className="page-header">
        <h1>Users</h1>
        <button onClick={() => setIsAddModalOpen(true)}>Add User</button>
      </div>

      {loading && <p>Loading users...</p>}
      {loadingError && <p className="error">{loadingError}</p>}

      {!loading && !loadingError && (
        <UsersTable users={users} onEditUser={handleEditUser} />
      )}

      <AddUserModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onCreate={handleCreateUser}
        isSubmitting={isSubmitting}
      />

      <EditUserModal
        isOpen={isEditModalOpen}
        user={editingUser}
        onClose={() => {
          setIsEditModalOpen(false);
          setEditingUser(null);
        }}
        onSave={handleUpdateUser}
        isSaving={isEditSubmitting}
      />

      <InviteLinkModal
        isOpen={isInviteModalOpen}
        inviteLink={inviteLink}
        onClose={() => setIsInviteModalOpen(false)}
      />

      <ErrorModal
        isOpen={isErrorModalOpen}
        title="User Operation Failed"
        message={errorMessage}
        onClose={() => setIsErrorModalOpen(false)}
      />
    </div>
  );
}
