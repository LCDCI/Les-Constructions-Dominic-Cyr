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
import ProjectMetadata from './pages/ProjectMetadata';
import ProjectsOverviewPage from './pages/ProjectsOverviewPage';
import ResidentialProjectsPage from './pages/ResidentialProjectsPage';
import RenovationsPage from './pages/RenovationsPage';
import ProjectManagementPage from './pages/ProjectManagementPage';
import ProfilePage from './pages/ProfilePage';
import Unauthorized from './pages/Unauthorized';
import ProtectedRoute from './components/ProtectedRoute';
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
            <Route
              path="/projects"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectsPage />}
                />
              }
            />
            <Route
              path="/projects/create"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<CreateProjectPage />}
                />
              }
            />
            <Route path="/houses" element={<HousesPage />} />
            <Route path="/renovations" element={<RenovationsPage />} />
            <Route path="/projectmanagement" element={<ProjectManagementPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/error" element={<ServerError />} />
            <Route
              path="/inquiries"
              element={<ProtectedRoute allowedRoles={[ 'OWNER' ]} element={<OwnerInquiriesPage />} />}
            />
            <Route
              path="/users"
              element={<ProtectedRoute allowedRoles={[ 'OWNER' ]} element={<UsersPage />} />}
            />
            <Route
              path="/owner/dashboard"
              element={<ProtectedRoute allowedRoles={[ 'OWNER' ]} element={<OwnerDashboard />} />}
            />
            <Route
              path="/projects/:projectId/metadata"
              element={<ProjectMetadata />}
            />
            <Route
              path="/customer/dashboard"
              element={<ProtectedRoute allowedRoles={[ 'CUSTOMER' ]} element={<CustomerDashboard />} />}
            />
            <Route
              path="/salesperson/dashboard"
              element={<ProtectedRoute allowedRoles={[ 'SALESPERSON' ]} element={<SalespersonDashboard />} />}
            />
            <Route
              path="/residential-projects"
              element={<ResidentialProjectsPage />}
            />
            <Route
              path="/contractor/dashboard"
              element={<ProtectedRoute allowedRoles={[ 'CONTRACTOR' ]} element={<ContractorDashboard />} />}
            />
            <Route
              path="/projects/:projectId/files"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER']}
                  element={<ProjectFilesPage />}
                />
              }
            />
            <Route
              path="/projects/:projectId/photos"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER']}
                  element={<ProjectPhotosPage />}
                />
              }
            />
            <Route path="/portal/login" element={<PortalLogin />} />
            <Route 
              path="/profile" 
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER']}
                  element={<ProfilePage />}
                />
              } 
            />
            <Route path="/unauthorized" element={<Unauthorized />} />
            <Route path="*" element={<NotFound />} />
            <Route
              path="/projects/:projectIdentifier/overview"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'SALESPERSON', 'CONTRACTOR', 'CUSTOMER']}
                  element={<ProjectsOverviewPage />}
                />
              }
            />
          </Routes>
        </main>
        <AppFooter />
      </div>
    </BrowserRouter>
  );
}
