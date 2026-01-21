import React from 'react';
import { FaUser, FaClock, FaMapMarkerAlt } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import '../../styles/Footers/ProjectsFooter.css';

export default function AppFooter() {
  const { t } = useTranslation();

  return (
    <footer className="app-footer">
      <div className="footer-inner">
        <div className="footer-section">
          <div className="footer-icon">
            <FaUser />
          </div>
          <h3 className="footer-title">
            {t('footer.contactTitle', 'Contact')}
          </h3>
          <div className="footer-content">
            <p className="footer-item">
              {t('footer.contactName', 'Isabelle Misiazeck')}
            </p>
            <p className="footer-item">
              {t('footer.contactPhone', '514-123-4567')}
            </p>
            <a
              href={`mailto:${t('footer.contactEmail', 'isabelle.misiazeck@foresta.ca')}`}
              className="footer-link"
            >
              {t('footer.contactEmail', 'isabelle.misiazeck@foresta.ca')}
            </a>
          </div>
        </div>

        <div className="footer-section">
          <div className="footer-icon">
            <FaClock />
          </div>
          <h3 className="footer-title">
            {t('footer.hoursTitle', 'Opening Hours')}
          </h3>
          <div className="footer-content">
            <p className="footer-item">
              {t('footer.hoursWeekdays', 'Monday to Wednesday: 1 p.m to 7 p.m')}
            </p>
            <p className="footer-item">
              {t(
                'footer.hoursWeekend',
                'Saturday and Sunday: 11 a.m. to 5 p.m.'
              )}
            </p>
          </div>
        </div>

        {/* Office Section */}
        <div className="footer-section">
          <div className="footer-icon">
            <FaMapMarkerAlt />
          </div>
          <h3 className="footer-title">{t('footer.officeTitle', 'Office')}</h3>
          <div className="footer-content">
            <p className="footer-item">
              {t('footer.officeAddress1', '104 rue du Boisé')}
            </p>
            <p className="footer-item">
              {t('footer.officeAddress2', 'St-Alphonse de Granby')}
            </p>
            <p className="footer-item">
              {t('footer.officeAddress3', 'Granby, QC J2J 2X4')}
            </p>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <p className="footer-copyright">
          {t(
            'footer.copyright',
            '© 2025 Les Constructions Dominic Cyr. All rights reserved.'
          )}
        </p>
      </div>
    </footer>
  );
}
