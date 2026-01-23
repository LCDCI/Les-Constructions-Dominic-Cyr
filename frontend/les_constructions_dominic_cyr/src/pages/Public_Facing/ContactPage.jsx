import React from 'react';
import { HiLocationMarker } from 'react-icons/hi';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import InquiryForm from '../../components/InquiryForm';
import '../../styles/Public_Facing/contact.css';
import HomeFooter from '../../components/Footers/HomeFooter';

export default function ContactPage() {
  const { t, isLoading, currentLanguage } = usePageTranslations('contact');

  const fallback = {
    fr: {
      hero: {
        eyebrow: 'Nous joindre',
        title: 'Nous collaborons avec vous à la réalisation de vos rêves !',
        subtitle:
          "Votre maison constitue l'un des plus importants investissements que vous ferez dans votre vie. Optez pour une valeur sûre et sans pareil avec Les Constructions Dominic Cyr Inc.",
      },
      contactInfo: {
        title: 'Coordonnées',
        companyName: 'Les Constructions Dominic Cyr Inc.',
        addressLine1: '155 rue Bourgeois',
        addressLine2: 'St-Mathieu-de-Beloeil (Québec)',
        addressLine3: 'J3G 0M9',
        directions: "Obtenir l'itinéraire",
        phoneLabel: 'Téléphone',
        phone: '514-123-4567',
        emailLabel: 'Courriel',
        email: 'constructions.dcyr@gmail.com',
        rbqLabel: 'Régie du bâtiment du Québec',
        rbqLicense: 'No licence : 8356-0169-03',
        rbqWebsite: 'rbq.gouv.qc.ca',
        gcqLabel: 'Garantie de construction résidentielle',
        gcqAccreditation: 'No accréditation : 11084',
        gcqWebsite: 'garantiegcr.com',
      },
    },
    en: {
      hero: {
        eyebrow: 'Contact Us',
        title: 'We collaborate with you to realize your dreams!',
        subtitle:
          'Your home is one of the most important investments you will make in your life. Choose a safe and unparalleled value with Les Constructions Dominic Cyr Inc.',
      },
      contactInfo: {
        title: 'Contact Information',
        companyName: 'Les Constructions Dominic Cyr Inc.',
        addressLine1: '155 rue Bourgeois',
        addressLine2: 'St-Mathieu-de-Beloeil (Quebec)',
        addressLine3: 'J3G 0M9',
        directions: 'Get Directions',
        phoneLabel: 'Phone',
        phone: '514-123-4567',
        emailLabel: 'Email',
        email: 'constructions.dcyr@gmail.com',
        rbqLabel: 'Régie du bâtiment du Québec',
        rbqLicense: 'License No: 8356-0169-03',
        rbqWebsite: 'rbq.gouv.qc.ca',
        gcqLabel: 'Residential Construction Guarantee',
        gcqAccreditation: 'Accreditation No: 11084',
        gcqWebsite: 'garantiegcr.com',
      },
    },
  };

  const f = fallback[currentLanguage] || fallback.fr;

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="contact-page">
      <section className="contact-hero">
        <div className="contact-hero__content">
          <p className="eyebrow">{t('hero.eyebrow', f.hero.eyebrow)}</p>
          <h1>{t('hero.title', f.hero.title)}</h1>
          <p className="subhead">{t('hero.subtitle', f.hero.subtitle)}</p>
        </div>
      </section>

      <section className="contact-body">
        <div className="contact-grid">
          <InquiryForm className="contact-form-wrapper" />

          <div className="contact-card">
            <h2>{t('contactInfo.title', f.contactInfo.title)}</h2>
            <div className="info-item">
              <strong>
                {t('contactInfo.companyName', f.contactInfo.companyName)}
              </strong>
              <p>{t('contactInfo.addressLine1', f.contactInfo.addressLine1)}</p>
              <p>{t('contactInfo.addressLine2', f.contactInfo.addressLine2)}</p>
              <p>{t('contactInfo.addressLine3', f.contactInfo.addressLine3)}</p>
              <p>
                <a
                  href="https://www.google.com/maps/dir/?api=1&destination=155+Rue+Bourgeois,Saint-Mathieu-de-Beloeil,QC+J3G+0M9"
                  target="_blank"
                  rel="noreferrer"
                  className="directions-link"
                >
                  <HiLocationMarker />
                  {t('contactInfo.directions', f.contactInfo.directions)}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>
                {t('contactInfo.phoneLabel', f.contactInfo.phoneLabel)}
              </strong>
              <p>
                <a href="tel:+15141234567">
                  {t('contactInfo.phone', f.contactInfo.phone)}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>
                {t('contactInfo.emailLabel', f.contactInfo.emailLabel)}
              </strong>
              <p>
                <a href="mailto:constructions.dcyr@gmail.com">
                  {t('contactInfo.email', f.contactInfo.email)}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>
                {t('contactInfo.rbqLabel', f.contactInfo.rbqLabel)}
              </strong>
              <p>{t('contactInfo.rbqLicense', f.contactInfo.rbqLicense)}</p>
              <p>
                <a
                  href="https://www.rbq.gouv.qc.ca"
                  target="_blank"
                  rel="noreferrer"
                >
                  {t('contactInfo.rbqWebsite', f.contactInfo.rbqWebsite)}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>
                {t('contactInfo.gcqLabel', f.contactInfo.gcqLabel)}
              </strong>
              <p>
                {t(
                  'contactInfo.gcqAccreditation',
                  f.contactInfo.gcqAccreditation
                )}
              </p>
              <p>
                <a
                  href="https://www.garantiegcr.com"
                  target="_blank"
                  rel="noreferrer"
                >
                  {t('contactInfo.gcqWebsite', f.contactInfo.gcqWebsite)}
                </a>
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
