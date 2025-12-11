import React, { useEffect, useState } from 'react';
import 'react-phone-number-input/style.css';
import PhoneInput from 'react-phone-number-input'; 
/**
 * Reusable InquiryForm component
 * Props:
 * - onSuccess(message: string): optional callback when submission succeeds
 * - className: optional wrapper class for layout/styling
 */

export default function InquiryForm({ onSuccess, className }) {
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
        message: 'Please fill out all required fields.',
        type: 'error',
      });
      return;
    }
    
    // Validate name has no numbers
    if (/\d/.test(form.name)) {
      setStatus({
        message: 'Name cannot contain numbers.',
        type: 'error',
      });
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
      setStatus({
        message: 'Please enter a valid email address.',
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
        message: 'Network error. Please try again later.',
        type: 'error',
      });
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
          <PhoneInput
            placeholder="Enter phone number"
            value={form.phone}
            onChange={(value) => setForm({ ...form, phone: value })}
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
            rows={5}
            disabled={loading}
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
