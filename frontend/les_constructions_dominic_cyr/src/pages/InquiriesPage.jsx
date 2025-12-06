import React, { useEffect, useState } from 'react';
import '../styles/inquiries.css';

export default function InquiriesPage() {
  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const fetchInquiries = async () => {
      try {
        const res = await fetch('/api/inquiries');
        if (!res.ok) {
          throw new Error('Failed to load inquiries');
        }
        const data = await res.json();
        if (!cancelled) {
          setInquiries(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!cancelled) {
          setError('Unable to load inquiries right now.');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchInquiries();

    return () => {
      cancelled = true;
    };
  }, []);

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleString();
    } catch (e) {
      return isoString;
    }
  };

  return (
    <div className="inquiries-page">
      <header className="inquiries-header">
        <div>
          <p className="eyebrow">Inbox</p>
          <h1>Customer inquiries</h1>
          <p className="subhead">Most recent first. Data is read-only for visitors.</p>
        </div>
      </header>

      {loading && <div className="inquiries-status">Loading inquiries…</div>}
      {error && <div className="inquiries-status error">{error}</div>}

      {!loading && !error && (
        <div className="inquiries-card">
          {inquiries.length === 0 ? (
            <div className="inquiries-empty">No inquiries yet.</div>
          ) : (
            <div className="inquiries-table" role="table" aria-label="Customer inquiries">
              <div className="table-head" role="row">
                <div role="columnheader">Received</div>
                <div role="columnheader">Name</div>
                <div role="columnheader">Email</div>
                <div role="columnheader">Phone</div>
                <div role="columnheader">Message</div>
              </div>
              <div className="table-body">
                {inquiries.map((item) => (
                  <div className="table-row" role="row" key={item.id ?? item.createdAt}>
                    <div role="cell" data-label="Received">{formatDate(item.createdAt)}</div>
                    <div role="cell" data-label="Name">{item.name || '—'}</div>
                    <div role="cell" data-label="Email">{item.email || '—'}</div>
                    <div role="cell" data-label="Phone">{item.phone || '—'}</div>
                    <div role="cell" data-label="Message" className="message-cell">{item.message || '—'}</div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
