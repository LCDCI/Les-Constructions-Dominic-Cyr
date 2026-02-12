import React, { useEffect } from 'react';
import { MdCheckCircle, MdError, MdInfo, MdClose } from 'react-icons/md';
import './NotificationModal.css';

/**
 * NotificationModal Component
 * Replaces native alerts with a styled modal
 * 
 * @param {boolean} isOpen - Whether the modal is open
 * @param {string} type - 'success', 'error', or 'info'
 * @param {string} message - The message to display
 * @param {function} onClose - Function to close the modal
 */
const NotificationModal = ({ isOpen, type = 'info', message, onClose }) => {
  if (!isOpen) return null;

  // Auto-close success messages after 3 seconds
  useEffect(() => {
    if (type === 'success') {
      const timer = setTimeout(() => {
        onClose();
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [type, isOpen, onClose]);

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <MdCheckCircle className="notification-icon success" />;
      case 'error':
        return <MdError className="notification-icon error" />;
      default:
        return <MdInfo className="notification-icon info" />;
    }
  };

  const getTitle = () => {
    switch (type) {
      case 'success':
        return 'Success';
      case 'error':
        return 'Error';
      default:
        return 'Information';
    }
  };

  return (
    <div className="notification-modal-overlay" onClick={onClose}>
      <div className="notification-modal" onClick={e => e.stopPropagation()}>
        <button className="close-button-absolute" onClick={onClose}>
          <MdClose />
        </button>
        
        <div className="notification-content">
          {getIcon()}
          <h3 className={`notification-title ${type}`}>{getTitle()}</h3>
          <p className="notification-message">{message}</p>
          
          <button className="btn-primary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default NotificationModal;
