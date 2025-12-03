import React, { useState } from 'react';

/**
 * Reusable InquiryForm component
 * Props:
 * - onSuccess(message: string): optional callback when submission succeeds
 * - className: optional wrapper class for layout/styling
 * note: might change this for a new style since theres no figma page for it
 */

export default function InquiryForm({ onSuccess, className }) {
  const [form, setForm] = useState({ name: '', email: '', phone: '', message: '' });
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!form.name || !form.email || !form.message) {
      setStatus('Please fill out all required fields.');
      return;
    }
    setLoading(true);
    setStatus('');
    try {
      const res = await fetch('/api/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      if (res.ok) {
        const text = await res.text();
        setStatus(text);
        setForm({ name: '', email: '', phone: '', message: '' });
        onSuccess && onSuccess(text);
      } else {
        const errText = await res.text();
        setStatus(errText || 'Submission failed.');
      }
    } catch (err) {
      setStatus('Network error. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`contact-form ${className || ''}`.trim()}>
      <h2>Send us a message</h2>
      <form onSubmit={onSubmit}>
        <div className="form-group">
          <label>Name *</label>
          <input
            name="name"
            value={form.name}
            onChange={onChange}
            placeholder="Your name"
            disabled={loading}
          />
        </div>
        <div className="form-group">
          <label>Email *</label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={onChange}
            placeholder="your.email@example.com"
            disabled={loading}
          />
        </div>
        <div className="form-group">
          <label>Phone</label>
          <input
            name="phone"
            value={form.phone}
            onChange={onChange}
            placeholder="(555) 123-4567"
            disabled={loading}
          />
        </div>
        <div className="form-group">
          <label>Message *</label>
          <textarea
            name="message"
            value={form.message}
            onChange={onChange}
            placeholder="Tell us about your project..."
            rows="5"
            disabled={loading}
          />
        </div>
        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? 'Sending...' : 'Submit Inquiry'}
        </button>
        {status && (
          <div className={`status-message ${status.includes('Thank you') ? 'success' : 'error'}`}>
            {status}
          </div>
        )}
      </form>
    </div>
  );
}
