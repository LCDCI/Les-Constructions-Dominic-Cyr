import React from 'react';
import PropTypes from 'prop-types';
import '../../../styles/FilesPage.css';

const BASE_API_URL = import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

const getFileIcon = (contentType = '') => {
    if (contentType.includes('pdf')) return <span className="file-icon" style={{ color: '#E53935' }}>&#128441;</span>;
    if (contentType.includes('wordprocessingml')) return <span className="file-icon" style={{ color: '#2965F1' }}>&#128441;</span>;
    if (contentType.includes('spreadsheetml')) return <span className="file-icon" style={{ color: '#4CAF50' }}>&#128441;</span>;
    if (contentType.includes('text/plain')) return <span className="file-icon" style={{ color: '#607D8B' }}>&#128441;</span>;
    return <span className="file-icon" style={{ color: '#9E9E9E' }}>&#128441;</span>;
};

const formatCategory = (contentType = '') => {
    if (contentType.includes('pdf')) return 'PDF';
    if (contentType.includes('wordprocessingml')) return 'DOCX';
    if (contentType.includes('spreadsheetml')) return 'XLSX';
    if (contentType.includes('text/plain')) return 'TXT';
    return 'Document';
};

export default function FileCard({ file, onDelete, canDelete = false }) {
    if (!file) return null;
    const safeContentType = file.contentType || '';

    return (
        <tr>
            <td className="file-name-cell">
                {getFileIcon(safeContentType)} {file.fileName || 'Untitled'}
            </td>
            <td>{formatCategory(safeContentType)}</td>
            <td>{file.uploadedBy || 'Unknown'}</td>
            <td className="document-action-buttons">
                <a href={`${BASE_API_URL}/files/${file.id}`} target="_blank" rel="noopener noreferrer">
                    <button className="btn-view">View</button>
                </a>
                {canDelete && (
                    <button className="btn-delete" onClick={() => onDelete(file.id)}>
                        Delete
                    </button>
                )}
            </td>
        </tr>
    );
}

FileCard.propTypes = {
    file: PropTypes.shape({
        id: PropTypes.string,
        fileName: PropTypes.string,
        contentType: PropTypes.string,
        category: PropTypes.string,
        uploadedBy: PropTypes.string,
        createdAt: PropTypes.string,
    }),
    onDelete: PropTypes.func.isRequired,
    canDelete: PropTypes.bool,
};