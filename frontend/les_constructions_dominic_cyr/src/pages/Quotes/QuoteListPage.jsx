import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import { MdOutlineRequestQuote, MdAdd } from 'react-icons/md';
import { AiOutlineEye, AiOutlineSend } from 'react-icons/ai';
import { BiSearch } from 'react-icons/bi';
import './QuoteListPage.css';

/**
 * QuoteListPage Component
 * Displays a list of quotes (bills) for projects
 * Matches the first screenshot design
 */
const QuoteListPage = () => {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);
  const [quotes, setQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [token, setToken] = useState(null);

  useEffect(() => {
    const initializePage = async () => {
      try {
        setLoading(true);
        setError(null);

        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);

        // Fetch projects
        const projectsResponse = await axios.get('/api/v1/projects', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (projectsResponse.data && Array.isArray(projectsResponse.data)) {
          setProjects(projectsResponse.data);
          if (projectsResponse.data.length > 0) {
            setSelectedProject(projectsResponse.data[0]);
            // Fetch quotes for first project
            await fetchQuotesForProject(projectsResponse.data[0].projectIdentifier, accessToken);
          }
        }
      } catch (err) {
        console.error('Error initializing quotes page:', err);
        setError(
          err.response?.data?.message ||
          err.message ||
          t('quote.errorLoading') ||
          'An error occurred while loading data'
        );
      } finally {
        setLoading(false);
      }
    };

    initializePage();
  }, [getAccessTokenSilently, t]);

  const fetchQuotesForProject = async (projectIdentifier, accessToken) => {
    try {
      const response = await axios.get(`/api/v1/quotes/project/${projectIdentifier}`, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
      setQuotes(response.data || []);
    } catch (err) {
      console.error('Error fetching quotes:', err);
      setQuotes([]);
    }
  };

  const handleProjectChange = async (e) => {
    const projectId = e.target.value;
    const project = projects.find(p => p.projectIdentifier === projectId);
    setSelectedProject(project);
    
    if (project && token) {
      setLoading(true);
      await fetchQuotesForProject(project.projectIdentifier, token);
      setLoading(false);
    }
  };

  const handleCreateNewBill = () => {
    navigate(`/quotes/create`, { 
      state: { 
        projectIdentifier: selectedProject?.projectIdentifier,
        projectName: selectedProject?.projectName 
      } 
    });
  };

  const handleViewQuote = (quoteNumber) => {
    navigate(`/quotes/${quoteNumber}`);
  };

  const filteredQuotes = quotes.filter(quote =>
    quote.quoteNumber.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading && !selectedProject) {
    return (
      <div className="quote-list-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>{t('common.loading') || 'Loading...'}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="quote-list-page">
        <div className="error-container">
          <div className="error-icon">⚠️</div>
          <h2>{t('common.error') || 'Error'}</h2>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={() => window.location.reload()}>
            {t('common.tryAgain') || 'Try Again'}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="quote-list-page">
      <div className="quote-list-container">
        {/* Header with Project Selector */}
        <div className="page-header">
          <div className="header-left">
            <div className="project-selector-wrapper">
              <label htmlFor="project-selector" className="project-selector-label">
                {t('quote.selectProject') || 'Select Project'}
              </label>
              <select
                id="project-selector"
                className="project-selector"
                value={selectedProject?.projectIdentifier || ''}
                onChange={handleProjectChange}
              >
                <option value="">
                  {t('quote.chooseProject') || 'Choose a project...'}
                </option>
                {projects.map((project) => (
                  <option
                    key={project.projectIdentifier}
                    value={project.projectIdentifier}
                  >
                    {project.projectName || project.projectIdentifier}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="header-right">
            <h1>{t('quote.bills') || 'Bills'}</h1>
          </div>
        </div>

        {/* Search and Actions Bar */}
        <div className="actions-bar">
          <div className="search-container">
            <BiSearch className="search-icon" />
            <input
              type="text"
              placeholder={t('quote.searchBills') || 'Search anything on Bills'}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="search-input"
            />
          </div>
          <button 
            className="btn btn-create"
            onClick={handleCreateNewBill}
          >
            {t('quote.createNewBill') || 'Create New Bill'}
          </button>
        </div>

        {/* Quotes Table */}
        {loading ? (
          <div className="loading-container">
            <div className="spinner"></div>
          </div>
        ) : filteredQuotes.length === 0 ? (
          <div className="empty-state">
            <MdOutlineRequestQuote className="empty-icon" />
            <h3>{t('quote.noBills') || 'No bills found'}</h3>
            <p>{t('quote.noBillsDesc') || 'Create your first bill to get started'}</p>
            <button 
              className="btn btn-primary"
              onClick={handleCreateNewBill}
            >
              <MdAdd /> {t('quote.createNewBill') || 'Create New Bill'}
            </button>
          </div>
        ) : (
          <div className="quotes-table-wrapper">
            <table className="quotes-table">
              <thead>
                <tr>
                  <th className="col-bill-number">{t('quote.billNumber') || 'BILL NUMBER'}</th>
                  <th className="col-amount">{t('quote.amount') || 'AMOUNT'}</th>
                  <th className="col-date">{t('quote.date') || 'DATE'}</th>
                  <th className="col-action">{t('quote.action') || 'ACTION'}</th>
                </tr>
              </thead>
              <tbody>
                {filteredQuotes.map((quote) => (
                  <tr key={quote.quoteNumber}>
                    <td className="col-bill-number">
                      <div className="bill-info">
                        <span className="bill-number">{quote.quoteNumber}</span>
                        <span className="bill-status">
                          {quote.status === 'ESTIMATED' 
                            ? (t('quote.estimatedInvoice') || 'Estimated Invoice')
                            : (t('quote.finalInvoice') || 'Final Invoice')}
                        </span>
                      </div>
                    </td>
                    <td className="col-amount">
                      ${quote.totalAmount?.toFixed(2) || '0.00'}
                    </td>
                    <td className="col-date">
                      {new Date(quote.createdAt).toLocaleDateString('en-US', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric'
                      })}
                      <span className="time">
                        {t('quote.at') || 'at'} {new Date(quote.createdAt).toLocaleTimeString('en-US', {
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </span>
                    </td>
                    <td className="col-action">
                      <div className="action-buttons">
                        <button 
                          className="btn btn-view"
                          onClick={() => handleViewQuote(quote.quoteNumber)}
                        >
                          {t('quote.view') || 'View'}
                        </button>
                        {quote.status === 'ESTIMATED' && (
                          <button className="btn btn-send">
                            {t('quote.sendFinalInvoice') || 'Send Final Invoice'}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default QuoteListPage;
