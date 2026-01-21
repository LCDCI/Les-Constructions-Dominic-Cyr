import React, { useState, useEffect } from 'react';
// eslint-disable-next-line no-unused-vars
import axios from 'axios';
import { FaTimes } from 'react-icons/fa';
import PropTypes from 'prop-types';
import { uploadFile } from '../../features/files/api/filesApi';
import '../../styles/FilesPage.css';

const SUPPORTED_DOC_TYPES = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'text/plain',
  'application/octet-stream',
];
const MAX_FILE_SIZE_MB = 10;

export default function FileUploadModal({
  projectId,
  uploadedBy,
  uploaderRole,
  onClose,
  onUploadSuccess,
}) {
  const [file, setFile] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // Hardcode category to DOCUMENT
  const category = 'DOCUMENT';

  // Debug: Log props when modal opens
  useEffect(() => {
    console.log('FileUploadModal props:', {
      projectId,
      uploadedBy,
      uploaderRole,
      hasUploadedBy: !!uploadedBy,
      hasUploaderRole: !!uploaderRole,
    });
  }, [projectId, uploadedBy, uploaderRole]);

  const handleFileChange = e => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
        setErrorMessage(
          `File size exceeds the maximum limit of ${MAX_FILE_SIZE_MB}MB.`
        );
        setFile(null);
        return;
      }
      setFile(selectedFile);
      setErrorMessage('');
    }
  };

  const isFileTypeValid = selectedFile => {
    return SUPPORTED_DOC_TYPES.includes(selectedFile.type);
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!file) {
      setErrorMessage('Please select a document to upload.');
      return;
    }

    if (!isFileTypeValid(file)) {
      setErrorMessage(
        `File type not supported. Allowed: PDF, DOCX, XLSX, TXT.`
      );
      return;
    }

    if (!uploadedBy || uploadedBy.trim() === '') {
      setErrorMessage(
        'User identifier is missing. Please refresh the page and try again.'
      );
      setIsLoading(false);
      console.error('Upload failed: uploadedBy is missing', {
        uploadedBy,
        uploaderRole,
      });
      return;
    }

    if (!uploaderRole || uploaderRole.trim() === '') {
      setErrorMessage(
        'User role is missing. Please refresh the page and try again.'
      );
      setIsLoading(false);
      console.error('Upload failed: uploaderRole is missing', {
        uploadedBy,
        uploaderRole,
      });
      return;
    }

    // Validate role is one of the expected values
    const validRoles = ['OWNER', 'CONTRACTOR', 'SALESPERSON', 'CUSTOMER'];
    const normalizedRole = uploaderRole.trim().toUpperCase();
    if (!validRoles.includes(normalizedRole)) {
      setErrorMessage(
        `Invalid user role: ${uploaderRole}. Please contact support.`
      );
      setIsLoading(false);
      console.error('Upload failed: invalid role', {
        uploadedBy,
        uploaderRole,
        normalizedRole,
      });
      return;
    }

    const trimmedUploadedBy = uploadedBy.trim();
    const trimmedUploaderRole = uploaderRole.trim().toUpperCase();

    // Debug logging
    console.log('Uploading file with:', {
      projectId,
      uploadedBy: trimmedUploadedBy,
      uploaderRole: trimmedUploaderRole,
      fileName: file.name,
      fileSize: file.size,
    });

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);
    formData.append('projectId', projectId);
    formData.append('uploadedBy', trimmedUploadedBy);
    formData.append('uploaderRole', trimmedUploaderRole);

    setIsLoading(true);
    try {
      const response = await uploadFile(formData);

      setSuccessMessage(`Document uploaded successfully!`);
      onUploadSuccess(response);
      setTimeout(onClose, 1500);
    } catch (error) {
      const msg =
        error.response?.data?.error ||
        'Failed to upload document. Please check the file type and size.';
      setErrorMessage(msg);
      setIsLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <h2>Upload Document</h2>
          <button
            className="btn-cancel"
            onClick={onClose}
            aria-label="Close modal"
          >
            <FaTimes />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="file-upload">
              Select Document (PDF, DOCX, XLSX, TXT) - Max {MAX_FILE_SIZE_MB}MB
            </label>
            <input
              id="file-upload"
              type="file"
              onChange={handleFileChange}
              accept=".pdf, .docx, .xlsx, .txt"
              disabled={isLoading}
            />
          </div>

          {errorMessage && <p className="error-message">{errorMessage}</p>}
          {successMessage && (
            <p className="success-message">{successMessage}</p>
          )}

          <div className="modal-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={!file || isLoading}
            >
              {isLoading ? 'Uploading...' : 'Upload Document'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

FileUploadModal.propTypes = {
  projectId: PropTypes.string.isRequired,
  uploadedBy: PropTypes.string.isRequired,
  uploaderRole: PropTypes.string,
  onClose: PropTypes.func.isRequired,
  onUploadSuccess: PropTypes.func.isRequired,
};
