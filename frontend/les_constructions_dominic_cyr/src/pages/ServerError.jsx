import React from 'react';
import { Link } from 'react-router-dom';
import './ErrorPage.css';

export default function ServerError() {
  return (
    <div className="error-page">
      <div className="error-page-content">
        <div className="error-icon">500</div>
        <h1>Server Error</h1>
        <p className="error-message">
          We&apos;re sorry, but something went wrong on our end.
        </p>
        <p className="error-description">
          Our team has been notified and is working to fix the issue. Please try again later or return to the home page.
        </p>
        <Link to="/" className="error-button">
          Return to Home Page
        </Link>
      </div>
    </div>
  );
}

