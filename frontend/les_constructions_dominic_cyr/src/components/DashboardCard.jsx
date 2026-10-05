import React from 'react';
import PropTypes from 'prop-types';

const DashboardCard = ({
  icon,
  title,
  buttonText,
  onClick,
  buttonOptions = [],
  onOptionChange,
}) => {
  return (
    <div className="dashboard-card">
      <div className="card-icon">{icon}</div>
      <h3>{title}</h3>
      {buttonOptions.length > 0 ? (
        <select
          className="card-button card-select"
          defaultValue=""
          onChange={event => onOptionChange?.(event.target.value)}
        >
          <option value="" disabled>
            {buttonText}
          </option>
          {buttonOptions.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : (
        <button className="card-button" onClick={onClick}>
          {buttonText}
        </button>
      )}
    </div>
  );
};

DashboardCard.propTypes = {
  icon: PropTypes.node,
  title: PropTypes.string.isRequired,
  buttonText: PropTypes.string.isRequired,
  onClick: PropTypes.func,
  buttonOptions: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    })
  ),
  onOptionChange: PropTypes.func,
};

export default DashboardCard;
