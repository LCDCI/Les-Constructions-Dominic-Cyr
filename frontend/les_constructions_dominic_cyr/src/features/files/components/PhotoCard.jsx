// src/features/files/components/PhotoCard.jsx
import React from 'react'; 
import PropTypes from 'prop-types';
import { FaTrashAlt, FaExpand, FaArchive, FaBoxOpen } from 'react-icons/fa'; 
import '../../../styles/PhotosPage.css';

const BASE_API_URL = import.meta.env.VITE_FILES_SERVICE_URL || 
    (typeof window !== 'undefined' && (window.location.hostname.includes('lcdci-portal') || window.location.hostname.includes('lcdci-frontend'))
      ? 'https://files-service-app-xubs2.ondigitalocean.app' 
      : `${window.location.origin}/files`);

export default function PhotoCard({ file, onDelete, canDelete = false, onArchive, onUnarchive, canArchive = false, canUnarchive = false }) {
    
    if (!file || !file.id || !file.fileName) {
        return null; 
    }

    const photoUrl = `${BASE_API_URL}/files/${file.id}`;
    const uploadedDate = new Date(file.createdAt).toLocaleDateString();

    return (
        <div className={`photo-card ${file.isArchived ? 'archived' : ''}`}>
            <a href={photoUrl} target="_blank" rel="noopener noreferrer" className="photo-link"> 
                <img 
                    src={photoUrl} 
                    alt={file.fileName} 
                    loading="lazy" 
                    className="photo-image"
                />
                
                <div className="photo-overlay">
                    <p className="photo-filename">{file.fileName}</p>
                    <p className="photo-meta">Uploaded by {file.uploadedBy} on {uploadedDate}</p>
                </div>
            </a>
            

            {/* Overlay unarchive button on image for archived items */}
            {file.isArchived && canUnarchive && (
                <button
                    className="btn-unarchive"
                    onClick={() => onUnarchive(file.id)}
                    aria-label={`Unarchive photo ${file.fileName}`}
                >
                    <FaBoxOpen />
                </button>
            )}

            <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                {canDelete && (
                    <button 
                        className="btn-delete" 
                        onClick={() => onDelete(file.id)}
                        aria-label={`Delete photo ${file.fileName}`}
                    >
                        <FaTrashAlt />
                    </button>
                )}
                {canArchive && !file.isArchived && (
                    <button
                        className="btn-archive"
                        onClick={() => onArchive(file.id)}
                        aria-label={`Archive photo ${file.fileName}`}
                    >
                        <FaArchive />
                    </button>
                )}
                {canUnarchive && file.isArchived && (
                    <button
                        className="btn-unarchive"
                        onClick={() => onUnarchive(file.id)}
                        aria-label={`Unarchive photo ${file.fileName}`}
                    >
                        <FaBoxOpen />
                    </button>
                )}
            </div>
            
            <a href={photoUrl} target="_blank" rel="noopener noreferrer">
                <button 
                    className="btn-preview" 
                    aria-label={`Preview photo ${file.fileName}`}
                >
                    <FaExpand />
                </button>
            </a>
        </div>
    );
}

PhotoCard.propTypes = {
    file: PropTypes.shape({
        id: PropTypes.string,
        fileName: PropTypes.string,
        contentType: PropTypes.string.isRequired,
        category: PropTypes.string.isRequired,
        uploadedBy: PropTypes.string.isRequired,
        createdAt: PropTypes.string.isRequired,
        isArchived: PropTypes.bool,
    }).isRequired,
    onDelete: PropTypes.func.isRequired,
    canDelete: PropTypes.bool,
    onArchive: PropTypes.func,
    onUnarchive: PropTypes.func,
    canArchive: PropTypes.bool,
    canUnarchive: PropTypes.bool,
};