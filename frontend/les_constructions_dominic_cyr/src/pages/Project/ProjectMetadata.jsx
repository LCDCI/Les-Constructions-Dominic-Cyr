import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import '../../styles/Project/ProjectMetadata.css';

const ProjectMetadata = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [metadata, setMetadata] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        // Wait for Auth0 to finish loading before deciding on token
        if (isLoading) return;

        setLoading(true);
        
        // Get token if authenticated, otherwise fetch without token
        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: { audience: import.meta.env.VITE_AUTH0_AUDIENCE },
            });
          } catch (tokenErr) {
            console.warn('Could not get token, proceeding without auth');
          }
        }
        
        const data = await getProjectMetadata(projectId, token);
        setMetadata(data);

        document.documentElement.style.setProperty(
          '--project-primary',
          data.primaryColor
        );
        document.documentElement.style.setProperty(
          '--project-tertiary',
          data.tertiaryColor
        );
        document.documentElement.style.setProperty(
          '--project-buyer',
          data.buyerColor
        );
      } catch (err) {
        const message = err.response?.data?.message || 'Failed to load project metadata';
        setError(message);
        if (err.response?.status === 403) {
          navigate('/unauthorized', { replace: true });
        }
      } finally {
        setLoading(false);
      }
    };

    fetchMetadata();

    const interval = setInterval(fetchMetadata, 30000);

    return () => {
      clearInterval(interval);
      document.documentElement.style.removeProperty('--project-primary');
      document.documentElement.style.removeProperty('--project-tertiary');
      document.documentElement.style.removeProperty('--project-buyer');
    };
  }, [projectId, isAuthenticated, isLoading, getAccessTokenSilently, navigate]);

  if (loading) {
    return (
      <div className="metadata-loading">
        <div className="spinner"></div>
        <p>Loading project information...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="metadata-error">
        <h2>Access Denied</h2>
        <p>{error}</p>
      </div>
    );
  }

  if (!metadata) {
    return null;
  }

  const formatDate = dateString => {
    if (!dateString) return 'Not set';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getStatusClass = status => {
    return `status-badge status-${status.toLowerCase().replace('_', '-')}`;
  };

  return (
    <div className="project-metadata">
      <div
        className="metadata-hero"
        style={{ backgroundColor: metadata.primaryColor }}
      >
        <div className="hero-content">
          <h1 className="project-title">{metadata.projectName}</h1>
          <span className={getStatusClass(metadata.status)}>
            {metadata.status.replace('_', ' ')}
          </span>
        </div>
        {metadata.imageIdentifier && (
          <div className="hero-image">
            <img
              src={`http://localhost:8082/files/${metadata.imageIdentifier}`}
              alt={metadata.projectName}
            />
          </div>
        )}
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: metadata.primaryColor }}>Project Overview</h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">Location</span>
              <span className="metadata-value">{metadata.location}</span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">Start Date</span>
              <span className="metadata-value">
                {formatDate(metadata.startDate)}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">End Date</span>
              <span className="metadata-value">
                {formatDate(metadata.endDate)}
              </span>
            </div>
            {metadata.completionDate && (
              <div className="metadata-item">
                <span className="metadata-label">Completion Date</span>
                <span className="metadata-value">
                  {formatDate(metadata.completionDate)}
                </span>
              </div>
            )}
            {metadata.progressPercentage !== null && (
              <div className="metadata-item full-width">
                <span className="metadata-label">Progress</span>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{
                      width: `${metadata.progressPercentage}%`,
                      backgroundColor: metadata.buyerColor,
                    }}
                  >
                    {metadata.progressPercentage}%
                  </div>
                </div>
              </div>
            )}
          </div>
          {metadata.projectDescription && (
            <div className="project-description">
              <p>{metadata.projectDescription}</p>
            </div>
          )}
        </section>

        {metadata.assignedUsers && (
          <section className="metadata-section">
            <h2 style={{ color: metadata.primaryColor }}>Assigned Team</h2>
            <div className="team-grid">
              {metadata.assignedUsers.contractor && (
                <div
                  className="team-member"
                  style={{ borderColor: metadata.tertiaryColor }}
                >
                  <h3>Contractor</h3>
                  <p className="member-name">
                    {metadata.assignedUsers.contractor.firstName}{' '}
                    {metadata.assignedUsers.contractor.lastName}
                  </p>
                  <p className="member-contact">
                    {metadata.assignedUsers.contractor.primaryEmail}
                  </p>
                  {metadata.assignedUsers.contractor.phone && (
                    <p className="member-contact">
                      {metadata.assignedUsers.contractor.phone}
                    </p>
                  )}
                </div>
              )}

              {metadata.assignedUsers.salesperson && (
                <div
                  className="team-member"
                  style={{ borderColor: metadata.tertiaryColor }}
                >
                  <h3>Salesperson</h3>
                  <p className="member-name">
                    {metadata.assignedUsers.salesperson.firstName}{' '}
                    {metadata.assignedUsers.salesperson.lastName}
                  </p>
                  <p className="member-contact">
                    {metadata.assignedUsers.salesperson.primaryEmail}
                  </p>
                  {metadata.assignedUsers.salesperson.phone && (
                    <p className="member-contact">
                      {metadata.assignedUsers.salesperson.phone}
                    </p>
                  )}
                </div>
              )}

              {metadata.assignedUsers.customer && (
                <div
                  className="team-member"
                  style={{ borderColor: metadata.tertiaryColor }}
                >
                  <h3>Customer</h3>
                  <p className="member-name">
                    {metadata.assignedUsers.customer.firstName}{' '}
                    {metadata.assignedUsers.customer.lastName}
                  </p>
                  <p className="member-contact">
                    {metadata.assignedUsers.customer.primaryEmail}
                  </p>
                  {metadata.assignedUsers.customer.phone && (
                    <p className="member-contact">
                      {metadata.assignedUsers.customer.phone}
                    </p>
                  )}
                </div>
              )}
            </div>
          </section>
        )}

        {metadata.buyerName && (
          <section className="metadata-section">
            <h2 style={{ color: metadata.primaryColor }}>Buyer Information</h2>
            <div
              className="buyer-info"
              style={{ backgroundColor: metadata.tertiaryColor }}
            >
              <p className="buyer-name">{metadata.buyerName}</p>
            </div>
          </section>
        )}
      </div>

      <div className="button-container">
        <a href={`/projects/${projectId}/schedule`} className="project-metadata-schedule">
          View Project Schedule
        </a>
        <a href={`/projects`} className="project-metadata-back">
          Back to projects
        </a>
      </div>
    </div>
  );
};

export default ProjectMetadata;
