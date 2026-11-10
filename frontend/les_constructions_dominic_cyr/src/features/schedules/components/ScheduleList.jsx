import React, { useState } from 'react';
import PropTypes from 'prop-types';

const ScheduleList = ({
  schedules,
  loading,
  error,
  loadingMessage = 'Loading schedules...',
  emptyMessage = 'No schedules available',
  upcomingWorkLabel = 'Upcoming work',
  lotFilterLabel = 'Filter by lot',
  allLotsLabel = 'All lots',
  dateDueLabel = 'Due',
  lotLabel = 'Lot',
  locale = 'en-US',
}) => {
  const [selectedLot, setSelectedLot] = useState('all');
  const formatDate = dateString => {
    const date = new Date(dateString);
    return date.toLocaleDateString(locale, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="schedule-loading">
        <p>{loadingMessage}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="schedule-error">
        <p className="error">{error}</p>
      </div>
    );
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const upcomingTasks = schedules
    .flatMap(schedule =>
      (schedule.tasks || []).map(task => ({
        ...task,
        scheduleIdentifier: schedule.scheduleIdentifier,
        projectIdentifier: task.projectIdentifier || schedule.projectIdentifier,
        lotDatabaseId: task.lotDatabaseId || schedule.lotDatabaseId,
        projectName: task.projectName || schedule.projectName,
      }))
    )
    .filter(task => task.periodStart && new Date(task.periodStart) >= today)
    .sort((first, second) => {
      return new Date(first.periodStart) - new Date(second.periodStart);
    });
  const lotOptions = Array.from(
    new Set([
      ...schedules.map(schedule => schedule.lotDatabaseId).filter(Boolean),
      ...upcomingTasks.map(task => task.lotDatabaseId).filter(Boolean),
    ])
  ).sort((first, second) => Number(first) - Number(second));
  const visibleTasks = upcomingTasks
    .filter(
      task =>
        selectedLot === 'all' ||
        String(task.lotDatabaseId) === String(selectedLot)
    )
    .slice(0, 5);

  if (schedules.length === 0 || visibleTasks.length === 0) {
    return (
      <div className="schedule-empty">
        <p>{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="schedule-list">
      <div className="upcoming-work-toolbar">
        <strong>{upcomingWorkLabel}</strong>
        {lotOptions.length > 1 && (
          <label>
            <span>{lotFilterLabel}</span>
            <select
              value={selectedLot}
              onChange={event => setSelectedLot(event.target.value)}
            >
              <option value="all">{allLotsLabel}</option>
              {lotOptions.map(lotId => (
                <option key={lotId} value={String(lotId)}>
                  {lotLabel} {lotId}
                </option>
              ))}
            </select>
          </label>
        )}
      </div>
      {visibleTasks.map(task => (
        <div
          key={`${task.scheduleIdentifier}-${task.taskId}`}
          className="schedule-item"
        >
          <span className="schedule-date">
            {dateDueLabel}: {formatDate(task.periodEnd || task.periodStart)}
          </span>
          <span className="schedule-separator"> - </span>
          <span className="schedule-task">
            {task.taskTitle || task.taskDescription}
          </span>
          <span className="schedule-separator">, </span>
          <span className="schedule-lot">
            {lotLabel} {task.lotDatabaseId || '—'}
          </span>
        </div>
      ))}
    </div>
  );
};

export default ScheduleList;

ScheduleList.propTypes = {
  schedules: PropTypes.arrayOf(
    PropTypes.shape({
      scheduleIdentifier: PropTypes.string,
      scheduleStartDate: PropTypes.string,
      scheduleDescription: PropTypes.string,
      lotDatabaseId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    })
  ).isRequired,
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  loadingMessage: PropTypes.string,
  emptyMessage: PropTypes.string,
  upcomingWorkLabel: PropTypes.string,
  lotFilterLabel: PropTypes.string,
  allLotsLabel: PropTypes.string,
  dateDueLabel: PropTypes.string,
  lotLabel: PropTypes.string,
  locale: PropTypes.string,
};
