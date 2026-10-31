import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { projectApi } from '../features/projects/api/projectApi';
import { getFileUrl } from '../utils/filesService';
import '../styles/Project/projectOverview.css';

export default function ProjectBanner({
  projectIdentifier,
  project,
  children,
}) {
  const [loadedProject, setLoadedProject] = useState(project || null);

  useEffect(() => {
    let active = true;
    setLoadedProject(project || null);

    if (project?.bannerImageIdentifier) {
      return () => {
        active = false;
      };
    }

    projectApi
      .getProjectById(projectIdentifier)
      .then(result => {
        if (active) setLoadedProject(current => ({ ...current, ...result }));
      })
      .catch(() => {
        if (active && !project) setLoadedProject(null);
      });

    return () => {
      active = false;
    };
  }, [project, projectIdentifier]);

  const bannerIdentifier =
    loadedProject?.bannerImageIdentifier ||
    (!project ? loadedProject?.imageIdentifier : null);
  const logoIdentifier = loadedProject?.imageIdentifier;
  const backgroundColor = loadedProject?.primaryColor || '#aab2a6';

  return (
    <section
      className="project-hero project-hero--image"
      style={{
        backgroundColor,
        ...(bannerIdentifier
          ? { backgroundImage: `url("${getFileUrl(bannerIdentifier)}")` }
          : {}),
      }}
      aria-label={loadedProject?.projectName || 'Residential project'}
    >
      {children && <div className="project-banner-content">{children}</div>}
      {logoIdentifier && (
        <div className="project-logo-above-banner">
          <img
            src={getFileUrl(logoIdentifier)}
            alt=""
            className="project-logo"
            onError={event => {
              event.currentTarget.onerror = null;
              event.currentTarget.src = '/fallback.jpg';
            }}
          />
        </div>
      )}
    </section>
  );
}

ProjectBanner.propTypes = {
  projectIdentifier: PropTypes.string.isRequired,
  project: PropTypes.shape({
    projectName: PropTypes.string,
    imageIdentifier: PropTypes.string,
    bannerImageIdentifier: PropTypes.string,
    primaryColor: PropTypes.string,
  }),
  children: PropTypes.node,
};

ProjectBanner.defaultProps = {
  project: null,
  children: null,
};
