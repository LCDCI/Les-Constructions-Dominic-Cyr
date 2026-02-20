import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../hooks/useBackendUser';

const ROLE_DASHBOARD = {
  OWNER: '/owner/dashboard',
  SALESPERSON: '/salesperson/dashboard',
  CONTRACTOR: '/contractor/dashboard',
  CUSTOMER: '/customer/dashboard',
};

/**
 * Renders the given fallback element (e.g. Home) for unauthenticated visitors.
 * Authenticated users are redirected to their role-specific dashboard.
 */
export default function RoleBasedRedirect({ fallback }) {
  const { isAuthenticated, isLoading: auth0Loading } = useAuth0();
  const { role, loading: roleLoading } = useBackendUser();

  if (auth0Loading || (isAuthenticated && roleLoading)) {
    return null; // or a spinner if preferred
  }

  if (isAuthenticated && role && ROLE_DASHBOARD[role]) {
    return <Navigate to={ROLE_DASHBOARD[role]} replace />;
  }

  return fallback;
}
