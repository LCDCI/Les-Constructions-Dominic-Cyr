import React, {
  useState,
  useEffect,
  useMemo,
  useRef,
  useCallback,
} from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { FiEdit2 } from 'react-icons/fi';
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
import { projectApi } from '../../features/projects/api/projectApi';
import { fetchAllContractors } from '../../features/users/api/usersApi';
import ScheduleDetailModal from '../../components/Modals/ScheduleDetailModal';
import ScheduleFormModal from '../../components/Modals/ScheduleFormModal';
import EditScheduleModal from '../../components/Modals/EditScheduleModal';
import TaskModal from '../../components/Modals/TaskModal';
import { useBackendUser } from '../../hooks/useBackendUser';
import { ROLES } from '../../utils/permissions';
import '../../styles/Project/ProjectSchedule.css';

const locales = {
  'en-US': enUS,
};

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek,
  getDay,
  locales,
});

const TASK_STATUSES = ['TO_DO', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD'];
const TASK_PRIORITIES = ['VERY_LOW', 'LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'];

const buildEmptyScheduleForm = () => ({
  scheduleDescription: '',
  scheduleStartDate: format(new Date(), 'yyyy-MM-dd'),
  scheduleEndDate: format(new Date(), 'yyyy-MM-dd'),
  lotId: '',
  scheduleIdentifier: null,
});

const buildEmptyTask = (start, end, isEditable = false) => ({
  taskId: null,
  taskTitle: '',
  taskStatus: TASK_STATUSES[0],
  taskPriority: TASK_PRIORITIES[2],
  periodStart: start || '',
  periodEnd: end || start || '',
  assignedToUserId: '',
  taskDescription: '',
  estimatedHours: '',
  hoursSpent: '',
  isEditable,
});

const normalizeDateForInput = value => {
  if (!value) return '';

  if (typeof value === 'string') {
    const match = value.match(/\d{4}-\d{2}-\d{2}/);
    if (match) return match[0];
    const parsed = parseISO(value);
    return Number.isNaN(parsed?.getTime())
      ? ''
      : formatDate(parsed, 'yyyy-MM-dd');
  }

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? '' : formatDate(value, 'yyyy-MM-dd');
  }

  const coerced = new Date(value);
  return Number.isNaN(coerced.getTime())
    ? ''
    : formatDate(coerced, 'yyyy-MM-dd');
};

const normalizeTaskFromApi = (task, scheduleStart, scheduleEnd) => ({
  ...task,
  periodStart: normalizeDateForInput(
    task?.periodStart || task?.startDate || scheduleStart
  ),
  periodEnd: normalizeDateForInput(
    task?.periodEnd || task?.endDate || task?.periodStart || scheduleEnd
  ),
});

const buildTaskFromExisting = (task, scheduleStart, scheduleEnd) => ({
  taskId: task?.taskId ?? task?.id ?? task?.identifier ?? null,
  taskTitle: task?.taskTitle ?? task?.title ?? '',
  taskStatus: task?.taskStatus ?? task?.status ?? TASK_STATUSES[0],
  taskPriority: task?.taskPriority ?? task?.priority ?? TASK_PRIORITIES[2],
  periodStart: normalizeDateForInput(
    task?.periodStart || task?.startDate || scheduleStart
  ),
  periodEnd: normalizeDateForInput(
    task?.periodEnd || task?.endDate || task?.periodStart || scheduleEnd
  ),
  assignedToUserId: task?.assignedToUserId ?? task?.assigneeId ?? '',
  taskDescription: task?.taskDescription ?? task?.description ?? '',
  estimatedHours: task?.estimatedHours ?? '',
  hoursSpent: task?.hoursSpent ?? '',
  isEditable: false,
});

const computeTaskProgress = (estimated, hours) => {
  const e =
    estimated === '' || estimated === undefined ? null : Number(estimated);
  const h = hours === '' || hours === undefined ? null : Number(hours);
  if (e === null || e === 0 || h === null) return null;
  return Math.round((h / e) * 100);
};

const ProjectSchedulePage = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();

  const [events, setEvents] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [projectName, setProjectName] = useState('Project');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lots, setLots] = useState([]);
  const [lotsLoading, setLotsLoading] = useState(false);
  const [lotsError, setLotsError] = useState(null);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [defaultDate, setDefaultDate] = useState(new Date());
  const [currentView, setCurrentView] = useState(Views.MONTH);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  const [selectedEvent, setSelectedEvent] = useState(null);
  const [scheduleForTasks, setScheduleForTasks] = useState(null);
  const [newSchedule, setNewSchedule] = useState(buildEmptyScheduleForm());
  const [editSchedule, setEditSchedule] = useState(buildEmptyScheduleForm());
  const [taskDrafts, setTaskDrafts] = useState([buildEmptyTask('', '')]);
  const [editTaskDrafts, setEditTaskDrafts] = useState([]);
  const [tasksToDelete, setTasksToDelete] = useState([]);

  const [projectContractors, setProjectContractors] = useState([]);

  const [isSaving, setIsSaving] = useState(false);
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [isDeletingSchedule, setIsDeletingSchedule] = useState(false);
  const [isSavingTasks, setIsSavingTasks] = useState(false);
  const [formError, setFormError] = useState('');
  const [editFormError, setEditFormError] = useState('');
  const [taskFormError, setTaskFormError] = useState('');

  const originalOverflow = useRef(null);
  const didLockBody = useRef(false);

  const parseDateSafe = input => {
    if (!input) return null;
    if (input instanceof Date) {
      return Number.isNaN(input.getTime()) ? null : input;
    }

    try {
      const parsed =
        typeof input === 'string' && !/^[0-9]+$/.test(input)
          ? parseISO(input)
          : new Date(input);
      return Number.isNaN(parsed.getTime()) ? null : parsed;
    } catch (err) {
      return null;
    }
  };

  const normalizeDateInput = input => {
    const date = parseDateSafe(input);
    return date ? format(date, 'yyyy-MM-dd') : '';
  };

  const combineDateAndTime = (dateValue, timeValue, isStart = true) => {
    const dateOnly = parseDateSafe(dateValue);
    if (!dateOnly) return null;

    if (timeValue) {
      const [hoursPart, minutesPart] = String(timeValue).split(':');
      const hoursNumber = Number(hoursPart);
      const minutesNumber = Number(minutesPart ?? 0);
      const safeHours = Number.isNaN(hoursNumber)
        ? isStart
          ? 8
          : 17
        : hoursNumber;
      const safeMinutes = Number.isNaN(minutesNumber) ? 0 : minutesNumber;
      return setMinutes(setHours(dateOnly, safeHours), safeMinutes);
    }

    return isStart
      ? setMinutes(setHours(dateOnly, 8), 0)
      : setMinutes(setHours(dateOnly, 23), 59);
  };

  const normalizeSchedule = (schedule, fallbackId = null) => {
    if (!schedule) {
      return {
        id: fallbackId,
        scheduleId: fallbackId,
        scheduleIdentifier: fallbackId,
        scheduleDescription: '',
        scheduleStartDate: '',
        scheduleEndDate: '',
        lotId: '',
        lotIdentifier: '',
        lotNumber: '',
        projectName: 'Project',
        tasks: [],
      };
    }

    const scheduleIdentifier =
      schedule.scheduleIdentifier ??
      schedule.scheduleId ??
      schedule.identifier ??
      schedule.id ??
      fallbackId;

    const rawStart =
      schedule.scheduleStartDate ??
      schedule.startDate ??
      schedule.start ??
      schedule.periodStart ??
      schedule.start_time ??
      schedule.startTime ??
      null;

    const rawEnd =
      schedule.scheduleEndDate ??
      schedule.endDate ??
      schedule.end ??
      schedule.periodEnd ??
      schedule.end_time ??
      schedule.endTime ??
      rawStart;

    const normalizedStartDate = normalizeDateInput(rawStart);
    const normalizedEndDate = normalizeDateInput(rawEnd) || normalizedStartDate;

    const normalizedLotId =
      schedule.lotIdentifier?.lotId ??
      schedule.lotId ??
      schedule.lot?.lotId ??
      schedule.lot?.id ??
      (typeof schedule.lotIdentifier === 'string' ||
      typeof schedule.lotIdentifier === 'number'
        ? schedule.lotIdentifier
        : '');

    const normalizedLotNumber =
      schedule.lotNumber ??
      schedule.lot?.lotNumber ??
      schedule.lotIdentifier?.lotNumber ??
      schedule.lot?.lotIdentifier?.lotNumber ??
      schedule.lotName ??
      '';

    const normalizedTasks = Array.isArray(schedule.tasks)
      ? schedule.tasks
      : Array.isArray(schedule.scheduleTasks)
        ? schedule.scheduleTasks
        : [];

    return {
      ...schedule,
      id: schedule.id ?? scheduleIdentifier ?? fallbackId ?? Date.now(),
      scheduleId: schedule.scheduleId ?? scheduleIdentifier ?? fallbackId,
      scheduleIdentifier,
      scheduleDescription:
        schedule.scheduleDescription ??
        schedule.description ??
        schedule.name ??
        '',
      scheduleStartDate: normalizedStartDate,
      scheduleEndDate: normalizedEndDate,
      lotId: normalizedLotId ? String(normalizedLotId) : '',
      lotIdentifier: normalizedLotId ? String(normalizedLotId) : '',
      lotNumber: normalizedLotNumber,
      projectName:
        schedule.projectName ??
        schedule.project?.projectName ??
        schedule.project?.name ??
        'Project',
      tasks: normalizedTasks,
    };
  };

  const mapScheduleToEvent = (scheduleEntity, fallbackId = null) => {
    const normalized = normalizeSchedule(scheduleEntity, fallbackId);

    const startDateTime = combineDateAndTime(
      normalized.scheduleStartDate,
      normalized.scheduleStartTime ??
        normalized.startTime ??
        normalized.startHour,
      true
    );

    const endDateTime = combineDateAndTime(
      normalized.scheduleEndDate,
      normalized.scheduleEndTime ?? normalized.endTime ?? normalized.endHour,
      false
    );

    const safeStart = startDateTime || new Date();
    const safeEnd = endDateTime || startDateTime || new Date();

    return {
      ...normalized,
      id:
        normalized.scheduleIdentifier ??
        normalized.id ??
        fallbackId ??
        Date.now(),
      title:
        normalized.scheduleDescription ||
        (normalized.lotNumber
          ? `Schedule · ${normalized.lotNumber}`
          : 'Schedule'),
      start: safeStart,
      end: safeEnd < safeStart ? safeStart : safeEnd,
      allDay:
        !(
          normalized.scheduleStartTime ||
          normalized.startTime ||
          normalized.startHour
        ) &&
        !(
          normalized.scheduleEndTime ||
          normalized.endTime ||
          normalized.endHour
        ),
    };
  };

  const validateTaskWithinSchedule = (task, scheduleStart, scheduleEnd) => {
    const taskStart = parseDateSafe(task?.periodStart || task?.startDate);
    const taskEnd = parseDateSafe(
      task?.periodEnd || task?.endDate || task?.periodStart
    );
    const scheduleStartDate = parseDateSafe(scheduleStart);
    const scheduleEndDate = parseDateSafe(scheduleEnd || scheduleStart);

    if (!taskStart || !taskEnd) return 'Task start and end dates are required.';
    if (taskEnd < taskStart)
      return 'Task end date cannot be before the start date.';
    if (scheduleStartDate && taskStart < scheduleStartDate)
      return 'Task start date is before the schedule window.';
    if (scheduleEndDate && taskEnd > scheduleEndDate)
      return 'Task end date is after the schedule window.';
    return null;
  };

  const extractErrorMessage = err => {
    const responseData = err?.response?.data;
    if (responseData) {
      if (typeof responseData === 'string') return responseData;
      if (typeof responseData.message === 'string') return responseData.message;
      if (Array.isArray(responseData.errors))
        return responseData.errors.join(', ');
    }
    return err?.message || 'Unexpected error';
  };

  const validateScheduleRange = (start, end) => {
    const startDate = parseDateSafe(start);
    const endDate = parseDateSafe(end);
    if (!startDate || !endDate) return 'Start and end dates are required.';
    if (endDate < startDate) {
      return 'Schedule end date cannot be before the start date.';
    }
    return null;
  };

  const getLotId = lot =>
    lot?.lotIdentifier?.lotId || lot?.lotId || (lot?.id ? String(lot.id) : '');

  const formatLotLabel = lot => {
    // Prefer human-friendly civic address when available
    const civic = lot?.civicAddress;
    if (civic) return civic;

    // Fall back to location, lot number, or the internal id
    if (lot?.location) return `${lot.location} (${getLotId(lot)})`;
    if (lot?.lotNumber) return `Lot ${lot.lotNumber} (${getLotId(lot)})`;
    return getLotId(lot) || 'Unknown lot';
  };

  const lotOptions = useMemo(
    () =>
      (lots || [])
        .map(lot => {
          const value = getLotId(lot);
          return value
            ? {
                value,
                label: formatLotLabel(lot),
              }
            : null;
        })
        .filter(Boolean),
    [lots]
  );

  const getScheduleIdentifier = entity =>
    entity?.scheduleId ??
    entity?.scheduleIdentifier ??
    entity?.identifier ??
    entity?.id ??
    null;

  const findEventForSchedule = scheduleEntity => {
    if (!scheduleEntity) return null;
    const targetId = getScheduleIdentifier(scheduleEntity);
    return (
      events.find(
        ev =>
          getScheduleIdentifier(ev) === targetId || ev.id === scheduleEntity.id
      ) || null
    );
  };

  useEffect(() => {
    const fetchSchedules = async () => {
      setLoading(true);

      try {
        let token = null;

        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {}
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

        // Fetch project data to get contractor IDs
        try {
          const projectData = await projectApi.getProjectById(projectId, token);
          const contractorIds = projectData.contractorIds || [];

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
          console.warn('Could not load project contractors:', projectErr);
          setProjectContractors([]);
        }

        setError(null);
      } catch (err) {
        setError('Failed to load project schedules. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (projectId) {
      fetchSchedules();
    }
  }, [projectId, isLoading, isAuthenticated, getAccessTokenSilently]);

  const loadLots = useCallback(async () => {
    setLotsLoading(true);
    try {
      let token = null;
      if (isAuthenticated) {
        try {
          token = await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            },
          });
        } catch (tokenErr) {}
      }

      const response = await fetchLots({ projectIdentifier: projectId, token });
      setLots(Array.isArray(response) ? response : []);
      setLotsError(null);
    } catch (err) {
      console.error('Lots loading error:', err);
      setLotsError('Unable to load lots.');
    } finally {
      setLotsLoading(false);
    }
  }, [isAuthenticated, getAccessTokenSilently, projectId]);

  useEffect(() => {
    loadLots();
  }, [loadLots]);

  const onEventClick = async event => {
    const scheduleIdentifier = getScheduleIdentifier(event);

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
        } catch (tokenErr) {}
      }

      const attemptFetch = async ident =>
        taskApi.getTasksForSchedule(ident, token);

      let tasks = await attemptFetch(scheduleIdentifier);

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
        } catch (altErr) {}
      }

      const scheduleStart = normalizeDateForInput(
        event.scheduleStartDate || event.start
      );
      const scheduleEnd = normalizeDateForInput(
        event.scheduleEndDate || event.end || event.start
      );

      const normalizedTasks = Array.isArray(tasks)
        ? tasks.map(task =>
            normalizeTaskFromApi(task, scheduleStart, scheduleEnd)
          )
        : [];

      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier
          ? {
              ...prev,
              tasks: normalizedTasks,
              tasksLoading: false,
              tasksError: null,
            }
          : prev
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
    } catch (taskErr) {
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
    const eventLike =
      findEventForSchedule(scheduleItem) || mapScheduleToEvent(scheduleItem);
    onEventClick(eventLike);
  };

  const onSlotSelect = slotInfo => {
    if (role === ROLES.CONTRACTOR) return;
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

  useEffect(() => {
    const state = location.state;
    const wantsEdit = state?.openEditSchedule;
    const wantsDetail = state?.reopenScheduleModal;
    if (!state || (!wantsEdit && !wantsDetail) || !state?.scheduleEventId)
      return;
    if (!events || !events.length) return;

    const eventToOpen = events.find(ev => {
      const evId =
        ev.id || ev.scheduleId || ev.scheduleIdentifier || ev.scheduleId;
      return evId === state.scheduleEventId;
    });

    if (eventToOpen) {
      if (wantsEdit) {
        openEditScheduleModal(eventToOpen);
      } else if (wantsDetail) {
        setSelectedEvent(eventToOpen);
        setIsModalOpen(true);
      }
    }

    // eslint-disable-next-line no-unused-vars
    const { reopenScheduleModal, scheduleEventId, openEditSchedule, ...rest } =
      state;
    navigate(location.pathname, { replace: true, state: rest });
  }, [location.state, events, navigate, location.pathname]);

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

  const closeCreateModal = () => {
    setIsCreateModalOpen(false);
    setFormError('');
    setNewSchedule(buildEmptyScheduleForm());
  };

  const handleCloseTaskModal = () => {
    setIsTaskModalOpen(false);
    if (scheduleForTasks) {
      resetTaskDrafts(
        scheduleForTasks.scheduleStartDate,
        scheduleForTasks.scheduleEndDate
      );
    }
    setScheduleForTasks(null);
    setTaskFormError('');
  };

  useEffect(() => {
    const anyModalOpen =
      isModalOpen || isCreateModalOpen || isTaskModalOpen || isEditModalOpen;

    if (anyModalOpen) {
      const current = document.body.style.overflow || '';
      if (current !== 'hidden') {
        originalOverflow.current = current;
        document.body.style.overflow = 'hidden';
        didLockBody.current = true;
      } else {
        didLockBody.current = false;
      }
    } else if (didLockBody.current && originalOverflow.current !== null) {
      document.body.style.overflow = originalOverflow.current;
      originalOverflow.current = null;
      didLockBody.current = false;
    }

    return () => {
      if (didLockBody.current && originalOverflow.current !== null) {
        document.body.style.overflow = originalOverflow.current;
        originalOverflow.current = null;
        didLockBody.current = false;
      }
    };
  }, [isModalOpen, isCreateModalOpen, isTaskModalOpen, isEditModalOpen]);

  const handleTaskChange = (index, field, value) => {
    setTaskDrafts(prev => {
      const updatedTasks = [...prev];
      updatedTasks[index] = { ...updatedTasks[index], [field]: value };
      return updatedTasks;
    });
  };

  const handleEditTaskChange = (index, field, value) => {
    setEditTaskDrafts(prev => {
      const updatedTasks = [...prev];
      updatedTasks[index] = { ...updatedTasks[index], [field]: value };
      return updatedTasks;
    });
  };

  const toggleEditTask = index => {
    setEditTaskDrafts(prev => {
      const updated = [...prev];
      const current = updated[index];
      updated[index] = { ...current, isEditable: !current?.isEditable };
      return updated;
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

  const addEditTaskRow = (startDate, endDate) => {
    setEditTaskDrafts(prev => {
      const start = startDate || editSchedule.scheduleStartDate;
      const end = endDate || editSchedule.scheduleEndDate || start;
      return [...prev, buildEmptyTask(start, end, true)];
    });
  };

  const removeTaskRow = index => {
    setTaskDrafts(prev => prev.filter((_, idx) => idx !== index));
  };

  const removeEditTaskRow = index => {
    setEditTaskDrafts(prev => {
      const target = prev[index];
      const remaining = prev.filter((_, idx) => idx !== index);
      if (target?.taskId) {
        setTasksToDelete(ids => [...ids, target.taskId]);
      }
      return remaining;
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
        } catch (tokenErr) {}
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
      const detailed = extractErrorMessage(err);
      setFormError(`Failed to create schedule: ${detailed}`);
    } finally {
      setIsSaving(false);
    }
  };

  const openEditScheduleModal = scheduleEntity => {
    const scheduleId = getScheduleIdentifier(scheduleEntity);
    const match =
      schedules.find(item => getScheduleIdentifier(item) === scheduleId) ||
      scheduleEntity;
    const normalized = normalizeSchedule(match, scheduleId || Date.now());

    setEditSchedule({
      scheduleDescription: normalized.scheduleDescription || '',
      lotId: normalized.lotId || normalized.lotIdentifier || '',
      scheduleStartDate:
        normalized.scheduleStartDate || format(new Date(), 'yyyy-MM-dd'),
      scheduleEndDate:
        normalized.scheduleEndDate || format(new Date(), 'yyyy-MM-dd'),
      scheduleIdentifier: scheduleId,
    });

    const existingTasksRaw = Array.isArray(normalized.tasks)
      ? normalized.tasks
      : Array.isArray(normalized.scheduleTasks)
        ? normalized.scheduleTasks
        : [];

    const existingTasks = existingTasksRaw.map(task =>
      normalizeTaskFromApi(
        task,
        normalized.scheduleStartDate,
        normalized.scheduleEndDate
      )
    );

    if (existingTasks.length) {
      setEditTaskDrafts(
        existingTasks.map(task =>
          buildTaskFromExisting(
            task,
            normalized.scheduleStartDate,
            normalized.scheduleEndDate
          )
        )
      );
    } else {
      setEditTaskDrafts([]);
    }

    setTasksToDelete([]);

    setEditFormError('');
    setIsModalOpen(false);
    setIsEditModalOpen(true);
  };

  const handleUpdateSchedule = async e => {
    e.preventDefault();
    setEditFormError('');

    const scheduleIdentifier = editSchedule.scheduleIdentifier;
    if (!scheduleIdentifier) {
      setEditFormError('Missing schedule identifier for update.');
      return false;
    }

    if (!editSchedule.scheduleDescription.trim()) {
      setEditFormError('Schedule description is required.');
      return false;
    }

    if (!editSchedule.lotId) {
      setEditFormError('Please select a lot.');
      return false;
    }

    const rangeError = validateScheduleRange(
      editSchedule.scheduleStartDate,
      editSchedule.scheduleEndDate
    );
    if (rangeError) {
      setEditFormError(rangeError);
      return false;
    }

    const tasksToSave = editTaskDrafts
      .filter(task => task.taskTitle?.trim() || task.taskDescription?.trim())
      .map((task, idx) => ({
        ...task,
        taskTitle: task.taskTitle?.trim() || `Task ${idx + 1}`,
        taskDescription:
          task.taskDescription?.trim() ||
          task.taskTitle?.trim() ||
          `Task ${idx + 1}`,
        periodStart: task.periodStart || editSchedule.scheduleStartDate,
        periodEnd: task.periodEnd || editSchedule.scheduleEndDate,
        taskStatus: task.taskStatus || TASK_STATUSES[0],
        taskPriority: task.taskPriority || TASK_PRIORITIES[2],
      }));

    for (let i = 0; i < tasksToSave.length; i += 1) {
      const validation = validateTaskWithinSchedule(
        tasksToSave[i],
        editSchedule.scheduleStartDate,
        editSchedule.scheduleEndDate
      );
      if (validation) {
        setEditFormError(`Task ${i + 1}: ${validation}`);
        return false;
      }
    }

    setIsSavingEdit(true);

    try {
      let token = null;
      if (isAuthenticated) {
        try {
          token = await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            },
          });
        } catch (tokenErr) {}
      }

      if (tasksToDelete.length) {
        await Promise.all(
          tasksToDelete.map(taskId => taskApi.deleteTask(taskId, token))
        );
      }

      const payload = {
        scheduleDescription: editSchedule.scheduleDescription.trim(),
        scheduleStartDate: editSchedule.scheduleStartDate,
        scheduleEndDate: editSchedule.scheduleEndDate,
        lotId: editSchedule.lotId,
      };

      const updatedSchedule = await projectScheduleApi.updateProjectSchedule(
        projectId,
        scheduleIdentifier,
        payload,
        token
      );

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
        taskProgress: computeTaskProgress(task.estimatedHours, task.hoursSpent),
        assignedToUserId: task.assignedToUserId || null,
        scheduleId: scheduleIdentifier,
        taskSequence: idx + 1,
        taskId: task.taskId,
      }));

      const updatedTasks = [];
      for (const task of preparedTasks) {
        if (task.taskId) {
          const { taskId, ...rest } = task;
          const result = await taskApi.updateTask(taskId, rest, token);
          updatedTasks.push(Array.isArray(result) ? result[0] : result);
        } else {
          const created = await taskApi.createTask(task, token);
          updatedTasks.push(Array.isArray(created) ? created[0] : created);
        }
      }

      const normalizedSchedule = normalizeSchedule(
        updatedSchedule,
        scheduleIdentifier
      );
      normalizedSchedule.tasks = updatedTasks;

      setSchedules(prev =>
        prev.map(item =>
          getScheduleIdentifier(item) === scheduleIdentifier
            ? normalizedSchedule
            : item
        )
      );

      setEvents(prev => {
        const updatedEvent = mapScheduleToEvent(
          normalizedSchedule,
          scheduleIdentifier
        );
        return prev.map(ev =>
          getScheduleIdentifier(ev) === scheduleIdentifier ? updatedEvent : ev
        );
      });

      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier
          ? mapScheduleToEvent(normalizedSchedule, scheduleIdentifier)
          : prev
      );

      setIsEditModalOpen(false);
      setEditFormError('');
      setTasksToDelete([]);
      return true;
    } catch (err) {
      const detailed = extractErrorMessage(err);
      setEditFormError(`Failed to update schedule: ${detailed}`);
      return false;
    } finally {
      setIsSavingEdit(false);
    }
  };

  const handleDeleteSchedule = async () => {
    const scheduleIdentifier = editSchedule.scheduleIdentifier;
    if (!scheduleIdentifier || isDeletingSchedule) return;

    setEditFormError('');
    setIsDeletingSchedule(true);

    try {
      let token = null;
      if (isAuthenticated) {
        try {
          token = await getAccessTokenSilently({
            authorizationParams: {
              audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            },
          });
        } catch (tokenErr) {}
      }

      const allTaskIds = Array.from(
        new Set([
          ...editTaskDrafts.map(task => task.taskId).filter(Boolean),
          ...tasksToDelete.filter(Boolean),
        ])
      );

      if (allTaskIds.length) {
        try {
          await Promise.all(
            allTaskIds.map(id => taskApi.deleteTask(id, token))
          );
        } catch (taskDeleteErr) {}
      }

      await projectScheduleApi.deleteProjectSchedule(
        projectId,
        scheduleIdentifier,
        token
      );

      setSchedules(prev =>
        prev.filter(item => getScheduleIdentifier(item) !== scheduleIdentifier)
      );
      setEvents(prev =>
        prev.filter(ev => getScheduleIdentifier(ev) !== scheduleIdentifier)
      );
      setSelectedEvent(prev =>
        prev && getScheduleIdentifier(prev) === scheduleIdentifier ? null : prev
      );

      setIsEditModalOpen(false);
      setEditTaskDrafts([]);
      setTasksToDelete([]);
    } catch (err) {
      const detailed = extractErrorMessage(err);
      setEditFormError(`Failed to delete schedule: ${detailed}`);
    } finally {
      setIsDeletingSchedule(false);
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
        } catch (tokenErr) {}
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
        taskProgress: computeTaskProgress(task.estimatedHours, task.hoursSpent),
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
      const status = taskErr?.response?.status;
      const detailed = extractErrorMessage(taskErr);
      const authHint =
        status === 401 || status === 403
          ? ' Please make sure you are signed in with a role that can create tasks.'
          : '';
      setTaskFormError(`Failed to save tasks: ${detailed}.${authHint}`);
    } finally {
      setIsSavingTasks(false);
    }
  };

  const CustomToolbar = toolbarProps => {
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

    const today = new Date();

    return (
      <div className="schedule-toolbar">
        <div className="toolbar-left">
          <div className="toolbar-today">
            <button
              type="button"
              className="toolbar-button"
              onClick={() => toolbarProps.onNavigate('TODAY')}
            >
              Today
            </button>
            <span className="toolbar-today-label">
              Today: {formatDate(today, 'eee, MMM d')}
            </span>
          </div>
          <div className="toolbar-nav">
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
          </div>
          <div className="toolbar-views">
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
          </div>
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
        <div className="spinner" aria-label="Loading schedule" />
        <span>Loading schedule…</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-error">
        <div>{error}</div>
        <button type="button" onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="project-schedule-page">
      <div className="schedule-header">
        <div>
          <h1>Project Schedule</h1>
          <div className="schedule-subtitle">{projectName}</div>
        </div>
        <div className="schedule-actions">
          {role !== ROLES.CONTRACTOR && (
            <button
              type="button"
              className="primary"
              onClick={async () => {
                if (!lots.length && !lotsLoading) {
                  await loadLots();
                }
                setIsCreateModalOpen(true);
                setTaskDrafts([
                  buildEmptyTask(
                    newSchedule.scheduleStartDate,
                    newSchedule.scheduleEndDate
                  ),
                ]);
              }}
            >
              + New Work
            </button>
          )}
        </div>
      </div>

      <div className="schedule-body">
        <div className="calendar-section">
          <Calendar
            localizer={localizer}
            events={events}
            startAccessor="start"
            endAccessor="end"
            defaultDate={defaultDate}
            date={currentDate}
            view={currentView}
            onNavigate={date => setCurrentDate(date)}
            onView={view => setCurrentView(view)}
            selectable
            popup
            onSelectSlot={onSlotSelect}
            onSelectEvent={onEventClick}
            eventPropGetter={eventStyleGetter}
            components={{ toolbar: CustomToolbar }}
            style={{ minHeight: '70vh', height: '100%' }}
          />
        </div>

        <div className="schedule-list">
          <div className="list-title">Upcoming Work</div>
          {schedules.length === 0 && (
            <div className="empty">No schedules yet.</div>
          )}
          {schedules.map(schedule => {
            const start = parseDateSafe(schedule.scheduleStartDate);
            const end = parseDateSafe(schedule.scheduleEndDate);
            const isToday = start && isSameDay(start, new Date());
            const tasksArray = Array.isArray(schedule.tasks)
              ? schedule.tasks
              : Array.isArray(schedule.scheduleTasks)
                ? schedule.scheduleTasks
                : [];
            const taskCount = tasksArray.length;
            const lotLabel = schedule.lotId
              ? `Lot ${schedule.lotId}`
              : 'Lot unknown';
            return (
              <div
                key={getScheduleIdentifier(schedule)}
                className="schedule-card schedule-card-clickable"
                role="button"
                tabIndex={0}
                onClick={() => onScheduleCardClick(schedule)}
                onKeyDown={e => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onScheduleCardClick(schedule);
                  }
                }}
              >
                <div className="card-top">
                  <div>
                    <div className="card-title">
                      {schedule.scheduleDescription || 'Schedule'}
                    </div>
                    <div className="card-meta">
                      {lotLabel} ·{' '}
                      {start ? formatDate(start, 'MMM d, yyyy') : 'Start ?'}
                      {end ? ` → ${formatDate(end, 'MMM d, yyyy')}` : ''}
                      {isToday ? ' · Today' : ''}
                    </div>
                    <div className="card-meta">
                      <span className="card-chip">
                        {taskCount} task{taskCount === 1 ? '' : 's'}
                      </span>
                    </div>
                  </div>
                  {role !== ROLES.CONTRACTOR && (
                    <div className="card-actions">
                      <button
                        type="button"
                        className="ghost"
                        onClick={e => {
                          e.stopPropagation();
                          openEditScheduleModal(schedule);
                        }}
                      >
                        <FiEdit2 size={16} /> Edit
                      </button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <ScheduleFormModal
        title="Create Work"
        isOpen={isCreateModalOpen}
        schedule={newSchedule}
        onChange={setNewSchedule}
        onSubmit={handleCreateSchedule}
        onClose={closeCreateModal}
        lots={lotOptions}
        lotsLoading={lotsLoading}
        lotsError={lotsError}
        isSaving={isSaving}
        errorMessage={formError}
      />

      <EditScheduleModal
        isOpen={isEditModalOpen}
        schedule={editSchedule}
        onChange={setEditSchedule}
        onSubmit={handleUpdateSchedule}
        onClose={() => {
          setIsEditModalOpen(false);
          setEditFormError('');
        }}
        lots={lotOptions}
        lotsLoading={lotsLoading}
        lotsError={lotsError}
        isSaving={isSavingEdit}
        isDeleting={isDeletingSchedule}
        errorMessage={editFormError}
        taskDrafts={editTaskDrafts}
        onTaskChange={handleEditTaskChange}
        onAddTask={addEditTaskRow}
        onRemoveTask={removeEditTaskRow}
        onToggleTaskEdit={toggleEditTask}
        taskStatuses={TASK_STATUSES}
        taskPriorities={TASK_PRIORITIES}
        contractors={projectContractors}
        onDeleteSchedule={handleDeleteSchedule}
      />

      <TaskModal
        isOpen={isTaskModalOpen}
        schedule={scheduleForTasks}
        tasks={taskDrafts}
        statuses={TASK_STATUSES}
        priorities={TASK_PRIORITIES}
        contractors={projectContractors}
        errorMessage={taskFormError}
        isSaving={isSavingTasks}
        onClose={handleCloseTaskModal}
        onSave={handleSaveTasks}
        onTaskChange={handleTaskChange}
        onAddTask={addTaskRow}
        onRemoveTask={removeTaskRow}
      />

      <ScheduleDetailModal
        isOpen={isModalOpen}
        event={selectedEvent}
        onClose={() => setIsModalOpen(false)}
        onTaskNavigate={(path, navState) => navigate(path, navState)}
        returnPath={location.pathname}
        projectId={projectId}
        onEditSchedule={
          role === ROLES.CONTRACTOR ? undefined : openEditScheduleModal
        }
        formatDisplayRange={formatDisplayRange}
      />
    </div>
  );
};

export default ProjectSchedulePage;
