import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { useAuth0 } from '@auth0/auth0-react';
import { projectApi } from '../api/projectApi';
import '../../../styles/Project/edit-project.css';

const EditProjectForm = ({ project, onCancel, onSuccess, onError }) => {
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState({
    projectName: '',
    projectDescription: '',
    status: 'PLANNED',
    startDate: '',
    endDate: '',
    completionDate: '',
    location: '',
    primaryColor: '#4A90A4',
    tertiaryColor: '#33FF57',
    buyerColor: '#3357FF',
    buyerName: '',
    progressPercentage: 0,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Initialize form with project data
  useEffect(() => {
    if (project) {
      setFormData({
        projectName: project.projectName || '',
        projectDescription: project.projectDescription || '',
        status: project.status || 'PLANNED',
        startDate: project.startDate || '',
        endDate: project.endDate || '',
        completionDate: project.completionDate || '',
        location: project.location || '',
        primaryColor: project.primaryColor || '#4A90A4',
        tertiaryColor: project.tertiaryColor || '#33FF57',
        buyerColor: project.buyerColor || '#3357FF',
        buyerName: project.buyerName || '',
        progressPercentage: project.progressPercentage || 0,
      });
    }
  }, [project]);

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: null }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.projectName || formData.projectName.trim() === '') {
      newErrors.projectName = 'Project name is required';
    }

    if (!formData.startDate) {
      newErrors.startDate = 'Start date is required';
    }

    if (formData.startDate && formData.endDate) {
      const start = new Date(formData.startDate);
      const end = new Date(formData.endDate);
      if (start > end) {
        newErrors.endDate = 'End date must be after start date';
      }
    }

    if (formData.location && formData.location.length > 255) {
      newErrors.location = 'Location cannot exceed 255 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      onError?.('Please fix the validation errors');
      return;
    }

    setIsSubmitting(true);

    try {
      // Get auth token
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: import.meta.env.VITE_AUTH0_AUDIENCE || 'https://construction-api.loca',
        },
      });

      // Prepare update data (only send changed fields)
      const updateData = {
        projectName: formData.projectName,
        projectDescription: formData.projectDescription,
        status: formData.status,
        startDate: formData.startDate,
        endDate: formData.endDate || null,
        completionDate: formData.completionDate || null,
        location: formData.location || null,
        primaryColor: formData.primaryColor,
        tertiaryColor: formData.tertiaryColor,
        buyerColor: formData.buyerColor,
        buyerName: formData.buyerName || null,
        progressPercentage: parseInt(formData.progressPercentage) || 0,
      };

      await projectApi.updateProject(project.projectIdentifier, updateData, token);
      onSuccess?.(project.projectIdentifier);
    } catch (error) {
      console.error('Failed to update project:', error);
      onError?.(error.message || 'Failed to update project');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="edit-project-form" onSubmit={handleSubmit}>
      <h2>Edit Project Details</h2>

      {/* Basic Information */}
      <div className="form-section">
        <h3>Basic Information</h3>

        <div className="form-group">
          <label htmlFor="projectName">
            Project Name <span className="required">*</span>
          </label>
          <input
            type="text"
            id="projectName"
            value={formData.projectName}
            onChange={(e) => handleInputChange('projectName', e.target.value)}
            className={errors.projectName ? 'error' : ''}
            disabled={isSubmitting}
          />
          {errors.projectName && (
            <span className="error-message">{errors.projectName}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="projectDescription">Project Description</label>
          <textarea
            id="projectDescription"
            value={formData.projectDescription}
            onChange={(e) => handleInputChange('projectDescription', e.target.value)}
            rows={4}
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label htmlFor="location">
            Location
          </label>
          <input
            type="text"
            id="location"
            value={formData.location}
            onChange={(e) => handleInputChange('location', e.target.value)}
            placeholder="Enter project location"
            className={errors.location ? 'error' : ''}
            disabled={isSubmitting}
          />
          {errors.location && (
            <span className="error-message">{errors.location}</span>
          )}
        </div>
      </div>

      {/* Status & Dates */}
      <div className="form-section">
        <h3>Status & Dates</h3>

        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={formData.status}
            onChange={(e) => handleInputChange('status', e.target.value)}
            disabled={isSubmitting}
          >
            <option value="PLANNED">Planned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DELAYED">Delayed</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="startDate">
              Start Date <span className="required">*</span>
            </label>
            <input
              type="date"
              id="startDate"
              value={formData.startDate}
              onChange={(e) => handleInputChange('startDate', e.target.value)}
              className={errors.startDate ? 'error' : ''}
              disabled={isSubmitting}
            />
            {errors.startDate && (
              <span className="error-message">{errors.startDate}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="endDate">End Date</label>
            <input
              type="date"
              id="endDate"
              value={formData.endDate}
              onChange={(e) => handleInputChange('endDate', e.target.value)}
              min={formData.startDate}
              className={errors.endDate ? 'error' : ''}
              disabled={isSubmitting}
            />
            {errors.endDate && (
              <span className="error-message">{errors.endDate}</span>
            )}
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="completionDate">Completion Date</label>
          <input
            type="date"
            id="completionDate"
            value={formData.completionDate}
            onChange={(e) => handleInputChange('completionDate', e.target.value)}
            min={formData.startDate}
            disabled={isSubmitting}
          />
        </div>
      </div>

      {/* Progress */}
      <div className="form-section">
        <h3>Progress</h3>

        <div className="form-group">
          <label htmlFor="progressPercentage">
            Progress Percentage (0-100)
          </label>
          <input
            type="number"
            id="progressPercentage"
            value={formData.progressPercentage}
            onChange={(e) => handleInputChange('progressPercentage', Math.min(100, Math.max(0, parseInt(e.target.value) || 0)))}
            min="0"
            max="100"
            disabled={isSubmitting}
          />
        </div>
      </div>

      {/* Colors */}
      <div className="form-section">
        <h3>Colors</h3>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="primaryColor">Primary Color</label>
            <input
              type="color"
              id="primaryColor"
              value={formData.primaryColor}
              onChange={(e) => handleInputChange('primaryColor', e.target.value)}
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="tertiaryColor">Tertiary Color</label>
            <input
              type="color"
              id="tertiaryColor"
              value={formData.tertiaryColor}
              onChange={(e) => handleInputChange('tertiaryColor', e.target.value)}
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="buyerColor">Buyer Color</label>
            <input
              type="color"
              id="buyerColor"
              value={formData.buyerColor}
              onChange={(e) => handleInputChange('buyerColor', e.target.value)}
              disabled={isSubmitting}
            />
          </div>
        </div>
      </div>

      {/* Buyer Information */}
      <div className="form-section">
        <h3>Buyer Information</h3>

        <div className="form-group">
          <label htmlFor="buyerName">Buyer Name</label>
          <input
            type="text"
            id="buyerName"
            value={formData.buyerName}
            onChange={(e) => handleInputChange('buyerName', e.target.value)}
            placeholder="Enter buyer name (optional)"
            disabled={isSubmitting}
          />
        </div>
      </div>

      {/* Form Actions */}
      <div className="form-actions">
        <button
          type="button"
          className="btn-cancel"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="btn-submit"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
};

EditProjectForm.propTypes = {
  project: PropTypes.object.isRequired,
  onCancel: PropTypes.func.isRequired,
  onSuccess: PropTypes.func.isRequired,
  onError: PropTypes.func,
};

export default EditProjectForm;
