/* eslint-disable react/prop-types */
import React, { useEffect, useRef } from 'react';
import { FiEdit2 } from 'react-icons/fi';

const ScheduleDetailModal = ({
  isOpen,
  event,
  onClose,
  onTaskNavigate,
  returnPath,
  onEditSchedule,
  formatDisplayRange,
}) => {
  const shouldRender = isOpen && event;

  const tasks = Array.isArray(event?.tasks) ? event.tasks : null;
  const handleTaskClick = (path, task) => {
    if (!path || !onTaskNavigate) return;
    const scheduleEventId =
      event?.id || event?.scheduleId || event?.scheduleIdentifier;

    onTaskNavigate(path, {
      state: {
        fromScheduleModal: true,
        scheduleEventId,
        returnTo: returnPath,
        task,
      },
    });
  };

  const originalOverflow = useRef(null);
  const didLockBody = useRef(false);

  useEffect(() => {
    // Lock body scroll only if it wasn't already locked elsewhere.
    if (isOpen) {
      const current = document.body.style.overflow || '';
      if (current !== 'hidden') {
        originalOverflow.current = current;
        document.body.style.overflow = 'hidden';
        didLockBody.current = true;
      } else {
        // Already locked by another modal; don't record/restore.
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
  }, [isOpen]);

  const handleOverlayClick = e => {
    if (e.target === e.currentTarget && onClose) {
      onClose();
    }
  };

  if (!shouldRender) return null;

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div className="schedule-modal" onMouseDown={e => e.stopPropagation()}>
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">{event.title}</div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <div className="schedule-modal-section">
          <h4>Date</h4>
          <div>{formatDisplayRange(event.start, event.end)}</div>
        </div>

        {event.description && (
          <div className="schedule-modal-section">
            <h4>Description</h4>
            <div>{event.description}</div>
          </div>
        )}

        {tasks && (
          <div className="schedule-modal-section schedule-modal-tasks">
            <h4>Tasks</h4>
            {event.tasksLoading ? (
              <div>Loading tasks…</div>
            ) : event.tasksError ? (
              <div>{event.tasksError}</div>
            ) : tasks.length === 0 ? (
              <div>None listed</div>
            ) : (
              <ul className="schedule-task-list">
                {tasks.map((task, idx) => {
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
                  const isClickable = Boolean(taskPath && onTaskNavigate);

                  return (
                    <li key={taskId} className="schedule-task-item">
                      <button
                        type="button"
                        className="schedule-task-card"
                        onClick={() =>
                          isClickable ? handleTaskClick(taskPath, task) : null
                        }
                        disabled={!isClickable}
                        aria-disabled={!isClickable}
                      >
                        <div className="schedule-task-copy">
                          <div className="schedule-task-title">{title}</div>
                          <div className="schedule-task-meta">ID: {taskId}</div>
                        </div>
                        <div className="schedule-task-status">
                          <span className="task-chip task-chip-inline">
                            {status}
                          </span>
                        </div>
                      </button>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        )}

        <div className="schedule-modal-actions">
          <button
            type="button"
            className="modal-primary"
            onClick={() => onEditSchedule?.(event)}
          >
            <FiEdit2 aria-hidden="true" style={{ marginRight: 6 }} />
            Edit Work
          </button>
        </div>
      </div>
    </div>
  );
};

export default ScheduleDetailModal;
