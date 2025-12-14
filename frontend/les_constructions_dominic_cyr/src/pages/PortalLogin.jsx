import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import '../styles/portal.css';

export default function PortalLogin() {
  const { loginWithRedirect, isAuthenticated, isLoading } = useAuth0();

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
        <h1>Access Portal</h1>
        <p>Sign in to access your dashboard</p>
        <button onClick={() => loginWithRedirect()}>
          Continue to Login →
        </button>
      </div>
    </div>
  );
}
