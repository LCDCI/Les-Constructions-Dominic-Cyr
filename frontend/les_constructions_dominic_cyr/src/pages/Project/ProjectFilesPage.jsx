import React, { useState, useEffect } from 'react';
import { FaSync, FaArchive } from 'react-icons/fa';
import { FaFileArrowUp } from 'react-icons/fa6';
import FileUploadModal from '../../components/Modals/FileUploadModal';
import ConfirmationModal from '../../components/Modals/ConfirmationModal';
import FileCard from '../../features/files/components/FileCard';
import {
  deleteFile,
  fetchProjectDocuments,
  reconcileProject,
  downloadFile,
  downloadAllFilesAsZip,
} from '../../features/files/api/filesApi';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchUserById } from '../../features/users/api/usersApi';
import '../../styles/FilesPage.css';
import { useParams, useNavigate } from 'react-router-dom';
import useBackendUser from '../../hooks/useBackendUser';
import {
  canUploadDocuments,
  canDeleteDocuments,
} from '../../utils/permissions';
import { useAuth0 } from '@auth0/auth0-react';

export default function ProjectFilesPage() {
  const { projectId } = useParams();
  const [allFiles, setAllFiles] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [fileToDelete, setFileToDelete] = useState(null);
  const [deleteError, setDeleteError] = useState(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isDownloadingZip, setIsDownloadingZip] = useState(false);
  const [zipError, setZipError] = useState(null);
  const [projectName, setProjectName] = useState(null);
  const [userNameMap, setUserNameMap] = useState({});

  const { profile, role, loading: profileLoading } = useBackendUser();
  const { getAccessTokenSilently } = useAuth0();
  const userId = profile?.userIdentifier || '';
  const uploaderName =
    (profile?.fullName && profile.fullName.trim()) ||
    [profile?.firstName, profile?.lastName].filter(Boolean).join(' ').trim() ||
    profile?.name ||
    profile?.email ||
    'Unknown';
  const navigate = useNavigate();

  // Permission checks based on role
  const canUpload = canUploadDocuments(role);
  const canDelete = canDeleteDocuments(role);

  // Fetch project name
  useEffect(() => {
    const loadProjectName = async () => {
      try {
        const token = await getAccessTokenSilently();
        const project = await projectApi.getProjectById(projectId, token);
        setProjectName(project?.projectName || null);
      } catch (err) {
        console.error('Failed to fetch project name:', err);
        // Continue without project name - will use fallback
      }
    };
    if (projectId) {
      loadProjectName();
    }
  }, [projectId, getAccessTokenSilently]);

  // Data Fetching - use fetchProjectDocuments with role-based filtering
  useEffect(() => {
    const loadFiles = async () => {
      // Don't load if we don't have required data
      if (!projectId || !role || !userId) {
        console.warn('Missing required data for file loading:', {
          projectId,
          role,
          userId,
        });
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);
      try {
        // Pass role and userId for filtering
        const files = await fetchProjectDocuments(projectId, role, userId);
        setAllFiles(files || []);

        // Fetch user names for all unique user IDs
        if (files && files.length > 0) {
          const uniqueUserIds = [
            ...new Set(files.map(f => f.uploadedBy).filter(Boolean)),
          ];
          if (uniqueUserIds.length > 0) {
            try {
              const token = await getAccessTokenSilently();
              const userPromises = uniqueUserIds.map(async uid => {
                // Skip if we already have this user in the map (e.g., current user)
                if (uid === userId && profile) {
                  const currentUserName =
                    (profile?.fullName && profile.fullName.trim()) ||
                    [profile?.firstName, profile?.lastName]
                      .filter(Boolean)
                      .join(' ')
                      .trim() ||
                    profile?.name ||
                    profile?.primaryEmail ||
                    profile?.email ||
                    uid;
                  return { [uid]: currentUserName };
                }

                try {
                  const user = await fetchUserById(uid, token);
                  if (user) {
                    const userName =
                      (user?.fullName && user.fullName.trim()) ||
                      [user?.firstName, user?.lastName]
                        .filter(Boolean)
                        .join(' ')
                        .trim() ||
                      user?.name ||
                      user?.primaryEmail ||
                      user?.email ||
                      uid; // Fallback to ID if no name found
                    return { [uid]: userName };
                  } else {
                    console.warn(`User ${uid} not found in database`);
                    return { [uid]: uid }; // Show ID if user not found
                  }
                } catch (err) {
                  console.error(`Failed to fetch user ${uid}:`, err);
                  // If 404, user doesn't exist - show ID. Otherwise show "Unknown"
                  if (err.response?.status === 404) {
                    return { [uid]: uid };
                  }
                  return { [uid]: 'Unknown' };
                }
              });
              const userMaps = await Promise.all(userPromises);
              const nameMap = Object.assign({}, ...userMaps);
              setUserNameMap(nameMap);
            } catch (err) {
              console.error('Failed to fetch user names:', err);
              // Continue without user names - will show IDs
            }
          }
        }
      } catch (err) {
        console.error('Failed to fetch project files:', err);

        const status = err.response?.status || 'Network Error';
        const msg =
          err.response?.data?.error ||
          `Failed to load documents. Status: ${status}.`;

        setError(msg);

        if (status === 500 || status === 404 || status === 'Network Error') {
          alert(`Error (${status}): ${msg} Redirecting to Home.`);
          navigate('/');
        }
      } finally {
        setIsLoading(false);
      }
    };
    if (projectId && role && userId) {
      loadFiles();
    }
  }, [projectId, role, userId, navigate, getAccessTokenSilently]);

  // Update userNameMap when profile loads (in case files loaded before profile)
  useEffect(() => {
    if (profile && userId && allFiles && allFiles.length > 0) {
      // Check if any files were uploaded by the current user
      const currentUserFiles = allFiles.filter(f => f.uploadedBy === userId);
      if (currentUserFiles.length > 0 && !userNameMap[userId]) {
        const currentUserName =
          (profile?.fullName && profile.fullName.trim()) ||
          [profile?.firstName, profile?.lastName]
            .filter(Boolean)
            .join(' ')
            .trim() ||
          profile?.name ||
          profile?.primaryEmail ||
          profile?.email ||
          userId;
        setUserNameMap(prev => ({
          ...prev,
          [userId]: currentUserName,
        }));
      }
    }
  }, [profile, userId, allFiles, userNameMap]);

  // The documents endpoint already returns only DOCUMENT category files
  const documents = allFiles || [];

  const handleUploadSuccess = async newFileMetadata => {
    const newFile = {
      id: newFileMetadata.fileId,
      fileName: newFileMetadata.fileName,
      contentType: newFileMetadata.contentType || 'application/pdf',
      category: newFileMetadata.category || 'DOCUMENT',
      uploadedBy: newFileMetadata.uploadedBy,
      createdAt: new Date().toISOString(),
    };

    // Add file to list immediately
    setAllFiles(prev => [newFile, ...(prev || [])]);

    // Fetch user name for the newly uploaded file
    const uploadedByUserId = newFileMetadata.uploadedBy;
    if (uploadedByUserId) {
      // If it's the current user, use profile data immediately
      if (uploadedByUserId === userId && profile) {
        const currentUserName =
          (profile?.fullName && profile.fullName.trim()) ||
          [profile?.firstName, profile?.lastName]
            .filter(Boolean)
            .join(' ')
            .trim() ||
          profile?.name ||
          profile?.primaryEmail ||
          profile?.email ||
          uploadedByUserId;
        setUserNameMap(prev => ({
          ...prev,
          [uploadedByUserId]: currentUserName,
        }));
      } else {
        // Otherwise, fetch from API
        try {
          const token = await getAccessTokenSilently();
          const user = await fetchUserById(uploadedByUserId, token);
          if (user) {
            const userName =
              (user?.fullName && user.fullName.trim()) ||
              [user?.firstName, user?.lastName]
                .filter(Boolean)
                .join(' ')
                .trim() ||
              user?.name ||
              user?.primaryEmail ||
              user?.email ||
              uploadedByUserId;
            setUserNameMap(prev => ({
              ...prev,
              [uploadedByUserId]: userName,
            }));
          }
        } catch (err) {
          console.error(
            `Failed to fetch user name for ${uploadedByUserId}:`,
            err
          );
          // If 404, user doesn't exist - use ID. Otherwise use "Unknown"
          if (err.response?.status === 404) {
            setUserNameMap(prev => ({
              ...prev,
              [uploadedByUserId]: uploadedByUserId,
            }));
          } else {
            setUserNameMap(prev => ({
              ...prev,
              [uploadedByUserId]: 'Unknown',
            }));
          }
        }
      }
    }
  };

  const handleDelete = async fileId => {
    setFileToDelete(fileId);
    setIsDeleteModalOpen(true);
  };

  const confirmDelete = async () => {
    if (!fileToDelete) return;

    setDeleteError(null);
    try {
      await deleteFile(fileToDelete, { deletedBy: userId });
      setAllFiles(prev =>
        (prev || []).filter(file => file?.id !== fileToDelete)
      );
      setIsDeleteModalOpen(false);
      setFileToDelete(null);
    } catch (error) {
      console.error('Failed to delete file:', error);
      const errorMsg =
        error.response?.data?.error ||
        'Failed to delete file. Please try again.';
      setDeleteError(errorMsg);
      // Keep modal open to show error
    }
  };

  const cancelDelete = () => {
    setIsDeleteModalOpen(false);
    setFileToDelete(null);
  };

  const handleRefresh = async () => {
    setIsRefreshing(true);
    try {
      await reconcileProject(projectId);
      const files = await fetchProjectDocuments(projectId, role, userId);
      setAllFiles(files || []);
    } catch (err) {
      console.error('Failed to refresh:', err);
      alert('Failed to refresh files. Please try again.');
    } finally {
      setIsRefreshing(false);
    }
  };

  const handleDownload = async (fileId, fileName) => {
    try {
      await downloadFile(fileId, fileName, role, userId);
    } catch (error) {
      console.error('Failed to download file:', error);
      const errorMsg =
        error.response?.data?.error ||
        'Failed to download file. Please try again.';
      alert(`Download error: ${errorMsg}`);
    }
  };

  const handleDownloadAll = async () => {
    setIsDownloadingZip(true);
    setZipError(null);
    try {
      await downloadAllFilesAsZip(projectId, role, userId, projectName);
    } catch (error) {
      console.error('Failed to download ZIP:', error);
      const errorMsg =
        error.response?.data?.error ||
        'Failed to download ZIP file. Please try again.';
      setZipError(errorMsg);
    } finally {
      setIsDownloadingZip(false);
    }
  };

  if (isLoading) {
    return (
      <div
        className="documents-page container"
        style={{ textAlign: 'center', padding: '50px' }}
      >
        Loading project documents...
      </div>
    );
  }

  if (error) {
    return (
      <div className="documents-page container">
        <p
          className="error-message"
          style={{ color: '#E53935', textAlign: 'center', marginTop: '20px' }}
        >
          Error loading documents: {error}
        </p>
      </div>
    );
  }

  return (
    <div className="documents-page container">
      <div className="documents-header">
        <h1>Project Documents: {projectId}</h1>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <button
            className="btn-refresh"
            onClick={handleRefresh}
            disabled={isRefreshing}
            style={{
              background: '#6366F1',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '8px',
              cursor: isRefreshing ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <FaSync className={isRefreshing ? 'spinning' : ''} />{' '}
            {isRefreshing ? 'Syncing...' : 'Sync Files'}
          </button>
          <button
            className="btn-download-all"
            onClick={handleDownloadAll}
            disabled={isDownloadingZip || documents.length === 0}
            style={{
              background: documents.length === 0 ? '#9CA3AF' : '#10B981',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '8px',
              cursor:
                isDownloadingZip || documents.length === 0
                  ? 'not-allowed'
                  : 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <FaArchive />{' '}
            {isDownloadingZip ? 'Downloading...' : 'Download All as ZIP'}
          </button>
          {canUpload && (
            <button
              className="btn-upload"
              onClick={() => {
                if (!userId || !role) {
                  alert(
                    'User information is still loading. Please wait a moment and try again.'
                  );
                  return;
                }
                setIsModalOpen(true);
              }}
              disabled={profileLoading || !userId || !role}
            >
              <FaFileArrowUp /> Upload Document
            </button>
          )}
        </div>
      </div>
      {zipError && (
        <p
          className="error-message"
          style={{ color: '#E53935', textAlign: 'center', marginTop: '10px' }}
        >
          {zipError}
        </p>
      )}

      <div className="document-list-container">
        <div className="document-table">
          <table>
            <thead>
              <tr>
                <th style={{ width: '45%' }}>Document Name</th>
                <th style={{ width: '20%' }}>Type</th>
                <th style={{ width: '20%' }}>Uploaded By</th>
                <th style={{ width: '15%' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {documents.map(file => (
                <FileCard
                  key={file.id || file.fileName}
                  file={file}
                  onDelete={handleDelete}
                  onDownload={handleDownload}
                  canDelete={canDelete}
                  userNameMap={userNameMap}
                />
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {documents.length === 0 && (
        <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
          No documents found for this project. Upload your first file to get
          started!
        </p>
      )}

      {isModalOpen && userId && role && (
        <FileUploadModal
          projectId={projectId}
          uploadedBy={userId}
          uploaderRole={role}
          onClose={() => setIsModalOpen(false)}
          onUploadSuccess={handleUploadSuccess}
        />
      )}

      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        onCancel={cancelDelete}
        config={{
          title: 'Delete Document',
          message: deleteError
            ? `Error: ${deleteError}`
            : 'Are you sure you want to delete this document? This action will archive the file and it will no longer be accessible to users. The file may be recoverable by administrators.',
          onConfirm: confirmDelete,
          confirmText: deleteError ? 'Try Again' : 'Delete',
          cancelText: 'Cancel',
          isDestructive: !deleteError,
        }}
      />
    </div>
  );
}
