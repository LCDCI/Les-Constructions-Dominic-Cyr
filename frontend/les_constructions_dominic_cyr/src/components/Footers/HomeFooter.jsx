import React, { useEffect, useState } from 'react';
import {
  FaMapMarkerAlt,
  FaShieldAlt,
  FaPhoneAlt,
  FaEnvelope,
} from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { fetchPublicSiteContent } from '../../features/siteContent/api/siteContentApi';
import '../../styles/Footers/HomeFooter.css';

export default function HomeFooter() {
  const { t, i18n } = useTranslation();
  const [content, setContent] = useState({});
  const currentYear = new Date().getFullYear();
  const language = i18n.language?.startsWith('fr') ? 'fr' : 'en';

  useEffect(() => {
    let active = true;
    fetchPublicSiteContent(language, 'footer').then(items => {
      if (!active) return;
      setContent(
        Object.fromEntries(
          (Array.isArray(items) ? items : []).map(item => [
            item.pageKey,
            item.contentText || '',
          ])
        )
      );
    });
    return () => {
      active = false;
    };
  }, [language]);

  const value = (key, fallback, options) =>
    content[key]
      ? content[key].replace(
          /\{\{year\}\}/g,
          String(options?.year ?? currentYear)
        )
      : t(`footer.${key}`, fallback, options);

  return (
    <footer className="app-footer home-footer">
      <div className="footer-inner">
        <div className="footer-section">
          <div className="footer-title-group">
            <FaMapMarkerAlt className="footer-section-icon" />
            <h3 className="footer-title">
              {value('information', 'Informations')}
            </h3>
          </div>
          <div className="footer-content">
            <div className="footer-item-with-icon">
              <FaPhoneAlt className="footer-mini-icon" />
              <p className="footer-item">{value('phone', '514-705-7848')}</p>
            </div>
            <div className="footer-item-with-icon">
              <FaEnvelope className="footer-mini-icon" />
              <a
                href={`mailto:${value('email', 'constructions.dcyr@gmail.com')}`}
                className="footer-link"
              >
                {value('email', 'constructions.dcyr@gmail.com')}
              </a>
            </div>
          </div>
        </div>

        <div className="footer-section footer-section-middle">
          <Link to="/contact" className="footer-contact-link">
            {t('footer.contactUs', 'Contact Us')}
          </Link>
        </div>

        <div className="footer-section">
          <div className="footer-title-group">
            <FaShieldAlt className="footer-section-icon" />
            <h3 className="footer-title">
              {value('accreditations', 'Accréditations')}
            </h3>
          </div>
          <div className="footer-content">
            <a
              href={value(
                'rbqUrl',
                'https://www.pes.rbq.gouv.qc.ca/RegistreLicences/FicheDetenteur/8356016903?mode=Entreprise'
              )}
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
            >
              <strong>
                {value('rbqLabel', 'Régie du Bâtiment du Québec')}:
              </strong>{' '}
              {value('rbq', '8356-0169-03')}
            </a>

            <a
              href={value(
                'gcrUrl',
                'https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html'
              )}
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
              style={{ marginTop: '8px' }}
            >
              <strong>
                {value('gcrLabel', 'Garantie Construction Résidentielle')}:
              </strong>{' '}
              {value('gcr', '11084')}
            </a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p className="footer-copyright">
          {value(
            'copyright',
            '© {{year}} Les Constructions Dominic Cyr Inc. Tous droits réservés.',
            { year: currentYear }
          )}
        </p>
      </div>
    </footer>
  );
}
