import React, { useState } from 'react';
import { reportService } from '../../features/reports/reportService';
import '../../styles/Reports/ReportGenerator.css';

const ReportGenerator = ({ onReportGenerated }) => {
    const [formData, setFormData] = useState({
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

        if (!formData.startDate || !formData.endDate) {
            setError("Please select both a start and end date.");
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const formatToBackend = dateStr => {
                if (!dateStr) return null;
                return `${dateStr}T00:00:00`;
            };

            const payload = {
                reportType: 'ANALYTICS_SUMMARY',
                fileFormat: formData.fileFormat,
                startDate: formatToBackend(formData.startDate),
                endDate: formatToBackend(formData.endDate),
            };

            console.log('Generating Master Audit with Payload:', payload);

            await reportService.generateReport(payload);

            setSuccess('Master Audit generated successfully!');

            // Clear dates after success
            setFormData(prev => ({ ...prev, startDate: '', endDate: '' }));

            if (onReportGenerated) {
                onReportGenerated();
            }

        } catch (err) {
            const serverMessage = err.response?.data?.message || 'Failed to generate report. Check Digital Ocean connection.';
            setError(serverMessage);
            console.error('Build 500 Error Details:', err.response);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="report-generator">
            <div className="report-generator-header">
                <h2>Generate Analytics Report</h2>
                <p>Create a 5-page Master Audit with insights from Google Analytics</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit} className="report-form">
                {/* Report Type is now hidden/removed from user selection */}
                <div className="form-group">
                    <label>Selected Report Plan</label>
                    <input
                        type="text"
                        value="Master Analytics Summary (5-Page Audit)"
                        disabled
                        style={{ backgroundColor: '#f4f4f4', cursor: 'not-allowed' }}
                    />
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
                        <option value="PDF">PDF (Recommended)</option>
                        <option value="XLSX">Excel (XLSX Data Raw)</option>
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
                            required
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
                            required
                        />
                    </div>
                </div>

                <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? 'Processing Large Data...' : 'Generate Master Audit'}
                </button>
            </form>
        </div>
    );
};

export default ReportGenerator;