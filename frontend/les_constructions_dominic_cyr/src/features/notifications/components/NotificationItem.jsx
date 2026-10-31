import React from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import {
  GoTasklist,
  GoProject,
  GoFileDiff,
  GoCalendar,
  GoFile,
  GoPeople,
  GoCreditCard,
  GoCommentDiscussion,
  GoBell,
  GoHome,
  GoTrash,
} from 'react-icons/go';
import useBackendUser from '../../../hooks/useBackendUser';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './NotificationItem.css';

const NotificationItem = ({ notification, onMarkAsRead, onDelete }) => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth0();
  const { role } = useBackendUser();
  const { t, currentLanguage } = usePageTranslations('inbox');

  const resolveNotificationLink = link => {
    if (!link) return link;
    if (link === '/forms') {
      if (!role) {
        return link;
      }
      return role === 'CUSTOMER' ? '/customers/forms' : '/salesperson/forms';
    }
    return link;
  };

  const handleClick = async () => {
    // Mark as read if not already read
    if (!notification.isRead && isAuthenticated) {
      try {
        // Call the callback which handles both API call and state update
        if (onMarkAsRead) {
          await onMarkAsRead(notification.notificationId);
        }
      } catch (error) {
        // Error is already logged by the hook, just handle silently
      }
    }

    // Navigate to the link if provided
    if (notification.link) {
      const targetLink = resolveNotificationLink(notification.link);
      if (targetLink) {
        navigate(targetLink);
      }
    }
  };

  const formatDate = dateString => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);

    if (diffInSeconds < 60) {
      return t('time.justNow', 'À l’instant');
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return t('time.minutesAgo', '{{count}} minute(s)', { count: minutes });
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return t('time.hoursAgo', '{{count}} heure(s)', { count: hours });
    } else if (diffInSeconds < 604800) {
      const days = Math.floor(diffInSeconds / 86400);
      return t('time.daysAgo', '{{count}} jour(s)', { count: days });
    } else {
      return date.toLocaleDateString();
    }
  };

  const categoryLabel = t(
    `categories.${notification.category}`,
    notification.category.replace(/_/g, ' ')
  );

  const translateNotificationText = value => {
    if (!value || currentLanguage !== 'fr') return value;
    const replacements = [
      [/^New Inquiry Received$/, 'Nouvelle demande reçue'],
      [
        /^A new inquiry has been received from (.+)$/,
        'Une nouvelle demande a été reçue de $1',
      ],
      [/^New documents uploaded$/, 'Nouveaux documents téléversés'],
      [/^(.+) uploaded (.+)$/, '$1 a téléversé $2'],
      [/^Assigned to Lot (.+)$/, 'Assigné au lot $1'],
      [
        /^You have been assigned to lot (.+) at (.+)$/,
        'Vous avez été assigné au lot $1 à l’adresse $2',
      ],
      [/^Task Updated$/, 'Tâche mise à jour'],
      [
        /^Task "(.+)" was updated\. Period: (.+) to (.+)\. Status: (.+)\.$/,
        'La tâche « $1 » a été mise à jour. Période : $2 à $3. Statut : $4.',
      ],
      [/^Project Schedule Updated$/, 'Calendrier du projet mis à jour'],
      [
        /^The project schedule was updated\. Tasks: (.+)\. Start: (.+), End: (.+)\.$/,
        'Le calendrier du projet a été mis à jour. Tâches : $1. Début : $2, fin : $3.',
      ],
      [/^New Form to Complete: (.+)$/, 'Nouveau formulaire à remplir : $1'],
      [/^Form Submitted: (.+)$/, 'Formulaire soumis : $1'],
      [/^Form Reopened: (.+)$/, 'Formulaire rouvert : $1'],
      [
        /^Your (.+) form for project (.+) has been reopened\. Reason: (.+)\. Please review and resubmit\.$/,
        'Votre formulaire $1 pour le projet $2 a été rouvert. Motif : $3. Veuillez le réviser et le soumettre à nouveau.',
      ],
      [
        /^(.+) has submitted their (.+) form for project (.+)\.$/,
        '$1 a soumis son formulaire $2 pour le projet $3.',
      ],
      [/^Quote submitted: (.+)$/, 'Soumission envoyée : $1'],
      [/^Quote approved: (.+)$/, 'Soumission approuvée : $1'],
      [/^Quote rejected: (.+)$/, 'Soumission refusée : $1'],
      [
        /^A contractor submitted a quote for project (.+)\.$/,
        'Un entrepreneur a soumis une soumission pour le projet $1.',
      ],
      [
        /^Your quote (.+) was (approved|rejected)\.(.*)$/,
        'Votre soumission $1 a été $2.$3',
      ],
    ];
    const replacement = replacements.find(([pattern]) => pattern.test(value));
    return replacement ? value.replace(replacement[0], replacement[1]) : value;
  };

  const translatedTitle = translateNotificationText(notification.title);
  const translatedMessage = translateNotificationText(notification.message);

  const getCategoryIcon = category => {
    // Returns appropriate icon component based on notification category
    const iconProps = { size: 24, className: 'category-icon' };

    switch (category) {
      case 'TASK_ASSIGNED':
      case 'TASK_COMPLETED':
      case 'TASK_UPDATED':
        return <GoTasklist {...iconProps} />;
      case 'LOT_ASSIGNED':
        return <GoHome {...iconProps} />;
      case 'PROJECT_CREATED':
      case 'PROJECT_UPDATED':
      case 'PROJECT_COMPLETED':
        return <GoProject {...iconProps} />;
      case 'FORM_SUBMITTED':
      case 'FORM_SIGNED':
      case 'FORM_REJECTED':
        return <GoFileDiff {...iconProps} />;
      case 'SCHEDULE_CREATED':
      case 'SCHEDULE_UPDATED':
        return <GoCalendar {...iconProps} />;
      case 'DOCUMENT_UPLOADED':
      case 'DOCUMENT_SHARED':
        return <GoFile {...iconProps} />;
      case 'TEAM_MEMBER_ASSIGNED':
      case 'TEAM_MEMBER_REMOVED':
        return <GoPeople {...iconProps} />;
      case 'QUOTE_SUBMITTED':
      case 'QUOTE_APPROVED':
      case 'QUOTE_REJECTED':
        return <GoCreditCard {...iconProps} />;
      case 'INQUIRY_RECEIVED':
        return <GoCommentDiscussion {...iconProps} />;
      default:
        return <GoBell {...iconProps} />;
    }
  };

  return (
    <div
      className={`notification-item ${notification.isRead ? 'read' : 'unread'}`}
      onClick={handleClick}
    >
      <div className="notification-icon">
        {getCategoryIcon(notification.category)}
      </div>
      <div className="notification-content">
        <div className="notification-header">
          <h3 className="notification-title">{translatedTitle}</h3>
          {!notification.isRead && (
            <span className="notification-badge">{t('new', 'Nouveau')}</span>
          )}
        </div>
        <p className="notification-message">{translatedMessage}</p>
        <div className="notification-footer">
          <span className="notification-category">{categoryLabel}</span>
          <span className="notification-time">
            {formatDate(notification.createdAt)}
          </span>
          <button
            type="button"
            className="notification-delete-button"
            aria-label={t('deleteNotification', 'Supprimer la notification')}
            onClick={event => {
              event.stopPropagation();
              onDelete?.(notification.notificationId);
            }}
          >
            <GoTrash />
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotificationItem;

NotificationItem.propTypes = {
  notification: PropTypes.shape({
    notificationId: PropTypes.string.isRequired,
    isRead: PropTypes.bool.isRequired,
    link: PropTypes.string,
    category: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
    createdAt: PropTypes.string.isRequired,
  }).isRequired,
  onMarkAsRead: PropTypes.func,
  onDelete: PropTypes.func,
};
