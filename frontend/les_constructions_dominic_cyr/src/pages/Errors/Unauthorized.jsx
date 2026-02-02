import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../../hooks/useBackendUser';
import '../../styles/Errors/ErrorPage.css';
import { usePageTranslations } from '../../hooks/usePageTranslations';

export default function Unauthorized() {
  const { t } = usePageTranslations('unauthorized');
  const { isAuthenticated } = useAuth0();
  const { role } = useBackendUser();

  // Determine home path based on role
  const getHomePath = () => {
    if (!isAuthenticated || !role) return '/';
    switch (role) {
      case 'OWNER':
        return '/owner/dashboard';
      case 'SALESPERSON':
        return '/salesperson/dashboard';
      case 'CONTRACTOR':
        return '/contractor/dashboard';
      case 'CUSTOMER':
        return '/customer/dashboard';
      default:
        return '/';
    }
  };

  return (
    <div className="error-page">
      <div className="error-content">
        <h1>{t('title', '🚫 Access Denied')}</h1>
        <p>
          {t(
            'message',
            'You do not have permission to access this page or feature.'
          )}
        </p>
        <p className="error-hint">
          {t(
            'hint',
            'This area is restricted based on your role. If you believe this is an error, please contact your administrator.'
          )}
        </p>
        <div className="error-actions">
          <Link to={getHomePath()} className="btn-primary">
            {t('goToDashboard', 'Go to Dashboard')}
          </Link>
          <Link to="/" className="btn-secondary">
            {t('goHome', 'Go Home')}
          </Link>
        </div>
      </div>
    </div>
  );
}
