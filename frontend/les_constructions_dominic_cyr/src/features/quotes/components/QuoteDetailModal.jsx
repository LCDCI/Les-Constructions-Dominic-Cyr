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
  const { t } = useTranslation();
  const [quote, setQuote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [contractorName, setContractorName] = useState('');
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
              <p>Loading...</p>
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
          {/* Quote Header */}
          <div className="quote-header">
            <div className="header-row">
              <div className="header-item">
                <span className="label">Quote #</span>
                <span className="value">{quote.quoteNumber}</span>
              </div>
              <div className="header-item">
                <span className="label">Status</span>
                <span className={`status-badge status-${quote.status?.toLowerCase()}`}>
                  {quote.status}
                </span>
              </div>
            </div>
            <div className="header-row">
              <div className="header-item">
                <span className="label">Created</span>
                <span className="value">{formatDate(quote.createdAt)}</span>
              </div>
              <div className="header-item">
                <span className="label">Total Amount</span>
                <span className="value amount">{formatCurrency(quote.totalAmount)}</span>
              </div>
            </div>
          </div>

          {/* Project & Contractor Info */}
          <div className="info-section">
            <h3>Project Information</h3>
            <div className="info-row">
              <span className="label">Project</span>
              <span className="value">{quote.projectIdentifier}</span>
            </div>
            <div className="info-row">
              <span className="label">Lot</span>
              <span className="value">
                {lotDetails ? (
                  <>
                    {lotDetails.lotNumber && `Lot ${lotDetails.lotNumber}`}
                    {lotDetails.address && ` - ${lotDetails.address}`}
                  </>
                ) : (
                  quote.lotIdentifier || '-'
                )}
              </span>
            </div>
            <div className="info-row">
              <span className="label">Contractor</span>
              <span className="value">{contractorName || quote.contractorId}</span>
            </div>
          </div>

          {/* Line Items */}
          <div className="line-items-section">
            <h3>Line Items</h3>
            {quote.lineItems && quote.lineItems.length > 0 ? (
              <table className="line-items-table">
                <thead>
                  <tr>
                    <th>Description</th>
                    <th>Qty</th>
                    <th>Rate</th>
                    <th>Total</th>
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
              <p className="no-items">No line items</p>
            )}
          </div>

          {/* Approval Info (if applicable) */}
          {quote.status === 'APPROVED' && quote.approvedAt && (
            <div className="approval-info approved">
              <h3>Approval Information</h3>
              <div className="info-row">
                <span className="label">Approved Date</span>
                <span className="value">{formatDate(quote.approvedAt)}</span>
              </div>
              <div className="info-row">
                <span className="label">Approved By</span>
                <span className="value">{quote.approvedBy || '-'}</span>
              </div>
            </div>
          )}

          {/* Rejection Info (if applicable) */}
          {quote.status === 'REJECTED' && quote.rejectionReason && (
            <div className="approval-info rejected">
              <h3>Rejection Information</h3>
              <div className="info-row">
                <span className="label">Rejection Reason</span>
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
