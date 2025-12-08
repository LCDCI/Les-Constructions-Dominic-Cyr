import React from 'react';
import { useNavigate } from 'react-router-dom';
import useSchedules from '../features/schedules/hooks/useSchedules';
import ScheduleList from '../features/schedules/components/ScheduleList';
import DashboardCard from '../components/DashboardCard';
import '../styles/OwnerDashboard.css';
import {
  GoInbox,
  GoArrowUp,
  GoPeople,
  GoGraph,
  GoGear,
  GoHome,
  GoPackage,
  GoFileDiff,
  GoFile,
  GoPaperAirplane,
} from 'react-icons/go';
import { FaFileInvoiceDollar, FaMapLocationDot } from 'react-icons/fa6';

const OwnerDashboard = () => {
  const { schedules, loading, error } = useSchedules(false);
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
      icon: <FaFileInvoiceDollar />,
      title: 'Billing',
      buttonText: 'View Billing',
      action: () => navigate('/owner/billing'),
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
      action: () => navigate('/owner/users'),
    },
    {
      icon: <GoGear />,
      title: 'Settings',
      buttonText: 'View Settings',
      action: () => navigate('/owner/settings'),
    },
    {
      icon: <GoPaperAirplane />,
      title: 'Send Messages',
      buttonText: 'Send Message',
      action: () => navigate('/owner/messages'),
    },
    {
      icon: <GoHome />,
      title: 'Home Content',
      buttonText: 'Edit Home',
      action: () => navigate('/'),
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
