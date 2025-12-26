import { useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './components/NavBars/AppNavBar';
import Home from './pages/Public_Facing/Home';
import LotsPage from './pages/LotsPage';
import ProjectsPage from './pages/Project/ProjectsPage';
import CreateProjectPage from './pages/Project/CreateProjectPage';
import HousesPage from './pages/Public_Facing/HousesPage';
import RenovationsPage from './pages/Public_Facing/RenovationsPage';
import ProjectManagementPage from './pages/Project/ProjectManagementPage';
import ContactPage from './pages/Public_Facing/ContactPage';
import ServerError from './pages/Errors/ServerError';
import OwnerInquiriesPage from './pages/OwnerInquiriesPage';
import UsersPage from './pages/UsersPage';
import OwnerDashboard from './pages/Dashboards/OwnerDashboard';
import ProjectMetadata from './pages/Project/ProjectMetadata';
import CustomerDashboard from './pages/Dashboards/CustomerDashboard';
import SalespersonDashboard from './pages/Dashboards/SalespersonDashboard';
import ResidentialProjectsPage from './pages/Public_Facing/ResidentialProjectsPage';
import ContractorDashboard from './pages/Dashboards/ContractorDashboard';
import ProjectFilesPage from './pages/Project/ProjectFilesPage';
import ProjectPhotosPage from './pages/Project/ProjectPhotosPage';
import PortalLogin from './pages/PortalLogin';
import ProfilePage from './pages/ProfilePage';
import Unauthorized from './pages/Errors/Unauthorized';
import NotFound from './pages/Errors/NotFound';
import ProjectsOverviewPage from './pages/Project/ProjectsOverviewPage';
import ProtectedRoute from './components/ProtectedRoute';
import HomeFooter from './components/Footers/HomeFooter';
import NavigationSetter from './components/NavigationSetter';
import { loadTheme } from './utils/themeLoader';


export default function App() {

  useEffect(() => {
  loadTheme();
}, []);


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
        <HomeFooter />
      </div>
    </BrowserRouter>
  );
}