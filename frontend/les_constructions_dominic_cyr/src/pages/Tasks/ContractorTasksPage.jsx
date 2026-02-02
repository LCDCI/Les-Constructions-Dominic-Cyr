/* eslint-disable no-console */
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { taskApi } from '../../features/schedules/api/taskApi';
import { projectApi } from '../../features/projects/api/projectApi';
import { useBackendUser } from '../../hooks/useBackendUser';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Tasks/ContractorTasksPage.css';

const STATUS_LABELS = {
  TO_DO: 'To Do',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  ON_HOLD: 'On Hold',
  CANCELLED: 'Cancelled',
};

const PRIORITY_LABELS = {
  VERY_LOW: 'Very Low',
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  VERY_HIGH: 'Very High',
};

const ContractorTasksPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, getAccessTokenSilently } = useAuth0();
  const { userId } = useBackendUser();
  // eslint-disable-next-line no-unused-vars
  const { t, isLoading: translationsLoading } =
    usePageTranslations('contractorTasks');

  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [selectedProject, setSelectedProject] = useState('all');
  const [selectedLot, setSelectedLot] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [priorityFilter, setPriorityFilter] = useState('all');

  useEffect(() => {
    const fetchData = async () => {
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
            console.error('Error getting token:', tokenErr);
          }
        }

        // Fetch all tasks with project/lot info
        const tasksData = await taskApi.getAllTasksForContractorView(token);
        setTasks(tasksData || []);

        // Fetch projects for filter dropdown
        try {
          const projectsData = await projectApi.getAllProjects(token);
          setProjects(projectsData || []);
        } catch (projErr) {
          console.error('Error fetching projects:', projErr);
          setProjects([]);
        }
      } catch (err) {
        console.error('Error fetching tasks:', err);
        setError('Failed to load tasks. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated, getAccessTokenSilently]);

  // Get unique lots for the selected project (only from tasks assigned to current user)
  const availableLots = useMemo(() => {
    // Filter tasks assigned to current user first
    const myTasks = userId
      ? tasks.filter(task => task.assignedToUserId === userId)
      : tasks;

    if (selectedProject === 'all') {
      const lotSet = new Set();
      myTasks.forEach(task => {
        if (task.lotId) {
          lotSet.add(task.lotId);
        }
      });
      return Array.from(lotSet).sort();
    }

    const projectTasks = myTasks.filter(
      task => task.projectIdentifier === selectedProject
    );
    const lotSet = new Set();
    projectTasks.forEach(task => {
      if (task.lotId) {
        lotSet.add(task.lotId);
      }
    });
    return Array.from(lotSet).sort();
  }, [tasks, selectedProject, userId]);

  // Filter tasks based on selections
  const filteredTasks = useMemo(() => {
    let result = [...tasks];

    // Filter by assigned to current user (always applied)
    if (userId) {
      result = result.filter(task => task.assignedToUserId === userId);
    }

    // Filter by project
    if (selectedProject !== 'all') {
      result = result.filter(
        task => task.projectIdentifier === selectedProject
      );
    }

    // Filter by lot
    if (selectedLot !== 'all') {
      result = result.filter(task => task.lotId === selectedLot);
    }

    // Filter by status
    if (statusFilter !== 'all') {
      result = result.filter(task => task.taskStatus === statusFilter);
    }

    // Filter by priority
    if (priorityFilter !== 'all') {
      result = result.filter(task => task.taskPriority === priorityFilter);
    }

    return result;
  }, [
    tasks,
    selectedProject,
    selectedLot,
    statusFilter,
    priorityFilter,
    userId,
  ]);

  // Group filtered tasks by project and lot
  const groupedTasks = useMemo(() => {
    const groups = {};

    filteredTasks.forEach(task => {
      const projectKey = task.projectIdentifier || 'Unknown Project';
      const lotKey = task.lotId || 'Unknown Lot';

      if (!groups[projectKey]) {
        groups[projectKey] = {
          projectName: task.projectName || projectKey,
          lots: {},
        };
      }

      if (!groups[projectKey].lots[lotKey]) {
        groups[projectKey].lots[lotKey] = {
          lotNumber: task.lotNumber || lotKey,
          tasks: [],
        };
      }

      groups[projectKey].lots[lotKey].tasks.push(task);
    });

    // Sort tasks within each lot by date
    Object.values(groups).forEach(project => {
      Object.values(project.lots).forEach(lot => {
        lot.tasks.sort((a, b) => {
          const dateA = a.periodStart ? new Date(a.periodStart) : new Date(0);
          const dateB = b.periodStart ? new Date(b.periodStart) : new Date(0);
          return dateA - dateB;
        });
      });
    });

    return groups;
  }, [filteredTasks]);

  const handleTaskClick = task => {
    navigate(`/tasks/${task.taskId}`, {
      state: {
        task,
        projectId: task.projectIdentifier,
      },
    });
  };

  const formatDate = dateString => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getStatusClass = status => {
    const statusMap = {
      TO_DO: 'status-todo',
      IN_PROGRESS: 'status-in-progress',
      COMPLETED: 'status-completed',
      ON_HOLD: 'status-on-hold',
      CANCELLED: 'status-cancelled',
    };
    return statusMap[status] || '';
  };

  const getPriorityClass = priority => {
    const priorityMap = {
      VERY_LOW: 'priority-very-low',
      LOW: 'priority-low',
      MEDIUM: 'priority-medium',
      HIGH: 'priority-high',
      VERY_HIGH: 'priority-very-high',
    };
    return priorityMap[priority] || '';
  };

  // Reset lot filter when project changes
  useEffect(() => {
    setSelectedLot('all');
  }, [selectedProject]);

  if (loading) {
    return (
      <div className="contractor-tasks-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{t('loading', 'Loading tasks...')}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="contractor-tasks-page">
        <div className="error-container">
          <p className="error-message">{error}</p>
          <button onClick={() => window.location.reload()}>
            {t('retry', 'Retry')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="contractor-tasks-page">
      <div className="page-header">
        <h1>{t('title', 'My Assigned Tasks')}</h1>
        <p className="subtitle">
          {filteredTasks.length}{' '}
          {t('tasksFound', { count: filteredTasks.length })}
        </p>
      </div>

      <div className="filters-container">
        <div className="filter-group">
          <label htmlFor="project-filter">
            {t('filters.project', 'Project')}:
          </label>
          <select
            id="project-filter"
            value={selectedProject}
            onChange={e => setSelectedProject(e.target.value)}
          >
            <option value="all">
              {t('filters.allProjects', 'All Projects')}
            </option>
            {projects.map(project => (
              <option
                key={project.projectIdentifier}
                value={project.projectIdentifier}
              >
                {project.projectName}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="lot-filter">{t('filters.lot', 'Lot')}:</label>
          <select
            id="lot-filter"
            value={selectedLot}
            onChange={e => setSelectedLot(e.target.value)}
            disabled={availableLots.length === 0}
          >
            <option value="all">{t('filters.allLots', 'All Lots')}</option>
            {availableLots.map(lot => (
              <option key={lot} value={lot}>
                {t('filters.lotNumber', { number: lot })}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="status-filter">
            {t('filters.status', 'Status')}:
          </label>
          <select
            id="status-filter"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
          >
            <option value="all">
              {t('filters.allStatuses', 'All Statuses')}
            </option>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <option key={key} value={key}>
                {t(`status.${key}`, label)}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="priority-filter">
            {t('filters.priority', 'Priority')}:
          </label>
          <select
            id="priority-filter"
            value={priorityFilter}
            onChange={e => setPriorityFilter(e.target.value)}
          >
            <option value="all">
              {t('filters.allPriorities', 'All Priorities')}
            </option>
            {Object.entries(PRIORITY_LABELS).map(([key, label]) => (
              <option key={key} value={key}>
                {t(`priority.${key}`, label)}
              </option>
            ))}
          </select>
        </div>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="no-tasks-message">
          <p>{t('noTasks', 'No tasks found matching your filters.')}</p>
        </div>
      ) : (
        <div className="tasks-grouped-container">
          {Object.entries(groupedTasks).map(([projectId, projectData]) => (
            <div key={projectId} className="project-group">
              <div className="project-header">
                <h2>{projectData.projectName}</h2>
              </div>

              {Object.entries(projectData.lots).map(([lotId, lotData]) => (
                <div key={lotId} className="lot-group">
                  <div className="lot-header">
                    <h3>
                      {t('filters.lotNumber', {
                        number: lotData.lotNumber,
                      })}
                    </h3>
                    <span className="task-count-badge">
                      {lotData.tasks.length}{' '}
                      {t('tasksCount', {
                        count: lotData.tasks.length,
                      })}
                    </span>
                  </div>

                  <div className="tasks-table-container">
                    <table className="tasks-table">
                      <thead>
                        <tr>
                          <th>{t('table.title', 'Title')}</th>
                          <th>{t('table.status', 'Status')}</th>
                          <th>{t('table.priority', 'Priority')}</th>
                          <th>{t('table.startDate', 'Start Date')}</th>
                          <th>{t('table.endDate', 'End Date')}</th>
                          <th>{t('table.assignedTo', 'Assigned To')}</th>
                          <th>{t('table.progress', 'Progress')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {lotData.tasks.map(task => (
                          <tr
                            key={task.taskId}
                            onClick={() => handleTaskClick(task)}
                            className="task-row clickable"
                          >
                            <td className="task-title">{task.taskTitle}</td>
                            <td>
                              <span
                                className={`status-badge ${getStatusClass(
                                  task.taskStatus
                                )}`}
                              >
                                {t(
                                  `status.${task.taskStatus}`,
                                  STATUS_LABELS[task.taskStatus]
                                )}
                              </span>
                            </td>
                            <td>
                              <span
                                className={`priority-badge ${getPriorityClass(
                                  task.taskPriority
                                )}`}
                              >
                                {t(
                                  `priority.${task.taskPriority}`,
                                  PRIORITY_LABELS[task.taskPriority]
                                )}
                              </span>
                            </td>
                            <td>{formatDate(task.periodStart)}</td>
                            <td>{formatDate(task.periodEnd)}</td>
                            <td>{task.assignedToUserName || '—'}</td>
                            <td>
                              <div className="progress-cell">
                                <div className="progress-bar-container">
                                  <div
                                    className="progress-bar-fill"
                                    style={{
                                      width: `${task.taskProgress || 0}%`,
                                    }}
                                  ></div>
                                </div>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ContractorTasksPage;
