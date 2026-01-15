import React, { useEffect, useRef } from 'react';

const ScheduleFormModal = ({
  title,
  isOpen,
  schedule,
  onChange,
  onSubmit,
  onClose,
  lots,
  lotsLoading,
  lotsError,
  isSaving,
}) => {
  const originalOverflow = useRef(null);

  useEffect(() => {
    // Lock body scroll when the modal is open, restore on close/unmount.
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

  const handleOverlayClick = e => {
    if (e.target === e.currentTarget && onClose) {
      onClose();
    }
  };

  if (!isOpen) return null;

  const handleFieldChange = (field, value) => {
    onChange(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="schedule-modal create-schedule-modal"
        onMouseDown={e => e.stopPropagation()}
      >
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">{title}</div>
          <button
            type="button"
            className="modal-close"
            aria-label="Close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

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
                disabled={lotsLoading}
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

          <div className="form-row form-row-note">
            <div className="tasks-subtitle">
              After saving these schedule details, you will add tasks in the
              next step. Tasks will be constrained to the start and end dates
              you set here.
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="modal-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
              Cancel
            </button>
            <button type="submit" className="modal-primary" disabled={isSaving}>
              {isSaving ? 'Saving…' : 'Save schedule'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ScheduleFormModal;
