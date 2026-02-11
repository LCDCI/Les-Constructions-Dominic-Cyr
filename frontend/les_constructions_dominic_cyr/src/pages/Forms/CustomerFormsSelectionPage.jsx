import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchLots } from '../../features/lots/api/lots';
import { getMyForms } from '../../features/forms/api/formsApi';
import '../../styles/Forms/customer-forms.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const CustomerFormsSelectionPage = () => {
  const { t } = usePageTranslations('customerForms');
  const [projects, setProjects] = useState([]);
  const [lots, setLots] = useState([]);
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedProject, setSelectedProject] = useState('');

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

  useEffect(() => {
    if (selectedProject) {
      fetchLotsForProject();
    } else {
      setLots([]);
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

      const [projectsData, formsData] = await Promise.all([
        projectApi.getAllProjects({}, token),
        getMyForms(token),
      ]);

      setProjects(projectsData || []);
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
        token,
      });
      setLots(lotsData || []);
    } catch (error) {
      setLots([]);
    }
  };

  const getFormCountForLot = (projectId, lotId) => {
    return forms.filter(
      form => form.projectIdentifier === projectId && form.lotIdentifier === lotId
    ).length;
  };

  const handleLotClick = (projectId, lotId) => {
    navigate(`/projects/${projectId}/lots/${lotId}/forms`);
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
          <p className="forms-hero-subtitle">
            Select a project and lot to view your forms
          </p>
        </div>
      </div>

      <div className="forms-content">
        <div className="forms-container">
          <div className="forms-selection-container">
            <div className="forms-form-group">
              <label htmlFor="project">Project</label>
              <select
                id="project"
                value={selectedProject}
                onChange={e => setSelectedProject(e.target.value)}
                className="forms-form-select"
              >
                <option value="">Select a project</option>
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

            {selectedProject && lots.length > 0 && (
              <div className="lots-list">
                <h3>Select a lot:</h3>
                <div className="forms-list">
                  {lots.map(lot => {
                    const formCount = getFormCountForLot(
                      selectedProject,
                      lot.lotIdentifier
                    );
                    return (
                      <div
                        key={lot.lotIdentifier}
                        className="form-card clickable"
                        onClick={() =>
                          handleLotClick(selectedProject, lot.lotIdentifier)
                        }
                      >
                        <div className="form-card-header">
                          <h3 className="form-card-title">
                            Lot {lot.lotNumber}
                          </h3>
                          {formCount > 0 && (
                            <span className="form-count-badge">
                              {formCount} {formCount === 1 ? 'form' : 'forms'}
                            </span>
                          )}
                        </div>
                        <div className="form-card-body">
                          {lot.civicAddress && (
                            <p>
                              <strong>Address:</strong> {lot.civicAddress}
                            </p>
                          )}
                          {formCount === 0 && (
                            <p className="no-forms-text">No forms assigned yet</p>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {selectedProject && lots.length === 0 && (
              <div className="no-forms">
                <p>No lots found for this project</p>
              </div>
            )}

            {!selectedProject && (
              <div className="no-forms">
                <p>Please select a project to view lots</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CustomerFormsSelectionPage;
