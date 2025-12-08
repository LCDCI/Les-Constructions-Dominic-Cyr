// src/features/files/components/PhotoCard.jsx
import React from 'react'; 
import PropTypes from 'prop-types';
import { FaTrashAlt, FaExpand } from 'react-icons/fa'; 
import '../../../styles/PhotosPage.css';

const BASE_API_URL = 'http://localhost:8082';
const ASSIGNED_USER_ROLE = 'CONTRACTOR';

export default function PhotoCard({ file, onDelete }) { // onPreview removed
    
    // All useState, imageError, and retry logic removed
    
    if (!file || !file.id || !file.fileName) {
        return null; 
    }

    const photoUrl = `${BASE_API_URL}/files/${file.id}`;
    const uploadedDate = new Date(file.createdAt).toLocaleDateString();

    const isEditable = (ASSIGNED_USER_ROLE !== 'CUSTOMER');

    // handleError function removed

    return (
        <div className="photo-card">
            {/* Reverted back to standard <a> tag for preview in new tab */}
            <a href={photoUrl} target="_blank" rel="noopener noreferrer" className="photo-link"> 
                <img 
                    src={photoUrl} 
                    alt={file.fileName} 
                    loading="lazy" 
                    className="photo-image"
                    // onError removed
                />
                
                <div className="photo-overlay">
                    <p className="photo-filename">{file.fileName}</p>
                    <p className="photo-meta">Uploaded by {file.uploadedBy} on {uploadedDate}</p>
                </div>
            </a>
            
            {isEditable && (
                <button 
                    className="btn-delete" 
                    onClick={() => onDelete(file.id)}
                    aria-label={`Delete photo ${file.fileName}`}
                >
                    <FaTrashAlt />
                </button>
            )}
            
            {/* The separate preview button remains a standard link to the image */}
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
    }).isRequired,
    onDelete: PropTypes.func.isRequired,
    // onPreview removed
};