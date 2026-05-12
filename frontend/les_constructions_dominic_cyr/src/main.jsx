// src/main.jsx

import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import { getAuthAudience, AUTH0_CLIENT_ID, AUTH0_DOMAIN } from './utils/authConfig';
import App from './App';
import ErrorBoundary from './components/ErrorBoundary';
import './styles/global.css';
import './utils/i18n'; // Initialize i18n
import './utils/setupImageFallback';

const onRedirectCallback = appState => {
  window.history.replaceState(
    {},
    document.title,
    appState?.returnTo || window.location.pathname
  );
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <ErrorBoundary>
    <Auth0Provider
      domain={AUTH0_DOMAIN}
      clientId={AUTH0_CLIENT_ID}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: getAuthAudience(),
      }}
      useRefreshTokens
      cacheLocation="localstorage"
      onRedirectCallback={onRedirectCallback}
    >
      <App />
    </Auth0Provider>
  </ErrorBoundary>
);
