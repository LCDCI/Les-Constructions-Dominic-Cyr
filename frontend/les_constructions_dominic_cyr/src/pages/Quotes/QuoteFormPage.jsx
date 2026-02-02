import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import axios from 'axios';
import { MdDelete, MdAdd, MdClose } from 'react-icons/md';
import { AiOutlineEye } from 'react-icons/ai';
import ErrorModal from '../../features/users/components/ErrorModal';
import './QuoteFormPage.css';

/**
 * QuoteFormPage Component
 * Premium Create/Edit Bill Estimate with full customer info and payment terms
 */
const QuoteFormPage = () => {
  const { t } = useTranslation();
  const { getAccessTokenSilently, user } = useAuth0();
  const location = useLocation();
  const navigate = useNavigate();

  const { lotIdentifier, lotNumber, projectIdentifier, projectName } = location.state || {};

  // Form state
  const [lineItems, setLineItems] = useState([
    {
      id: 1,
      itemDescription: '',
      hours: '',
      rate: '',
      displayOrder: 1,
      errors: {},
    },
  ]);
  const [nextId, setNextId] = useState(2);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [isErrorModalOpen, setIsErrorModalOpen] = useState(false);
  const [discountPercentage, setDiscountPercentage] = useState(0);
  const [gstPercentage, setGstPercentage] = useState(5); // Quebec GST
  const [qstPercentage, setQstPercentage] = useState(9.975); // Quebec QST
  const [token, setToken] = useState(null);
  const [showPreview, setShowPreview] = useState(false);
  const [generatedQuoteNumber, setGeneratedQuoteNumber] = useState(null);

  // Info state
  const [contractorInfo, setContractorInfo] = useState(null);
  const [projectInfo, setProjectInfo] = useState(null);
  const [customerInfo, setCustomerInfo] = useState(null);

  // Additional form fields
  const [formData, setFormData] = useState({
    category: 'Kitchen',
    description: '',
    paymentTerms: 'Net 30',
    deliveryDate: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    notes: '',
  });

  const quoteCategories = [
    'Kitchen',
    'Bathroom',
    'Flooring',
    'Painting',
    'Electrical',
    'Plumbing',
    'Roofing',
    'Renovation',
    'Custom',
  ];

  useEffect(() => {
    const initializeForm = async () => {
      try {
        const accessToken = await getAccessTokenSilently();
        setToken(accessToken);

        // Fetch contractor info
        const userResponse = await axios.get('/api/v1/users/me', {
          headers: { Authorization: `Bearer ${accessToken}` },
        });

        if (userResponse.data) {
          setContractorInfo({
            name: userResponse.data.firstName && userResponse.data.lastName 
              ? `${userResponse.data.firstName} ${userResponse.data.lastName}`
              : userResponse.data.name || 'Contractor',
            email: userResponse.data.email || '',
            phone: userResponse.data.phone || '',
            address: userResponse.data.address || '',
            city: userResponse.data.city || '',
            province: userResponse.data.province || 'Quebec',
            country: userResponse.data.country || 'Canada',
          });
        }

        // Fetch lot info and project info
        if (lotIdentifier && projectIdentifier) {
          try {
            // First, fetch project to get customer ID
            const projectResponse = await axios.get(`/api/v1/projects/${projectIdentifier}`, {
              headers: { Authorization: `Bearer ${accessToken}` },
            });

            if (projectResponse.data) {
              setProjectInfo({
                name: projectName || projectResponse.data.projectName || 'Project',
                lotNumber: lotNumber,
                address: projectResponse.data.location || projectResponse.data.address || '',
                city: projectResponse.data.city || '',
                province: projectResponse.data.province || 'Quebec',
                country: 'Canada',
              });

              // Fetch customer details from project's customerId
              if (projectResponse.data.customerId) {
                try {
                  const customerResponse = await axios.get(
                    `/api/v1/users/${projectResponse.data.customerId}`,
                    { headers: { Authorization: `Bearer ${accessToken}` } }
                  );
                  
                  if (customerResponse.data) {
                    setCustomerInfo({
                      name: customerResponse.data.firstName && customerResponse.data.lastName
                        ? `${customerResponse.data.firstName} ${customerResponse.data.lastName}`
                        : customerResponse.data.name || 'Customer',
                      email: customerResponse.data.email || '',
                      phone: customerResponse.data.phone || '',
                      address: customerResponse.data.address || '',
                      city: customerResponse.data.city || '',
                      province: customerResponse.data.province || 'Quebec',
                      country: customerResponse.data.country || 'Canada',
                    });
                  }
                } catch (err) {
                  console.error('Error fetching customer info:', err);
                }
              }
            }
          } catch (err) {
            console.error('Error fetching project info:', err);
          }
        }
      } catch (err) {
        console.error('Error initializing form:', err);
      }
    };

    initializeForm();
  }, [getAccessTokenSilently, lotIdentifier, projectIdentifier]);

  const calculateLineTotal = (hours, rate) => {
    if (!hours || !rate || isNaN(hours) || isNaN(rate)) return 0;
    return parseFloat(hours) * parseFloat(rate);
  };
  
  // Calculate totals as derived values
  const subtotal = lineItems.reduce(
    (sum, item) => sum + calculateLineTotal(item.hours, item.rate),
    0
  );
  const discount = (subtotal * discountPercentage) / 100;
  const subtotalAfterDiscount = subtotal - discount;
  const gst = (subtotalAfterDiscount * gstPercentage) / 100;
  const qst = (subtotalAfterDiscount * qstPercentage) / 100;
  const total = subtotalAfterDiscount + gst + qst;
  const totalHours = lineItems.reduce(
    (sum, item) => sum + (parseFloat(item.hours) || 0),
    0
  );

  const handleLineItemChange = (id, field, value) => {
    setLineItems(items =>
      items.map(item =>
        item.id === id
          ? { ...item, [field]: value, errors: { ...item.errors, [field]: null } }
          : item
      )
    );
  };

  const showError = (message) => {
    let formattedMessage = message;
    if (typeof formattedMessage === 'string') {
      if (formattedMessage.includes('Contractor is not assigned to this project') || formattedMessage.includes('Contractor is not assigned to this lot')) {
        formattedMessage = t('quote.errors.notAssignedToLot') || 'You are not assigned to this lot.';
      }
    }
    setSubmitError(formattedMessage);
    setIsErrorModalOpen(true);
  };

  const handleAddLineItem = () => {
    setLineItems([
      ...lineItems,
      {
        id: nextId,
        itemDescription: '',
        hours: '',
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
    // Filter out empty line items (items with no description, hours, or rate)
    const validItems = lineItems.filter(item => 
      item.itemDescription?.trim() || parseFloat(item.hours) > 0 || parseFloat(item.rate) >= 0
    );

    // Check if we have at least one valid item
    if (validItems.length === 0) {
      showError(t('quote.errors.atLeastOneItem') || 'Please add at least one item to the quote');
      return false;
    }

    let isValid = true;
    const updatedItems = lineItems.map(item => {
      const errors = {};

      // Skip validation for completely empty items
      if (!item.itemDescription?.trim() && !item.hours && !item.rate) {
        return { ...item, errors };
      }

      if (!item.itemDescription || item.itemDescription.trim() === '') {
        errors.itemDescription = t('quote.errors.descriptionRequired') || 'Description required';
        isValid = false;
      }

      const hrs = parseFloat(item.hours);
      if (!item.hours || isNaN(hrs) || hrs <= 0) {
        errors.hours = t('quote.errors.hoursInvalid') || 'Must be > 0';
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
      showError(t('quote.errors.fixErrors') || 'Please fix the errors in the form');
      return;
    }

    if (!lotIdentifier) {
      showError(t('quote.errors.noLot') || 'No lot selected');
      return;
    }

    try {
      setIsSubmitting(true);
      setSubmitError(null);
      setIsErrorModalOpen(false);

      const quoteData = {
        lotIdentifier,
        projectIdentifier,
        category: formData.category,
        description: formData.description,
        paymentTerms: formData.paymentTerms,
        deliveryDate: formData.deliveryDate,
        notes: formData.notes,
        discountPercentage,
        gstPercentage,
        qstPercentage,
        lineItems: lineItems
          .filter(item => item.itemDescription?.trim() || parseFloat(item.hours) > 0 || parseFloat(item.rate) >= 0)
          .map(item => ({
            itemDescription: item.itemDescription,
            quantity: parseFloat(item.hours),
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

      // Capture the generated quote number
      if (response.data && response.data.quoteNumber) {
        setGeneratedQuoteNumber(response.data.quoteNumber);
      }

      navigate('/quotes', { 
        state: { message: t('quote.createSuccess') || 'Quote created successfully!' } 
      });

    } catch (error) {
      console.error('Quote creation error:', error);
      showError(
        error.response?.data?.message ||
        error.message ||
        t('quote.errors.createFailed') || 'Failed to create quote'
      );
    } finally {
      setIsSubmitting(false);
    }
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
                <span>{contractorInfo.name.charAt(0).toUpperCase()}</span>
              </div>
              <div className="contractor-details">
                <h3>{contractorInfo.name}</h3>
                <p>{contractorInfo.email}</p>
                <p>{contractorInfo.phone}</p>
                <p className="address">
                  {contractorInfo.address}<br />
                  {contractorInfo.city}, {contractorInfo.province}<br />
                  {contractorInfo.country}
                </p>
              </div>
            </div>
          )}
        </div>

        <div className="form-body-grid">
          <div className="form-main">
            {/* Bill Details Section */}
            <div className="bill-details-section">
              <h2 className="section-heading">Bill Details</h2>
              <div className="details-grid details-grid-3">
                <div className="detail-item">
                  <label>{t('quote.billNumber') || 'Bill Number'}</label>
                  <p className="detail-value">{generatedQuoteNumber || 'QT-XXXXXX'}</p>
                  <span className="detail-meta">
                    {generatedQuoteNumber ? t('quote.confirmed') || 'Confirmed' : t('quote.auto') || 'Auto-generated'}
                  </span>
                </div>
                <div className="detail-item">
                  <label>{t('quote.category') || 'Category'}</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    className="form-select"
                  >
                    {quoteCategories.map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                </div>
                <div className="detail-item">
                  <label>{t('quote.issuedDate') || 'Issued Date'}</label>
                  <p className="detail-value">{new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</p>
                </div>
              </div>
            </div>

            {/* Customer/Project Section */}
            <div className="bill-details-section">
              <h2 className="section-heading">Billed To</h2>
              {customerInfo ? (
                <div className="customer-info-card">
                  <div className="customer-avatar">
                    <span>{customerInfo.name.charAt(0).toUpperCase()}</span>
                  </div>
                  <div className="customer-details">
                    <h3>{customerInfo.name}</h3>
                    <p className="email-phone">
                      <span>{customerInfo.email}</span>
                      {customerInfo.phone && <span>{customerInfo.phone}</span>}
                    </p>
                    <p className="address">
                      {customerInfo.address && <>{customerInfo.address}<br /></>}
                      {customerInfo.city}, {customerInfo.province}, {customerInfo.country}
                    </p>
                  </div>
                </div>
              ) : (
                <div className="customer-info-card placeholder">
                  <p>Loading customer information...</p>
                </div>
              )}
            </div>

            {/* Form Fields Section */}
            <div className="form-fields-section">
              <h2 className="section-heading">Quote Details</h2>
              <div className="form-grid form-grid-2">
                <div className="form-group">
                  <label htmlFor="description">{t('quote.description') || 'Description'}</label>
                  <textarea
                    id="description"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="Describe the work, materials, or services included in this quote..."
                    rows="4"
                    className="form-textarea"
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="notes">{t('quote.notes') || 'Notes'}</label>
                  <textarea
                    id="notes"
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                    placeholder="Add any additional notes or special instructions..."
                    rows="4"
                    className="form-textarea"
                  />
                </div>
              </div>
              <div className="form-grid form-grid-2">
                <div className="form-group">
                  <label htmlFor="paymentTerms">{t('quote.paymentTerms') || 'Payment Terms'}</label>
                  <select
                    id="paymentTerms"
                    value={formData.paymentTerms}
                    onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
                    className="form-select"
                  >
                    <option value="Net 15">Net 15</option>
                    <option value="Net 30">Net 30</option>
                    <option value="Net 45">Net 45</option>
                    <option value="Net 60">Net 60</option>
                    <option value="Due on Receipt">Due on Receipt</option>
                    <option value="50% Deposit">50% Deposit</option>
                  </select>
                </div>
                <div className="form-group">
                  <label htmlFor="deliveryDate">{t('quote.deliveryDate') || 'Delivery Date'}</label>
                  <input
                    id="deliveryDate"
                    type="date"
                    value={formData.deliveryDate}
                    onChange={(e) => setFormData({ ...formData, deliveryDate: e.target.value })}
                    className="form-input"
                  />
                </div>
              </div>
            </div>

            {/* Item Details Section */}
            <div className="item-details-section">
              <div className="item-details-header">
                <div className="item-details-title">
                  <h2 className="section-heading">{t('quote.itemDetails') || 'Item Details'}</h2>
                  <span className="item-summary">{lineItems.length} items • {totalHours.toFixed(2)} hrs</span>
                </div>
                <button
                  type="button"
                  onClick={handleAddLineItem}
                  className="btn btn-secondary btn-sm add-item-button"
                >
                  <MdAdd /> {t('quote.addItem') || 'Add Item'}
                </button>
              </div>
              <div className="items-table-wrapper">
                <table className="items-table">
                  <thead>
                    <tr>
                      <th>{t('quote.description') || 'Description'}</th>
                      <th>{t('quote.hours') || 'Hours'}</th>
                      <th>{t('quote.rate') || 'Rate ($/hr)'}</th>
                      <th>{t('quote.amount') || 'Amount'}</th>
                      <th>{t('quote.action') || 'Action'}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lineItems.map((item, index) => (
                      <tr key={item.id}>
                        <td>
                          <input
                            type="text"
                            value={item.itemDescription}
                            onChange={(e) => handleLineItemChange(item.id, 'itemDescription', e.target.value)}
                            placeholder="e.g., Labor, Consulting, Installation"
                            className={`form-input ${item.errors.itemDescription ? 'error' : ''}`}
                          />
                          {item.errors.itemDescription && <span className="error-text">{item.errors.itemDescription}</span>}
                        </td>
                        <td>
                          <input
                            type="number"
                            value={item.hours}
                            onChange={(e) => handleLineItemChange(item.id, 'hours', e.target.value)}
                            placeholder="0"
                            step="0.01"
                            min="0"
                            className={`form-input input-number ${item.errors.hours ? 'error' : ''}`}
                          />
                          {item.errors.hours && <span className="error-text">{item.errors.hours}</span>}
                        </td>
                        <td>
                          <div className="input-with-symbol">
                            <span className="currency">$</span>
                            <input
                              type="number"
                              value={item.rate}
                              onChange={(e) => handleLineItemChange(item.id, 'rate', e.target.value)}
                              placeholder="0.00"
                              step="0.01"
                              min="0"
                              className={`form-input input-number ${item.errors.rate ? 'error' : ''}`}
                            />
                            <span className="currency-suffix">/hr</span>
                          </div>
                          {item.errors.rate && <span className="error-text">{item.errors.rate}</span>}
                        </td>
                        <td className="amount-cell">
                          <span className="amount">${calculateLineTotal(item.hours, item.rate).toFixed(2)}</span>
                        </td>
                        <td>
                          <button
                            type="button"
                            onClick={() => handleRemoveLineItem(item.id)}
                            className="btn btn-icon btn-danger"
                            disabled={lineItems.length === 1}
                            title="Remove item"
                          >
                            <MdDelete />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

            </div>
          </div>

          <aside className="form-sidebar">
            <div className="summary-card">
              <h3>{t('quote.summary') || 'Summary'}</h3>
              <div className="summary-row">
                <span>{t('quote.project') || 'Project'}</span>
                <strong>{projectInfo?.name || projectName || '—'}</strong>
              </div>
              <div className="summary-row">
                <span>{t('quote.lot') || 'Lot'}</span>
                <strong>{projectInfo?.lotNumber || lotNumber || '—'}</strong>
              </div>
              <div className="summary-row">
                <span>{t('quote.customer') || 'Customer'}</span>
                <strong>{customerInfo?.name || '—'}</strong>
              </div>
              <div className="summary-row">
                <span>{t('quote.paymentTerms') || 'Payment Terms'}</span>
                <strong>{formData.paymentTerms}</strong>
              </div>
              <div className="summary-row">
                <span>{t('quote.deliveryDate') || 'Delivery Date'}</span>
                <strong>{formData.deliveryDate}</strong>
              </div>
              <div className="summary-divider" />
              <div className="summary-row">
                <span>{t('quote.items') || 'Items'}</span>
                <strong>{lineItems.length}</strong>
              </div>
              <div className="summary-row">
                <span>{t('quote.hours') || 'Hours'}</span>
                <strong>{totalHours.toFixed(2)}</strong>
              </div>
            </div>

            {/* Totals Section */}
            <div className="totals-section">
              <div className="totals-row">
                <span className="totals-label">{t('quote.subtotal') || 'Subtotal'}</span>
                <span className="totals-value">${subtotal.toFixed(2)}</span>
              </div>
              <div className="totals-row discount-row">
                <span className="totals-label">
                  {t('quote.discount') || 'Discount'} ({discountPercentage}%)
                </span>
                <div className="totals-input-group">
                  <span>-${discount.toFixed(2)}</span>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    value={discountPercentage}
                    onChange={(e) => setDiscountPercentage(Math.max(0, Math.min(100, parseFloat(e.target.value) || 0)))}
                    className="totals-input"
                  />
                </div>
              </div>
              <div className="totals-row tax-row">
                <span className="totals-label">
                  {t('quote.gst') || 'GST'} ({gstPercentage}%)
                </span>
                <div className="totals-input-group">
                  <span>+${gst.toFixed(2)}</span>
                </div>
              </div>
              <div className="totals-row tax-row">
                <span className="totals-label">
                  {t('quote.qst') || 'QST'} ({qstPercentage}%)
                </span>
                <div className="totals-input-group">
                  <span>+${qst.toFixed(2)}</span>
                </div>
              </div>
              <div className="totals-row totals-total">
                <span className="totals-label">{t('quote.total') || 'Total'}</span>
                <span className="totals-value">${total.toFixed(2)}</span>
              </div>
            </div>
          </aside>
        </div>

        {/* Totals Section */}
        <div className="totals-section">
          <div className="totals-row">
            <span className="totals-label">{t('quote.subtotal') || 'Subtotal'}</span>
            <span className="totals-value">${subtotal.toFixed(2)}</span>
          </div>
          <div className="totals-row discount-row">
            <span className="totals-label">
              {t('quote.discount') || 'Discount'} ({discountPercentage}%)
            </span>
            <div className="totals-input-group">
              <span>-${discount.toFixed(2)}</span>
              <input
                type="number"
                min="0"
                max="100"
                value={discountPercentage}
                onChange={(e) => setDiscountPercentage(Math.max(0, Math.min(100, parseFloat(e.target.value) || 0)))}
                className="totals-input"
              />
            </div>
          </div>
          <div className="totals-row tax-row">
            <span className="totals-label">
              {t('quote.gst') || 'GST'} ({gstPercentage}%)
            </span>
            <div className="totals-input-group">
              <span>+${gst.toFixed(2)}</span>
            </div>
          </div>
          <div className="totals-row tax-row">
            <span className="totals-label">
              {t('quote.qst') || 'QST'} ({qstPercentage}%)
            </span>
            <div className="totals-input-group">
              <span>+${qst.toFixed(2)}</span>
            </div>
          </div>
          <div className="totals-row totals-total">
            <span className="totals-label">{t('quote.total') || 'Total'}</span>
            <span className="totals-value">${total.toFixed(2)}</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="form-actions">
          <button
            type="button"
            onClick={() => setShowPreview(true)}
            className="btn btn-preview"
          >
            <AiOutlineEye /> {t('quote.preview') || 'Preview'}
          </button>
          <div className="form-actions-right">
            <button
              type="button"
              onClick={handleCancel}
              className="btn btn-secondary"
            >
              {t('common.cancel') || 'Cancel'}
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn btn-primary"
            >
              {isSubmitting ? t('common.submitting') || 'Submitting...' : t('quote.createQuote') || 'Create Quote'}
            </button>
          </div>
        </div>
      </form>

      {/* Preview Modal */}
      {showPreview && (
        <div className="preview-modal-overlay" onClick={() => setShowPreview(false)}>
          <div className="preview-modal" onClick={(e) => e.stopPropagation()}>
            <div className="preview-header">
              <h2>{t('quote.preview') || 'Preview'}</h2>
              <button
                type="button"
                onClick={() => setShowPreview(false)}
                className="preview-close"
              >
                <MdClose />
              </button>
            </div>
            <div className="preview-content">
              <div className="preview-section">
                <h3>Quote Summary</h3>
                <div className="preview-grid">
                  <div>
                    <p className="preview-label">From</p>
                    <p className="preview-value">{contractorInfo?.name}</p>
                    <p className="preview-meta">{contractorInfo?.email}</p>
                  </div>
                  <div>
                    <p className="preview-label">To</p>
                    <p className="preview-value">{customerInfo?.name}</p>
                    <p className="preview-meta">{customerInfo?.email}</p>
                  </div>
                  <div>
                    <p className="preview-label">Amount</p>
                    <p className="preview-value preview-amount">${total.toFixed(2)}</p>
                  </div>
                </div>
              </div>

              <div className="preview-section">
                <h3>Items</h3>
                <table className="preview-table">
                  <thead>
                    <tr>
                      <th>Description</th>
                      <th>Hours</th>
                      <th>Rate ($/hr)</th>
                      <th>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lineItems.map(item => (
                      <tr key={item.id}>
                        <td>{item.itemDescription}</td>
                        <td>{item.hours}</td>
                        <td>${parseFloat(item.rate).toFixed(2)}/hr</td>
                        <td>${calculateLineTotal(item.hours, item.rate).toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="preview-section">
                <div className="preview-totals">
                  <div className="preview-total-row">
                    <span>Subtotal:</span>
                    <span>${subtotal.toFixed(2)}</span>
                  </div>
                  {discountPercentage > 0 && (
                    <div className="preview-total-row">
                      <span>Discount ({discountPercentage}%):</span>
                      <span>-${discount.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="preview-total-row">
                    <span>GST ({gstPercentage}%):</span>
                    <span>+${gst.toFixed(2)}</span>
                  </div>
                  <div className="preview-total-row">
                    <span>QST ({qstPercentage}%):</span>
                    <span>+${qst.toFixed(2)}</span>
                  </div>
                  <div className="preview-total-row preview-total">
                    <span>Total:</span>
                    <span>${total.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              {formData.notes && (
                <div className="preview-section">
                  <h3>Notes</h3>
                  <p>{formData.notes}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <ErrorModal
        isOpen={isErrorModalOpen}
        title={t('quote.errorTitle') || 'Permission Error'}
        message={submitError}
        onClose={() => setIsErrorModalOpen(false)}
      />
    </div>
  );
};

export default QuoteFormPage;
