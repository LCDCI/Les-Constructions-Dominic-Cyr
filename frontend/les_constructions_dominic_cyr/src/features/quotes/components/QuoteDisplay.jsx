import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { quoteApi } from '../api/quoteApi';
import '../styles/QuoteDisplay.css';

/**
 * QuoteDisplay Component
 * Displays a quote with all its line items and totals.
 */
const QuoteDisplay = ({ projectIdentifier, token, quoteNumber = null }) => {
  const { t } = useTranslation();
  const [quotes, setQuotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedQuote, setSelectedQuote] = useState(null);

  useEffect(() => {
    const fetchQuotes = async () => {
      try {
        setLoading(true);
        setError(null);

        if (quoteNumber) {
          // Fetch specific quote by number
          const quote = await quoteApi.getQuoteByNumber(quoteNumber, token);
          setQuotes([quote]);
          setSelectedQuote(quote);
        } else if (projectIdentifier) {
          // Fetch all quotes for project
          const allQuotes = await quoteApi.getQuotesByProject(projectIdentifier, token);
          setQuotes(allQuotes);
          if (allQuotes.length > 0) {
            setSelectedQuote(allQuotes[0]);
          }
        }
      } catch (err) {
        setError(err.message || t('quote.errorFetchingQuotes'));
        console.error('Error fetching quotes:', err);
      } finally {
        setLoading(false);
      }
    };

    if (projectIdentifier || quoteNumber) {
      fetchQuotes();
    }
  }, [projectIdentifier, quoteNumber, token, t]);

  if (loading) {
    return <div className="quote-display loading">{t('common.loading')}</div>;
  }

  if (error) {
    return <div className="quote-display error">{error}</div>;
  }

  if (!quotes.length) {
    return (
      <div className="quote-display empty">
        <p>{t('quote.noQuotesFound')}</p>
      </div>
    );
  }

  const quote = selectedQuote || quotes[0];

  return (
    <div className="quote-display">
      {quotes.length > 1 && (
        <div className="quote-selector">
          <label htmlFor="quote-select">{t('quote.selectQuote')}:</label>
          <select
            id="quote-select"
            value={selectedQuote?.quoteNumber || ''}
            onChange={(e) => {
              const selected = quotes.find((q) => q.quoteNumber === e.target.value);
              setSelectedQuote(selected);
            }}
          >
            {quotes.map((q) => (
              <option key={q.quoteNumber} value={q.quoteNumber}>
                {q.quoteNumber}
              </option>
            ))}
          </select>
        </div>
      )}

      <div className="quote-container">
        <div className="quote-header">
          <div className="quote-number">
            <span className="label">{t('quote.quoteNumber')}:</span>
            <span className="value">{quote.quoteNumber}</span>
          </div>
          <div className="quote-metadata">
            <div>
              <span className="label">{t('quote.createdAt')}:</span>
              <span className="value">
                {new Date(quote.createdAt).toLocaleDateString()}
              </span>
            </div>
            {quote.updatedAt !== quote.createdAt && (
              <div>
                <span className="label">{t('quote.updatedAt')}:</span>
                <span className="value">
                  {new Date(quote.updatedAt).toLocaleDateString()}
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="line-items-display">
          <h3>{t('quote.lineItems')}</h3>
          <table className="line-items-table">
            <thead>
              <tr>
                <th className="col-description">{t('quote.description')}</th>
                <th className="col-quantity">{t('quote.quantity')}</th>
                <th className="col-rate">{t('quote.rate')}</th>
                <th className="col-total">{t('quote.lineTotal')}</th>
              </tr>
            </thead>
            <tbody>
              {quote.lineItems.map((item, index) => (
                <tr key={item.lineItemId || index}>
                  <td className="col-description">{item.itemDescription}</td>
                  <td className="col-quantity">
                    {parseFloat(item.quantity).toFixed(2)}
                  </td>
                  <td className="col-rate">
                    ${parseFloat(item.rate).toFixed(2)}
                  </td>
                  <td className="col-total">
                    ${parseFloat(item.lineTotal).toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="quote-totals">
          <div className="total-row">
            <span className="label">{t('quote.subtotal')}:</span>
            <span className="amount">
              ${parseFloat(quote.totalAmount).toFixed(2)}
            </span>
          </div>
          <div className="total-row total">
            <span className="label">{t('quote.total')}:</span>
            <span className="amount">
              ${parseFloat(quote.totalAmount).toFixed(2)}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuoteDisplay;
