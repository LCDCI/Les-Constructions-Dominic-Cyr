import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../hooks/useBackendUser';

export default function ProtectedRoute({ allowedRoles, element }) {
  const location = useLocation();
  const { isAuthenticated, isLoading, user } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();
  const auth0Roles = user?.['https://construction-api.loca/roles'] || [];

  if (isLoading || roleLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/portal/login"
        state={{ returnTo: location.pathname }}
        replace
      />
    );
  }

  // If allowedRoles is specified, we need a valid role to proceed
  if (allowedRoles && allowedRoles.length > 0) {
    // Prefer backend role when available
    if (role) {
      if (!allowedRoles.includes(role)) return <Navigate to="/unauthorized" replace />;
      return element;
    }

    // Fallback: check Auth0 role claim (useful if backend profile hasn't loaded yet)
    if (auth0Roles && auth0Roles.length > 0) {
      const has = auth0Roles.some(r => allowedRoles.includes(r));
      if (!has) return <Navigate to="/unauthorized" replace />;
      return element;
    }

    // No role information at all -> unauthorized
    return <Navigate to="/unauthorized" replace />;
  }

  return element;
}
