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
  const { t, i18n } = useTranslation('contact');
  const currentLanguage = i18n.language || 'fr';

  const fallback = {
    fr: {
      title: 'Envoyez-nous un message',
      nameLabel: 'Nom',
      namePlaceholder: 'Votre nom',
      emailLabel: 'Courriel',
      emailPlaceholder: 'votre.courriel@exemple.com',
      phoneLabel: 'Téléphone',
      phonePlaceholder: 'Entrez votre numéro de téléphone',
      messageLabel: 'Message',
      messagePlaceholder: 'Parlez-nous de votre projet...',
      submitButton: 'Soumettre la demande',
      submittingButton: 'Envoi en cours...',
      requiredFields: 'Veuillez remplir tous les champs obligatoires.',
      invalidEmail: 'Veuillez entrer une adresse courriel valide.',
      networkError: 'Erreur réseau. Veuillez réessayer plus tard.',
      submissionFailed: 'Échec de la soumission.',
    },
    en: {
      title: 'Send us a message',
      nameLabel: 'Name',
      namePlaceholder: 'Your name',
      emailLabel: 'Email',
      emailPlaceholder: 'your.email@example.com',
      phoneLabel: 'Phone',
      phonePlaceholder: 'Enter phone number',
      messageLabel: 'Message',
      messagePlaceholder: 'Tell us about your project...',
      submitButton: 'Submit Inquiry',
      submittingButton: 'Sending...',
      requiredFields: 'Please fill out all required fields.',
      invalidEmail: 'Please enter a valid email address.',
      networkError: 'Network error. Please try again later.',
      submissionFailed: 'Submission failed.',
    },
  };

  const f = fallback[currentLanguage] || fallback.fr;
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
        message: t('inquiryForm.requiredFields', f.requiredFields),
        type: 'error',
      });
      return;
    }

    // Validate email format using react-email-validator
    if (!validate(form.email)) {
      setStatus({
        message: t('inquiryForm.invalidEmail', f.invalidEmail),
        type: 'error',
      });
      return;
    }

    setLoading(true);
    setStatus({ message: '', type: '' });
    try {
      const res = await fetch('/api/v1/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
        credentials: 'omit', // prevent sending cookies that could trigger auth on the public endpoint
      });
      if (res.ok) {
        const data = await res.json();
        const message =
          data.message || 'Thank you! Your inquiry has been received.';
        setStatus({ message, type: 'success' });
        setForm({ name: '', email: '', phone: '', message: '' });
        onSuccess && onSuccess(message);
      } else {
        let errorMessage = t('inquiryForm.submissionFailed', f.submissionFailed);
        try {
          const data = await res.json();
          if (data && typeof data.message === 'string') {
            errorMessage = data.message;
          } else if (data && typeof data.error === 'string') {
            errorMessage = data.error;
          }
        } catch {
          errorMessage = t('inquiryForm.submissionFailed', f.submissionFailed);
        }
        setStatus({ message: errorMessage, type: 'error' });
      }
    } catch (err) {
      setStatus({
        message: t('inquiryForm.networkError', f.networkError),
        type: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`contact-form ${className || ''}`.trim()}>
      <h2>{t('inquiryForm.title', f.title)}</h2>
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>{t('inquiryForm.nameLabel', f.nameLabel)} *</label>
          <input
            name="name"
            value={form.name}
            onChange={onChange}
            placeholder={t('inquiryForm.namePlaceholder', f.namePlaceholder)}
            disabled={loading}
            required
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.emailLabel', f.emailLabel)} *</label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={onChange}
            placeholder={t('inquiryForm.emailPlaceholder', f.emailPlaceholder)}
            disabled={loading}
            required
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.phoneLabel', f.phoneLabel)}</label>
          <PhoneInput
            placeholder={t('inquiryForm.phonePlaceholder', f.phonePlaceholder)}
            value={form.phone}
            onChange={value => setForm({ ...form, phone: value })}
            disabled={loading}
            defaultCountry="CA"
          />
        </div>
        <div className="form-group">
          <label>{t('inquiryForm.messageLabel', f.messageLabel)} *</label>
          <textarea
            name="message"
            value={form.message}
            onChange={onChange}
            placeholder={t(
              'inquiryForm.messagePlaceholder',
              f.messagePlaceholder
            )}
            rows={5}
            disabled={loading}
            required
          />
        </div>
        <button type="submit" className="submit-btn" disabled={loading}>
          {loading
            ? t('inquiryForm.submittingButton', f.submittingButton)
            : t('inquiryForm.submitButton', f.submitButton)}
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
