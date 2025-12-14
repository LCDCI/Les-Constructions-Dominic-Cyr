import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import '../styles/AppNavBar.css';
import OwnerNavBar from '../components/OwnerNavBar';
import SalespersonNavBar from '../components/SalespersonNavBar';
import ContractorNavBar from '../components/ContractorNavBar';
import CustomerNavBar from '../components/CustomerNavBar';

export default function AppNavBar() {
  const { i18n, t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth0();

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

  return (
    <header className="site-nav">
      <div className="site-nav-inner">
        <SalespersonNavBar />
        <OwnerNavBar />
        <ContractorNavBar />
        <CustomerNavBar />

        <nav className="desktop-nav">
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

          <button type="button" className="btn-get-started">
            {t('nav.getStarted', 'Get Started')}
          </button>

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

          <button type="button" className="btn-get-started">
            {t('nav.getStarted', 'Get Started')}
          </button>

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
