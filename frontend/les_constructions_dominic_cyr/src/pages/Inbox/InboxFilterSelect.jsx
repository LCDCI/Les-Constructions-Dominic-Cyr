import React, { useState, useRef, useEffect } from 'react';
import { GoChevronDown } from 'react-icons/go';

const InboxFilterSelect = ({
  value,
  onChange,
  options,
  icon: Icon,
  className = '',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef(null);

  const selectedLabel = options.find(o => o.value === value)?.label ?? value;

  useEffect(() => {
    const handleClickOutside = e => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = option => {
    onChange(option.value);
    setIsOpen(false);
  };

  return (
    <div className={`inbox-filter-select ${className}`} ref={containerRef}>
      <button
        type="button"
        className="inbox-filter-select-trigger"
        onClick={() => setIsOpen(prev => !prev)}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        aria-label={selectedLabel}
      >
        {Icon && <Icon className="inbox-filter-select-icon" aria-hidden />}
        <span className="inbox-filter-select-value">{selectedLabel}</span>
        <GoChevronDown
          className={`inbox-filter-select-caret ${isOpen ? 'is-open' : ''}`}
          aria-hidden
        />
      </button>
      {isOpen && (
        <div
          className="inbox-filter-select-dropdown"
          role="listbox"
          aria-activedescendant={value}
        >
          {options.map(option => (
            <button
              key={option.value}
              type="button"
              role="option"
              aria-selected={option.value === value}
              className={`inbox-filter-select-option ${option.value === value ? 'is-selected' : ''}`}
              onClick={() => handleSelect(option)}
            >
              {option.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default InboxFilterSelect;
