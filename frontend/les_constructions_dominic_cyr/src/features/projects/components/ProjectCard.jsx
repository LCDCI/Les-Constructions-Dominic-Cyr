import React from 'react';
import { projectApi } from '../api/projectApi';

const ProjectCard = ({ project, onEdit, canEdit }) => {
  const imageUrl = project.imageIdentifier
    ? projectApi.getImageUrl(project.imageIdentifier)
    : '/placeholder-project. png';

  const getStatusColor = status => {
    switch (status) {
      case 'PLANNED':
        return '#9CA3AF';
      case 'IN_PROGRESS':
        return '#4D85B9';
      case 'DELAYED':
        return '#EF4444';
      case 'COMPLETED':
        return '#10B981';
      case 'CANCELLED':
        return '#6B7280';
      default:
        return '#4D85B9';
    }
  };

  const getStatusLabel = status => {
    switch (status) {
      case 'PLANNED':
        return 'Planned';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'DELAYED':
        return 'Delayed';
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return status;
    }
  };

  return (
    <div
      style={{
        backgroundColor: '#E5E7EB',
        borderRadius: '8px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
      }}
    >
      <div
        style={{
          display: 'flex',
          gap: '20px',
          alignItems: 'flex-start',
        }}
      >
        <div
          style={{
            width: '120px',
            height: '120px',
            borderRadius: '8px',
            overflow: 'hidden',
            flexShrink: 0,
            backgroundColor: project.primaryColor || '#D1D5DB',
          }}
        >
          <img
            src={imageUrl}
            alt={project.projectName}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
            onError={e => {
              e.target.style.display = 'none';
            }}
          />
        </div>

        <div style={{ flex: 1 }}>
          <h3
            style={{
              fontSize: '20px',
              fontWeight: '600',
              color: '#1F2937',
              marginBottom: '8px',
            }}
          >
            {project.projectName}
          </h3>

          <p
            style={{
              fontSize: '14px',
              color: '#6B7280',
              marginBottom: '12px',
              lineHeight: '1. 5',
            }}
          >
            {project.projectDescription}
          </p>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <span
              style={{
                padding: '4px 12px',
                borderRadius: '12px',
                backgroundColor: getStatusColor(project.status),
                color: 'white',
                fontSize: '12px',
                fontWeight: '500',
              }}
            >
              {getStatusLabel(project.status)}
            </span>

            {project.progressPercentage !== null && (
              <span
                style={{
                  padding: '4px 12px',
                  borderRadius: '12px',
                  backgroundColor: '#D1D5DB',
                  color: '#1F2937',
                  fontSize: '12px',
                  fontWeight: '500',
                }}
              >
                {project.progressPercentage}% Complete
              </span>
            )}
          </div>
        </div>
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          paddingTop: '12px',
          borderTop: '1px solid #D1D5DB',
        }}
      >
        <button
          onClick={() =>
            (window.location.href = `/projects/${project.projectIdentifier}`)
          }
          style={{
            padding: '8px 20px',
            backgroundColor: '#4D85B9',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '500',
          }}
        >
          View this project
        </button>

        {canEdit && (
          <button
            onClick={() => onEdit(project)}
            style={{
              padding: '8px 20px',
              backgroundColor: 'white',
              color: '#4D85B9',
              border: '2px solid #4D85B9',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '500',
            }}
          >
            Edit
          </button>
        )}
      </div>
    </div>
  );
};

export default ProjectCard;
