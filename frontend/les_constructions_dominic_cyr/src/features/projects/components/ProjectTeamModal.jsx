import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { projectApi } from '../api/projectApi';
import { fetchActiveContractors } from '../../users/api/usersApi';
import '../../../styles/Project/project-team-modal.css';

export default function ProjectTeamModal({ isOpen, projectIdentifier, currentContractorId, onClose, onSave }) {
  const { getAccessTokenSilently } = useAuth0();
  const [contractors, setContractors] = useState([]);
  const [selectedContractorId, setSelectedContractorId] = useState(currentContractorId || '');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      loadContractors();
      setSelectedContractorId(currentContractorId || '');
    }
  }, [isOpen, currentContractorId]);

  const loadContractors = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const token = await getAccessTokenSilently();
      const activeContractors = await fetchActiveContractors(token);
      setContractors(activeContractors || []);
    } catch (err) {
      console.error('Error loading contractors:', err);
      setError('Failed to load contractors');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      setError(null);
      const token = await getAccessTokenSilently();

      if (selectedContractorId && selectedContractorId !== currentContractorId) {
        // Assign contractor
        await projectApi.assignContractorToProject(projectIdentifier, selectedContractorId, token);
      } else if (!selectedContractorId && currentContractorId) {
        // Remove contractor
        await projectApi.removeContractorFromProject(projectIdentifier, token);
      }

      if (onSave) {
        onSave();
      }
      onClose();
    } catch (err) {
      console.error('Error saving contractor assignment:', err);
      setError(err.message || 'Failed to save changes');
    } finally {
      setIsSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content project-team-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Assign Contractor to Project</h2>
          <button className="modal-close" onClick={onClose} disabled={isSaving}>
            ×
          </button>
        </div>

        <div className="modal-body">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="contractor-select">Select Contractor:</label>
            {isLoading ? (
              <p>Loading contractors...</p>
            ) : contractors.length > 0 ? (
              <>
                <select
                  id="contractor-select"
                  value={selectedContractorId}
                  onChange={(e) => setSelectedContractorId(e.target.value)}
                  disabled={isSaving}
                  className="contractor-select"
                >
                  <option value="">-- No Contractor --</option>
                  {contractors.map((contractor) => (
                    <option key={contractor.userIdentifier} value={contractor.userIdentifier}>
                      {contractor.firstName} {contractor.lastName} ({contractor.primaryEmail})
                    </option>
                  ))}
                </select>
                <p className="hint-text">Only active contractors are shown</p>
              </>
            ) : (
              <p className="no-contractors">No active contractors available</p>
            )}
          </div>

          {selectedContractorId && (
            <div className="contractor-preview">
              {contractors.find((c) => c.userIdentifier === selectedContractorId) && (
                <>
                  <p>
                    <strong>Selected Contractor:</strong>
                  </p>
                  <p>
                    {contractors.find((c) => c.userIdentifier === selectedContractorId)?.firstName}{' '}
                    {contractors.find((c) => c.userIdentifier === selectedContractorId)?.lastName}
                  </p>
                  <p className="email">
                    {contractors.find((c) => c.userIdentifier === selectedContractorId)?.primaryEmail}
                  </p>
                  {contractors.find((c) => c.userIdentifier === selectedContractorId)?.phone && (
                    <p className="phone">
                      {contractors.find((c) => c.userIdentifier === selectedContractorId)?.phone}
                    </p>
                  )}
                </>
              )}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose} disabled={isSaving}>
            Cancel
          </button>
          <button className="btn-primary" onClick={handleSave} disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  );
}
