import React from 'react'
import './LotList.css'

function formatPrice(p) {
  if (p === null || p === undefined || p === '') return '—'
  // If numeric, format as currency (use CAD by default)
  if (typeof p === 'number') {
    try {
      return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'CAD' }).format(p)
    } catch (e) {
      return p.toString()
    }
  }
  // If string that looks like a number, try to parse and format
  const n = Number(p)
  if (!Number.isNaN(n)) {
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'CAD' }).format(n)
  }
  // Otherwise, return as-is
  return String(p)
}

export default function LotList({ lots = [] }) {
  if (!lots || lots.length === 0) return null

  return (
    <table className="lots-table" aria-label="Lots list">
      <thead>
        <tr>
          <th>Location</th>
          <th>Size</th>
          <th>Price</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {lots.map((l) => (
          <tr key={l.lotId || `${l.location}-${l.dimensions}`}>
            <td className="location" title={l.location}>
              {l.location || '—'}
            </td>
            <td>{l.dimensions || '—'}</td>
            <td className="price">{formatPrice(l.price)}</td>
            <td>
              <span className={`status ${String(l.lotStatus || '').toLowerCase()}`}>
                {l.lotStatus || 'UNKNOWN'}
              </span>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}