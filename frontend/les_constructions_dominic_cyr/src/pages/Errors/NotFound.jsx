import React from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Errors/ErrorPage.css';

export default function NotFound() {
  const { t, isLoading } = usePageTranslations('notfound');

  if (isLoading) {
    return (
      <div className="error-page">
        <div className="error-page-content">
          <div className="error-icon">404</div>
          <h1>Page Not Found</h1>
          <p className="error-message">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="error-page">
      <div className="error-page-content">
        <div className="error-icon">{t('errorIcon', '404')}</div>
        <h1>{t('title', 'Page Not Found')}</h1>
        <p className="error-message">
          {t('message', "We're sorry, but the page you're looking for doesn't exist or has been moved.")}
        </p>
        <p className="error-description">
          {t('description', 'The resource you requested could not be found. Please check the URL or return to the home page.')}
        </p>
        <Link to="/" className="error-button">
          {t('returnButton', 'Return to Home Page')}
        </Link>
      </div>
    </div>
  );
}
