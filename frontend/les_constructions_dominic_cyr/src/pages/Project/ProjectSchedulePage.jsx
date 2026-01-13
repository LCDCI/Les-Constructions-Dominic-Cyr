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
} from 'date-fns';
import enUS from 'date-fns/locale/en-US';

import 'react-big-calendar/lib/css/react-big-calendar.css';
import { projectScheduleApi } from '../../features/schedules/api/projectScheduleApi';
import '../../styles/Project/ProjectSchedule.css';

const ProjectSchedulePage = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [projectName, setProjectName] = useState('');
  const [schedules, setSchedules] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [defaultDate, setDefaultDate] = useState(new Date());

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
          setError('No schedules found for this project.');
          setLoading(false);
          return;
        }

        const scheduleWithIds = scheduleResponse.map((schedule, index) => {
          const tasks = Array.isArray(schedule.scheduleTasks)
            ? schedule.scheduleTasks
            : Array.isArray(schedule.tasks)
              ? schedule.tasks
              : [];

          return {
            ...schedule,
            id: index + 1,
            tasks,
          };
        });

        const mappedEvents = scheduleWithIds.map(schedule => ({
          id: schedule.id,
          title: schedule.scheduleDescription,
          start: toDate(schedule.scheduleStartDate, 8),
          end: toDate(schedule.scheduleEndDate, 17),
          lot: schedule.lotNumber,
          projectName: schedule.projectName,
          tasks: schedule.tasks,
        }));

        const firstDate = mappedEvents[0]?.start ?? new Date();
        setDefaultDate(firstDate);

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

  const onEventClick = event => {
    setSelectedEvent(event);
  };

  const onSlotSelect = slotInfo => {
    setSelectedEvent({
      id: `${slotInfo.start.toISOString()}`,
      title: 'New Task Placeholder',
      description: `Selected ${slotInfo.start.toDateString()}`,
      start: slotInfo.start,
      end: slotInfo.end,
      tasks: [],
    });
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

  if (events.length === 0) {
    return null;
  }

  return (
    <div className="project-schedule-page">
      <div className="schedule-header">
        <h1>Project Schedule: {projectName}</h1>
        <button onClick={() => navigate(-1)} className="back-button-small">
          ← Back
        </button>
      </div>

      <div className="schedule-layout">
        <div className="schedule-panel">
          <Calendar
            localizer={localizer}
            events={events}
            views={[Views.DAY, Views.WEEK, Views.MONTH]}
            defaultView={Views.WEEK}
            defaultDate={defaultDate}
            startAccessor="start"
            endAccessor="end"
            style={{ height: 680 }}
            popup={false}
            onSelectEvent={onEventClick}
            selectable
            onSelectSlot={onSlotSelect}
            eventPropGetter={eventStyleGetter}
          />
        </div>

        <div className="schedule-side">
          <h2>Schedules</h2>
          {selectedEvent && (
            <div className="schedule-selected">
              <h3>Selected</h3>
              <div className="schedule-selected-title">
                {selectedEvent.title}
              </div>

              {selectedEvent.description && (
                <div className="schedule-selected-meta">
                  {selectedEvent.description}
                </div>
              )}
              <div className="schedule-selected-dates">
                <span>
                  {formatDisplayRange(selectedEvent.start, selectedEvent.end)}
                </span>
              </div>
              {Array.isArray(selectedEvent.tasks) && (
                <div className="schedule-selected-tasks">
                  <h4>Tasks</h4>
                  {selectedEvent.tasks.length === 0 ? (
                    <div className="schedule-selected-meta">
                      No tasks linked
                    </div>
                  ) : (
                    <ul>
                      {selectedEvent.tasks.map((task, idx) => {
                        const taskId =
                          task.taskId ??
                          task.id ??
                          task.identifier ??
                          `task-${idx + 1}`;
                        const label =
                          task.taskDescription ??
                          task.description ??
                          task.name ??
                          JSON.stringify(task);
                        return (
                          <li key={taskId}>
                            <strong>{taskId}:</strong> {label}
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </div>
              )}
            </div>
          )}

          <div className="schedule-list-items">
            {schedules.map(item => (
              <div
                key={
                  item.id ??
                  `${item.scheduleStartDate}-${item.scheduleDescription}`
                }
                className="schedule-card"
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
      </div>
    </div>
  );
};

export default ProjectSchedulePage;
