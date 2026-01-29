/* eslint-disable no-console */
import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import {
  fetchLots,
  resolveProjectIdentifier,
} from '../../features/lots/api/lots';
import { projectApi } from '../../features/projects/api/projectApi';
import LotList from '../../features/lots/components/LotList';
import Footer from '../../components/Footers/ProjectsFooter';
import '../../styles/lots.css';

const LotsPage = () => {
  const { projectIdentifier: urlProjectIdentifier } = useParams();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
  } = useAuth0();

  const [lots, setLots] = useState([]);
  const [filteredLots, setFilteredLots] = useState([]);
  const [projectName, setProjectName] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sortConfig, setSortConfig] = useState({
    key: 'none',
    direction: 'asc',
  });

  useEffect(() => {
    let cancelled = false;

    const resolveAndFetch = async () => {
      setLoading(true);
      try {
        const resolved = urlProjectIdentifier || resolveProjectIdentifier();
        if (!resolved) return;

        const token = isAuthenticated
          ? await getAccessTokenSilently().catch(() => null)
          : null;

        try {
          const projectData = await projectApi.getProjectById(resolved, token);
          if (!cancelled) setProjectName(projectData.projectName);
        } catch (e) {
          //
        }

        const data = await fetchLots({ projectIdentifier: resolved, token });

        if (!cancelled) {
          // Filter to only show AVAILABLE lots for public view
          const availableLots = (data || []).filter(
            lot => lot.lotStatus?.toUpperCase() === 'AVAILABLE'
          );
          setLots(availableLots);
          setFilteredLots(availableLots);
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to fetch');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (!authLoading) resolveAndFetch();
    return () => {
      cancelled = true;
    };
  }, [
    urlProjectIdentifier,
    isAuthenticated,
    authLoading,
    getAccessTokenSilently,
  ]);

  // Filtering and Sorting Logic for public view
  useEffect(() => {
    let result = [...lots];

    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        lot =>
          lot.civicAddress?.toLowerCase().includes(term) ||
          lot.lotNumber?.toLowerCase().includes(term)
      );
    }
    if (sortConfig.key !== 'none') {
      result.sort((a, b) => {
        const valA = a[sortConfig.key] || 0;
        const valB = b[sortConfig.key] || 0;
        return sortConfig.direction === 'asc'
          ? valA > valB
            ? 1
            : -1
          : valA < valB
            ? 1
            : -1;
      });
    }
    setFilteredLots(result);
  }, [searchTerm, sortConfig, lots]);

  if (loading)
    return (
      <div className="lots-page">
        <div className="lots-content">Loading project data...</div>
      </div>
    );

  return (
    <div className="lots-page">
      <div className="lots-content">
        <div className="lots-header-section">
          <h1>{projectName ? `${projectName}'s Lots` : 'Project Lots'}</h1>
        </div>

        <div className="toolbar-section">
          <div className="search-box">
            <input
              type="text"
              className="search-input"
              placeholder="Search by address or lot..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <select
              className="filter-select"
              onChange={e => {
                const [key, dir] = e.target.value.split('-');
                setSortConfig({ key, direction: dir });
              }}
            >
              <option value="none-asc">Sort By</option>
              <option value="dimensionsSquareFeet-asc">Size: Smallest</option>
              <option value="dimensionsSquareFeet-desc">Size: Largest</option>
            </select>
          </div>
        </div>

        <div className="list-section">
          {error ? (
            <div className="no-results">{error}</div>
          ) : (
            <LotList lots={filteredLots} isOwner={false} />
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default LotsPage;
