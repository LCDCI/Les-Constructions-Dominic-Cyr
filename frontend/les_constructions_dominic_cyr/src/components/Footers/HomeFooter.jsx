import React from 'react';
import {
  FaPhoneAlt,
  FaEnvelope,
  FaMapMarkerAlt,
  FaShieldAlt,
  FaIdCard,
} from 'react-icons/fa';
import '../../styles/Footers/HomeFooter.css';

export default function HomeFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="app-footer">
      <div className="footer-inner">
        <div className="footer-section">
          <h3 className="footer-title">Contact</h3>
          <div className="footer-content">
            <div className="footer-item-with-icon">
              <FaPhoneAlt className="footer-mini-icon" />
              <p className="footer-item">514-123-4567</p>
            </div>
            <div className="footer-item-with-icon">
              <FaEnvelope className="footer-mini-icon" />
              <a
                href="mailto:constructions.dcyr@gmail.com"
                className="footer-link"
              >
                constructions.dcyr@gmail.com
              </a>
            </div>
          </div>
        </div>

        <div className="footer-section">
          <div className="footer-title-group">
            <FaMapMarkerAlt className="footer-section-icon" />
            <h3 className="footer-title">Localisation</h3>
          </div>
          <div className="footer-content">
            <p className="footer-item">155 rue Bourgeois</p>
            <p className="footer-item">
              St-Mathieu-de-Beloeil (Québec) J3G 0M9
            </p>
          </div>
        </div>

        <div className="footer-section">
          <div className="footer-title-group">
            <FaShieldAlt className="footer-section-icon" />
            <h3 className="footer-title">Accréditations</h3>
          </div>
          <div className="footer-content">
            <p className="footer-item">
              <strong>RBQ:</strong> 8356-0169-03
            </p>
            <a
              href="https://www.pes.rbq.gouv.qc.ca/RegistreLicences/Recherche/Resultats/FicheDetenteur?mode=Entreprise&crit=r1TSc6INGEWKtcUK6jcX9%2FEByYB22EJQyiTukjrpiNiBdLFBdmxsUQD5hDhRnpniSWRgQPOq4Oq7AYd4UqvduXNoxS7GrtZarAE5%2FQv2Tj9k9EtXsKZODcycaRS2vCDNPQ6XfEMFITtraZrvf%2BgSWYCujnyk5pjSv%2Bn4i4fIsPKcf%2BPb8Z5RSbvKvHajTbuyX5Dvx9GsimL%2FOHlp6DxDoJ5FP8q94u2ewsoBsQq0VhthO9VtsuE1wYESAcFhyiUIbnNiMS6dBHk2XPVhhCVXDs93MtpN4cuBS%2B3XvsKTj7ZIHNuYwyiYsaYFfpuOofJulS3vTsVihLb7898tOi0SsOtAnfY2%2BFYerzER6TO4TIM7Fwh25dHZC5AARA6k0Q1JFPckDujovDNBflrWPX6ipKRRisQTIjkNwowf0MZXpEs%2F9t2hOEjx29qd%2Fp%2FgVVXh0pnV%2BAcmHQdRVcT0Ib9L2OKI53F4OEdOzktVpYlvF5e%2Byd4pInDRrybDEWGQd2tXuL8EwlgAwkwSYbbVXhT%2BGvjvvD%2B%2BOAeNLHpvnyGWvd%2BqyeTv%2Bxap7%2FwaThYJmcMvtrVpO3LhkjxCVdyyhvZ4rR6L%2BGVoit76HJclXqBxW4HyXwZOY7DMXlaBZFEJt2JcrKLEn5aHNyjOPYVKcq9qorx7obYiAyJ57dVpEs8BqXg645HMfZqPMSmTQPiCBoHL&ent=Dfp5TGXsmaSEEtcnF555TQ%3D%3D"
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
            >
              rbq.gouv.qc.ca
            </a>

            <p className="footer-item" style={{ marginTop: '8px' }}>
              <strong>GCR:</strong> 11084
            </p>
            <a
              href="https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html
"
              target="_blank"
              rel="noopener noreferrer"
              className="footer-link"
            >
              garantiegcr.com
            </a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p className="footer-copyright">
          © {currentYear} Les Constructions Dominic Cyr Inc. Tous droits
          réservés.
        </p>
      </div>
    </footer>
  );
}
