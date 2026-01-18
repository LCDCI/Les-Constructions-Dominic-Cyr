import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { taskApi } from '../../features/schedules/api/taskApi';
import '../../styles/Project/ProjectSchedule.css';

const TaskDetailsPage = () => {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();

  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadTask = async () => {
      try {
        setLoading(true);
        setError('');

        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {
            console.warn(
              'Could not get token for task fetch, proceeding without auth'
            );
          }
        }

        const response = await taskApi.getTaskById(taskId, token);
        const normalized = Array.isArray(response) ? response[0] : response;
        setTask(normalized);
      } catch (err) {
        console.error('Error loading task:', err);
        const respMessage = err?.response?.data?.message;
        const respError = err?.response?.data?.error;
        setError(
          respMessage ||
            respError ||
            err.message ||
            'Failed to load task details.'
        );
      } finally {
        setLoading(false);
      }
    };

    if (taskId) {
      loadTask();
    }
  }, [taskId, getAccessTokenSilently, isAuthenticated]);

  if (loading) {
    return (
      <div className="schedule-loading">
        <div className="spinner"></div>
        <p>Loading task...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-error">
        <h2>Error</h2>
        <p>{error}</p>
        <button className="back-button" onClick={() => navigate(-1)}>
          Go Back
        </button>
      </div>
    );
  }

  if (!task) {
    return null;
  }

  const title = task.taskTitle || task.taskDescription || `Task ${taskId}`;
  const status = task.taskStatus || 'Not set';
  const priority = task.taskPriority || 'Not set';

  const toDateOnly = value => {
    if (!value) return null;
    if (typeof value === 'string' && value.length >= 10) {
      return value.slice(0, 10);
    }
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime())
      ? String(value)
      : parsed.toISOString().slice(0, 10);
  };

  const startOnly = toDateOnly(task.periodStart);
  const endOnly = toDateOnly(task.periodEnd);
  const dateMissing = !startOnly && !endOnly;

  const numericOrDash = value => (value === 0 || value ? value : '—');

  const goBack = () => {
    const fromSchedule = location.state?.fromScheduleModal;
    const returnTo = location.state?.returnTo;
    const scheduleEventId = location.state?.scheduleEventId;

    if (fromSchedule && returnTo && scheduleEventId) {
      navigate(returnTo, {
        state: {
          reopenScheduleModal: true,
          scheduleEventId,
        },
      });
      return;
    }

    navigate(-1);
  };

  return (
    <div className="project-schedule-page task-details-page">
      <div className="schedule-header">
        <div>
          <div className="task-eyebrow">Task</div>
          <h1>{title}</h1>
          <div className="task-subtle">ID: {taskId}</div>
        </div>
        <div className="task-header-actions">
          <button className="back-button-small" onClick={goBack}>
            ← Back
          </button>
        </div>
      </div>

      <div className="task-sections">
        <div className="task-card task-card-row">
          <div className="task-meta-block">
            <span className="task-label">Schedule window</span>
            {dateMissing ? (
              <div className="task-value">No dates provided</div>
            ) : (
              <div className="task-value task-value-stacked">
                <span>Start: {startOnly || '—'}</span>
                <span>End: {endOnly || '—'}</span>
              </div>
            )}
          </div>
          <div className="task-meta-block">
            <span className="task-label">Assignee</span>
            <div className="task-value">
              {task.assignedToUserId || 'Unassigned'}
            </div>
          </div>
          <div className="task-meta-block">
            <span className="task-label">Task Status</span>
            <div className="task-value">{status}</div>
          </div>
          <div className="task-meta-block">
            <span className="task-label">Task Priority</span>
            <div className="task-value">{priority}</div>
          </div>
        </div>

        <div className="task-card">
          <h3>Description</h3>
          <p className="task-body-text">
            {task.taskDescription || 'No description provided.'}
          </p>
        </div>

        <div className="task-card task-stats-grid">
          <h3>Time + effort</h3>
          <div className="task-stats">
            <div className="task-stat">
              <span className="task-label">Estimated hours</span>
              <div className="task-value">
                {numericOrDash(task.estimatedHours)}
              </div>
            </div>
            <div className="task-stat">
              <span className="task-label">Hours spent</span>
              <div className="task-value">{numericOrDash(task.hoursSpent)}</div>
            </div>
            <div className="task-stat">
              <span className="task-label">Progress</span>
              <div className="task-value">
                {task.taskProgress === 0 || task.taskProgress
                  ? `${task.taskProgress}%`
                  : '—'}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaskDetailsPage;
