import React from 'react';

const ScheduleList = ({ schedules, loading, error }) => {
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      month: 'long', 
      day: 'numeric', 
      weekday: 'long' 
    });
  };

  if (loading) {
    return (
      <div className="schedule-loading">
        <p>Loading schedules...</p>
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
        <p>No schedules for this week</p>
      </div>
    );
  }

  return (
    <div className="schedule-list">
      {schedules.map((schedule) => (
        <div key={schedule.scheduleIdentifier} className="schedule-item">
          <span className="schedule-date">{formatDate(schedule.taskDate)}</span>
          <span className="schedule-separator"> - </span>
          <span className="schedule-task">{schedule.taskDescription}</span>
          <span className="schedule-separator">, </span>
          <span className="schedule-lot">{schedule.lotNumber}</span>
        </div>
      ))}
    </div>
  );
};

export default ScheduleList;