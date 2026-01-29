import React from 'react';
import PropTypes from 'prop-types';
import { FaClock, FaMapMarkerAlt, FaPhoneAlt, FaEnvelope } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import '../../styles/Footers/ProjectsFooter.css';

const PROJECT_INFO = {
  'proj-001-foresta': {
    contact: {
      name: 'Isabelle Misiazeck',
      phone: '514-123-4567',
      email: 'Isabelle.misiazeck@foresta.ca',
    },
    hours: {
      weekdays: 'Lundi au mercredi de 13h à 19h',
      weekend: 'Samedi et dimanche de 11h à 17h',
    },
    location: {
      address1: '104 rue du Boisé',
      address2: 'St-Alphonse de Granby',
      mapLink: 'https://www.google.com/maps/dir/?api=1&destination=104+rue+du+Boisé+St-Alphonse+de+Granby',
    },
  },
  'proj-002-panorama': {
    contact: {
      name: 'Elyse St-Jean',
      phone: '514-123-4567',
      email: 'elyse.stjean@panorama.ca',
    },
    hours: {
      weekdays: 'Lundi au mercredi de 13h à 19h',
      weekend: 'Samedi et dimanche de 11h à 17h',
    },
    location: {
      address1: '14 rue du Ruisseau',
      address2: 'Sutton',
      mapLink: 'https://www.google.com/maps/dir/?api=1&destination=14+rue+du+Ruisseau+Sutton',
    },
  },
};

export default function ProjectsFooter({ projectId }) {
  const { t } = useTranslation();
  
  const projectData = PROJECT_INFO[projectId] || PROJECT_INFO['proj-001-foresta'];

  return (
    <footer className="app-footer">
      <div className="footer-inner">
        <div className="footer-section">
          <div className="footer-icon">
            <FaClock />
          </div>
          <h3 className="footer-title">
            {t('footer.hoursTitle', 'Heures d\'ouverture')}
          </h3>
          <div className="footer-content">
            <p className="footer-item">
              {projectData.hours.weekdays}
            </p>
            <p className="footer-item">
              {projectData.hours.weekend}
            </p>
          </div>
        </div>

        <div className="footer-section">
          <div className="footer-icon">
            <FaMapMarkerAlt />
          </div>
          <h3 className="footer-title">
            {t('footer.locationTitle', 'Emplacement du projet')}
          </h3>
          <div className="footer-content">
            <p className="footer-item">{projectData.location.address1}</p>
            <p className="footer-item">{projectData.location.address2}</p>
            <a 
              href={projectData.location.mapLink}
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
            >
              {t('footer.getDirections', 'Obtenir l\'itinéraire')}
            </a>
          </div>
        </div>

        <div className="footer-section">
          <div className="footer-icon">
            <FaPhoneAlt />
          </div>
          <h3 className="footer-title">{t('footer.contactTitle', 'Contact')}</h3>
          <div className="footer-content">
            <p className="footer-item"><strong>{projectData.contact.name}</strong></p>
            <div className="footer-item-with-icon">
              <FaPhoneAlt className="footer-mini-icon" />
              <a href={`tel:${projectData.contact.phone.replace(/[^0-9]/g, '')}`} className="footer-link">
                {projectData.contact.phone}
              </a>
            </div>
            <div className="footer-item-with-icon">
              <FaEnvelope className="footer-mini-icon" />
              <a href={`mailto:${projectData.contact.email}`} className="footer-link">
                {projectData.contact.email}
              </a>
            </div>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <p className="footer-copyright">
          {t(
            'footer.copyright',
            '© 2025 Les Constructions Dominic Cyr. Tous droits réservés.'
          )}
        </p>
      </div>
    </footer>
  );
}

ProjectsFooter.propTypes = {
  projectId: PropTypes.string,
};
