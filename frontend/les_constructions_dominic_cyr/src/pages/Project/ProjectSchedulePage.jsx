import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { Calendar, dateFnsLocalizer, Views } from 'react-big-calendar';
import {
  format,
  parse,
  startOfWeek,
  getDay,
  format as formatDate,
  parseISO,
  setHours,
  setMinutes,
  isSameDay,
} from 'date-fns';
import enUS from 'date-fns/locale/en-US';

import 'react-big-calendar/lib/css/react-big-calendar.css';
import { projectScheduleApi } from '../../features/schedules/api/projectScheduleApi';
import { taskApi } from '../../features/schedules/api/taskApi';
import { fetchLots } from '../../features/lots/api/lots';
import '../../styles/Project/ProjectSchedule.css';

const TASK_STATUSES = ['TO_DO', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD'];
const TASK_PRIORITIES = ['VERY_LOW', 'LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'];

const ProjectSchedulePage = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [projectName, setProjectName] = useState('');
  const [schedules, setSchedules] = useState([]);
  const [lots, setLots] = useState([]);
  const [lotsLoading, setLotsLoading] = useState(true);
  const [lotsError, setLotsError] = useState('');
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [defaultDate, setDefaultDate] = useState(new Date());
  const [currentDate, setCurrentDate] = useState(new Date());
  const [currentView, setCurrentView] = useState(Views.WEEK);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isSavingTasks, setIsSavingTasks] = useState(false);
  const [formError, setFormError] = useState('');
  const [taskFormError, setTaskFormError] = useState('');

  const buildEmptyTask = (startDate, endDate) => ({
    taskTitle: '',
    taskDescription: '',
    taskStatus: TASK_STATUSES[0],
    taskPriority: TASK_PRIORITIES[2],
    periodStart: startDate ?? format(new Date(), 'yyyy-MM-dd'),
    periodEnd: endDate ?? format(new Date(), 'yyyy-MM-dd'),
    estimatedHours: '',
    hoursSpent: '',
    taskProgress: '',
    assignedToUserId: '',
  });

  const buildEmptyScheduleForm = () => {
    const today = format(new Date(), 'yyyy-MM-dd');
    return {
      scheduleDescription: '',
      lotId: '',
      scheduleStartDate: today,
      scheduleEndDate: today,
    };
  };

  const [newSchedule, setNewSchedule] = useState(buildEmptyScheduleForm);
  const [taskDrafts, setTaskDrafts] = useState([buildEmptyTask()]);
  const [scheduleForTasks, setScheduleForTasks] = useState(null);

  const localizer = useMemo(
    () =>
      dateFnsLocalizer({
        format,
        parse,
        startOfWeek,
        getDay,
        locales: { 'en-US': enUS },
      }),
    []
  );

  const toDate = (dateStr, hour = 8) => {
    if (!dateStr) return new Date();
    const base = parseISO(dateStr);
    const withHour = setMinutes(setHours(base, hour), 0);
    return withHour;
  };

  const parseDateSafe = value => {
    if (!value) return null;
    const parsed = parseISO(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  };

  const extractErrorMessage = err => {
    if (!err) return 'Unknown error';
    const responseMsg = err.response?.data?.message;
    const responseError = err.response?.data?.error;
    const responseErrors = err.response?.data?.errors;
    const statusText = err.response?.statusText;
    const statusCode = err.response?.status;

    if (Array.isArray(responseErrors) && responseErrors.length) {
      return responseErrors.join(', ');
    }

    if (typeof responseMsg === 'string' && responseMsg.trim())
      return responseMsg;
    if (typeof responseError === 'string' && responseError.trim())
      return responseError;
    if (statusCode || statusText) {
      return `Request failed${statusCode ? ` (${statusCode})` : ''}${statusText ? ` ${statusText}` : ''}`.trim();
    }

    return err.message || 'Unknown error';
  };

  const validateScheduleRange = (startStr, endStr) => {
    const start = parseDateSafe(startStr);
    const end = parseDateSafe(endStr);
    if (!start || !end) return 'Start and end dates are required.';
    if (end < start)
      return 'Schedule end date cannot be before the start date.';
    return null;
  };

  const validateTaskWithinSchedule = (
    task,
    scheduleStartStr,
    scheduleEndStr
  ) => {
    const scheduleStart = parseDateSafe(scheduleStartStr);
    const scheduleEnd = parseDateSafe(scheduleEndStr);
    if (!scheduleStart || !scheduleEnd) {
      return 'Schedule dates are missing.';
    }

    const periodStart = parseDateSafe(task.periodStart || scheduleStartStr);
    const periodEnd = parseDateSafe(task.periodEnd || scheduleEndStr);

    if (!periodStart || !periodEnd) {
      return 'Task start and end dates are required.';
    }

    if (periodEnd < periodStart) {
      return 'Task end date cannot be before the task start date.';
    }

    if (periodStart < scheduleStart || periodEnd > scheduleEnd) {
      return 'Task dates must fall within the schedule start and end dates.';
    }

    return null;
  };

  const normalizeSchedule = (schedule, fallbackId) => {
    const tasks = Array.isArray(schedule.scheduleTasks)
      ? schedule.scheduleTasks
      : Array.isArray(schedule.tasks)
        ? schedule.tasks
        : [];

    return {
      ...schedule,
      scheduleId:
        schedule.scheduleId ??
        schedule.scheduleIdentifier ??
        schedule.identifier ??
        schedule.id ??
        fallbackId,
      id:
        schedule.scheduleId ??
        schedule.id ??
        schedule.scheduleIdentifier ??
        schedule.identifier ??
        fallbackId,
      tasks,
    };
  };

  const mapScheduleToEvent = (schedule, fallbackId) => {
    const normalized = normalizeSchedule(schedule, fallbackId);
    return {
      id: normalized.scheduleId ?? normalized.id,
      scheduleIdentifier:
        normalized.scheduleId ??
        normalized.scheduleIdentifier ??
        normalized.identifier ??
        normalized.id,
      title: normalized.scheduleDescription ?? 'Schedule',
      start: toDate(normalized.scheduleStartDate, 8),
      end: toDate(normalized.scheduleEndDate, 17),
      lot: normalized.lotNumber,
      projectName: normalized.projectName,
      tasks: normalized.tasks,
      description: normalized.description ?? normalized.scheduleDescription,
    };
  };

  const getLotId = lot =>
    lot?.lotIdentifier?.lotId || lot?.lotId || (lot?.id ? String(lot.id) : '');

  const formatLotLabel = lot => {
    const lotId = getLotId(lot);
    if (lot?.location) {
      return `${lot.location} (${lotId})`;
    }
    return lotId || 'Unknown lot';
  };

  const getScheduleIdentifier = entity =>
    entity?.scheduleId ??
    entity?.scheduleIdentifier ??
    entity?.identifier ??
    entity?.id ??
    null;

  const findEventForSchedule = scheduleEntity => {
    const targetId = getScheduleIdentifier(scheduleEntity);
    const match = events.find(
      ev =>
        getScheduleIdentifier(ev) === targetId || ev.id === scheduleEntity.id
    );
    if (match) return match;
    return mapScheduleToEvent(scheduleEntity, scheduleEntity.id || Date.now());
  };

  useEffect(() => {
    const loadLots = async () => {
      try {
        setLotsLoading(true);
        setLotsError('');
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
              'Could not get token for lots, proceeding without auth'
            );
          }
        }

        const response = await fetchLots(token);
        setLots(Array.isArray(response) ? response : []);
      } catch (err) {
        console.error('Error fetching lots:', err);
        setLots([]);
        const detailed = extractErrorMessage(err);
        setLotsError(`Unable to load lots. ${detailed}`);
      } finally {
        setLotsLoading(false);
      }
    };

    loadLots();
  }, [getAccessTokenSilently, isAuthenticated]);

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        if (isLoading) return;

        setLoading(true);

        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {
            console.warn('Could not get token, proceeding without auth');
          }
        }

        const scheduleResponse = await projectScheduleApi.getProjectSchedules(
          projectId,
          token
        );

        if (!scheduleResponse || scheduleResponse.length === 0) {
          setSchedules([]);
          setEvents([]);
          setProjectName('Project');
          setDefaultDate(new Date());
          setCurrentDate(new Date());
          setError(null);
          setLoading(false);
          return;
        }

        const scheduleWithIds = scheduleResponse.map((schedule, index) =>
          normalizeSchedule(schedule, index + 1)
        );

        const mappedEvents = scheduleWithIds.map((schedule, index) =>
          mapScheduleToEvent(schedule, index + 1)
        );

        const firstDate = mappedEvents[0]?.start ?? new Date();
        setDefaultDate(firstDate);
        setCurrentDate(firstDate);

        setEvents(mappedEvents);
        setSchedules(scheduleWithIds);
        setProjectName(scheduleWithIds[0]?.projectName || 'Project');
        setError(null);
      } catch (err) {
        console.error('Error fetching schedules:', err);
        setError('Failed to load project schedules. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (projectId) {
      fetchSchedules();
    }
  }, [projectId, isLoading, isAuthenticated, getAccessTokenSilently]);

  const onEventClick = async event => {
    const scheduleIdentifier = getScheduleIdentifier(event);

    // Show the modal immediately with a loading state for tasks
    setSelectedEvent({
      ...event,
      tasks: event.tasks ?? [],
      tasksLoading: Boolean(scheduleIdentifier),
      tasksError: null,
    });
    setIsModalOpen(true);

    if (!scheduleIdentifier) return;

    try {
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

      const attemptFetch = async ident =>
        taskApi.getTasksForSchedule(ident, token);

      let tasks = await attemptFetch(scheduleIdentifier);

      // Fallback: some backends key tasks by numeric id instead of identifier
      if (
        (!Array.isArray(tasks) || tasks.length === 0) &&
        event.id &&
        event.id !== scheduleIdentifier
      ) {
        try {
          const altTasks = await attemptFetch(event.id);
          if (Array.isArray(altTasks) && altTasks.length) {
            tasks = altTasks;
          }
        } catch (altErr) {
          console.warn('Alternate task fetch by id failed:', altErr);
        }
      }

      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier
          ? {
              ...prev,
              tasks,
              tasksLoading: false,
              tasksError: null,
            }
          : prev
      );

      setSchedules(prev =>
        prev.map(item =>
          getScheduleIdentifier(item) === scheduleIdentifier
            ? { ...item, tasks }
            : item
        )
      );

      setEvents(prev =>
        prev.map(ev =>
          getScheduleIdentifier(ev) === scheduleIdentifier
            ? { ...ev, tasks }
            : ev
        )
      );
    } catch (taskErr) {
      console.error('Error fetching tasks for schedule:', taskErr);
      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier
          ? {
              ...prev,
              tasksLoading: false,
              tasksError: 'Failed to load tasks for this schedule.',
            }
          : prev
      );
    }
  };

  const onScheduleCardClick = scheduleItem => {
    const eventLike = findEventForSchedule(scheduleItem);
    onEventClick(eventLike);
  };

  const onSlotSelect = slotInfo => {
    setSelectedEvent(null);
    setIsModalOpen(false);
    setIsCreateModalOpen(true);
    const startValue = format(slotInfo.start, 'yyyy-MM-dd');
    const endValue = format(slotInfo.end ?? slotInfo.start, 'yyyy-MM-dd');
    setNewSchedule(prev => ({
      ...prev,
      scheduleStartDate: startValue,
      scheduleEndDate: endValue,
    }));
    setTaskDrafts([buildEmptyTask(startValue, endValue)]);
  };

  const eventStyleGetter = () => {
    return {
      style: {
        backgroundColor: 'var(--accent-color, #5A7D8C)',
        borderRadius: '6px',
        color: '#fff',
        border: 'none',
        padding: '4px 6px',
        boxShadow: '0 1px 2px rgba(0,0,0,0.2)',
      },
    };
  };

  const formatDisplayRange = (start, end) => {
    if (!start) return '';
    const startText = formatDate(start, 'eee, MMM d, yyyy h:mm a');
    if (!end || end.getTime() === start.getTime()) return startText;
    const sameDay =
      start.getFullYear() === end.getFullYear() &&
      start.getMonth() === end.getMonth() &&
      start.getDate() === end.getDate();
    const endText = formatDate(
      end,
      sameDay ? 'h:mm a' : 'eee, MMM d, yyyy h:mm a'
    );
    return `${startText} → ${endText}`;
  };

  const handleTaskChange = (index, field, value) => {
    setTaskDrafts(prev => {
      const updatedTasks = [...prev];
      updatedTasks[index] = { ...updatedTasks[index], [field]: value };
      return updatedTasks;
    });
  };

  const addTaskRow = (startDate, endDate) => {
    setTaskDrafts(prev => {
      const start =
        startDate ||
        newSchedule.scheduleStartDate ||
        format(new Date(), 'yyyy-MM-dd');
      const end = endDate || newSchedule.scheduleEndDate || start;
      return [...prev, buildEmptyTask(start, end)];
    });
  };

  const removeTaskRow = index => {
    setTaskDrafts(prev => {
      if (prev.length === 1) return prev;
      return prev.filter((_, idx) => idx !== index);
    });
  };

  const resetTaskDrafts = (start, end) => {
    setTaskDrafts([buildEmptyTask(start, end)]);
  };

  const handleCreateSchedule = async e => {
    e.preventDefault();
    setFormError('');

    if (!newSchedule.scheduleDescription.trim()) {
      setFormError('Schedule description is required.');
      return;
    }

    if (!newSchedule.lotId) {
      setFormError('Please select a lot.');
      return;
    }

    const scheduleDateError = validateScheduleRange(
      newSchedule.scheduleStartDate,
      newSchedule.scheduleEndDate
    );
    if (scheduleDateError) {
      setFormError(scheduleDateError);
      return;
    }

    setIsSaving(true);

    try {
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
            'Could not get token for create, proceeding without auth'
          );
        }
      }

      const payload = {
        scheduleDescription: newSchedule.scheduleDescription.trim(),
        scheduleStartDate: newSchedule.scheduleStartDate,
        scheduleEndDate: newSchedule.scheduleEndDate,
        lotId: newSchedule.lotId,
      };

      const created = await projectScheduleApi.createProjectSchedule(
        projectId,
        payload,
        token
      );

      const createdSchedule = Array.isArray(created) ? created[0] : created;
      const normalized = normalizeSchedule(
        createdSchedule,
        schedules.length + 1
      );

      const newEvent = mapScheduleToEvent(normalized, schedules.length + 1);

      setSchedules(prev => [...prev, normalized]);
      setEvents(prev => [...prev, newEvent]);
      setSelectedEvent(newEvent);
      setProjectName(prev => prev || normalized.projectName || 'Project');
      setScheduleForTasks(normalized);
      resetTaskDrafts(payload.scheduleStartDate, payload.scheduleEndDate);
      setIsCreateModalOpen(false);
      setIsTaskModalOpen(true);
      setTaskFormError('');
      setNewSchedule(buildEmptyScheduleForm());
    } catch (err) {
      console.error('Error creating schedule:', err);
      const detailed = extractErrorMessage(err);
      setFormError(`Failed to create schedule: ${detailed}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveTasks = async () => {
    if (!scheduleForTasks) {
      setTaskFormError('No schedule selected. Please create a schedule first.');
      return;
    }

    setTaskFormError('');

    const scheduleStart = scheduleForTasks.scheduleStartDate;
    const scheduleEnd = scheduleForTasks.scheduleEndDate;

    const tasksToSave = taskDrafts
      .filter(task => task.taskTitle?.trim() || task.taskDescription?.trim())
      .map((task, idx) => ({
        ...task,
        taskTitle: task.taskTitle?.trim() || `Task ${idx + 1}`,
        taskDescription:
          task.taskDescription?.trim() ||
          task.taskTitle?.trim() ||
          `Task ${idx + 1}`,
        periodStart: task.periodStart || scheduleStart,
        periodEnd: task.periodEnd || scheduleEnd,
        taskStatus: task.taskStatus || TASK_STATUSES[0],
        taskPriority: task.taskPriority || TASK_PRIORITIES[2],
      }));

    for (let i = 0; i < tasksToSave.length; i += 1) {
      const validation = validateTaskWithinSchedule(
        tasksToSave[i],
        scheduleStart,
        scheduleEnd
      );
      if (validation) {
        setTaskFormError(`Task ${i + 1}: ${validation}`);
        return;
      }
    }

    if (tasksToSave.length === 0) {
      setIsTaskModalOpen(false);
      setScheduleForTasks(null);
      resetTaskDrafts(scheduleStart, scheduleEnd);
      return;
    }

    setIsSavingTasks(true);

    try {
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
            'Could not get token for tasks, proceeding without auth'
          );
        }
      }

      const scheduleIdentifier = getScheduleIdentifier(scheduleForTasks);
      if (!scheduleIdentifier) {
        setTaskFormError('Could not determine schedule id for tasks.');
        return;
      }

      const preparedTasks = tasksToSave.map((task, idx) => ({
        taskStatus: task.taskStatus,
        taskTitle: task.taskTitle,
        periodStart: task.periodStart,
        periodEnd: task.periodEnd,
        taskDescription: task.taskDescription,
        taskPriority: task.taskPriority,
        estimatedHours:
          task.estimatedHours === '' || task.estimatedHours === undefined
            ? null
            : Number(task.estimatedHours),
        hoursSpent:
          task.hoursSpent === '' || task.hoursSpent === undefined
            ? null
            : Number(task.hoursSpent),
        taskProgress:
          task.taskProgress === '' || task.taskProgress === undefined
            ? null
            : Number(task.taskProgress),
        assignedToUserId: task.assignedToUserId || null,
        scheduleId: scheduleIdentifier,
        taskSequence: idx + 1,
      }));

      const createdTasks = await Promise.all(
        preparedTasks.map(task => taskApi.createTask(task, token))
      );

      const normalizedTasks = createdTasks.map(task =>
        Array.isArray(task) ? task[0] : task
      );

      setSchedules(prev =>
        prev.map(item =>
          getScheduleIdentifier(item) === scheduleIdentifier
            ? { ...item, tasks: normalizedTasks }
            : item
        )
      );

      setEvents(prev =>
        prev.map(ev =>
          getScheduleIdentifier(ev) === scheduleIdentifier
            ? { ...ev, tasks: normalizedTasks }
            : ev
        )
      );

      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier
          ? { ...prev, tasks: normalizedTasks }
          : prev
      );

      setIsTaskModalOpen(false);
      setScheduleForTasks(null);
      resetTaskDrafts(scheduleStart, scheduleEnd);
    } catch (taskErr) {
      console.error('Error saving tasks:', taskErr);
      const status = taskErr?.response?.status;
      const detailed = extractErrorMessage(taskErr);
      const authHint = status === 401 || status === 403
        ? ' Please make sure you are signed in with a role that can create tasks.'
        : '';
      setTaskFormError(`Failed to save tasks: ${detailed}.${authHint}`);
    } finally {
      setIsSavingTasks(false);
    }
  };

  const CustomToolbar = toolbarProps => {
    const today = new Date();
    const monthNames = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];

    const currentYear = currentDate.getFullYear();
    const years = Array.from({ length: 11 }, (_, idx) => currentYear - 5 + idx);

    const handleMonthChange = e => {
      const nextDate = new Date(currentDate);
      nextDate.setMonth(Number(e.target.value));
      setCurrentDate(nextDate);
      toolbarProps.onNavigate('DATE', nextDate);
    };

    const handleYearChange = e => {
      const nextDate = new Date(currentDate);
      nextDate.setFullYear(Number(e.target.value));
      setCurrentDate(nextDate);
      toolbarProps.onNavigate('DATE', nextDate);
    };

    const handleViewChange = view => {
      setCurrentView(view);
      toolbarProps.onView(view);
    };

    return (
      <div className="schedule-toolbar">
        <div className="toolbar-left">
          <button
            type="button"
            className="toolbar-button"
            onClick={() => toolbarProps.onNavigate('TODAY')}
          >
            Today
          </button>
          <button
            type="button"
            className="toolbar-button"
            onClick={() => toolbarProps.onNavigate('PREV')}
          >
            ←
          </button>
          <button
            type="button"
            className="toolbar-button"
            onClick={() => toolbarProps.onNavigate('NEXT')}
          >
            →
          </button>
          <button
            type="button"
            className={`toolbar-button ${currentView === Views.WEEK ? 'toolbar-button-active' : ''}`}
            onClick={() => handleViewChange(Views.WEEK)}
          >
            Week
          </button>
          <button
            type="button"
            className={`toolbar-button ${currentView === Views.MONTH ? 'toolbar-button-active' : ''}`}
            onClick={() => handleViewChange(Views.MONTH)}
          >
            Month
          </button>
          <span className="today-chip">
            Today: {formatDate(today, 'eee, MMM d')}
          </span>
        </div>

        <div className="toolbar-right">
          <select
            aria-label="Select month"
            className="toolbar-select"
            value={currentDate.getMonth()}
            onChange={handleMonthChange}
          >
            {monthNames.map((label, idx) => (
              <option key={label} value={idx}>
                {label}
              </option>
            ))}
          </select>
          <select
            aria-label="Select year"
            className="toolbar-select"
            value={currentDate.getFullYear()}
            onChange={handleYearChange}
          >
            {years.map(year => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
          <span>{toolbarProps.label}</span>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="schedule-loading">
        <div className="spinner"></div>
        <p>Loading project schedule...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-error">
        <h2>Error</h2>
        <p>{error}</p>
        <button onClick={() => navigate(-1)} className="back-button">
          Go Back
        </button>
      </div>
    );
  }

  return (
    <div className="project-schedule-page">
      <div className="schedule-header">
        <h1>Project Schedule: {projectName}</h1>
        <div className="schedule-actions">
          <button
            type="button"
            className="create-schedule-button"
            onClick={() => {
              setIsCreateModalOpen(true);
              setIsModalOpen(false);
              setFormError('');
              setTaskFormError('');
              setScheduleForTasks(null);
              setNewSchedule(buildEmptyScheduleForm());
              resetTaskDrafts();
            }}
          >
            Create Schedule
          </button>
          <button onClick={() => navigate(-1)} className="back-button-small">
            ← Back
          </button>
        </div>
      </div>

      <div className="schedule-layout">
        <div className="schedule-panel">
          <Calendar
            localizer={localizer}
            events={events}
            views={[Views.WEEK, Views.MONTH]}
            defaultView={Views.WEEK}
            defaultDate={defaultDate}
            date={currentDate}
            view={currentView}
            startAccessor="start"
            endAccessor="end"
            style={{ height: 680 }}
            popup={false}
            onSelectEvent={onEventClick}
            selectable
            onSelectSlot={onSlotSelect}
            eventPropGetter={eventStyleGetter}
            dayPropGetter={date =>
              isSameDay(date, new Date())
                ? { className: 'rbc-day-today-strong' }
                : undefined
            }
            onNavigate={(date, view) => {
              setCurrentDate(date);
              setCurrentView(view);
            }}
            onView={view => setCurrentView(view)}
            components={{
              toolbar: toolbarProps => <CustomToolbar {...toolbarProps} />,
            }}
          />
        </div>

        <div className="schedule-side">
          <h2>Schedules</h2>
          {schedules.length === 0 && (
            <div className="schedule-empty-state">
              No schedules yet. Use "Create Schedule" to add one.
            </div>
          )}
          <div className="schedule-list-items">
            {schedules.map(item => (
              <div
                key={
                  item.id ??
                  `${item.scheduleStartDate}-${item.scheduleDescription}`
                }
                className="schedule-card schedule-card-clickable"
                role="button"
                tabIndex={0}
                onClick={() => onScheduleCardClick(item)}
                onKeyDown={e => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onScheduleCardClick(item);
                  }
                }}
              >
                <div className="schedule-card-date">
                  <span className="schedule-card-date-text">
                    {item.scheduleStartDate}
                  </span>
                </div>
                <div className="schedule-card-body">
                  <div className="schedule-card-title">
                    {item.scheduleDescription}
                  </div>
                  <div className="schedule-card-meta">
                    Lot: {item.lotNumber}
                  </div>
                  {Array.isArray(item.tasks) ||
                  Array.isArray(item.scheduleTasks) ? (
                    <div className="schedule-card-meta">
                      Tasks:{' '}
                      {(Array.isArray(item.scheduleTasks)
                        ? item.scheduleTasks
                        : Array.isArray(item.tasks)
                          ? item.tasks
                          : []
                      ).length || 0}
                    </div>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </div>
        {isCreateModalOpen && (
          <div
            className="schedule-modal-overlay"
            role="dialog"
            aria-modal="true"
          >
            <div className="schedule-modal create-schedule-modal">
              <div className="schedule-modal-header">
                <div className="schedule-modal-title">Create Schedule</div>
                <button
                  type="button"
                  className="modal-close"
                  aria-label="Close"
                  onClick={() => {
                    setIsCreateModalOpen(false);
                    setFormError('');
                    setNewSchedule(buildEmptyScheduleForm());
                  }}
                >
                  ×
                </button>
              </div>

              <form
                className="create-schedule-form"
                onSubmit={handleCreateSchedule}
              >
                <div className="form-row">
                  <label>
                    <span>Schedule description</span>
                    <input
                      type="text"
                      value={newSchedule.scheduleDescription}
                      onChange={e =>
                        setNewSchedule(prev => ({
                          ...prev,
                          scheduleDescription: e.target.value,
                        }))
                      }
                      placeholder="Foundation pour, framing, etc."
                      required
                    />
                  </label>
                </div>

                <div className="form-row two-col">
                  <label>
                    <span>Lot / Phase</span>
                    <select
                      value={newSchedule.lotId}
                      onChange={e =>
                        setNewSchedule(prev => ({
                          ...prev,
                          lotId: e.target.value,
                        }))
                      }
                      disabled={lotsLoading}
                    >
                      <option value="" disabled>
                        {lotsLoading ? 'Loading lots...' : 'Select a lot'}
                      </option>
                      {lots.map(lot => {
                        const lotId = getLotId(lot);
                        return (
                          <option key={lotId} value={lotId}>
                            {formatLotLabel(lot)}
                          </option>
                        );
                      })}
                    </select>
                    {lotsError && (
                      <div className="form-error subtle">{lotsError}</div>
                    )}
                  </label>

                  <label>
                    <span>Start date</span>
                    <input
                      type="date"
                      value={newSchedule.scheduleStartDate}
                      onChange={e =>
                        setNewSchedule(prev => ({
                          ...prev,
                          scheduleStartDate: e.target.value,
                        }))
                      }
                      required
                    />
                  </label>
                </div>

                <div className="form-row two-col">
                  <label>
                    <span>End date</span>
                    <input
                      type="date"
                      value={newSchedule.scheduleEndDate}
                      onChange={e =>
                        setNewSchedule(prev => ({
                          ...prev,
                          scheduleEndDate: e.target.value,
                        }))
                      }
                      required
                    />
                  </label>
                </div>

                <div className="form-row form-row-note">
                  <div className="tasks-subtitle">
                    After saving these schedule details, you will add tasks in
                    the next step. Tasks will be constrained to the start and
                    end dates you set here.
                  </div>
                </div>

                {formError && <div className="form-error">{formError}</div>}

                <div className="form-actions">
                  <button
                    type="button"
                    className="modal-secondary"
                    onClick={() => {
                      setIsCreateModalOpen(false);
                      setFormError('');
                      setNewSchedule(buildEmptyScheduleForm());
                    }}
                    disabled={isSaving}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="modal-primary"
                    disabled={isSaving}
                  >
                    {isSaving ? 'Saving…' : 'Save schedule'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
        {scheduleForTasks && isTaskModalOpen && (
          <div
            className="schedule-modal-overlay"
            role="dialog"
            aria-modal="true"
          >
            <div className="schedule-modal tasks-modal">
              <div className="schedule-modal-header">
                <div className="schedule-modal-title">
                  Add tasks for {scheduleForTasks.scheduleDescription}
                </div>
                <button
                  type="button"
                  className="modal-close"
                  aria-label="Close"
                  onClick={() => {
                    setIsTaskModalOpen(false);
                    setScheduleForTasks(null);
                    resetTaskDrafts(
                      scheduleForTasks.scheduleStartDate,
                      scheduleForTasks.scheduleEndDate
                    );
                    setTaskFormError('');
                  }}
                >
                  ×
                </button>
              </div>

              <div className="schedule-modal-section">
                <h4>Schedule window</h4>
                <div>
                  {scheduleForTasks.scheduleStartDate} →{' '}
                  {scheduleForTasks.scheduleEndDate}
                  {scheduleForTasks.lotNumber &&
                    ` · Lot ${scheduleForTasks.lotNumber}`}
                </div>
                <div className="tasks-subtitle">
                  Task dates must stay within this window.
                </div>
              </div>

              <div className="task-list">
                {taskDrafts.map((task, idx) => (
                  <div key={`task-${idx}`} className="task-row">
                    <div className="task-row-header">
                      <span className="task-row-title">Task {idx + 1}</span>
                      <button
                        type="button"
                        className="task-remove-button"
                        onClick={() => removeTaskRow(idx)}
                        aria-label={`Remove task ${idx + 1}`}
                        disabled={taskDrafts.length === 1}
                      >
                        Remove
                      </button>
                    </div>

                    <div className="task-row-grid">
                      <label>
                        <span>Title</span>
                        <input
                          type="text"
                          value={task.taskTitle}
                          onChange={e =>
                            handleTaskChange(idx, 'taskTitle', e.target.value)
                          }
                          placeholder={`Task ${idx + 1} title`}
                        />
                      </label>

                      <label>
                        <span>Status</span>
                        <select
                          value={task.taskStatus}
                          onChange={e =>
                            handleTaskChange(idx, 'taskStatus', e.target.value)
                          }
                        >
                          {TASK_STATUSES.map(status => (
                            <option key={status} value={status}>
                              {status.replaceAll('_', ' ')}
                            </option>
                          ))}
                        </select>
                      </label>

                      <label>
                        <span>Priority</span>
                        <select
                          value={task.taskPriority}
                          onChange={e =>
                            handleTaskChange(
                              idx,
                              'taskPriority',
                              e.target.value
                            )
                          }
                        >
                          {TASK_PRIORITIES.map(priority => (
                            <option key={priority} value={priority}>
                              {priority.replaceAll('_', ' ')}
                            </option>
                          ))}
                        </select>
                      </label>
                    </div>

                    <div className="task-row-grid">
                      <label>
                        <span>Period start</span>
                        <input
                          type="date"
                          value={task.periodStart}
                          min={scheduleForTasks.scheduleStartDate}
                          max={scheduleForTasks.scheduleEndDate}
                          onChange={e =>
                            handleTaskChange(idx, 'periodStart', e.target.value)
                          }
                        />
                      </label>

                      <label>
                        <span>Period end</span>
                        <input
                          type="date"
                          value={task.periodEnd}
                          min={scheduleForTasks.scheduleStartDate}
                          max={scheduleForTasks.scheduleEndDate}
                          onChange={e =>
                            handleTaskChange(idx, 'periodEnd', e.target.value)
                          }
                        />
                      </label>

                      <label>
                        <span>Assignee (user UUID)</span>
                        <input
                          type="text"
                          value={task.assignedToUserId}
                          onChange={e =>
                            handleTaskChange(
                              idx,
                              'assignedToUserId',
                              e.target.value
                            )
                          }
                          placeholder="Optional"
                        />
                      </label>
                    </div>

                    <label className="task-row-full">
                      <span>Description</span>
                      <textarea
                        value={task.taskDescription}
                        onChange={e =>
                          handleTaskChange(
                            idx,
                            'taskDescription',
                            e.target.value
                          )
                        }
                        placeholder="Describe the work to be completed"
                        rows={2}
                      />
                    </label>

                    <div className="task-row-grid task-row-numbers">
                      <label>
                        <span>Estimated hours</span>
                        <input
                          type="number"
                          min="0"
                          step="0.5"
                          value={task.estimatedHours}
                          onChange={e =>
                            handleTaskChange(
                              idx,
                              'estimatedHours',
                              e.target.value
                            )
                          }
                          placeholder="Optional"
                        />
                      </label>

                      <label>
                        <span>Hours spent</span>
                        <input
                          type="number"
                          min="0"
                          step="0.5"
                          value={task.hoursSpent}
                          onChange={e =>
                            handleTaskChange(idx, 'hoursSpent', e.target.value)
                          }
                          placeholder="Optional"
                        />
                      </label>

                      <label>
                        <span>Progress (%)</span>
                        <input
                          type="number"
                          min="0"
                          max="100"
                          step="1"
                          value={task.taskProgress}
                          onChange={e =>
                            handleTaskChange(
                              idx,
                              'taskProgress',
                              e.target.value
                            )
                          }
                          placeholder="Optional"
                        />
                      </label>
                    </div>
                  </div>
                ))}
              </div>

              <div className="task-actions">
                <button
                  type="button"
                  className="task-add-button"
                  onClick={() =>
                    addTaskRow(
                      scheduleForTasks.scheduleStartDate,
                      scheduleForTasks.scheduleEndDate
                    )
                  }
                >
                  + Add task
                </button>
              </div>

              {taskFormError && (
                <div className="form-error">{taskFormError}</div>
              )}

              <div className="form-actions">
                <button
                  type="button"
                  className="modal-secondary"
                  onClick={() => {
                    setIsTaskModalOpen(false);
                    setScheduleForTasks(null);
                    resetTaskDrafts(
                      scheduleForTasks.scheduleStartDate,
                      scheduleForTasks.scheduleEndDate
                    );
                    setTaskFormError('');
                  }}
                  disabled={isSavingTasks}
                >
                  Skip for now
                </button>
                <button
                  type="button"
                  className="modal-primary"
                  onClick={handleSaveTasks}
                  disabled={isSavingTasks}
                >
                  {isSavingTasks ? 'Saving tasks…' : 'Save tasks'}
                </button>
              </div>
            </div>
          </div>
        )}
        {selectedEvent && isModalOpen && (
          <div
            className="schedule-modal-overlay"
            role="dialog"
            aria-modal="true"
          >
            <div className="schedule-modal">
              <div className="schedule-modal-header">
                <div className="schedule-modal-title">
                  {selectedEvent.title}
                </div>
                <button
                  type="button"
                  className="modal-close"
                  aria-label="Close"
                  onClick={() => setIsModalOpen(false)}
                >
                  ×
                </button>
              </div>

              <div className="schedule-modal-section">
                <h4>Date</h4>
                <div>
                  {formatDisplayRange(selectedEvent.start, selectedEvent.end)}
                </div>
              </div>

              {selectedEvent.description && (
                <div className="schedule-modal-section">
                  <h4>Description</h4>
                  <div>{selectedEvent.description}</div>
                </div>
              )}

              {Array.isArray(selectedEvent.tasks) && (
                <div className="schedule-modal-section schedule-modal-tasks">
                  <h4>Tasks</h4>
                  {selectedEvent.tasksLoading ? (
                    <div>Loading tasks…</div>
                  ) : selectedEvent.tasksError ? (
                    <div>{selectedEvent.tasksError}</div>
                  ) : selectedEvent.tasks.length === 0 ? (
                    <div>None listed</div>
                  ) : (
                    <ul className="schedule-task-list">
                      {selectedEvent.tasks.map((task, idx) => {
                        const taskId =
                          task.taskId ??
                          task.id ??
                          task.identifier ??
                          `task-${idx + 1}`;
                        const title =
                          task.taskTitle ||
                          task.taskDescription ||
                          task.description ||
                          task.name ||
                          'Untitled task';
                        const status = task.taskStatus || 'Not set';
                        const taskPath = taskId ? `/tasks/${taskId}` : null;

                        return (
                          <li key={taskId} className="schedule-task-item">
                            <div className="schedule-task-main">
                              {taskPath ? (
                                <button
                                  type="button"
                                  className="task-link"
                                  onClick={() => navigate(taskPath)}
                                >
                                  {title}
                                </button>
                              ) : (
                                <span>{title}</span>
                              )}
                              <span className="task-chip task-chip-inline">
                                {status}
                              </span>
                            </div>
                            <div className="schedule-task-sub">
                              ID: {taskId}
                            </div>
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProjectSchedulePage;
