/* eslint-disable react/prop-types */
import React from 'react';

const TaskModal = ({
  isOpen,
  schedule,
  tasks,
  statuses,
  priorities,
  errorMessage,
  isSaving,
  onClose,
  onSave,
  onTaskChange,
  onAddTask,
  onRemoveTask,
}) => {
  if (!isOpen || !schedule) return null;

  const handleOverlayMouseDown = event => {
    if (event.target === event.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayMouseDown}
    >
      <div
        className="schedule-modal tasks-modal"
        onMouseDown={e => e.stopPropagation()}
      >
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">
            Add tasks for {schedule.scheduleDescription}
          </div>
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
          <h4>Work window</h4>
          <div>
            {schedule.scheduleStartDate} → {schedule.scheduleEndDate}
            {schedule.lotNumber && ` · Lot ${schedule.lotNumber}`}
          </div>
          <div className="tasks-subtitle">
            Task dates must stay within this window.
          </div>
        </div>

        <div className="task-list">
          {tasks?.map((task, idx) => (
            <div key={`task-${idx}`} className="task-row">
              <div className="task-row-header">
                <span className="task-row-title">Task {idx + 1}</span>
                <button
                  type="button"
                  className="task-remove-button"
                  onClick={() => onRemoveTask(idx)}
                  aria-label={`Remove task ${idx + 1}`}
                  disabled={tasks.length === 1}
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
                      onTaskChange(idx, 'taskTitle', e.target.value)
                    }
                    placeholder={`Task ${idx + 1} title`}
                  />
                </label>

                <label>
                  <span>Status</span>
                  <select
                    value={task.taskStatus}
                    onChange={e =>
                      onTaskChange(idx, 'taskStatus', e.target.value)
                    }
                  >
                    {statuses.map(status => (
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
                      onTaskChange(idx, 'taskPriority', e.target.value)
                    }
                  >
                    {priorities.map(priority => (
                      <option key={priority} value={priority}>
                        {priority.replaceAll('_', ' ')}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="task-row-grid">
                <label>
                  <span>Task Start Date</span>
                  <input
                    type="date"
                    value={task.periodStart}
                    min={schedule.scheduleStartDate}
                    max={schedule.scheduleEndDate}
                    onChange={e =>
                      onTaskChange(idx, 'periodStart', e.target.value)
                    }
                  />
                </label>

                <label>
                  <span>Task End Date</span>
                  <input
                    type="date"
                    value={task.periodEnd}
                    min={schedule.scheduleStartDate}
                    max={schedule.scheduleEndDate}
                    onChange={e =>
                      onTaskChange(idx, 'periodEnd', e.target.value)
                    }
                  />
                </label>

                <label>
                  <span>Assignee (user UUID)</span>
                  <input
                    type="text"
                    value={task.assignedToUserId}
                    onChange={e =>
                      onTaskChange(idx, 'assignedToUserId', e.target.value)
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
                    onTaskChange(idx, 'taskDescription', e.target.value)
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
                      onTaskChange(idx, 'estimatedHours', e.target.value)
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
                      onTaskChange(idx, 'hoursSpent', e.target.value)
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
                      onTaskChange(idx, 'taskProgress', e.target.value)
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
              onAddTask(schedule.scheduleStartDate, schedule.scheduleEndDate)
            }
          >
            + Add task
          </button>
        </div>

        {errorMessage && <div className="form-error">{errorMessage}</div>}

        <div className="form-actions">
          <button
            type="button"
            className="modal-secondary"
            onClick={onClose}
            disabled={isSaving}
          >
            Skip for now
          </button>
          <button
            type="button"
            className="modal-primary"
            onClick={onSave}
            disabled={isSaving}
          >
            {isSaving ? 'Saving tasks…' : 'Save tasks'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaskModal;
