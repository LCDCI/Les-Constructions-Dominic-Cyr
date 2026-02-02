import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import { quoteApi } from '../../features/quotes/api/quoteApi';
import { MdCheckCircle, MdCancel } from 'react-icons/md';
import { FiEye } from 'react-icons/fi';
import ErrorModal from '../../features/users/components/ErrorModal';
import ApprovalModal from '../../features/quotes/components/ApprovalModal';
import QuoteDetailModal from '../../features/quotes/components/QuoteDetailModal';
import './QuoteApprovalPage.css';

/**
 * QuoteApprovalPage Component
 * Allows owners to view, filter, approve, and reject submitted quotes
 */
const QuoteApprovalPage = () => {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();

  const [quotes, setQuotes] = useState([]);
  const [filteredQuotes, setFilteredQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isErrorModalOpen, setIsErrorModalOpen] = useState(false);
  const [contractorNames, setContractorNames] = useState({});
  const [projectNames, setProjectNames] = useState({});

  // Filter state
  const [filterStatus, setFilterStatus] = useState('SUBMITTED');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('date-desc');

  // Approval modal state
  const [isApprovalModalOpen, setIsApprovalModalOpen] = useState(false);
  const [selectedQuote, setSelectedQuote] = useState(null);
  const [approvalAction, setApprovalAction] = useState(null);

  // Quote detail modal state
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [detailQuoteNumber, setDetailQuoteNumber] = useState(null);
  const [detailToken, setDetailToken] = useState(null);

  // Fetch quotes on mount
  useEffect(() => {
    fetchQuotes();
    fetchContractorNames();
  }, []);

  const fetchQuotes = async () => {
    try {
      setLoading(true);
      setError(null);

      const token = await getAccessTokenSilently();
      const allQuotes = await quoteApi.getSubmittedQuotes(token);
      setQuotes(allQuotes);
      applyFilters(allQuotes);
    } catch (err) {
      console.error('Error fetching quotes:', err);
      const message =
        err.message || t('quote.errorFetchingQuotes') || 'Failed to load quotes';
      setError(message);
      setIsErrorModalOpen(true);
    } finally {
      setLoading(false);
    }
  };

  const fetchContractorNames = async () => {
    try {
      const token = await getAccessTokenSilently();
      const response = await fetch('/api/v1/users', {
        headers: {
          Authorization: `Bearer ${token}`,
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

  const applyFilters = (quotesToFilter, status = filterStatus, query = searchQuery, sort = sortBy) => {
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

  // Update filters
  useEffect(() => {
    applyFilters(quotes, filterStatus, searchQuery, sortBy);
  }, [filterStatus, searchQuery, sortBy, quotes, contractorNames]);

  const handleApproveClick = (quote) => {
    setSelectedQuote(quote);
    setApprovalAction('APPROVE');
    setIsApprovalModalOpen(true);
  };

  const handleRejectClick = (quote) => {
    setSelectedQuote(quote);
    setApprovalAction('REJECT');
    setIsApprovalModalOpen(true);
  };

  const handleViewQuote = async (quoteNumber) => {
    try {
      const token = await getAccessTokenSilently();
      setDetailToken(token);
      setDetailQuoteNumber(quoteNumber);
      setIsDetailModalOpen(true);
    } catch (err) {
      console.error('Error getting token:', err);
      setError('Failed to load quote details');
      setIsErrorModalOpen(true);
    }
  };

  const handleApprovalConfirm = async (rejectionReason) => {
    try {
      const token = await getAccessTokenSilently();

      if (approvalAction === 'APPROVE') {
        await quoteApi.approveQuote(selectedQuote.quoteNumber, token);
        alert(`Quote ${selectedQuote.quoteNumber} approved successfully!`);
      } else if (approvalAction === 'REJECT') {
        await quoteApi.rejectQuote(selectedQuote.quoteNumber, rejectionReason, token);
        alert(`Quote ${selectedQuote.quoteNumber} rejected successfully!`);
      }

      setIsApprovalModalOpen(false);
      setSelectedQuote(null);
      setApprovalAction(null);

      // Refresh quotes
      await fetchQuotes();
      
      // Auto-switch to ALL to see the updated quote
      setFilterStatus('ALL');
    } catch (err) {
      console.error('Error processing approval:', err);
      const message = err.message || 'Failed to process quote approval';
      setError(message);
      setIsErrorModalOpen(true);
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'badge badge-approved';
      case 'REJECTED':
        return 'badge badge-rejected';
      case 'SUBMITTED':
        return 'badge badge-submitted';
      default:
        return 'badge';
    }
  };

  const formatCurrency = (amount) => {
    if (!amount) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(dateString));
  };

  if (loading) {
    return (
      <div className="quote-approval-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>{t('common.loading')}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="quote-approval-page">
      <div className="page-header">
        <h1>{t('quote.approval.title') || 'Quote Approval Management'}</h1>
        <p>{t('quote.approval.subtitle') || 'Review and manage submitted quotes'}</p>
      </div>

      {/* Filters Section */}
      <div className="filters-section">
        <div className="filter-group">
          <label htmlFor="search-query">{t('common.search') || 'Search'}</label>
          <input
            id="search-query"
            type="text"
            placeholder={t('quote.approval.searchPlaceholder') || 'Quote # or Contractor...'}
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="filter-group">
          <label htmlFor="filter-status">{t('quote.status') || 'Status'}</label>
          <select
            id="filter-status"
            value={filterStatus}
            onChange={e => setFilterStatus(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">{t('common.all') || 'All'}</option>
            <option value="SUBMITTED">{t('quote.approval.submitted') || 'Submitted'}</option>
            <option value="APPROVED">{t('quote.approval.approved') || 'Approved'}</option>
            <option value="REJECTED">{t('quote.approval.rejected') || 'Rejected'}</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="sort-by">{t('common.sortBy') || 'Sort By'}</label>
          <select
            id="sort-by"
            value={sortBy}
            onChange={e => setSortBy(e.target.value)}
            className="filter-select"
          >
            <option value="date-desc">{t('quote.approval.newestFirst') || 'Newest First'}</option>
            <option value="date-asc">{t('quote.approval.oldestFirst') || 'Oldest First'}</option>
            <option value="amount-desc">
              {t('quote.approval.highestAmount') || 'Highest Amount'}
            </option>
            <option value="amount-asc">{t('quote.approval.lowestAmount') || 'Lowest Amount'}</option>
          </select>
        </div>

        <button onClick={fetchQuotes} className="btn btn-refresh">
          {t('common.refresh') || 'Refresh'}
        </button>
      </div>

      {/* Quotes Table */}
      <div className="quotes-table-container">
        {filteredQuotes.length === 0 ? (
          <div className="empty-state">
            <p>{t('quote.approval.noQuotes') || 'No quotes found'}</p>
          </div>
        ) : (
          <table className="quotes-table">
            <thead>
              <tr>
                <th>{t('quote.number') || 'Quote #'}</th>
                <th>{t('quote.amount') || 'Amount'}</th>
                <th>{t('quote.contractor') || 'Contractor'}</th>
                <th>{t('quote.status') || 'Status'}</th>
                <th>{t('quote.createdDate') || 'Submitted'}</th>
                <th>{t('common.actions') || 'Actions'}</th>
              </tr>
            </thead>
            <tbody>
              {filteredQuotes.map(quote => (
                <tr key={quote.quoteNumber} className="quote-row">
                  <td className="quote-number">{quote.quoteNumber}</td>
                  <td className="quote-amount">{formatCurrency(quote.totalAmount)}</td>
                  <td className="quote-contractor">
                    {contractorNames[quote.contractorId] || quote.contractorId}
                  </td>
                  <td className="quote-status">
                    <span className={getStatusBadgeClass(quote.status)}>
                      {quote.status}
                    </span>
                  </td>
                  <td className="quote-date">{formatDate(quote.createdAt)}</td>
                  <td className="quote-actions">
                    <button
                      onClick={() => handleApproveClick(quote)}
                      className="btn-action approve"
                      title="Approve"
                      disabled={quote.status !== 'SUBMITTED'}
                    >
                      <MdCheckCircle />
                    </button>
                    <button
                      onClick={() => handleRejectClick(quote)}
                      className="btn-action reject"
                      title="Reject"
                      disabled={quote.status !== 'SUBMITTED'}
                    >
                      <MdCancel />
                    </button>
                    <button
                      onClick={() => handleViewQuote(quote.quoteNumber)}
                      className="btn-action view"
                      title="View Details"
                    >
                      <FiEye />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Approval Modal */}
      {isApprovalModalOpen && selectedQuote && (
        <ApprovalModal
          quote={selectedQuote}
          action={approvalAction}
          onConfirm={handleApprovalConfirm}
          onCancel={() => {
            setIsApprovalModalOpen(false);
            setSelectedQuote(null);
            setApprovalAction(null);
          }}
        />
      )}

      {/* Error Modal */}
      {isErrorModalOpen && (
        <ErrorModal
          message={error}
          onClose={() => setIsErrorModalOpen(false)}
        />
      )}

      {/* Quote Detail Modal */}
      {isDetailModalOpen && detailQuoteNumber && detailToken && (
        <QuoteDetailModal
          quoteNumber={detailQuoteNumber}
          token={detailToken}
          onClose={() => {
            setIsDetailModalOpen(false);
            setDetailQuoteNumber(null);
            setDetailToken(null);
          }}
        />
      )}
    </div>
  );
};

export default QuoteApprovalPage;
