import React, { useEffect, useRef } from 'react';
import { usePageTranslations } from '../../hooks/usePageTranslations';

// Modal for editing a single task, sharing the same layout conventions as other schedule modals.
const EditTaskModal = ({
  isOpen,
  task,
  statuses,
  contractors,
  errorMessage,
  isSaving,
  onClose,
  onChange,
  onSave,
  scheduleWindow,
}) => {
  const { t } = usePageTranslations('taskDetails');
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
          <div className="schedule-modal-title">
            {t('editTask', 'Edit Task')}
          </div>
          <button
            type="button"
            className="modal-close"
            aria-label={t('editModal.close', 'Close')}
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {scheduleWindow?.start && scheduleWindow?.end && (
          <div className="schedule-modal-section">
            <h4>{t('editModal.workWindow', 'Work window')}</h4>
            <div>
              {scheduleWindow.start} → {scheduleWindow.end}
            </div>
            <div className="tasks-subtitle">
              {t(
                'editModal.dateConstraint',
                'Task dates must stay within this window.'
              )}
            </div>
          </div>
        )}

        <div className="task-row single-task-row">
          <div className="task-row-grid">
            <label>
              <span>{t('editModal.title', 'Title')}</span>
              <input
                type="text"
                value={task.taskTitle}
                onChange={e => handleField('taskTitle', e.target.value)}
                placeholder={t('editModal.titlePlaceholder', 'Task title')}
              />
            </label>

            <label>
              <span>{t('status', 'Status')}</span>
              <select
                value={task.taskStatus}
                onChange={e => handleField('taskStatus', e.target.value)}
              >
                {statuses.map(status => (
                  <option key={status} value={status}>
                    {t(`statusValues.${status}`, status.replaceAll('_', ' '))}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="task-row-grid">
            <label>
              <span>{t('editModal.taskStartDate', 'Task Start Date')}</span>
              <input
                type="date"
                value={task.periodStart}
                min={minDate}
                max={maxDate}
                onChange={e => handleField('periodStart', e.target.value)}
              />
            </label>

            <label>
              <span>{t('editModal.taskEndDate', 'Task End Date')}</span>
              <input
                type="date"
                value={task.periodEnd}
                min={minDate}
                max={maxDate}
                onChange={e => handleField('periodEnd', e.target.value)}
              />
            </label>

            <label>
              <span>{t('assignee', 'Assignee')}</span>
              <select
                value={task.assignedToUserId}
                onChange={e => handleField('assignedToUserId', e.target.value)}
              >
                <option value="">{t('unassigned', 'Unassigned')}</option>
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
            <span>{t('description', 'Description')}</span>
            <textarea
              value={task.taskDescription}
              onChange={e => handleField('taskDescription', e.target.value)}
              placeholder={t(
                'editModal.descriptionPlaceholder',
                'Describe the work to be completed'
              )}
              rows={3}
            />
          </label>
        </div>

        {errorMessage && <div className="form-error">{errorMessage}</div>}

        <div className="form-actions">
          <button
            type="button"
            className="modal-secondary"
            onClick={onClose}
            disabled={isSaving}
          >
            {t('editModal.cancel', 'Cancel')}
          </button>
          <button
            type="button"
            className="modal-primary"
            onClick={onSave}
            disabled={isSaving}
          >
            {isSaving
              ? t('editModal.saving', 'Saving...')
              : t('editModal.saveChanges', 'Save changes')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EditTaskModal;
