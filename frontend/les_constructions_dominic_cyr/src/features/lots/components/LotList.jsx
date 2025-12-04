import React from 'react'
import './LotList.css'

export default function LotList({ lots = [] }) {
  if (!lots || lots.length === 0) return null

  return (
    <table className="lots-table" aria-label="Lots list">
      <thead>
        <tr>
          <th>Location</th>
          <th>Size</th>
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