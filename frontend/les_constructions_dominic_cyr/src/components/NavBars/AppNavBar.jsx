import React, { useEffect, useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import useBackendUser from '../../hooks/useBackendUser';
import { projectApi } from '../../features/projects/api/projectApi';

import '../../styles/NavBars/AppNavBar.css';
import OwnerNavBar from '../../components/NavBars/OwnerNavBar';
import SalespersonNavBar from '../../components/NavBars/SalespersonNavBar';
import ContractorNavBar from '../../components/NavBars/ContractorNavBar';
import CustomerNavBar from '../../components/NavBars/CustomerNavBar';

const DESKTOP_LOGO_URL =
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Logos/Full_Logo_Mono.png';
const MOBILE_LOGO_URL =
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Logos/Half_logo_mono.png';

function clearAppSession() {
  const APP_KEYS = [
    'user',
    'userProfile',
    'roles',
    'portal',
    'selectedPortal',
    'lastVisitedRoute',
  ];

  APP_KEYS.forEach(k => {
    localStorage.removeItem(k);
    sessionStorage.removeItem(k);
  });

  const purgeByPrefix = (storage, prefixes) => {
    const keys = [];
    for (let i = 0; i < storage.length; i++) keys.push(storage.key(i));
    keys
      .filter(k => k && prefixes.some(p => k.startsWith(p)))
      .forEach(k => storage.removeItem(k));
  };

  purgeByPrefix(localStorage, ['auth0', '@@auth0spajs@@']);
  purgeByPrefix(sessionStorage, ['auth0', '@@auth0spajs@@']);

  delete axios.defaults.headers.common.Authorization;
}

export default function AppNavBar() {
  const { i18n, t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();

  const [isPublicMobileMenuOpen, setIsPublicMobileMenuOpen] = useState(false);
  const [isDashboardMenuOpen, setIsDashboardMenuOpen] = useState(false);
  const [isProjectsMenuOpen, setIsProjectsMenuOpen] = useState(false);
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(true);
  const [projectsError, setProjectsError] = useState(false);
  const [isNavHidden, setIsNavHidden] = useState(false);

  const currentLanguage = i18n.language || 'en';
  const isFrench = currentLanguage === 'fr';

  const isResidentialProjectsActive =
    location.pathname.startsWith('/residential-projects') ||
    /^\/projects\/[^/]+\/(overview|living-environment|lots)(?:\/|$)/.test(
      location.pathname
    );

  useEffect(() => {
    let isMounted = true;

    const loadProjects = async () => {
      try {
        setProjectsLoading(true);
        setProjectsError(false);
        const projectList = await projectApi.getAllProjects();
        if (isMounted)
          setProjects(Array.isArray(projectList) ? projectList : []);
      } catch (error) {
        if (isMounted) setProjectsError(true);
      } finally {
        if (isMounted) setProjectsLoading(false);
      }
    };

    loadProjects();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let previousScrollY = window.scrollY;

    const handleScroll = () => {
      const currentScrollY = window.scrollY;
      const scrollingDown = currentScrollY > previousScrollY;

      if (currentScrollY <= 24 || !scrollingDown) {
        setIsNavHidden(false);
      } else if (currentScrollY > 80) {
        setIsNavHidden(true);
      }

      previousScrollY = currentScrollY;
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const projectLinks = (
    <>
      {projectsLoading && (
        <li className="project-dropdown-status" role="status">
          {t('nav.projectsLoading', 'Loading projects...')}
        </li>
      )}
      {projectsError && (
        <li className="project-dropdown-status" role="status">
          {t('nav.projectsLoadError', 'Unable to load projects')}
        </li>
      )}
      {!projectsLoading && !projectsError && projects.length === 0 && (
        <li className="project-dropdown-status" role="status">
          {t('nav.noProjects', 'No projects available')}
        </li>
      )}
      {!projectsLoading &&
        !projectsError &&
        projects.map(project => (
          <li key={project.projectIdentifier}>
            <NavLink
              to={`/projects/${project.projectIdentifier}/overview`}
              onClick={() => setIsPublicMobileMenuOpen(false)}
            >
              {project.projectName}
            </NavLink>
          </li>
        ))}
      <li className="project-dropdown-all">
        <NavLink
          to="/residential-projects"
          onClick={() => setIsPublicMobileMenuOpen(false)}
        >
          {t('nav.viewAllProjects', 'View all projects')}
        </NavLink>
      </li>
    </>
  );

  const toggleLanguage = () => {
    const newLang = isFrench ? 'en' : 'fr';
    i18n.changeLanguage(newLang);
  };

  const toggleProjectsMenu = () => {
    setIsProjectsMenuOpen(prev => !prev);
  };

  const handleMobileLogoClick = () => {
    // If menu is already open, just close it
    if (isPublicMobileMenuOpen) {
      setIsPublicMobileMenuOpen(false);
    } else {
      // If menu is closed, open it to show project pages
      setIsPublicMobileMenuOpen(true);
      setIsDashboardMenuOpen(false);
    }
  };

  const toggleDashboardMenu = () => {
    setIsDashboardMenuOpen(prev => !prev);
    setIsPublicMobileMenuOpen(false);
  };

  const closeDashboardMenu = () => {
    setIsDashboardMenuOpen(false);
  };

  const goToPortal = () => {
    setIsPublicMobileMenuOpen(false);
    setIsDashboardMenuOpen(false);
    navigate('/portal/login');
  };

  const handleLogout = () => {
    setIsPublicMobileMenuOpen(false);
    setIsDashboardMenuOpen(false);
    clearAppSession();

    logout({
      logoutParams: {
        returnTo: `${window.location.origin}/`,
      },
    });
  };

  return (
    <header className={`site-nav ${isNavHidden ? 'nav-hidden' : ''}`}>
      <div className="site-nav-inner">
        {isAuthenticated && !roleLoading && (
          <div className="dashboard-toggle">
            <button
              className="navbar-toggle"
              onClick={toggleDashboardMenu}
              aria-label="Toggle dashboard menu"
              aria-expanded={isDashboardMenuOpen}
            >
              <span className="hamburger-line"></span>
              <span className="hamburger-line"></span>
              <span className="hamburger-line"></span>
            </button>
          </div>
        )}

        {/* Desktop navigation */}
        <nav className="desktop-nav">
          <div className="projects-nav-item">
            <button
              type="button"
              className={`projects-nav-trigger ${isResidentialProjectsActive ? 'active' : ''}`}
              onClick={toggleProjectsMenu}
              aria-expanded={isProjectsMenuOpen}
              aria-haspopup="true"
            >
              {t('nav.projects', 'Residential Projects')}
            </button>
            <ul
              className={`projects-dropdown ${isProjectsMenuOpen ? 'open' : ''}`}
            >
              {projectLinks}
            </ul>
          </div>

          <NavLink
            to="/renovations"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            {t('nav.renovation', 'Renovations')}
          </NavLink>

          <NavLink to="/" className="brand">
            <img
              src={DESKTOP_LOGO_URL}
              alt="Les Constructions Dominic Cyr"
              className="logo-image"
            />
          </NavLink>

          <NavLink
            to="/projectmanagement"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            {t('nav.projectManagement', 'Project Management')}
          </NavLink>

          <NavLink
            to="/realizations"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            {t('nav.realizations', 'Realizations')}
          </NavLink>
        </nav>

        {/* Desktop actions */}
        <div className="nav-actions">
          {!isAuthenticated && (
            <button type="button" className="btn-portal" onClick={goToPortal}>
              {t('nav.accessPortal', 'Access Portal')}{' '}
              <span className="arrow">→</span>
            </button>
          )}

          {isAuthenticated && (
            <button type="button" className="btn-portal" onClick={handleLogout}>
              {t('nav.logout', 'Logout')}
            </button>
          )}

          <button
            type="button"
            className="btn-language"
            onClick={toggleLanguage}
            aria-label="Toggle language"
          >
            {isFrench ? 'EN' : 'FR'}
          </button>
        </div>

        {/* Mobile logo button - right side (acts as home + project pages dropdown) */}
        <button
          className="mobile-logo-menu-button"
          onClick={handleMobileLogoClick}
          aria-label="Toggle public menu"
        >
          <img
            src={MOBILE_LOGO_URL}
            alt="Les Constructions Dominic Cyr"
            className="mobile-logo-menu"
          />
        </button>
      </div>

      {isAuthenticated && !roleLoading && (
        <div className="dashboard-menu">
          {role === 'OWNER' && (
            <OwnerNavBar
              isOpen={isDashboardMenuOpen}
              onToggle={toggleDashboardMenu}
              onClose={closeDashboardMenu}
              showToggle={false}
            />
          )}
          {role === 'SALESPERSON' && (
            <SalespersonNavBar
              isOpen={isDashboardMenuOpen}
              onToggle={toggleDashboardMenu}
              onClose={closeDashboardMenu}
              showToggle={false}
            />
          )}
          {role === 'CONTRACTOR' && (
            <ContractorNavBar
              isOpen={isDashboardMenuOpen}
              onToggle={toggleDashboardMenu}
              onClose={closeDashboardMenu}
              showToggle={false}
            />
          )}
          {role === 'CUSTOMER' && (
            <CustomerNavBar
              isOpen={isDashboardMenuOpen}
              onToggle={toggleDashboardMenu}
              onClose={closeDashboardMenu}
              showToggle={false}
            />
          )}
        </div>
      )}

      {/* Project pages menu (mobile only, when logo button is clicked) */}
      <nav className={`mobile-nav ${isPublicMobileMenuOpen ? 'open' : ''}`}>
        <NavLink
          to="/"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsPublicMobileMenuOpen(false)}
        >
          {t('nav.home', 'Home')}
        </NavLink>

        <div className="mobile-projects-nav-item">
          <button
            type="button"
            className={`projects-nav-trigger ${isResidentialProjectsActive ? 'active' : ''}`}
            onClick={toggleProjectsMenu}
            aria-expanded={isProjectsMenuOpen}
            aria-haspopup="true"
          >
            {t('nav.projects', 'Residential Projects')}
          </button>
          {isProjectsMenuOpen && (
            <ul className="mobile-projects-dropdown">{projectLinks}</ul>
          )}
        </div>

        <NavLink
          to="/renovations"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsPublicMobileMenuOpen(false)}
        >
          {t('nav.renovation', 'Renovations')}
        </NavLink>

        <NavLink
          to="/projectmanagement"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsPublicMobileMenuOpen(false)}
        >
          {t('nav.projectManagement', 'Project Management')}
        </NavLink>

        <NavLink
          to="/realizations"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsPublicMobileMenuOpen(false)}
        >
          {t('nav.realizations', 'Realizations')}
        </NavLink>

        <div className="mobile-actions">
          {!isAuthenticated && (
            <button type="button" className="btn-portal" onClick={goToPortal}>
              {t('nav.accessPortal', 'Access Portal')}{' '}
              <span className="arrow">→</span>
            </button>
          )}

          {isAuthenticated && (
            <button type="button" className="btn-portal" onClick={handleLogout}>
              {t('nav.logout', 'Logout')}
            </button>
          )}

          <button
            type="button"
            className="btn-language"
            onClick={toggleLanguage}
            aria-label="Toggle language"
          >
            {isFrench ? 'FR' : 'EN'}
          </button>
        </div>
      </nav>
    </header>
  );
}
