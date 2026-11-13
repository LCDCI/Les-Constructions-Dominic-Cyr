export function getFilesServiceBase() {
  const browserHost =
    typeof window !== 'undefined' ? window.location.hostname : '';
  const useFrontendProxy =
    typeof window !== 'undefined' &&
    (browserHost === 'localhost' ||
      browserHost.endsWith('.constructions-dominiccyr.com'));

  if (useFrontendProxy) {
    return window.location.origin;
  }

  // Prefer explicit env var when provided
  const env = import.meta.env.VITE_FILES_SERVICE_URL;

  // If env provided, return it (don't append /files here)
  if (env && typeof env === 'string' && env.trim())
    return env.replace(/\/$/, '');

  // For production on DigitalOcean, use files-service directly
  if (browserHost.includes('constructions-dominiccyr')) {
    return 'https://files-service-app-xubs2.ondigitalocean.app';
  }

  // Keep local image URLs on the same service used by the upload API.
  return 'http://localhost:8082';
}

export function getFileUrl(fileId) {
  if (!fileId) return null;
  const base = getFilesServiceBase();
  const browserHost =
    typeof window !== 'undefined' ? window.location.hostname : '';
  const useFrontendProxy =
    typeof window !== 'undefined' &&
    (browserHost === 'localhost' ||
      browserHost.endsWith('.constructions-dominiccyr.com'));

  // Rewrite stored files-service URLs through the frontend proxy so public
  // media receives the internal service key without exposing it in the browser.
  try {
    const parsedUrl = new URL(fileId);
    if (
      useFrontendProxy &&
      parsedUrl.hostname === 'files-service-app-xubs2.ondigitalocean.app'
    ) {
      return `${window.location.origin}${parsedUrl.pathname}${parsedUrl.search}`;
    }
    return parsedUrl.toString();
  } catch (e) {
    // not an absolute URL
  }
  const normalizedBase = base.replace(/\/$/, '');
  const filesBase = normalizedBase.endsWith('/files')
    ? normalizedBase
    : `${normalizedBase}/files`;
  return `${filesBase}/${fileId}`;
}
