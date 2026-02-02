import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import { MdArrowBack, MdPrint } from 'react-icons/md';
import ErrorModal from '../../features/users/components/ErrorModal';
import './QuoteDetailPage.css';

/**
 * QuoteDetailPage Component
 * Displays full quote details with all line items
 */
const QuoteDetailPage = () => {
  const { t } = useTranslation();
  const { quoteNumber } = useParams();
  const navigate = useNavigate();
  const { getAccessTokenSilently } = useAuth0();

  const [quote, setQuote] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isErrorModalOpen, setIsErrorModalOpen] = useState(false);
  const [projectInfo, setProjectInfo] = useState(null);

  useEffect(() => {
    const fetchQuote = async () => {
      try {
        setLoading(true);
        const token = await getAccessTokenSilently();

        const response = await axios.get(`/api/v1/quotes/${quoteNumber}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setQuote(response.data);
      } catch (err) {
        console.error('Error fetching quote:', err);
        const message =
          err.response?.data?.message ||
          err.message ||
          t('quote.errorLoadingDetail') ||
          'Failed to load quote details';
        setError(message);
        setIsErrorModalOpen(true);
      } finally {
        setLoading(false);
      }
    };

    if (quoteNumber) {
      fetchQuote();
    }
  }, [quoteNumber, getAccessTokenSilently, t]);

  useEffect(() => {
    const loadRelatedInfo = async () => {
      if (!quote) return;

      try {
        const token = await getAccessTokenSilently();

        if (quote.projectIdentifier) {
          try {
            const projectResponse = await axios.get(
              `/api/v1/projects/${quote.projectIdentifier}`,
              {
                headers: { Authorization: `Bearer ${token}` },
              }
            );
            if (projectResponse.data) {
              setProjectInfo(projectResponse.data);
            }
          } catch (err) {
            console.error('Error fetching project info:', err);
          }
        }
      } catch (err) {
        console.error('Error loading related info:', err);
      }
    };

    loadRelatedInfo();
  }, [quote, getAccessTokenSilently]);

  const handleBack = () => {
    navigate('/quotes');
  };

  const handlePrint = () => {
    window.print();
  };

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

  if (loading) {
    return (
      <div className="quote-detail-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>{t('common.loading') || 'Loading...'}</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="quote-detail-page">
        <div className="quote-detail-container">
          <div className="detail-header">
            <button className="btn btn-back" onClick={handleBack}>
              <MdArrowBack /> {t('common.back') || 'Back'}
            </button>
          </div>
          <div className="empty-state">
            <h3>{t('quote.notFound') || 'Quote not found'}</h3>
            <button className="btn btn-primary" onClick={handleBack}>
              {t('common.goBack') || 'Go Back'}
            </button>
          </div>
        </div>
        <ErrorModal
          isOpen={isErrorModalOpen}
          title={t('quote.errorTitle') || 'Quote Error'}
          message={error}
          onClose={() => setIsErrorModalOpen(false)}
        />
      </div>
    );
  }

  if (!quote) {
    return (
      <div className="quote-detail-page">
        <div className="empty-state">
          <h3>{t('quote.notFound') || 'Quote not found'}</h3>
          <button className="btn btn-primary" onClick={handleBack}>
            {t('common.goBack') || 'Go Back'}
          </button>
        </div>
      </div>
    );
  }

  // Calculate totals
  const subtotal = (quote.lineItems || []).reduce(
    (sum, item) => sum + item.lineTotal,
    0
  );
  const discount = 0; // TODO: Get from quote if available
  const gst = subtotal * 0.05;
  const qst = subtotal * 0.09975;
  const total = quote.totalAmount || subtotal + gst + qst;

  return (
    <div className="quote-detail-page">
      <div className="quote-detail-container">
        {/* Header Actions */}
        <div className="detail-header">
          <button className="btn btn-back" onClick={handleBack}>
            <MdArrowBack /> {t('common.back') || 'Back'}
          </button>
          <div className="detail-actions">
            <button className="btn btn-secondary" onClick={handlePrint}>
              <MdPrint /> {t('quote.print') || 'Print'}
            </button>
          </div>
        </div>

        {/* Quote Header */}
        <div className="quote-header-section">
          <div className="quote-title">
            <h1>{t('quote.billEstimate') || 'Bill Estimate'}</h1>
            <span className="quote-number">{quote.quoteNumber}</span>
          </div>
          <div className="quote-meta">
            <div className="meta-item">
              <span className="meta-label">
                {t('quote.category') || 'Category'}
              </span>
              <span className="meta-value">{quote.category || 'N/A'}</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">{t('quote.date') || 'Date'}</span>
              <span className="meta-value">
                {new Date(quote.createdAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </span>
            </div>
            <div className="meta-item">
              <span className="meta-label">
                {t('quote.status') || 'Status'}
              </span>
              <span className={getStatusClass(quote.status)}>
                {getStatusLabel(quote.status)}
              </span>
            </div>
          </div>
        </div>

        {/* Parties & Project Section */}
        <div className="quote-party-section">
          <div className="party-card">
            <span className="party-label">{t('quote.from') || 'From'}</span>
            <h3>{quote.contractorName || '—'}</h3>
            <p>{quote.contractorPhone || '—'}</p>
          </div>
          <div className="party-card">
            <span className="party-label">{t('quote.to') || 'To'}</span>
            <h3>{quote.customerName || '—'}</h3>
            <p>{quote.customerPhone || '—'}</p>
          </div>
          <div className="party-card">
            <span className="party-label">
              {t('quote.project') || 'Project'}
            </span>
            <h3>{projectInfo?.projectName || quote.projectIdentifier}</h3>
            <p>{quote.lotAddress || '—'}</p>
          </div>
        </div>

        {/* Line Items Table */}
        <div className="quote-items-section">
          <h2>{t('quote.itemDetails') || 'Item Details'}</h2>
          <table className="items-table">
            <thead>
              <tr>
                <th>{t('quote.description') || 'Description'}</th>
                <th className="text-center">{t('quote.hours') || 'Hours'}</th>
                <th className="text-right">
                  {t('quote.rate') || 'Rate ($/hr)'}
                </th>
                <th className="text-right">{t('quote.amount') || 'Amount'}</th>
              </tr>
            </thead>
            <tbody>
              {(quote.lineItems || []).map((item, index) => (
                <tr key={index}>
                  <td>{item.itemDescription}</td>
                  <td className="text-center">{item.quantity}</td>
                  <td className="text-right">${item.rate?.toFixed(2)}/hr</td>
                  <td className="text-right">${item.lineTotal?.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Totals Section */}
        <div className="quote-totals-section">
          <div className="totals-row">
            <span className="totals-label">
              {t('quote.subtotal') || 'Subtotal'}
            </span>
            <span className="totals-value">${subtotal.toFixed(2)}</span>
          </div>
          {discount > 0 && (
            <div className="totals-row">
              <span className="totals-label">
                {t('quote.discount') || 'Discount'}
              </span>
              <span className="totals-value">-${discount.toFixed(2)}</span>
            </div>
          )}
          <div className="totals-row">
            <span className="totals-label">{t('quote.gst') || 'GST'} (5%)</span>
            <span className="totals-value">+${gst.toFixed(2)}</span>
          </div>
          <div className="totals-row">
            <span className="totals-label">
              {t('quote.qst') || 'QST'} (9.975%)
            </span>
            <span className="totals-value">+${qst.toFixed(2)}</span>
          </div>
          <div className="totals-row totals-total">
            <span className="totals-label">{t('quote.total') || 'Total'}</span>
            <span className="totals-value">${total.toFixed(2)}</span>
          </div>
        </div>

        {/* Notes Section */}
        {quote.notes && (
          <div className="quote-notes-section">
            <h3>{t('quote.notes') || 'Notes'}</h3>
            <p>{quote.notes}</p>
          </div>
        )}

        {/* Approval Status Section */}
        {quote.status && (
          <div className={`approval-status-section status-${quote.status.toLowerCase()}`}>
            <h3>{t('quote.approvalStatus') || 'Approval Status'}</h3>
            <div className="status-detail">
              <div className="status-info">
                <span className="status-label">{t('quote.currentStatus') || 'Current Status'}</span>
                <span className={`status-badge status-${quote.status.toLowerCase()}`}>
                  {quote.status}
                </span>
              </div>
              {quote.approvedBy && (
                <div className="status-info">
                  <span className="status-label">{t('quote.approvedBy') || 'Approved By'}</span>
                  <span className="status-value">{quote.approvedBy}</span>
                </div>
              )}
              {quote.approvedAt && (
                <div className="status-info">
                  <span className="status-label">{t('quote.approvedDate') || 'Approved Date'}</span>
                  <span className="status-value">
                    {new Date(quote.approvedAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                </div>
              )}
              {quote.rejectionReason && (
                <div className="status-info rejection-reason">
                  <span className="status-label">{t('quote.rejectionReason') || 'Rejection Reason'}</span>
                  <p className="reason-text">{quote.rejectionReason}</p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default QuoteDetailPage;
