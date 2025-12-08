import React from 'react';
import InquiryForm from '../components/InquiryForm';
import '../styles/contact.css';

export default function ContactPage() {
  return (
    <div className="contact-page">
      <section className="contact-hero">
        <div className="contact-hero__content">
          <p className="eyebrow">Nous joindre</p>
          <h1>Nous collaborons avec vous à la réalisation de vos rêves !</h1>
          <p className="subhead">
            Votre maison constitue l’un des plus importants investissements que
            vous ferez dans votre vie. Optez pour une valeur sûre et sans pareil
            avec Les Constructions Dominic Cyr Inc.
          </p>
        </div>
      </section>

      <section className="contact-body">
        <div className="contact-grid">
          <div className="contact-card">
            <h2>Coordonnées</h2>
            <div className="info-item">
              <strong>Les Constructions Dominic Cyr Inc.</strong>
              <p>155 rue Bourgeois</p>
              <p>St-Mathieu-de-Beloeil (Québec)</p>
              <p>J3G 0M9</p>
            </div>
            <div className="info-item">
              <strong>Téléphone</strong>
              <p>
                <a href="tel:+15141234567">514-123-4567</a>
              </p>
            </div>
            <div className="info-item">
              <strong>Courriel</strong>
              <p>
                <a href="mailto:constructions.dcyr@gmail.com">
                  constructions.dcyr@gmail.com
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>Régie du bâtiment du Québec</strong>
              <p>No licence : 8356-0169-03</p>
              <p>
                <a
                  href="https://www.rbq.gouv.qc.ca"
                  target="_blank"
                  rel="noreferrer"
                >
                  rbq.gouv.qc.ca
                </a>
              </p>
            </div>
            <div className="info-item">
              <strong>Garantie de construction résidentielle</strong>
              <p>No accréditation : 11084</p>
              <p>
                <a
                  href="https://www.garantiegcr.com"
                  target="_blank"
                  rel="noreferrer"
                >
                  garantiegcr.com
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
