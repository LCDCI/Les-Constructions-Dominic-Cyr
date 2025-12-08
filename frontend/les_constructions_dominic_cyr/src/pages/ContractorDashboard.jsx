import React from 'react';
import { useNavigate } from 'react-router-dom';
import contractorUseSchedules from '../features/schedules/hooks/contractorUsesSchedules';
import ScheduleList from '../features/schedules/components/ScheduleList';
import DashboardCard from '../components/DashboardCard';
import '../styles/ContractorDashboard.css';
import { GoInbox } from "react-icons/go";
import { GoPackage } from "react-icons/go";
import { GoFile } from "react-icons/go";
import { MdOutlineRequestQuote } from "react-icons/md";

const ContractorDashboard = () => {
  const { schedules, loading, error } = contractorUseSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    navigate('/contractors/schedules/all');
  };

  const dashboardCards = [
    {
      icon: <MdOutlineRequestQuote />,
      title: 'Quotes',
      buttonText: 'Upload Quote',
      action: () => navigate('/contractors/quotes'),
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
      action: () => navigate('/contractors/inbox'),
    },
    {
      icon: <GoFile />,
      title: 'Documents',
      buttonText: 'View Documents',
      action: () => navigate('/contractors/documents'),
    }
  
  ];

  return (
    <div className="contractor-dashboard">
      <h1 className="dashboard-title">Contractor Dashboard</h1>

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
        <h2>Here are your upcoming tasks:</h2>
        <ScheduleList schedules={schedules} loading={loading} error={error} />
        <button className="see-more-button" onClick={handleSeeMore}>
          See more
        </button>
      </div>
    </div>
  );
};

export default ContractorDashboard;
