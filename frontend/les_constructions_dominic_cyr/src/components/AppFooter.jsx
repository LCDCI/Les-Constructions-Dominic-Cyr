import React from 'react';
import { FaUser, FaClock, FaMapMarkerAlt } from 'react-icons/fa';
import '../styles/AppFooter.css';

export default function AppFooter() {
  return (
    <footer className="app-footer">
      <div className="footer-inner">
        {/* Contact Section */}
        <div className="footer-section">
          <div className="footer-icon">
            <FaUser />
          </div>
          <h3 className="footer-title">Contact</h3>
          <div className="footer-content">
            <p className="footer-item">Isabelle Misiazeck</p>
            <p className="footer-item">514-123-4567</p>
            <a href="mailto:isabelle.misiazeck@foresta.ca" className="footer-link">
              isabelle.misiazeck@foresta.ca
            </a>
          </div>
        </div>

        {/* Opening Hours Section */}
        <div className="footer-section">
          <div className="footer-icon">
            <FaClock />
          </div>
          <h3 className="footer-title">Opening Hours</h3>
          <div className="footer-content">
            <p className="footer-item">Monday to Wednesday: 1 p.m to 7 p.m</p>
            <p className="footer-item">Saturday and Sunday: 11 a.m. to 5 p.m.</p>
          </div>
        </div>

        {/* Office Section */}
        <div className="footer-section">
          <div className="footer-icon">
            <FaMapMarkerAlt />
          </div>
          <h3 className="footer-title">Office</h3>
          <div className="footer-content">
            <p className="footer-item">104 rue du Boisé</p>
            <p className="footer-item">St-Alphonse de Granby</p>
            <p className="footer-item">Granby, QC J2J 2X4</p>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <p className="footer-copyright">
          © 2025 Les Constructions Dominic Cyr. All rights reserved.
        </p>
      </div>
    </footer>
  );
}

