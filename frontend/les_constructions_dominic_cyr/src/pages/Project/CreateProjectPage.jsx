import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CreateProjectForm from '../../features/projects/components/CreateProjectForm';
import '../../styles/Project/create-project.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const CreateProjectPage = () => {
  const { t } = usePageTranslations('createProjectPage');
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState(null);

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
          <h1>{t('title', 'Create New Project')}</h1>
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
