import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getMyForms,
  updateFormData,
  getFormHistory,
  downloadFinalizedForm,
} from '../../features/forms/api/formsApi';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const FORM_FIELDS = {
  EXTERIOR_DOORS: [
    {
      name: 'doorType',
      translationKey: 'formFields.doorType',
      type: 'text',
      required: true,
    },
    {
      name: 'doorColor',
      translationKey: 'formFields.doorColor',
      type: 'text',
      required: true,
    },
    {
      name: 'doorSize',
      translationKey: 'formFields.doorSize',
      type: 'text',
      required: true,
    },
    {
      name: 'numberOfDoors',
      translationKey: 'formFields.numberOfDoors',
      type: 'number',
      required: true,
    },
    {
      name: 'hardwareFinish',
      translationKey: 'formFields.hardwareFinish',
      type: 'text',
      required: false,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
      required: false,
    },
  ],
  GARAGE_DOORS: [
    {
      name: 'doorStyle',
      translationKey: 'formFields.doorStyle',
      type: 'text',
      required: true,
    },
    {
      name: 'doorColor',
      translationKey: 'formFields.doorColor',
      type: 'text',
      required: true,
    },
    {
      name: 'doorSize',
      translationKey: 'formFields.doorSize',
      type: 'text',
      required: true,
    },
    {
      name: 'openerType',
      translationKey: 'formFields.openerType',
      type: 'text',
      required: true,
    },
    {
      name: 'windowInserts',
      translationKey: 'formFields.windowInserts',
      type: 'text',
      required: false,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
      required: false,
    },
  ],
  WINDOWS: [
    {
      name: 'windowType',
      translationKey: 'formFields.windowType',
      type: 'text',
      required: true,
    },
    {
      name: 'frameColor',
      translationKey: 'formFields.frameColor',
      type: 'text',
      required: true,
    },
    {
      name: 'glassType',
      translationKey: 'formFields.glassType',
      type: 'text',
      required: true,
    },
    {
      name: 'numberOfWindows',
      translationKey: 'formFields.numberOfWindows',
      type: 'number',
      required: true,
    },
    {
      name: 'dimensions',
      translationKey: 'formFields.dimensions',
      type: 'text',
      required: true,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
      required: false,
    },
  ],
  ASPHALT_SHINGLES: [
    {
      name: 'shingleBrand',
      translationKey: 'formFields.shingleBrand',
      type: 'text',
      required: true,
    },
    {
      name: 'shingleColor',
      translationKey: 'formFields.shingleColor',
      type: 'text',
      required: true,
    },
    {
      name: 'shingleStyle',
      translationKey: 'formFields.shingleStyle',
      type: 'text',
      required: true,
    },
    {
      name: 'roofArea',
      translationKey: 'formFields.roofArea',
      type: 'number',
      required: true,
    },
    {
      name: 'ventilationNeeded',
      translationKey: 'formFields.ventilationNeeded',
      type: 'text',
      required: false,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
      required: false,
    },
  ],
  WOODWORK: [
    {
      name: 'woodType',
      translationKey: 'formFields.woodType',
      type: 'text',
      required: true,
    },
    {
      name: 'finish',
      translationKey: 'formFields.finish',
      type: 'text',
      required: true,
    },
    {
      name: 'stainColor',
      translationKey: 'formFields.stainColor',
      type: 'text',
      required: true,
    },
    {
      name: 'roomsAffected',
      translationKey: 'formFields.roomsAffected',
      type: 'text',
      required: true,
    },
    {
      name: 'trimStyle',
      translationKey: 'formFields.trimStyle',
      type: 'text',
      required: false,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
      required: false,
    },
  ],
  PAINT: [
    {
      name: 'paintBrand',
      translationKey: 'formFields.paintBrand',
      type: 'text',
      required: true,
    },
    {
      name: 'paintFinish',
      translationKey: 'formFields.paintFinish',
      type: 'text',
      required: true,
    },
    {
      name: 'interiorColors',
      translationKey: 'formFields.interiorColors',
      type: 'text',
      required: true,
    },
    {
      name: 'exteriorColors',
      translationKey: 'formFields.exteriorColors',
      type: 'text',
      required: false,
    },
    {
      name: 'roomsToPaint',
      translationKey: 'formFields.roomsToPaint',
      type: 'text',
      required: true,
    },
    {
      name: 'additionalNotes',
      translationKey: 'formFields.additionalNotes',
      type: 'textarea',
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

  const { getAccessTokenSilently } = useAuth0();
  const navigate = useNavigate();

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
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

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
      console.error(`[fetchForms] Error:`, error);
      setForms([]);
      setLoading(false);
    }
  };

  const openEditModal = form => {
    setSelectedForm(form);
    setFormData(form.formData || {});
    setIsEditModalOpen(true);
    setSubmitError(null);
  };

  const handleFieldChange = (fieldName, value) => {
    setFormData(prev => ({
      ...prev,
      [fieldName]: value,
    }));
  };

  const handleSaveForm = async () => {
    try {
      setSubmitError(null);

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

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
        setSubmitError('Failed to save form. Please try again.');
      }
    }
  };

  const handleSubmitForm = async () => {
    try {
      setSubmitError(null);

      // Validate required fields
      const fields = FORM_FIELDS[selectedForm.formType] || [];
      const requiredFields = fields.filter(f => f.required);
      const missingFields = requiredFields.filter(
        f => !formData[f.name] || formData[f.name].toString().trim() === ''
      );

      if (missingFields.length > 0) {
        setSubmitError(
          `Please fill in all required fields: ${missingFields.map(f => f.label).join(', ')}`
        );
        return;
      }

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

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
        setSubmitError('Failed to submit form. Please try again.');
      }
    }
  };

  const handleViewHistory = async form => {
    try {
      setHistoryLoading(true);
      setIsHistoryModalOpen(true);

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

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

  const renderFormFields = () => {
    if (!selectedForm) return null;

    const fields = FORM_FIELDS[selectedForm.formType] || [];

    return fields.map(field => (
      <div key={field.name} className="forms-form-group">
        <label htmlFor={field.name}>
          {t(field.translationKey, field.name)}{' '}
          {field.required && (
            <span className="required">{t('modal.required', '*')}</span>
          )}
        </label>
        {field.type === 'textarea' ? (
          <textarea
            id={field.name}
            value={formData[field.name] || ''}
            onChange={e => handleFieldChange(field.name, e.target.value)}
            className="forms-form-textarea"
            rows="4"
          />
        ) : (
          <input
            id={field.name}
            type={field.type}
            value={formData[field.name] || ''}
            onChange={e => handleFieldChange(field.name, e.target.value)}
            className="forms-form-input"
          />
        )}
      </div>
    ));
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
                      <button
                        className="form-action-button form-action-history"
                        onClick={() => handleViewHistory(form)}
                      >
                        {t('buttons.viewHistory', 'View History')}
                      </button>
                    )}
                    {form.formStatus === 'COMPLETED' && (
                      <button
                        className="form-action-button form-action-download"
                        onClick={() => handleDownloadForm(form)}
                      >
                        {t('buttons.downloadPdf', 'Download PDF')}
                      </button>
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
            <div className="forms-modal-body">{renderFormFields()}</div>
            <div className="forms-modal-footer">
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
                onClick={handleSubmitForm}
              >
                {t('buttons.submitForm', 'Submit Form')}
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
                        <pre>
                          {JSON.stringify(history.formDataSnapshot, null, 2)}
                        </pre>
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
