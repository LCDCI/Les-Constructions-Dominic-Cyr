import React from 'react';
import './LotList.css';

function formatPrice(p) {
  if (p === null || p === undefined || p === '') return '—';
  // Try to coerce to a number, then format as currency (use CAD by default)
  const n = typeof p === 'number' ? p : Number(p);
  if (!Number.isNaN(n)) {
    try {
      return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency: 'CAD',
      }).format(n);
    } catch (e) {
      return n.toString();
    }
  }
  return String(p);
}

// eslint-disable-next-line react/prop-types
export default function LotList({ lots = [] }) {
  if (!lots || lots.length === 0) return null;

  return (
    <div className="lots-list" aria-label="Available lots">
      {lots.map(l => {
        const imgSrc = l.image
          ? `/images/lots/${l.image}`
          : l.lotId
            ? `/images/lots/lot-${l.lotId}.jpg`
            : '/images/lots/default.jpg';

        return (
          <div
            className="lot-card"
            key={l.lotId || `${l.location}-${l.dimensions}`}
          >
            <div className="thumb">
              <img
                src={imgSrc}
                alt={l.location ? `Photo of ${l.location}` : 'Lot photo'}
                loading="lazy"
              />
            </div>
            <div className="lot-info">
              <h3 className="location" title={l.location}>
                {l.location || '—'}
              </h3>
              <div className="meta">
                <div className="meta-item">
                  <strong>Size:</strong> <span>{l.dimensions || '—'}</span>
                </div>
                <div className="meta-item">
                  <span className="price">{formatPrice(l.price)}</span>
                </div>
                <div className="meta-item">
                  <span
                    className={`status ${String(l.lotStatus || '').toLowerCase()}`}
                    aria-label={l.lotStatus || 'UNKNOWN'}
                  />
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
