import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import { quoteApi } from '../../features/quotes/api/quoteApi';
import {
  MdCheckCircle,
  MdVisibility,
  MdRefresh,
  MdSearch,
  MdPayment,
} from 'react-icons/md';
import CustomerApprovalModal from '../../features/quotes/components/CustomerApprovalModal';
import QuoteDetailModal from '../../features/quotes/components/QuoteDetailModal';
import NotificationModal from '../../components/Modals/NotificationModal';
import './CustomerQuoteApprovalPage.css';

/**
 * CustomerQuoteApprovalPage Component
 * Allows customers to view and approve quotes that have been approved by the owner
 */
const CustomerQuoteApprovalPage = () => {
  const { t } = useTranslation('quotes');
  const { getAccessTokenSilently } = useAuth0();

  const [quotes, setQuotes] = useState([]);
  const [filteredQuotes, setFilteredQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [token, setToken] = useState(null);
  const [quotesWithDetails, setQuotesWithDetails] = useState({});

  // Notification state
  const [notification, setNotification] = useState({
    isOpen: false,
    type: 'info',
    message: '',
  });

  // Search state
  const [searchTerm, setSearchTerm] = useState('');

  // Approval modal state
  const [showApprovalModal, setShowApprovalModal] = useState(false);
  const [selectedQuote, setSelectedQuote] = useState(null);

  // Quote detail modal state
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [detailQuote, setDetailQuote] = useState(null);

  // Fetch access token
  useEffect(() => {
    const fetchToken = async () => {
      try {
        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);
      } catch (err) {
        console.error('Error fetching token:', err);
        setError(t('errors.tokenFetch'));
      }
    };
    fetchToken();
  }, [getAccessTokenSilently, t]);

  // Fetch pending quotes
  const fetchPendingQuotes = async () => {
    if (!token) return;

    setLoading(true);
    try {
      const data = await quoteApi.getCustomerPendingQuotes(token);
      setQuotes(data);
      setFilteredQuotes(data);
      setError(null);

      // Resolve lot and project details for each quote
      const details = {};
      for (const quote of data) {
        try {
          // Fetch project info
          let projectName = quote.projectIdentifier;
          if (quote.projectIdentifier) {
            try {
              const projectRes = await axios.get(
                `/api/v1/projects/${quote.projectIdentifier}`,
                { headers: { Authorization: `Bearer ${token}` } }
              );
              if (projectRes.data?.projectName) {
                projectName = projectRes.data.projectName;
              }
            } catch (err) {
              console.warn('Failed to fetch project info:', err);
            }
          }

          // Fetch lot info
          let lotNumber = quote.lotIdentifier;
          if (quote.lotIdentifier && quote.projectIdentifier) {
            try {
              const lotRes = await axios.get(
                `/api/v1/projects/${quote.projectIdentifier}/lots/${quote.lotIdentifier}`,
                { headers: { Authorization: `Bearer ${token}` } }
              );
              if (lotRes.data?.lotNumber) {
                lotNumber = lotRes.data.lotNumber;
              }
            } catch (err) {
              console.warn('Failed to fetch lot info:', err);
            }
          }

          details[quote.quoteNumber] = { projectName, lotNumber };
        } catch (err) {
          console.warn(
            `Error resolving details for quote ${quote.quoteNumber}:`,
            err
          );
        }
      }
      setQuotesWithDetails(details);
    } catch (err) {
      console.error('Error fetching pending quotes:', err);
      setError(t('errors.fetchQuotes'));
      showNotification('error', t('errors.fetchQuotes'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingQuotes();
  }, [token]);

  // Filter quotes by search term
  useEffect(() => {
    if (!searchTerm) {
      setFilteredQuotes(quotes);
      return;
    }

    const filtered = quotes.filter(quote => {
      const searchLower = searchTerm.toLowerCase();
      return (
        quote.quoteNumber?.toLowerCase().includes(searchLower) ||
        quote.projectName?.toLowerCase().includes(searchLower) ||
        quote.lotNumber?.toLowerCase().includes(searchLower)
      );
    });

    setFilteredQuotes(filtered);
  }, [searchTerm, quotes]);

  // Show notification
  const showNotification = (type, message) => {
    setNotification({ isOpen: true, type, message });
  };

  // Close notification
  const closeNotification = () => {
    setNotification({ ...notification, isOpen: false });
  };

  // Handle approve click
  const handleApproveClick = quote => {
    setSelectedQuote(quote);
    setShowApprovalModal(true);
  };

  // Handle view details
  const handleViewDetails = quote => {
    setDetailQuote(quote);
    setShowDetailModal(true);
  };

  // Handle approval confirmation
  const handleApprovalConfirm = async () => {
    if (!selectedQuote || !token) return;

    try {
      await quoteApi.customerApproveQuote(selectedQuote.quoteNumber, token);
      showNotification('success', t('notifications.customerApprovalSuccess'));
      setShowApprovalModal(false);
      setSelectedQuote(null);
      // Refresh the list
      await fetchPendingQuotes();
    } catch (err) {
      console.error('Error approving quote:', err);
      showNotification('error', t('errors.approvalFailed'));
    }
  };

  // Handle refresh
  const handleRefresh = () => {
    fetchPendingQuotes();
  };

  return (
    <div className="customer-quote-approval-page">
      <div className="page-header">
        <div className="header-content">
          <h1 className="page-title">{t('customerApproval.title')}</h1>
          <p className="page-subtitle">{t('customerApproval.subtitle')}</p>
        </div>
        <button
          className="btn-refresh btn-icon-only"
          onClick={handleRefresh}
          disabled={loading}
          title={t('actions.refresh')}
        >
          <MdRefresh className={loading ? 'spinning' : ''} />
        </button>
      </div>

      {/* Payment Info Section */}
      <div className="payment-info-card">
        <div className="payment-info-icon">
          <MdPayment />
        </div>
        <div className="payment-info-content">
          <h3>{t('customerApproval.paymentInfo.title')}</h3>
          <p>{t('customerApproval.paymentInfo.description')}</p>
          <a
            href="https://www.acceo.com/nos-solutions/pme/"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-payment-link"
          >
            {t('customerApproval.paymentInfo.button')}
          </a>
        </div>
      </div>

      {/* Search Bar */}
      <div className="search-bar">
        <MdSearch className="search-icon" />
        <input
          type="text"
          placeholder={t('search.placeholder')}
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      {/* Error State */}
      {error && (
        <div className="error-message">
          <p>{error}</p>
          <button onClick={handleRefresh} className="btn-retry">
            {t('actions.retry')}
          </button>
        </div>
      )}

      {/* Loading State */}
      {loading && !error && (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>{t('loading.quotes')}</p>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && filteredQuotes.length === 0 && (
        <div className="empty-state">
          <MdCheckCircle className="empty-icon" />
          <h3>{t('customerApproval.noQuotes')}</h3>
          <p>{t('customerApproval.noQuotesDescription')}</p>
        </div>
      )}

      {/* Quotes Table */}
      {!loading && !error && filteredQuotes.length > 0 && (
        <div className="table-container">
          <table className="quotes-table">
            <thead>
              <tr>
                <th>{t('table.quoteNumber')}</th>
                <th>{t('table.project')}</th>
                <th>{t('table.lot')}</th>
                <th>{t('table.totalAmount')}</th>
                <th>{t('table.ownerApprovedAt')}</th>
                <th>{t('table.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {filteredQuotes.map(quote => (
                <tr key={quote.quoteNumber}>
                  <td className="quote-number">{quote.quoteNumber}</td>
                  <td>
                    {quotesWithDetails[quote.quoteNumber]?.projectName ||
                      quote.projectIdentifier ||
                      '-'}
                  </td>
                  <td>
                    {quotesWithDetails[quote.quoteNumber]?.lotNumber ||
                      quote.lotIdentifier ||
                      '-'}
                  </td>
                  <td className="amount">
                    $
                    {quote.totalAmount?.toLocaleString('en-US', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                    })}
                  </td>
                  <td>
                    {quote.approvedAt
                      ? new Date(quote.approvedAt).toLocaleDateString()
                      : '-'}
                  </td>
                  <td className="actions">
                    <button
                      className="btn-action btn-view"
                      onClick={() => handleViewDetails(quote)}
                      title={t('actions.viewDetails')}
                    >
                      <MdVisibility />
                    </button>
                    <button
                      className="btn-action btn-approve"
                      onClick={() => handleApproveClick(quote)}
                      title={t('actions.approve')}
                    >
                      <MdCheckCircle />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Customer Approval Modal */}
      {showApprovalModal && selectedQuote && (
        <CustomerApprovalModal
          quote={selectedQuote}
          onConfirm={handleApprovalConfirm}
          onCancel={() => {
            setShowApprovalModal(false);
            setSelectedQuote(null);
          }}
        />
      )}

      {/* Quote Detail Modal */}
      {showDetailModal && detailQuote && (
        <QuoteDetailModal
          quoteNumber={detailQuote.quoteNumber}
          token={token}
          onClose={() => {
            setShowDetailModal(false);
            setDetailQuote(null);
          }}
        />
      )}

      {/* Notification Modal */}
      <NotificationModal
        isOpen={notification.isOpen}
        type={notification.type}
        message={notification.message}
        onClose={closeNotification}
      />
    </div>
  );
};

export default CustomerQuoteApprovalPage;
