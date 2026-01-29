import React from 'react';
import { HiLocationMarker } from 'react-icons/hi';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import InquiryForm from '../../components/InquiryForm';
import '../../styles/Public_Facing/contact.css';

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
        phone: '514-705-7848',
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
        phone: '514-705-7848',
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
                <a href="tel:+15147057848">
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
                  href="https://www.pes.rbq.gouv.qc.ca/RegistreLicences/Recherche/Resultats/FicheDetenteur?mode=Entreprise&crit=r1TSc6INGEWKtcUK6jcX9%2FEByYB22EJQyiTukjrpiNiBdLFBdmxsUQD5hDhRnpniSWRgQPOq4Oq7AYd4UqvduXNoxS7GrtZarAE5%2FQv2Tj9k9EtXsKZODcycaRS2vCDNPQ6XfEMFITtraZrvf%2BgSWYCujnyk5pjSv%2Bn4i4fIsPKcf%2BPb8Z5RSbvKvHajTbuyX5Dvx9GsimL%2FOHlp6DxDoJ5FP8q94u2ewsoBsQq0VhthO9VtsuE1wYESAcFhyiUIbnNiMS6dBHk2XPVhhCVXDs93MtpN4cuBS%2B3XvsKTj7ZIHNuYwyiYsaYFfpuOofJulS3vTsVihLb7898tOi0SsOtAnfY2%2BFYerzER6TO4TIM7Fwh25dHZC5AARA6k0Q1JFPckDujovDNBflrWPX6ipKRRisQTIjkNwowf0MZXpEs%2F9t2hOEjx29qd%2Fp%2FgVVXh0pnV%2BAcmHQdRVcT0Ib9L2OKI53F4OEdOzktVpYlvF5e%2Byd4pInDRrybDEWGQd2tXuL8EwlgAwkwSYbbVXhT%2BGvjvvD%2B%2BOAeNLHpvnyGWvd%2BqyeTv%2Bxap7%2FwaThYJmcMvtrVpO3LhkjxCVdyyhvZ4rR6L%2BGVoit76HJclXqBxW4HyXwZOY7DMXlaBZFEJt2JcrKLEn5aHNyjOPYVKcq9qorx7obYiAyJ57dVpEs8BqXg645HMfZqPMSmTQPiCBoHL&ent=Dfp5TGXsmaSEEtcnF555TQ%3D%3D"
                  target="_blank"
                  rel="noopener noreferrer"
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
                  href="https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html"
                  target="_blank"
                  rel="noopener noreferrer"
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
