import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './components/AppNavBar';
import AppFooter from './components/AppFooter';
import Home from './pages/Home';
import ProjectsPage from './pages/ProjectsPage';
import HousesPage from './pages/HousesPage';
import LotsPage from './pages/LotsPage';
import ContactPage from './pages/ContactPage';
import ServerError from './pages/ServerError';
import NotFound from './pages/NotFound';
import OwnerDashboard from './pages/OwnerDashboard';
import SalespersonDashboard from './pages/SalespersonDashboard';
import NavigationSetter from './components/NavigationSetter';
import ProjectFilesPage from './pages/ProjectFilesPage';
import ProjectPhotosPage from './pages/ProjectPhotosPage';
import ProjectDocumentsPage from './pages/ProjectDocumentsPage';
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <NavigationSetter />
      <div className="app-container">
        <AppNavBar />
        <main style={{ padding: '16px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/lots" element={<LotsPage />} />
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/houses" element={<HousesPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/error" element={<ServerError />} />
            <Route path="*" element={<NotFound />} />
            <Route path="/owner/dashboard" element={<OwnerDashboard />} />
              <Route path="/salesperson/dashboard" element={<SalespersonDashboard />} />
            <Route path="/projects/:projectId/files" element={<ProjectFilesPage />} /> 
            <Route path="/projects/:projectId/photos" element={<ProjectPhotosPage />} /> 
            <Route path="/projects/:projectId/documents" element={<ProjectDocumentsPage />} /> 
            {/* Remember to add more routes here as App grows */}
          </Routes>
        </main>
        <AppFooter />
      </div>
    </BrowserRouter>
  );
}
