// src/features/files/components/PhotoCard.jsx
import React from 'react';
import PropTypes from 'prop-types';
import { FaTrashAlt, FaExpand } from 'react-icons/fa'; // Added FaExpand for preview
import '../../../styles/PhotosPage.css';

const BASE_API_URL = 'http://localhost:8082';

// Simulate assigning a role (In production, this comes from a context/auth provider)
// Assumes the user is a CUSTOMER to demonstrate the "No editing allowed" criteria.
// Change this constant manually (e.g., to 'CONTRACTOR') to enable the delete button.
const ASSIGNED_USER_ROLE = 'CUSTOMER'; 

export default function PhotoCard({ file, onDelete }) {
    const photoUrl = `${BASE_API_URL}/files/${file.id}`;
    const uploadedDate = new Date(file.createdAt).toLocaleDateString();

   
    const isEditable = (
        ASSIGNED_USER_ROLE !== 'CUSTOMER' 
    );

    return (
        <div className="photo-card">
            <a href={photoUrl} target="_blank" rel="noopener noreferrer" className="photo-link">
                <img 
                    src={photoUrl} 
                    alt={file.fileName} 
                    loading="lazy" 
                    className="photo-image"
                />
                {/* Overlay for quick info/actions */}
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
        id: PropTypes.string.isRequired,
        fileName: PropTypes.string.isRequired,
        contentType: PropTypes.string.isRequired,
        category: PropTypes.string.isRequired,
        uploadedBy: PropTypes.string.isRequired,
        createdAt: PropTypes.string.isRequired,
    }).isRequired,
    onDelete: PropTypes.func.isRequired,
};