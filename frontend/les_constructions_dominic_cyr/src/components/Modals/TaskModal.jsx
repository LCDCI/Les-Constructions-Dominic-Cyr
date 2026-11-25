/* eslint-disable react/prop-types */
import React from 'react';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const TaskModal = ({
  isOpen,
  schedule,
  tasks,
  statuses,
  contractors,
  errorMessage,
  isSaving,
  onClose,
  onSave,
  onTaskChange,
  onAddTask,
  onRemoveTask,
}) => {
  const { t, currentLanguage } = usePageTranslations('projectSchedulePage');
  if (!isOpen || !schedule) return null;
  const locale = currentLanguage === 'fr' ? 'fr-CA' : 'en-US';
  const formatDate = value =>
    value
      ? new Date(`${value}T00:00:00`).toLocaleDateString(locale, {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
        })
      : '';

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
            {t('taskForm.title', {
              defaultValue: 'Add tasks for {{work}}',
              work: schedule.scheduleDescription,
            })}
          </div>
          <button
            type="button"
            className="modal-close"
            aria-label={t('modal.close', 'Close')}
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <div className="schedule-modal-section">
          <h4>{t('taskForm.workWindow', 'Work window')}</h4>
          <div>
            {formatDate(schedule.scheduleStartDate)} →{' '}
            {formatDate(schedule.scheduleEndDate)}
            {schedule.lotNumber &&
              ` · ${t('lot', 'Lot')} ${schedule.lotNumber}`}
          </div>
          <div className="tasks-subtitle">
            {t(
              'taskForm.dateConstraint',
              'Task dates must stay within this window.'
            )}
          </div>
        </div>

        <div className="task-list">
          {tasks?.map((task, idx) => (
            <div key={`task-${idx}`} className="task-row">
              <div className="task-row-header">
                <span className="task-row-title">
                  {t('taskForm.taskNumber', {
                    defaultValue: 'Task {{number}}',
                    number: idx + 1,
                  })}
                </span>
                <button
                  type="button"
                  className="task-remove-button"
                  onClick={() => onRemoveTask(idx)}
                  aria-label={t('taskForm.removeTask', {
                    defaultValue: 'Remove task {{number}}',
                    number: idx + 1,
                  })}
                  disabled={tasks.length === 1}
                >
                  {t('taskForm.remove', 'Remove')}
                </button>
              </div>

              <div className="task-row-grid">
                <label>
                  <span>{t('taskForm.taskTitle', 'Title')}</span>
                  <input
                    type="text"
                    value={task.taskTitle}
                    onChange={e =>
                      onTaskChange(idx, 'taskTitle', e.target.value)
                    }
                    placeholder={t('taskForm.taskTitlePlaceholder', {
                      number: idx + 1,
                    })}
                  />
                </label>

                <label>
                  <span>{t('taskForm.status', 'Status')}</span>
                  <select
                    value={task.taskStatus}
                    onChange={e =>
                      onTaskChange(idx, 'taskStatus', e.target.value)
                    }
                  >
                    {statuses.map(status => (
                      <option key={status} value={status}>
                        {t(
                          `statusValues.${status}`,
                          status.replaceAll('_', ' ')
                        )}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="task-row-grid">
                <label>
                  <span>{t('taskForm.taskStartDate', 'Task Start Date')}</span>
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
                  <span>{t('taskForm.taskEndDate', 'Task End Date')}</span>
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
                  <span>{t('taskForm.assignee', 'Assignee')}</span>
                  <select
                    value={task.assignedToUserId}
                    onChange={e =>
                      onTaskChange(idx, 'assignedToUserId', e.target.value)
                    }
                  >
                    <option value="">
                      {t('taskForm.unassigned', 'Unassigned')}
                    </option>
                    {contractors?.map(contractor => (
                      <option
                        key={contractor.userId || contractor.userIdentifier}
                        value={contractor.userId || contractor.userIdentifier}
                      >
                        {contractor.firstName} {contractor.lastName}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <label className="task-row-full">
                <span>{t('taskForm.description', 'Description')}</span>
                <textarea
                  value={task.taskDescription}
                  onChange={e =>
                    onTaskChange(idx, 'taskDescription', e.target.value)
                  }
                  placeholder={t(
                    'taskForm.descriptionPlaceholder',
                    'Describe the work to be completed'
                  )}
                  rows={2}
                />
              </label>
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
            {t('taskForm.addTask', '+ Add task')}
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
            {t('taskForm.skip', 'Skip for now')}
          </button>
          <button
            type="button"
            className="modal-primary"
            onClick={onSave}
            disabled={isSaving}
          >
            {isSaving
              ? t('taskForm.saving', 'Saving tasks…')
              : t('taskForm.save', 'Save tasks')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaskModal;
