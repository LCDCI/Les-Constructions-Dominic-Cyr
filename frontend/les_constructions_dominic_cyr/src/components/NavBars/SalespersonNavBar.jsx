import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useUnreadCount } from '../../features/notifications/hooks/useUnreadCount';
import '../../styles/NavBars/ownerNavbar.css';
import { GoInbox } from 'react-icons/go';
import { GoGear } from 'react-icons/go';
import { GoPackage } from 'react-icons/go';
import { GoFileDiff } from 'react-icons/go';
import { GoFile } from 'react-icons/go';
import { GoProject } from 'react-icons/go';
import { CiLogout } from 'react-icons/ci';
import { IoIosNotifications } from 'react-icons/io';
import { GoHome } from 'react-icons/go';
import { CgProfile } from 'react-icons/cg';

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();
  const { unreadCount } = useUnreadCount();

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
    setIsOpen(!isOpen);
    if (!isOpen) {
      document.body.classList.add('menu-open');
    } else {
      document.body.classList.remove('menu-open');
    }
  };

  const closeMenu = () => {
    setIsOpen(false);
    document.body.classList.remove('menu-open');
  };

  useEffect(() => {
    // Cleanup on unmount
    return () => {
      document.body.classList.remove('menu-open');
    };
  }, []);

  const isActive = path => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <>
      <div
        className={`navbar-overlay ${isOpen ? 'active' : ''}`}
        onClick={closeMenu}
        aria-hidden="true"
      />

      <button
        className="navbar-toggle"
        onClick={toggleMenu}
        aria-label="Toggle navigation menu"
        aria-expanded={isOpen}
      >
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
      </button>
      <aside className={`navbar-sidebar ${isOpen ? 'open' : ''}`}>
        <nav className="navbar-sidebar-nav">
          <div className="navbar-section">
            <h3 className="navbar-section-title">Dashboard</h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/salesperson/dashboard"
                  className={`navbar-link ${isActive('/salesperson/dashboard')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoProject />
                  </span>
                  <span className="navbar-text">Dashboard</span>
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
                  <span className="navbar-text">Home</span>
                </Link>
              </li>
            </ul>
          </div>

          {/* Management Section */}
          <div className="navbar-section">
            <h3 className="navbar-section-title">Management</h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/salesperson/inbox"
                  className={`navbar-link ${isActive('/salesperson/inbox')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoInbox />
                  </span>
                  <span className="navbar-text">Inbox</span>
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
                  <span className="navbar-text">Projects</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/salesperson/forms"
                  className={`navbar-link ${isActive('/salesperson/forms')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoFileDiff />
                  </span>
                  <span className="navbar-text">Forms</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/salesperson/documents"
                  className={`navbar-link ${isActive('/salesperson/documents')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoFile />
                  </span>
                  <span className="navbar-text">Documents</span>
                </Link>
              </li>
            </ul>
          </div>

          {/* Settings Section */}
          <div className="navbar-section">
            <h3 className="navbar-section-title">Settings</h3>
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
                  <span className="navbar-text">My Profile</span>
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
                  <span className="navbar-text">Account Settings</span>
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
                  <span className="navbar-text">Notification Preferences</span>
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
              // Add logout logic here
              console.log('Logout clicked');
            }}
          >
            <span className="navbar-icon">
              <CiLogout />
            </span>
            <span className="navbar-text">Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default Navbar;
