import React, { useState, useEffect } from 'react';
import { FaCamera, FaCloudUploadAlt } from 'react-icons/fa';
import PhotoUploadModal from '../../components/Modals/PhotoUploadModal';
import { deleteFile, fetchProjectFiles } from '../../features/files/api/filesApi'; 
import PhotoCard from '../../features/files/components/PhotoCard'; 
import '../../styles/PhotosPage.css';
import { useParams, useNavigate } from 'react-router-dom';
import useBackendUser from '../../hooks/useBackendUser';
import { canUploadPhotos, canDeletePhotos } from '../../utils/permissions';

export default function ProjectPhotosPage() {
    const { projectId } = useParams();
    const [allFiles, setAllFiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null); 
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    const { profile, role } = useBackendUser();
    const uploadedBy = profile?.userIdentifier || '';
    const navigate = useNavigate();

    // Permission checks based on role
    const canUpload = canUploadPhotos(role);
    const canDelete = canDeletePhotos(role);

    // Data Fetching
    useEffect(() => {
        const loadFiles = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const photos = await fetchProjectFiles(projectId); 
                setAllFiles(photos);
            } catch (err) {
                console.error("Failed to fetch project files:", err);
                
                const status = err.response?.status || 'Network Error';
                const msg = err.response?.data?.error || `Failed to load photos. Status: ${status}.`;

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

    const photos = allFiles; 

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
            await deleteFile(fileId, { deletedBy: uploadedBy });
            setAllFiles(prevFiles => prevFiles.filter(file => file.id !== fileId));
        } catch (error) {
            alert('Failed to delete photo. Please try again.');
        }
    };

    if (isLoading) {
        return <div className="photos-page container" style={{textAlign: 'center', padding: '50px'}}>Loading project photos...</div>;
    }
    
    if (error) {
        return (
            <div className="photos-page container">
                <p className="error-message" style={{color: '#E53935', textAlign: 'center', marginTop: '20px'}}>
                    Error loading photos: {error}
                </p>
            </div>
        );
    }

    return (
        <div className="photos-page container">
            <div className="photos-header">
                <h1><FaCamera style={{ marginRight: '10px' }} /> Project Photos: {projectId}</h1>
                {canUpload && (
                    <button className="btn-upload" onClick={() => setIsModalOpen(true)}>
                        <FaCloudUploadAlt /> Upload Photo
                    </button>
                )}
            </div>

            <div className="photo-grid-container">
                {photos.length > 0 ? (
                    <div className="photo-grid">
                        {photos.map((file) => (
                            <PhotoCard 
                                key={file.id} 
                                file={file} 
                                onDelete={handleDelete}
                                canDelete={canDelete}
                            /> 
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
