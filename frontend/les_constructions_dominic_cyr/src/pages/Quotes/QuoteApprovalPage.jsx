import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import { quoteApi } from '../../features/quotes/api/quoteApi';
import {
  MdCheckCircle,
  MdCancel,
  MdVisibility,
  MdRefresh,
  MdSearch,
  MdFilterList,
} from 'react-icons/md';
import ApprovalModal from '../../features/quotes/components/ApprovalModal';
import QuoteDetailModal from '../../features/quotes/components/QuoteDetailModal';
import NotificationModal from '../../components/Modals/NotificationModal';
import './QuoteApprovalPage.css';

/**
 * QuoteApprovalPage Component
 * Allows owners to view, filter, approve, and reject submitted quotes
 */
const QuoteApprovalPage = () => {
  const { t } = useTranslation('quotes');
  const { getAccessTokenSilently } = useAuth0();

  const [quotes, setQuotes] = useState([]);
  const [filteredQuotes, setFilteredQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [contractorNames, setContractorNames] = useState({});
  const [token, setToken] = useState(null);
  
  // Notification state
  const [notification, setNotification] = useState({
    isOpen: false,
    type: 'info', // success, error, info
    message: '',
  });

  // Filter state
  const [filterStatus, setFilterStatus] = useState('SUBMITTED');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('date-desc');

  // Approval modal state
  const [showApprovalModal, setShowApprovalModal] = useState(false);
  const [selectedQuote, setSelectedQuote] = useState(null);
  const [approvalAction, setApprovalAction] = useState(null);

  // Quote detail modal state
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Fetch quotes on mount
  useEffect(() => {
    const init = async () => {
      try {
        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);
        await fetchQuotes(accessToken);
        fetchContractorNames(accessToken);
      } catch (err) {
        console.error('Error initializing:', err);
        setError('Failed to initialize');
      }
    };
    init();
  }, []);

  const fetchQuotes = async (accessToken = token) => {
    try {
      setLoading(true);
      setError(null);

      const authToken = accessToken || await getAccessTokenSilently();
      if (!token) setToken(authToken);

      const allQuotes = await quoteApi.getAllQuotes(authToken);
      setQuotes(allQuotes);
      applyFilters(allQuotes, filterStatus, searchTerm, sortBy);
    } catch (err) {
      console.error('Error fetching quotes:', err);
      const message =
        err.message || t('quote.errorFetchingQuotes') || 'Failed to load quotes';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const fetchContractorNames = async (accessToken = token) => {
    try {
      const authToken = accessToken || await getAccessTokenSilently();
      const response = await fetch('/api/v1/users', {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      if (!response.ok) {
        console.warn('Failed to fetch contractor names');
        return;
      }
      const users = await response.json();
      const nameMap = {};
      users.forEach(user => {
        if (user.userId) {
          nameMap[user.userId] = user.firstName + ' ' + (user.lastName || '');
        }
      });
      setContractorNames(nameMap);
    } catch (err) {
      console.warn('Error fetching contractor names:', err);
    }
  };

  const applyFilters = (quotesToFilter, status = filterStatus, query = searchTerm, sort = sortBy) => {
    let result = [...quotesToFilter];

    // Filter by status
    if (status && status !== 'ALL') {
      result = result.filter(q => q.status === status);
    }

    // Filter by search query (quote number or contractor name)
    if (query.trim()) {
      const queryLower = query.toLowerCase();
      result = result.filter(q =>
        q.quoteNumber.toLowerCase().includes(queryLower) ||
        (contractorNames[q.contractorId] &&
          contractorNames[q.contractorId].toLowerCase().includes(queryLower))
      );
    }

    // Sort
    if (sort === 'date-desc') {
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (sort === 'date-asc') {
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    } else if (sort === 'amount-desc') {
      result.sort((a, b) => (b.totalAmount || 0) - (a.totalAmount || 0));
    } else if (sort === 'amount-asc') {
      result.sort((a, b) => (a.totalAmount || 0) - (b.totalAmount || 0));
    }

    setFilteredQuotes(result);
  };
  
  // Updates when filters change
  useEffect(() => {
    applyFilters(quotes);
  }, [filterStatus, searchTerm, sortBy, contractorNames]);

  const handleApproveClick = (quote) => {
    setSelectedQuote(quote);
    setApprovalAction('APPROVE');
    setShowApprovalModal(true);
  };

  const handleRejectClick = (quote) => {
    setSelectedQuote(quote);
    setApprovalAction('REJECT');
    setShowApprovalModal(true);
  };

  const handleViewClick = (quote) => {
    setSelectedQuote(quote);
    setShowDetailModal(true);
  };

  const handleApprovalConfirm = async (rejectionReason) => {
    if (!selectedQuote || !token) return;

    try {
      if (approvalAction === 'APPROVE') {
        await quoteApi.approveQuote(selectedQuote.quoteNumber, token);
        showNotification('success', t('quote.approval.approveSuccess') || 'Quote approved successfully');
      } else {
        await quoteApi.rejectQuote(selectedQuote.quoteNumber, rejectionReason, token);
        showNotification('success', t('quote.approval.rejectSuccess') || 'Quote rejected successfully');
      }
      
      setShowApprovalModal(false);
      fetchQuotes(); // Refresh list
    } catch (err) {
      console.error('Error processing quote:', err);
      showNotification('error', err.message || 'Failed to process quote');
      // Do not close modal on error so user can try again
    }
  };

  const showNotification = (type, message) => {
    setNotification({ isOpen: true, type, message });
  };

  const closeNotification = () => {
    setNotification({ ...notification, isOpen: false });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString();
  };

  if (loading && !quotes.length) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>{t('common.loading') || 'Loading...'}</p>
      </div>
    );
  }

  // Helper to translate status
  const getStatusLabel = (status) => {
    switch (status) {
      case 'SUBMITTED':
        return t('quote.approval.submitted') || 'Submitted';
      case 'APPROVED':
        return t('quote.approval.approved') || 'Approved';
      case 'REJECTED':
        return t('quote.approval.rejected') || 'Rejected';
      default:
        return status;
    }
  };

  return (
    <div className="quote-approval-page">
      <div className="page-header">
        <h1>{t('quote.approval.title') || 'Quote Approvals'}</h1>
        <p>{t('quote.approval.subtitle') || 'Review and manage contractor quotes'}</p>
      </div>

      <div className="filters-section">
        <div className="filter-group">
          <label><MdSearch /> {t('common.search') || 'Search'}</label>
          <input
            type="text"
            className="search-input"
            placeholder={t('quote.approval.searchPlaceholder') || 'Quote #, Project, Contractor...'}
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <label><MdFilterList /> {t('quote.status') || 'Status'}</label>
          <select
            className="filter-select"
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
          >
            <option value="ALL">{t('common.all') || 'All Statuses'}</option>
            <option value="SUBMITTED">{t('quote.approval.submitted') || 'Pending Review'}</option>
            <option value="APPROVED">{t('quote.approval.approved') || 'Approved'}</option>
            <option value="REJECTED">{t('quote.approval.rejected') || 'Rejected'}</option>
          </select>
        </div>

        <button className="btn-refresh" onClick={() => fetchQuotes()} title={t('common.refresh') || 'Refresh List'}>
          <MdRefresh size={20} />
        </button>
      </div>

      <div className="quotes-table-container">
        {filteredQuotes.length === 0 ? (
          <div className="empty-state">
            <p>{t('quote.noQuotes') || 'No quotes found matching your filters.'}</p>
          </div>
        ) : (
          <table className="quotes-table">
            <thead>
              <tr>
                <th>{t('quote.number') || 'Quote #'}</th>
                <th>{t('quote.project') || 'Project'}</th>
                <th className="quote-contractor">{t('quote.contractor') || 'Contractor'}</th>
                <th className="quote-date">{t('quote.date') || 'Date'}</th>
                <th>{t('quote.amount') || 'Amount'}</th>
                <th>{t('quote.status') || 'Status'}</th>
                <th>{t('common.actions') || 'Actions'}</th>
              </tr>
            </thead>
            <tbody>
              {filteredQuotes.map(quote => (
                <tr key={quote.quoteId}>
                  <td className="quote-number" data-label={t('quote.number') || 'Quote #'}>{quote.quoteNumber}</td>
                  <td data-label={t('quote.project') || 'Project'}>{quote.projectIdentifier}</td>
                  <td className="quote-contractor" data-label={t('quote.contractor') || 'Contractor'}>
                    {contractorNames[quote.contractorId] || quote.contractorId}
                  </td>
                  <td className="quote-date" data-label={t('quote.date') || 'Date'}>{formatDate(quote.createdAt)}</td>
                  <td className="quote-amount" data-label={t('quote.amount') || 'Amount'}>{formatCurrency(quote.totalAmount)}</td>
                  <td className="quote-status" data-label={t('quote.status') || 'Status'}>
                    <span className={`badge badge-${quote.status.toLowerCase()}`}>
                      {getStatusLabel(quote.status)}
                    </span>
                  </td>
                  <td data-label={t('common.actions') || 'Actions'}>
                    <div className="quote-actions">
                      <button
                        className="btn-action view"
                        onClick={() => handleViewClick(quote)}
                        title={t('common.view') || 'View Details'}
                      >
                        <MdVisibility />
                      </button>
                      {quote.status === 'SUBMITTED' && (
                        <>
                          <button
                            className="btn-action approve"
                            onClick={() => handleApproveClick(quote)}
                            title={t('quote.approval.approve') || 'Approve'}
                          >
                            <MdCheckCircle />
                          </button>
                          <button
                            className="btn-action reject"
                            onClick={() => handleRejectClick(quote)}
                            title={t('quote.approval.reject') || 'Reject'}
                          >
                            <MdCancel />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modals */}
      {showApprovalModal && selectedQuote && (
        <ApprovalModal
          quote={selectedQuote}
          contractorName={contractorNames[selectedQuote.contractorId]}
          action={approvalAction}
          onConfirm={handleApprovalConfirm}
          onCancel={() => setShowApprovalModal(false)}
        />
      )}

      {showDetailModal && selectedQuote && (
        <QuoteDetailModal
          quoteNumber={selectedQuote.quoteNumber}
          token={token}
          onClose={() => setShowDetailModal(false)}
        />
      )}
      
      <NotificationModal 
        isOpen={notification.isOpen}
        type={notification.type}
        message={notification.message}
        onClose={closeNotification}
      />
    </div>
  );
};

export default QuoteApprovalPage;
