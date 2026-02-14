import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { MdClose, MdCheckCircle, MdInfo } from 'react-icons/md';
import './CustomerApprovalModal.css';

/**
 * CustomerApprovalModal Component
 * Modal for customers to acknowledge and approve quotes
 */
const CustomerApprovalModal = ({ quote, onConfirm, onCancel }) => {
  const { t } = useTranslation('quotes');
  const [acknowledged, setAcknowledged] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const handleConfirm = async () => {
    if (!acknowledged) {
      alert(t('customerApproval.acknowledgmentRequired') || 'Please acknowledge that you have reviewed the quote');
      return;
    }

    setIsProcessing(true);
    try {
      await onConfirm();
    } finally {
      setIsProcessing(false);
    }
  };

  const formatCurrency = (amount) => {
    if (!amount) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <div className="customer-approval-modal-overlay">
      <div className="customer-approval-modal">
        <div className="modal-header">
          <div className="header-icon">
            <MdCheckCircle />
          </div>
          <h2>{t('customerApproval.modalTitle')}</h2>
          <button
            onClick={onCancel}
            className="close-button"
            disabled={isProcessing}
          >
            <MdClose />
          </button>
        </div>

        <div className="customer-approval-modal-content">
          {/* Quote Summary */}
          <div className="quote-summary">
            <div className="summary-row">
              <span className="label">{t('table.quoteNumber')}</span>
              <span className="value quote-number">{quote.quoteNumber}</span>
            </div>
            <div className="summary-row">
              <span className="label">{t('table.project')}</span>
              <span className="value">{quote.projectName || '-'}</span>
            </div>
            <div className="summary-row">
              <span className="label">{t('table.lot')}</span>
              <span className="value">{quote.lotNumber || '-'}</span>
            </div>
            <div className="summary-row total">
              <span className="label">{t('table.totalAmount')}</span>
              <span className="value amount">{formatCurrency(quote.totalAmount)}</span>
            </div>
          </div>

          {/* Information Notice */}
          <div className="info-notice">
            <MdInfo className="info-icon" />
            <p>{t('customerApproval.infoMessage')}</p>
          </div>

          {/* Acknowledgment Checkbox */}
          <div className="acknowledgment-section">
            <label className="checkbox-container">
              <input
                type="checkbox"
                checked={acknowledged}
                onChange={(e) => setAcknowledged(e.target.checked)}
                disabled={isProcessing}
              />
              <span className="checkmark"></span>
              <span className="checkbox-label">
                {t('customerApproval.acknowledgmentText')}
              </span>
            </label>
          </div>
        </div>

        <div className="modal-footer">
          <button
            onClick={onCancel}
            className="btn btn-secondary"
            disabled={isProcessing}
          >
            {t('common.cancel')}
          </button>
          <button
            onClick={handleConfirm}
            className="btn btn-approve"
            disabled={isProcessing || !acknowledged}
          >
            {isProcessing ? (
              <>
                <span className="spinner-small"></span>
                {t('common.processing')}
              </>
            ) : (
              <>
                <MdCheckCircle />
                {t('actions.approve')}
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CustomerApprovalModal;
