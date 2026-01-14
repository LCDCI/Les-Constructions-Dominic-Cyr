import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchUsers, reactivateUser } from '../../features/users/api/usersApi';
import { FiUsers, FiMapPin, FiCalendar, FiUser, FiSave, FiActivity, FiInfo } from 'react-icons/fi';
import '../../styles/Project/project-team-management.css';

export default function ProjectTeamManagementPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { getAccessTokenSilently } = useAuth0();

  const [project, setProject] = useState(null);
  const [contractors, setContractors] = useState([]);
  const [salespersons, setSalespersons] = useState([]);
  const [selectedContractorId, setSelectedContractorId] = useState('');
  const [selectedSalespersonId, setSelectedSalespersonId] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [activityLog, setActivityLog] = useState([]);
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    loadProjectData();
  }, [projectId]);

  const loadProjectData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const token = await getAccessTokenSilently();

      // Load project details with authentication
      const projectData = await projectApi.getProjectById(projectId, token);
      setProject(projectData);
      setSelectedContractorId(projectData.contractorId || '');
      setSelectedSalespersonId(projectData.salespersonId || '');

      // Load available contractors and salespersons
      const allUsers = await fetchUsers(token);
      const contractorsData = (allUsers || []).filter((u) => u.userRole === 'CONTRACTOR');
      const salespersonsData = (allUsers || []).filter((u) => u.userRole === 'SALESPERSON');

      setContractors(contractorsData);
      setSalespersons(salespersonsData);

      // Fetch activity logs from backend
      try {
        const activityLogsData = await projectApi.getProjectActivityLog(projectId, token);
        const formattedActivities = activityLogsData.map((log) => ({
          action: log.description,
          timeAgo: formatTimeAgo(new Date(log.timestamp)),
          changedBy: log.changedByName,
        }));
        setActivityLog(formattedActivities);
      } catch (activityErr) {
        console.warn('Unable to load activity logs, continuing without them:', activityErr);
        setActivityLog([]);
      }
    } catch (err) {
      console.error('Error loading project data:', err);
      setError('Failed to load project information');
    } finally {
      setIsLoading(false);
    }
  };

  const formatTimeAgo = (date) => {
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    if (diffDays < 30) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

    const diffWeeks = Math.floor(diffDays / 7);
    if (diffWeeks < 4) return `${diffWeeks} week${diffWeeks > 1 ? 's' : ''} ago`;

    const diffMonths = Math.floor(diffDays / 30);
    return `${diffMonths} month${diffMonths > 1 ? 's' : ''} ago`;
  };

  const formatDate = (date) => {
    if (!date) return 'Not available';
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const handleSaveChanges = async () => {
    try {
      setIsSaving(true);
      setError(null);
      setSuccessMessage(null);
      const token = await getAccessTokenSilently();

      // Handle contractor assignment (reactivate if needed)
      if (selectedContractorId !== (project.contractorId || '')) {
        if (selectedContractorId) {
          const contractor = contractors.find((c) => c.userIdentifier === selectedContractorId);
          if (contractor && contractor.userStatus !== 'ACTIVE') {
            await reactivateUser(selectedContractorId, token);
          }
          await projectApi.assignContractorToProject(projectId, selectedContractorId, token);
        } else if (project.contractorId) {
          await projectApi.removeContractorFromProject(projectId, token);
        }
      }

      // Handle salesperson assignment (reactivate if needed)
      if (selectedSalespersonId !== (project.salespersonId || '')) {
        if (selectedSalespersonId) {
          const salesperson = salespersons.find((s) => s.userIdentifier === selectedSalespersonId);
          if (salesperson && salesperson.userStatus !== 'ACTIVE') {
            await reactivateUser(selectedSalespersonId, token);
          }
          await projectApi.assignSalespersonToProject(projectId, selectedSalespersonId, token);
        } else if (project.salespersonId) {
          await projectApi.removeSalespersonFromProject(projectId, token);
        }
      }

      setSuccessMessage('Changes saved successfully!');
      await loadProjectData();
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error saving changes:', err);
      setError(err.message || 'Failed to save changes');
    } finally {
      setIsSaving(false);
    }
  };

  const getUserStatusLabel = (userStatus) => {
    if (!userStatus) return 'Unknown';
    return userStatus.charAt(0).toUpperCase() + userStatus.slice(1).toLowerCase();
  };

  const getStatusBadge = (userStatus) => {
    const statusText = getUserStatusLabel(userStatus);
    const colors = {
      Active: { bg: 'var(--status-active-bg)', color: 'var(--status-active-color)', border: 'var(--status-active-border)' },
      Inactive: { bg: 'var(--status-inactive-bg)', color: 'var(--status-inactive-color)', border: 'var(--status-inactive-border)' },
      Deactivated: { bg: 'var(--status-deactivated-bg)', color: 'var(--status-deactivated-color)', border: 'var(--status-deactivated-border)' },
      Unknown: { bg: 'var(--status-inactive-bg)', color: 'var(--status-inactive-color)', border: 'var(--status-inactive-border)' }
    };
    const style = colors[statusText] || colors.Unknown;
    return (
      <span
        style={{
          display: 'inline-block',
          padding: '2px 8px',
          borderRadius: '12px',
          fontSize: '12px',
          fontWeight: '500',
          backgroundColor: style.bg,
          color: style.color,
          border: `1px solid ${style.border}`,
          marginLeft: '4px'
        }}
      >
        {statusText}
      </span>
    );
  };

  const filteredContractors = contractors.filter(c => {
    if (statusFilter === 'all') return true;
    if (statusFilter === 'active') return c.userStatus === 'ACTIVE';
    if (statusFilter === 'inactive') return c.userStatus === 'INACTIVE' || c.userStatus === 'DEACTIVATED';
    return true;
  });

  const filteredSalespersons = salespersons.filter(s => {
    if (statusFilter === 'all') return true;
    if (statusFilter === 'active') return s.userStatus === 'ACTIVE';
    if (statusFilter === 'inactive') return s.userStatus === 'INACTIVE' || s.userStatus === 'DEACTIVATED';
    return true;
  });

  if (isLoading) {
    return <div className="page team-management-page">Loading...</div>;
  }

  if (!project) {
    return <div className="page team-management-page">Project not found</div>;
  }

  return (
    <div className="page team-management-page">
      <div className="team-management-container">
        <div className="team-management-header">
          <h1>Add Contractor & Salesperson</h1>
          <p className="project-id">Project ID: {project.projectIdentifier}</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {successMessage && <div className="alert alert-success">{successMessage}</div>}

        <div className="team-management-content">
          {/* Project Information Section */}
          <section className="info-section">
            <div className="section-header">
              <h2>Project Information</h2>
              <FiInfo className="section-icon" />
            </div>
            <ul className="project-info-list">
              <li>
                <strong>Project Name:</strong> {project.projectName}
              </li>
              <li>
                <strong>Location:</strong> {project.location || 'Not specified'}
              </li>
              <li>
                <strong>Timeline:</strong> {project.startDate} – {project.endDate || 'TBD'}
              </li>
              <li>
                <strong>Customer:</strong> {project.buyerName || 'Not specified'}
              </li>
            </ul>
          </section>

          {/* Team Assignment Section */}
          <section className="team-assignment-section">
            <div className="section-header">
              <h2>Team Assignment</h2>
              <FiUsers className="section-icon" />
            </div>

            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '16px', alignItems: 'center', justifyContent: 'space-between' }}>
              <button className="add-button" onClick={() => navigate('/users')}>
                + Create New Team Member
              </button>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                <span style={{ fontSize: '14px', fontWeight: '500', color: 'var(--primary-color)' }}>Filter:</span>
                <button
                  onClick={() => setStatusFilter('all')}
                  style={{
                    padding: '6px 12px',
                    border: '1px solid var(--secondary-color)',
                    borderRadius: '4px',
                    backgroundColor: statusFilter === 'all' ? 'var(--primary-color)' : 'white',
                    color: statusFilter === 'all' ? 'white' : 'var(--primary-color)',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: '500'
                  }}
                >
                  All
                </button>
                <button
                  onClick={() => setStatusFilter('active')}
                  style={{
                    padding: '6px 12px',
                    border: '1px solid var(--secondary-color)',
                    borderRadius: '4px',
                    backgroundColor: statusFilter === 'active' ? 'var(--status-active-color)' : 'white',
                    color: statusFilter === 'active' ? 'white' : 'var(--primary-color)',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: '500'
                  }}
                >
                  Active
                </button>
                <button
                  onClick={() => setStatusFilter('inactive')}
                  style={{
                    padding: '6px 12px',
                    border: '1px solid var(--secondary-color)',
                    borderRadius: '4px',
                    backgroundColor: statusFilter === 'inactive' ? 'var(--status-inactive-color)' : 'white',
                    color: statusFilter === 'inactive' ? 'white' : 'var(--primary-color)',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: '500'
                  }}
                >
                  Inactive
                </button>
              </div>
            </div>

            {/* Salesperson Assignment */}
            <div className="assignment-group">
              <h3>Salesperson</h3>
              <div className="select-wrapper">
                <select
                  value={selectedSalespersonId}
                  onChange={(e) => setSelectedSalespersonId(e.target.value)}
                  className="assignment-select"
                  disabled={isSaving}
                >
                  <option value="">Select Salesperson</option>
                  {filteredSalespersons.map((salesperson) => (
                    <option key={salesperson.userIdentifier} value={salesperson.userIdentifier}>
                      {salesperson.firstName} {salesperson.lastName} ({getUserStatusLabel(salesperson.userStatus)})
                    </option>
                  ))}
                </select>
              </div>

              {selectedSalespersonId && (
                <div className="selected-person-list">
                  {salespersons
                    .filter((s) => s.userIdentifier === selectedSalespersonId)
                    .map((salesperson) => (
                      <div key={salesperson.userIdentifier} className="selected-person">
                        <input type="checkbox" checked disabled readOnly />
                        <span>
                          {salesperson.firstName} {salesperson.lastName}
                          {getStatusBadge(salesperson.userStatus)}
                        </span>
                      </div>
                    ))}
                </div>
              )}
            </div>

            {/* Contractors Section */}
            <div className="assignment-group">
              <h3>Contractors</h3>
              <div className="contractors-list">
                {filteredContractors.map((contractor) => (
                  <div key={contractor.userIdentifier} className="contractor-checkbox">
                    <input
                      type="checkbox"
                      id={contractor.userIdentifier}
                      checked={selectedContractorId === contractor.userIdentifier}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedContractorId(contractor.userIdentifier);
                        } else {
                          setSelectedContractorId('');
                        }
                      }}
                      disabled={isSaving}
                    />
                    <label htmlFor={contractor.userIdentifier} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span>{contractor.firstName} {contractor.lastName}</span>
                      {getStatusBadge(contractor.userStatus)}
                    </label>
                  </div>
                ))}
              </div>
            </div>

            <button
              className="save-button"
              onClick={handleSaveChanges}
              disabled={isSaving}
            >
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
          </section>

          {/* Recent Activity Section */}
          <section className="activity-section">
            <div className="section-header">
              <h2>Recent Activity</h2>
              <FiActivity className="section-icon" />
            </div>
            <ul className="activity-list">
              {activityLog.length > 0 ? (
                activityLog.map((activity, index) => (
                  <li key={index}>
                    • {activity.action} — {activity.timeAgo}
                    {activity.changedBy && <span className="changed-by"> (by {activity.changedBy})</span>}
                  </li>
                ))
              ) : (
                <li className="no-activity">No activity yet</li>
              )}
            </ul>
          </section>

          {/* Additional Details Section */}
          <section className="details-section">
            <div className="section-header">
              <h2>Additional Details</h2>
              <FiInfo className="section-icon" />
            </div>
            <div className="details-grid">
              <div className="detail-item">
                <span className="detail-label">Project ID:</span>
                <span className="detail-value">{project.projectIdentifier}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Start Date:</span>
                <span className="detail-value">{formatDate(project.startDate)}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">End Date:</span>
                <span className="detail-value">{formatDate(project.endDate)}</span>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
