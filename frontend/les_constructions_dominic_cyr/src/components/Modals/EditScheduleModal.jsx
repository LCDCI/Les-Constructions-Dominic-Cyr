import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { FiEdit2 } from 'react-icons/fi';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import DeleteScheduleConfirmationModal from './DeleteScheduleConfirmationModal';

const EditScheduleModal = ({
  isOpen,
  schedule,
  onChange,
  onSubmit,
  onClose,
  lots,
  lotsLoading = false,
  lotsError = '',
  isSaving = false,
  isDeleting = false,
  errorMessage = '',
  taskDrafts,
  onTaskChange,
  onAddTask,
  onRemoveTask,
  onToggleTaskEdit,
  taskStatuses,
  contractors,
  onDeleteSchedule = null,
}) => {
  const { t } = usePageTranslations('projectSchedulePage');
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

  const handleFormSubmit = async e => {
    e.preventDefault();
    if (isSaving || isDeleting) return;

    try {
      const result = await onSubmit?.(e);
      if (result !== false) {
        onClose?.();
      }
      // eslint-disable-next-line no-empty
    } catch (err) {}
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
          <div className="schedule-modal-title">
            {t('editForm.title', 'Edit work')}
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

        {errorMessage ? <div className="form-error">{errorMessage}</div> : null}

        <form className="create-schedule-form" onSubmit={handleFormSubmit}>
          <div className="form-row">
            <label>
              <span>{t('form.workDescription', 'Work description')}</span>
              <input
                type="text"
                value={schedule.scheduleDescription}
                onChange={e =>
                  handleFieldChange('scheduleDescription', e.target.value)
                }
                placeholder={t(
                  'form.descriptionPlaceholder',
                  'Foundation pour, framing, etc.'
                )}
                required
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              <span>{t('form.lot', 'Lot')}</span>
              <select
                value={schedule.lotId}
                onChange={e => handleFieldChange('lotId', e.target.value)}
              >
                <option value="" disabled>
                  {lotsLoading
                    ? t('form.loadingLots', 'Loading lots...')
                    : t('form.selectLot', 'Select a lot')}
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
          </div>

          <div className="form-row two-col">
            <label>
              <span>{t('form.startDate', 'Start date')}</span>
              <input
                type="date"
                value={schedule.scheduleStartDate}
                onChange={e =>
                  handleFieldChange('scheduleStartDate', e.target.value)
                }
                required
              />
            </label>

            <label>
              <span>{t('form.endDate', 'End date')}</span>
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
              <div className="tasks-title">{t('taskForm.tasks', 'Tasks')}</div>
              <div className="tasks-subtitle">
                {t(
                  'editForm.tasksNote',
                  'Edit, remove, or add tasks for this work.'
                )}
              </div>
            </div>
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

          <div className="task-list">
            {taskDrafts.map((task, idx) => (
              <div key={`edit-task-${idx}`} className="task-row">
                <div className="task-row-header">
                  <span className="task-row-title">
                    {t('taskForm.taskNumber', {
                      defaultValue: 'Task {{number}}',
                      number: idx + 1,
                    })}
                  </span>
                  <div className="task-row-actions">
                    <button
                      type="button"
                      className={`task-edit-button ${task.isEditable ? 'is-active' : ''}`}
                      onClick={() => onToggleTaskEdit(idx)}
                      aria-label={t('editForm.toggleTask', {
                        defaultValue: 'Toggle edit for task {{number}}',
                        number: idx + 1,
                      })}
                    >
                      <FiEdit2 size={14} />{' '}
                      {task.isEditable
                        ? t('editForm.done', 'Done')
                        : t('editForm.edit', 'Edit')}
                    </button>
                    <button
                      type="button"
                      className="task-remove-button"
                      onClick={() => onRemoveTask(idx)}
                      aria-label={t('taskForm.removeTask', {
                        defaultValue: 'Remove task {{number}}',
                        number: idx + 1,
                      })}
                      disabled={false}
                    >
                      {t('taskForm.remove', 'Remove')}
                    </button>
                  </div>
                </div>

                <div className="task-row-grid">
                  <label>
                    <span>{t('taskForm.taskTitle', 'Title')}</span>
                    <input
                      type="text"
                      value={task.taskTitle}
                      disabled={!task.isEditable}
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
                      disabled={!task.isEditable}
                      onChange={e =>
                        onTaskChange(idx, 'taskStatus', e.target.value)
                      }
                    >
                      {taskStatuses.map(status => (
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
                    <span>
                      {t('taskForm.taskStartDate', 'Task Start Date')}
                    </span>
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
                    <span>{t('taskForm.taskEndDate', 'Task End Date')}</span>
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
                    <span>{t('taskForm.assignee', 'Assignee')}</span>
                    <select
                      value={task.assignedToUserId}
                      disabled={!task.isEditable}
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
                    disabled={!task.isEditable}
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
                  {isDeleting
                    ? t('deleteModal.deleting', 'Deleting…')
                    : t('deleteModal.deleteWork', 'Delete work')}
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
                {t('form.cancel', 'Cancel')}
              </button>
              <button
                type="button"
                className="modal-primary"
                onClick={handleFormSubmit}
                disabled={isSaving}
              >
                {isSaving
                  ? t('form.saving', 'Saving…')
                  : t('editForm.saveChanges', 'Save changes')}
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
      PropTypes.oneOf([null]),
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
  contractors: PropTypes.arrayOf(
    PropTypes.shape({
      userId: PropTypes.string,
      userIdentifier: PropTypes.string,
      firstName: PropTypes.string.isRequired,
      lastName: PropTypes.string.isRequired,
    })
  ),
  onDeleteSchedule: PropTypes.func,
};
