import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import {
  getMyForms,
  updateFormData,
  submitForm,
  getFormHistory,
} from '../../features/forms/api/formsApi';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const FORM_STATUS_LABELS = {
  DRAFT: 'Draft',
  ASSIGNED: 'Assigned',
  IN_PROGRESS: 'In Progress',
  SUBMITTED: 'Submitted',
  REOPENED: 'Reopened',
  COMPLETED: 'Completed',
};

const FORM_FIELDS = {
  EXTERIOR_DOORS: [
    { name: 'doorType', label: 'Door Type', type: 'text', required: true },
    { name: 'doorColor', label: 'Door Color', type: 'text', required: true },
    { name: 'doorSize', label: 'Door Size', type: 'text', required: true },
    { name: 'numberOfDoors', label: 'Number of Doors', type: 'number', required: true },
    { name: 'hardwareFinish', label: 'Hardware Finish', type: 'text', required: false },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
  GARAGE_DOORS: [
    { name: 'doorStyle', label: 'Door Style', type: 'text', required: true },
    { name: 'doorColor', label: 'Door Color', type: 'text', required: true },
    { name: 'doorSize', label: 'Door Size (Width x Height)', type: 'text', required: true },
    { name: 'openerType', label: 'Opener Type', type: 'text', required: true },
    { name: 'windowInserts', label: 'Window Inserts', type: 'text', required: false },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
  WINDOWS: [
    { name: 'windowType', label: 'Window Type', type: 'text', required: true },
    { name: 'frameColor', label: 'Frame Color', type: 'text', required: true },
    { name: 'glassType', label: 'Glass Type', type: 'text', required: true },
    { name: 'numberOfWindows', label: 'Number of Windows', type: 'number', required: true },
    { name: 'dimensions', label: 'Dimensions (per window)', type: 'text', required: true },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
  ASPHALT_SHINGLES: [
    { name: 'shingleBrand', label: 'Shingle Brand', type: 'text', required: true },
    { name: 'shingleColor', label: 'Shingle Color', type: 'text', required: true },
    { name: 'shingleStyle', label: 'Shingle Style', type: 'text', required: true },
    { name: 'roofArea', label: 'Roof Area (sq ft)', type: 'number', required: true },
    { name: 'ventilationNeeded', label: 'Ventilation Needed', type: 'text', required: false },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
  WOODWORK: [
    { name: 'woodType', label: 'Wood Type', type: 'text', required: true },
    { name: 'finish', label: 'Finish', type: 'text', required: true },
    { name: 'stainColor', label: 'Stain/Paint Color', type: 'text', required: true },
    { name: 'roomsAffected', label: 'Rooms Affected', type: 'text', required: true },
    { name: 'trimStyle', label: 'Trim Style', type: 'text', required: false },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
  PAINT: [
    { name: 'paintBrand', label: 'Paint Brand', type: 'text', required: true },
    { name: 'paintFinish', label: 'Paint Finish (Matte/Satin/Gloss)', type: 'text', required: true },
    { name: 'interiorColors', label: 'Interior Colors', type: 'text', required: true },
    { name: 'exteriorColors', label: 'Exterior Colors', type: 'text', required: false },
    { name: 'roomsToPaint', label: 'Rooms to Paint', type: 'text', required: true },
    { name: 'additionalNotes', label: 'Additional Notes', type: 'textarea', required: false },
  ],
};

const CustomerFormsPage = () => {
  const { t } = usePageTranslations('customerForms');
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
      setForms(formsData || []);
      setLoading(false);
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
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

      await updateFormData(
        selectedForm.formId,
        { formData },
        token
      );

      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      fetchForms();
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

      // Save first, then submit
      await updateFormData(
        selectedForm.formId,
        { formData },
        token
      );

      await submitForm(selectedForm.formId, token);

      setIsEditModalOpen(false);
      setSelectedForm(null);
      setFormData({});
      fetchForms();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else if (error?.response?.data?.message) {
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

  const canEditForm = form => {
    return (
      form.status === 'ASSIGNED' ||
      form.status === 'IN_PROGRESS' ||
      form.status === 'REOPENED'
    );
  };

  const renderFormFields = () => {
    if (!selectedForm) return null;

    const fields = FORM_FIELDS[selectedForm.formType] || [];

    return fields.map(field => (
      <div key={field.name} className="forms-form-group">
        <label htmlFor={field.name}>
          {field.label} {field.required && <span className="required">*</span>}
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
          </div>
        </div>
        <div className="forms-content">
          <div className="forms-container">
            <div className="forms-loading">Loading...</div>
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
              <p>{t('noForms', 'No forms assigned to you yet')}</p>
            </div>
          ) : (
            <div className="forms-list">
              {forms.map(form => (
                <div key={form.formId} className="form-card">
                  <div className="form-card-header">
                    <h3 className="form-card-title">
                      {form.formType.replace(/_/g, ' ')}
                    </h3>
                    <span className={`form-status form-status-${form.status}`}>
                      {FORM_STATUS_LABELS[form.status]}
                    </span>
                  </div>
                  <div className="form-card-body">
                    <p>
                      <strong>Assigned:</strong>{' '}
                      {new Date(form.assignmentDate).toLocaleDateString()}
                    </p>
                    {form.submissionDate && (
                      <p>
                        <strong>Submitted:</strong>{' '}
                        {new Date(form.submissionDate).toLocaleDateString()}
                      </p>
                    )}
                    {form.completionDate && (
                      <p>
                        <strong>Completed:</strong>{' '}
                        {new Date(form.completionDate).toLocaleDateString()}
                      </p>
                    )}
                    {form.reopenReason && (
                      <p>
                        <strong>Reopen Reason:</strong> {form.reopenReason}
                      </p>
                    )}
                  </div>
                  <div className="form-card-actions">
                    {canEditForm(form) && (
                      <button
                        className="form-action-button form-action-edit"
                        onClick={() => openEditModal(form)}
                      >
                        {form.status === 'ASSIGNED' ? 'Fill Form' : 'Edit Form'}
                      </button>
                    )}
                    {form.status === 'SUBMITTED' && (
                      <button
                        className="form-action-button form-action-history"
                        onClick={() => handleViewHistory(form)}
                      >
                        View History
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
              <h2>{selectedForm.formType.replace(/_/g, ' ')} Form</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsEditModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              {renderFormFields()}
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsEditModalOpen(false)}
              >
                Cancel
              </button>
              <button
                className="forms-modal-button forms-modal-button-primary"
                onClick={handleSaveForm}
              >
                Save Draft
              </button>
              <button
                className="forms-modal-button forms-modal-button-success"
                onClick={handleSubmitForm}
              >
                Submit Form
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
              <h2>Submission History</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsHistoryModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              {historyLoading ? (
                <div className="forms-loading">Loading history...</div>
              ) : formHistory.length === 0 ? (
                <p>No submission history available</p>
              ) : (
                <div className="forms-history-list">
                  {formHistory.map((history, index) => (
                    <div key={history.historyId} className="history-item">
                      <h4>Submission #{formHistory.length - index}</h4>
                      <p>
                        <strong>Date:</strong>{' '}
                        {new Date(history.submissionDate).toLocaleString()}
                      </p>
                      <div className="history-data">
                        <strong>Form Data:</strong>
                        <pre>{JSON.stringify(history.formDataSnapshot, null, 2)}</pre>
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
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerFormsPage;
