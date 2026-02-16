/* eslint-disable no-console */
import { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getMyForms,
  updateFormData,
  getFormHistory,
  downloadFinalizedForm,
  viewFinalizedForm,
} from '../../features/forms/api/formsApi';
import { uploadFile, downloadFile } from '../../features/files/api/filesApi';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

/**
 * Form field configuration for each form type.
 *
 * Field types:
 *  - text / number / textarea: standard inputs
 *  - instructions: static info block (no data stored)
 *  - external-link: opens a URL in a new tab (no data stored)
 *  - section-header: visual divider with optional reference link (no data stored)
 *  - file-upload: PDF upload via files-service, stores { fileId, fileName } in formData
 *  - image-radio: styled radio group with option cards
 *
 * Fields whose name starts with "__" are UI-only and skipped during validation/save.
 */
const FORM_FIELDS = {
  /* ── Exterior Doors ─────────────────────────────────────── */
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

  /* ── Garage Doors ───────────────────────────────────────── */
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

  /* ── Windows ────────────────────────────────────────────── */
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

  /* ── Asphalt Shingles ───────────────────────────────────── */
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

  /* ── Woodwork / Trim ────────────────────────────────────── */
  WOODWORK: [
    /* Interior Door Models */
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

    /* Interior Handles */
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

    /* Baseboards / Trim */
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

    /* Quarter-round */
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

  /* ── Paint ──────────────────────────────────────────────── */
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

const CustomerFormsPage = () => {
  const { t } = usePageTranslations('customerForms');
  const { projectId, lotId } = useParams();
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedForm, setSelectedForm] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [formData, setFormData] = useState({});
  const [submitError, setSubmitError] = useState(null);
  const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
  const [formHistory, setFormHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [uploadError, setUploadError] = useState(null);
  const [isSubmitConfirmOpen, setIsSubmitConfirmOpen] = useState(false);
  const [isViewOnly, setIsViewOnly] = useState(false);
  const fileInputRef = useRef(null);

  const { getAccessTokenSilently, user } = useAuth0();
  const navigate = useNavigate();

  const getToken = () =>
    getAccessTokenSilently({
      authorizationParams: {
        audience:
          import.meta.env.VITE_AUTH0_AUDIENCE ||
          'https://construction-api.loca',
      },
    });

  const redirectToError = (status = 500) => {
    if (status === 404) {
      navigate('/404', { replace: true });
    } else {
      navigate('/error', { replace: true });
    }
  };

  useEffect(() => {
    fetchForms();
  }, []);

  const fetchForms = async () => {
    try {
      setLoading(true);
      const token = await getToken();
      const formsData = await getMyForms(token);
      // Filter forms by projectId and lotId from URL params
      const filteredForms =
        formsData?.filter(
          form =>
            form.projectIdentifier === projectId && form.lotIdentifier === lotId
        ) || [];

      setForms(filteredForms);
      setLoading(false);
    } catch (error) {
      setForms([]);
      setLoading(false);
    }
  };

  const openEditModal = (form, readOnly = false) => {
    setSelectedForm(form);
    setIsViewOnly(readOnly);
    // Normalize file data structure to use fileId consistently
    const normalizedData = { ...(form.formData || {}) };
    Object.keys(normalizedData).forEach(key => {
      const val = normalizedData[key];
      if (val && typeof val === 'object') {
        // Convert various ID property names to both id and fileId for consistency
        const fileId = val.fileId || val.id || val._id || val.uuid || val.UUID;
        if (fileId) {
          normalizedData[key] = { ...val, fileId, id: fileId };
        }
      }
    });
    setFormData(normalizedData);
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

  /* ── File upload handler ──────────────────────────────── */
  const handleFileUpload = async (fieldName, file) => {
    if (!file) return;
    try {
      setUploadingFile(true);
      setUploadError(null);

      const fd = new FormData();
      fd.append('file', file);
      fd.append('category', 'DOCUMENT');
      fd.append('projectId', projectId);
      fd.append('uploadedBy', user?.sub || '');
      fd.append('uploaderRole', 'CUSTOMER');

      const result = await uploadFile(fd);

      // Backend might return id, fileId, _id, uuid, or UUID
      const uploadedId =
        result.id || result.fileId || result._id || result.uuid || result.UUID;

      if (!uploadedId) {
        console.error('No file ID found in upload response. Response:', result);
        throw new Error('File uploaded but no ID returned from server');
      }

      handleFieldChange(fieldName, {
        fileId: uploadedId,
        id: uploadedId, // Store both for compatibility
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
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleDownloadFile = async (fileId, fileName) => {
    try {
      await downloadFile(fileId, fileName, 'CUSTOMER', user?.sub);
    } catch (err) {
      console.error('Failed to download file:', err);
      setUploadError(t('errors.downloadFailed', 'Failed to download file.'));
    }
  };

  /* ── Save / Submit ────────────────────────────────────── */
  const handleSaveForm = async () => {
    try {
      setSubmitError(null);
      const token = await getToken();
      await updateFormData(selectedForm.formId, { formData }, token);
      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      await fetchForms();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else if (error?.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError(t('errors.saveFailed', 'Failed to save form.'));
      }
    }
  };

  const handleSubmitFormClick = () => {
    // Validate required fields before showing confirmation
    const fields = FORM_FIELDS[selectedForm.formType] || [];
    const requiredFields = fields.filter(
      f => f.required && !f.name.startsWith('__')
    );

    const missingFields = requiredFields.filter(f => {
      const val = formData[f.name];
      if (f.type === 'file-upload') {
        const hasFile = val && (val.fileId || val.id);
        return !hasFile;
      }
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
      const token = await getToken();
      await updateFormData(
        selectedForm.formId,
        { formData, isSubmitting: true },
        token
      );

      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      await fetchForms();
    } catch (error) {
      if (error.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError(t('errors.submitFailed', 'Failed to submit form.'));
      }
    }
  };

  const handleViewHistory = async form => {
    try {
      setHistoryLoading(true);
      setIsHistoryModalOpen(true);
      const token = await getToken();
      const historyData = await getFormHistory(form.formId, token);
      setFormHistory(historyData || []);
      setHistoryLoading(false);
    } catch (error) {
      setHistoryLoading(false);
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const handleDownloadForm = async form => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      await downloadFinalizedForm(form.formId, token);
    } catch (error) {
      setSubmitError('Failed to download form. Please try again.');
    }
  };

  const canEditForm = form => {
    return (
      form.formStatus === 'ASSIGNED' ||
      form.formStatus === 'IN_PROGRESS' ||
      form.formStatus === 'REOPENED'
    );
  };

  const formatHistoryValue = value => {
    if (value === null || value === undefined) {
      return '';
    }

    if (Array.isArray(value)) {
      return value.map(formatHistoryValue).filter(Boolean).join(', ');
    }

    if (typeof value === 'object') {
      return Object.entries(value)
        .map(([key, nestedValue]) => {
          const formatted = formatHistoryValue(nestedValue);
          return formatted ? `${key}: ${formatted}` : key;
        })
        .join(', ');
    }

    return String(value);
  };

  /* ── Field renderers ──────────────────────────────────── */

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
    const hasFile = fileVal && (fileVal.fileId || fileVal.id);
    return (
      <div key={field.name} className="forms-form-group">
        <label>
          {t(field.translationKey, field.name)}{' '}
          {field.required && <span className="required">*</span>}
        </label>

        {hasFile ? (
          <div className="forms-file-uploaded">
            <span
              className="forms-file-name forms-file-name-link"
              onClick={() =>
                handleDownloadFile(
                  fileVal.fileId || fileVal.id,
                  fileVal.fileName
                )
              }
              style={{ cursor: 'pointer', textDecoration: 'underline' }}
              title="Click to download"
            >
              📄 {fileVal.fileName}
            </span>
            {!isViewOnly && (
              <button
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
                ref={fileInputRef}
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
              onClick={() => !isViewOnly && handleFieldChange(field.name, opt.value)}
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

  const renderTextInput = field => (
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
        disabled={isViewOnly}
      />
    </div>
  );

  const renderTextarea = field => (
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
        disabled={isViewOnly}
      />
    </div>
  );

  const renderSelect = field => (
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

  if (loading) {
    return (
      <div className="forms-page">
        <div className="forms-hero">
          <div className="forms-hero-content">
            <h1 className="forms-hero-title">{t('title', 'My Forms')}</h1>
            <p className="forms-hero-subtitle">
              {t('project', 'Project')}: {projectId} | {t('lot', 'Lot')}:{' '}
              {lotId}
            </p>
          </div>
        </div>
        <div className="forms-content">
          <div className="forms-container">
            <div className="forms-loading">{t('loading', 'Loading...')}</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="forms-page">
      <div className="forms-hero">
        <div className="forms-hero-content">
          <h1 className="forms-hero-title">{t('title', 'My Forms')}</h1>
          <p className="forms-hero-subtitle">
            Project: {projectId} | Lot: {lotId}
          </p>
        </div>
      </div>

      <div className="forms-content">
        <div className="forms-container">
          <div className="forms-header">
            <h2 className="forms-subtitle">
              {t('subtitle', 'Fill out your assigned forms')}
            </h2>
          </div>

          {submitError && (
            <div className="forms-error">
              <p>{submitError}</p>
              <button onClick={() => setSubmitError(null)}>×</button>
            </div>
          )}

          {forms.length === 0 ? (
            <div className="no-forms">
              <p>
                {t('noForms', 'No forms assigned for this project and lot yet')}
              </p>
            </div>
          ) : (
            <div className="forms-list">
              {forms.map(form => (
                <div key={form.formId} className="form-card">
                  <div className="form-card-header">
                    <h3 className="form-card-title">
                      {t(
                        `formTypes.${form.formType}`,
                        form.formType.replace(/_/g, ' ')
                      )}
                    </h3>
                    <span
                      className={`form-status form-status-${form.formStatus}`}
                    >
                      {t(
                        `status.${form.formStatus.toLowerCase().replace('_', '')}`,
                        form.formStatus
                      )}
                    </span>
                  </div>
                  <div className="form-card-body">
                    <p>
                      <strong>{t('labels.assigned', 'Assigned')}:</strong>{' '}
                      {new Date(form.assignedDate).toLocaleDateString()}
                    </p>
                    {form.lastSubmittedDate && (
                      <p>
                        <strong>{t('labels.submitted', 'Submitted')}:</strong>{' '}
                        {new Date(form.lastSubmittedDate).toLocaleDateString()}
                      </p>
                    )}
                    {form.completedDate && (
                      <p>
                        <strong>{t('labels.completed', 'Completed')}:</strong>{' '}
                        {new Date(form.completedDate).toLocaleDateString()}
                      </p>
                    )}
                    {form.reopenReason && (
                      <p>
                        <strong>
                          {t('labels.reopenReason', 'Reopen Reason')}:
                        </strong>{' '}
                        {form.reopenReason}
                      </p>
                    )}
                  </div>
                  <div className="form-card-actions">
                    {canEditForm(form) && (
                      <button
                        className="form-action-button form-action-edit"
                        onClick={() => openEditModal(form)}
                      >
                        {form.formStatus === 'ASSIGNED'
                          ? t('buttons.fillForm', 'Fill Form')
                          : t('buttons.editForm', 'Edit Form')}
                      </button>
                    )}
                    {form.formStatus === 'SUBMITTED' && (
                      <>
                        <button
                          className="form-action-button form-action-history"
                          onClick={() => handleViewHistory(form)}
                        >
                          {t('buttons.viewHistory', 'View History')}
                        </button>
                        <button
                          className="form-action-button form-action-download"
                          onClick={() => handleDownloadForm(form)}
                        >
                          {t('buttons.downloadPdf', 'Download PDF')}
                        </button>
                      </>
                    )}
                    {form.formStatus === 'COMPLETED' && (
                      <>
                        <button
                          className="form-action-button form-action-history"
                          onClick={() => handleViewHistory(form)}
                        >
                          {t('buttons.viewHistory', 'View History')}
                        </button>
                        <button
                          className="form-action-button form-action-download"
                          onClick={() => handleDownloadForm(form)}
                        >
                          {t('buttons.downloadPdf', 'Download PDF')}
                        </button>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Edit/Fill Form Modal */}
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
                {t(
                  `formTypes.${selectedForm.formType}`,
                  selectedForm.formType.replace(/_/g, ' ')
                )}{' '}
                {t('modal.editTitle', 'Form')}
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
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsEditModalOpen(false)}
              >
                {isViewOnly ? t('buttons.close', 'Close') : t('buttons.cancel', 'Cancel')}
              </button>
              {!isViewOnly && (
                <>
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

      {/* History Modal */}
      {isHistoryModalOpen && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsHistoryModalOpen(false)}
        >
          <div
            className="forms-modal forms-modal-large"
            onClick={e => e.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>{t('modal.historyTitle', 'Submission History')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsHistoryModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              {historyLoading ? (
                <div className="forms-loading">
                  {t('modal.historyLoading', 'Loading history...')}
                </div>
              ) : formHistory.length === 0 ? (
                <p>{t('modal.noHistory', 'No submission history available')}</p>
              ) : (
                <div className="forms-history-list">
                  {formHistory.map((history, index) => (
                    <div key={history.id} className="history-item">
                      <h4>
                        {t(
                          'modal.submissionNumber',
                          'Submission #{{number}}'
                        ).replace('{{number}}', formHistory.length - index)}
                      </h4>
                      <p>
                        <strong>{t('modal.date', 'Date')}:</strong>{' '}
                        {new Date(history.submittedAt).toLocaleString()}
                      </p>
                      <div className="history-data">
                        <strong>{t('modal.formData', 'Form Data')}:</strong>
                        {Object.keys(history.formDataSnapshot || {}).length ===
                        0 ? (
                          <p className="history-data-empty">
                            {t('modal.noFormData', 'No form data available')}
                          </p>
                        ) : (
                          <div className="history-data-list">
                            {Object.entries(history.formDataSnapshot || {}).map(
                              ([key, value]) => (
                                <div key={key} className="history-data-row">
                                  <span className="history-data-key">
                                    {key}
                                  </span>
                                  <span className="history-data-value">
                                    {formatHistoryValue(value)}
                                  </span>
                                </div>
                              )
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsHistoryModalOpen(false)}
              >
                {t('buttons.close', 'Close')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerFormsPage;
