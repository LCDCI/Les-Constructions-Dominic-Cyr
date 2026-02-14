import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import useSchedules from '../../features/schedules/hooks/ownerUseSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/SalespersonDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const SalespersonDashboard = () => {
  const { t } = usePageTranslations('salespersonDashboard');
  const { schedules, loading, error } = useSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    navigate('/salesperson/schedules/all');
  };

  const dashboardCards = useMemo(
    () => [
      {
        icon: <GoPackage />,
        title: t('cards.projects.title', 'My Projects'),
        buttonText: t('cards.projects.button', 'View Projects'),
        action: () => navigate('/projects'),
      },
      {
        icon: <GoInbox />,
        title: t('cards.inbox.title', 'Inbox'),
        buttonText: t('cards.inbox.button', 'View Inbox'),
        action: () => navigate('/salesperson/inbox'),
      },
      {
        icon: <GoFile />,
        title: t('cards.documents.title', 'Documents'),
        buttonText: t('cards.documents.button', 'View Documents'),
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
      <h1 className="dashboard-title">{t('title', 'Salesperson Dashboard')}</h1>

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
        <ScheduleList schedules={schedules} loading={loading} error={error} />
        <button className="see-more-button" onClick={handleSeeMore}>
          {t('seeMore', 'See more')}
        </button>
      </div>
    </div>
  );
};

export default SalespersonDashboard;
