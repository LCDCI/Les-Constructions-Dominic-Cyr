import React from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../hooks/usePageTranslations';
import useBackendUser from '../hooks/useBackendUser';
import '../styles/ComingSoonPage.css';

const LOGO_URL =
  'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/main_logo.png';

const DASHBOARD_ROUTES = {
  OWNER: '/owner/dashboard',
  SALESPERSON: '/salesperson/dashboard',
  CONTRACTOR: '/contractor/dashboard',
  CUSTOMER: '/customer/dashboard',
};

export default function ComingSoonPage() {
  const { t, isLoading } = usePageTranslations('comingSoon');
  const { role } = useBackendUser();
  const dashboardUrl = DASHBOARD_ROUTES[role] || '/';

  if (isLoading) {
    return (
      <div className="coming-soon-page">
        <div className="coming-soon-content">
          <img src={LOGO_URL} alt="LCDC Logo" className="coming-soon-logo" />
          <h1>Bientôt disponible</h1>
          <p className="coming-soon-message">Chargement...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="coming-soon-page">
      <div className="coming-soon-content">
        <img
          src={LOGO_URL}
          alt="Les Constructions Dominic Cyr"
          className="coming-soon-logo"
        />
        <h1>{t('title', 'Bientôt disponible')}</h1>
        <p className="coming-soon-message">
          {t('message', 'Cette fonctionnalité est en cours de développement.')}
        </p>
        <p className="coming-soon-description">
          {t(
            'description',
            'Nous travaillons activement sur cette section. Elle sera disponible très prochainement. Merci de votre patience.'
          )}
        </p>
        <Link to={dashboardUrl} className="coming-soon-button">
          {t('returnButton', 'Retour au tableau de bord')}
        </Link>
      </div>
    </div>
  );
}
