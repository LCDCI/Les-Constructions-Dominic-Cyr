import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import { MdDelete, MdAdd } from 'react-icons/md';
import { AiOutlineEye, AiOutlineDownload } from 'react-icons/ai';
import './QuoteFormPage.css';

/**
 * QuoteFormPage Component
 * Create/Edit Bill Estimate matching second screenshot design
 * Shows contractor info, bill number, item details, discount, tax, totals
 */
const QuoteFormPage = () => {
  const { t } = useTranslation();
  const { getAccessTokenSilently, user } = useAuth0();
  const location = useLocation();
  const navigate = useNavigate();

  const { projectIdentifier, projectName } = location.state || {};

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
  const [discountPercentage, setDiscountPercentage] = useState(0);
  const [taxPercentage, setTaxPercentage] = useState(13); // QC default
  const [token, setToken] = useState(null);
  
  // Contractor info
  const [contractorInfo, setContractorInfo] = useState(null);
  const [projectInfo, setProjectInfo] = useState(null);

  useEffect(() => {
    const initializeForm = async () => {
      try {
        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);

        // Fetch contractor info from user profile
        const userResponse = await axios.get('/api/v1/users/me', {
          headers: { Authorization: `Bearer ${accessToken}` },
        });

        if (userResponse.data) {
          setContractorInfo({
            name: userResponse.data.name || 'Contractor Name',
            email: userResponse.data.email || '',
            address: userResponse.data.address || '',
            city: userResponse.data.city || '',
            province: userResponse.data.province || 'Quebec',
            country: userResponse.data.country || 'Canada',
          });
        }

        // Fetch project details if projectIdentifier provided
        if (projectIdentifier) {
          const projectResponse = await axios.get(`/api/v1/projects/${projectIdentifier}`, {
            headers: { Authorization: `Bearer ${accessToken}` },
          });

          if (projectResponse.data) {
            setProjectInfo({
              name: projectResponse.data.projectName,
              address: projectResponse.data.address || '',
              city: projectResponse.data.city || '',
              province: projectResponse.data.province || 'Quebec',
              country: 'Canada',
            });
          }
        }
      } catch (err) {
        console.error('Error initializing form:', err);
      }
    };

    initializeForm();
  }, [getAccessTokenSilently, projectIdentifier]);

  const calculateLineTotal = (quantity, rate) => {
    if (!quantity || !rate || isNaN(quantity) || isNaN(rate)) return 0;
    return parseFloat(quantity) * parseFloat(rate);
  };

  const calculateSubtotal = () => {
    return lineItems.reduce((sum, item) => {
      return sum + calculateLineTotal(item.quantity, item.rate);
    }, 0);
  };

  const calculateDiscount = () => {
    const subtotal = calculateSubtotal();
    return (subtotal * (discountPercentage / 100));
  };

  const calculateTax = () => {
    const subtotal = calculateSubtotal();
    const discount = calculateDiscount();
    return ((subtotal - discount) * (taxPercentage / 100));
  };

  const calculateTotal = () => {
    const subtotal = calculateSubtotal();
    const discount = calculateDiscount();
    const tax = calculateTax();
    return subtotal - discount + tax;
  };

  const handleLineItemChange = (id, field, value) => {
    setLineItems(items =>
      items.map(item =>
        item.id === id
          ? { ...item, [field]: value, errors: { ...item.errors, [field]: null } }
          : item
      )
    );
  };

  const handleAddLineItem = () => {
    setLineItems([
      ...lineItems,
      {
        id: nextId,
        itemDescription: '',
        quantity: '',
        rate: '',
        displayOrder: lineItems.length + 1,
        errors: {},
      },
    ]);
    setNextId(nextId + 1);
  };

  const handleRemoveLineItem = (id) => {
    if (lineItems.length > 1) {
      setLineItems(lineItems.filter(item => item.id !== id));
    }
  };

  const validateForm = () => {
    let isValid = true;
    const updatedItems = lineItems.map(item => {
      const errors = {};

      if (!item.itemDescription || item.itemDescription.trim() === '') {
        errors.itemDescription = t('quote.errors.descriptionRequired') || 'Description required';
        isValid = false;
      }

      const qty = parseFloat(item.quantity);
      if (!item.quantity || isNaN(qty) || qty <= 0) {
        errors.quantity = t('quote.errors.quantityInvalid') || 'Must be > 0';
        isValid = false;
      }

      const rateVal = parseFloat(item.rate);
      if (item.rate === '' || isNaN(rateVal) || rateVal < 0) {
        errors.rate = t('quote.errors.rateInvalid') || 'Must be >= 0';
        isValid = false;
      }

      return { ...item, errors };
    });

    setLineItems(updatedItems);
    return isValid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      setSubmitError(t('quote.errors.fixErrors') || 'Please fix the errors in the form');
      return;
    }

    if (!projectIdentifier) {
      setSubmitError(t('quote.errors.noProject') || 'No project selected');
      return;
    }

    try {
      setIsSubmitting(true);
      setSubmitError(null);

      const quoteData = {
        projectIdentifier,
        lineItems: lineItems.map(item => ({
          itemDescription: item.itemDescription,
          quantity: parseFloat(item.quantity),
          rate: parseFloat(item.rate),
          displayOrder: item.displayOrder,
        })),
      };

      const response = await axios.post('/api/v1/quotes', quoteData, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      });

      // Success - navigate back to list
      navigate('/quotes', { 
        state: { message: t('quote.createSuccess') || 'Quote created successfully!' } 
      });

    } catch (error) {
      console.error('Quote creation error:', error);
      setSubmitError(
        error.response?.data?.message ||
        error.message ||
        t('quote.errors.createFailed') || 'Failed to create quote'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePreview = () => {
    // TODO: Implement preview functionality
    console.log('Preview clicked');
  };

  const handleCancel = () => {
    navigate('/quotes');
  };

  return (
    <div className="quote-form-page">
      <form onSubmit={handleSubmit} className="quote-form-container">
        {/* Header */}
        <div className="form-header">
          <div className="header-left">
            <h1>{t('quote.createBillEstimate') || 'Create Bill Estimate'}</h1>
            <p className="bill-number-label">
              {t('quote.newBill') || 'New Bill'}: <span className="bill-number-value">QT-XXXXXX</span>
            </p>
          </div>
          {contractorInfo && (
            <div className="contractor-card">
              <div className="contractor-avatar">
                <span>{contractorInfo.name.charAt(0)}</span>
              </div>
              <div className="contractor-details">
                <h3>{contractorInfo.name}</h3>
                <p>{contractorInfo.email}</p>
                <p className="address">
                  {contractorInfo.address}<br />
                  {contractorInfo.city}, {contractorInfo.province}<br />
                  {contractorInfo.country}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Bill Details Section */}
        <div className="bill-details-section">
          <div className="details-grid">
            <div className="detail-item">
              <label>{t('quote.billNumber') || 'Bill Number'}</label>
              <p className="detail-value">QT-XXXXXX</p>
              <span className="detail-meta">
                {t('quote.issuedDate') || 'Issued Date'}: {new Date().toLocaleDateString()}
              </span>
              <span className="detail-meta">
                {t('quote.dueDate') || 'Due Date'}: {new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toLocaleDateString()}
              </span>
            </div>
            <div className="detail-item">
              <label>{t('quote.billedTo') || 'Billed to'}</label>
              {projectInfo && (
                <>
                  <p className="detail-value">{projectInfo.name}</p>
                  <span className="detail-meta">{projectInfo.address}</span>
                  <span className="detail-meta">{projectInfo.city}, {projectInfo.province}</span>
                  <span className="detail-meta">{projectInfo.country}</span>
                </>
              )}
            </div>
          </div>
        </div>

        {submitError && (
          <div className="error-banner">{submitError}</div>
        )}

        {/* Item Details Section */}
        <div className="item-details-section">
          <h3>{t('quote.itemDetails') || 'Item Details'}</h3>
          <p className="section-subtitle">{t('quote.materialsLabor') || 'Materials/Labor'}</p>

          <div className="items-table-wrapper">
            <table className="items-table">
              <thead>
                <tr>
                  <th className="col-item">{t('quote.item') || 'ITEM'}</th>
                  <th className="col-amount">{t('quote.amount') || 'AMOUNT'}</th>
                  <th className="col-rate">{t('quote.rate') || 'RATE'}</th>
                  <th className="col-total">{t('quote.amount') || 'AMOUNT'}</th>
                  <th className="col-actions"></th>
                </tr>
              </thead>
              <tbody>
                {lineItems.map((item) => (
                  <tr key={item.id} className={Object.keys(item.errors).length > 0 ? 'row-error' : ''}>
                    <td className="col-item">
                      <input
                        type="text"
                        value={item.itemDescription}
                        onChange={(e) => handleLineItemChange(item.id, 'itemDescription', e.target.value)}
                        placeholder={t('quote.itemPlaceholder') || 'Item description'}
                        className={item.errors.itemDescription ? 'input-error' : ''}
                      />
                      {item.errors.itemDescription && (
                        <span className="error-hint">{item.errors.itemDescription}</span>
                      )}
                    </td>
                    <td className="col-amount">
                      <input
                        type="number"
                        step="0.01"
                        value={item.quantity}
                        onChange={(e) => handleLineItemChange(item.id, 'quantity', e.target.value)}
                        placeholder="0"
                        className={item.errors.quantity ? 'input-error' : ''}
                      />
                      {item.errors.quantity && (
                        <span className="error-hint">{item.errors.quantity}</span>
                      )}
                    </td>
                    <td className="col-rate">
                      <div className="rate-input">
                        <span className="currency">$</span>
                        <input
                          type="number"
                          step="0.01"
                          value={item.rate}
                          onChange={(e) => handleLineItemChange(item.id, 'rate', e.target.value)}
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
                        ${calculateLineTotal(item.quantity, item.rate).toFixed(2)}
                      </span>
                    </td>
                    <td className="col-actions">
                      <button
                        type="button"
                        className="btn-delete"
                        onClick={() => handleRemoveLineItem(item.id)}
                        disabled={lineItems.length === 1}
                      >
                        <MdDelete />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <button
            type="button"
            className="add-item-link"
            onClick={handleAddLineItem}
          >
            <MdAdd /> {t('quote.addItem') || 'Add Item'}
          </button>
        </div>

        {/* Totals Section */}
        <div className="totals-section">
          <div className="total-row">
            <span className="label">{t('quote.subtotal') || 'Subtotal'}</span>
            <span className="value">${calculateSubtotal().toFixed(2)}</span>
          </div>
          <div className="total-row discount-row">
            <span className="label">{t('quote.discount') || 'Discount'}</span>
            <input
              type="number"
              className="discount-input"
              value={discountPercentage}
              onChange={(e) => setDiscountPercentage(parseFloat(e.target.value) || 0)}
              min="0"
              max="100"
              step="0.1"
            />
            <span className="discount-amount">-${calculateDiscount().toFixed(2)}</span>
          </div>
          <div className="total-row discount-row">
            <span className="label">{t('quote.tax') || 'Tax'}</span>
            <input
              type="number"
              className="tax-input"
              value={taxPercentage}
              onChange={(e) => setTaxPercentage(parseFloat(e.target.value) || 0)}
              min="0"
              max="100"
              step="0.1"
            />
            <span className="tax-amount">${calculateTax().toFixed(2)}</span>
          </div>
          <div className="total-row total">
            <span className="label">{t('quote.total') || 'Total'}</span>
            <span className="value">${calculateTotal().toFixed(2)}</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-cancel"
            onClick={handleCancel}
            disabled={isSubmitting}
          >
            {t('common.cancel') || 'Cancel'}
          </button>
          <button
            type="button"
            className="btn btn-preview"
            onClick={handlePreview}
            disabled={isSubmitting}
          >
            <AiOutlineEye /> {t('quote.preview') || 'Preview'}
          </button>
          <button
            type="submit"
            className="btn btn-send"
            disabled={isSubmitting}
          >
            {isSubmitting ? (t('common.saving') || 'Saving...') : (t('quote.sendEstimatedBill') || 'Send Estimated Bill')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default QuoteFormPage;
