import React from 'react';
import { FiEdit2, FiTrash2 } from 'react-icons/fi';
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

// eslint-disable-next-line react/prop-types
export default function LotList({ lots = [], isOwner = false, onEdit, onDelete }) {
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
            {/* Replace ID column with Assigned Customer for owners */}
            {isOwner && <th style={{ width: '20%' }}>Assigned Customer</th>}

            <th style={{ width: isOwner ? '12%' : '15%' }}>Lot #</th>
            <th style={{ width: isOwner ? '22%' : '35%' }}>Civic Address</th>
            <th style={{ width: '12%' }}>Area (sqft)</th>
            <th style={{ width: '12%' }}>Area (m²)</th>
            <th style={{ width: '15%' }}>Price</th>
            <th style={{ width: '11%' }}>Status</th>
            {isOwner && <th style={{ width: '8%' }}>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {lots.map(l => (
            <tr key={l.lotId}>
              {/* Show assigned customer instead of ID for owners */}
              {isOwner && (
                <td className="customer-cell">
                  {l.assignedCustomerName ? (
                    <div className="customer-info">
                      <div className="customer-name">{l.assignedCustomerName}</div>
                    </div>
                  ) : (
                    <span className="no-customer">No customer assigned</span>
                  )}
                </td>
              )}

              <td className="bold-cell">{l.lotNumber || '—'}</td>
              <td className="address-cell">{l.civicAddress || '—'}</td>
              <td>{l.dimensionsSquareFeet || '—'}</td>
              <td>{l.dimensionsSquareMeters || '—'}</td>
              <td className="price-cell">{formatPrice(l.price, isOwner)}</td>
              <td>
                <span
                  className={`status-pill ${String(l.lotStatus || '').toLowerCase()}`}
                >
                  {/* Visitor only ever sees "AVAILABLE" */}
                  {isOwner ? l.lotStatus || 'UNKNOWN' : 'AVAILABLE'}
                </span>
              </td>
              {isOwner && (
                <td className="actions-cell">
                  <div className="action-buttons">
                    <button
                      className="action-btn edit-btn"
                      onClick={() => onEdit?.(l)}
                      title="Edit lot"
                    >
                      <FiEdit2 size={16} />
                    </button>
                    <button
                      className="action-btn delete-btn"
                      onClick={() => onDelete?.(l)}
                      title="Delete lot"
                    >
                      <FiTrash2 size={16} />
                    </button>
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
