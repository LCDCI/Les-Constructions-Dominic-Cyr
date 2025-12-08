import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import '../styles/AppNavBar.css';
import OwnerNavBar from '../components/OwnerNavBar';
import SalespersonNavBar from '../components/SalespersonNavBar';

export default function AppNavBar() {
  const { i18n, t } = useTranslation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const currentLanguage = i18n.language || 'en';
  const isFrench = currentLanguage === 'fr';

  const toggleLanguage = () => {
    const newLang = isFrench ? 'en' : 'fr';
    i18n.changeLanguage(newLang);
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(prev => !prev);
  };

  return (
    <header className="site-nav">
      <div className="site-nav-inner">
          <SalespersonNavBar />
          <OwnerNavBar />

        {/* Desktop Navigation */}
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

        {/* Action Buttons */}
        <div className="nav-actions">
          <button className="btn-signin">
            {t('nav.signIn', 'Sign in')} <span className="arrow">→</span>
          </button>
          <button className="btn-get-started">
            {t('nav.getStarted', 'Get Started')}
          </button>
          <button 
            className="btn-language"
            onClick={toggleLanguage}
            aria-label="Toggle language"
          >
            {isFrench ? 'FR' : 'EN'}
          </button>
        </div>

        {/* Mobile Menu Toggle */}
        <button
          className="mobile-menu-toggle"
          onClick={toggleMobileMenu}
          aria-label="Toggle menu"
        >
          <span className={isMobileMenuOpen ? 'hamburger open' : 'hamburger'}>
            <span></span>
            <span></span>
            <span></span>
          </span>
        </button>
      </div>

      {/* Mobile Navigation */}
      <nav className={`mobile-nav ${isMobileMenuOpen ? 'open' : ''}`}>
        <NavLink
          to="/projects"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          {t('nav.projects', 'Projets résidentiels')}
        </NavLink>
        <NavLink
          to="/renovation"
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
        <div className="mobile-actions">
          <button className="btn-signin">
            {t('nav.signIn', 'Sign in')} <span className="arrow">→</span>
          </button>
          <button className="btn-get-started">
            {t('nav.getStarted', 'Get Started')}
          </button>
          <button 
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
