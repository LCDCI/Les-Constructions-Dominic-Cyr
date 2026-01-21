export function getFilesServiceBase() {
  // Prefer explicit env var when provided
  const env = import.meta.env.VITE_FILES_SERVICE_URL;

  // If env provided, return it (don't append /files here)
  if (env && typeof env === 'string' && env.trim()) return env.replace(/\/$/, '');

  // If running locally in dev, use developer files service
  if (typeof window !== 'undefined' && window.location.hostname === 'localhost') {
    return 'http://localhost:8082';
  }

  // For deployed sites, avoid pointing to developer localhost (blocked by browser PNA/CORS).
  // Use relative `/files` so requests go to the same origin (backend should proxy or serve files),
  // or configure `VITE_FILES_SERVICE_URL` to a publicly reachable file service.
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
