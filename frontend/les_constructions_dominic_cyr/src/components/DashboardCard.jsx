import React from 'react';

const DashboardCard = ({ icon, title, buttonText, onClick }) => {
  return (
    <div className="dashboard-card">
      <div className="card-icon">{icon}</div>
      <h3>{title}</h3>
      <button className="card-button" onClick={onClick}>
        {buttonText}
      </button>
    </div>
  );
};

export default DashboardCard;
