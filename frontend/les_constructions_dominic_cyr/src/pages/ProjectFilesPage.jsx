import React, { useState, useEffect } from 'react';
import { FaFolderOpen, FaFileArrowUp } from 'react-icons/fa6';
import { useParams, useNavigate } from 'react-router-dom';
import FileUploadModal from '../components/FileUploadModal';
import FileCard from '../features/files/components/FileCard';
import { deleteFile, fetchProjectFiles } from '../features/files/api/filesApi';
import '../styles/FilesPage.css';

export default function ProjectFilesPage() {
    const { projectId } = useParams();
    const [allFiles, setAllFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const uploadedBy = '123-user-id';
    const navigate = useNavigate();

    // Data Fetching
    useEffect(() => {
        const loadFiles = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const documents = await fetchProjectFiles(projectId, 'document');
                setAllFiles(documents);
            } catch (err) {
                console.error("Failed to fetch project documents:", err);
                
                const status = err.response?.status || 'Network Error';
                const msg = err.response?.data?.error || `Failed to load documents. Status: ${status}.`;

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

    const handleUploadSuccess = (newFileMetadata) => {
        const newFile = {
            id: newFileMetadata.fileId,
            fileName: newFileMetadata.fileName,
            contentType: newFileMetadata.contentType,
            category: newFileMetadata.category, 
            uploadedBy: newFileMetadata.uploadedBy,
            createdAt: new Date().toISOString(),
        };
        setAllFiles(prevFiles => [newFile, ...prevFiles]);
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm("Are you sure you want to delete this file?")) return;

        try {
            await deleteFile(fileId);
            setAllFiles(prevFiles => prevFiles.filter(file => file.id !== fileId));
        } catch (error) {
            console.error('Error deleting file:', error);
            alert('Failed to delete file. Please try again.');
        }
    };

    if (isLoading) {
        return <div className="documents-page container" style={{textAlign: 'center', padding: '50px'}}>Loading project documents...</div>;
    }
    
    if (error) {
        return (
            <div className="documents-page container">
                <p className="error-message" style={{color: '#E53935', textAlign: 'center', marginTop: '20px'}}>
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
                                <th style={{ width: '35%' }}>Document Name</th>
                                <th style={{ width: '15%' }}>Type</th>
                                <th style={{ width: '20%' }}>Uploaded By</th>
                                <th style={{ width: '15%' }}>Upload Date</th>
                                <th style={{ width: '15%' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {allFiles.map((file) => (
                                <FileCard key={file.id} file={file} onDelete={handleDelete} />
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
            
            {allFiles.length === 0 && (
                <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
                    No documents found for this project. Upload your first file to get started!
                </p>
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