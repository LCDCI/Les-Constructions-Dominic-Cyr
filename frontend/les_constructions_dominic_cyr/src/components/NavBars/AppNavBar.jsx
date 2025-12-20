import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import useBackendUser from '../../hooks/useBackendUser';

import '../../styles/NavBars/AppNavBar.css';
import OwnerNavBar from '../../components/NavBars/OwnerNavBar';
import SalespersonNavBar from '../../components/NavBars/SalespersonNavBar';
import ContractorNavBar from '../../components/NavBars/ContractorNavBar';
import CustomerNavBar from '../../components/NavBars/CustomerNavBar';

function clearAppSession() {
  const APP_KEYS = [
    'user',
    'userProfile',
    'roles',
    'portal',
    'selectedPortal',
    'lastVisitedRoute',
  ];

  APP_KEYS.forEach((k) => {
    localStorage.removeItem(k);
    sessionStorage.removeItem(k);
  });

  const purgeByPrefix = (storage, prefixes) => {
    const keys = [];
    for (let i = 0; i < storage.length; i++) keys.push(storage.key(i));
    keys
        .filter((k) => k && prefixes.some((p) => k.startsWith(p)))
        .forEach((k) => storage.removeItem(k));
  };

  purgeByPrefix(localStorage, ['auth0', '@@auth0spajs@@']);
  purgeByPrefix(sessionStorage, ['auth0', '@@auth0spajs@@']);

  delete axios.defaults.headers.common.Authorization;
}

export default function AppNavBar() {
  const { i18n, t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();

  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const currentLanguage = i18n.language || 'en';
  const isFrench = currentLanguage === 'fr';

  const toggleLanguage = () => {
    const newLang = isFrench ? 'en' : 'fr';
    i18n.changeLanguage(newLang);
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen((prev) => !prev);
  };

  const goToPortal = () => {
    setIsMobileMenuOpen(false);
    navigate('/portal/login');
  };

  const handleLogout = () => {
    setIsMobileMenuOpen(false);
    clearAppSession();

    logout({
      logoutParams: {
        returnTo: `${window.location.origin}/`,
      },
    });
  };

  return (
      <header className="site-nav">
        <div className="site-nav-inner">
          {!roleLoading && role === 'OWNER' && <OwnerNavBar />}
          {!roleLoading && role === 'SALESPERSON' && <SalespersonNavBar />}
          {!roleLoading && role === 'CONTRACTOR' && <ContractorNavBar />}
          {!roleLoading && role === 'CUSTOMER' && <CustomerNavBar />}

          <nav className="desktop-nav">
            <NavLink
                to="/"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {t('nav.home', 'Accueil')}
            </NavLink>

            <NavLink
                to="/residential-projects"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {t('nav.projects', 'Projets résidentiels')}
            </NavLink>

            <NavLink
                to="/renovations"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {t('nav.renovation', 'Rénovation')}
            </NavLink>

            <NavLink
                to="/projectmanagement"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {t('nav.projectManagement', 'Gestion de projet')}
            </NavLink>

            <NavLink
                to="/realisations"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {t('nav.realisations', 'Réalisations')}
            </NavLink>

            <NavLink
                to="/contact"
                className={({ isActive }) => (isActive ? 'active' : '')}
            >
              Contact
            </NavLink>
          </nav>

          <div className="nav-actions">
            {!isAuthenticated && (
                <button type="button" className="btn-portal" onClick={goToPortal}>
                  {t('nav.accessPortal', 'Access Portal')} <span className="arrow">→</span>
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

          <button
              className="mobile-menu-toggle"
              onClick={toggleMobileMenu}
              aria-label="Toggle menu"
              type="button"
          >
          <span className={isMobileMenuOpen ? 'hamburger open' : 'hamburger'}>
            <span></span>
            <span></span>
            <span></span>
          </span>
          </button>
        </div>

        <nav className={`mobile-nav ${isMobileMenuOpen ? 'open' : ''}`}>
          <NavLink
              to="/"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            {t('nav.home', 'Accueil')}
          </NavLink>

          <NavLink
              to="/residential-projects"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            {t('nav.projects', 'Projets résidentiels')}
          </NavLink>

          <NavLink
              to="/renovations"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            {t('nav.renovation', 'Rénovation')}
          </NavLink>

          <NavLink
              to="/projectmanagement"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            {t('nav.projectManagement', 'Gestion de projet')}
          </NavLink>

          <NavLink
              to="/realisations"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            {t('nav.realisations', 'Réalisations')}
          </NavLink>

          <NavLink
              to="/contact"
              className={({ isActive }) => (isActive ? 'active' : '')}
              onClick={() => setIsMobileMenuOpen(false)}
          >
            Contact
          </NavLink>

          <div className="mobile-actions">
            {!isAuthenticated && (
                <button type="button" className="btn-portal" onClick={goToPortal}>
                  {t('nav.accessPortal', 'Access Portal')} <span className="arrow">→</span>
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
