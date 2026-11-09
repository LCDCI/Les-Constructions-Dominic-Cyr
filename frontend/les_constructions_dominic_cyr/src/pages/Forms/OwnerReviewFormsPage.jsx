import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import {
  getAllForms,
  reopenForm,
  completeForm,
  decideFormRevision,
  downloadFinalizedForm,
} from '../../features/forms/api/formsApi';
import { FORM_FIELDS } from './CustomerFormsPage';
import {
  createSiteContent,
  fetchSiteContent,
  updateSiteContent,
} from '../../features/siteContent/api/siteContentApi';
import { uploadFile } from '../../features/files/api/filesApi';
import { getFileUrl } from '../../utils/filesService';
import '../../styles/Forms/owner-review-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const FORM_OPTION_FALLBACK_IMAGE = '/fallback.jpg';

const FORM_TYPES = [
  { value: 'ALL', translationKey: 'formTypes.all' },
  { value: 'EXTERIOR_DOORS', translationKey: 'formTypes.exteriorDoors' },
  { value: 'GARAGE_DOORS', translationKey: 'formTypes.garageDoors' },
  { value: 'WINDOWS', translationKey: 'formTypes.windows' },
  { value: 'ASPHALT_SHINGLES', translationKey: 'formTypes.asphaltShingles' },
  { value: 'WOODWORK', translationKey: 'formTypes.woodwork' },
  { value: 'PAINT', translationKey: 'formTypes.paint' },
];

const EDITABLE_FORM_TYPES = [
  'EXTERIOR_DOORS',
  'GARAGE_DOORS',
  'WINDOWS',
  'ASPHALT_SHINGLES',
  'WOODWORK',
  'PAINT',
];

const OwnerReviewFormsPage = () => {
  const { t, currentLanguage } = usePageTranslations('ownerReviewForms');
  const { t: tCustomer } = usePageTranslations('customerForms');
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('SUBMITTED');
  const [searchTerm, setSearchTerm] = useState('');
  const [isReopenModalOpen, setIsReopenModalOpen] = useState(false);
  const [formToReopen, setFormToReopen] = useState(null);
  const [reopenReason, setReopenReason] = useState('');
  const [newInstructions, setNewInstructions] = useState('');
  const [submitError, setSubmitError] = useState(null);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [formToView, setFormToView] = useState(null);
  const [isFormEditorOpen, setIsFormEditorOpen] = useState(false);
  const [editableFormType, setEditableFormType] = useState(
    EDITABLE_FORM_TYPES[0]
  );
  const [formConfiguration, setFormConfiguration] = useState({});
  const [editableFields, setEditableFields] = useState([]);
  const [optionFieldIndex, setOptionFieldIndex] = useState(0);
  const [isSavingConfiguration, setIsSavingConfiguration] = useState(false);
  const [revisionDecision, setRevisionDecision] = useState(null);
  const [revisionDecisionReason, setRevisionDecisionReason] = useState('');

  const { getAccessTokenSilently, user } = useAuth0();
  const navigate = useNavigate();

  const getToken = async () => {
    const { getAuthAudience } = await import('../../utils/authConfig');
    return getAccessTokenSilently({
      authorizationParams: {
        audience: getAuthAudience() || 'https://construction-api.loca',
      },
    });
  };

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
      const { getAuthAudience } = await import('../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: getAuthAudience() || 'https://construction-api.loca',
        },
      });

      const formsData = await getAllForms(token);
      setForms(formsData || []);
      try {
        const configurationItems = await fetchSiteContent(
          'en',
          token,
          'forms-config'
        );
        setFormConfiguration(
          Object.fromEntries(
            (configurationItems || []).map(item => [item.pageKey, item])
          )
        );
      } catch {
        setFormConfiguration({});
      }
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

  const getEditableFields = formType => {
    const savedItem = formConfiguration[formType];
    if (savedItem?.contentText) {
      try {
        const saved = JSON.parse(savedItem.contentText);
        if (Array.isArray(saved.fields)) return saved.fields;
      } catch {
        // Use the built-in form definition when saved content is invalid.
      }
    }

    return (FORM_FIELDS[formType] || []).map(field => ({
      name: field.name,
      label: tCustomer(field.translationKey, field.name),
      options: (field.options || []).map(option => ({
        value: option.value,
        label: option.label || tCustomer(option.labelKey, option.value),
        imageIdentifier: option.imageIdentifier || '',
      })),
    }));
  };

  const openFormEditor = () => {
    const fields = getEditableFields(editableFormType);
    setEditableFields(fields);
    setOptionFieldIndex(
      Math.max(
        0,
        fields.findIndex(field => field.options?.length)
      )
    );
    setIsFormEditorOpen(true);
  };

  const changeEditableFormType = formType => {
    setEditableFormType(formType);
    const fields = getEditableFields(formType);
    setEditableFields(fields);
    setOptionFieldIndex(
      Math.max(
        0,
        fields.findIndex(field => field.options?.length)
      )
    );
  };

  const updateOption = (fieldIndex, optionIndex, key, value) => {
    setEditableFields(fields =>
      fields.map((field, currentFieldIndex) =>
        currentFieldIndex !== fieldIndex
          ? field
          : {
              ...field,
              options: field.options.map((option, currentOptionIndex) =>
                currentOptionIndex === optionIndex
                  ? { ...option, [key]: value }
                  : option
              ),
            }
      )
    );
  };

  const addOption = fieldIndex => {
    setEditableFields(fields =>
      fields.map((field, index) =>
        index === fieldIndex
          ? {
              ...field,
              options: [
                ...field.options,
                {
                  value: `option_${field.options.length + 1}`,
                  label: `Option ${field.options.length + 1}`,
                  imageIdentifier: '',
                },
              ],
            }
          : field
      )
    );
  };

  const updateFieldLabel = (fieldIndex, value) => {
    setEditableFields(fields =>
      fields.map((field, index) =>
        index === fieldIndex ? { ...field, label: value } : field
      )
    );
  };

  const handleOptionImage = async (fieldIndex, optionIndex, file) => {
    if (!file) return;
    try {
      const token = await getToken();
      const data = new FormData();
      data.append('file', file);
      data.append('category', 'PHOTO');
      data.append('uploadedBy', user?.sub || 'owner');
      data.append('uploaderRole', 'OWNER');
      const result = await uploadFile(data, token);
      const imageIdentifier = result.fileId || result.id;
      updateOption(fieldIndex, optionIndex, 'imageIdentifier', imageIdentifier);
    } catch {
      setSubmitError(t('errors.imageUpload', 'Failed to upload image.'));
    }
  };

  const saveFormConfiguration = async () => {
    try {
      setIsSavingConfiguration(true);
      const token = await getToken();
      const contentText = JSON.stringify({ fields: editableFields });
      const existing = formConfiguration[editableFormType];
      const saved = existing
        ? await updateSiteContent(existing.id, contentText, null, token)
        : await createSiteContent(
            {
              pageGroup: 'forms-config',
              pageKey: editableFormType,
              language: 'en',
              contentText,
              sortOrder: 0,
            },
            token
          );
      setFormConfiguration(current => ({
        ...current,
        [editableFormType]: saved,
      }));
      setIsFormEditorOpen(false);
    } catch (error) {
      setSubmitError(
        error?.response?.data?.message ||
          t('errors.saveConfiguration', 'Failed to save form options.')
      );
    } finally {
      setIsSavingConfiguration(false);
    }
  };

  const handleReopenForm = async () => {
    try {
      if (!reopenReason.trim()) {
        setSubmitError('Please provide a reason for reopening');
        return;
      }

      const { getAuthAudience } = await import('../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: getAuthAudience() || 'https://construction-api.loca',
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
      const { getAuthAudience } = await import('../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: getAuthAudience() || 'https://construction-api.loca',
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

  const handleRevisionDecision = async decision => {
    if (!revisionDecisionReason.trim()) {
      setSubmitError(
        t('errors.decisionReasonRequired', 'Please provide a reason.')
      );
      return;
    }
    try {
      const token = await getToken();
      await decideFormRevision(
        revisionDecision.formId,
        decision,
        revisionDecisionReason.trim(),
        token
      );
      setRevisionDecision(null);
      setRevisionDecisionReason('');
      await fetchData();
    } catch (error) {
      setSubmitError(
        error?.response?.data?.message ||
          t('errors.revisionDecisionFailed', 'Failed to decide this revision.')
      );
    }
  };

  const handleDownloadForm = async formId => {
    try {
      const { getAuthAudience } = await import('../../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: getAuthAudience() || 'https://construction-api.loca',
        },
      });

      await downloadFinalizedForm(formId, token, currentLanguage);
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

  const getFieldDefinition = key =>
    Object.values(FORM_FIELDS)
      .flat()
      .find(item => item.name === key);

  const getFieldLabel = key => {
    const field = getFieldDefinition(key);
    return field ? tCustomer(field.translationKey, key) : key;
  };

  const formatFormValue = (value, fieldKey = null) => {
    if (value === null || value === undefined) {
      return '';
    }

    if (Array.isArray(value)) {
      return value
        .map(item => formatFormValue(item, fieldKey))
        .filter(Boolean)
        .join(', ');
    }

    if (typeof value === 'object') {
      if ('label' in value || 'value' in value) {
        const field = getFieldDefinition(fieldKey);
        const option = field?.options?.find(item => item.value === value.value);
        return option?.labelKey
          ? tCustomer(
              option.labelKey,
              option.label || value.label || value.value
            )
          : value.label || value.value || '';
      }
      return Object.entries(value)
        .filter(([key]) => key !== 'imageIdentifier')
        .map(([, nestedValue]) => formatFormValue(nestedValue, fieldKey))
        .filter(Boolean)
        .join(', ');
    }

    return String(value);
  };

  const filteredForms = forms.filter(form => {
    const matchesType = typeFilter === 'ALL' || form.formType === typeFilter;
    const matchesStatus =
      statusFilter === 'ALL' || form.formStatus === statusFilter;
    const search = searchTerm.trim().toLowerCase();
    const matchesSearch =
      !search ||
      [
        form.customerName,
        form.lotNumber,
        form.lotIdentifier,
        form.projectIdentifier,
      ]
        .filter(Boolean)
        .some(value => value.toLowerCase().includes(search));
    return matchesType && matchesStatus && matchesSearch;
  });
  const formsByLot = filteredForms.reduce((groups, form) => {
    const lot = form.lotNumber || t('labels.unknownLot', 'Unknown lot');
    (groups[lot] ||= []).push(form);
    return groups;
  }, {});

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

  const getTranslatedStatus = status =>
    t(
      `status.${String(status || '')
        .toLowerCase()
        .replace('_', '')}`,
      String(status || '').replace(/_/g, ' ')
    );

  return (
    <div className="owner-review-forms-page">
      <div className="forms-header-banner">
        <h1>{t('title', 'Review Submitted Forms')}</h1>
        <p className="forms-subtitle">
          {t('subtitle', 'Review and manage customer form submissions')}
        </p>
      </div>

      <div className="forms-filters">
        <div className="filter-group">
          <label htmlFor="formsSearch">
            {t('filters.search', 'Search by customer or lot')}
          </label>
          <input
            id="formsSearch"
            className="forms-search-input"
            value={searchTerm}
            onChange={event => setSearchTerm(event.target.value)}
            placeholder={t('searchPlaceholder', 'Search forms...')}
          />
        </div>
        <button
          type="button"
          className="form-action-button form-action-edit"
          onClick={openFormEditor}
        >
          {t('buttons.editForms', 'Edit Forms')}
        </button>
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
            <div className="forms-lot-groups">
              {Object.entries(formsByLot).map(([lot, lotForms]) => (
                <section key={lot} className="forms-lot-group">
                  <h2 className="forms-lot-title">
                    {t('labels.lot', 'Lot')}: {lot}
                  </h2>
                  <div className="forms-list">
                    {lotForms.map(form => (
                      <div key={form.formId} className="form-card">
                        <div className="form-card-header">
                          <h3>
                            {form.formType
                              ? t(
                                  `formTypes.${form.formType}`,
                                  form.formType.replace(/_/g, ' ')
                                )
                              : t('labels.form', 'Form')}
                          </h3>
                          <span
                            className={`status-badge ${getStatusBadgeClass(form.formStatus)}`}
                          >
                            {getTranslatedStatus(form.formStatus)}
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
                            {form.lotNumber || form.lotIdentifier}
                          </p>
                          {form.lastSubmittedDate && (
                            <p>
                              <strong>
                                {t('labels.submitted', 'Submitted')}:
                              </strong>{' '}
                              {new Date(
                                form.lastSubmittedDate
                              ).toLocaleDateString()}
                            </p>
                          )}
                          {form.reopenedDate && (
                            <p>
                              <strong>
                                {t('labels.reopened', 'Reopened')}:
                              </strong>{' '}
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
                              {form.pendingRevisionId && (
                                <>
                                  <button
                                    className="form-action-button form-action-complete"
                                    onClick={() => {
                                      setRevisionDecision({
                                        formId: form.formId,
                                        decision: 'accept',
                                      });
                                      setRevisionDecisionReason('');
                                    }}
                                  >
                                    {t(
                                      'buttons.acceptRevision',
                                      'Accept Changes'
                                    )}
                                  </button>
                                  <button
                                    className="form-action-button form-action-reopen"
                                    onClick={() => {
                                      setRevisionDecision({
                                        formId: form.formId,
                                        decision: 'deny',
                                      });
                                      setRevisionDecisionReason('');
                                    }}
                                  >
                                    {t('buttons.denyRevision', 'Deny Changes')}
                                  </button>
                                </>
                              )}
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
                            <>
                              <button
                                className="form-action-button form-action-download"
                                onClick={() => handleDownloadForm(form.formId)}
                              >
                                {t('buttons.downloadPdf', 'Download PDF')}
                              </button>
                              {form.pendingRevisionId && (
                                <>
                                  <button
                                    className="form-action-button form-action-complete"
                                    onClick={() => {
                                      setRevisionDecision({
                                        formId: form.formId,
                                        decision: 'accept',
                                      });
                                      setRevisionDecisionReason('');
                                    }}
                                  >
                                    {t(
                                      'buttons.acceptRevision',
                                      'Accept Changes'
                                    )}
                                  </button>
                                  <button
                                    className="form-action-button form-action-reopen"
                                    onClick={() => {
                                      setRevisionDecision({
                                        formId: form.formId,
                                        decision: 'deny',
                                      });
                                      setRevisionDecisionReason('');
                                    }}
                                  >
                                    {t('buttons.denyRevision', 'Deny Changes')}
                                  </button>
                                </>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </section>
              ))}
            </div>
          )}
        </div>
      )}

      {isFormEditorOpen && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsFormEditorOpen(false)}
        >
          <div
            className="forms-modal forms-config-modal"
            onClick={e => e.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>{t('modal.editForms.title', 'Edit Form Options')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsFormEditorOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              <div className="filter-group">
                <label htmlFor="editableFormType">
                  {t('filters.type', 'Form Type')}
                </label>
                <select
                  id="editableFormType"
                  className="forms-filter-select"
                  value={editableFormType}
                  onChange={e => changeEditableFormType(e.target.value)}
                >
                  {EDITABLE_FORM_TYPES.map(type => (
                    <option key={type} value={type}>
                      {t(
                        FORM_TYPES.find(item => item.value === type)
                          ?.translationKey || '',
                        type.replace(/_/g, ' ')
                      )}
                    </option>
                  ))}
                </select>
                <div className="forms-editor-add-option">
                  <label htmlFor="optionField">
                    {t('modal.editForms.field', 'Field')}
                  </label>
                  <select
                    id="optionField"
                    value={optionFieldIndex}
                    onChange={event =>
                      setOptionFieldIndex(Number(event.target.value))
                    }
                  >
                    {editableFields.map((field, index) => (
                      <option key={field.name} value={index}>
                        {field.label || field.name}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="form-action-button form-action-edit"
                    onClick={() => addOption(optionFieldIndex)}
                    disabled={editableFields.length === 0}
                  >
                    {t('modal.editForms.addOption', 'Add option')}
                  </button>
                </div>
              </div>

              {editableFields.length === 0 ? (
                <p className="forms-config-empty">
                  {t(
                    'modal.editForms.noOptions',
                    'This form has no configurable options yet.'
                  )}
                </p>
              ) : (
                editableFields.map((field, fieldIndex) => (
                  <section className="forms-config-field" key={field.name}>
                    <input
                      className="forms-config-field-label"
                      value={field.label || field.name}
                      aria-label={`Label for ${field.name}`}
                      onChange={e =>
                        updateFieldLabel(fieldIndex, e.target.value)
                      }
                    />
                    {field.options.map((option, optionIndex) => (
                      <div className="forms-config-option" key={option.value}>
                        <input
                          value={option.label || ''}
                          aria-label={`Label for ${option.value}`}
                          onChange={e =>
                            updateOption(
                              fieldIndex,
                              optionIndex,
                              'label',
                              e.target.value
                            )
                          }
                        />
                        <div className="forms-config-option-image">
                          {option.imageIdentifier && (
                            <img
                              src={
                                getFileUrl(option.imageIdentifier) ||
                                FORM_OPTION_FALLBACK_IMAGE
                              }
                              alt={option.label || option.value}
                              className="forms-config-image-preview"
                              onError={event => {
                                event.currentTarget.src =
                                  FORM_OPTION_FALLBACK_IMAGE;
                              }}
                            />
                          )}
                          <label className="forms-config-upload">
                            {t('modal.editForms.image', 'Option image')}
                            <input
                              type="file"
                              accept="image/*"
                              onChange={e =>
                                handleOptionImage(
                                  fieldIndex,
                                  optionIndex,
                                  e.target.files?.[0]
                                )
                              }
                            />
                          </label>
                        </div>
                      </div>
                    ))}
                  </section>
                ))
              )}
            </div>
            <div className="forms-modal-footer">
              <button
                type="button"
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsFormEditorOpen(false)}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                type="button"
                className="forms-modal-button forms-modal-button-primary"
                onClick={saveFormConfiguration}
                disabled={isSavingConfiguration}
              >
                {t('buttons.save', 'Save')}
              </button>
            </div>
          </div>
        </div>
      )}

      {revisionDecision && (
        <div
          className="forms-modal-overlay"
          onClick={() => setRevisionDecision(null)}
        >
          <div
            className="forms-modal"
            onClick={event => event.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>
                {t('modal.revisionDecision.title', 'Review Form Changes')}
              </h2>
            </div>
            <div className="forms-modal-body">
              <p>
                {t('modal.revisionDecision.reason', 'Customer reason')}:{' '}
                {
                  forms.find(form => form.formId === revisionDecision.formId)
                    ?.pendingRevisionReason
                }
              </p>
              <textarea
                rows={4}
                value={revisionDecisionReason}
                onChange={event =>
                  setRevisionDecisionReason(event.target.value)
                }
                placeholder={t(
                  'modal.revisionDecision.placeholder',
                  'Explain your decision'
                )}
              />
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setRevisionDecision(null)}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-primary"
                onClick={() =>
                  handleRevisionDecision(revisionDecision.decision)
                }
              >
                {revisionDecision.decision === 'accept'
                  ? t('buttons.acceptRevision', 'Accept Changes')
                  : t('buttons.denyRevision', 'Deny Changes')}
              </button>
            </div>
          </div>
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
                  {formToView.lotNumber || formToView.lotIdentifier}
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
                          <span className="forms-data-key">
                            {getFieldLabel(key)}
                          </span>
                          <span className="forms-data-value">
                            {formatFormValue(value, key)}
                          </span>
                        </div>
                      )
                    )}
                  </div>
                )}
              </div>
              {formToView.pendingRevisionId && (
                <div className="forms-data-block">
                  <strong>
                    {t('modal.pendingRevision', 'Pending Customer Revision')}:
                  </strong>
                  <p>{formToView.pendingRevisionReason}</p>
                  <div className="forms-data-list">
                    {Object.entries(formToView.pendingRevisionData || {}).map(
                      ([key, value]) => (
                        <div key={`revision-${key}`} className="forms-data-row">
                          <span className="forms-data-key">
                            {getFieldLabel(key)}
                          </span>
                          <span className="forms-data-value">
                            {formatFormValue(value, key)}
                          </span>
                        </div>
                      )
                    )}
                  </div>
                </div>
              )}
              {formToView.formData?.pdfFile && (
                <button
                  type="button"
                  className="form-action-button form-action-download"
                  onClick={() => handleDownloadForm(formToView.formId)}
                >
                  {t('buttons.downloadPdf', 'Download PDF')}
                </button>
              )}
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
