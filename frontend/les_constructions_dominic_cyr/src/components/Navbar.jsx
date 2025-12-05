import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import '../styles/navbar.css';

const Navbar = () => {
    const [isOpen, setIsOpen] = useState(false);
    const location = useLocation();

    const filesServiceUrl = import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
    const logoId = import.meta.env. VITE_LOGO_ID;

    const toggleMenu = () => {
        setIsOpen(!isOpen);
        if (! isOpen) {
            document. body.classList.add('menu-open');
        } else {
            document.body.classList. remove('menu-open');
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

    const isActive = (path) => {
        return location.pathname === path ?  'active' : '';
    };

    const getLogoUrl = () => {
        if (logoId) {
            return `${filesServiceUrl}/files/${logoId}`;
        }
        return null;
    };

    return (
        <>
            {/* Dark Overlay */}
            <div
                className={`navbar-overlay ${isOpen ? 'active' : ''}`}
                onClick={closeMenu}
                aria-hidden="true"
            />

            {/* Top Navbar */}
            <nav className="navbar">
                <div className="navbar-container">
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

                    <Link to="/" className="navbar-logo" onClick={closeMenu}>
                        {getLogoUrl() ? (
                            <img
                                src={getLogoUrl()}
                                alt="DCYR Logo"
                                className="navbar-logo-image"
                                onError={(e) => {
                                    console.error('Logo failed to load');
                                    e.target.style.display = 'none';
                                    const textSpan = e.target.nextSibling;
                                    if (textSpan) textSpan.style.display = 'block';
                                }}
                            />
                        ) : null}
                        <span style={{ display: getLogoUrl() ? 'none' : 'block' }}>DCYR</span>
                    </Link>
                </div>
            </nav>

            {/* Slide-out Sidebar */}
            <aside className={`navbar-sidebar ${isOpen ? 'open' : ''}`}>
                <div className="navbar-sidebar-header">
                    <Link to="/" className="navbar-sidebar-logo" onClick={closeMenu}>
                        {getLogoUrl() ? (
                            <img
                                src={getLogoUrl()}
                                alt="DCYR Logo"
                                className="navbar-sidebar-logo-image"
                            />
                        ) : (
                            <span className="navbar-sidebar-logo-text">DCYR</span>
                        )}
                    </Link>
                    <button
                        className="navbar-close"
                        onClick={closeMenu}
                        aria-label="Close navigation menu"
                    >
                        ✕
                    </button>
                </div>

                <nav className="navbar-sidebar-nav">
                    {/* Dashboard Section */}
                    <div className="navbar-section">
                        <h3 className="navbar-section-title">Dashboard</h3>
                        <ul className="navbar-menu">
                            <li className="navbar-item">
                                <Link
                                    to="/"
                                    className={`navbar-link ${isActive('/')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">📊</span>
                                    <span className="navbar-text">Dashboard</span>
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
                                    <span className="navbar-icon">📥</span>
                                    <span className="navbar-text">Inbox</span>
                                </Link>
                            </li>
                            <li className="navbar-item">
                                <Link
                                    to="/projects"
                                    className={`navbar-link ${isActive('/projects')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">🏗️</span>
                                    <span className="navbar-text">Projects</span>
                                </Link>
                            </li>
                            <li className="navbar-item">
                                <Link
                                    to="/billing"
                                    className={`navbar-link ${isActive('/billing')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">💵</span>
                                    <span className="navbar-text">Billing</span>
                                </Link>
                            </li>
                            <li className="navbar-item">
                                <Link
                                    to="/documents"
                                    className={`navbar-link ${isActive('/documents')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">📄</span>
                                    <span className="navbar-text">Documents</span>
                                </Link>
                            </li>
                            <li className="navbar-item">
                                <Link
                                    to="/lots"
                                    className={`navbar-link ${isActive('/lots')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">📍</span>
                                    <span className="navbar-text">Send Message</span>
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
                                    to="/account"
                                    className={`navbar-link ${isActive('/account')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">⚙️</span>
                                    <span className="navbar-text">Account Settings</span>
                                </Link>
                            </li>
                            <li className="navbar-item">
                                <Link
                                    to="/notifications"
                                    className={`navbar-link ${isActive('/notifications')}`}
                                    onClick={closeMenu}
                                >
                                    <span className="navbar-icon">🔔</span>
                                    <span className="navbar-text">Notification Preferences</span>
                                </Link>
                            </li>
                        </ul>
                    </div>
                </nav>

                {/* Sidebar Footer */}
                <div className="navbar-sidebar-footer">
                    <button className="navbar-logout" onClick={() => {
                        closeMenu();
                        // Add logout logic here
                        console.log('Logout clicked');
                    }}>
                        <span className="navbar-icon">🚪</span>
                        <span className="navbar-text">Logout</span>
                    </button>
                </div>
            </aside>
        </>
    );
};

export default Navbar;