import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useLocation } from 'react-router-dom';
import '../styles/portal.css';

export default function PortalLogin() {
  const { loginWithRedirect, isAuthenticated, isLoading, error } = useAuth0();
  const location = useLocation();

  if (isLoading) {
    return <div className="portal-page">Loading...</div>;
  }

  if (isAuthenticated) {
    return (
      <div className="portal-page">
        <h1>You are already logged in</h1>
      </div>
    );
  }

  return (
    <div className="portal-page">
      <div className="portal-card">
        {error?.message === 'verification_pending' && (
          <div className="verification-error-banner">
            <p>
              <strong>Verification Required:</strong> Please check your email
              and click the verification link before logging in.
            </p>
          </div>
        )}

        <h1>Access Portal</h1>
        <p>Sign in to access your dashboard</p>
        <button
          onClick={() =>
            loginWithRedirect({
              authorizationParams: {
                // Ensure this is inside authorizationParams
                prompt: 'login',
              },
              appState: {
                returnTo: (location.state && location.state.returnTo) || '/',
              },
            })
          }
        >
          Continue to Login →
        </button>
      </div>
    </div>
  );
}
