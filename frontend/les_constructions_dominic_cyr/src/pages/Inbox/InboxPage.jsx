import React, { useState, useMemo } from 'react';
import { useNotifications } from '../../features/notifications/hooks/useNotifications';
import NotificationItem from '../../features/notifications/components/NotificationItem';
import InboxFilterSelect from './InboxFilterSelect';
import '../../styles/Inbox/InboxPage.css';
import { GoInbox, GoSearch, GoFilter } from 'react-icons/go';

const FILTER_OPTIONS = [
  { value: 'all', label: 'All Notifications' },
  { value: 'unread', label: 'Unread Only' },
  { value: 'read', label: 'Read Only' },
];

const SORT_OPTIONS = [
  { value: 'newest', label: 'Newest First' },
  { value: 'oldest', label: 'Oldest First' },
];

const InboxPage = () => {
  const { notifications, loading, error, markAllAsRead, markAsRead } =
    useNotifications();
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all'); // 'all', 'unread', 'read'
  const [sortBy, setSortBy] = useState('newest'); // 'newest', 'oldest'

  const unreadCount = notifications.filter(n => !n.isRead).length;

  // Filter and sort notifications
  const filteredAndSortedNotifications = useMemo(() => {
    let filtered = [...notifications];

    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        notif =>
          notif.title.toLowerCase().includes(query) ||
          notif.message.toLowerCase().includes(query) ||
          notif.category.toLowerCase().includes(query)
      );
    }

    // Apply read/unread filter
    if (filterType === 'unread') {
      filtered = filtered.filter(notif => !notif.isRead);
    } else if (filterType === 'read') {
      filtered = filtered.filter(notif => notif.isRead);
    }

    // Apply sorting
    filtered.sort((a, b) => {
      const dateA = new Date(a.createdAt);
      const dateB = new Date(b.createdAt);
      return sortBy === 'newest' ? dateB - dateA : dateA - dateB;
    });

    return filtered;
  }, [notifications, searchQuery, filterType, sortBy]);

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  if (loading) {
    return (
      <div className="inbox-page">
        <div className="inbox-container">
          <div className="inbox-loading">Loading notifications...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="inbox-page">
        <div className="inbox-container">
          <div className="inbox-error">
            Error loading notifications: {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="inbox-page">
      <div className="inbox-container">
        <div className="inbox-header">
          <div className="inbox-title-section">
            <GoInbox className="inbox-icon" />
            <h1 className="inbox-title">Inbox</h1>
            {unreadCount > 0 && (
              <span className="inbox-unread-badge">{unreadCount} unread</span>
            )}
          </div>
          <div className="inbox-header-actions">
            {unreadCount > 0 && (
              <button
                className="btn-mark-all-read"
                onClick={handleMarkAllAsRead}
              >
                Mark all as read
              </button>
            )}
          </div>
        </div>

        <div className="inbox-controls">
          <div className="inbox-search">
            <GoSearch className="search-icon" />
            <input
              type="text"
              placeholder="Search notifications..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="search-input"
            />
          </div>

          <div className="inbox-filters">
            <div className="filter-group">
              <InboxFilterSelect
                value={filterType}
                onChange={setFilterType}
                options={FILTER_OPTIONS}
                icon={GoFilter}
              />
            </div>

            <div className="filter-group">
              <InboxFilterSelect
                value={sortBy}
                onChange={setSortBy}
                options={SORT_OPTIONS}
              />
            </div>
          </div>
        </div>

        <div className="inbox-content">
          {filteredAndSortedNotifications.length === 0 ? (
            <div className="inbox-empty">
              <GoInbox className="empty-icon" />
              <p>
                {searchQuery || filterType !== 'all'
                  ? 'No notifications match your filters'
                  : 'No notifications yet'}
              </p>
            </div>
          ) : (
            <div className="notifications-list">
              {filteredAndSortedNotifications.map(notification => (
                <NotificationItem
                  key={notification.notificationId}
                  notification={notification}
                  onMarkAsRead={markAsRead}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default InboxPage;
