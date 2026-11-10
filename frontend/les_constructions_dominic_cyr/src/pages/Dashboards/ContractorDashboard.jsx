import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import contractorUseSchedules from '../../features/schedules/hooks/contractorUsesSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/ContractorDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { GoTasklist } from 'react-icons/go';
import { MdOutlineRequestQuote } from 'react-icons/md';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { useBackendUser } from '../../hooks/useBackendUser';

const ContractorDashboard = () => {
  const { t } = usePageTranslations('contractorDashboard');
  const { profile } = useBackendUser();
  const { schedules, loading, error } = contractorUseSchedules(true);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    const projectId = schedules?.[0]?.projectIdentifier;
    navigate(
      projectId ? `/projects/${projectId}/schedule` : '/contractor/dashboard'
    );
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
        title: t('cards.quotes.title', 'Estimates'),
        buttonText: t('cards.quotes.button', 'View Estimates'),
        action: () => navigate('/quotes'),
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
      <h1 className="dashboard-welcome">
        {t('welcome', {
          defaultValue: 'Welcome, {{name}}!',
          name: profile?.firstName || '',
        })}
      </h1>
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
        <h2>{t('upcomingWork', 'Upcoming work')}</h2>
        <ScheduleList
          schedules={schedules}
          loading={loading}
          error={error}
          loadingMessage={t('loadingSchedules', 'Loading schedules...')}
          emptyMessage={t('noSchedules', 'No schedules available')}
          upcomingWorkLabel={t('upcomingWork', 'Upcoming work')}
          lotFilterLabel={t('filterByLot', 'Filter by lot')}
          allLotsLabel={t('allLots', 'All lots')}
          dateDueLabel={t('due', 'Due')}
          lotLabel={t('lot', 'Lot')}
        />
        <button className="see-more-button" onClick={handleSeeMore}>
          {t('viewSchedule', 'View schedule')}
        </button>
      </div>
    </div>
  );
};

export default ContractorDashboard;
