import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { getMyForms } from '../../features/forms/api/formsApi';
import { fetchLots } from '../../features/lots/api/lots';
import CustomerFormsPage from './CustomerFormsPage';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const CustomerFormsSelectionPage = () => {
  const { t } = usePageTranslations('customerForms');
  const [formsByProject, setFormsByProject] = useState({});
  const [loading, setLoading] = useState(true);
  const [activeForm, setActiveForm] = useState(null);

  const { getAccessTokenSilently } = useAuth0();

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

      const formsData = await getMyForms(token);
      const forms = formsData || [];
      const projectIds = [
        ...new Set(forms.map(form => form.projectIdentifier).filter(Boolean)),
      ];
      const lotsByIdentifier = {};
      await Promise.all(
        projectIds.map(async projectIdentifier => {
          const lots = await fetchLots({ projectIdentifier, token });
          lots.forEach(lot => {
            if (lot.lotId) {
              lotsByIdentifier[lot.lotId] = lot.id ?? lot.lotNumber;
            }
          });
        })
      );
      const formsWithLotNumbers = forms.map(form => ({
        ...form,
        lotNumber:
          form.lotNumber ||
          lotsByIdentifier[form.lotIdentifier] ||
          form.lotIdentifier,
      }));

      // Group forms by project
      const grouped = formsWithLotNumbers.reduce((acc, form) => {
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
                  <h2 className="forms-project-title">{projectId}</h2>
                  {Object.entries(
                    projectData.forms.reduce((groups, form) => {
                      const lot = form.lotNumber || form.lotIdentifier || '—';
                      (groups[lot] ||= []).push(form);
                      return groups;
                    }, {})
                  ).map(([lot, lotForms]) => (
                    <section key={lot} className="forms-lot-group">
                      <h3 className="forms-lot-title">
                        {t('lot', 'Lot')}: {lot}
                      </h3>
                      <div className="forms-list">
                        {lotForms.map(form => (
                          <div
                            key={form.formId}
                            className="form-card clickable"
                            onClick={() => setActiveForm(form)}
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
                              <button
                                type="button"
                                className="form-card-action"
                                onClick={event => {
                                  event.stopPropagation();
                                  setActiveForm(form);
                                }}
                              >
                                {form.formStatus === 'SUBMITTED' ||
                                form.formStatus === 'COMPLETED'
                                  ? t('viewForm', 'View form')
                                  : t('openForm', 'Open form')}
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </section>
                  ))}
                </div>
              ))}
            </div>
          )}
        </div>
        {activeForm && (
          <CustomerFormsPage
            initialForm={activeForm}
            embedded
            onClose={() => setActiveForm(null)}
            onSaved={fetchData}
          />
        )}
      </div>
    </div>
  );
};

export default CustomerFormsSelectionPage;
