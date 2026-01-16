import React, { useState, useEffect } from 'react';
import { reportService } from '../../features/reports/reportService';
import '../../styles/Reports/ReportList.css';

const ReportList = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [downloading, setDownloading] = useState(null);
    const [deleting, setDeleting] = useState(null); // Track which report is being deleted

    useEffect(() => {
        fetchReports();
    }, [page]);

    const fetchReports = async () => {
        try {
            setLoading(true);
            const response = await reportService.getReports(page, 10);
            setReports(response.content);
            setTotalPages(response.totalPages);
        } catch (err) {
            setError('Failed to load reports');
        } finally {
            setLoading(false);
        }
    };

    const handleDownload = async report => {
        try {
            setDownloading(report.id);
            const response = await reportService.downloadReport(report.id);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute(
                'download',
                `report_${report.id}.${report.fileFormat.toLowerCase()}`
            );
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (err) {
            alert('Failed to download report');
        } finally {
            setDownloading(null);
        }
    };

    const handleDelete = async (reportId) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this report? This action will permanently remove it from the database and cloud storage."
        );

        if (!confirmed) return;

        try {
            setDeleting(reportId);
            await reportService.deleteReport(reportId);

            // Optimistically update UI by removing the deleted report from state
            setReports(prevReports => prevReports.filter(r => r.id !== reportId));

            // If the page becomes empty, go back a page if possible
            if (reports.length === 1 && page > 0) {
                setPage(page - 1);
            }
        } catch (err) {
            alert('Failed to delete report. It may have already been removed.');
            console.error(err);
        } finally {
            setDeleting(null);
        }
    };

    if (loading) return <div className="loading-state">Loading your reports...</div>;
    if (error) return <div className="error-state">{error}</div>;

    return (
        <div className="report-list">
            {reports.length === 0 ? (
                <p className="no-data">No reports found. Generate one to see it here!</p>
            ) : (
                <table>
                    <thead>
                    <tr>
                        <th>Type</th>
                        <th>Format</th>
                        <th>Size</th>
                        <th>Generated</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {reports.map(report => (
                        <tr key={report.id}>
                            <td>{report.reportType.replace(/_/g, ' ')}</td>
                            <td>
                  <span className={`format-badge ${report.fileFormat.toLowerCase()}`}>
                    {report.fileFormat}
                  </span>
                            </td>
                            <td>{(report.fileSize / 1024).toFixed(1)} KB</td>
                            <td>{new Date(report.generationTimestamp).toLocaleString()}</td>
                            <td className="actions-cell">
                                <button
                                    className="download-btn"
                                    disabled={downloading === report.id}
                                    onClick={() => handleDownload(report)}
                                >
                                    {downloading === report.id ? '...' : 'Download'}
                                </button>
                                <button
                                    className="delete-btn"
                                    disabled={deleting === report.id}
                                    onClick={() => handleDelete(report.id)}
                                >
                                    {deleting === report.id ? 'Deleting...' : 'Delete'}
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {totalPages > 1 && (
                <div className="pagination">
                    <button disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
                    <span>Page {page + 1} of {totalPages}</span>
                    <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Next</button>
                </div>
            )}
        </div>
    );
};

export default ReportList;