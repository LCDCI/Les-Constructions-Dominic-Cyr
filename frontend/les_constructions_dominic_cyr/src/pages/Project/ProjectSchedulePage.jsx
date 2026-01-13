import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { Scheduler, SchedulerData, ViewType } from 'react-big-schedule';
import 'react-big-schedule/dist/css/style.css';
import { projectScheduleApi } from '../../features/schedules/api/projectScheduleApi';
import '../../styles/Project/ProjectSchedule.css';

const ProjectSchedulePage = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const [schedulerData, setSchedulerData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [projectName, setProjectName] = useState('');

  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        // Wait for Auth0 to finish loading before deciding on token
        if (isLoading) return;

        setLoading(true);
        
        // Get token if authenticated
        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: { audience: import.meta.env.VITE_AUTH0_AUDIENCE },
            });
          } catch (tokenErr) {
            console.warn('Could not get token, proceeding without auth');
          }
        }
        
        const schedules = await projectScheduleApi.getProjectSchedules(projectId, token);
        
        if (!schedules || schedules.length === 0) {
          setError('No schedules found for this project.');
          setLoading(false);
          return;
        }

        // Initialize scheduler with week view
        const scheduler = new SchedulerData(
          new Date(),
          ViewType.Week,
          false,
          false,
          {
            schedulerWidth: '100%',
            besidesWidth: 20,
            schedulerMaxHeight: 600,
            tableHeaderHeight: 40,
            views: [
              {
                viewName: 'Week',
                viewType: ViewType.Week,
                showAgenda: false,
                isEventPerspective: false,
              },
              {
                viewName: 'Month',
                viewType: ViewType.Month,
                showAgenda: false,
                isEventPerspective: false,
              },
              {
                viewName: 'Day',
                viewType: ViewType.Day,
                showAgenda: false,
                isEventPerspective: false,
              },
            ],
          }
        );

        // Convert schedules to events format for react-big-schedule
        const resources = [
          {
            id: 'r1',
            name: 'Construction Tasks',
          },
        ];

        const events = schedules.map((schedule, index) => ({
          id: index + 1,
          start: schedule.taskDate,
          end: schedule.taskDate,
          resourceId: 'r1',
          title: schedule.taskDescription,
          bgColor: '#3174ad',
          description: `${schedule.lotNumber} - ${schedule.dayOfWeek}`,
        }));

        scheduler.setResources(resources);
        scheduler.setEvents(events);

        setSchedulerData(scheduler);
        setProjectName(schedules[0]?.projectName || 'Project');
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
  }, [projectId, isAuthenticated, isLoading, getAccessTokenSilently]);

  const prevClick = schedulerData => {
    schedulerData.prev();
    setSchedulerData(schedulerData);
  };

  const nextClick = schedulerData => {
    schedulerData.next();
    setSchedulerData(schedulerData);
  };

  const onViewChange = (schedulerData, view) => {
    schedulerData.setViewType(
      view.viewType,
      view.showAgenda,
      view.isEventPerspective
    );
    setSchedulerData(schedulerData);
  };

  const onSelectDate = (schedulerData, date) => {
    schedulerData.setDate(date);
    setSchedulerData(schedulerData);
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

  if (!schedulerData) {
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

      <div className="schedule-container">
        <Scheduler
          schedulerData={schedulerData}
          prevClick={prevClick}
          nextClick={nextClick}
          onViewChange={onViewChange}
          onSelectDate={onSelectDate}
        />
      </div>
    </div>
  );
};

export default ProjectSchedulePage;
