import React from 'react';
import { HiLocationMarker } from 'react-icons/hi';
import { usePageTranslations } from '../hooks/usePageTranslations';
import InquiryForm from '../components/InquiryForm';
import '../styles/contact.css';

export default function ContactPage() {
  const { t, isLoading } = usePageTranslations('contact');

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="contact-page">
      <section className="contact-hero">
        <div className="contact-hero__content">
          <p className="eyebrow">{t('hero.eyebrow')}</p>
          <h1>{t('hero.title')}</h1>
          <p className="subhead">{t('hero.subtitle')}</p>
        </div>
      </section>

      <section className="contact-body">
        <div className="contact-grid">
          <div className="contact-card">
            <h2>{t('contactInfo.title')}</h2>
            <div className="info-item">
              <strong>{t('contactInfo.companyName')}</strong>
              <p>{t('contactInfo.addressLine1')}</p>
              <p>{t('contactInfo.addressLine2')}</p>
              <p>{t('contactInfo.addressLine3')}</p>
              <p>
                <a
                  href="https://www.google.com/maps/dir/?api=1&destination=155+Rue+Bourgeois,Saint-Mathieu-de-Beloeil,QC+J3G+0M9"
                  target="_blank"
                  rel="noreferrer"
                  className="directions-link"
                >
                  <HiLocationMarker /> {t('contactInfo.directions')}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>{t('contactInfo.phoneLabel')}</strong>
              <p>
                <a href="tel:+15141234567">{t('contactInfo.phone')}</a>
              </p>
            </div>
            <div className="info-item">
              <strong>{t('contactInfo.emailLabel')}</strong>
              <p>
                <a href="mailto:constructions.dcyr@gmail.com">
                  {t('contactInfo.email')}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>{t('contactInfo.rbqLabel')}</strong>
              <p>{t('contactInfo.rbqLicense')}</p>
              <p>
                <a
                  href="https://www.rbq.gouv.qc.ca"
                  target="_blank"
                  rel="noreferrer"
                >
                  {t('contactInfo.rbqWebsite')}
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>{t('contactInfo.gcqLabel')}</strong>
              <p>{t('contactInfo.gcqAccreditation')}</p>
              <p>
                <a
                  href="https://www.garantiegcr.com"
                  target="_blank"
                  rel="noreferrer"
                >
                  {t('contactInfo.gcqWebsite')}
                </a>
              </p>
            </div>
          </div>

          <InquiryForm className="contact-form-wrapper" />
        </div>
      </section>
    </div>
  );
}
