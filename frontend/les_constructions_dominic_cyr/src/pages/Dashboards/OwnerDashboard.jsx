import React from 'react';
import { useNavigate } from 'react-router-dom';
import ownerUseSchedules from '../../features/schedules/hooks/ownerUseSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/OwnerDashboard.css';
import { GoInbox } from "react-icons/go";
import { GoArrowUp } from "react-icons/go";
import { GoPeople } from "react-icons/go";
import { GoGraph } from "react-icons/go";
import { GoPackage } from "react-icons/go";
import { GoFileDiff } from "react-icons/go";
import { GoFile } from "react-icons/go";
import { FaMapLocationDot } from "react-icons/fa6";


const OwnerDashboard = () => {
  const { schedules, loading, error } = ownerUseSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    navigate('/owner/schedules/all');
  };

  const dashboardCards = [
    {
      icon: <GoArrowUp />,
      title: 'Uploads',
      buttonText: 'Begin Upload',
      action: () => navigate('/owner/uploads'),
    },
    {
      icon: <GoGraph />,
      title: 'Analytics & Reports',
      buttonText: 'View Reports',
      action: () => navigate('/owner/reports'),
    },
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
      action: () => navigate('/owner/inbox'),
    },
    {
      icon: <GoFile />,
      title: 'Documents',
      buttonText: 'View Documents',
      action: () => navigate('/owner/documents'),
    },
    {
      icon: <GoPeople />,
      title: 'Users',
      buttonText: 'View Users',
      action: () => navigate('/users'),
    },
    {
      icon: <GoFileDiff />,
      title: 'Forms',
      buttonText: 'Create Form',
      action: () => navigate('/owner/forms'),
    },
    {
      icon: <FaMapLocationDot />,
      title: 'Lots',
      buttonText: 'Add',
      action: () => navigate('/lots'),
    },
  ];

  return (
    <div className="owner-dashboard">
      <h1 className="dashboard-title">Owner Dashboard</h1>

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

export default OwnerDashboard;
