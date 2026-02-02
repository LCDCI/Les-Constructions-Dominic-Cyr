import React, { useState } from 'react';
import ReportGenerator from '../components/Reports/ReportGenerator';
import ReportList from '../components/Reports/ReportList';
import '../styles/Reports/ReportsPage.css';
import { usePageTranslations } from '../hooks/usePageTranslations';

const ReportsPage = () => {
  const { t } = usePageTranslations('reportsPage');
  const [activeTab, setActiveTab] = useState('generate');

  return (
    <div className="reports-page">
      <div className="page-header">
        <h1>{t('title', 'Analytics Reports')}</h1>
      </div>
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'generate' ? 'active' : ''}`}
          onClick={() => setActiveTab('generate')}
        >
          {t('tabs.generate', 'Generate New')}
        </button>
        <button
          className={`tab ${activeTab === 'list' ? 'active' : ''}`}
          onClick={() => setActiveTab('list')}
        >
          {t('tabs.myReports', 'My Reports')}
        </button>
      </div>
      <div className="tab-content">
        {activeTab === 'generate' ? <ReportGenerator /> : <ReportList />}
      </div>
    </div>
  );
};
export default ReportsPage;
