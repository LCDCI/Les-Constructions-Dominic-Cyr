import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ownerUseSchedules from '../../features/schedules/hooks/ownerUseSchedules';
import ScheduleList from '../../features/schedules/components/ScheduleList';
import DashboardCard from '../../components/DashboardCard';
import ProjectSelectionModal from '../../features/lots/components/ProjectSelectionModal';
import '../../styles/Dashboards/OwnerDashboard.css';
import { GoInbox } from 'react-icons/go';
import { GoArrowUp } from 'react-icons/go';
import { GoPeople } from 'react-icons/go';
import { GoGraph } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { FaMapLocationDot } from 'react-icons/fa6';
import { usePageTranslations } from '../../hooks/usePageTranslations';

const OwnerDashboard = () => {
  const { t } = usePageTranslations('ownerDashboard');
  const { schedules, loading, error } = ownerUseSchedules(false);
  const navigate = useNavigate();
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);

  const handleSeeMore = () => {
    navigate('/owner/schedules/all');
  };

  const dashboardCards = useMemo(
    () => [
      {
        icon: <GoGraph />,
        title: t('cards.analytics.title', 'Analytics & Reports'),
        buttonText: t('cards.analytics.button', 'View Reports'),
        action: () => navigate('/reports'),
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
        action: () => navigate('/owner/inbox'),
      },
      {
        icon: <GoFile />,
        title: t('cards.documents.title', 'Documents'),
        buttonText: t('cards.documents.button', 'View Documents'),
        action: () => navigate('/owner/documents'),
      },
      {
        icon: <GoPeople />,
        title: t('cards.users.title', 'Users'),
        buttonText: t('cards.users.button', 'View Users'),
        action: () => navigate('/users'),
      },
      {
        icon: <FaMapLocationDot />,
        title: t('cards.lots.title', 'Lots'),
        buttonText: t('cards.lots.button', 'View Lots'),
        action: () => setIsProjectModalOpen(true),
      },
    ],
    [t, navigate]
  );

  return (
    <div className="owner-dashboard">
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

      <ProjectSelectionModal
        isOpen={isProjectModalOpen}
        onClose={() => setIsProjectModalOpen(false)}
      />
    </div>
  );
};

export default OwnerDashboard;
