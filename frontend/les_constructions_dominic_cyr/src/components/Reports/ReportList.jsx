import React, { useState, useEffect } from 'react';
import { reportService } from '../../features/reports/reportService';
import DeleteConfirmationModal from '../../components/Reports/DeleteConfirmationModal';
import '../../styles/Reports/ReportList.css';

const ReportList = ({ refreshTrigger }) => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [deleting, setDeleting] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [reportToDelete, setReportToDelete] = useState(null);

    useEffect(() => {
        fetchReports();
    }, [page, refreshTrigger]);

    const fetchReports = async () => {
        try {
            setLoading(true);
            const response = await reportService.getReports(page, 10);
            setReports(response.content);
            setTotalPages(response.totalPages);
        } catch (err) {
            setError('Failed to load reports history');
        } finally {
            setLoading(false);
        }
    };

    const handleDownload = async (reportId, format) => {
        try {
            const response = await reportService.downloadReport(reportId);

            // Standard blob download logic
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;

            const extension = format.toLowerCase();
            link.setAttribute('download', `Audit_Report_${new Date().getTime()}.${extension}`);

            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Download failed:', err);
            alert('Failed to download the file. Please check if the file service is running.');
        }
    };

    const handleConfirmDelete = async () => {
        if (!reportToDelete) return;
        const reportId = reportToDelete.id;

        try {
            setDeleting(reportId);
            setIsModalOpen(false);

            await reportService.deleteReport(reportId);

            setReports(prev => prev.filter(r => r.id !== reportId));

            if (reports.length === 1 && page > 0) setPage(page - 1);

        } catch (err) {
            alert('Error: The server could not delete this report. This usually happens if the file service is unreachable.');
            fetchReports(); // Refresh to sync state
        } finally {
            setDeleting(null);
            setReportToDelete(null);
        }
    };

    if (loading && page === 0) return <div className="loading-state">Loading reports...</div>;

    return (
        <div className="report-list">
            {error && <div className="error-message">{error}</div>}
            {reports.length === 0 ? (
                <p className="no-data">No reports found.</p>
            ) : (
                <table>
                    <thead>
                    <tr>
                        <th>Format</th>
                        <th>Size</th>
                        <th>Generated Date</th>
                        <th style={{ textAlign: 'right' }}>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {reports.map(report => (
                        <tr key={report.id}>
                            <td>
                                <span className={`format-badge ${report.fileFormat.toLowerCase()}`}>
                                    {report.fileFormat}
                                </span>
                            </td>
                            <td>{(report.fileSize / 1024).toFixed(1)} KB</td>
                            <td>{new Date(report.generationTimestamp).toLocaleDateString()}</td>
                            <td className="actions-cell" style={{ textAlign: 'right' }}>
                                {/* DOWNLOAD BUTTON ADDED HERE */}
                                <button
                                    className="download-btn"
                                    onClick={() => handleDownload(report.id, report.fileFormat)}
                                    style={{ marginRight: '10px' }}
                                >
                                    Download
                                </button>

                                <button
                                    className="delete-btn"
                                    disabled={deleting === report.id}
                                    onClick={() => {
                                        setReportToDelete(report);
                                        setIsModalOpen(true);
                                    }}
                                >
                                    {deleting === report.id ? '...' : 'Delete'}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            <DeleteConfirmationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onConfirm={handleConfirmDelete}
                reportName={reportToDelete ? `Audit - ${new Date(reportToDelete.generationTimestamp).toLocaleDateString()}` : ''}
            />
        </div>
    );
};

export default ReportList;