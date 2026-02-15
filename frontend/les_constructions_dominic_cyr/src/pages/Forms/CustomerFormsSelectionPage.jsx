import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import { getMyForms } from '../../features/forms/api/formsApi';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const CustomerFormsSelectionPage = () => {
  const { t } = usePageTranslations('customerForms');
  const [formsByProject, setFormsByProject] = useState({});
  const [loading, setLoading] = useState(true);

  const { getAccessTokenSilently } = useAuth0();
  const navigate = useNavigate();

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

      const formsData = await getMyForms(token);
      const forms = formsData || [];

      // Group forms by project
      const grouped = forms.reduce((acc, form) => {
        const projectId = form.projectIdentifier || 'unknown';
        if (!acc[projectId]) {
          acc[projectId] = {
            projectName: form.projectName || projectId,
            forms: [],
          };
        }
        acc[projectId].forms.push(form);
        return acc;
      }, {});

      setFormsByProject(grouped);
      setLoading(false);
    } catch (error) {
      setFormsByProject({});
      setLoading(false);
    }
  };

  const handleFormClick = form => {
    navigate(
      `/projects/${form.projectIdentifier}/lots/${form.lotIdentifier}/forms`
    );
  };

  const getStatusClass = status => {
    switch (status) {
      case 'ASSIGNED':
        return 'status-assigned';
      case 'IN_PROGRESS':
        return 'status-in-progress';
      case 'SUBMITTED':
        return 'status-submitted';
      case 'COMPLETED':
        return 'status-completed';
      case 'REOPENED':
        return 'status-reopened';
      default:
        return '';
    }
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
            <div className="forms-loading">{t('loading', 'Loading...')}</div>
          </div>
        </div>
      </div>
    );
  }

  const projectEntries = Object.entries(formsByProject);

  return (
    <div className="forms-page">
      <div className="forms-hero">
        <div className="forms-hero-content">
          <h1 className="forms-hero-title">{t('title', 'My Forms')}</h1>
          <p className="forms-hero-subtitle">
            {t('heroSubtitleAll', 'View and fill out all your assigned forms')}
          </p>
        </div>
      </div>

      <div className="forms-content">
        <div className="forms-container">
          {projectEntries.length === 0 ? (
            <div className="no-forms">
              <p>
                {t(
                  'noFormsAssigned',
                  'No forms have been assigned to you yet. They will appear here once assigned.'
                )}
              </p>
            </div>
          ) : (
            <div className="forms-all-projects">
              {projectEntries.map(([projectId, projectData]) => (
                <div key={projectId} className="forms-project-group">
                  <h2 className="forms-project-title">
                    {projectData.projectName}
                  </h2>
                  <div className="forms-list">
                    {projectData.forms.map(form => (
                      <div
                        key={form.formId}
                        className="form-card clickable"
                        onClick={() => handleFormClick(form)}
                      >
                        <div className="form-card-header">
                          <h3 className="form-card-title">
                            {t(
                              `formTypes.${form.formType}`,
                              form.formType?.replace(/_/g, ' ') || 'Form'
                            )}
                          </h3>
                          <span
                            className={`form-status-badge ${getStatusClass(form.formStatus)}`}
                          >
                            {t(
                              `status.${(form.formStatus || '').toLowerCase()}`,
                              form.formStatus || 'Unknown'
                            )}
                          </span>
                        </div>
                        <div className="form-card-body">
                          {form.lotNumber && (
                            <p>
                              <strong>{t('lot', 'Lot')}:</strong>{' '}
                              {form.lotNumber}
                            </p>
                          )}
                          {form.assignedAt && (
                            <p>
                              <strong>
                                {t('labels.assigned', 'Assigned')}:
                              </strong>{' '}
                              {new Date(form.assignedAt).toLocaleDateString()}
                            </p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CustomerFormsSelectionPage;
