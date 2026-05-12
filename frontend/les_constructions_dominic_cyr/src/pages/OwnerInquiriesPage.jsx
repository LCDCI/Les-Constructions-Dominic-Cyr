import React, { useEffect, useMemo, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { usePageTranslations } from '../hooks/usePageTranslations';

const fetchInquiries = async getAccessTokenSilently => {
  const { getAuthAudience } = await import('../utils/authConfig');
  const token = await getAccessTokenSilently({
    authorizationParams: {
      audience: getAuthAudience(),
    },
  });
  const res = await fetch('/api/v1/inquiries', {
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
  const { t } = usePageTranslations('ownerInquiriesPage');
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
    // Decode numeric and common named HTML entities without using innerHTML
    return String(str).replace(
      /&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);/g,
      (match, code) => {
        if (!code) return match;
        if (code[0] === '#') {
          // Numeric entity
          try {
            if (code[1] === 'x' || code[1] === 'X') {
              const num = parseInt(code.slice(2), 16);
              return isNaN(num) ? match : String.fromCharCode(num);
            }
            const num = parseInt(code.slice(1), 10);
            return isNaN(num) ? match : String.fromCharCode(num);
          } catch (_) {
            return match;
          }
        }

        // Common named entities
        switch (code) {
          case 'amp':
            return '&';
          case 'lt':
            return '<';
          case 'gt':
            return '>';
          case 'quot':
            return '"';
          case 'apos':
            return "'";
          case 'nbsp':
            return '\u00A0';
          default:
            return match;
        }
      }
    );
  };

  const decodedInquiries = useMemo(
    () =>
      inquiries.map(inq => ({
        ...inq,
        name: unescapeHtml(inq.name),
        email: unescapeHtml(inq.email),
        phone: unescapeHtml(inq.phone),
        message: unescapeHtml(inq.message),
      })),
    [inquiries]
  );

  return (
    <>
      <div style={{ padding: '2rem', minHeight: 'calc(100vh - 200px)' }}>
        <h1>{t('title', 'Inquiry Review')}</h1>
        {loading && <p>{t('loading', 'Loading inquiries...')}</p>}
        {error && (
          <p style={{ color: 'red' }}>
            {t('error', 'Error:')} {error.message}
          </p>
        )}
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
                  {t('table.name', 'Name')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.email', 'Email')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.phone', 'Phone')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.message', 'Message')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.timestamp', 'Timestamp')}
                </th>
              </tr>
            </thead>
            <tbody>
              {decodedInquiries.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    style={{ textAlign: 'center', padding: '2rem' }}
                  >
                    {t('noInquiries', 'No inquiries found.')}
                  </td>
                </tr>
              ) : (
                decodedInquiries.map(inq => (
                  <tr key={inq.id}>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.name}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      <a
                        href={`mailto:${encodeURIComponent(inq.email || '')}`}
                        style={{
                          color: '#0066cc',
                          textDecoration: 'underline',
                          cursor: 'pointer',
                        }}
                      >
                        {inq.email}
                      </a>
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.phone ? inq.phone : '-'}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.message}
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
    </>
  );
}
