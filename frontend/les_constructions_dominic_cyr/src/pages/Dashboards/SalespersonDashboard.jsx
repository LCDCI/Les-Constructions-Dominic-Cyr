import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import useSchedules from '../../features/schedules/hooks/salespersonUseSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/SalespersonDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import { useBackendUser } from '../../hooks/useBackendUser';

const SalespersonDashboard = () => {
  const { t } = usePageTranslations('salespersonDashboard');
  const { profile } = useBackendUser();
  const { schedules, loading, error } = useSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    const projectId = schedules?.[0]?.projectIdentifier;
    navigate(
      projectId ? `/projects/${projectId}/schedule` : '/salesperson/dashboard'
    );
  };

  const dashboardCards = useMemo(
    () => [
      {
        icon: <GoInbox />,
        title: t('cards.inbox.title', 'Inbox'),
        buttonText: t('cards.inbox.button', 'View Inbox'),
        action: () => navigate('/salesperson/inbox'),
      },
      {
        icon: <GoFile />,
        title: t('cards.documents.title', 'Lot Documents'),
        buttonText: t('cards.documents.button', 'View Lot Documents'),
        action: () => navigate('/salesperson/documents'),
      },
      {
        icon: <GoFileDiff />,
        title: t('cards.forms.title', 'Forms'),
        buttonText: t('cards.forms.button', 'Assign Forms'),
        action: () => navigate('/salesperson/forms'),
      },
    ],
    [t, navigate]
  );

  return (
    <div className="salesperson-dashboard">
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
        <h2>{t('thisWeek', 'This week:')}</h2>
        <ScheduleList
          schedules={schedules}
          loading={loading}
          error={error}
          loadingMessage={t('loadingSchedules', 'Loading schedules...')}
          emptyMessage={t('noSchedules', 'No schedules available')}
        />
        <button className="see-more-button" onClick={handleSeeMore}>
          {t('viewSchedule', 'View schedule')}
        </button>
      </div>
    </div>
  );
};

export default SalespersonDashboard;
