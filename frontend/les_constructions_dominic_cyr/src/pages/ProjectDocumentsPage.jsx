// src/pages/ProjectDocumentsPage.jsx
import React, { useState, useEffect } from 'react';
import { FaFileAlt, FaCloudUploadAlt } from 'react-icons/fa';
import DocumentUploadModal from '../components/DocumentUploadModal';
import { deleteFile, fetchProjectDocuments } from '../features/files/api/filesApi'; 
import DocumentCard from '../features/files/components/DocumentCard'; 
import '../styles/PhotosPage.css';
import { useParams, useNavigate } from 'react-router-dom'; 

export default function ProjectDocumentsPage() {
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
                const documents = await fetchProjectDocuments(projectId); 
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

    const documents = allFiles; 

    const handleUploadSuccess = (newFileMetadata) => {
        const newFile = {
            id: newFileMetadata.fileId,
            fileName: newFileMetadata.fileName,
            contentType: newFileMetadata.contentType || 'application/pdf', 
            category: newFileMetadata.category, 
            uploadedBy: newFileMetadata.uploadedBy,
            createdAt: new Date().toISOString(),
        };
        setAllFiles(prevFiles => [newFile, ...prevFiles]);
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm("Are you sure you want to delete this document?")) return;

        try {
            await deleteFile(fileId);
            setAllFiles(prevFiles => prevFiles.filter(file => file.id !== fileId));
        } catch (error) {
            alert('Failed to delete document. Please try again.');
        }
    };

    if (isLoading) {
        return <div className="photos-page container" style={{textAlign: 'center', padding: '50px'}}>Loading project documents...</div>;
    }
    
    if (error) {
        return (
            <div className="photos-page container">
                <p className="error-message" style={{color: '#E53935', textAlign: 'center', marginTop: '20px'}}>
                    Error loading documents: {error}
                </p>
            </div>
        );
    }

    return (
        <div className="photos-page container">
            <div className="photos-header">
                <h1><FaFileAlt style={{ marginRight: '10px' }} /> Project Documents: {projectId}</h1>
                <button className="btn-upload" onClick={() => setIsModalOpen(true)}>
                    <FaCloudUploadAlt /> Upload Document
                </button>
            </div>

            <div className="photo-grid-container">
                {documents.length > 0 ? (
                    <div className="photo-grid">
                        {documents.map((file) => (
                            <DocumentCard 
                                key={file.id} 
                                file={file} 
                                onDelete={handleDelete} 
                            /> 
                        ))}
                    </div>
                ) : (
                    <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
                        No documents uploaded yet. Upload your first document to get started!
                    </p>
                )}
            </div>

            {isModalOpen && (
                <DocumentUploadModal
                    projectId={projectId}
                    uploadedBy={uploadedBy}
                    onClose={() => setIsModalOpen(false)}
                    onUploadSuccess={handleUploadSuccess}
                />
            )}
        </div>
    );
}
