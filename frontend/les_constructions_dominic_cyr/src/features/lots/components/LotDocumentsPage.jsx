import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../../../hooks/useBackendUser';
import {
  fetchLotDocuments,
  uploadLotDocuments,
  downloadLotDocument,
  deleteLotDocument,
} from '../api/lotDocumentsApi';
import { getFormsByLot, downloadFinalizedForm } from '../../forms/api/formsApi';
import { fetchLotById } from '../api/lots';
import {
  FaDownload,
  FaTrash,
  FaUpload,
  FaSearch,
  FaImage,
  FaFile,
  FaArrowLeft,
} from 'react-icons/fa';
import './LotDocumentsPage.css';

const LotDocumentsPage = () => {
  const { lotId } = useParams();
  const navigate = useNavigate();
  const { getAccessTokenSilently } = useAuth0();
  const { role: userRole, profile: userProfile } = useBackendUser();

  const getApiToken = () =>
    getAccessTokenSilently({
      authorizationParams: { audience: import.meta.env.VITE_AUTH0_AUDIENCE },
    });

  const [lot, setLot] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [filteredDocuments, setFilteredDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  const [imageDataUrls, setImageDataUrls] = useState({});
  const [finalizedForms, setFinalizedForms] = useState([]);
  const [formsLoading, setFormsLoading] = useState(false);
  const [formsError, setFormsError] = useState(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all'); // 'all', 'image', 'file'
  const [deleteConfirmModal, setDeleteConfirmModal] = useState(null); // { documentId, fileName }

  const fileInputRef = useRef(null);
  const searchTimeoutRef = useRef(null);

  const canUpload = userRole === 'OWNER' || userRole === 'CONTRACTOR';
  const canViewForms = userRole === 'OWNER' || userRole === 'CUSTOMER';
  const backLinkTarget =
    userRole === 'OWNER'
      ? '/owner/documents'
      : userRole === 'CONTRACTOR'
        ? '/contractors/documents'
        : lot?.projectIdentifier
          ? `/projects/${lot.projectIdentifier}/lots`
          : '/projects';

  useEffect(() => {
    loadLotAndDocuments();
  }, [lotId]);

  useEffect(() => {
    if (lotId && canViewForms) {
      loadFinalizedForms();
    }
  }, [lotId, canViewForms, loadFinalizedForms]);

  useEffect(() => {
    // Load images with authentication
    const loadImages = async () => {
      const token = await getApiToken();
      const urls = {};

      for (const doc of filteredDocuments) {
        if (doc.isImage && !imageDataUrls[doc.id]) {
          try {
            const response = await fetch(doc.downloadUrl, {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            });
            if (response.ok) {
              const blob = await response.blob();
              urls[doc.id] = URL.createObjectURL(blob);
            }
          } catch (err) {
            console.error(`Failed to load image ${doc.id}:`, err);
          }
        }
      }

      if (Object.keys(urls).length > 0) {
        setImageDataUrls(prev => ({ ...prev, ...urls }));
      }
    };

    if (filteredDocuments.length > 0) {
      loadImages();
    }
  }, [filteredDocuments]);

  useEffect(() => {
    // Debounce search
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      applyFilters();
    }, 300);

    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, [searchQuery, filterType, documents]);

  const loadLotAndDocuments = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = await getApiToken();

      // Fetch lot details
      const lotData = await fetchLotById({ lotId, token });
      setLot(lotData);

      // Fetch documents
      await loadDocuments(token);
    } catch (err) {
      console.error('Failed to load lot or documents:', err);
      setError(err.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const loadDocuments = async (token = null) => {
    try {
      const authToken = token || (await getApiToken());
      const docs = await fetchLotDocuments(
        lotId,
        { type: filterType },
        authToken
      );
      setDocuments(docs);
    } catch (err) {
      console.error('Failed to load documents:', err);
      throw err;
    }
  };

  const loadFinalizedForms = async (token = null) => {
    try {
      setFormsLoading(true);
      setFormsError(null);
      const authToken = token || (await getApiToken());
      const forms = await getFormsByLot(
        lotId,
        { status: 'COMPLETED' },
        authToken
      );
      setFinalizedForms(forms || []);
    } catch (err) {
      setFormsError(err?.message || 'Failed to load finalized forms');
      setFinalizedForms([]);
    } finally {
      setFormsLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...documents];

    // Filter by type
    if (filterType === 'image') {
      filtered = filtered.filter(doc => doc.isImage);
    } else if (filterType === 'file') {
      filtered = filtered.filter(doc => !doc.isImage);
    }

    // Filter by search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(doc =>
        doc.fileName?.toLowerCase().includes(query)
      );
    }

    setFilteredDocuments(filtered);
  };

  const handleFileSelect = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async event => {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    setUploadError(null);

    const allowedDocumentTypes = [
      'application/pdf',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'text/plain',
      'application/json',
      'application/octet-stream',
    ];
    const allowedImageTypes = [
      'image/png',
      'image/jpeg',
      'image/webp',
      'video/mp4',
    ];
    const allowedTypes = new Set([
      ...allowedDocumentTypes,
      ...allowedImageTypes,
    ]);
    const allowedTypesLabel =
      'PDF, DOCX, XLSX, TXT, JSON, PNG, JPG, JPEG, WEBP, MP4';

    const filesArray = files instanceof FileList ? Array.from(files) : [files];
    const emptyFile = filesArray.find(file => !file || file.size === 0);
    if (emptyFile) {
      setUploadError(
        'The selected file is empty. Please choose a file with content.'
      );
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    const invalidFile = filesArray.find(
      file => file && !allowedTypes.has(file.type)
    );
    if (invalidFile) {
      setUploadError(
        `This file type is invalid. Supported types: ${allowedTypesLabel}.`
      );
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    try {
      setUploading(true);
      const token = await getApiToken();

      await uploadLotDocuments(lotId, files, token);

      // Reload documents
      await loadDocuments(token);

      // Clear file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err) {
      setUploadError(
        `Failed to upload files. ${err?.message ? `(${err.message})` : ''}`.trim()
      );
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (documentId, fileName) => {
    try {
      const token = await getApiToken();
      await downloadLotDocument(lotId, documentId, fileName, token);
    } catch (err) {
      alert('Failed to download file: ' + (err.message || 'Unknown error'));
    }
  };

  const handleFormDownload = async form => {
    try {
      const token = await getApiToken();
      await downloadFinalizedForm(form.formId, token);
    } catch (err) {
      alert('Failed to download form: ' + (err.message || 'Unknown error'));
    }
  };

  const handleDeleteClick = (documentId, fileName) => {
    setDeleteConfirmModal({ documentId, fileName });
  };

  const handleDeleteConfirm = async () => {
    if (!deleteConfirmModal) return;

    try {
      const token = await getApiToken();
      await deleteLotDocument(lotId, deleteConfirmModal.documentId, token);

      // Reload documents
      await loadDocuments(token);

      setDeleteConfirmModal(null);
    } catch (err) {
      alert('Failed to delete file: ' + (err.message || 'Unknown error'));
      setDeleteConfirmModal(null);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteConfirmModal(null);
  };

  const canDeleteDocument = document => {
    if (userRole === 'OWNER') return true;
    if (userProfile && document.uploaderUserId === userProfile.userIdentifier)
      return true;
    return false;
  };

  if (loading) {
    return (
      <div className="lot-documents-page" data-testid="lot-documents-page">
        <div className="loading-state">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="lot-documents-page" data-testid="lot-documents-page">
        <div className="error-state" data-testid="error-state">
          <p>{error}</p>
          <button
            onClick={() => navigate(backLinkTarget)}
            className="btn btn-secondary"
          >
            Back to Lot Documents
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="lot-documents-page" data-testid="lot-documents-page">
      {/* Header */}
      <div className="page-header">
        <Link to={backLinkTarget} className="back-link">
          <FaArrowLeft /> Back to Lot Documents
        </Link>
        <div className="page-header-title">
          <h1>Lot {lot?.lotNumber || lotId} Documents</h1>
          <div className="page-header-subtitle">
            <span className="lot-address">
              {lot?.civicAddress || 'Address not available'}
            </span>
            {lot?.lotStatus && (
              <span
                className={`lot-status lot-status-${String(lot.lotStatus).toLowerCase()}`}
              >
                {lot.lotStatus}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Search & Filters */}
      <div className="documents-toolbar">
        <div className="search-bar">
          <FaSearch className="search-icon" />
          <input
            type="text"
            placeholder="Search documents..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            data-testid="lot-documents-search"
            className="search-input"
          />
        </div>

        <div className="toolbar-actions">
          <div className="filter-tabs">
            <button
              className={`tab-button ${filterType === 'all' ? 'active' : ''}`}
              onClick={() => setFilterType('all')}
              data-testid="lot-documents-tab-all"
            >
              All
            </button>
            <button
              className={`tab-button ${filterType === 'image' ? 'active' : ''}`}
              onClick={() => setFilterType('image')}
              data-testid="lot-documents-tab-photos"
            >
              <FaImage /> Photos
            </button>
            <button
              className={`tab-button ${filterType === 'file' ? 'active' : ''}`}
              onClick={() => setFilterType('file')}
              data-testid="lot-documents-tab-files"
            >
              <FaFile /> Files
            </button>
          </div>

          {canUpload && (
            <button
              onClick={handleFileSelect}
              disabled={uploading}
              className="btn btn-primary upload-button"
              data-testid="lot-documents-upload-button"
            >
              <FaUpload /> {uploading ? 'Uploading...' : 'Upload'}
            </button>
          )}
        </div>
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          multiple
          style={{ display: 'none' }}
          data-testid="lot-documents-upload-input"
        />
      </div>

      {uploadError && (
        <div className="error-state" data-testid="lot-documents-upload-error">
          {uploadError}
        </div>
      )}

      {canViewForms && (
        <div className="lot-forms-section" data-testid="lot-forms-section">
          <div className="lot-forms-header">
            <h2>Finalized Forms</h2>
            <p>Download completed forms for this lot.</p>
          </div>

          {formsLoading ? (
            <div className="lot-forms-loading">Loading finalized forms...</div>
          ) : formsError ? (
            <div className="lot-forms-error">{formsError}</div>
          ) : finalizedForms.length === 0 ? (
            <div className="lot-forms-empty">No finalized forms yet.</div>
          ) : (
            <div className="lot-forms-list">
              {finalizedForms.map(form => (
                <div key={form.formId} className="lot-form-card">
                  <div className="lot-form-info">
                    <h3>
                      {form.formType
                        ? form.formType.replace(/_/g, ' ')
                        : 'Form'}
                    </h3>
                    <div className="lot-form-meta">
                      <span>Form ID: {form.formId}</span>
                      {form.completedDate && (
                        <span>
                          Completed:{' '}
                          {new Date(form.completedDate).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                  <button
                    className="btn btn-secondary lot-form-download"
                    onClick={() => handleFormDownload(form)}
                  >
                    <FaDownload /> Download PDF
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Documents Display */}
      <div className="documents-container">
        {filteredDocuments.length === 0 ? (
          <div className="empty-state" data-testid="empty-state">
            <p>No documents found.</p>
            {canUpload && <p>Upload files to get started.</p>}
          </div>
        ) : (
          <div
            className={
              filterType === 'image' || filterType === 'all'
                ? 'documents-grid'
                : 'documents-list'
            }
            data-testid="lot-documents-list"
          >
            {filteredDocuments.map(doc =>
              doc.isImage ? (
                // Photo card
                <div
                  key={doc.id}
                  className="document-card photo-card"
                  data-testid={`lot-document-card-${doc.id}`}
                >
                  <div className="photo-preview">
                    <img
                      src={imageDataUrls[doc.id] || doc.downloadUrl}
                      alt={doc.fileName}
                    />
                  </div>
                  <div className="document-info">
                    <p className="document-name">{doc.fileName}</p>
                    <p className="document-meta">
                      {doc.uploaderName} •{' '}
                      {new Date(doc.uploadedAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="document-actions">
                    <button
                      onClick={() => handleDownload(doc.id, doc.fileName)}
                      className="btn-icon"
                      title="Download"
                      data-testid={`lot-document-download-${doc.id}`}
                    >
                      <FaDownload />
                    </button>
                    {canDeleteDocument(doc) && (
                      <button
                        onClick={() => handleDeleteClick(doc.id, doc.fileName)}
                        className="btn-icon btn-danger"
                        title="Delete"
                        data-testid={`lot-document-delete-${doc.id}`}
                      >
                        <FaTrash />
                      </button>
                    )}
                  </div>
                </div>
              ) : (
                // File row
                <div
                  key={doc.id}
                  className="document-row"
                  data-testid={`lot-document-row-${doc.id}`}
                >
                  <div className="document-icon">
                    <FaFile />
                  </div>
                  <div className="document-details">
                    <p className="document-name">{doc.fileName}</p>
                    <p className="document-meta">
                      {doc.uploaderName} •{' '}
                      {new Date(doc.uploadedAt).toLocaleDateString()} •{' '}
                      {(doc.sizeBytes / 1024).toFixed(1)} KB
                    </p>
                  </div>
                  <div className="document-actions">
                    <button
                      onClick={() => handleDownload(doc.id, doc.fileName)}
                      className="btn-icon"
                      title="Download"
                      data-testid={`lot-document-download-${doc.id}`}
                    >
                      <FaDownload />
                    </button>
                    {canDeleteDocument(doc) && (
                      <button
                        onClick={() => handleDeleteClick(doc.id, doc.fileName)}
                        className="btn-icon btn-danger"
                        title="Delete"
                        data-testid={`lot-document-delete-${doc.id}`}
                      >
                        <FaTrash />
                      </button>
                    )}
                  </div>
                </div>
              )
            )}
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirmModal && (
        <div className="modal-overlay" data-testid="confirm-delete-modal">
          <div className="modal-content">
            <h3>Confirm Deletion</h3>
            <p>
              Are you sure you want to delete "{deleteConfirmModal.fileName}"?
            </p>
            <p className="modal-warning">This action cannot be undone.</p>
            <div className="modal-actions">
              <button
                onClick={handleDeleteCancel}
                className="btn btn-secondary"
                data-testid="confirm-delete-no"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                className="btn btn-danger"
                data-testid="confirm-delete-yes"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LotDocumentsPage;
