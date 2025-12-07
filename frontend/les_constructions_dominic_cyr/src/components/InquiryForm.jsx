import React, { useState } from 'react';

/**
 * Reusable InquiryForm component
 * Props:
 * - onSuccess(message: string): optional callback when submission succeeds
 * - className: optional wrapper class for layout/styling
 */

export default function InquiryForm({ onSuccess, className }) {
  const [form, setForm] = useState({ name: '', email: '', phone: '', message: '' });
  const [status, setStatus] = useState({ message: '', type: '' });
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!form.name || !form.email || !form.message) {
      setStatus({ message: 'Please fill out all required fields.', type: 'error' });
      return;
    }
    setLoading(true);
    setStatus({ message: '', type: '' });
    try {
      const res = await fetch('/api/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      if (res.ok) {
        const text = await res.text();
        setStatus({ message: text, type: 'success' });
        setForm({ name: '', email: '', phone: '', message: '' });
        onSuccess && onSuccess(text);
      } else {
        const errText = await res.text();
        setStatus({ message: errText || 'Submission failed.', type: 'error' });
      }
    } catch (err) {
      setStatus({ message: 'Network error. Please try again later.', type: 'error' });
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
            required
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
            required
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
            required
            required
          />
        </div>
        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? 'Sending...' : 'Submit Inquiry'}
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
