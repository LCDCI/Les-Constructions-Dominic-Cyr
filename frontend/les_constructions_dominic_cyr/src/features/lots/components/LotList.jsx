import React from 'react';
import './LotList.css';

function formatPrice(p, isOwner) {
  if (!isOwner) return '—'; 
  if (!p) return '—';
  const n = typeof p === 'number' ? p : Number(p);
  return !Number.isNaN(n) ? new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(n) : String(p);
}

export default function LotList({ lots = [], isOwner = false }) {
  if (!lots || lots.length === 0) {
    return <div className="no-results"><p>No available lots found.</p></div>;
  }

  return (
    <div className="table-container">
      <table className="fixed-table">
        <thead>
          <tr>
            {/* Hide Column Header for ID */}
            {isOwner && <th style={{ width: '18%' }}>Identifier</th>}
            
            <th style={{ width: isOwner ? '10%' : '15%' }}>Lot #</th>
            <th style={{ width: isOwner ? '22%' : '35%' }}>Civic Address</th>
            <th style={{ width: '12%' }}>Area (sqft)</th>
            <th style={{ width: '12%' }}>Area (m²)</th>
            <th style={{ width: '15%' }}>Price</th>
            <th style={{ width: '11%' }}>Status</th>
          </tr>
        </thead>
        <tbody>
          {lots.map((l) => (
            <tr key={l.lotId}>
              {/* Hide ID Cell */}
              {isOwner && <td className="id-cell" title={l.lotId}>{l.lotId}</td>}
              
              <td className="bold-cell">{l.lotNumber || '—'}</td>
              <td className="address-cell">{l.civicAddress || '—'}</td>
              <td>{l.dimensionsSquareFeet || '—'}</td>
              <td>{l.dimensionsSquareMeters || '—'}</td>
              <td className="price-cell">{formatPrice(l.price, isOwner)}</td>
              <td>
                <span className={`status-pill ${String(l.lotStatus || '').toLowerCase()}`}>
                  {/* Visitor only ever sees "AVAILABLE" */}
                  {isOwner ? (l.lotStatus || 'UNKNOWN') : 'AVAILABLE'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}