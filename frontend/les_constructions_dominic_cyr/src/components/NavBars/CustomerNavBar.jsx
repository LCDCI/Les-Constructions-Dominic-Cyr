import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useUnreadCount } from '../../features/notifications/hooks/useUnreadCount';
import '../../styles/NavBars/customerNavbar.css';
import {
  GoProject,
  GoInbox,
  GoPackage,
  GoFileDiff,
  GoFile,
  GoGear,
  GoHome,
} from 'react-icons/go';
import { IoIosNotifications } from 'react-icons/io';
import { CiLogout } from 'react-icons/ci';
import { CgProfile } from 'react-icons/cg';

const Navbar = ({
  isOpen: controlledOpen,
  onToggle,
  onClose,
  showToggle = true,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const { logout, isAuthenticated } = useAuth0();
  const { t } = useTranslation();
  const { unreadCount } = useUnreadCount();
  const isControlled = typeof controlledOpen === 'boolean';
  const menuOpen = isControlled ? controlledOpen : isOpen;

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL ||
    (typeof window !== 'undefined' &&
    window.location.hostname.includes('constructions-dominiccyr')
      ? 'https://files-service-app-xubs2.ondigitalocean.app'
      : typeof window !== 'undefined' &&
          window.location.hostname === 'localhost'
        ? 'http://localhost:8082'
        : `${window.location.origin}/files`);
  const logoId = import.meta.env.VITE_LOGO_ID;

  const toggleMenu = () => {
    if (onToggle) {
      onToggle();
      return;
    }
    setIsOpen(prev => !prev);
  };

  const closeMenu = () => {
    if (onClose) {
      onClose();
      return;
    }
    setIsOpen(false);
  };

  useEffect(() => {
    if (menuOpen) {
      document.body.classList.add('menu-open');
    } else {
      document.body.classList.remove('menu-open');
    }

    return () => {
      document.body.classList.remove('menu-open');
    };
  }, [menuOpen]);

  const isActive = path => {
    return location.pathname === path ? 'active' : '';
  };

  // eslint-disable-next-line no-unused-vars
  const getLogoUrl = () => {
    if (logoId) {
      return `${filesServiceUrl}/files/${logoId}`;
    }
    return null;
  };

  return (
    <>
      <div
        className={`navbar-overlay ${menuOpen ? 'active' : ''}`}
        onClick={closeMenu}
        aria-hidden="true"
      />

      {showToggle && (
        <button
          className="navbar-toggle"
          onClick={toggleMenu}
          aria-label="Toggle navigation menu"
          aria-expanded={menuOpen}
        >
          <span className="hamburger-line"></span>
          <span className="hamburger-line"></span>
          <span className="hamburger-line"></span>
        </button>
      )}
      <aside className={`navbar-sidebar ${menuOpen ? 'open' : ''}`}>
        <nav className="navbar-sidebar-nav">
          <div className="navbar-section">
            <h3 className="navbar-section-title">
              {t('navbar.sections.dashboard', 'Dashboard')}
            </h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/customer/dashboard"
                  className={`navbar-link ${isActive('/customer/dashboard')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoProject />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.dashboard', 'Dashboard')}
                  </span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/"
                  className={`navbar-link ${isActive('/')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoHome />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.home', 'Home')}
                  </span>
                </Link>
              </li>
            </ul>
          </div>

          {/* Management Section */}
          <div className="navbar-section">
            <h3 className="navbar-section-title">
              {t('navbar.sections.management', 'Management')}
            </h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/inbox"
                  className={`navbar-link ${isActive('/inbox')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoInbox />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.inbox', 'Inbox')}
                  </span>
                  {unreadCount > 0 && (
                    <span className="navbar-badge">{unreadCount}</span>
                  )}
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/projects"
                  className={`navbar-link ${isActive('/projects')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoPackage />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.projects', 'Projects')}
                  </span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/customers/forms"
                  className={`navbar-link ${isActive('/customers/forms')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoFileDiff />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.forms', 'Forms')}
                  </span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/customers/documents"
                  className={`navbar-link ${isActive('/customers/documents')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoFile />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.lotDocuments', 'Lot Documents')}
                  </span>
                </Link>
              </li>
            </ul>
          </div>
          <div className="navbar-section">
            <h3 className="navbar-section-title">
              {t('navbar.sections.settings', 'Settings')}
            </h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/profile"
                  className={`navbar-link ${isActive('/profile')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <CgProfile />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.myProfile', 'My Profile')}
                  </span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/account"
                  className={`navbar-link ${isActive('/account')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoGear />
                  </span>
                  <span className="navbar-text">
                    {t('navbar.menuItems.accountSettings', 'Account Settings')}
                  </span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/notifications"
                  className={`navbar-link ${isActive('/notifications')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <IoIosNotifications />
                  </span>
                  <span className="navbar-text">
                    {t(
                      'navbar.menuItems.notificationPreferences',
                      'Notification Preferences'
                    )}
                  </span>
                </Link>
              </li>
            </ul>
          </div>
        </nav>

        {/* Sidebar Footer */}
        <div className="navbar-sidebar-footer">
          <button
            className="navbar-logout"
            onClick={() => {
              closeMenu();
              if (isAuthenticated) {
                logout({ logoutParams: { returnTo: window.location.origin } });
              }
            }}
          >
            <span className="navbar-icon">
              <CiLogout />
            </span>
            <span className="navbar-text">
              {t('navbar.menuItems.logout', 'Logout')}
            </span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default Navbar;
