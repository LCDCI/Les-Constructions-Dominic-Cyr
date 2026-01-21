import React from 'react';

// eslint-disable-next-line react/prop-types
export default function GlobalPhotoDisplay({ filePath }) {
  // filePath may be full URL or path. If it's a path like /files/uuid, prefix VITE_FILES_SERVICE_URL
  if (!filePath) return null;

  const filesServiceBase =
    import.meta.env.VITE_FILES_SERVICE_URL ?? (typeof window !== 'undefined' && window.location.hostname === 'localhost' ? 'http://localhost:8082' : `${window.location.origin}/files`);
  // eslint-disable-next-line react/prop-types
  const src = filePath.startsWith('http')
    ? filePath
    : `${filesServiceBase}${filePath}`;

  return (
    <img src={src} alt="Global" style={{ maxWidth: '100%', height: 'auto' }} />
  );
}
