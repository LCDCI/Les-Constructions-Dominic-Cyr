import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { taskApi } from '../../features/schedules/api/taskApi';
import { projectApi } from '../../features/projects/api/projectApi';
import { useBackendUser } from '../../hooks/useBackendUser';
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

  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [selectedProject, setSelectedProject] = useState('all');
  const [selectedLot, setSelectedLot] = useState('all');
  const [showMyTasksOnly, setShowMyTasksOnly] = useState(false);
  const [statusFilter, setStatusFilter] = useState('all');

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

  // Get unique lots for the selected project
  const availableLots = useMemo(() => {
    if (selectedProject === 'all') {
      const lotSet = new Set();
      tasks.forEach(task => {
        if (task.lotId) {
          lotSet.add(task.lotId);
        }
      });
      return Array.from(lotSet).sort();
    }

    const projectTasks = tasks.filter(
      task => task.projectIdentifier === selectedProject
    );
    const lotSet = new Set();
    projectTasks.forEach(task => {
      if (task.lotId) {
        lotSet.add(task.lotId);
      }
    });
    return Array.from(lotSet).sort();
  }, [tasks, selectedProject]);

  // Filter tasks based on selections
  const filteredTasks = useMemo(() => {
    let result = [...tasks];

    // Filter by project
    if (selectedProject !== 'all') {
      result = result.filter(task => task.projectIdentifier === selectedProject);
    }

    // Filter by lot
    if (selectedLot !== 'all') {
      result = result.filter(task => task.lotId === selectedLot);
    }

    // Filter by assigned to me
    if (showMyTasksOnly && userId) {
      result = result.filter(task => task.assignedToUserId === userId);
    }

    // Filter by status
    if (statusFilter !== 'all') {
      result = result.filter(task => task.taskStatus === statusFilter);
    }

    return result;
  }, [tasks, selectedProject, selectedLot, showMyTasksOnly, userId, statusFilter]);

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

  const handleTaskClick = (task) => {
    navigate(`/tasks/${task.taskId}`, {
      state: {
        task,
        projectId: task.projectIdentifier,
      },
    });
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getStatusClass = (status) => {
    const statusMap = {
      TO_DO: 'status-todo',
      IN_PROGRESS: 'status-in-progress',
      COMPLETED: 'status-completed',
      ON_HOLD: 'status-on-hold',
      CANCELLED: 'status-cancelled',
    };
    return statusMap[status] || '';
  };

  const getPriorityClass = (priority) => {
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
          <p>Loading tasks...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="contractor-tasks-page">
        <div className="error-container">
          <p className="error-message">{error}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="contractor-tasks-page">
      <div className="page-header">
        <h1>All Tasks</h1>
        <p className="subtitle">View all tasks across projects and lots</p>
      </div>

      <div className="filters-container">
        <div className="filter-group">
          <label htmlFor="project-filter">Project:</label>
          <select
            id="project-filter"
            value={selectedProject}
            onChange={(e) => setSelectedProject(e.target.value)}
          >
            <option value="all">All Projects</option>
            {projects.map((project) => (
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
          <label htmlFor="lot-filter">Lot:</label>
          <select
            id="lot-filter"
            value={selectedLot}
            onChange={(e) => setSelectedLot(e.target.value)}
            disabled={availableLots.length === 0}
          >
            <option value="all">All Lots</option>
            {availableLots.map((lot) => (
              <option key={lot} value={lot}>
                Lot {lot}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="status-filter">Status:</label>
          <select
            id="status-filter"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="all">All Statuses</option>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group checkbox-filter">
          <label>
            <input
              type="checkbox"
              checked={showMyTasksOnly}
              onChange={(e) => setShowMyTasksOnly(e.target.checked)}
            />
            My Assigned Tasks Only
          </label>
        </div>
      </div>

      <div className="tasks-summary">
        <span className="task-count">
          {filteredTasks.length} task{filteredTasks.length !== 1 ? 's' : ''} found
        </span>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="no-tasks-message">
          <p>No tasks found matching your filters.</p>
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
                    <h3>Lot {lotData.lotNumber}</h3>
                    <span className="task-count-badge">
                      {lotData.tasks.length} task{lotData.tasks.length !== 1 ? 's' : ''}
                    </span>
                  </div>

                  <div className="tasks-table-container">
                    <table className="tasks-table">
                      <thead>
                        <tr>
                          <th>Title</th>
                          <th>Status</th>
                          <th>Priority</th>
                          <th>Start Date</th>
                          <th>End Date</th>
                          <th>Assigned To</th>
                          <th>Progress</th>
                        </tr>
                      </thead>
                      <tbody>
                        {lotData.tasks.map((task) => (
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
                                {STATUS_LABELS[task.taskStatus] || task.taskStatus}
                              </span>
                            </td>
                            <td>
                              <span
                                className={`priority-badge ${getPriorityClass(
                                  task.taskPriority
                                )}`}
                              >
                                {PRIORITY_LABELS[task.taskPriority] ||
                                  task.taskPriority}
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
                                <span className="progress-text">
                                  {task.taskProgress || 0}%
                                </span>
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

