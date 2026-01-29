/* eslint-disable react/prop-types */
import React from 'react';
import './LotList.css';

function formatPrice(p, isOwner) {
  if (!isOwner) return '—';
  if (!p) return '—';
  const n = typeof p === 'number' ? p : Number(p);
  return !Number.isNaN(n)
    ? new Intl.NumberFormat('en-CA', {
        style: 'currency',
        currency: 'CAD',
      }).format(n)
    : String(p);
}

export default function LotList({ lots = [], isOwner = false }) {
  if (!lots || lots.length === 0) {
    return (
      <div className="no-results">
        <p>No available lots found.</p>
      </div>
    );
  }

  return (
    <div className="table-container">
      <table className="fixed-table">
        <thead>
          <tr>
            <th style={{ width: '15%' }}>Lot #</th>
            <th style={{ width: '35%' }}>Civic Address</th>
            <th style={{ width: '15%' }}>Area (sqft)</th>
            <th style={{ width: '15%' }}>Area (m²)</th>
            <th style={{ width: '10%' }}>Price</th>
            <th style={{ width: '10%' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {lots.map(l => (
            <tr key={l.lotId}>
              <td className="bold-cell">{l.lotNumber || '—'}</td>
              <td className="address-cell">{l.civicAddress || '—'}</td>
              <td>{l.dimensionsSquareFeet || '—'}</td>
              <td>{l.dimensionsSquareMeters || '—'}</td>
              <td className="price-cell">{formatPrice(l.price, isOwner)}</td>
              <td>
                <span className="status-pill available">AVAILABLE</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
