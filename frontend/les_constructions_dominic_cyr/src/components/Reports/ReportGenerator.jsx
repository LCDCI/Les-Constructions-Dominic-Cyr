import React, { useState } from 'react';
import { reportService } from '../../features/reports/reportService';
import '../../styles/Reports/ReportGenerator.css';

const ReportGenerator = () => {
  const [formData, setFormData] = useState({
    reportType: 'ANALYTICS_SUMMARY',
    fileFormat: 'PDF',
    startDate: '',
    endDate: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleChange = e => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Helper to format date as yyyy-MM-ddTHH:mm:ss
      const formatToBackend = dateStr => {
        if (!dateStr) return null;
        const date = new Date(dateStr);
        // This ensures we get YYYY-MM-DDTHH:mm:ss without the .SSSZ
        return date.toISOString().split('.')[0];
      };

      const payload = {
        reportType: formData.reportType,
        fileFormat: formData.fileFormat,
        startDate: formatToBackend(formData.startDate),
        endDate: formatToBackend(formData.endDate),
      };

      console.log('Sending Payload:', payload); // Debug this in your console!

      await reportService.generateReport(payload);
      // ... success logic
    } catch (err) {
      // 400 errors often have a detailed message from Spring Validation
      const serverMessage = err.response?.data?.errors
        ? Object.values(err.response.data.errors).join(', ')
        : err.response?.data?.message;

      setError(serverMessage || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="report-generator">
      <div className="report-generator-header">
        <h2>Generate Analytics Report</h2>
        <p>Create custom reports with insights from Google Analytics</p>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <form onSubmit={handleSubmit} className="report-form">
        <div className="form-group">
          <label htmlFor="reportType">Report Type</label>
          <select
            id="reportType"
            name="reportType"
            value={formData.reportType}
            onChange={handleChange}
            required
          >
            <option value="ANALYTICS_SUMMARY">Analytics Summary</option>
            <option value="USER_BEHAVIOR">User Behavior</option>
            <option value="TRAFFIC_ANALYSIS">Traffic Analysis</option>
            <option value="CONVERSION_METRICS">Conversion Metrics</option>
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="fileFormat">File Format</label>
          <select
            id="fileFormat"
            name="fileFormat"
            value={formData.fileFormat}
            onChange={handleChange}
            required
          >
            <option value="PDF">PDF</option>
            <option value="XLSX">Excel (XLSX)</option>
          </select>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="startDate">Start Date</label>
            <input
              type="date"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
            />
          </div>
          <div className="form-group">
            <label htmlFor="endDate">End Date</label>
            <input
              type="date"
              id="endDate"
              name="endDate"
              value={formData.endDate}
              onChange={handleChange}
            />
          </div>
        </div>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Generating...' : 'Generate Report'}
        </button>
      </form>
    </div>
  );
};
export default ReportGenerator;
