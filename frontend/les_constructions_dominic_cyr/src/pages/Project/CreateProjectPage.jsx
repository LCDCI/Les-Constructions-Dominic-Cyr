import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import CreateProjectForm from '../../features/projects/components/CreateProjectForm';
import '../../styles/Project/create-project.css';

const CreateProjectPage = () => {
  const { i18n } = useTranslation();
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState(null);
  const isFrench = (i18n.language || '').toLowerCase().startsWith('fr');
  const pageTitle = isFrench ? "Créer un nouveau projet" : 'Create New Project';

  const handleCancel = () => {
    navigate('/projects');
  };

  const handleSuccess = projectIdentifier => {
    // Redirect to projects list or detail page
    navigate(`/projects/${projectIdentifier}`);
  };

  return (
    <div className="create-project-page">
      <div className="create-project-container">
        <div className="create-project-header">
          <h1>{pageTitle}</h1>
        </div>

        {submitError && <div className="error-message">{submitError}</div>}

        <CreateProjectForm
          onCancel={handleCancel}
          onSuccess={handleSuccess}
          onError={setSubmitError}
        />
      </div>
    </div>
  );
};

export default CreateProjectPage;
