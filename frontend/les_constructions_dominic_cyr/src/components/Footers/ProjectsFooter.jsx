import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { FaClock, FaPhoneAlt, FaEnvelope, FaArrowRight } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import '../../styles/Footers/HomeFooter.css';
import '../../styles/Footers/ProjectsFooter.css';
import { projectApi } from '../../features/projects/api/projectApi';

const PROJECT_INFO = {
  'proj-001-foresta': {
    contact: {
      name: 'Isabelle Misiaszek',
      phone: '514-705-7848',
      email: 'isabellemisi1@hotmail.com',
    },
    hours: {
      weekdays: 'Lundi au mercredi de 13h à 19h',
      weekend: 'Samedi et dimanche de 11h à 17h',
    },
    location: {
      address1: '104 rue du Boisé',
      address2: 'St-Alphonse de Granby',
      mapLink:
        'https://www.google.com/maps/dir/?api=1&destination=104+rue+du+Boisé+St-Alphonse+de+Granby',
    },
  },
  'proj-002-panorama': {
    contact: {
      name: 'Dominic Cyr',
      phone: '514-705-7848',
      email: 'constructions.dcyr@gmail.com',
    },
    hours: {
      weekdays: 'Lundi au mercredi de 13h à 19h',
      weekend: 'Samedi et dimanche de 11h à 17h',
    },
    location: {
      address1: '14 rue du Ruisseau',
      address2: 'Sutton',
      mapLink:
        'https://www.google.com/maps/dir/?api=1&destination=14+rue+du+Ruisseau+Sutton',
    },
  },
};

export default function ProjectsFooter({ projectId }) {
  const { i18n, t } = useTranslation();
  const currentYear = new Date().getFullYear();
  const isFrench = i18n.language?.startsWith('fr');
  const [project, setProject] = useState(null);

  useEffect(() => {
    let active = true;
    projectApi
      .getProjectById(projectId)
      .then(result => {
        if (active) setProject(result);
      })
      .catch(() => {
        if (active) setProject(null);
      });
    return () => {
      active = false;
    };
  }, [projectId]);

  const projectData =
    PROJECT_INFO[projectId] || PROJECT_INFO['proj-001-foresta'];

  return (
    <footer
      className="app-footer"
      data-project={projectId}
      style={{
        '--icon-primary': project?.primaryColor || undefined,
        '--icon-secondary': project?.tertiaryColor || undefined,
        '--project-tertiary': project?.tertiaryColor || project?.buyerColor,
        '--project-buyer': project?.buyerColor || project?.tertiaryColor,
      }}
    >
      <div className="footer-inner">
        <div className="footer-section">
          <div className="footer-title-group">
            <FaClock className="footer-section-icon" />
            <h3 className="footer-title">
              {t('footer.hoursTitle', "Heures d'ouverture")}
            </h3>
          </div>
          <div className="footer-content">
            <p className="footer-item">
              {t(
                'footer.projectHoursWeekdays',
                isFrench
                  ? 'Lundi au Vendredi: À venir'
                  : 'Monday to Friday: Coming Shortly'
              )}
            </p>
            <p className="footer-item">
              {t(
                'footer.projectHoursWeekend',
                isFrench
                  ? 'Samedi et Dimanche: À venir'
                  : 'Saturday and Sunday: Coming Shortly'
              )}
            </p>
          </div>
        </div>

        <div className="footer-section footer-section-middle">
          <div className="footer-vertical-divider"></div>
          <Link to="/contact" className="footer-contact-link">
            {t('footer.contactUs', 'Contactez-nous')}
            <FaArrowRight className="footer-arrow-icon" />
          </Link>
        </div>

        <div className="footer-section">
          <div className="footer-title-group">
            <FaPhoneAlt className="footer-section-icon" />
            <h3 className="footer-title">
              {t('footer.contactTitle', 'Contact')}
            </h3>
          </div>
          <div className="footer-content">
            <p className="footer-item">
              <strong>{projectData.contact.name}</strong>
            </p>
            <div className="footer-item-with-icon">
              <FaPhoneAlt className="footer-mini-icon" />
              <a
                href={`tel:${projectData.contact.phone.replace(/[^0-9]/g, '')}`}
                className="footer-link"
              >
                {projectData.contact.phone}
              </a>
            </div>
            <div className="footer-item-with-icon">
              <FaEnvelope className="footer-mini-icon" />
              <a
                href={`mailto:${projectData.contact.email}`}
                className="footer-link"
              >
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
            '© {{year}} Les Constructions Dominic Cyr Inc. Tous droits réservés.',
            { year: currentYear }
          )}
        </p>
      </div>
    </footer>
  );
}

ProjectsFooter.propTypes = {
  projectId: PropTypes.string,
};
