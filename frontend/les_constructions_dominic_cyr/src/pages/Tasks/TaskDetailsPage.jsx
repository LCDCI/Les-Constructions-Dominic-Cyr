import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { taskApi } from '../../features/schedules/api/taskApi';
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchAllContractors } from '../../features/users/api/usersApi';
import { useBackendUser } from '../../hooks/useBackendUser';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { ROLES } from '../../utils/permissions';
import EditTaskModal from '../../components/Modals/EditTaskModal';
import ConfirmationModal from '../../components/Modals/ConfirmationModal';
import '../../styles/Project/ProjectSchedule.css';

const TASK_STATUSES = [
  'TO_DO',
  'IN_PROGRESS',
  'COMPLETED',
  'ON_HOLD',
  'CANCELLED',
];

const TaskDetailsPage = () => {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();
  const { t } = usePageTranslations('taskDetails');
  const stateTask = location.state?.task;

  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditTaskOpen, setIsEditTaskOpen] = useState(false);
  const [taskDraft, setTaskDraft] = useState(null);
  const [editError, setEditError] = useState('');
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [projectContractors, setProjectContractors] = useState([]);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => {
    const loadTask = async () => {
      try {
        if (stateTask) {
          setTask(stateTask);
        }

        setLoading(true);
        setError('');

        let token = null;
        if (isAuthenticated) {
          try {
            const { getAuthAudience } = await import('../../utils/authConfig');
            token = await getAccessTokenSilently({
              authorizationParams: { audience: getAuthAudience() },
            });
          } catch (tokenErr) {
            //no error message
          }
        }

        const response = await taskApi.getTaskById(taskId, token);
        const normalized = Array.isArray(response) ? response[0] : response;
        setTask(normalized);

        // Fetch project contractors if we have a projectId
        const projectId =
          normalized.projectId ||
          normalized.projectIdentifier ||
          location.state?.projectId;
        if (projectId) {
          try {
            const projectData = await projectApi.getProjectById(
              projectId,
              token
            );
            const contractorIds = projectData.contractorIds || [];
            //no error message

            if (contractorIds.length > 0) {
              const allContractors = await fetchAllContractors(token);
              const projectContractorsList = allContractors.filter(contractor =>
                contractorIds.includes(
                  contractor.userId || contractor.userIdentifier
                )
              );
              setProjectContractors(projectContractorsList);
            } else {
              setProjectContractors([]);
            }
          } catch (projectErr) {
            setProjectContractors([]);
          }
        }
      } catch (err) {
        const status = err?.response?.status;

        if (status === 403 && stateTask) {
          setError('');
          setTask(prev => prev || stateTask);
        } else {
          setError(
            err?.response?.data?.message ||
              err.message ||
              t('errors.loadFailed', 'Failed to load task details.')
          );
        }
      } finally {
        setLoading(false);
      }
    };

    if (taskId) {
      loadTask();
    }
  }, [
    taskId,
    getAccessTokenSilently,
    location.state?.projectId,
    isAuthenticated,
    stateTask,
    t,
  ]);

  if (loading) {
    return (
      <div className="schedule-loading">
        <div className="spinner"></div>
        <p>{t('loading', 'Loading task...')}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-error">
        <h2>{t('error', 'Error')}</h2>
        <p>{error}</p>
        <button className="back-button" onClick={() => navigate(-1)}>
          {t('goBack', 'Go Back')}
        </button>
      </div>
    );
  }

  if (!task) return null;

  const title =
    task.taskTitle || task.taskDescription || `${t('task', 'Task')} ${taskId}`;
  const status = task.taskStatus || 'notSet';

  const toDateOnly = value => {
    if (!value) return null;
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime())
      ? String(value)
      : parsed.toISOString().slice(0, 10);
  };

  const startOnly = toDateOnly(task.periodStart);
  const endOnly = toDateOnly(task.periodEnd);
  const dateMissing = !startOnly && !endOnly;

  const scheduleWindow = {
    start: startOnly || task.scheduleStartDate || undefined,
    end: endOnly || task.scheduleEndDate || undefined,
  };

  const canEditTask =
    isAuthenticated &&
    ((role && (role === ROLES.OWNER || role === ROLES.CONTRACTOR)) ||
      (!role && !roleLoading));

  const getContractorIdentifier = c => c?.userId || c?.userIdentifier;
  const assigneeIdentifier =
    task?.assignedToUserId ?? task?.assigneeId ?? task?.assignedTo ?? null;
  const assigneeObj = projectContractors.find(
    c => getContractorIdentifier(c) === assigneeIdentifier
  );
  const assigneeName = assigneeObj
    ? `${assigneeObj.firstName} ${assigneeObj.lastName}`
    : assigneeIdentifier || t('unassigned', 'Unassigned');

  const openEditTaskModal = () => {
    setTaskDraft({
      taskTitle: task.taskTitle || '',
      taskStatus: task.taskStatus || TASK_STATUSES[0],
      periodStart: startOnly || '',
      periodEnd: endOnly || startOnly || '',
      assignedToUserId: task.assignedToUserId || '',
      taskDescription: task.taskDescription || '',
    });
    setEditError('');
    setIsEditTaskOpen(true);
  };

  const handleEditTaskSave = async () => {
    if (!taskDraft) return;
    setEditError('');

    if (!taskDraft.taskTitle?.trim() && !taskDraft.taskDescription?.trim()) {
      setEditError(
        t(
          'errors.titleOrDescriptionRequired',
          'Please provide a title or description.'
        )
      );
      return;
    }

    setIsSavingEdit(true);

    try {
      let token = null;
      if (isAuthenticated) {
        const { getAuthAudience } = await import('../../utils/authConfig');
        token = await getAccessTokenSilently({
          authorizationParams: { audience: getAuthAudience() },
        });
      }

      const payload = {
        ...taskDraft,
        taskPriority: task.taskPriority || 'MEDIUM',
        scheduleId: task.scheduleId || task.scheduleIdentifier || null,
      };

      const updated = await taskApi.updateTask(taskId, payload, token);
      const normalized = Array.isArray(updated) ? updated[0] : updated;

      setTask(normalized);
      setTaskDraft(null);
      setIsEditTaskOpen(false);
    } catch (err) {
      const status = err?.response?.status;
      let msg =
        err?.response?.data?.message ||
        err.message ||
        t('errors.updateFailed', 'Failed to update task.');

      if (status === 403) {
        msg = t(
          'errors.updateForbidden',
          'You do not have permission to update this task. Please ensure you are the assigned supplier or an owner.'
        );
      }

      setEditError(msg);
    } finally {
      setIsSavingEdit(false);
    }
  };

  const handleDeleteTask = () => {
    setDeleteError('');
    setIsDeleteModalOpen(true);
  };

  const confirmDeleteTask = async () => {
    setIsDeleting(true);
    setDeleteError('');

    try {
      let token = null;
      if (isAuthenticated) {
        const { getAuthAudience } = await import('../../utils/authConfig');
        token = await getAccessTokenSilently({
          authorizationParams: { audience: getAuthAudience() },
        });
      }

      await taskApi.deleteTask(taskId, token);

      // Navigate back to the previous page or project schedule
      const projectId =
        task.projectId || task.projectIdentifier || location.state?.projectId;
      if (projectId) {
        navigate(`/projects/${projectId}/schedule`, {
          state: { message: 'Task deleted successfully' },
        });
      } else {
        navigate(-1);
      }
    } catch (err) {
      const status = err?.response?.status;
      let msg =
        err?.response?.data?.message ||
        err.message ||
        t('errors.deleteFailed', 'Failed to delete task.');

      if (status === 403) {
        msg = t(
          'errors.deleteForbidden',
          'You do not have permission to delete this task. Only owners can delete tasks.'
        );
      }

      setDeleteError(msg);
      setIsDeleting(false);
    }
  };

  const cancelDeleteTask = () => {
    setIsDeleteModalOpen(false);
    setDeleteError('');
  };

  const goBack = () => navigate(-1);

  return (
    <div className="project-schedule-page task-details-page">
      <div className="schedule-header">
        <div>
          <div className="task-eyebrow">{t('task', 'Task')}</div>
          <h1>{title}</h1>
          <div className="task-subtle">ID: {taskId}</div>
        </div>
        <div className="task-header-actions">
          {role === ROLES.OWNER && (
            <button className="modal-danger" onClick={handleDeleteTask}>
              {t('deleteTask', 'Delete Task')}
            </button>
          )}
          {canEditTask && (
            <button className="primary" onClick={openEditTaskModal}>
              {t('editTask', 'Edit Task')}
            </button>
          )}
          <button className="back-button-small" onClick={goBack}>
            ← {t('back', 'Back')}
          </button>
        </div>
      </div>

      <div className="task-sections">
        <div className="task-card task-card-row">
          <div className="task-meta-block">
            <span className="task-label">
              {t('scheduleWindow', 'Schedule window')}
            </span>
            {dateMissing ? (
              <div className="task-value">
                {t('noDates', 'No dates provided')}
              </div>
            ) : (
              <div className="task-value task-value-stacked">
                <span>
                  {t('start', 'Start')}: {startOnly || '—'}
                </span>
                <span>
                  {t('end', 'End')}: {endOnly || '—'}
                </span>
              </div>
            )}
          </div>
          <div className="task-meta-block">
            <span className="task-label">{t('assignee', 'Assignee')}</span>
            <div className="task-value">{assigneeName}</div>
          </div>
          <div className="task-meta-block">
            <span className="task-label">{t('status', 'Status')}</span>
            <div className="task-value">
              {t(`statusValues.${status}`, status)}
            </div>
          </div>
        </div>

        <div className="task-card">
          <h3>{t('description', 'Description')}</h3>
          <p className="task-body-text">
            {task.taskDescription ||
              t('noDescription', 'No description provided.')}
          </p>
        </div>
      </div>

      <ConfirmationModal
        isOpen={isDeleteModalOpen}
        onCancel={cancelDeleteTask}
        config={{
          title: t('deleteModal.title'),
          message: deleteError
            ? `${t('errors.errorPrefix', 'Error')}: ${deleteError}`
            : t('deleteModal.message'),
          onConfirm: confirmDeleteTask,
          confirmText: isDeleting
            ? t('deleteModal.confirmTextDeleting')
            : t('deleteModal.confirmText'),
          cancelText: t('deleteModal.cancelText'),
          isDestructive: true,
        }}
      />

      <EditTaskModal
        isOpen={isEditTaskOpen}
        task={taskDraft}
        statuses={TASK_STATUSES}
        contractors={projectContractors}
        errorMessage={editError}
        isSaving={isSavingEdit}
        onClose={() => setIsEditTaskOpen(false)}
        onChange={setTaskDraft}
        onSave={handleEditTaskSave}
        scheduleWindow={scheduleWindow}
      />
    </div>
  );
};

export default TaskDetailsPage;
