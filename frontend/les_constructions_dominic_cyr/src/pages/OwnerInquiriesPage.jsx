import React, { useEffect, useState } from 'react';

// TODO: Replace with secure retrieval of owner key (e.g., from env, context, or user input)
const OWNER_KEY = 'dev-owner-key';

const fetchInquiries = async () => {
  const res = await fetch('/api/inquiries', {
    headers: {
      'X-OWNER-KEY': OWNER_KEY,
    },
  });
  if (!res.ok) throw new Error('Failed to fetch inquiries');
  return res.json();
};

export default function OwnerInquiriesPage() {
  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchInquiries()
      .then(setInquiries)
      .catch(setError)
      .finally(() => setLoading(false));
  }, []);

  const formatDate = value => {
    const d = value ? new Date(value) : null;
    return d && !Number.isNaN(d.getTime()) ? d.toLocaleString() : 'N/A';
  };

  const unescapeHtml = str => {
    if (!str) return str;
    const textarea = document.createElement('textarea');
    textarea.innerHTML = str;
    return textarea.value;
  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Owner Inquiry Review</h1>
      <p style={{ color: '#888' }}>
        This page is hidden and only accessible at <b>/inquiries</b> for
        demonstration.
      </p>
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
                    {unescapeHtml(inq.email)}
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
  );
}
