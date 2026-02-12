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
  const { t } = useTranslation('quotes');
  const { getAccessTokenSilently } = useAuth0();
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [lots, setLots] = useState([]);
  const [selectedLot, setSelectedLot] = useState(null);
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

        // Fetch all projects
        const projectsResponse = await axios.get('/api/v1/projects', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        if (projectsResponse.data && Array.isArray(projectsResponse.data)) {
          setProjects(projectsResponse.data);

          // Fetch lots for each project and flatten them
          const allLots = [];
          for (const project of projectsResponse.data) {
            try {
              const lotsResponse = await axios.get(
                `/api/v1/projects/${project.projectIdentifier}/lots`,
                {
                  headers: {
                    Authorization: `Bearer ${accessToken}`,
                  },
                }
              );
              if (lotsResponse.data && Array.isArray(lotsResponse.data)) {
                const mappedLots = lotsResponse.data.map(lot => {
                  return {
                    ...lot,
                    lotIdentifier: lot.lotId, // Backend returns 'lotId', map to 'lotIdentifier' for consistency
                    projectIdentifier: project.projectIdentifier,
                    projectName: project.projectName,
                  };
                });
                allLots.push(...mappedLots);
              }
            } catch (err) {
              console.error(
                `Error fetching lots for project ${project.projectIdentifier}:`,
                err
              );
            }
          }

          setLots(allLots);
          if (allLots.length > 0) {
            setSelectedLot(allLots[0]);
            // Fetch quotes for first lot
            await fetchQuotesForLot(allLots[0].lotIdentifier, accessToken);
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

  const fetchQuotesForLot = async (lotIdentifier, accessToken) => {
    try {
      const response = await axios.get(`/api/v1/quotes/lot/${lotIdentifier}`, {
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

  const handleLotChange = async e => {
    const selectedValue = e.target.value;

    if (!selectedValue) return;

    const lot = lots.find(l => l.lotIdentifier === selectedValue);

    if (!lot) {
      console.error('Lot not found. Selected value:', selectedValue);
      return;
    }

    setSelectedLot(lot);

    if (token) {
      setLoading(true);
      try {
        await fetchQuotesForLot(lot.lotIdentifier, token);
      } catch (err) {
        console.error('Error fetching quotes for lot:', err);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleCreateNewBill = () => {
    navigate(`/quotes/create`, {
      state: {
        lotIdentifier: selectedLot?.lotIdentifier,
        lotNumber: selectedLot?.lotNumber,
        projectIdentifier: selectedLot?.projectIdentifier,
        projectName: selectedLot?.projectName,
      },
    });
  };

  const handleViewQuote = quoteNumber => {
    navigate(`/quotes/${quoteNumber}`);
  };

  const filteredQuotes = quotes.filter(
    quote =>
      quote.quoteNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      quote.customerName?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusLabel = status => {
    switch (status) {
      case 'PENDING_OWNER_APPROVAL':
        return t('quote.statusPendingOwner') || 'Waiting for Owner Approval';
      case 'OWNER_APPROVED':
        return t('quote.statusOwnerApproved') || 'Owner Approved';
      case 'CUSTOMER_APPROVED':
        return t('quote.statusCustomerApproved') || 'Customer Approved';
      case 'REJECTED':
        return t('quote.statusRejected') || 'Rejected';
      case 'ESTIMATED':
        return t('quote.estimatedInvoice') || 'Estimated Invoice';
      default:
        return t('quote.statusUnknown') || 'Pending';
    }
  };

  const getStatusClass = status => {
    switch (status) {
      case 'PENDING_OWNER_APPROVAL':
        return 'status-pill status-pending-owner';
      case 'OWNER_APPROVED':
        return 'status-pill status-owner-approved';
      case 'CUSTOMER_APPROVED':
        return 'status-pill status-customer-approved';
      case 'REJECTED':
        return 'status-pill status-rejected';
      case 'ESTIMATED':
        return 'status-pill status-estimated';
      default:
        return 'status-pill status-pending-owner';
    }
  };

  if (loading && !selectedLot) {
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
          <button
            className="btn btn-primary"
            onClick={() => window.location.reload()}
          >
            {t('common.tryAgain') || 'Try Again'}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="quote-list-page">
      <div className="quote-list-container">
        {/* Header with Lot Selector */}
        <div className="page-header">
          <div className="header-left">
            <div className="project-selector-wrapper">
              <label
                htmlFor="project-selector"
                className="project-selector-label"
              >
                {t('quote.selectLot') || 'Select Lot'}
              </label>
              <select
                id="project-selector"
                className="project-selector"
                value={selectedLot?.lotIdentifier || ''}
                onChange={handleLotChange}
                disabled={lots.length === 0}
              >
                <option value="">
                  {lots.length === 0
                    ? t('quote.noLotsAvailable') || 'No lots available'
                    : t('quote.chooseLot') || 'Choose a lot...'}
                </option>
                {lots &&
                  lots.length > 0 &&
                  lots.map(lot => (
                    <option
                      key={lot.lotIdentifier || `lot-${lot.lotNumber}`}
                      value={lot.lotIdentifier}
                    >
                      {lot.projectName} - {lot.lotNumber}
                    </option>
                  ))}
              </select>
            </div>
          </div>
          <div className="header-right">
            <h1>{t('quote.bills') || 'Quotes'}</h1>
          </div>
        </div>

        {/* Search and Actions Bar */}
        <div className="actions-bar">
          <div className="search-container">
            <BiSearch className="search-icon" />
            <input
              type="text"
              placeholder={t('quote.searchBills') || 'Search anything on Quotes'}
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="search-input"
            />
          </div>
          <button className="btn btn-create" onClick={handleCreateNewBill}>
            {t('quote.createNewBill') || 'Create New Quote'}
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
            <h3>{t('quote.noBills') || 'No quotes found'}</h3>
            <p>
              {t('quote.noBillsDesc') ||
                'Create your first quote to get started'}
            </p>
            <button className="btn btn-primary" onClick={handleCreateNewBill}>
              <MdAdd /> {t('quote.createNewBill') || 'Create New Quote'}
            </button>
          </div>
        ) : (
          <div className="quotes-table-wrapper">
            <table className="quotes-table">
              <thead>
                <tr>
                  <th className="col-bill-number">
                    {t('quote.billNumber') || 'QUOTE NUMBER'}
                  </th>
                  <th className="col-amount">
                    {t('quote.amount') || 'AMOUNT'}
                  </th>
                  <th className="col-date">{t('quote.date') || 'DATE'}</th>
                  <th className="col-action">
                    {t('quote.action') || 'ACTION'}
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredQuotes.map(quote => (
                  <tr key={quote.quoteNumber}>
                    <td className="col-bill-number">
                      <div className="bill-info">
                        <span className="bill-number">{quote.quoteNumber}</span>
                        <span className={getStatusClass(quote.status)}>
                          {getStatusLabel(quote.status)}
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
                        year: 'numeric',
                      })}
                      <span className="time">
                        {t('quote.at') || 'at'}{' '}
                        {new Date(quote.createdAt).toLocaleTimeString('en-US', {
                          hour: '2-digit',
                          minute: '2-digit',
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
                            {t('quote.sendFinalInvoice') ||
                              'Send Final Invoice'}
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
