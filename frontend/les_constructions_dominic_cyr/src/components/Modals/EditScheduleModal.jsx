import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { FiEdit2 } from 'react-icons/fi';
import DeleteScheduleConfirmationModal from './DeleteScheduleConfirmationModal';

const EditScheduleModal = ({
  isOpen,
  schedule,
  onChange,
  onSubmit,
  onClose,
  lots,
  lotsLoading,
  lotsError,
  isSaving,
  isDeleting,
  errorMessage,
  taskDrafts,
  onTaskChange,
  onAddTask,
  onRemoveTask,
  onToggleTaskEdit,
  taskStatuses,
  taskPriorities,
  onDeleteSchedule,
}) => {
  const originalOverflow = useRef(null);
  const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);

  useEffect(() => {
    if (!isOpen) return undefined;

    if (originalOverflow.current === null) {
      originalOverflow.current = document.body.style.overflow || '';
    }
    document.body.style.overflow = 'hidden';

    return () => {
      if (originalOverflow.current !== null) {
        document.body.style.overflow = originalOverflow.current;
        originalOverflow.current = null;
      }
    };
  }, [isOpen]);

  const handleOverlayClick = e => {
    if (e.target === e.currentTarget && onClose) {
      onClose();
    }
  };

  const handleFieldChange = (field, value) => {
    onChange(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleDeleteClick = () => {
    if (isSaving || isDeleting || !onDeleteSchedule) return;
    setIsDeleteConfirmOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!onDeleteSchedule || isDeleting) return;
    await onDeleteSchedule();
    setIsDeleteConfirmOpen(false);
  };

  const handleCancelDelete = () => {
    if (isDeleting) return;
    setIsDeleteConfirmOpen(false);
  };

  if (!isOpen) return null;

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="schedule-modal edit-schedule-modal"
        onMouseDown={e => e.stopPropagation()}
      >
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">Edit Schedule</div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {errorMessage ? <div className="form-error">{errorMessage}</div> : null}

        <form className="create-schedule-form" onSubmit={onSubmit}>
          <div className="form-row">
            <label>
              <span>Schedule description</span>
              <input
                type="text"
                value={schedule.scheduleDescription}
                onChange={e =>
                  handleFieldChange('scheduleDescription', e.target.value)
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
                value={schedule.lotId}
                onChange={e => handleFieldChange('lotId', e.target.value)}
              >
                <option value="" disabled>
                  {lotsLoading ? 'Loading lots...' : 'Select a lot'}
                </option>
                {lots.map(lot => (
                  <option key={lot.value} value={lot.value}>
                    {lot.label}
                  </option>
                ))}
              </select>
              {lotsError && (
                <div className="form-error subtle">{lotsError}</div>
              )}
            </label>

            <label>
              <span>Start date</span>
              <input
                type="date"
                value={schedule.scheduleStartDate}
                onChange={e =>
                  handleFieldChange('scheduleStartDate', e.target.value)
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
                value={schedule.scheduleEndDate}
                onChange={e =>
                  handleFieldChange('scheduleEndDate', e.target.value)
                }
                required
              />
            </label>
          </div>

          <div className="tasks-header" style={{ marginTop: '6px' }}>
            <div>
              <div className="tasks-title">Tasks</div>
              <div className="tasks-subtitle">
                Edit, remove, or add tasks for this schedule.
              </div>
            </div>
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

          <div className="task-list">
            {taskDrafts.map((task, idx) => (
              <div key={`edit-task-${idx}`} className="task-row">
                <div className="task-row-header">
                  <span className="task-row-title">Task {idx + 1}</span>
                  <div className="task-row-actions">
                    <button
                      type="button"
                      className={`task-edit-button ${task.isEditable ? 'is-active' : ''}`}
                      onClick={() => onToggleTaskEdit(idx)}
                      aria-label={`Toggle edit for task ${idx + 1}`}
                    >
                      <FiEdit2 size={14} /> {task.isEditable ? 'Done' : 'Edit'}
                    </button>
                    <button
                      type="button"
                      className="task-remove-button"
                      onClick={() => onRemoveTask(idx)}
                      aria-label={`Remove task ${idx + 1}`}
                      disabled={false}
                    >
                      Remove
                    </button>
                  </div>
                </div>

                <div className="task-row-grid">
                  <label>
                    <span>Title</span>
                    <input
                      type="text"
                      value={task.taskTitle}
                      disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
                      onChange={e =>
                        onTaskChange(idx, 'taskStatus', e.target.value)
                      }
                    >
                      {taskStatuses.map(status => (
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
                      disabled={!task.isEditable}
                      onChange={e =>
                        onTaskChange(idx, 'taskPriority', e.target.value)
                      }
                    >
                      {taskPriorities.map(priority => (
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
                      min={schedule.scheduleStartDate}
                      max={schedule.scheduleEndDate}
                      disabled={!task.isEditable}
                      onChange={e =>
                        onTaskChange(idx, 'periodStart', e.target.value)
                      }
                    />
                  </label>

                  <label>
                    <span>Period end</span>
                    <input
                      type="date"
                      value={task.periodEnd}
                      min={schedule.scheduleStartDate}
                      max={schedule.scheduleEndDate}
                      disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
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
                    disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
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

          <div
            className={`form-actions ${onDeleteSchedule ? 'with-danger' : ''}`}
          >
            {onDeleteSchedule ? (
              <div className="form-actions-danger">
                <button
                  type="button"
                  className="modal-danger"
                  onClick={handleDeleteClick}
                  disabled={isSaving || isDeleting}
                >
                  {isDeleting ? 'Deleting…' : 'Delete schedule'}
                </button>
              </div>
            ) : null}
            <div className="form-actions-main">
              <button
                type="button"
                className="modal-secondary"
                onClick={onClose}
                disabled={isSaving}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="modal-primary"
                disabled={isSaving}
              >
                {isSaving ? 'Saving…' : 'Save changes'}
              </button>
            </div>
          </div>
        </form>
      </div>

      <DeleteScheduleConfirmationModal
        isOpen={isDeleteConfirmOpen}
        onCancel={handleCancelDelete}
        onConfirm={handleConfirmDelete}
        isDeleting={isDeleting}
      />
    </div>
  );
};

export default EditScheduleModal;

EditScheduleModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  schedule: PropTypes.shape({
    scheduleDescription: PropTypes.string.isRequired,
    lotId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    scheduleStartDate: PropTypes.string.isRequired,
    scheduleEndDate: PropTypes.string.isRequired,
    scheduleIdentifier: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
      PropTypes.null,
    ]),
  }).isRequired,
  onChange: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  lots: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.any.isRequired,
      label: PropTypes.string.isRequired,
    })
  ).isRequired,
  lotsLoading: PropTypes.bool,
  lotsError: PropTypes.string,
  isSaving: PropTypes.bool,
  errorMessage: PropTypes.string,
  isDeleting: PropTypes.bool,
  taskDrafts: PropTypes.arrayOf(PropTypes.object).isRequired,
  onTaskChange: PropTypes.func.isRequired,
  onAddTask: PropTypes.func.isRequired,
  onRemoveTask: PropTypes.func.isRequired,
  onToggleTaskEdit: PropTypes.func.isRequired,
  taskStatuses: PropTypes.arrayOf(PropTypes.string).isRequired,
  taskPriorities: PropTypes.arrayOf(PropTypes.string).isRequired,
  onDeleteSchedule: PropTypes.func,
};

EditScheduleModal.defaultProps = {
  lotsLoading: false,
  lotsError: '',
  isSaving: false,
  isDeleting: false,
  onDeleteSchedule: null,
  errorMessage: '',
};
