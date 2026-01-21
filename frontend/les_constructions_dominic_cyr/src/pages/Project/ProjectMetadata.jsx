import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import { FiUsers } from 'react-icons/fi';
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

    // Refresh data every 30 seconds
    const interval = setInterval(fetchMetadata, 30000);

    // Refresh when page becomes visible (tab focus)
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        fetchMetadata();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      clearInterval(interval);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
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
              src={`${import.meta.env.VITE_FILES_SERVICE_URL || (typeof window !== 'undefined' && (window.location.hostname.includes('lcdci-portal') || window.location.hostname.includes('lcdci-frontend')) ? 'https://files-service-app-xubs2.ondigitalocean.app' : `${window.location.origin}/files`)}/files/${metadata.imageIdentifier}`}
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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <h2 style={{ color: metadata.primaryColor, margin: 0 }}>Assigned Team</h2>
              <a
                href={`/projects/${projectId}/team-management`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '8px 16px',
                  backgroundColor: metadata.primaryColor,
                  color: 'white',
                  textDecoration: 'none',
                  borderRadius: '4px',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer'
                }}
              >
                <FiUsers size={18} />
                Manage Team
              </a>
            </div>
            <div className="team-grid">
              {metadata.assignedUsers.contractors && metadata.assignedUsers.contractors.length > 0 && (
                <div
                  className="team-member"
                  style={{ borderColor: metadata.tertiaryColor }}
                >
                  <h3>Contractors</h3>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'grid', gap: '10px' }}>
                    {metadata.assignedUsers.contractors.map((contractor) => (
                      <li key={contractor.userIdentifier} style={{ borderBottom: '1px solid rgba(0,0,0,0.08)', paddingBottom: '8px' }}>
                        <p className="member-name" style={{ marginBottom: '4px' }}>
                          {contractor.firstName} {contractor.lastName}
                        </p>
                        <p className="member-contact" style={{ marginBottom: contractor.phone ? '2px' : 0 }}>
                          {contractor.primaryEmail}
                        </p>
                        {contractor.phone && (
                          <p className="member-contact" style={{ marginBottom: 0 }}>
                            {contractor.phone}
                          </p>
                        )}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {metadata.assignedUsers.salespersons && metadata.assignedUsers.salespersons.length > 0 && (
                <div
                  className="team-member"
                  style={{ borderColor: metadata.tertiaryColor }}
                >
                  <h3>Salespersons</h3>
                  <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'grid', gap: '10px' }}>
                    {metadata.assignedUsers.salespersons.map((salesperson) => (
                      <li key={salesperson.userIdentifier} style={{ borderBottom: '1px solid rgba(0,0,0,0.08)', paddingBottom: '8px' }}>
                        <p className="member-name" style={{ marginBottom: '4px' }}>
                          {salesperson.firstName} {salesperson.lastName}
                        </p>
                        <p className="member-contact" style={{ marginBottom: salesperson.phone ? '2px' : 0 }}>
                          {salesperson.primaryEmail}
                        </p>
                        {salesperson.phone && (
                          <p className="member-contact" style={{ marginBottom: 0 }}>
                            {salesperson.phone}
                          </p>
                        )}
                      </li>
                    ))}
                  </ul>
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

              {(!metadata.assignedUsers.contractors || metadata.assignedUsers.contractors.length === 0) && 
               (!metadata.assignedUsers.salespersons || metadata.assignedUsers.salespersons.length === 0) && 
               !metadata.assignedUsers.customer && (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px 20px' }}>
                  <p style={{ color: '#999', fontSize: '14px' }}>
                    No team members assigned yet.
                  </p>
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
