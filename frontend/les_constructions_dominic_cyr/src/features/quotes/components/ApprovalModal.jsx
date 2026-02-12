import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { MdClose } from 'react-icons/md';
import './ApprovalModal.css';

/**
 * ApprovalModal Component
 * Modal for approving or rejecting quotes
 */
const ApprovalModal = ({ quote, contractorName, action, onConfirm, onCancel }) => {
  const { t } = useTranslation('quotes');
  const [rejectionReason, setRejectionReason] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const handleConfirm = async () => {
    if (action === 'REJECT' && !rejectionReason.trim()) {
      alert(t('quote.approval.reasonRequired') || 'Please provide a rejection reason');
      return;
    }

    setIsProcessing(true);
    try {
      await onConfirm(rejectionReason);
    } finally {
      setIsProcessing(false);
    }
  };

  const isApproval = action === 'APPROVE';
  const modalTitle = isApproval
    ? t('quote.approval.confirmApprove') || 'Approve Quote'
    : t('quote.approval.confirmReject') || 'Reject Quote';

  const formatCurrency = (amount) => {
    if (!amount) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <div className="approval-modal-overlay">
      <div className="approval-modal">
        <div className="modal-header">
          <h2>{modalTitle}</h2>
          <button
            onClick={onCancel}
            className="close-button"
            disabled={isProcessing}
          >
            <MdClose />
          </button>
        </div>

        <div className="modal-content">
          {/* Quote Summary */}
          <div className="quote-summary">
            <div className="summary-row">
              <span className="label">{t('quote.number') || 'Quote #'}</span>
              <span className="value">{quote.quoteNumber}</span>
            </div>
            <div className="summary-row">
              <span className="label">{t('quote.amount') || 'Amount'}</span>
              <span className="value">{formatCurrency(quote.totalAmount)}</span>
            </div>
            <div className="summary-row">
              <span className="label">{t('quote.contractor') || 'Contractor'}</span>
              <span className="value">{contractorName || quote.contractorId}</span>
            </div>
          </div>

          {/* Rejection Reason (only for reject) */}
          {!isApproval && (
            <div className="reason-section">
              <label htmlFor="rejection-reason">
                {t('quote.approval.rejectionReason') || 'Rejection Reason'}
                <span className="required">*</span>
              </label>
              <textarea
                id="rejection-reason"
                value={rejectionReason}
                onChange={e => setRejectionReason(e.target.value)}
                placeholder={
                  t('quote.approval.reasonPlaceholder') ||
                  'Explain why this quote is being rejected...'
                }
                rows="4"
                disabled={isProcessing}
                className="reason-textarea"
              />
              <div className="character-count">
                {rejectionReason.length} / 500
              </div>
            </div>
          )}

          {/* Confirmation Message */}
          <div className={`confirmation-message ${isApproval ? 'approve' : 'reject'}`}>
            {isApproval ? (
              <p>
                {t('quote.approval.approveMessage') ||
                  'Are you sure you want to approve this quote? It will be marked as valid for sending to customers.'}
              </p>
            ) : (
              <p>
                {t('quote.approval.rejectMessage') ||
                  'Are you sure you want to reject this quote? A reason must be provided and the contractor will be notified.'}
              </p>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button
            onClick={onCancel}
            className="btn btn-secondary"
            disabled={isProcessing}
          >
            {t('common.cancel') || 'Cancel'}
          </button>
          <button
            onClick={handleConfirm}
            className={`btn ${isApproval ? 'btn-approve' : 'btn-reject'}`}
            disabled={isProcessing || (!isApproval && !rejectionReason.trim())}
          >
            {isProcessing ? (
              <>
                <span className="spinner-small"></span>
                {t('common.processing') || 'Processing...'}
              </>
            ) : isApproval ? (
              t('quote.approval.approve') || 'Approve'
            ) : (
              t('quote.approval.reject') || 'Reject'
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ApprovalModal;
