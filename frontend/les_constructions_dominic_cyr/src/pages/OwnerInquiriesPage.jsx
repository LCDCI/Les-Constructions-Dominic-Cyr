import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import HomeFooter from '../components/Footers/HomeFooter';

const fetchInquiries = async getAccessTokenSilently => {
  const token = await getAccessTokenSilently();
  const res = await fetch('/api/inquiries', {
    method: 'GET',
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error('Failed to fetch inquiries');
  return res.json();
};

export default function OwnerInquiriesPage() {
  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [redirecting, setRedirecting] = useState(false);
  const {
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    loginWithRedirect,
  } = useAuth0();

  useEffect(() => {
    if (isLoading) return;
    if (!isAuthenticated && !redirecting) {
      setRedirecting(true);
      setLoading(false);
      loginWithRedirect({ appState: { returnTo: '/inquiries' } });
      return;
    }

    fetchInquiries(getAccessTokenSilently)
      .then(setInquiries)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    loginWithRedirect,
    redirecting,
  ]);

  const formatDate = value => {
    const d = value ? new Date(value) : null;
    return d && !Number.isNaN(d.getTime()) ? d.toLocaleString() : 'N/A';
  };

  const unescapeHtml = str => {
    if (!str) return str;
    const parser = new DOMParser();
    const doc = parser.parseFromString(str, 'text/html');
    return doc.documentElement.textContent || str;
  };

  return (
    <>
      <div style={{ padding: '2rem', minHeight: 'calc(100vh - 200px)' }}>
        <h1>Inquiry Review</h1>
        {loading && <p>Loading inquiries...</p>}
        {error && <p style={{ color: 'red' }}>Error: {error.message}</p>}
        {!loading && !error && (
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              marginTop: '2rem',
            }}
          >
            <thead>
              <tr style={{ background: '#f0f0f0' }}>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  Name
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  Email
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  Phone
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  Message
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  Timestamp
                </th>
              </tr>
            </thead>
            <tbody>
              {inquiries.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    style={{ textAlign: 'center', padding: '2rem' }}
                  >
                    No inquiries found.
                  </td>
                </tr>
              ) : (
                inquiries.map(inq => (
                  <tr key={inq.id}>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {unescapeHtml(inq.name)}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      <a
                        href={`mailto:${unescapeHtml(inq.email)}`}
                        style={{
                          color: '#0066cc',
                          textDecoration: 'underline',
                          cursor: 'pointer',
                        }}
                      >
                        {unescapeHtml(inq.email)}
                      </a>
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.phone ? unescapeHtml(inq.phone) : '-'}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {unescapeHtml(inq.message)}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {formatDate(inq.createdAt)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
      <HomeFooter />
    </>
  );
}
