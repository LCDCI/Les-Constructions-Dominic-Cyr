import React, { useEffect, useState } from 'react';
import { fetchLots } from '../../features/lots/api/lots';
import LotList from '../../features/lots/components/LotList';
import Navbar from '../../components/Navbar';
import Footer from '../../components/Footer';
import '../../styles/lots.css';

export default function LotsPage() {
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchLots();
      setLots(Array.isArray(data) ? data : []);
    } catch (e) {
      const msg =
        e && e.response
          ? `Request failed ${e.response.status}: ${JSON.stringify(e.response.data)}`
          : e && e.message
            ? e.message
            : 'Unknown error';
      setError(msg);
      setLots([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="lots-page">
      <Navbar />

      <div className="lots-content">
        <div className="lots-container">
          <h2>Available Lots</h2>

          <div className="lots-actions">
            <button className="refresh-button" onClick={load}>
              Refresh
            </button>
          </div>

          {loading && <div className="info">Loading lots…</div>}

          {error && (
            <div className="error">
              <strong>Unable to load lots. </strong>
              <div className="error-details">{String(error)}</div>
            </div>
          )}

          {!loading && !error && <LotList lots={lots} />}

          {!loading && !error && lots.length === 0 && (
            <div className="info">No lots available at the moment.</div>
          )}
        </div>
      </div>

      <Footer />
    </div>
  );
}
