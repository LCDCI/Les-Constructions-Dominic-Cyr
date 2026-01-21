// loadTheme optionally accepts either a string access token or a function
// that returns a token (e.g. Auth0's getAccessTokenSilently).
export async function loadTheme(getToken) {
  // Determine API base with safe defaults. Prefer environment var, but
  // avoid using a localhost target when the site is not running on localhost
  // (this prevents deployed builds from calling http://localhost:...).
  const envBase = import.meta.env.VITE_API_BASE || import.meta.env.VITE_API_BASE_URL || null;
  const inferredLocal = typeof window !== 'undefined' && window.location && window.location.hostname === 'localhost'
    ? 'http://localhost:8080/api/v1'
    : '/api/v1';

  let RAW_BASE = envBase || inferredLocal;
  function isLocalhostString(s) {
    return typeof s === 'string' && /localhost|127\.0\.0\.1/.test(s);
  }
  // If env explicitly points to localhost but current page is not localhost,
  // ignore the env value to avoid connection attempts to the developer machine.
  if (envBase && isLocalhostString(envBase) && !(typeof window !== 'undefined' && /localhost|127\.0\.0\.1/.test(window.location.hostname))) {
    RAW_BASE = inferredLocal;
  }

  function normalizeBase(raw) {
    if (!raw) return '/api/v1';
    let s = String(raw).trim();
    if (s.startsWith('http://') || s.startsWith('https://')) {
      return s.endsWith('/') && s !== '/' ? s.slice(0, -1) : s;
    }
    if (!s.startsWith('/')) s = '/' + s;
    if (s !== '/' && s.endsWith('/')) s = s.slice(0, -1);
    return s;
  }

  const API_BASE = normalizeBase(RAW_BASE);

  const headers = { 'Content-Type': 'application/json' };

  // Resolve token precedence:
  // 1) If caller provided a token string, use it.
  // 2) If caller provided a function (getToken), call it to obtain a token.
  // 3) Fallback to localStorage 'authToken' if present.
  try {
    if (typeof getToken === 'string' && getToken) {
      headers.Authorization = `Bearer ${getToken}`;
    } else if (typeof getToken === 'function') {
      try {
        const t = await getToken();
        if (t) headers.Authorization = `Bearer ${t}`;
      } catch (e) {
        // ignore token provider failures
      }
    } else {
      const token = typeof window !== 'undefined' ? localStorage.getItem('authToken') : null;
      if (token) headers.Authorization = `Bearer ${token}`;
    }
  } catch (e) {
    // ignore localStorage access errors
  }

  // Perform the fetch with robust error handling. If the fetch fails (connection
  // refused, CORS, network offline, etc.) use a small default theme so the app
  // can continue to render instead of crashing the service worker/renderer.
  let res;
  try {
    res = await fetch(`${API_BASE}/theme`, { headers });
  } catch (e) {
    console.warn('Theme fetch failed (network error)', e);
    applyTheme(getDefaultTheme());
    return;
  }

  if (!res || !res.ok) {
    const statusInfo = res ? `${res.status} ${res.statusText}` : 'no response';
    console.warn(`Theme load failed: ${statusInfo}`);
    applyTheme(getDefaultTheme());
    return;
  }

  let theme;
  try {
    theme = await res.json();
  } catch (e) {
    console.warn('Failed to parse theme response as JSON', e);
    applyTheme(getDefaultTheme());
    return;
  }

  applyTheme(theme);

  function applyTheme(t) {
    if (!t || typeof document === 'undefined') return;
    const root = document.documentElement;
    const safe = v => (v === null || v === undefined ? '' : String(v));

    root.style.setProperty('--primary-color', safe(t.primaryColor || '#1659ff'));
    root.style.setProperty('--secondary-color', safe(t.secondaryColor || '#f5f7fa'));
    root.style.setProperty('--accent-color', safe(t.accentColor || '#ff4081'));
    root.style.setProperty('--card-background', safe(t.cardBackground || '#ffffff'));
    root.style.setProperty('--background-color', safe(t.backgroundColor || '#f4f6f8'));
    root.style.setProperty('--text-primary', safe(t.textPrimary || '#111827'));
    root.style.setProperty('--white', safe(t.white || '#ffffff'));

    root.style.setProperty('--border-radius', safe(t.borderRadius || '6px'));
    root.style.setProperty('--box-shadow', safe(t.boxShadow || '0 1px 3px rgba(0,0,0,0.1)'));
    root.style.setProperty('--transition', safe(t.transition || 'all 0.2s ease'));
  }

  function getDefaultTheme() {
    return {
      primaryColor: '#1659ff',
      secondaryColor: '#f5f7fa',
      accentColor: '#ff4081',
      cardBackground: '#ffffff',
      backgroundColor: '#f4f6f8',
      textPrimary: '#111827',
      white: '#ffffff',
      borderRadius: '6px',
      boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
      transition: 'all 0.2s ease'
    };
  }
}
