import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchAllContractors, fetchAllSalespersons, fetchAllCustomers, reactivateUser } from '../../features/users/api/usersApi';
import { FiUsers, FiActivity, FiInfo } from 'react-icons/fi';
import '../../styles/Project/project-team-management.css';

export default function ProjectTeamManagementPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { getAccessTokenSilently } = useAuth0();

  const [project, setProject] = useState(null);
  const [contractors, setContractors] = useState([]);
  const [salespersons, setSalespersons] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [selectedContractorIds, setSelectedContractorIds] = useState([]);
  const [selectedSalespersonIds, setSelectedSalespersonIds] = useState([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [activityLog, setActivityLog] = useState([]);
  const [statusFilter, setStatusFilter] = useState('all');
  const [showReactivateConfirm, setShowReactivateConfirm] = useState(false);
  const [userToReactivate, setUserToReactivate] = useState(null);

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
      setSelectedContractorIds(projectData.contractorIds || []);
      setSelectedSalespersonIds(projectData.salespersonIds || []);
      setSelectedCustomerId(projectData.customerId || '');

      // Load all contractors, salespersons, and customers (including inactive) to support filtering
      const contractorsData = await fetchAllContractors(token);
      const salespersonsData = await fetchAllSalespersons(token);
      const customersData = await fetchAllCustomers(token);

      setContractors(contractorsData || []);
      setSalespersons(salespersonsData || []);
      setCustomers(customersData || []);

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

      const previousContractorIds = project.contractorIds || [];
      const previousSalespersonIds = project.salespersonIds || [];
      const previousCustomerId = project.customerId || '';

      // Determine what changed
      const contractorsChanged = JSON.stringify([...selectedContractorIds].sort()) !== JSON.stringify([...previousContractorIds].sort());
      const salespersonsChanged = JSON.stringify([...selectedSalespersonIds].sort()) !== JSON.stringify([...previousSalespersonIds].sort());
      const customerChanged = selectedCustomerId !== previousCustomerId;

      // Check for inactive users that need reactivation
      const inactiveContractorsToAdd = selectedContractorIds
        .filter(id => !previousContractorIds.includes(id))
        .map(id => contractors.find((c) => c.userIdentifier === id))
        .filter(c => c && c.userStatus !== 'ACTIVE');

      const inactiveSalespersonsToAdd = selectedSalespersonIds
        .filter(id => !previousSalespersonIds.includes(id))
        .map(id => salespersons.find((s) => s.userIdentifier === id))
        .filter(s => s && s.userStatus !== 'ACTIVE');

      const inactiveCustomerToAdd = selectedCustomerId && selectedCustomerId !== previousCustomerId
        ? customers.find((c) => c.userIdentifier === selectedCustomerId)
        : null;

      // Check for reactivation confirmation
      for (const contractor of inactiveContractorsToAdd) {
        const confirmed = await new Promise((resolve) => {
          setUserToReactivate({
            user: contractor,
            role: 'contractor',
            resolve
          });
          setShowReactivateConfirm(true);
        });
        
        if (!confirmed) {
          setIsSaving(false);
          return;
        }
        
        await reactivateUser(contractor.userIdentifier, token);
      }

      for (const salesperson of inactiveSalespersonsToAdd) {
        const confirmed = await new Promise((resolve) => {
          setUserToReactivate({
            user: salesperson,
            role: 'salesperson',
            resolve
          });
          setShowReactivateConfirm(true);
        });
        
        if (!confirmed) {
          setIsSaving(false);
          return;
        }
        
        await reactivateUser(salesperson.userIdentifier, token);
      }

      if (inactiveCustomerToAdd && inactiveCustomerToAdd.userStatus !== 'ACTIVE') {
        const confirmed = await new Promise((resolve) => {
          setUserToReactivate({
            user: inactiveCustomerToAdd,
            role: 'customer',
            resolve
          });
          setShowReactivateConfirm(true);
        });
        
        if (!confirmed) {
          setIsSaving(false);
          return;
        }
        
        await reactivateUser(inactiveCustomerToAdd.userIdentifier, token);
      }

      // Handle contractors - remove all then add selected ones
      if (contractorsChanged) {
        // Remove all existing contractors if there are any
        if (previousContractorIds.length > 0) {
          await projectApi.removeContractorFromProject(projectId, token);
        }
        
        // Add all selected contractors
        for (const contractorId of selectedContractorIds) {
          await projectApi.assignContractorToProject(projectId, contractorId, token);
        }
      }

      // Handle salespersons - remove all then add selected ones
      if (salespersonsChanged) {
        // Remove all existing salespersons if there are any
        if (previousSalespersonIds.length > 0) {
          await projectApi.removeSalespersonFromProject(projectId, token);
        }
        
        // Add all selected salespersons
        for (const salespersonId of selectedSalespersonIds) {
          await projectApi.assignSalespersonToProject(projectId, salespersonId, token);
        }
      }

      // Handle customer - remove if cleared, assign if changed
      if (customerChanged) {
        if (previousCustomerId && !selectedCustomerId) {
          // Remove customer
          await projectApi.removeCustomerFromProject(projectId, token);
        } else if (selectedCustomerId) {
          // Assign new customer
          await projectApi.assignCustomerToProject(projectId, selectedCustomerId, token);
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

  const filteredCustomers = customers.filter(c => {
    if (statusFilter === 'all') return true;
    if (statusFilter === 'active') return c.userStatus === 'ACTIVE';
    if (statusFilter === 'inactive') return c.userStatus === 'INACTIVE' || c.userStatus === 'DEACTIVATED';
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
          <h1>Add Contractor, Salesperson & Customer</h1>
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
              <h3>Salespersons</h3>
              <div className="contractors-list">
                {filteredSalespersons.map((salesperson) => (
                  <div key={salesperson.userIdentifier} className="contractor-checkbox">
                    <input
                      type="checkbox"
                      id={`sp-${salesperson.userIdentifier}`}
                      checked={selectedSalespersonIds.includes(salesperson.userIdentifier)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedSalespersonIds([...selectedSalespersonIds, salesperson.userIdentifier]);
                        } else {
                          setSelectedSalespersonIds(selectedSalespersonIds.filter(id => id !== salesperson.userIdentifier));
                        }
                      }}
                      disabled={isSaving}
                    />
                    <label htmlFor={`sp-${salesperson.userIdentifier}`} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span>{salesperson.firstName} {salesperson.lastName}</span>
                      {getStatusBadge(salesperson.userStatus)}
                    </label>
                  </div>
                ))}
              </div>
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
                      checked={selectedContractorIds.includes(contractor.userIdentifier)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedContractorIds([...selectedContractorIds, contractor.userIdentifier]);
                        } else {
                          setSelectedContractorIds(selectedContractorIds.filter(id => id !== contractor.userIdentifier));
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

            {/* Customer Assignment */}
            <div className="assignment-group">
              <h3>Customer</h3>
              <div className="customer-select-container">
                <select
                  value={selectedCustomerId}
                  onChange={(e) => setSelectedCustomerId(e.target.value)}
                  disabled={isSaving}
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: '4px',
                    border: '1px solid var(--secondary-color)',
                    fontSize: '14px',
                    backgroundColor: 'white',
                    cursor: 'pointer'
                  }}
                >
                  <option value="">-- No Customer Assigned --</option>
                  {filteredCustomers.map((customer) => (
                    <option key={customer.userIdentifier} value={customer.userIdentifier}>
                      {customer.firstName} {customer.lastName} {customer.userStatus !== 'ACTIVE' ? `(${getUserStatusLabel(customer.userStatus)})` : ''}
                    </option>
                  ))}
                </select>
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

      {/* Reactivation Confirmation Dialog */}
      {showReactivateConfirm && userToReactivate && (
        <div className="modal-overlay" onClick={() => {
          userToReactivate.resolve(false);
          setShowReactivateConfirm(false);
          setUserToReactivate(null);
        }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Reactivate User?</h3>
            <p>
              The selected {userToReactivate.role} <strong>{userToReactivate.user.firstName} {userToReactivate.user.lastName}</strong> is currently {getUserStatusLabel(userToReactivate.user.userStatus).toLowerCase()}.
            </p>
            <p>
              Do you want to reactivate this user and assign them to the project?
            </p>
            <div className="modal-actions">
              <button 
                className="btn-cancel" 
                onClick={() => {
                  userToReactivate.resolve(false);
                  setShowReactivateConfirm(false);
                  setUserToReactivate(null);
                }}
              >
                Cancel
              </button>
              <button 
                className="btn-confirm" 
                onClick={() => {
                  userToReactivate.resolve(true);
                  setShowReactivateConfirm(false);
                  setUserToReactivate(null);
                }}
              >
                Reactivate & Assign
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
