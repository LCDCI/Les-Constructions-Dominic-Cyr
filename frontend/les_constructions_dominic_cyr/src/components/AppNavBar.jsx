import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import '../styles/AppNavBar.css';
import OwnerNavBar from '../components/OwnerNavBar';
import SalespersonNavBar from '../components/SalespersonNavBar';

export default function AppNavBar() {
  const [language, setLanguage] = useState('FR');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleLanguage = () => {
    setLanguage(prev => prev === 'FR' ? 'EN' : 'FR');
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
            to="/projets-residentiels"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Residential Projects
          </NavLink>
          <NavLink
            to="/renovations"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Renovations
          </NavLink>
          <NavLink
            to="/gestion-de-projet"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Project Management
          </NavLink>
          <NavLink
            to="/realisations"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Realizations
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
          <button className="btn-get-started">
            Portal Access
          </button>
          <button 
            className="btn-language"
            onClick={toggleLanguage}
            aria-label="Toggle language"
          >
            {language}
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
          to="/projets-residentiels"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          Residential Projects
        </NavLink>
        <NavLink
          to="/renovations"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          Renovations
        </NavLink>
        <NavLink
          to="/gestion-de-projet"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          Gestion de projet
        </NavLink>
        <NavLink
          to="/realisations"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          Realizations
        </NavLink>
        <div className="mobile-actions">
          <button className="btn-get-started">
            Portal Access
          </button>
          <button 
            className="btn-language"
            onClick={toggleLanguage}
            aria-label="Toggle language"
          >
            {language}
          </button>
        </div>
      </nav>
    </header>
  );
}
