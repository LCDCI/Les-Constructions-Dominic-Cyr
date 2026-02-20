import React from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useLocation, Navigate } from 'react-router-dom';
import '../styles/portal.css';
import { usePageTranslations } from '../hooks/usePageTranslations';
import useBackendUser from '../hooks/useBackendUser';

const ROLE_DASHBOARD = {
  OWNER: '/owner/dashboard',
  SALESPERSON: '/salesperson/dashboard',
  CONTRACTOR: '/contractor/dashboard',
  CUSTOMER: '/customer/dashboard',
};

export default function PortalLogin() {
  const { t } = usePageTranslations('portalLogin');
  const { loginWithRedirect, isAuthenticated, isLoading, error } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();
  const location = useLocation();

  if (isLoading || (isAuthenticated && roleLoading)) {
    return <div className="portal-page">{t('loading', 'Loading...')}</div>;
  }

  if (isAuthenticated) {
    const destination = ROLE_DASHBOARD[role] || '/';
    return <Navigate to={destination} replace />;
  }

  return (
    <div className="portal-page">
      <div className="portal-card">
        {error?.message === 'verification_pending' && (
          <div className="verification-error-banner">
            <p>
              <strong>
                {t('verificationRequired', 'Verification Required:')}
              </strong>{' '}
              {t(
                'verificationMessage',
                'Please check your email and click the verification link before logging in.'
              )}
            </p>
          </div>
        )}

        <h1>{t('title', 'Access Portal')}</h1>
        <p>{t('subtitle', 'Sign in to access your dashboard')}</p>
        <button
          onClick={() =>
            loginWithRedirect({
              authorizationParams: {
                // Ensure this is inside authorizationParams
                prompt: 'login',
              },
              appState: {
                returnTo: (location.state && location.state.returnTo) || '/portal/login',
              },
            })
          }
        >
          {t('continueLogin', 'Continue to Login →')}
        </button>
      </div>
    </div>
  );
}
