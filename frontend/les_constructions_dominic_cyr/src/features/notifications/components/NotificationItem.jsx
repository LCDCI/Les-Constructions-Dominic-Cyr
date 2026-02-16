import React from 'react';
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
} from 'react-icons/go';
import useBackendUser from '../../../hooks/useBackendUser';
import './NotificationItem.css';

const NotificationItem = ({ notification, onMarkAsRead }) => {
  const navigate = useNavigate();
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const { role } = useBackendUser();

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
      return 'Just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 604800) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} day${days > 1 ? 's' : ''} ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

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
          <h3 className="notification-title">{notification.title}</h3>
          {!notification.isRead && (
            <span className="notification-badge">New</span>
          )}
        </div>
        <p className="notification-message">{notification.message}</p>
        <div className="notification-footer">
          <span className="notification-category">
            {notification.category.replace(/_/g, ' ')}
          </span>
          <span className="notification-time">
            {formatDate(notification.createdAt)}
          </span>
        </div>
      </div>
    </div>
  );
};

export default NotificationItem;
