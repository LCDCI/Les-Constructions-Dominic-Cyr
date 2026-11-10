import React from 'react';
import PropTypes from 'prop-types';

const ScheduleList = ({
  schedules,
  loading,
  error,
  loadingMessage = 'Loading schedules...',
  emptyMessage = 'No schedules available',
}) => {
  const formatDate = dateString => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      weekday: 'long',
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

  if (schedules.length === 0) {
    return (
      <div className="schedule-empty">
        <p>{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="schedule-list">
      {schedules.map(schedule => (
        <div key={schedule.scheduleIdentifier} className="schedule-item">
          <span className="schedule-date">
            {formatDate(schedule.scheduleStartDate)}
          </span>
          <span className="schedule-separator"> - </span>
          <span className="schedule-task">{schedule.scheduleDescription}</span>
          <span className="schedule-separator">, </span>
          <span className="schedule-lot">{schedule.lotNumber}</span>
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
      lotNumber: PropTypes.string,
    })
  ).isRequired,
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  loadingMessage: PropTypes.string,
  emptyMessage: PropTypes.string,
};
