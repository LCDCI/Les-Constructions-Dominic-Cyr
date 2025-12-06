import React from 'react';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../hooks/usePageTranslations';
import './ErrorPage.css';

export default function ServerError() {
  const { t, isLoading } = usePageTranslations('servererror');

  if (isLoading) {
    return (
      <div className="error-page">
        <div className="error-page-content">
          <div className="error-icon">500</div>
          <h1>Server Error</h1>
          <p className="error-message">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="error-page">
      <div className="error-page-content">
        <div className="error-icon">{t('errorIcon', '500')}</div>
        <h1>{t('title', 'Server Error')}</h1>
        <p className="error-message">
          {t('message', "We're sorry, but something went wrong on our end.")}
        </p>
        <p className="error-description">
          {t('description', 'Our team has been notified and is working to fix the issue. Please try again later or return to the home page.')}
        </p>
        <Link to="/" className="error-button">
          {t('returnButton', 'Return to Home Page')}
        </Link>
      </div>
    </div>
  );
}

