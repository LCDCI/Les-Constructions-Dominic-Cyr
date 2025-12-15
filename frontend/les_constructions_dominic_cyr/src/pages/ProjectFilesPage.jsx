import React, { useState, useEffect } from 'react';
import { FaFileArrowUp } from 'react-icons/fa6';
import FileUploadModal from '../components/FileUploadModal';
import ConfirmationModal from '../components/ConfirmationModal';
import FileCard from '../features/files/components/FileCard';
import { deleteFile, fetchProjectFiles } from '../features/files/api/filesApi';
import '../styles/FilesPage.css';
import { useParams, useNavigate } from 'react-router-dom';

export default function ProjectFilesPage() {
    const { projectId } = useParams();
    const [allFiles, setAllFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [fileToDelete, setFileToDelete] = useState(null);
    const [deleteError, setDeleteError] = useState(null);

    // TODO: Replace PLACEHOLDER_USER_ID with actual current user ID from authentication context before production
    const PLACEHOLDER_USER_ID = '123-user-id';
    const navigate = useNavigate();

    // Data Fetching (match the photos page pattern)
    useEffect(() => {
        const loadFiles = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const files = await fetchProjectFiles(projectId);
                setAllFiles(files || []);
            } catch (err) {
                console.error('Failed to fetch project files:', err);

                const status = err.response?.status || 'Network Error';
                const msg =
                    err.response?.data?.error || `Failed to load documents. Status: ${status}.`;

                setError(msg);

                if (status === 500 || status === 404 || status === 'Network Error') {
                    alert(`Error (${status}): ${msg} Redirecting to Home.`);
                    navigate('/');
                }
            } finally {
                setIsLoading(false);
            }
        };
        loadFiles();
    }, [projectId, navigate]);

    // Only show DOCUMENT files; ignore bad entries
    const documents = (allFiles || []).filter(
        (file) => file && (file.category || 'DOCUMENT') === 'DOCUMENT'
    );

    const handleUploadSuccess = (newFileMetadata) => {
        const newFile = {
            id: newFileMetadata.fileId,
            fileName: newFileMetadata.fileName,
            contentType: newFileMetadata.contentType || 'application/pdf',
            category: newFileMetadata.category || 'DOCUMENT',
            uploadedBy: newFileMetadata.uploadedBy,
            createdAt: new Date().toISOString(),
        };
        setAllFiles((prev) => [newFile, ...(prev || [])]);
    };

    const handleDelete = async (fileId) => {
        setFileToDelete(fileId);
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!fileToDelete) return;

        setDeleteError(null);
        try {
            await deleteFile(fileToDelete, { deletedBy: PLACEHOLDER_USER_ID });
            setAllFiles((prev) => (prev || []).filter((file) => file?.id !== fileToDelete));
            setIsDeleteModalOpen(false);
            setFileToDelete(null);
        } catch (error) {
            console.error('Failed to delete file:', error);
            const errorMsg = error.response?.data?.error || 'Failed to delete file. Please try again.';
            setDeleteError(errorMsg);
            // Keep modal open to show error
        }
    };

    const cancelDelete = () => {
        setIsDeleteModalOpen(false);
        setFileToDelete(null);
    };

    if (isLoading) {
        return (
            <div className="documents-page container" style={{ textAlign: 'center', padding: '50px' }}>
                Loading project documents...
            </div>
        );
    }

    if (error) {
        return (
            <div className="documents-page container">
                <p className="error-message" style={{ color: '#E53935', textAlign: 'center', marginTop: '20px' }}>
                    Error loading documents: {error}
                </p>
            </div>
        );
    }

    return (
        <div className="documents-page container">
            <div className="documents-header">
                <h1>Project Documents: {projectId}</h1>
                <button className="btn-upload" onClick={() => setIsModalOpen(true)}>
                    <FaFileArrowUp /> Upload Document
                </button>
            </div>

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
                        {documents.map((file) => (
                            <FileCard key={file.id || file.fileName} file={file} onDelete={handleDelete} />
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {documents.length === 0 && (
                <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
                    No documents found for this project. Upload your first file to get started!
                </p>
            )}

            {isModalOpen && (
                <FileUploadModal
                    projectId={projectId}
                    uploadedBy={PLACEHOLDER_USER_ID}
                    onClose={() => setIsModalOpen(false)}
                    onUploadSuccess={handleUploadSuccess}
                />
            )}

            <ConfirmationModal
                isOpen={isDeleteModalOpen}
                onCancel={cancelDelete}
                config={{
                    title: "Delete Document",
                    message: deleteError 
                        ? `Error: ${deleteError}` 
                        : "Are you sure you want to delete this document? This action will archive the file and it will no longer be accessible to users. The file may be recoverable by administrators.",
                    onConfirm: confirmDelete,
                    confirmText: deleteError ? "Try Again" : "Delete",
                    cancelText: "Cancel",
                    isDestructive: !deleteError,
                }}
            />
        </div>
    );
}