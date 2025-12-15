import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../hooks/useBackendUser';

export default function ProtectedRoute({ allowedRoles, element }) {
  const location = useLocation();
  const { isAuthenticated, isLoading } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();

  if (isLoading || roleLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/portal/login" state={{ returnTo: location.pathname }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && role && !allowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return element;
}
