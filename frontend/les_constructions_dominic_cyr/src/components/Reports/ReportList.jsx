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

  if (loading) return <div>Loading...</div>;

  return (
    <div className="report-list">
      <table>
        <thead>
          <tr>
            <th>Type</th>
            <th>Format</th>
            <th>Generated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {reports.map(report => (
            <tr key={report.id}>
              <td>{report.reportType}</td>
              <td>{report.fileFormat}</td>
              <td>{new Date(report.generationTimestamp).toLocaleString()}</td>
              <td>
                <button onClick={() => handleDownload(report)}>Download</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
export default ReportList;
