import React, { useState } from 'react';
import ReportGenerator from '../components/Reports/ReportGenerator';
import ReportList from '../components/Reports/ReportList';
import '../styles/Reports/ReportsPage.css';

const ReportsPage = () => {
  const [activeTab, setActiveTab] = useState('generate');

  return (
    <div className="reports-page">
      <div className="page-header">
        <h1>Analytics Reports</h1>
      </div>
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'generate' ? 'active' : ''}`}
          onClick={() => setActiveTab('generate')}
        >
          Generate New
        </button>
        <button
          className={`tab ${activeTab === 'list' ? 'active' : ''}`}
          onClick={() => setActiveTab('list')}
        >
          My Reports
        </button>
      </div>
      <div className="tab-content">
        {activeTab === 'generate' ? <ReportGenerator /> : <ReportList />}
      </div>
    </div>
  );
};
export default ReportsPage;
