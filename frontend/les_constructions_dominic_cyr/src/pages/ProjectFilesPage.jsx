import React, { useState, useMemo } from 'react';
import { FaFolderOpen, FaFileArrowUp } from 'react-icons/fa6';
import FileUploadModal from '../components/FileUploadModal';
import FileCard from '../features/files/components/FileCard';
import { deleteFile } from '../features/files/api/filesApi';
import '../styles/FilesPage.css';

const initialDocuments = [
    { id: '1', fileName: 'Blueprints_V4.pdf', contentType: 'application/pdf', category: 'DOCUMENT', uploadedBy: 'Mark Dupres', createdAt: '2025-11-20T10:00:00Z' },
    { id: '2', fileName: 'Site_Photo_Roofing.jpg', contentType: 'image/jpeg', category: 'PHOTO', uploadedBy: 'Contractor A', createdAt: '2025-11-25T14:30:00Z' },
    { id: '3', fileName: 'Contract_Signed.docx', contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', category: 'DOCUMENT', uploadedBy: 'Owner', createdAt: '2025-12-01T09:15:00Z' },
];

export default function ProjectFilesPage() {
    const [allFiles, setAllFiles] = useState(initialDocuments);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const projectId = 'BILL-223067';
    const uploadedBy = '123-user-id';

    // Filter to show only Documents for this ticket
    const documents = useMemo(() => {
        return allFiles.filter(file => file.category === 'DOCUMENT');
    }, [allFiles]);

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