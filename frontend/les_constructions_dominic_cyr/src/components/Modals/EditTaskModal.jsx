import React, { useEffect, useRef } from 'react';

// Modal for editing a single task, sharing the same layout conventions as other schedule modals.
const EditTaskModal = ({
  isOpen,
  task,
  statuses,
  priorities,
  errorMessage,
  isSaving,
  onClose,
  onChange,
  onSave,
  scheduleWindow,
}) => {
  const originalOverflow = useRef(null);

  useEffect(() => {
    if (isOpen) {
      if (originalOverflow.current === null) {
        originalOverflow.current = document.body.style.overflow || '';
      }
      document.body.style.overflow = 'hidden';
    } else if (originalOverflow.current !== null) {
      document.body.style.overflow = originalOverflow.current;
      originalOverflow.current = null;
    }

    return () => {
      if (originalOverflow.current !== null) {
        document.body.style.overflow = originalOverflow.current;
        originalOverflow.current = null;
      }
    };
  }, [isOpen]);

  if (!isOpen || !task) return null;

  const handleOverlayMouseDown = event => {
    if (event.target === event.currentTarget) {
      onClose();
    }
  };

  const handleField = (field, value) => {
    onChange(prev => ({ ...prev, [field]: value }));
  };

  const minDate = scheduleWindow?.start || undefined;
  const maxDate = scheduleWindow?.end || undefined;

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
          <div className="schedule-modal-title">Edit Task</div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {scheduleWindow?.start && scheduleWindow?.end && (
          <div className="schedule-modal-section">
            <h4>Work window</h4>
            <div>
              {scheduleWindow.start} → {scheduleWindow.end}
            </div>
            <div className="tasks-subtitle">
              Task dates must stay within this window.
            </div>
          </div>
        )}

        <div className="task-row single-task-row">
          <div className="task-row-grid">
            <label>
              <span>Title</span>
              <input
                type="text"
                value={task.taskTitle}
                onChange={e => handleField('taskTitle', e.target.value)}
                placeholder="Task title"
              />
            </label>

            <label>
              <span>Status</span>
              <select
                value={task.taskStatus}
                onChange={e => handleField('taskStatus', e.target.value)}
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
                onChange={e => handleField('taskPriority', e.target.value)}
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
                min={minDate}
                max={maxDate}
                onChange={e => handleField('periodStart', e.target.value)}
              />
            </label>

            <label>
              <span>Task End Date</span>
              <input
                type="date"
                value={task.periodEnd}
                min={minDate}
                max={maxDate}
                onChange={e => handleField('periodEnd', e.target.value)}
              />
            </label>

            <label>
              <span>Assignee (user UUID)</span>
              <input
                type="text"
                value={task.assignedToUserId}
                onChange={e => handleField('assignedToUserId', e.target.value)}
                placeholder="Optional"
              />
            </label>
          </div>

          <label className="task-row-full">
            <span>Description</span>
            <textarea
              value={task.taskDescription}
              onChange={e => handleField('taskDescription', e.target.value)}
              placeholder="Describe the work to be completed"
              rows={3}
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
                onChange={e => handleField('estimatedHours', e.target.value)}
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
                onChange={e => handleField('hoursSpent', e.target.value)}
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
                onChange={e => handleField('taskProgress', e.target.value)}
                placeholder="Optional"
              />
            </label>
          </div>
        </div>

        {errorMessage && <div className="form-error">{errorMessage}</div>}

        <div className="form-actions">
          <button
            type="button"
            className="modal-secondary"
            onClick={onClose}
            disabled={isSaving}
          >
            Cancel
          </button>
          <button
            type="button"
            className="modal-primary"
            onClick={onSave}
            disabled={isSaving}
          >
            {isSaving ? 'Saving…' : 'Save changes'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EditTaskModal;
