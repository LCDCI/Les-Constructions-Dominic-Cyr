import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';
import { fetchLotsByUserId } from '../api/lots';
import { useAuth0 } from '@auth0/auth0-react';
import './LotsListDashboard.css';

/**
 * LotsListDashboard - Displays user's assigned lots grouped by project with navigation to lot documents
 */
const LotsListDashboard = ({ userId }) => {
  const [lotsByProject, setLotsByProject] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { getAccessTokenSilently } = useAuth0();

  useEffect(() => {
    loadLots();
  }, [userId]);

  const loadLots = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = await getAccessTokenSilently();
      
      // Fetch lots assigned to current user
      const userLots = await fetchLotsByUserId(userId, token);
      
      // Group lots by project
      const grouped = (userLots || []).reduce((acc, lot) => {
        const projectId = lot.projectIdentifier || 'unknown';
        if (!acc[projectId]) {
          acc[projectId] = {
            projectName: lot.projectName || 'Unknown Project',
            projectIdentifier: projectId,
            lots: []
          };
        }
        acc[projectId].lots.push(lot);
        return acc;
      }, {});
      
      setLotsByProject(grouped);
    } catch (err) {
      console.error('Failed to load lots:', err);
      setError('Failed to load lots');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="lots-list-dashboard" data-testid="lots-list">
        <div className="loading-state">Loading your lots...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="lots-list-dashboard" data-testid="lots-list">
        <div className="error-state" data-testid="error-state">
          {error}
        </div>
      </div>
    );
  }

  const projectEntries = Object.entries(lotsByProject);

  if (projectEntries.length === 0) {
    return (
      <div className="lots-list-dashboard" data-testid="lots-list">
        <div className="empty-state" data-testid="empty-state">
          <p>No lots assigned to you yet.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="lots-list-dashboard" data-testid="lots-list">
      <h1 className="page-title">My Lot Documents</h1>
      
      {projectEntries.map(([projectId, projectData]) => (
        <div key={projectId} className="project-section">
          <div className="project-header">
            <h2 className="project-title">{projectData.projectName}</h2>
            <Link 
              to={`/projects/${projectData.projectIdentifier}/manage-lots`}
              className="btn btn-secondary"
            >
              Manage Lots
            </Link>
          </div>
          
          <div className="lots-table">
            <div className="lots-table-header">
              <div className="lots-cell">Identifier</div>
              <div className="lots-cell">Lot #</div>
              <div className="lots-cell">Civic Address</div>
              <div className="lots-cell">Area (sqft)</div>
              <div className="lots-cell">Area (m²)</div>
              <div className="lots-cell">Price</div>
              <div className="lots-cell">Status</div>
              <div className="lots-cell lots-cell-actions">Actions</div>
            </div>

            {projectData.lots.map((lot) => (
              <div
                key={lot.lotId}
                className="lots-table-row"
                data-testid={`lot-card-${lot.lotId}`}
              >
                <div className="lots-cell" title={lot.lotId}>{lot.lotId || '—'}</div>
                <div className="lots-cell">{lot.lotNumber || '—'}</div>
                <div className="lots-cell">{lot.civicAddress || '—'}</div>
                <div className="lots-cell">{lot.dimensionsSquareFeet || '—'}</div>
                <div className="lots-cell">{lot.dimensionsSquareMeters || '—'}</div>
                <div className="lots-cell">
                  {lot.price ? `$${lot.price.toLocaleString()}` : '—'}
                </div>
                <div className="lots-cell">
                  {lot.lotStatus ? (
                    <span className={`lot-status lot-status-${lot.lotStatus?.toLowerCase()}`}>
                      {lot.lotStatus}
                    </span>
                  ) : (
                    '—'
                  )}
                </div>
                <div className="lots-cell lots-cell-actions">
                  <Link
                    to={`/projects/${projectData.projectIdentifier}/lots/${lot.lotId}/documents`}
                    className="btn btn-primary lots-action-button"
                    data-testid={`lot-documents-link-${lot.lotId}`}
                  >
                    View Documents
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};

LotsListDashboard.propTypes = {
  userId: PropTypes.string.isRequired,
};

export default LotsListDashboard;
