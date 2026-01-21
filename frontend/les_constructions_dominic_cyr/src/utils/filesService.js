export function getFilesServiceBase() {
  // Prefer explicit env var when provided
  const env = import.meta.env.VITE_FILES_SERVICE_URL;

  // If env provided, return it (don't append /files here)
  if (env && typeof env === 'string' && env.trim()) return env.replace(/\/$/, '');

  // For production on DigitalOcean, use files-service directly
  if (typeof window !== 'undefined' && window.location.hostname.includes('ondigitalocean')) {
    return 'https://files-service-app-xubs2.ondigitalocean.app';
  }

  // For local development, use relative path
  return `${window.location.origin}/files`;
}

export function getFileUrl(fileId) {
  if (!fileId) return null;
  const base = getFilesServiceBase();
  // If fileId already looks like a URL, return as-is
  try {
    const u = new URL(fileId);
    return fileId;
  } catch (e) {
    // not an absolute URL
  }
  return `${base.replace(/\/$/, '')}/files/${fileId}`;
}
