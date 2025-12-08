import React, { useState, useEffect, useMemo } from 'react';
import { FaCamera, FaCloudUploadAlt } from 'react-icons/fa';
import PhotoUploadModal from '../components/PhotoUploadModal';
import { deleteFile, fetchProjectFiles } from '../features/files/api/filesApi'; 
import PhotoCard from '../features/files/components/PhotoCard'; 
import '../styles/PhotosPage.css'; 
import { useParams } from 'react-router-dom';

const initialFiles = [];

export default function ProjectPhotosPage() {
    const { projectId } = useParams();
    const [allFiles, setAllFiles] = useState(initialFiles);
    const [isLoading, setIsLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const uploadedBy = '123-user-id'; 

    useEffect(() => {
        const loadFiles = async () => {
            setIsLoading(true);
            try {
           
                const files = await fetchProjectFiles(projectId); 
                setAllFiles(files);
            } catch (error) {
                console.error("Failed to fetch project files:", error);
            } finally {
                setIsLoading(false);
            }
        };
        loadFiles();
    }, [projectId]); 

    const photos = useMemo(() => {
       
        return allFiles.filter(file => file.category === 'PHOTO');
    }, [allFiles]);

    const handleUploadSuccess = (newFileMetadata) => {
        const newFile = {
            id: newFileMetadata.fileId,
            fileName: newFileMetadata.fileName,
            contentType: newFileMetadata.contentType || 'image/jpeg', 
            category: newFileMetadata.category, 
            uploadedBy: newFileMetadata.uploadedBy,
            createdAt: new Date().toISOString(),
        };
    
        setAllFiles(prevFiles => [newFile, ...prevFiles]);
    };

    const handleDelete = async (fileId) => {
        if (!window.confirm("Are you sure you want to delete this photo?")) return;

        try {
            await deleteFile(fileId);
            setAllFiles(prevFiles => prevFiles.filter(file => file.id !== fileId));
            alert('Photo deleted successfully.');
        } catch (error) {
            console.error('Error deleting file:', error);
            alert('Failed to delete photo. Please try again.');
        }
    };

    if (isLoading) {
        return <div className="photos-page container" style={{textAlign: 'center', padding: '50px'}}>Loading project photos...</div>;
    }

    return (
        <div className="photos-page container">
            <div className="photos-header">
                <h1><FaCamera style={{ marginRight: '10px' }} /> Project Photos: {projectId}</h1>
                <button className="btn-upload" onClick={() => setIsModalOpen(true)}>
                    <FaCloudUploadAlt /> Upload Photo
                </button>
            </div>

            <div className="photo-grid-container">
                {photos.length > 0 ? (
                    <div className="photo-grid">
                        {photos.map((file) => (
                            <PhotoCard key={file.id} file={file} onDelete={handleDelete} />
                        ))}
                    </div>
                ) : (
                    <p style={{ marginTop: '20px', textAlign: 'center', color: '#6B7280' }}>
                        No photos uploaded yet. Visually document your progress!
                    </p>
                )}
            </div>

            {isModalOpen && (
                <PhotoUploadModal
                    projectId={projectId}
                    uploadedBy={uploadedBy}
                    onClose={() => setIsModalOpen(false)}
                    onUploadSuccess={handleUploadSuccess}
                />
            )}
        </div>
    );
}