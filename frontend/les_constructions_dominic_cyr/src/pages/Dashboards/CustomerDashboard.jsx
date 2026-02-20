import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import ownerUseSchedules from '../../features/schedules/hooks/customerUseSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import '../../styles/Dashboards/CustomerDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const CustomerDashboard = () => {
  const { t } = usePageTranslations('customerDashboard');
  const { schedules, loading, error } = ownerUseSchedules(false);
  const navigate = useNavigate();

  const handleSeeMore = () => {
    const projectId = schedules?.[0]?.projectIdentifier;
    navigate(projectId ? `/projects/${projectId}/schedule` : '/projects');
  };

  const dashboardCards = useMemo(
    () => [
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
        action: () => navigate('/customers/inbox'),
      },
      {
        icon: <GoFile />,
        title: t('cards.documents.title', 'Lot Documents'),
        buttonText: t('cards.documents.button', 'View Lot Documents'),
        action: () => navigate('/customers/documents'),
      },
      {
        icon: <GoFileDiff />,
        title: t('cards.forms.title', 'Forms'),
        buttonText: t('cards.forms.button', 'Fill Out Forms'),
        action: () => navigate('/customers/forms'),
      },
    ],
    [t, navigate]
  );

  return (
    <div className="customer-dashboard">
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

export default CustomerDashboard;
