/* eslint-disable no-console */
import React, { useState, useEffect, useRef, useCallback } from 'react';
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
  getAllForms,
  getFormById,
  updateFormData,
  submitForm,
} from '../../forms/api/formsApi';
import { uploadFile, downloadFile } from '../../files/api/filesApi';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import {
  FaDownload,
  FaTrash,
  FaUpload,
  FaSearch,
  FaImage,
  FaFile,
  FaArrowLeft,
} from 'react-icons/fa';
import { GoFileDiff } from 'react-icons/go';
import './LotDocumentsPage.css';
import '../../../styles/Forms/customer-forms.css';

/**
 * Form field configuration for each form type.
 */
const FORM_FIELDS = {
  EXTERIOR_DOORS: [
    {
      name: '__instructions',
      type: 'instructions',
      translationKey: 'formFields.exteriorDoors.instructions',
    },
    {
      name: '__link',
      type: 'external-link',
      url: 'https://design.novatechgroup.com/fr',
      translationKey: 'formFields.exteriorDoors.linkLabel',
    },
    {
      name: 'pdfFile',
      type: 'file-upload',
      translationKey: 'formFields.exteriorDoors.uploadPdf',
      required: true,
      accept: '.pdf',
    },
    {
      name: 'additionalNotes',
      type: 'textarea',
      translationKey: 'formFields.additionalNotes',
      required: false,
    },
  ],
  GARAGE_DOORS: [
    {
      name: '__instructions',
      type: 'instructions',
      translationKey: 'formFields.garageDoors.instructions',
    },
    {
      name: '__link',
      type: 'external-link',
      url: 'https://www.portesuniverselles.com/fr-ca/centredesign/selection',
      translationKey: 'formFields.garageDoors.linkLabel',
    },
    {
      name: 'pdfFile',
      type: 'file-upload',
      translationKey: 'formFields.garageDoors.uploadPdf',
      required: true,
      accept: '.pdf',
    },
    {
      name: 'additionalNotes',
      type: 'textarea',
      translationKey: 'formFields.additionalNotes',
      required: false,
    },
  ],
  WINDOWS: [
    {
      name: 'exteriorColorFacade',
      type: 'text',
      translationKey: 'formFields.windows.exteriorColorFacade',
      required: true,
    },
    {
      name: 'exteriorColorSidesBack',
      type: 'text',
      translationKey: 'formFields.windows.exteriorColorSidesBack',
      required: true,
    },
    {
      name: 'interiorColorFacade',
      type: 'text',
      translationKey: 'formFields.windows.interiorColorFacade',
      required: true,
    },
    {
      name: 'interiorColorSidesBack',
      type: 'text',
      translationKey: 'formFields.windows.interiorColorSidesBack',
      required: true,
    },
    {
      name: 'otherDetails',
      type: 'textarea',
      translationKey: 'formFields.windows.otherDetails',
      required: false,
    },
  ],
  ASPHALT_SHINGLES: [
    {
      name: 'company',
      type: 'text',
      translationKey: 'formFields.shingles.company',
      required: true,
    },
    {
      name: 'collection',
      type: 'text',
      translationKey: 'formFields.shingles.collection',
      required: true,
    },
    {
      name: 'color',
      type: 'text',
      translationKey: 'formFields.shingles.color',
      required: true,
    },
    {
      name: 'steelRoofColor',
      type: 'text',
      translationKey: 'formFields.shingles.steelRoofColor',
      required: false,
    },
  ],
  WOODWORK: [
    {
      name: '__doorSection',
      type: 'section-header',
      translationKey: 'formFields.woodwork.doorModelSection',
      referenceUrl: 'https://www.intermat.ca/portes-de-masonites',
      referenceTranslationKey: 'formFields.woodwork.viewOnWebsite',
    },
    {
      name: 'interiorDoorModel',
      type: 'image-radio',
      translationKey: 'formFields.woodwork.interiorDoorModel',
      required: true,
      options: [
        { value: 'unie', labelKey: 'formFields.woodwork.doors.unie' },
        { value: 'carrara', labelKey: 'formFields.woodwork.doors.carrara' },
        { value: 'coloniale', labelKey: 'formFields.woodwork.doors.coloniale' },
        { value: 'rockport', labelKey: 'formFields.woodwork.doors.rockport' },
        { value: 'logan', labelKey: 'formFields.woodwork.doors.logan' },
        {
          value: 'lincoln_park',
          labelKey: 'formFields.woodwork.doors.lincolnPark',
        },
        { value: 'conmore', labelKey: 'formFields.woodwork.doors.conmore' },
      ],
    },
    {
      name: '__handleSection',
      type: 'section-header',
      translationKey: 'formFields.woodwork.handleSection',
      referenceUrl: 'https://www.intermat.ca/leviers',
      referenceTranslationKey: 'formFields.woodwork.viewOnWebsite',
    },
    {
      name: 'interiorHandleModel',
      type: 'image-radio',
      translationKey: 'formFields.woodwork.interiorHandleModel',
      required: true,
      options: [
        { value: 'boston', labelKey: 'formFields.woodwork.handles.boston' },
        { value: 'brava', labelKey: 'formFields.woodwork.handles.brava' },
        { value: 'destin', labelKey: 'formFields.woodwork.handles.destin' },
        { value: 'zen', labelKey: 'formFields.woodwork.handles.zen' },
        { value: 'verona', labelKey: 'formFields.woodwork.handles.verona' },
        {
          value: 'linea_rosette_carre',
          labelKey: 'formFields.woodwork.handles.lineaRosetteCarre',
        },
        {
          value: 'linea_rosette_ronde',
          labelKey: 'formFields.woodwork.handles.lineaRosetteRonde',
        },
        { value: 'sanford', labelKey: 'formFields.woodwork.handles.sanford' },
      ],
    },
    {
      name: 'handleFinish',
      type: 'select',
      translationKey: 'formFields.woodwork.handleFinish',
      required: true,
      options: [
        {
          value: 'chrome',
          labelKey: 'formFields.woodwork.finishes.chrome',
          label: 'Chrome',
        },
        {
          value: 'satin_nickel',
          labelKey: 'formFields.woodwork.finishes.satinNickel',
          label: 'Satin Nickel',
        },
        {
          value: 'matte_black',
          labelKey: 'formFields.woodwork.finishes.matteBlack',
          label: 'Matte Black',
        },
        {
          value: 'antique_bronze',
          labelKey: 'formFields.woodwork.finishes.antiqueBronze',
          label: 'Antique Bronze',
        },
        {
          value: 'glossy_black',
          labelKey: 'formFields.woodwork.finishes.glossyBlack',
          label: 'Glossy Black',
        },
      ],
    },
    {
      name: '__baseboardSection',
      type: 'section-header',
      translationKey: 'formFields.woodwork.baseboardSection',
      referenceUrl: 'https://www.intermat.ca/plinthes',
      referenceTranslationKey: 'formFields.woodwork.viewOnWebsite',
    },
    {
      name: 'baseboardModel',
      type: 'image-radio',
      translationKey: 'formFields.woodwork.baseboardModel',
      required: true,
      options: [
        {
          value: 'urbaine',
          labelKey: 'formFields.woodwork.baseboards.urbaine',
        },
        { value: '1000', labelKey: 'formFields.woodwork.baseboards.m1000' },
        { value: 'oblik', labelKey: 'formFields.woodwork.baseboards.oblik' },
        { value: '1500', labelKey: 'formFields.woodwork.baseboards.m1500' },
        { value: '1249', labelKey: 'formFields.woodwork.baseboards.m1249' },
      ],
    },
    {
      name: 'baseboardHeight',
      type: 'text',
      translationKey: 'formFields.woodwork.baseboardHeight',
      required: true,
    },
    {
      name: '__quarterRoundSection',
      type: 'section-header',
      translationKey: 'formFields.woodwork.quarterRoundSection',
      referenceUrl: 'https://www.intermat.ca/quart-de-rond',
      referenceTranslationKey: 'formFields.woodwork.viewOnWebsite',
    },
    {
      name: 'quarterRoundModel',
      type: 'image-radio',
      translationKey: 'formFields.woodwork.quarterRoundModel',
      required: true,
      options: [
        {
          value: '1000',
          labelKey: 'formFields.woodwork.quarterRounds.m1000',
        },
        {
          value: '1234',
          labelKey: 'formFields.woodwork.quarterRounds.m1234',
        },
        {
          value: '1500',
          labelKey: 'formFields.woodwork.quarterRounds.m1500',
        },
        {
          value: '2000',
          labelKey: 'formFields.woodwork.quarterRounds.m2000',
        },
      ],
    },
    {
      name: 'additionalNotes',
      type: 'textarea',
      translationKey: 'formFields.additionalNotes',
      required: false,
    },
  ],
  PAINT: [
    {
      name: 'paintBrand',
      type: 'text',
      translationKey: 'formFields.paint.brand',
      required: true,
    },
    {
      name: 'paintFinish',
      type: 'text',
      translationKey: 'formFields.paint.finish',
      required: true,
    },
    {
      name: 'interiorColors',
      type: 'text',
      translationKey: 'formFields.paint.interiorColors',
      required: true,
    },
    {
      name: 'exteriorColors',
      type: 'text',
      translationKey: 'formFields.paint.exteriorColors',
      required: false,
    },
    {
      name: 'roomsToPaint',
      type: 'text',
      translationKey: 'formFields.paint.roomsToPaint',
      required: true,
    },
    {
      name: 'additionalNotes',
      type: 'textarea',
      translationKey: 'formFields.additionalNotes',
      required: false,
    },
  ],
};

const LotDocumentsPage = () => {
  const { t } = usePageTranslations('customerForms');
  const { lotId } = useParams();
  const navigate = useNavigate();
  const { getAccessTokenSilently, user } = useAuth0();
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
  const [finalizedFormsLoading, setFinalizedFormsLoading] = useState(false);
  const [finalizedFormsError, setFinalizedFormsError] = useState(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all'); // 'all', 'image', 'file'
  const [deleteConfirmModal, setDeleteConfirmModal] = useState(null); // { documentId, fileName }
  const [viewMode, setViewMode] = useState('documents'); // 'documents' or 'forms'
  const [lotForms, setLotForms] = useState([]);
  const [lotFormsLoading, setLotFormsLoading] = useState(false);

  // Form modal state
  const [selectedForm, setSelectedForm] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [formData, setFormData] = useState({});
  const [submitError, setSubmitError] = useState(null);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [isSubmitConfirmOpen, setIsSubmitConfirmOpen] = useState(false);
  const formFileInputRef = useRef(null);

  const fileInputRef = useRef(null);
  const searchTimeoutRef = useRef(null);

  const canUpload = userRole === 'OWNER' || userRole === 'CONTRACTOR';
  const canViewForms =
    userRole === 'OWNER' ||
    userRole === 'CUSTOMER' ||
    userRole === 'SALESPERSON';
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

  const loadFinalizedForms = useCallback(async () => {
    if (!lotId || !canViewForms) {
      return;
    }
    try {
      setFinalizedFormsLoading(true);
      setFinalizedFormsError(null);
      const token = await getApiToken();
      const forms = await getFormsByLot(
        lotId,
        { status: 'COMPLETED' },
        token
      );
      setFinalizedForms(forms || []);
    } catch (err) {
      console.error('Failed to load finalized forms:', err);
      setFinalizedFormsError('Impossible de charger les formulaires finalisés.');
    } finally {
      setFinalizedFormsLoading(false);
    }
  }, [lotId, canViewForms, getApiToken]);

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

  const loadFormsForLot = async () => {
    try {
      setLotFormsLoading(true);
      const token = await getApiToken();
      // Use getAllForms to get all forms accessible by current role (includes forms for lots user has access to)
      const allForms = await getAllForms(token);
      const filtered = (allForms || []).filter(
        form => form.lotIdentifier === lotId
      );
      setLotForms(filtered);
    } catch (err) {
      console.error('Failed to load forms:', err);
    } finally {
      setLotFormsLoading(false);
    }
  };

  useEffect(() => {
    if (viewMode === 'forms') {
      loadFormsForLot();
    }
  }, [viewMode]);

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

  /* ── Form Modal Handlers ───────────────────────────────────────── */
  const openFormModal = form => {
    setSelectedForm(form);
    setFormData(form.formData || {});
    setIsEditModalOpen(true);
    setSubmitError(null);
    setUploadError(null);
  };

  const handleFieldChange = (fieldName, value) => {
    setFormData(prev => ({
      ...prev,
      [fieldName]: value,
    }));
  };

  const handleFileUpload = async (fieldName, file) => {
    if (!file) return;
    try {
      setUploadingFile(true);
      setUploadError(null);

      const fd = new FormData();
      fd.append('file', file);
      fd.append('category', 'DOCUMENT');
      fd.append('projectId', lot?.projectIdentifier || 'unknown');
      fd.append('uploadedBy', user?.sub || '');
      fd.append('uploaderRole', userRole || 'CUSTOMER');

      const result = await uploadFile(fd);

      handleFieldChange(fieldName, {
        fileId: result.id,
        fileName: result.fileName || file.name,
      });
      setUploadingFile(false);
    } catch (err) {
      setUploadingFile(false);
      setUploadError(
        t('errors.uploadFailed', 'Failed to upload file. Please try again.')
      );
    }
  };

  const handleRemoveFile = fieldName => {
    handleFieldChange(fieldName, null);
    if (formFileInputRef.current) formFileInputRef.current.value = '';
  };

  const handleDownloadFile = async (fileId, fileName) => {
    console.log('[LotDocumentsPage] Download attempt:', {
      fileId,
      fileName,
      userRole,
      userId: user?.sub,
    });
    try {
      await downloadFile(fileId, fileName, userRole, user?.sub);
    } catch (err) {
      console.error('Failed to download file:', err);
      console.error('Error response:', err?.response?.data);
      setUploadError(t('errors.downloadFailed', 'Failed to download file.'));
    }
  };

  const handleSaveForm = async () => {
    try {
      setSubmitError(null);
      const token = await getApiToken();
      await updateFormData(selectedForm.formId, { formData }, token);
      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      loadFormsForLot();
    } catch (error) {
      if (error?.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError(t('errors.saveFailed', 'Failed to save form.'));
      }
    }
  };

  const handleSubmitFormClick = () => {
    const fields = FORM_FIELDS[selectedForm.formType] || [];
    const requiredFields = fields.filter(
      f => f.required && !f.name.startsWith('__')
    );

    const missingFields = requiredFields.filter(f => {
      const val = formData[f.name];
      if (f.type === 'file-upload') return !val || !val.fileId;
      if (val == null) return true;
      return val.toString().trim() === '';
    });

    if (missingFields.length > 0) {
      const names = missingFields
        .map(f => t(f.translationKey, f.name))
        .join(', ');
      setSubmitError(
        t(
          'errors.missingFields',
          'Please fill in all required fields: {{fields}}'
        ).replace('{{fields}}', names)
      );
      return;
    }

    setIsSubmitConfirmOpen(true);
  };

  const handleSubmitForm = async () => {
    try {
      setSubmitError(null);
      setIsSubmitConfirmOpen(false);

      const token = await getApiToken();
      await updateFormData(selectedForm.formId, { formData }, token);
      await submitForm(selectedForm.formId, token);

      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      loadFormsForLot();
    } catch (error) {
      if (error?.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError(t('errors.submitFailed', 'Failed to submit form.'));
      }
    }
  };

  const handleViewForm = async form => {
    try {
      setSubmitError(null);
      setUploadError(null);
      // Fetch the latest form data to ensure we have the most recent customer inputs
      const token = await getApiToken();
      const freshFormData = await getFormById(form.formId, token);
      setSelectedForm(freshFormData);
      setFormData(freshFormData.formData || {});
      setIsEditModalOpen(true);
    } catch (err) {
      console.error('Failed to load form data:', err);
      setSubmitError('Failed to load form data. Please try again.');
    }
  };

  /* ── Field Renderers ───────────────────────────────────────────── */
  const renderInstructions = field => (
    <div key={field.name} className="forms-instructions">
      <p>{t(field.translationKey, '')}</p>
    </div>
  );

  const renderExternalLink = field => (
    <div key={field.name} className="forms-external-link">
      <a href={field.url} target="_blank" rel="noopener noreferrer">
        {t(field.translationKey, 'Open website')} ↗
      </a>
    </div>
  );

  const renderSectionHeader = field => (
    <div key={field.name} className="forms-section-header">
      <h3>{t(field.translationKey, '')}</h3>
      {field.referenceUrl && (
        <a
          href={field.referenceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="forms-section-ref"
        >
          {t(field.referenceTranslationKey, 'View on website')} ↗
        </a>
      )}
    </div>
  );

  const renderFileUpload = field => {
    const fileVal = formData[field.name];
    const isViewOnly = selectedForm && selectedForm.formStatus === 'SUBMITTED';
    return (
      <div key={field.name} className="forms-form-group">
        <label>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>

        {fileVal?.fileId ? (
          <div className="forms-file-uploaded">
            <span
              className="forms-file-name"
              onClick={() =>
                handleDownloadFile(fileVal.fileId, fileVal.fileName)
              }
              style={{ cursor: 'pointer', textDecoration: 'underline' }}
              title={t('forms.downloadFile', 'Click to download')}
            >
              📄 {fileVal.fileName}
            </span>
            {!isViewOnly && (
              <button
                type="button"
                className="forms-file-remove"
                onClick={() => handleRemoveFile(field.name)}
              >
                ×
              </button>
            )}
          </div>
        ) : (
          !isViewOnly && (
            <div className="forms-file-dropzone">
              <input
                ref={formFileInputRef}
                type="file"
                accept={field.accept || '.pdf'}
                onChange={e => handleFileUpload(field.name, e.target.files[0])}
                disabled={uploadingFile}
              />
              {uploadingFile && (
                <p className="forms-file-uploading">
                  {t('modal.uploading', 'Uploading...')}
                </p>
              )}
            </div>
          )
        )}

        {uploadError && <p className="forms-field-error">{uploadError}</p>}
      </div>
    );
  };

  const renderImageRadio = field => {
    const selected = formData[field.name] || '';
    const isViewOnly = selectedForm && selectedForm.formStatus === 'SUBMITTED';
    return (
      <div key={field.name} className="forms-form-group">
        <label>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>
        <div className="forms-radio-grid">
          {(field.options || []).map(opt => (
            <button
              key={opt.value}
              type="button"
              className={`forms-radio-card${selected === opt.value ? ' selected' : ''}`}
              onClick={() =>
                !isViewOnly && handleFieldChange(field.name, opt.value)
              }
              disabled={isViewOnly}
            >
              <span className="forms-radio-indicator" />
              <span className="forms-radio-label">
                {t(opt.labelKey, opt.label || opt.value)}
              </span>
            </button>
          ))}
        </div>
      </div>
    );
  };

  const renderTextInput = field => {
    const isViewOnly = selectedForm && selectedForm.formStatus === 'SUBMITTED';
    return (
      <div key={field.name} className="forms-form-group">
        <label htmlFor={field.name}>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>
        <input
          id={field.name}
          type={field.type}
          value={formData[field.name] || ''}
          onChange={e => handleFieldChange(field.name, e.target.value)}
          className="forms-form-input"
          readOnly={isViewOnly}
          disabled={isViewOnly}
        />
      </div>
    );
  };

  const renderTextarea = field => {
    const isViewOnly = selectedForm && selectedForm.formStatus === 'SUBMITTED';
    return (
      <div key={field.name} className="forms-form-group">
        <label htmlFor={field.name}>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>
        <textarea
          id={field.name}
          value={formData[field.name] || ''}
          onChange={e => handleFieldChange(field.name, e.target.value)}
          className="forms-form-textarea"
          rows="4"
          readOnly={isViewOnly}
          disabled={isViewOnly}
        />
      </div>
    );
  };

  const renderSelect = field => {
    const isViewOnly = selectedForm && selectedForm.formStatus === 'SUBMITTED';
    return (
      <div key={field.name} className="forms-form-group">
        <label htmlFor={field.name}>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>
        <select
          id={field.name}
          value={formData[field.name] || ''}
          onChange={e => handleFieldChange(field.name, e.target.value)}
          className="forms-form-select"
          disabled={isViewOnly}
        >
          <option value="">{t('modal.selectOption', '-- Select --')}</option>
          {(field.options || []).map(opt => (
            <option key={opt.value} value={opt.value}>
              {t(opt.labelKey, opt.label || opt.value)}
            </option>
          ))}
        </select>
      </div>
    );
  };

  const renderField = field => {
    switch (field.type) {
      case 'instructions':
        return renderInstructions(field);
      case 'external-link':
        return renderExternalLink(field);
      case 'section-header':
        return renderSectionHeader(field);
      case 'file-upload':
        return renderFileUpload(field);
      case 'image-radio':
        return renderImageRadio(field);
      case 'select':
        return renderSelect(field);
      case 'textarea':
        return renderTextarea(field);
      case 'text':
      case 'number':
      default:
        return renderTextInput(field);
    }
  };

  const renderFormFields = () => {
    if (!selectedForm) return null;
    const fields = FORM_FIELDS[selectedForm.formType] || [];
    return fields.map(field => renderField(field));
  };

  const isViewOnlyForm = () => {
    return selectedForm && selectedForm.formStatus === 'SUBMITTED';
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

      {/* View Mode Toggle */}
      <div className="view-mode-toggle">
        <button
          className={`view-mode-button ${viewMode === 'documents' ? 'active' : ''}`}
          onClick={() => setViewMode('documents')}
          data-testid="view-mode-documents"
        >
          <FaFile /> Documents
        </button>
        <button
          className={`view-mode-button ${viewMode === 'forms' ? 'active' : ''}`}
          onClick={() => setViewMode('forms')}
          data-testid="view-mode-forms"
        >
          <GoFileDiff /> Forms
        </button>
      </div>

      {viewMode === 'documents' ? (
        <>
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
            <div
              className="error-state"
              data-testid="lot-documents-upload-error"
            >
              {uploadError}
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
                            onClick={() =>
                              handleDeleteClick(doc.id, doc.fileName)
                            }
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
                            onClick={() =>
                              handleDeleteClick(doc.id, doc.fileName)
                            }
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
        </>
      ) : (
        /* Forms View */
        <div className="forms-view-container" data-testid="lot-forms-view">
          {lotFormsLoading ? (
            <div className="loading-state">Loading forms...</div>
          ) : lotForms.length === 0 ? (
            <div className="empty-state" data-testid="no-forms-state">
              <p>No forms assigned for this lot.</p>
            </div>
          ) : (
            <div className="lot-forms-list">
              {lotForms.map(form => (
                <div
                  key={form.formId}
                  className="lot-form-card"
                  data-testid={`lot-form-card-${form.formId}`}
                >
                  <div className="lot-form-info">
                    <h3 className="lot-form-title">
                      {form.formType?.replace(/_/g, ' ') || 'Form'}
                    </h3>
                    <span
                      className={`lot-form-status lot-form-status-${(form.formStatus || '').toLowerCase()}`}
                    >
                      {form.formStatus || 'Unknown'}
                    </span>
                  </div>
                  <div className="lot-form-meta">
                    {form.assignedAt && (
                      <p>
                        Assigned:{' '}
                        {new Date(form.assignedAt).toLocaleDateString()}
                      </p>
                    )}
                    {form.submittedAt && (
                      <p>
                        Submitted:{' '}
                        {new Date(form.submittedAt).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  {/* Show Fill Form button only for customers on non-submitted forms */}
                  {userRole === 'CUSTOMER' &&
                    (form.formStatus === 'ASSIGNED' ||
                      form.formStatus === 'IN_PROGRESS' ||
                      form.formStatus === 'REOPENED') && (
                      <button
                        onClick={() => openFormModal(form)}
                        className="btn btn-primary lot-form-action"
                        data-testid={`lot-form-fill-${form.formId}`}
                      >
                        Fill Form
                      </button>
                    )}
                  {/* Show View Form button only for submitted forms (all users) */}
                  {form.formStatus === 'SUBMITTED' && (
                    <button
                      onClick={() => handleViewForm(form)}
                      className="btn btn-secondary lot-form-action"
                      data-testid={`lot-form-view-${form.formId}`}
                    >
                      View Form
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {canViewForms && (
        <div className="lot-forms-section" data-testid="lot-forms-section">
          <div className="lot-forms-header">
            <h2>Finalized Forms</h2>
            <p>Download completed forms for this lot.</p>
          </div>

          {finalizedFormsLoading ? (
            <div className="lot-forms-loading">Loading finalized forms...</div>
          ) : finalizedFormsError ? (
            <div className="lot-forms-error">{finalizedFormsError}</div>
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
      {/* Form Edit Modal */}
      {isEditModalOpen && selectedForm && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsEditModalOpen(false)}
        >
          <div
            className="forms-modal forms-modal-large"
            onClick={e => e.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>
                {t('modal.editTitle', 'Form')} -{' '}
                {t(
                  `formTypes.${selectedForm.formType}`,
                  selectedForm.formType?.replace(/_/g, ' ') || 'Form'
                )}
              </h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsEditModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              {submitError && (
                <div className="forms-error forms-error-modal">
                  <p>{submitError}</p>
                  <button onClick={() => setSubmitError(null)}>×</button>
                </div>
              )}
              {renderFormFields()}
            </div>
            <div className="forms-modal-footer">
              {isViewOnlyForm() || userRole !== 'CUSTOMER' ? (
                // View-only mode: Only show Close button
                <button
                  className="forms-modal-button forms-modal-button-secondary"
                  onClick={() => setIsEditModalOpen(false)}
                >
                  {t('buttons.close', 'Close')}
                </button>
              ) : (
                // Edit mode: Show all action buttons (only for customers on non-submitted forms)
                <>
                  <button
                    className="forms-modal-button forms-modal-button-secondary"
                    onClick={() => setIsEditModalOpen(false)}
                  >
                    {t('buttons.cancel', 'Cancel')}
                  </button>
                  <button
                    className="forms-modal-button forms-modal-button-primary"
                    onClick={handleSaveForm}
                  >
                    {t('buttons.saveDraft', 'Save Draft')}
                  </button>
                  <button
                    className="forms-modal-button forms-modal-button-success"
                    onClick={handleSubmitFormClick}
                  >
                    {t('buttons.submitForm', 'Submit Form')}
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Submit Confirmation Modal */}
      {isSubmitConfirmOpen && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsSubmitConfirmOpen(false)}
        >
          <div className="forms-modal" onClick={e => e.stopPropagation()}>
            <div className="forms-modal-header">
              <h2>{t('modal.confirmSubmitTitle', 'Confirm Submission')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsSubmitConfirmOpen(false)}
              >
                &times;
              </button>
            </div>
            <div className="forms-modal-body">
              <p>
                {t(
                  'modal.confirmSubmitMessage',
                  'Are you sure you want to submit this form? You will not be able to edit it after submission.'
                )}
              </p>
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsSubmitConfirmOpen(false)}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-success"
                onClick={handleSubmitForm}
              >
                {t('buttons.confirmSubmit', 'Yes, Submit')}
              </button>
            </div>
          </div>
        </div>
      )}

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
