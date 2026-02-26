import React from 'react';
import { Link } from 'react-router-dom';
import {
  FaMapMarkerAlt,
  FaShieldAlt,
  FaPhoneAlt,
  FaEnvelope,
  FaArrowRight,
} from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import '../../styles/Footers/HomeFooter.css';

export default function HomeFooter() {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();

  return (
    <footer className="app-footer">
      <div className="footer-inner">
        <div className="footer-section">
          <div className="footer-title-group">
            <FaMapMarkerAlt className="footer-section-icon" />
            <h3 className="footer-title">
              {t('footer.information', 'Information')}
            </h3>
          </div>
          <div className="footer-content">
            <div className="footer-item-with-icon">
              <FaPhoneAlt className="footer-mini-icon" />
              <p className="footer-item">{t('footer.phone', '514-705-7848')}</p>
            </div>
            <div className="footer-item-with-icon">
              <FaEnvelope className="footer-mini-icon" />
              <a
                href={`mailto:${t('footer.email', 'constructions.dcyr@gmail.com')}`}
                className="footer-link"
              >
                {t('footer.email', 'constructions.dcyr@gmail.com')}
              </a>
            </div>
            <p className="footer-item" style={{ marginTop: '8px' }}>
              {t('footer.address1', '155 rue Bourgeois')}
            </p>
            <p className="footer-item">
              {t('footer.address2', 'St-Mathieu-de-Beloeil (Québec) J3G 0M9')}
            </p>
          </div>
        </div>

        <div className="footer-section footer-section-middle">
          <div className="footer-vertical-divider"></div>
          <Link to="/contact" className="footer-contact-link">
            {t('footer.contactUs', 'Contact Us')}
            <FaArrowRight className="footer-arrow-icon" />
          </Link>
        </div>

        <div className="footer-section">
          <div className="footer-title-group">
            <FaShieldAlt className="footer-section-icon" />
            <h3 className="footer-title">
              {t('footer.accreditations', 'Accréditations')}
            </h3>
          </div>
          <div className="footer-content">
            <a
              href="https://www.pes.rbq.gouv.qc.ca/RegistreLicences/FicheDetenteur/8356016903?mode=Entreprise"
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
            >
              <strong>
                {t('footer.rbqLabel', 'Régie du Bâtiment du Québec')}:
              </strong>{' '}
              {t('footer.rbq', '8356-0169-03')}
            </a>

            <a
              href="https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html"
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
              style={{ marginTop: '8px' }}
            >
              <strong>
                {t('footer.gcrLabel', 'Garantie Construction Résidentielle')}:
              </strong>{' '}
              {t('footer.gcr', '11084')}
            </a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p className="footer-copyright">
          {t(
            'footer.copyright',
            '© {{year}} Les Constructions Dominic Cyr Inc. Tous droits réservés.',
            { year: currentYear }
          )}
        </p>
      </div>
    </footer>
  );
}
