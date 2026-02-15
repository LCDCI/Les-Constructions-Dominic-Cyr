import { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import {
  getAllForms,
  createForm,
  reopenForm,
  completeForm,
  deleteForm,
} from '../../features/forms/api/formsApi';
import { fetchCustomersWithSharedLots } from '../../features/users/api/usersApi';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchLots } from '../../features/lots/api/lots';
import '../../styles/Forms/salesperson-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const FORM_TYPES = [
  { value: 'EXTERIOR_DOORS', translationKey: 'formTypes.exteriorDoors' },
  { value: 'GARAGE_DOORS', translationKey: 'formTypes.garageDoors' },
  { value: 'WINDOWS', translationKey: 'formTypes.windows' },
  { value: 'ASPHALT_SHINGLES', translationKey: 'formTypes.asphaltShingles' },
  { value: 'WOODWORK', translationKey: 'formTypes.woodwork' },
  { value: 'PAINT', translationKey: 'formTypes.paint' },
];

const SalespersonFormsPage = () => {
  const { t } = usePageTranslations('salespersonForms');
  const [forms, setForms] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [selectedProject, setSelectedProject] = useState('');
  const [selectedLot, setSelectedLot] = useState('');
  const [selectedFormType, setSelectedFormType] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isReopenModalOpen, setIsReopenModalOpen] = useState(false);
  const [formToReopen, setFormToReopen] = useState(null);
  const [reopenReason, setReopenReason] = useState('');
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [formToDelete, setFormToDelete] = useState(null);
  const [submitError, setSubmitError] = useState(null);

  const { getAccessTokenSilently } = useAuth0();
  const navigate = useNavigate();
  const createModalRef = useRef(null);

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

  useEffect(() => {
    if (selectedCustomer) {
      fetchProjectsForCustomer();
    } else {
      setProjects([]);
      setSelectedProject('');
      setLots([]);
      setSelectedLot('');
    }
  }, [selectedCustomer]);

  useEffect(() => {
    if (selectedProject) {
      fetchLotsForProject();
    } else {
      setLots([]);
      setSelectedLot('');
    }
  }, [selectedProject]);

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

      const [formsData, customersData] = await Promise.all([
        getAllForms(token),
        fetchCustomersWithSharedLots(token),
      ]);

      setForms(formsData || []);
      setCustomers(customersData || []);
      setLoading(false);
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const fetchProjectsForCustomer = async () => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const projectsData = await projectApi.getAllProjects(
        { customerId: selectedCustomer },
        token
      );
      setProjects(projectsData || []);
    } catch (error) {
      setProjects([]);
    }
  };

  const fetchLotsForProject = async () => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const lotsData = await fetchLots({
        projectIdentifier: selectedProject,
        customerId: selectedCustomer, // Filter lots by both salesperson and customer
        token,
      });
      setLots(lotsData || []);
    } catch (error) {
      setLots([]);
    }
  };

  const handleCreateForm = async () => {
    try {
      setSubmitError(null);

      if (
        !selectedCustomer ||
        !selectedFormType ||
        !selectedProject ||
        !selectedLot
      ) {
        setSubmitError('Please select a customer, project, lot, and form type');
        return;
      }

      const customer = customers.find(
        c => c.userIdentifier === selectedCustomer
      );
      if (!customer || !customer.userIdentifier) {
        setSubmitError('Invalid customer selected');
        return;
      }

      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const payload = {
        customerId: customer.userIdentifier,
        formType: selectedFormType,
        projectIdentifier: selectedProject,
        lotIdentifier: selectedLot,
      };

      await createForm(payload, token);

      handleCloseModal();
      fetchData();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else if (error?.response?.data?.message) {
        setSubmitError(error.response.data.message);
      } else {
        setSubmitError('Failed to create form. Please try again.');
      }
    }
  };

  const handleCloseModal = () => {
    setIsCreateOpen(false);
    setSelectedCustomer('');
    setSelectedProject('');
    setSelectedLot('');
    setSelectedFormType('');
    setSubmitError(null);
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

      await reopenForm(formToReopen.formId, { reopenReason }, token);

      setIsReopenModalOpen(false);
      setFormToReopen(null);
      setReopenReason('');
      fetchData();
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
      fetchData();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const handleDeleteForm = async () => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      await deleteForm(formToDelete.formId, token);

      setIsDeleteModalOpen(false);
      setFormToDelete(null);
      fetchData();
    } catch (error) {
      if (error?.response?.status === 404) {
        redirectToError(404);
      } else {
        redirectToError();
      }
    }
  };

  const filteredForms = forms.filter(form => {
    const matchesSearch =
      form.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      form.formType?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' || form.formStatus === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const getCustomerName = customerId => {
    const customer = customers.find(c => c.userIdentifier === customerId);
    return customer
      ? `${customer.firstName} ${customer.lastName}`
      : 'Unknown Customer';
  };

  const getProjectName = projectId => {
    const project = projects.find(p => p.projectIdentifier === projectId);
    return project?.projectName || projectId;
  };

  if (loading) {
    return (
      <div className="forms-page">
        <div className="forms-hero">
          <div className="forms-hero-content">
            <h1 className="forms-hero-title">{t('title', 'Customer Forms')}</h1>
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
          <h1 className="forms-hero-title">{t('title', 'Customer Forms')}</h1>
        </div>
      </div>

      <div className="forms-content">
        <div className="forms-container">
          <div className="forms-header">
            <h2 className="forms-subtitle">
              {t('subtitle', 'Assign and Manage Customer Forms')}
            </h2>
            <button
              className="forms-create-button"
              onClick={() => setIsCreateOpen(true)}
            >
              {t('createButton', 'Assign New Form')}
            </button>
          </div>

          {submitError && (
            <div className="forms-error">
              <p>{submitError}</p>
              <button onClick={() => setSubmitError(null)}>×</button>
            </div>
          )}

          <div className="forms-filters">
            <div className="forms-search-container">
              <input
                type="text"
                placeholder={t('searchPlaceholder', 'Search forms...')}
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className="forms-search-input"
              />
            </div>
            <select
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="forms-filter-select"
            >
              <option value="ALL">
                {t('filters.allStatuses', 'All Statuses')}
              </option>
              <option value="ASSIGNED">
                {t('filters.assigned', 'Assigned')}
              </option>
              <option value="IN_PROGRESS">
                {t('filters.inProgress', 'In Progress')}
              </option>
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

          {filteredForms.length === 0 ? (
            <div className="no-forms">
              <p>{t('noForms', 'No forms found')}</p>
            </div>
          ) : (
            <div className="forms-list">
              {filteredForms.map(form => (
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
                      <strong>{t('labels.customer', 'Customer')}:</strong>{' '}
                      {getCustomerName(form.customerId)}
                    </p>
                    <p>
                      <strong>{t('labels.project', 'Project')}:</strong>{' '}
                      {getProjectName(form.projectIdentifier)}
                    </p>
                    <p>
                      <strong>{t('labels.lot', 'Lot')}:</strong>{' '}
                      {form.lotIdentifier}
                    </p>
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
                    {form.reopenedDate && (
                      <p>
                        <strong>{t('labels.reopened', 'Reopened')}:</strong>{' '}
                        {new Date(form.reopenedDate).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  <div className="form-card-actions">
                    {form.formStatus === 'SUBMITTED' && (
                      <>
                        <button
                          className="form-action-button form-action-reopen"
                          onClick={() => {
                            setFormToReopen(form);
                            setIsReopenModalOpen(true);
                          }}
                        >
                          {t('buttons.reopen', 'Reopen')}
                        </button>
                        <button
                          className="form-action-button form-action-complete"
                          onClick={() => handleCompleteForm(form.formId)}
                        >
                          {t('buttons.complete', 'Complete')}
                        </button>
                      </>
                    )}
                    {(form.formStatus === 'ASSIGNED' ||
                      form.formStatus === 'DRAFT') && (
                      <button
                        className="form-action-button form-action-delete"
                        onClick={() => {
                          setFormToDelete(form);
                          setIsDeleteModalOpen(true);
                        }}
                      >
                        {t('buttons.delete', 'Delete')}
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Create Form Modal */}
      {isCreateOpen && (
        <div className="forms-modal-overlay" onClick={handleCloseModal}>
          <div
            className="forms-modal"
            ref={createModalRef}
            onClick={e => e.stopPropagation()}
          >
            <div className="forms-modal-header">
              <h2>{t('modal.create.title', 'Assign New Form')}</h2>
              <button className="forms-modal-close" onClick={handleCloseModal}>
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
              <div className="forms-form-group">
                <label htmlFor="customer">
                  {t('modal.create.customer', 'Customer')}{' '}
                  {t('modal.create.required', '*')}
                </label>
                <select
                  id="customer"
                  value={selectedCustomer}
                  onChange={e => setSelectedCustomer(e.target.value)}
                  className="forms-form-select"
                >
                  <option value="">
                    {t('modal.create.selectCustomer', 'Select a customer')}
                  </option>
                  {customers.map(customer => (
                    <option
                      key={customer.userIdentifier}
                      value={customer.userIdentifier}
                    >
                      {customer.firstName} {customer.lastName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="forms-form-group">
                <label htmlFor="project">
                  {t('modal.create.project', 'Project')}{' '}
                  {t('modal.create.required', '*')}
                </label>
                <select
                  id="project"
                  value={selectedProject}
                  onChange={e => setSelectedProject(e.target.value)}
                  className="forms-form-select"
                  disabled={!selectedCustomer}
                >
                  <option value="">
                    {!selectedCustomer
                      ? t(
                          'modal.create.selectCustomerFirst',
                          'Select a customer first'
                        )
                      : projects.length === 0
                        ? t(
                            'modal.create.noProjectsAvailable',
                            'No projects available for this customer'
                          )
                        : t('modal.create.selectProject', 'Select a project')}
                  </option>
                  {projects.map(project => (
                    <option
                      key={project.projectIdentifier}
                      value={project.projectIdentifier}
                    >
                      {project.projectName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="forms-form-group">
                <label htmlFor="lot">
                  {t('modal.create.lot', 'Lot')}{' '}
                  {t('modal.create.required', '*')}
                </label>
                <select
                  id="lot"
                  value={selectedLot}
                  onChange={e => setSelectedLot(e.target.value)}
                  className="forms-form-select"
                  disabled={!selectedProject}
                >
                  <option value="">
                    {selectedProject
                      ? t('modal.create.selectLot', 'Select a lot')
                      : t(
                          'modal.create.selectProjectFirst',
                          'Select a project first'
                        )}
                  </option>
                  {lots.map(lot => (
                    <option key={lot.lotId} value={lot.lotId}>
                      {lot.lotNumber} -{' '}
                      {lot.civicAddress ||
                        t('modal.create.noAddress', 'No address')}
                    </option>
                  ))}
                </select>
              </div>
              <div className="forms-form-group">
                <label htmlFor="formType">
                  {t('modal.create.formType', 'Form Type')}{' '}
                  {t('modal.create.required', '*')}
                </label>
                <select
                  id="formType"
                  value={selectedFormType}
                  onChange={e => setSelectedFormType(e.target.value)}
                  className="forms-form-select"
                >
                  <option value="">
                    {t('modal.create.selectFormType', 'Select a form type')}
                  </option>
                  {FORM_TYPES.map(type => (
                    <option key={type.value} value={type.value}>
                      {t(type.translationKey, type.value)}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={handleCloseModal}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-primary"
                onClick={handleCreateForm}
              >
                {t('buttons.assignForm', 'Assign Form')}
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
              <h2>{t('modal.reopen.title', 'Reopen Form')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsReopenModalOpen(false)}
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
              <div className="forms-form-group">
                <label htmlFor="reopenReason">
                  {t('modal.reopen.reasonLabel', 'Reason for Reopening')}{' '}
                  {t('modal.reopen.required', '*')}
                </label>
                <textarea
                  id="reopenReason"
                  value={reopenReason}
                  onChange={e => setReopenReason(e.target.value)}
                  className="forms-form-textarea"
                  placeholder={t(
                    'modal.reopen.reasonPlaceholder',
                    'Explain why this form needs to be reopened...'
                  )}
                  rows="4"
                />
              </div>
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsReopenModalOpen(false)}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-primary"
                onClick={handleReopenForm}
              >
                {t('modal.reopen.buttonReopen', 'Reopen Form')}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {isDeleteModalOpen && (
        <div
          className="forms-modal-overlay"
          onClick={() => setIsDeleteModalOpen(false)}
        >
          <div className="forms-modal" onClick={e => e.stopPropagation()}>
            <div className="forms-modal-header">
              <h2>{t('modal.delete.title', 'Delete Form')}</h2>
              <button
                className="forms-modal-close"
                onClick={() => setIsDeleteModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="forms-modal-body">
              <p>
                {t(
                  'modal.delete.message',
                  'Are you sure you want to delete this form? This action cannot be undone.'
                )}
              </p>
            </div>
            <div className="forms-modal-footer">
              <button
                className="forms-modal-button forms-modal-button-secondary"
                onClick={() => setIsDeleteModalOpen(false)}
              >
                {t('buttons.cancel', 'Cancel')}
              </button>
              <button
                className="forms-modal-button forms-modal-button-danger"
                onClick={handleDeleteForm}
              >
                {t('modal.delete.buttonDelete', 'Delete')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SalespersonFormsPage;
