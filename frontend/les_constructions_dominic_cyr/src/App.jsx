import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './components/AppNavBar';
import AppFooter from './components/AppFooter';
import CustomerDashboard from './pages/CustomerDashboard';
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
import OwnerInquiriesPage from './pages/OwnerInquiriesPage';
import ProjectFilesPage from './pages/ProjectFilesPage';
import ProjectPhotosPage from './pages/ProjectPhotosPage';
import ContractorDashboard from './pages/ContractorDashboard';
import CreateProjectPage from './pages/CreateProjectPage';
import UsersPage from './pages/UsersPage';
import PortalLogin from './pages/PortalLogin';


import ProjectsOverviewPage from './pages/ProjectsOverviewPage';
import ResidentialProjectsPage from './pages/ResidentialProjectsPage';
import RenovationsPage from './pages/RenovationsPage';
import './App.css';
import './styles/users.css';

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
            <Route path="/projects/create" element={<CreateProjectPage />} />
            <Route path="/houses" element={<HousesPage />} />
            <Route path="/renovations" element={<RenovationsPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/error" element={<ServerError />} />
            <Route path="/inquiries" element={<OwnerInquiriesPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/owner/dashboard" element={<OwnerDashboard />} />
            <Route path="/customer/dashboard" element={<CustomerDashboard />} />
            <Route
              path="/salesperson/dashboard"
              element={<SalespersonDashboard />}
            />
            <Route
              path="/residential-projects"
              element={<ResidentialProjectsPage />}
            />
            <Route
              path="/contractor/dashboard"
              element={<ContractorDashboard />}
            />
            <Route
              path="/projects/:projectId/files"
              element={<ProjectFilesPage />}
            />
            <Route
              path="/projects/:projectId/photos"
              element={<ProjectPhotosPage />}
            />
            <Route path="/portal/login" element={<PortalLogin />} />
            <Route path="*" element={<NotFound />} />
            <Route
              path="/projects/:projectIdentifier/overview"
              element={<ProjectsOverviewPage />}
            />
          </Routes>
        </main>
        <AppFooter />
      </div>
    </BrowserRouter>
  );
}
