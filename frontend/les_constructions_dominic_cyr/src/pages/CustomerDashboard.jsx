import React from 'react';
import { useNavigate } from 'react-router-dom';
import ownerUseSchedules from '../features/schedules/hooks/customerUseSchedules';
import ScheduleList from '../features/schedules/components/ScheduleList';
import DashboardCard from '../components/DashboardCard';
import '../styles/CustomerDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';

const CustomerDashboard = () => {
  const { schedules, loading, error } = ownerUseSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    navigate('/customers/schedules/all');
  };

  const dashboardCards = [
    {
      icon: <GoPackage />,
      title: 'Projects',
      buttonText: 'View Projects',
      action: () => navigate('/projects'),
    },
    {
      icon: <GoInbox />,
      title: 'Inbox',
      buttonText: 'View Inbox',
      action: () => navigate('/customers/inbox'),
    },
    {
      icon: <GoFile />,
      title: 'Documents',
      buttonText: 'View Documents',
      action: () => navigate('/customers/documents'),
    },
    {
      icon: <GoFileDiff />,
      title: 'Forms',
      buttonText: 'Fill Out Forms',
      action: () => navigate('/customers/forms'),
    },
  ];

  return (
    <div className="customer-dashboard">
      <h1 className="dashboard-title">Customer Dashboard</h1>

      <div className="dashboard-grid">
        {dashboardCards.map((card, index) => (
          <DashboardCard
            key={index}
            icon={card.icon}
            title={card.title}
            buttonText={card.buttonText}
            onClick={card.action}
          />
        ))}
      </div>

      <div className="schedule-section">
        <h2>This week:</h2>
        <ScheduleList schedules={schedules} loading={loading} error={error} />
        <button className="see-more-button" onClick={handleSeeMore}>
          See more
        </button>
      </div>
    </div>
  );
};

export default CustomerDashboard;
