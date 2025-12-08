import React from 'react';
import PropTypes from 'prop-types';
import { FaTrashAlt } from 'react-icons/fa';
import '../../../styles/PhotosPage.css';

const BASE_API_URL = 'http://localhost:8082';

export default function PhotoCard({ file, onDelete }) {
    const photoUrl = `${BASE_API_URL}/files/${file.id}`;
    const uploadedDate = new Date(file.createdAt).toLocaleDateString();

    return (
        <div className="photo-card">
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
            <button 
                className="btn-delete" 
                onClick={() => onDelete(file.id)}
                aria-label={`Delete photo ${file.fileName}`}
            >
                <FaTrashAlt />
            </button>
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