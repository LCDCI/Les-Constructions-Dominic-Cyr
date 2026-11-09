// src/main.jsx

import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import {
  getAuthAudience,
  AUTH0_CLIENT_ID,
  AUTH0_DOMAIN,
} from './utils/authConfig';
import App from './App';
import ErrorBoundary from './components/ErrorBoundary';
import './styles/global.css';
import './utils/i18n'; // Initialize i18n
import './utils/setupImageFallback';

const AUTH_TAB_MARKER = 'lcdci-auth-tab-active';

const clearStaleAuthCache = () => {
  if (window.name === AUTH_TAB_MARKER) return;

  const prefixes = ['auth0', '@@auth0spajs@@'];
  const keysToRemove = [];
  for (let index = 0; index < localStorage.length; index += 1) {
    const key = localStorage.key(index);
    if (key && prefixes.some(prefix => key.startsWith(prefix))) {
      keysToRemove.push(key);
    }
  }
  keysToRemove.forEach(key => localStorage.removeItem(key));
  window.name = AUTH_TAB_MARKER;
};

clearStaleAuthCache();

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
