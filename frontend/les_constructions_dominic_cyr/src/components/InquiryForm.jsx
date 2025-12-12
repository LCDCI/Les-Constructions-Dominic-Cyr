import React, { useEffect, useState } from 'react';
import 'react-phone-number-input/style.css';
import PhoneInput from 'react-phone-number-input';
import { validate } from 'react-email-validator';
import { useTranslation } from 'react-i18next'; 
/**
 * Reusable InquiryForm component
 * Props:
 * - onSuccess(message: string): optional callback when submission succeeds
 * - className: optional wrapper class for layout/styling
 */

export default function InquiryForm({ onSuccess, className }) {
  const { t } = useTranslation('contact');
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    message: '',
  });
  const [status, setStatus] = useState({ message: '', type: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (status.type === 'success' && status.message) {
      const timer = setTimeout(() => {
        setStatus({ message: '', type: '' });
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [status]);

  const onChange = e => {
    const { name, value } = e.target;

    // Validate name: no numbers allowed
    if (name === 'name') {
      // Remove any digits from the input
      const filteredValue = value.replace(/\d/g, '');
      setForm({ ...form, [name]: filteredValue });
      return;
    }

    setForm({ ...form, [name]: value });
  };

  const onSubmit = async e => {
    e.preventDefault();
    if (!form.name || !form.email || !form.message) {
      setStatus({
        message: t('inquiryForm.requiredFields'),
        type: 'error',
      });
      return;
    }

    // Validate email format using react-email-validator
    if (!validate(form.email)) {
      setStatus({
        message: t('inquiryForm.invalidEmail'),
        type: 'error',
      });
      return;
    }

    setLoading(true);
    setStatus({ message: '', type: '' });
    try {
      const res = await fetch('/api/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      if (res.ok) {
        const text = await res.text();
        setStatus({ message: text, type: 'success' });
        setForm({ name: '', email: '', phone: '', message: '' });
        onSuccess && onSuccess(text);
      } else {
        let errorMessage = 'Submission failed.';
        const text = await res.text();
        try {
          const data = JSON.parse(text);
          if (data && typeof data.message === 'string') {
            errorMessage = data.message;
          }
        } catch {
          // If not JSON, use the text directly
          if (text) errorMessage = text;
        }
        setStatus({ message: errorMessage, type: 'error' });
      }
    } catch (err) {
      setStatus({
        message: t('inquiryForm.networkError'),
        type: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`contact-form ${className || ''}`.trim()}>
      <h2>{t('inquiryForm.title')}</h2>
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>{t('inquiryForm.nameLabel')} *</label>
          <input
            name="name"
            value={form.name}
            onChange={onChange}
            placeholder={t('inquiryForm.namePlaceholder')}
            disabled={loading}
            required
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.emailLabel')} *</label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={onChange}
            placeholder={t('inquiryForm.emailPlaceholder')}
            disabled={loading}
            required
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.phoneLabel')}</label>
          <PhoneInput
            placeholder={t('inquiryForm.phonePlaceholder')}
            value={form.phone}
            onChange={(value) => setForm({ ...form, phone: value })}
            disabled={loading}
            defaultCountry="CA"
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.messageLabel')} *</label>
          <textarea
            name="message"
            value={form.message}
            onChange={onChange}
            placeholder={t('inquiryForm.messagePlaceholder')}
            rows={5}
            disabled={loading}
            required
          />
        </div>
        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? t('inquiryForm.submittingButton') : t('inquiryForm.submitButton')}
        </button>
        {status.message && (
          <div className={`status-message ${status.type}`}>
            {status.message}
          </div>
        )}
      </form>
    </div>
  );
}
