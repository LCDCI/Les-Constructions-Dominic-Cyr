import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import contractorUseSchedules from '../../features/schedules/hooks/contractorUsesSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/ContractorDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { GoTasklist } from 'react-icons/go';
import { MdOutlineRequestQuote } from 'react-icons/md';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const ContractorDashboard = () => {
  const { t } = usePageTranslations('contractorDashboard');
  const { schedules, loading, error } = contractorUseSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    navigate('/contractor/tasks');
  };

  const dashboardCards = useMemo(
    () => [
      {
        icon: <GoTasklist />,
        title: t('cards.myTasks.title', 'My Tasks'),
        buttonText: t('cards.myTasks.button', 'View My Tasks'),
        action: () => navigate('/contractor/tasks'),
      },
      {
        icon: <MdOutlineRequestQuote />,
        title: t('cards.quotes.title', 'Quotes'),
        buttonText: t('cards.quotes.button', 'Upload Quote'),
        action: () => navigate('/contractors/quotes'),
      },
      {
        icon: <GoPackage />,
        title: t('cards.projects.title', 'Projects'),
        buttonText: t('cards.projects.button', 'View Projects'),
        action: () => navigate('/projects'),
      },
      {
        icon: <GoInbox />,
        title: t('cards.inbox.title', 'Inbox'),
        buttonText: t('cards.inbox.button', 'View Inbox'),
        action: () => navigate('/contractors/inbox'),
      },
      {
        icon: <GoFile />,
        title: t('cards.documents.title', 'Documents'),
        buttonText: t('cards.documents.button', 'View Documents'),
        action: () => navigate('/contractors/documents'),
      },
    ],
    [t, navigate]
  );

  return (
    <div className="contractor-dashboard">
      <h1 className="dashboard-title">{t('title', 'Contractor Dashboard')}</h1>

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
        <h2>{t('upcomingTasks', 'Here are your upcoming tasks:')}</h2>
        <ScheduleList schedules={schedules} loading={loading} error={error} />
        <button className="see-more-button" onClick={handleSeeMore}>
          {t('seeMore', 'See more')}
        </button>
      </div>
    </div>
  );
};

export default ContractorDashboard;
