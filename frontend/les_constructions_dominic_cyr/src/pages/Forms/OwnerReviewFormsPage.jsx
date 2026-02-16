import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import {
  getAllForms,
  reopenForm,
  completeForm,
  downloadFinalizedForm,
} from '../../features/forms/api/formsApi';
import '../../styles/Forms/owner-review-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const FORM_TYPES = [
  { value: 'ALL', translationKey: 'formTypes.all' },
  { value: 'EXTERIOR_DOORS', translationKey: 'formTypes.exteriorDoors' },
  { value: 'GARAGE_DOORS', translationKey: 'formTypes.garageDoors' },
  { value: 'WINDOWS', translationKey: 'formTypes.windows' },
  { value: 'FLOORING', translationKey: 'formTypes.flooring' },
  { value: 'KITCHEN_CABINETS', translationKey: 'formTypes.kitchenCabinets' },
  { value: 'BATHROOM_FIXTURES', translationKey: 'formTypes.bathroomFixtures' },
  { value: 'PAINT', translationKey: 'formTypes.paint' },
  { value: 'LIGHTING', translationKey: 'formTypes.lighting' },
  { value: 'ROOFING', translationKey: 'formTypes.roofing' },
  { value: 'HVAC', translationKey: 'formTypes.hvac' },
];

const OwnerReviewFormsPage = () => {
  const { t } = usePageTranslations('ownerReviewForms');
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('SUBMITTED');
  const [isReopenModalOpen, setIsReopenModalOpen] = useState(false);
  const [formToReopen, setFormToReopen] = useState(null);
  const [reopenReason, setReopenReason] = useState('');
  const [newInstructions, setNewInstructions] = useState('');
  const [submitError, setSubmitError] = useState(null);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [formToView, setFormToView] = useState(null);

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
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const formsData = await getAllForms(token);
      setForms(formsData || []);
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleReopenForm = async () => {
    try {
      if (!reopenReason.trim()) {
        setSubmitError('Please provide a reason for reopening');
        return;
      }

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const payload = { reopenReason };
      if (newInstructions.trim()) {
        payload.newInstructions = newInstructions;
      }

      await reopenForm(formToReopen.formId, payload, token);
      setIsReopenModalOpen(false);
      setFormToReopen(null);
      setReopenReason('');
      setNewInstructions('');
      await fetchData();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else if (error?.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError('Failed to reopen form. Please try again.');
      }
    }
  };

  const handleCompleteForm = async formId => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      await completeForm(formId, token);
      await fetchData();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const handleDownloadForm = async formId => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      await downloadFinalizedForm(formId, token);
    } catch (error) {
      setSubmitError('Failed to download form. Please try again.');
    }
  };

  const handleViewForm = form => {
    setFormToView(form);
    setIsViewModalOpen(true);
  };

  const closeViewModal = () => {
    setIsViewModalOpen(false);
    setFormToView(null);
  };

  const formatFormValue = value => {
    if (value === null || value === undefined) {
      return '';
    }

    if (Array.isArray(value)) {
      return value.map(formatFormValue).filter(Boolean).join(', ');
    }

    if (typeof value === 'object') {
      return Object.entries(value)
        .map(([key, nestedValue]) => {
          const formatted = formatFormValue(nestedValue);
          return formatted ? `${key}: ${formatted}` : key;
        })
        .join(', ');
    }

    return String(value);
  };

  const filteredForms = forms.filter(form => {
    const matchesType = typeFilter === 'ALL' || form.formType === typeFilter;
    const matchesStatus =
      statusFilter === 'ALL' || form.formStatus === statusFilter;
    return matchesType && matchesStatus;
  });

  const getStatusBadgeClass = status => {
    switch (status) {
      case 'SUBMITTED':
        return 'status-submitted';
      case 'REOPENED':
        return 'status-reopened';
      case 'COMPLETED':
        return 'status-completed';
      default:
        return '';
    }
  };

  return (
    <div className="owner-review-forms-page">
      <div className="forms-header">
        <h1>{t('title', 'Review Submitted Forms')}</h1>
        <p className="forms-subtitle">
          {t('subtitle', 'Review and manage customer form submissions')}
        </p>
      </div>

      <div className="forms-filters">
        <div className="filter-group">
          <label htmlFor="typeFilter">{t('filters.type', 'Form Type')}</label>
          <select
            id="typeFilter"
            value={typeFilter}
            onChange={e => setTypeFilter(e.target.value)}
            className="forms-filter-select"
          >
            {FORM_TYPES.map(type => (
              <option key={type.value} value={type.value}>
                {t(type.translationKey, type.value.replace(/_/g, ' '))}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="statusFilter">{t('filters.status', 'Status')}</label>
          <select
            id="statusFilter"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="forms-filter-select"
          >
            <option value="ALL">{t('filters.allStatuses', 'All')}</option>
            <option value="SUBMITTED">
              {t('filters.submitted', 'Submitted')}
            </option>
            <option value="REOPENED">
              {t('filters.reopened', 'Reopened')}
            </option>
            <option value="COMPLETED">
              {t('filters.completed', 'Completed')}
            </option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="forms-loading">
          <p>{t('loading', 'Loading forms...')}</p>
        </div>
      ) : (
        <div className="forms-container">
          {filteredForms.length === 0 ? (
            <div className="no-forms">
              <p>{t('noForms', 'No forms found')}</p>
            </div>
          ) : (
            <div className="forms-list">
              {filteredForms.map(form => (
                <div key={form.formId} className="form-card">
                  <div className="form-card-header">
                    <h3>
                      {form.formType
                        ? form.formType.replace(/_/g, ' ')
                        : 'Form'}
                    </h3>
                    <span
                      className={`status-badge ${getStatusBadgeClass(form.formStatus)}`}
                    >
                      {form.formStatus}
                    </span>
                  </div>
                  <div className="form-card-details">
                    <p>
                      <strong>{t('labels.customer', 'Customer')}:</strong>{' '}
                      {form.customerName || 'N/A'}
                    </p>
                    <p>
                      <strong>{t('labels.project', 'Project')}:</strong>{' '}
                      {form.projectIdentifier}
                    </p>
                    <p>
                      <strong>{t('labels.lot', 'Lot')}:</strong>{' '}
                      {form.lotIdentifier}
                    </p>
                    {form.lastSubmittedDate && (
                      <p>
                        <strong>{t('labels.submitted', 'Submitted')}:</strong>{' '}
                        {new Date(form.lastSubmittedDate).toLocaleDateString()}
                      </p>
                    )}
                    {form.reopenedDate && (
                      <p>
                        <strong>{t('labels.reopened', 'Reopened')}:</strong>{' '}
                        {new Date(form.reopenedDate).toLocaleDateString()}
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
                    {form.formStatus === 'SUBMITTED' && (
                      <>
                        <button
                          className="form-action-button form-action-view"
                          onClick={() => handleViewForm(form)}
                        >
                          {t('buttons.viewForm', 'View Form')}
                        </button>
                        <button
                          className="form-action-button form-action-reopen"
                          onClick={() => {
                            setFormToReopen(form);
                            setIsReopenModalOpen(true);
                          }}
                        >
                          {t('buttons.reopen', 'Request Changes')}
                        </button>
                        <button
                          className="form-action-button form-action-complete"
                          onClick={() => handleCompleteForm(form.formId)}
                        >
                          {t('buttons.complete', 'Approve & Complete')}
                        </button>
                      </>
                    )}
                    {form.formStatus === 'COMPLETED' && (
                      <button
                        className="form-action-button form-action-download"
                        onClick={() => handleDownloadForm(form.formId)}
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
      )}

      {/* Reopen Form Modal */}
      {isReopenModalOpen && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsReopenModalOpen(false)}
        >
          <div className="forms-modal" onClick={e => e.stopPropagation()}>
            <div className="forms-modal-header">
              <h2>{t('modal.reopen.title', 'Request Form Changes')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsReopenModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              <div className="forms-form-group">
                <label htmlFor="reopenReason">
                  {t(
                    'modal.reopen.reasonLabel',
                    'Reason for Requesting Changes'
                  )}{' '}
                  <span className="required">*</span>
                </label>
                <textarea
                  id="reopenReason"
                  value={reopenReason}
                  onChange={e => setReopenReason(e.target.value)}
                  className="forms-form-textarea"
                  placeholder={t(
                    'modal.reopen.reasonPlaceholder',
                    'Explain what needs to be corrected...'
                  )}
                  rows={4}
                />
              </div>

              <div className="forms-form-group">
                <label htmlFor="newInstructions">
                  {t('modal.reopen.instructionsLabel', 'New Instructions')}{' '}
                  <span className="optional">(Optional)</span>
                </label>
                <textarea
                  id="newInstructions"
                  value={newInstructions}
                  onChange={e => setNewInstructions(e.target.value)}
                  className="forms-form-textarea"
                  placeholder={t(
                    'modal.reopen.instructionsPlaceholder',
                    'Provide additional guidance for the customer...'
                  )}
                  rows={3}
                />
              </div>

              {submitError && (
                <div className="forms-error-message">{submitError}</div>
              )}
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => {
                  setIsReopenModalOpen(false);
                  setReopenReason('');
                  setNewInstructions('');
                  setSubmitError(null);
                }}
              >
                {t('modal.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-primary"
                onClick={handleReopenForm}
              >
                {t('modal.reopen.submit', 'Send for Correction')}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* View Form Modal */}
      {isViewModalOpen && formToView && (
        <div className="forms-modal-overlay" onClick={closeViewModal}>
          <div
            className="forms-modal forms-modal-large"
            onClick={e => e.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>{t('modal.view.title', 'Form Details')}</h2>
              <button className="forms-modal-close" onClick={closeViewModal}>
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              <div className="forms-view-summary">
                <p>
                  <strong>{t('labels.customer', 'Customer')}:</strong>{' '}
                  {formToView.customerName || 'N/A'}
                </p>
                <p>
                  <strong>{t('labels.project', 'Project')}:</strong>{' '}
                  {formToView.projectIdentifier}
                </p>
                <p>
                  <strong>{t('labels.lot', 'Lot')}:</strong>{' '}
                  {formToView.lotIdentifier}
                </p>
              </div>
              <div className="forms-data-block">
                <strong>{t('modal.formData', 'Form Data')}:</strong>
                {Object.keys(formToView.formData || {}).length === 0 ? (
                  <p className="forms-data-empty">
                    {t('modal.noFormData', 'No form data available')}
                  </p>
                ) : (
                  <div className="forms-data-list">
                    {Object.entries(formToView.formData || {}).map(
                      ([key, value]) => (
                        <div key={key} className="forms-data-row">
                          <span className="forms-data-key">{key}</span>
                          <span className="forms-data-value">
                            {formatFormValue(value)}
                          </span>
                        </div>
                      )
                    )}
                  </div>
                )}
              </div>
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={closeViewModal}
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

export default OwnerReviewFormsPage;
