import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { quoteApi } from '../../api/quoteApi';
import '../styles/QuoteForm.css';

/**
 * QuoteForm Component
 * Matches the design from the screenshot with:
 * - Bill estimate style header
 * - Item details table
 * - Add item functionality
 * - Dynamic totals and tax calculation
 * - Send/Preview/Download buttons
 */
const QuoteForm = ({
  projectIdentifier,
  projectName,
  token,
  onQuoteCreated,
  onCancel,
}) => {
  const { t } = useTranslation();
  const [lineItems, setLineItems] = useState([
    {
      id: 1,
      itemDescription: '',
      quantity: '',
      rate: '',
      displayOrder: 1,
      errors: {},
    },
  ]);
  const [nextId, setNextId] = useState(2);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [totalAmount, setTotalAmount] = useState('0.00');
  const [discountPercentage, setDiscountPercentage] = useState(0);
  const [taxPercentage, setTaxPercentage] = useState(13); // Default 13% for QC

  /**
   * Calculate line total from quantity × rate
   */
  const calculateLineTotal = (quantity, rate) => {
    if (!quantity || !rate || isNaN(quantity) || isNaN(rate)) {
      return 0;
    }
    return (parseFloat(quantity) * parseFloat(rate)).toFixed(2);
  };

  /**
   * Recalculate total amount from all line items
   */
  const recalculateTotal = items => {
    const subtotal = items.reduce((sum, item) => {
      const lineTotal = calculateLineTotal(item.quantity, item.rate);
      return sum + parseFloat(lineTotal || 0);
    }, 0);
    setTotalAmount(subtotal.toFixed(2));
  };

  /**
   * Update a line item field
   */
  const handleLineItemChange = (id, field, value) => {
    const updatedItems = lineItems.map(item => {
      if (item.id === id) {
        const updated = { ...item, [field]: value, errors: { ...item.errors } };

        if (
          field === 'quantity' ||
          field === 'rate' ||
          field === 'itemDescription'
        ) {
          delete updated.errors[field];
        }

        return updated;
      }
      return item;
    });

    setLineItems(updatedItems);
    recalculateTotal(updatedItems);
  };

  /**
   * Validate a single line item
   */
  const validateLineItem = item => {
    const errors = {};

    if (!item.itemDescription || item.itemDescription.trim() === '') {
      errors.itemDescription =
        t('form.validation.itemDescriptionRequired') || 'Required';
    }

    if (
      !item.quantity ||
      isNaN(item.quantity) ||
      parseFloat(item.quantity) <= 0
    ) {
      errors.quantity =
        t('form.validation.quantityMustBePositive') || 'Must be > 0';
    }

    if (item.rate === '' || isNaN(item.rate) || parseFloat(item.rate) < 0) {
      errors.rate =
        t('form.validation.rateCannotBeNegative') || 'Cannot be negative';
    }

    return errors;
  };

  /**
   * Add a new line item
   */
  const handleAddLineItem = () => {
    const newLineItem = {
      id: nextId,
      itemDescription: '',
      quantity: '',
      rate: '',
      displayOrder: lineItems.length + 1,
      errors: {},
    };

    setLineItems([...lineItems, newLineItem]);
    setNextId(nextId + 1);
  };

  /**
   * Remove a line item
   */
  const handleRemoveLineItem = id => {
    if (lineItems.length === 1) {
      setSubmitError(
        t('form.validation.minOneLineItem') || 'At least one item required'
      );
      return;
    }

    const updatedItems = lineItems
      .filter(item => item.id !== id)
      .map((item, index) => ({
        ...item,
        displayOrder: index + 1,
      }));

    setLineItems(updatedItems);
    recalculateTotal(updatedItems);
    setSubmitError(null);
  };

  /**
   * Validate all line items
   */
  const validateAllLineItems = () => {
    let isValid = true;
    const validatedItems = lineItems.map(item => {
      const errors = validateLineItem(item);
      if (Object.keys(errors).length > 0) {
        isValid = false;
      }
      return { ...item, errors };
    });

    setLineItems(validatedItems);
    return isValid;
  };

  /**
   * Submit the quote
   */
  const handleSubmit = async e => {
    e.preventDefault();
    setSubmitError(null);

    if (!validateAllLineItems()) {
      setSubmitError(
        t('form.validation.pleaseFixErrors') || 'Please fix errors'
      );
      return;
    }

    setIsSubmitting(true);

    try {
      const quoteData = {
        projectIdentifier,
        lineItems: lineItems.map(item => ({
          itemDescription: item.itemDescription.trim(),
          quantity: parseFloat(item.quantity),
          rate: parseFloat(item.rate),
          displayOrder: item.displayOrder,
        })),
      };

      const createdQuote = await quoteApi.createQuote(quoteData, token);

      if (onQuoteCreated) {
        onQuoteCreated(createdQuote);
      }
    } catch (error) {
      setSubmitError(
        error.message || t('form.errors.submitFailed') || 'Submit failed'
      );
      console.error('Quote creation error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Calculate totals
  const subtotal = parseFloat(totalAmount);
  const discount = (subtotal * discountPercentage) / 100;
  const subtotalAfterDiscount = subtotal - discount;
  const tax = (subtotalAfterDiscount * taxPercentage) / 100;
  const total = subtotalAfterDiscount + tax;

  const isFormValid = lineItems.every(
    item =>
      item.itemDescription.trim() !== '' &&
      !isNaN(item.quantity) &&
      parseFloat(item.quantity) > 0 &&
      !isNaN(item.rate) &&
      parseFloat(item.rate) >= 0
  );

  return (
    <div className="quote-form-wrapper">
      <form onSubmit={handleSubmit} className="quote-form">
        {/* Header Section */}
        <div className="quote-header-section">
          <div className="header-left">
            <h1>{t('quote.createNewQuote') || 'Create Bill Estimate'}</h1>
          </div>
          <div className="header-right">
            <p className="note">
              {t('quote.noteQuoteNumberAuto') ||
                'Quote number will be auto-generated'}
            </p>
          </div>
        </div>

        {submitError && <div className="form-error-banner">{submitError}</div>}

        {/* Item Details Section */}
        <div className="item-details-section">
          <h3>{t('quote.itemDetails') || 'Item Details'}</h3>

          <div className="line-items-table-wrapper">
            <table className="line-items-table">
              <thead>
                <tr>
                  <th className="col-item">{t('quote.item') || 'ITEM'}</th>
                  <th className="col-amount">
                    {t('quote.amount') || 'AMOUNT'}
                  </th>
                  <th className="col-rate">{t('quote.rate') || 'RATE'}</th>
                  <th className="col-total">{t('quote.amount') || 'AMOUNT'}</th>
                  <th className="col-actions"></th>
                </tr>
              </thead>
              <tbody>
                {lineItems.map(item => (
                  <tr
                    key={item.id}
                    className={item.errors.itemDescription ? 'row-error' : ''}
                  >
                    <td className="col-item">
                      <input
                        type="text"
                        value={item.itemDescription}
                        onChange={e =>
                          handleLineItemChange(
                            item.id,
                            'itemDescription',
                            e.target.value
                          )
                        }
                        placeholder="Item name / description"
                        className={
                          item.errors.itemDescription ? 'input-error' : ''
                        }
                      />
                      {item.errors.itemDescription && (
                        <span className="error-hint">
                          {item.errors.itemDescription}
                        </span>
                      )}
                    </td>
                    <td className="col-amount">
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        value={item.quantity}
                        onChange={e =>
                          handleLineItemChange(
                            item.id,
                            'quantity',
                            e.target.value
                          )
                        }
                        placeholder="0"
                        className={item.errors.quantity ? 'input-error' : ''}
                      />
                      {item.errors.quantity && (
                        <span className="error-hint">
                          {item.errors.quantity}
                        </span>
                      )}
                    </td>
                    <td className="col-rate">
                      <div className="rate-input">
                        <span className="currency">$</span>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          value={item.rate}
                          onChange={e =>
                            handleLineItemChange(
                              item.id,
                              'rate',
                              e.target.value
                            )
                          }
                          placeholder="0.00"
                          className={item.errors.rate ? 'input-error' : ''}
                        />
                      </div>
                      {item.errors.rate && (
                        <span className="error-hint">{item.errors.rate}</span>
                      )}
                    </td>
                    <td className="col-total">
                      <span className="total-value">
                        ${calculateLineTotal(item.quantity, item.rate)}
                      </span>
                    </td>
                    <td className="col-actions">
                      <button
                        type="button"
                        onClick={() => handleRemoveLineItem(item.id)}
                        className="btn-delete"
                        title="Remove"
                      >
                        ✕
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <a
            href="#"
            className="add-item-link"
            onClick={e => {
              e.preventDefault();
              handleAddLineItem();
            }}
          >
            + {t('quote.addItem') || 'Add Item'}
          </a>
        </div>

        {/* Totals Section */}
        <div className="totals-section">
          <div className="totals-grid">
            <div className="total-row">
              <span className="label">{t('quote.subtotal') || 'Subtotal'}</span>
              <span className="value">${subtotal.toFixed(2)}</span>
            </div>

            <div className="total-row discount-row">
              <span className="label">{t('quote.discount') || 'Discount'}</span>
              <span className="value">
                <input
                  type="number"
                  min="0"
                  max="100"
                  step="0.01"
                  value={discountPercentage}
                  onChange={e =>
                    setDiscountPercentage(parseFloat(e.target.value) || 0)
                  }
                  placeholder="0"
                  className="discount-input"
                />
                %
              </span>
              <span className="value discount-amount">
                -${discount.toFixed(2)}
              </span>
            </div>

            <div className="total-row">
              <span className="label">{t('quote.tax') || 'Tax'}</span>
              <span className="value">
                <input
                  type="number"
                  min="0"
                  max="100"
                  step="0.01"
                  value={taxPercentage}
                  onChange={e =>
                    setTaxPercentage(parseFloat(e.target.value) || 0)
                  }
                  placeholder="0"
                  className="tax-input"
                />
                %
              </span>
              <span className="value tax-amount">${tax.toFixed(2)}</span>
            </div>

            <div className="total-row total">
              <span className="label">{t('quote.total') || 'Total'}</span>
              <span className="value">${total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Actions Section */}
        <div className="actions-section">
          <button
            type="button"
            onClick={onCancel}
            className="btn btn-secondary"
            disabled={isSubmitting}
          >
            {t('form.actions.cancel') || 'Cancel'}
          </button>
          <button
            type="button"
            className="btn btn-outline"
            disabled={!isFormValid || isSubmitting}
          >
            {t('quote.preview') || 'Preview'}
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={!isFormValid || isSubmitting}
          >
            {isSubmitting
              ? t('form.actions.submitting') || 'Sending...'
              : t('form.actions.submit') || 'Send'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default QuoteForm;
