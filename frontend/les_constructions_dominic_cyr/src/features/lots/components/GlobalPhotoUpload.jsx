import React, { useState } from 'react';

// eslint-disable-next-line react/prop-types
export default function GlobalPhotoUpload({ onUploaded }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const filesServiceBase =
    import.meta.env.VITE_FILES_SERVICE_URL ?? 'http://localhost:8082';

  async function uploadPhoto() {
    if (!file) {
      alert('Choose a file first');
      return;
    }
    setUploading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', file);

      // Use relative API path so the browser calls your backend proxy (same-origin)
      const resp = await fetch('/api/photos/upload', {
        method: 'POST',
        headers: {
          'X-User': 'demo-user-123',
        },
        body: formData,
      });

      if (!resp.ok) {
        const text = await resp.text();
        throw new Error(`Upload failed: ${resp.status} ${text}`);
      }

      // Expect backend to return JSON (or stringified JSON) with a url path like "/files/uuid"
      const json = await resp.json();
      // If backend returned a raw string, adapt accordingly.
      // Try to find a url field or use the response body as-is:
      const filePath = json?.url ?? (typeof json === 'string' ? json : null);
      if (!filePath) {
        throw new Error('No file path returned from server');
      }

      const fullUrl = `${filesServiceBase}${filePath}`;
      setFile(null);
      if (onUploaded) onUploaded(fullUrl);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  }

  return (
    <div>
      <h3>Upload Global Photo</h3>
      <input
        type="file"
        accept="image/*"
        onChange={e => setFile(e.target.files?.[0] ?? null)}
      />
      <button onClick={uploadPhoto} disabled={uploading}>
        {uploading ? 'Uploading…' : 'Upload'}
      </button>
      {error && <div className="error">{error}</div>}
    </div>
  );
}
