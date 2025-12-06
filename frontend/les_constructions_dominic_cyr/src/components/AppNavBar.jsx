import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import '../styles/AppNavBar.css';

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
        {/* Logo */}
        <div className="brand">
          <img 
            src="https://via.placeholder.com/120x40?text=DCYR+Logo" 
            alt="DCYR Logo" 
            className="logo-image"
          />
        </div>

        {/* Desktop Navigation */}
        <nav className="desktop-nav">
          <NavLink
            to="/projets-residentiels"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Projets résidentiels
          </NavLink>
          <NavLink
            to="/renovation"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Rénovation
          </NavLink>
          <NavLink
            to="/gestion-de-projet"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Gestion de projet
          </NavLink>
          <NavLink
            to="/realisations"
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            Réalisations
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
            Sign in <span className="arrow">→</span>
          </button>
          <button className="btn-get-started">
            Get Started
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
          Projets résidentiels
        </NavLink>
        <NavLink
          to="/renovation"
          className={({ isActive }) => (isActive ? 'active' : '')}
          onClick={() => setIsMobileMenuOpen(false)}
        >
          Rénovation
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
          Réalisations
        </NavLink>
        <div className="mobile-actions">
          <button className="btn-signin">
            Sign in <span className="arrow">→</span>
          </button>
          <button className="btn-get-started">
            Get Started
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
