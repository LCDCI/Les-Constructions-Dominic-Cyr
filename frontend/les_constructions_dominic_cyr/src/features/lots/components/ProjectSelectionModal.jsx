/* eslint-disable no-console */
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { projectApi } from '../../projects/api/projectApi';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './ProjectSelectionModal.css';

const ProjectSelectionModal = ({ isOpen, onClose }) => {
  const { t } = usePageTranslations('ownerLots');
  const navigate = useNavigate();
  const { getAccessTokenSilently } = useAuth0();

  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      const loadProjects = async () => {
        setLoading(true);
        setError('');
        try {
          const token = await getAccessTokenSilently();
          const projectsData = await projectApi.getAllProjects({}, token);
          setProjects(projectsData);
        } catch (err) {
          console.error('Failed to load projects:', err);
          setError(
            t('projectSelection.errorLoading', 'Failed to load projects')
          );
        } finally {
          setLoading(false);
        }
      };
      loadProjects();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen]);

  const handleProjectSelect = projectIdentifier => {
    onClose();
    navigate(`/projects/${projectIdentifier}/manage-lots`);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="project-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{t('projectSelection.title', 'Select a Project')}</h3>
          <button className="close-btn" onClick={onClose}>
            &times;
          </button>
        </div>

        {loading && <p className="modal-message">Loading...</p>}

        {error && (
          <div className="modal-message error">
            <p>{error}</p>
          </div>
        )}

        {!loading && !error && projects.length === 0 && (
          <p className="modal-message">No projects found</p>
        )}

        {!loading && !error && projects.length > 0 && (
          <ul className="project-list">
            {projects.map(project => (
              <li key={project.projectIdentifier}>
                <button
                  className="project-item"
                  onClick={() => handleProjectSelect(project.projectIdentifier)}
                >
                  {project.projectName}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

ProjectSelectionModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default ProjectSelectionModal;
