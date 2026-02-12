import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { MdClose } from 'react-icons/md';
import { quoteApi } from '../api/quoteApi';
import './QuoteDetailModal.css';

/**
 * QuoteDetailModal Component
 * Displays detailed quote information in a modal
 */
const QuoteDetailModal = ({ quoteNumber, token, onClose }) => {
  const { t } = useTranslation('quotes');
  const [quote, setQuote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [contractorName, setContractorName] = useState('');
  const [approvedByName, setApprovedByName] = useState('');
  const [lotDetails, setLotDetails] = useState(null);

  useEffect(() => {
    fetchQuoteDetails();
  }, [quoteNumber, token]);

  const fetchQuoteDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const quoteData = await quoteApi.getQuoteByNumber(quoteNumber, token);
      setQuote(quoteData);

      // Fetch contractor name
      if (quoteData.contractorId) {
        try {
          const contractorResponse = await fetch(
            `/api/v1/users/${quoteData.contractorId}`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
          );
          if (contractorResponse.ok) {
            const contractorData = await contractorResponse.json();
            setContractorName(
              `${contractorData.firstName} ${contractorData.lastName || ''}`.trim()
            );
          }
        } catch (err) {
          console.warn('Failed to fetch contractor name:', err);
        }
      }

      // Fetch approver name
      if (quoteData.approvedBy) {
        try {
          if (quoteData.approvedBy.includes('|')) {
             const encodedId = encodeURIComponent(quoteData.approvedBy);
             const userResponse = await fetch(
              `/api/v1/users/auth0/${encodedId}`,
              {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              }
            );
            if (userResponse.ok) {
              const userData = await userResponse.json();
              setApprovedByName(
                `${userData.firstName} ${userData.lastName || ''}`.trim()
              );
            }
          } else {
             setApprovedByName(quoteData.approvedBy);
          }
        } catch (err) {
          console.warn('Failed to fetch approver name:', err);
          setApprovedByName(quoteData.approvedBy);
        }
      }

      // Fetch lot details if lot identifier exists
      if (quoteData.lotIdentifier && quoteData.projectIdentifier) {
        try {
          const lotResponse = await fetch(
            `/api/v1/projects/${quoteData.projectIdentifier}/lots/${quoteData.lotIdentifier}`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
          );
          if (lotResponse.ok) {
            const lotData = await lotResponse.json();
            setLotDetails(lotData);
          }
        } catch (err) {
          console.warn('Failed to fetch lot details:', err);
        }
      }
    } catch (err) {
      console.error('Error fetching quote details:', err);
      setError(err.message || 'Failed to load quote details');
    } finally {
      setLoading(false);
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
      <div className="quote-detail-modal-overlay" onClick={onClose}>
        <div className="quote-detail-modal" onClick={e => e.stopPropagation()}>
          <div className="modal-header">
            <h2>Quote Details</h2>
            <button onClick={onClose} className="close-button">
              <MdClose />
            </button>
          </div>
          <div className="modal-content">
            <div className="loading">
              <div className="spinner"></div>
              <p>{t('common.loading') || 'Loading...'}</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="quote-detail-modal-overlay" onClick={onClose}>
        <div className="quote-detail-modal" onClick={e => e.stopPropagation()}>
          <div className="modal-header">
            <h2>Quote Details</h2>
            <button onClick={onClose} className="close-button">
              <MdClose />
            </button>
          </div>
          <div className="modal-content">
            <div className="error-message">{error}</div>
          </div>
        </div>
      </div>
    );
  }

  if (!quote) {
    return null;
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
      case 'PENDING_OWNER_APPROVAL':
        return t('quote.statusPendingOwner') || 'Waiting for Owner Approval';
      case 'OWNER_APPROVED':
        return t('quote.statusOwnerApproved') || 'Owner Approved';
      case 'CUSTOMER_APPROVED':
        return t('quote.statusCustomerApproved') || 'Customer Approved';
      default:
        return status;
    }
  };

  return (
    <div className="quote-detail-modal-overlay" onClick={onClose}>
      <div className="quote-detail-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{t('quote.details') || 'Quote Details'}</h2>
          <button onClick={onClose} className="close-button">
            <MdClose />
          </button>
        </div>

        <div className="modal-content">
          {/* Quote Header */}
          <div className="quote-summary-grid">
            <div className="header-item">
              <span className="label">{t('quote.number') || 'Quote #'}</span>
              <span className="value">{quote.quoteNumber}</span>
            </div>
            <div className="header-item">
              <span className="label">{t('quote.status') || 'Status'}</span>
              <span className={`status-badge status-${quote.status?.toLowerCase()}`}>
                {getStatusLabel(quote.status)}
              </span>
            </div>
            <div className="header-item">
              <span className="label">{t('quote.created') || 'Created'}</span>
              <span className="value">{formatDate(quote.createdAt)}</span>
            </div>
            <div className="header-item">
              <span className="label">{t('quote.totalAmount') || 'Total Amount'}</span>
              <span className="value amount">{formatCurrency(quote.totalAmount)}</span>
            </div>
          </div>

          {/* Project & Contractor Info */}
          <div className="info-section">
            <h3>{t('quote.projectInfo') || 'Project Information'}</h3>
            <div className="info-row">
              <span className="label">{t('quote.project') || 'Project'}</span>
              <span className="value">{quote.projectIdentifier}</span>
            </div>
            <div className="info-row">
              <span className="label">{t('quote.lot') || 'Lot'}</span>
              <span className="value">
                {lotDetails ? (
                  <>
                    {lotDetails.lotNumber && `${t('quote.lot') || 'Lot'} ${lotDetails.lotNumber}`}
                    {lotDetails.address && ` - ${lotDetails.address}`}
                  </>
                ) : (
                  quote.lotIdentifier || '-'
                )}
              </span>
            </div>
            <div className="info-row">
              <span className="label">{t('quote.contractor') || 'Contractor'}</span>
              <span className="value">{contractorName || quote.contractorId}</span>
            </div>
          </div>

          {/* Line Items */}
          <div className="line-items-section">
            <h3>{t('quote.items') || 'Line Items'}</h3>
            {quote.lineItems && quote.lineItems.length > 0 ? (
              <table className="line-items-table">
                <thead>
                  <tr>
                    <th>{t('quote.description') || 'Description'}</th>
                    <th>{t('quote.qty') || 'Qty'}</th>
                    <th>{t('quote.rate') || 'Rate'}</th>
                    <th>{t('quote.total') || 'Total'}</th>
                  </tr>
                </thead>
                <tbody>
                  {quote.lineItems.map((item, idx) => (
                    <tr key={idx}>
                      <td>{item.itemDescription}</td>
                      <td>{item.quantity}</td>
                      <td>{formatCurrency(item.rate)}</td>
                      <td>{formatCurrency(item.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="no-items">{t('quote.noItems') || 'No line items'}</p>
            )}
          </div>

          {/* Approval Info (if applicable) */}
          {quote.status === 'APPROVED' && quote.approvedAt && (
            <div className="approval-info approved">
              <h3>{t('quote.approvalInfo') || 'Approval Information'}</h3>
              <div className="info-row">
                <span className="label">{t('quote.approvedDate') || 'Approved Date'}</span>
                <span className="value">{formatDate(quote.approvedAt)}</span>
              </div>
              <div className="info-row">
                <span className="label">{t('quote.approvedBy') || 'Approved By'}</span>
                <span className="value">{quote.approvedBy || '-'}</span>
              </div>
            </div>
          )}

          {/* Rejection Info (if applicable) */}
          {quote.status === 'REJECTED' && quote.rejectionReason && (
            <div className="approval-info rejected">
              <h3>{t('quote.rejectionInfo') || 'Rejection Information'}</h3>
              <div className="info-row">
                <span className="label">{t('quote.approval.rejectionReason') || 'Rejection Reason'}</span>
                <span className="value">{quote.rejectionReason}</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default QuoteDetailModal;
