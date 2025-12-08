import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import PropTypes from 'prop-types';
import { uploadFile } from '../features/files/api/filesApi';
import '../styles/PhotosPage.css';
import { useNavigate } from 'react-router-dom'; 

const SUPPORTED_IMAGE_TYPES = [
    'image/png',
    'image/jpeg',
    'image/webp',
];
const MAX_FILE_SIZE_MB = 10;
const PHOTO_CATEGORY = 'PHOTO'; 

export default function PhotoUploadModal({ projectId, uploadedBy, onClose, onUploadSuccess }) {
    const [file, setFile] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const navigate = useNavigate(); 

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            if (selectedFile.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
                setErrorMessage(`File size exceeds the maximum limit of ${MAX_FILE_SIZE_MB}MB.`);
                setFile(null);
                return;
            }
            setFile(selectedFile);
            setErrorMessage('');
        }
    };

    const isFileTypeValid = (selectedFile) => {
        return SUPPORTED_IMAGE_TYPES.includes(selectedFile.type);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        if (!file) {
            setErrorMessage('Please select a photo to upload.');
            return;
        }

        if (!isFileTypeValid(file)) {
            setErrorMessage(`File type not supported. Allowed: PNG, JPG/JPEG, WEBP.`);
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('category', PHOTO_CATEGORY);
        formData.append('projectId', projectId);
        formData.append('uploadedBy', uploadedBy);

        setIsLoading(true);
        try {
            const response = await uploadFile(formData);
            
            setSuccessMessage(`Photo uploaded successfully!`);
            onUploadSuccess(response);
            setTimeout(onClose, 1500);
        } catch (error) {
            setIsLoading(false);
            
          
            if (error.response) {
                
                const status = error.response.status;
                const msg = error.response.data?.error || `Upload failed (Status: ${status}).`;
                
                if (status >= 500) {
                    
                    alert(`Critical server error (${status}). Redirecting to homepage.`);
                    navigate('/'); 
                } else {
                    setErrorMessage(msg);
                }
            } else {
        
                alert('Network connection error. Redirecting to homepage.');
                navigate('/'); 
            }
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h2>Upload Project Photo</h2>
                    <button className="btn-cancel" onClick={onClose} aria-label="Close modal"><FaTimes /></button>
                </div>
                
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="file-upload">Select Photo (PNG, JPG, WEBP) - Max {MAX_FILE_SIZE_MB}MB</label>
                        <input 
                            id="file-upload"
                            type="file" 
                            onChange={handleFileChange}
                            accept="image/png, image/jpeg, image/webp"
                            disabled={isLoading}
                        />
                    </div>

                    {errorMessage && <p className="error-message">{errorMessage}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}

                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={onClose} disabled={isLoading}>
                            Cancel
                        </button>
                        <button type="submit" className="btn-submit" disabled={!file || isLoading}>
                            {isLoading ? 'Uploading...' : 'Upload Photo'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

PhotoUploadModal.propTypes = {
    projectId: PropTypes.string.isRequired,
    uploadedBy: PropTypes.string.isRequired,
    onClose: PropTypes.func.isRequired,
    onUploadSuccess: PropTypes.func.isRequired,
};