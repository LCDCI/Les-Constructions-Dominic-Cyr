// src/features/files/components/DocumentCard.jsx
import React from 'react'; 
import PropTypes from 'prop-types';
import { FaTrashAlt, FaDownload } from 'react-icons/fa'; 
import '../../../styles/PhotosPage.css';

const BASE_API_URL = 'http://localhost:8082';
const ASSIGNED_USER_ROLE = 'CONTRACTOR';

const getFileIcon = (contentType) => {
    // Unicode Document Icon (U+1F4C4) with color coding
    if (contentType.includes('pdf')) return <span className="file-icon" style={{ fontSize: '48px', color: '#E53935' }}>📄</span>; 
    if (contentType.includes('wordprocessingml')) return <span className="file-icon" style={{ fontSize: '48px', color: '#2965F1' }}>📄</span>; 
    if (contentType.includes('spreadsheetml')) return <span className="file-icon" style={{ fontSize: '48px', color: '#4CAF50' }}>📄</span>; 
    if (contentType.includes('text/plain')) return <span className="file-icon" style={{ fontSize: '48px', color: '#607D8B' }}>📄</span>; 
    return <span className="file-icon" style={{ fontSize: '48px', color: '#9E9E9E' }}>📄</span>;
};

export default function DocumentCard({ file, onDelete }) {
    
    if (!file || !file.id || !file.fileName) {
        return null; 
    }

    const documentUrl = `${BASE_API_URL}/files/${file.id}`;
    const uploadedDate = new Date(file.createdAt).toLocaleDateString();

    const isEditable = (ASSIGNED_USER_ROLE !== 'CUSTOMER');

    return (
        <div className="photo-card">
            <a href={documentUrl} target="_blank" rel="noopener noreferrer" className="photo-link"> 
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '200px', backgroundColor: '#f5f5f5' }}>
                    {getFileIcon(file.contentType)}
                </div>
                
                <div className="photo-overlay">
                    <p className="photo-filename">{file.fileName}</p>
                    <p className="photo-meta">Uploaded by {file.uploadedBy} on {uploadedDate}</p>
                </div>
            </a>
            
            {isEditable && (
                <button 
                    className="btn-delete" 
                    onClick={() => onDelete(file.id)}
                    aria-label={`Delete document ${file.fileName}`}
                >
                    <FaTrashAlt />
                </button>
            )}
            
            <a href={documentUrl} target="_blank" rel="noopener noreferrer">
                <button 
                    className="btn-preview" 
                    aria-label={`Download document ${file.fileName}`}
                >
                    <FaDownload />
                </button>
            </a>
        </div>
    );
}

DocumentCard.propTypes = {
    file: PropTypes.shape({
        id: PropTypes.string,
        fileName: PropTypes.string,
        contentType: PropTypes.string.isRequired,
        category: PropTypes.string.isRequired,
        uploadedBy: PropTypes.string.isRequired,
        createdAt: PropTypes.string.isRequired,
    }).isRequired,
    onDelete: PropTypes.func.isRequired,
};
