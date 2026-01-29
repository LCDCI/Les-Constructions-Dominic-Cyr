/* eslint-disable react/prop-types */
import React from 'react';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation('lots');

  const getStatusLabel = status => {
    const normalized = String(status || '').toLowerCase();
    const knownStatuses = ['available', 'reserved', 'sold'];

    if (!normalized) {
      return t('status.unknown', { defaultValue: 'Unknown' });
    }

    if (!knownStatuses.includes(normalized)) {
      return status || t('status.unknown', { defaultValue: 'Unknown' });
    }

    return t(`status.${normalized}`, { defaultValue: status || normalized });
  };

  if (!lots || lots.length === 0) {
    return (
      <div className="no-results">
        <p>{t('common.noResults', { defaultValue: 'No lots found.' })}</p>
      </div>
    );
  }

  return (
    <div className="table-container">
      <table className="fixed-table">
        <thead>
          <tr>
            {/* Hide Column Header for ID */}
            {isOwner && (
              <th style={{ width: '18%' }}>
                {t('table.identifier', { defaultValue: 'Identifier' })}
              </th>
            )}

            <th style={{ width: isOwner ? '10%' : '15%' }}>
              {t('table.lotNumber', { defaultValue: 'Lot #' })}
            </th>
            <th style={{ width: isOwner ? '22%' : '35%' }}>
              {t('table.address', { defaultValue: 'Civic Address' })}
            </th>
            <th style={{ width: '12%' }}>
              {t('table.areaSqft', { defaultValue: 'Area (sqft)' })}
            </th>
            <th style={{ width: '12%' }}>
              {t('table.areaSqm', { defaultValue: 'Area (m²)' })}
            </th>
            <th style={{ width: '15%' }}>
              {t('table.price', { defaultValue: 'Price' })}
            </th>
            <th style={{ width: '11%' }}>
              {t('table.status', { defaultValue: 'Status' })}
            </th>
          </tr>
        </thead>
        <tbody>
          {lots.map(l => (
            <tr key={l.lotId}>
              {/* Hide ID Cell */}
              {isOwner && (
                <td className="id-cell" title={l.lotId}>
                  {l.lotId}
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
                  {isOwner
                    ? getStatusLabel(l.lotStatus)
                    : t('status.available', { defaultValue: 'Available' })}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
