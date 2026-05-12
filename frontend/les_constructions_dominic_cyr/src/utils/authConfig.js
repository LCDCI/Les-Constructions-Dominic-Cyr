// Small helper to normalize Auth0 configuration values used across the app
export function getAuthAudience() {
  const raw = import.meta.env.VITE_AUTH0_AUDIENCE || '';
  const normalized = String(raw)
    .trim()
    .replace(/\/+$|\s+/g, '');
  // Remove trailing slashes only (keep empty as undefined)
  const withoutTrailing = normalized.replace(/\/+$/g, '');
  return withoutTrailing || undefined;
}

export const AUTH0_DOMAIN = import.meta.env.VITE_AUTH0_DOMAIN;
export const AUTH0_CLIENT_ID = import.meta.env.VITE_AUTH0_CLIENT_ID;
