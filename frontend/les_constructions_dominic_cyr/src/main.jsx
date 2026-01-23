// src/main.jsx

import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App';
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
  <Auth0Provider
    domain={import.meta.env.VITE_AUTH0_DOMAIN}
    clientId={import.meta.env.VITE_AUTH0_CLIENT_ID}
    authorizationParams={{
      redirect_uri: window.location.origin,
      audience: import.meta.env.VITE_AUTH0_AUDIENCE,
    }}
    useRefreshTokens
    cacheLocation="localstorage"
    onRedirectCallback={onRedirectCallback}
  >
    <App />
  </Auth0Provider>
);
