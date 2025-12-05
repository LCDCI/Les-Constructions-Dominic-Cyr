import React from 'react';
import { Link } from 'react-router-dom';
import './ErrorPage.css';

export default function NotFound() {
  return (
    <div className="error-page">
      <div className="error-page-content">
        <div className="error-icon">404</div>
        <h1>Page Not Found</h1>
        <p className="error-message">
          We're sorry, but the page you're looking for doesn't exist or has been moved.
        </p>
        <p className="error-description">
          The resource you requested could not be found. Please check the URL or return to the home page.
        </p>
        <Link to="/" className="error-button">
          Return to Home Page
        </Link>
      </div>
    </div>
  );
}

