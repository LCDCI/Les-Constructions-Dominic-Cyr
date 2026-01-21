import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import '../../styles/NavBars/ownerNavbar.css';
import {
  GoProject,
  GoInbox,
  GoPackage,
  GoFileDiff,
  GoArrowUp,
  GoFile,
  GoPeople,
  GoGraph,
  GoGear,
  GoHome,
  GoCommentDiscussion,
} from 'react-icons/go';
import { IoIosNotifications } from 'react-icons/io';
import { CiLogout } from 'react-icons/ci';
import { FaMapLocationDot } from 'react-icons/fa6';
import { CgProfile } from 'react-icons/cg';

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();

  const filesServiceUrl =
    import.meta.env.VITE_FILES_SERVICE_URL || (typeof window !== 'undefined' && window.location.hostname === 'localhost' ? 'http://localhost:8082' : `${window.location.origin}/files`);
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
    return () => {
      document.body.classList.remove('menu-open');
    };
  }, []);

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
                  to="/owner/dashboard"
                  className={`navbar-link ${isActive('/owner/dashboard')}`}
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
                  to="/inbox"
                  className={`navbar-link ${isActive('/inbox')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoInbox />
                  </span>
                  <span className="navbar-text">Inbox</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/inquiries"
                  className={`navbar-link ${isActive('/inquiries')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoCommentDiscussion />
                  </span>
                  <span className="navbar-text">Inquiries</span>
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
                  to="/forms"
                  className={`navbar-link ${isActive('/forms')}`}
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
                  to="/uploads"
                  className={`navbar-link ${isActive('/uploads')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoArrowUp />
                  </span>
                  <span className="navbar-text">Uploads</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/documents"
                  className={`navbar-link ${isActive('/documents')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoFile />
                  </span>
                  <span className="navbar-text">Documents</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/lots"
                  className={`navbar-link ${isActive('/lots')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <FaMapLocationDot />
                  </span>
                  <span className="navbar-text">Lots</span>
                </Link>
              </li>
            </ul>
          </div>
          <div className="navbar-section">
            <h3 className="navbar-section-title">Administrative</h3>
            <ul className="navbar-menu">
              <li className="navbar-item">
                <Link
                  to="/users"
                  className={`navbar-link ${isActive('/users')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoPeople />
                  </span>
                  <span className="navbar-text">Users</span>
                </Link>
              </li>
              <li className="navbar-item">
                <Link
                  to="/reports"
                  className={`navbar-link ${isActive('/reports')}`}
                  onClick={closeMenu}
                >
                  <span className="navbar-icon">
                    <GoGraph />
                  </span>
                  <span className="navbar-text">Analytics & Reports</span>
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
