import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchUsers, createUser, updateUser, updateUserAsOwner, getCurrentUser} from '../features/users/api/usersApi';
import UsersTable from '../features/users/components/UsersTable';
import AddUserModal from '../features/users/components/AddUserModal';
import EditUserModal from '../features/users/components/EditUserModal';
import OwnerEditUserModal from '../features/users/components/OwnerEditUserModal.jsx';
import InviteLinkModal from '../features/users/components/InviteLinkModal';
import ErrorModal from '../features/users/components/ErrorModal';
import '../styles/users.css';

export default function UsersPage() {
  const { getAccessTokenSilently } = useAuth0();
  const [users, setUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadingError, setLoadingError] = useState(null);

  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [inviteLink, setInviteLink] = useState('');
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

  const [errorMessage, setErrorMessage] = useState('');
  const [isErrorModalOpen, setIsErrorModalOpen] = useState(false);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isOwnerEditModalOpen, setIsOwnerEditModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [isEditSubmitting, setIsEditSubmitting] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setLoadingError(null);
        
        const token = await getAccessTokenSilently();
        
        const [usersData, currentUserData] = await Promise.all([
          fetchUsers(token),
          getCurrentUser(token)
        ]);
        
        setUsers(usersData);
        setCurrentUser(currentUserData);
      } catch (err) {
        console.error(err);
        setLoadingError('Failed to load users.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [getAccessTokenSilently]);

  const openErrorModal = (message) => {
    setErrorMessage(message);
    setIsErrorModalOpen(true);
  };

  const handleCreateUser = async (formValues) => {
    try {
      setIsSubmitting(true);
      const token = await getAccessTokenSilently();
      const createdUser = await createUser(formValues, token);

      setUsers((prev) => [...prev, createdUser]);

      if (createdUser.inviteLink) {
        setInviteLink(createdUser.inviteLink);
        setIsInviteModalOpen(true);
      }

      setIsAddModalOpen(false);
    } catch (err) {
      console.error(err);
      let niceMessage = 'Failed to create user. Please try again.';
      if (err.response?.data?.message) {
        niceMessage = err.response.data.message;
      }
      openErrorModal(niceMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditUser = (user) => {
    setEditingUser(user);
    if (currentUser?.userRole === 'OWNER') {
      setIsOwnerEditModalOpen(true);
    } else {
      setIsEditModalOpen(true);
    }
  };

  const handleUpdateUser = async (formValues) => {
    try {
      setIsEditSubmitting(true);
      const token = await getAccessTokenSilently();

      let updatedUser;
      
      if (currentUser?.userRole === 'OWNER') {
        updatedUser = await updateUserAsOwner(editingUser.userIdentifier, formValues, token);
      } else {
        updatedUser = await updateUser(editingUser.userIdentifier, formValues, token);
      }

      setUsers((prev) =>
        prev.map((u) =>
          u.userIdentifier === updatedUser.userIdentifier ? updatedUser : u
        )
      );

      setIsEditModalOpen(false);
      setIsOwnerEditModalOpen(false);
      setEditingUser(null);
    } catch (err) {
      console.error(err);

      let niceMessage = 'Failed to update user.  Please try again.';
      if (err.response?. data?.message) {
        niceMessage = err.response.data.message;
      }

      openErrorModal(niceMessage);
    } finally {
      setIsEditSubmitting(false);
    }
  };

  const handleCloseEditModals = () => {
    setIsEditModalOpen(false);
    setIsOwnerEditModalOpen(false);
    setEditingUser(null);
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
        onClose={handleCloseEditModals}
        onSave={handleUpdateUser}
        isSaving={isEditSubmitting}
      />

      <OwnerEditUserModal
        isOpen={isOwnerEditModalOpen}
        user={editingUser}
        onClose={handleCloseEditModals}
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