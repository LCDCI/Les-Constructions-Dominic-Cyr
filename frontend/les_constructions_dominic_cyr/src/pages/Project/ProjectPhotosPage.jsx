import React, { useState, useEffect } from 'react';
import { FaCamera, FaCloudUploadAlt, FaSync } from 'react-icons/fa';
import { useParams, useNavigate } from 'react-router-dom';

import PhotoUploadModal from '../../components/Modals/PhotoUploadModal';
import ConfirmationModal from '../../components/Modals/ConfirmationModal';
import PhotoCard from '../../features/files/components/PhotoCard';

import {
    deleteFile,
    fetchProjectFiles,
    reconcileProject,
    archiveFile,
    unarchiveFile,
} from '../../features/files/api/filesApi';

import useBackendUser from '../../hooks/useBackendUser';
import { canUploadPhotos, canDeletePhotos } from '../../utils/permissions';

import '../../styles/PhotosPage.css';
import '../../styles/FilesPage.css';

export default function ProjectPhotosPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();

    const [allFiles, setAllFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [actionError, setActionError] = useState(null);

    const [modalState, setModalState] = useState({
        isOpen: false,
        action: null, // delete | archive | unarchive
        fileId: null,
        fileName: '',
    });

    const { profile, role } = useBackendUser();

    const uploadedBy =
        (profile?.fullName && profile.fullName.trim()) ||
        [profile?.firstName, profile?.lastName].filter(Boolean).join(' ').trim() ||
        profile?.name ||
        profile?.email ||
        profile?.userIdentifier ||
        '';

    const canUpload = canUploadPhotos(role);
    const canDelete = canDeletePhotos(role);
    const canArchive = canDelete;
    const canUnarchive = canDelete;

    const [filter, setFilter] = useState('active'); // 'all' | 'active' | 'archived'

    /* =========================
       Fetch photos
    ========================= */
    useEffect(() => {
        const loadFiles = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const photos = await fetchProjectFiles(projectId, { archived: filter === 'archived' });
                // Normalize files to ensure required fields exist for PhotoCard
                const normalized = photos.map(p => ({
                    contentType: p.contentType || 'image/jpeg',
                    category: p.category || 'PHOTO',
                    uploadedBy: p.uploadedBy || uploadedBy || 'Unknown',
                    createdAt: p.createdAt || new Date().toISOString(),
                    isArchived: Boolean(p.isArchived),
                    ...p,
                }));
                setAllFiles(normalized);
            } catch (err) {
                console.error(err);
                const status = err.response?.status || 'Network Error';
                const msg =
                    err.response?.data?.error ||
                    `Failed to load photos. Status: ${status}.`;
                setError(msg);

                if (status === 500 || status === 404 || status === 'Network Error') {
                    alert(`Error (${status}): ${msg}`);
                    navigate('/');
                }
            } finally {
                setIsLoading(false);
            }
        };

        loadFiles();
    }, [projectId, navigate, filter]);

    // Ensure required file fields exist for PhotoCard
    const normalizeFiles = (files) =>
        (files || []).map((p) => ({
            contentType: p.contentType || 'image/jpeg',
            category: p.category || 'PHOTO',
            uploadedBy: p.uploadedBy || uploadedBy || 'Unknown',
            createdAt: p.createdAt || new Date().toISOString(),
            isArchived: Boolean(p.isArchived),
            ...p,
        }));

    /* =========================
       Modal helpers
    ========================= */
    const openModal = (action, file) => {
        setModalState({
            isOpen: true,
            action,
            fileId: file.id,
            fileName: file.fileName,
        });
        setActionError(null);
    };

    const closeModal = () => {
        setModalState({
            isOpen: false,
            action: null,
            fileId: null,
            fileName: '',
        });
    };

    /* =========================
       Actions
    ========================= */
    const refreshFiles = async () => {
        setIsRefreshing(true);
        try {
            const files = await fetchProjectFiles(projectId, { archived: filter === 'archived' });
            setAllFiles(normalizeFiles(files));
        } finally {
            setIsRefreshing(false);
        }
    };

    const doDelete = async () => {
        try {
            const actor = profile?.userIdentifier;
            if (!actor) {
                setActionError('Cannot determine your user identifier. Please reload and try again.');
                return;
            }
            console.debug('[ProjectPhotosPage] Deleting file', { fileId: modalState.fileId, deletedBy: actor });
            await deleteFile(modalState.fileId, { deletedBy: actor });
            closeModal();
            await refreshFiles();
        } catch (err) {
            console.error('[ProjectPhotosPage] delete error', err);
            const msg = err.response?.data?.error || err.message || 'Failed to delete photo.';
            setActionError(msg);
        }
    };

    const doArchive = async () => {
        try {
            const actor = profile?.userIdentifier;
            if (!actor) {
                setActionError('Cannot determine your user identifier. Please reload and try again.');
                return;
            }
            await archiveFile(modalState.fileId, { archivedBy: actor });
            closeModal();
            await refreshFiles();
        } catch (err) {
            const msg = err.response?.data?.error || err.message || 'Failed to archive photo.';
            setActionError(msg);
        }
    };

    const doUnarchive = async () => {
        try {
            await unarchiveFile(modalState.fileId);
            closeModal();
            refreshFiles();
        } catch (err) {
            setActionError('Failed to unarchive photo.');
        }
    };

    /* =========================
       Render
    ========================= */
    return (
        <div className="photos-page">
            <div className="photos-header">
                <h1>
                    <FaCamera /> Project Photos
                </h1>

                <div className="photos-actions">
                    <button
                        className="btn-refresh"
                        onClick={refreshFiles}
                        disabled={isRefreshing}
                        style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 12px', borderRadius: 6, border: 'none', background: '#6366F1', color: '#fff', cursor: isRefreshing ? 'not-allowed' : 'pointer' }}
                    >
                        <FaSync className={isRefreshing ? 'spinning' : ''} />
                        <span style={{ marginLeft: 6 }}>{isRefreshing ? 'Syncing...' : 'Sync Photos'}</span>
                    </button>

                    {canUpload && (
                        <button className="btn-upload" onClick={() => setIsUploadModalOpen(true)}>
                            <FaCloudUploadAlt />
                            <span style={{ marginLeft: 6 }}>Upload Photo</span>
                        </button>
                    )}
                </div>
                <div style={{ marginTop: 8, display: 'flex', gap: 8 }}>
                    <button
                        onClick={() => setFilter('active')}
                        style={{ padding: '6px 10px', borderRadius: 6, border: filter === 'active' ? '1px solid #6366F1' : '1px solid #E5E7EB', background: filter === 'active' ? '#EEF2FF' : '#fff' }}
                    >
                        Active
                    </button>
                    <button
                        onClick={() => setFilter('archived')}
                        style={{ padding: '6px 10px', borderRadius: 6, border: filter === 'archived' ? '1px solid #6366F1' : '1px solid #E5E7EB', background: filter === 'archived' ? '#EEF2FF' : '#fff' }}
                    >
                        Archived
                    </button>
                </div>
            </div>

            {isLoading && <p>Loading photos...</p>}
            {error && <p className="error-message">{error}</p>}

            <div className="photos-grid">
                {normalizeFiles(allFiles)
                    .filter((file) => {
                        if (filter === 'active') return !file.isArchived;
                        if (filter === 'archived') return file.isArchived;
                        return true;
                    })
                    .map((file) => (
                        <PhotoCard
                            key={file.id}
                            file={file}
                            // only allow 'delete' (which archives) on non-archived items
                            canDelete={canDelete && !file.isArchived}
                            // Delete now archives instead of deleting
                            onDelete={() => openModal('archive', file)}
                            onArchive={() => openModal('archive', file)}
                            onUnarchive={() => openModal('unarchive', file)}
                            canArchive={canArchive && !file.isArchived}
                            // ensure archived photos show the restore button in archived view
                            canUnarchive={(canUnarchive || filter === 'archived') && file.isArchived}
                        />
                    ))}
            </div>

            {/* Upload Modal */}
            {isUploadModalOpen && (
                <PhotoUploadModal
                    projectId={projectId}
                    uploadedBy={uploadedBy}
                    onClose={() => setIsUploadModalOpen(false)}
                    onUploadSuccess={refreshFiles}
                />
            )}

            {/* Confirmation Modal */}
            <ConfirmationModal
                isOpen={modalState.isOpen}
                onCancel={closeModal}
                config={{
                    title:
                        modalState.action === 'delete'
                            ? 'Delete Photo'
                            : modalState.action === 'archive'
                            ? 'Archive Photo'
                            : 'Unarchive Photo',
                    message:
                        modalState.action === 'delete'
                            ? `Are you sure you want to delete "${modalState.fileName}"? This cannot be undone.`
                            : modalState.action === 'archive'
                            ? `Archive "${modalState.fileName}"? It will be hidden but not deleted.`
                            : `Restore "${modalState.fileName}" to visible photos?`,
                    onConfirm:
                        modalState.action === 'delete'
                            ? doDelete
                            : modalState.action === 'archive'
                            ? doArchive
                            : doUnarchive,
                    confirmText:
                        modalState.action === 'delete'
                            ? 'Delete'
                            : modalState.action === 'archive'
                            ? 'Archive'
                            : 'Unarchive',
                    cancelText: 'Cancel',
                    isDestructive:
                        modalState.action === 'delete' ||
                        modalState.action === 'archive',
                }}
            />

            {actionError && (
                <div
                    className="error-message"
                    style={{ color: '#E53935', textAlign: 'center', marginTop: 16 }}
                >
                    {actionError}
                </div>
            )}
        </div>
    );
}
