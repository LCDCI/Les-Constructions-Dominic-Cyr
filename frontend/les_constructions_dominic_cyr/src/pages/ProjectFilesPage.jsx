import React, { useState, useEffect } from 'react';
import { FaFolderOpen, FaFileArrowUp } from 'react-icons/fa6';
import FileUploadModal from '../components/FileUploadModal';
import FileCard from '../features/files/components/FileCard';
import { fetchProjectFiles, deleteFile } from '../features/files/api/filesApi';
import '../styles/FilesPage.css';

export default function ProjectFilesPage() {
    const [documents, setDocuments] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const projectId = 'BILL-223067';
    const uploadedBy = '123-user-id';

    useEffect(() => {
        loadDocuments();
    }, []);

    const loadDocuments = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const files = await fetchProjectFiles(projectId);
            setDocuments(files);
        } catch (err) {
            console.error('Error loading documents:', err);
            setError('Failed to load documents. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleUploadSuccess = (newFileMetadata) => {
        const newFile = {
            id: newFileMetadata.fileId,
            fileName: newFileMetadata.fileName,
            contentType: newFileMetadata.contentType,
            category: newFileMetadata.category, 
            uploadedBy: newFileMetadata.uploadedBy,
            createdAt: new Date().toISOString(),
        };
        setDocuments(prevFiles => [newFile, ...prevFiles]);
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm("Are you sure you want to delete this file?")) return;

        try {
            await deleteFile(fileId);
            setDocuments(prevFiles => prevFiles.filter(file => file.id !== fileId));
            alert('File deleted successfully.');
        } catch (error) {
            console.error('Error deleting file:', error);
            alert('Failed to delete file. Please try again.');
        }
    };

    return (
        <div className="documents-page container">
            <div className="documents-header">
                <h1>Project Documents: {projectId}</h1>
                <button className="btn-upload" onClick={() => setIsModalOpen(true)}>
                    <FaFileArrowUp /> Upload Document
                </button>
            </div>

            {isLoading && (
                <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
                    Loading documents...
                </p>
            )}

            {error && (
                <p style={{ marginTop: '20px', textAlign: 'center', color: '#E53935' }}>
                    {error}
                </p>
            )}

            {!isLoading && !error && (
                <>
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
                                        <FileCard key={file.id} file={file} onDelete={handleDelete} />
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
                </>
            )}

            {isModalOpen && (
                <FileUploadModal
                    projectId={projectId}
                    uploadedBy={uploadedBy}
                    onClose={() => setIsModalOpen(false)}
                    onUploadSuccess={handleUploadSuccess}
                />
            )}
        </div>
    );
}