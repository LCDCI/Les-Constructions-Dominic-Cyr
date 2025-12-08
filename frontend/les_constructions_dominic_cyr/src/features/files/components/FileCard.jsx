import React from 'react';
import PropTypes from 'prop-types';
// import { FaFileExport, FaTrashAlt } from 'react-icons/fa';
import '../../../styles/FilesPage.css';

const BASE_API_URL = 'http://localhost:8082';

const getFileIcon = (contentType) => {
    // Unicode Document Icon (U+1F4C4) with color coding
    if (contentType.includes('pdf')) return <span className="file-icon" style={{ color: '#E53935' }}>&#128441;</span>; 
    if (contentType.includes('wordprocessingml')) return <span className="file-icon" style={{ color: '#2965F1' }}>&#128441;</span>; 
    if (contentType.includes('spreadsheetml')) return <span className="file-icon" style={{ color: '#4CAF50' }}>&#128441;</span>; 
    if (contentType.includes('text/plain')) return <span className="file-icon" style={{ color: '#607D8B' }}>&#128441;</span>; 
    return <span className="file-icon" style={{ color: '#9E9E9E' }}>&#128441;</span>;
};

const formatCategory = (contentType) => {
    if (contentType.includes('pdf')) return 'PDF';
    if (contentType.includes('wordprocessingml')) return 'DOCX';
    if (contentType.includes('spreadsheetml')) return 'XLSX';
    if (contentType.includes('text/plain')) return 'TXT';
    return 'Document';
};

const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-CA', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    });
}

export default function FileCard({ file, onDelete }) {
    return (
        <tr>
            <td className="file-name-cell">
                {getFileIcon(file.contentType)} {file.fileName}
            </td>
            <td>{formatCategory(file.contentType)}</td>
            <td>{file.uploadedBy}</td>
            <td>{formatDate(file.createdAt)}</td>
            <td className="document-action-buttons">
                <a 
                    href={`${BASE_API_URL}/files/${file.id}`} 
                    target="_blank" 
                    rel="noopener noreferrer"
                >
                    <button className="btn-view">
                        View
                    </button>
                </a>
                <button 
                    className="btn-delete" 
                    onClick={() => onDelete(file.id)}
                >
                    Delete
                </button>
            </td>
        </tr>
    );
}

FileCard.propTypes = {
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