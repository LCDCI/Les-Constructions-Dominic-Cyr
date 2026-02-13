import { useEffect, useState, useRef } from 'react';
import {
  BrowserRouter,
  Routes,
  Route,
  useLocation,
  matchPath,
} from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import AppNavBar from './components/NavBars/AppNavBar';
import Home from './pages/Public_Facing/Home';
import LotsPage from './pages/Public_Facing/LotsPage';
import LivingEnvironmentPage from './pages/Public_Facing/LivingEnvironmentPage';
import OwnerLotsPage from './pages/Project/OwnerLotsPage';
import ProjectsPage from './pages/Project/ProjectsPage';
import CreateProjectPage from './pages/Project/CreateProjectPage';
import RealizationsPage from './pages/Public_Facing/RealizationsPage';
import RenovationsPage from './pages/Public_Facing/RenovationsPage';
import ProjectManagementPage from './pages/Project/ProjectManagementPage';
import ContactPage from './pages/Public_Facing/ContactPage';
import ServerError from './pages/Errors/ServerError';
import OwnerInquiriesPage from './pages/OwnerInquiriesPage';
import UsersPage from './pages/UsersPage';
import OwnerDashboard from './pages/Dashboards/OwnerDashboard';
import ProjectMetadata from './pages/Project/ProjectMetadata';
import ProjectEntryRouter from './pages/Project/ProjectEntryRouter';
import LotSelectPage from './pages/Project/LotSelectPage';
import LotMetadata from './pages/Project/LotMetadata';
import CustomerDashboard from './pages/Dashboards/CustomerDashboard';
import SalespersonDashboard from './pages/Dashboards/SalespersonDashboard';
import ResidentialProjectsPage from './pages/Public_Facing/ResidentialProjectsPage';
import ContractorDashboard from './pages/Dashboards/ContractorDashboard';
import LotDocumentsPage from './features/lots/components/LotDocumentsPage';
import LotsListDashboard from './features/lots/components/LotsListDashboard';
import ProjectFilesPage from './pages/Project/ProjectFilesPage';
import ProjectPhotosPage from './pages/Project/ProjectPhotosPage';
import ProjectSchedulePage from './pages/Project/ProjectSchedulePage';
import PortalLogin from './pages/PortalLogin';
import TaskDetailsPage from './pages/Tasks/TaskDetailsPage';
import ContractorTasksPage from './pages/Tasks/ContractorTasksPage';
import ProfilePage from './pages/ProfilePage';
import QuoteListPage from './pages/Quotes/QuoteListPage';
import QuoteFormPage from './pages/Quotes/QuoteFormPage';
import QuoteDetailPage from './pages/Quotes/QuoteDetailPage';
import Unauthorized from './pages/Errors/Unauthorized';
import NotFound from './pages/Errors/NotFound';
import ProjectsOverviewPage from './pages/Project/ProjectsOverviewPage';
import ProtectedRoute from './components/ProtectedRoute';
import HomeFooter from './components/Footers/HomeFooter';
import NavigationSetter from './components/NavigationSetter';
import IdleTimeoutModal from './components/Modals/IdleTimeoutModal';
import ReportsPage from './pages/ReportsPage';
import InboxPage from './pages/Inbox/InboxPage';
import SalespersonFormsPage from './pages/Forms/SalespersonFormsPage';
import CustomerFormsPage from './pages/Forms/CustomerFormsPage';
import CustomerFormsSelectionPage from './pages/Forms/CustomerFormsSelectionPage';
import ReactGA from 'react-ga4';
// import { loadTheme } from './utils/themeLoader';
import { setupAxiosInterceptors } from './utils/axios';
import { clearAppSession } from './features/users/api/clearAppSession';
import useBackendUser from './hooks/useBackendUser';

function PageViewTracker() {
  const location = useLocation();
  useEffect(() => {
    ReactGA.send({ hitType: 'pageview', page: location.pathname });
  }, [location]);
  return null;
}

function ContractorLotsDocuments() {
  const { profile, loading } = useBackendUser();

  if (loading || !profile) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>Loading...</div>
    );
  }

  return <LotsListDashboard userId={profile.userId} />;
}

function ConditionalFooter() {
  const location = useLocation();

  const isProjectPage =
    matchPath('/projects/:projectIdentifier/overview', location.pathname) ||
    matchPath('/projects/:projectIdentifier/lots', location.pathname);

  const isContactPage = location.pathname === '/contact';
  const isPortalLoginPage = location.pathname === '/portal/login';

  if (isProjectPage || isContactPage || isPortalLoginPage) {
    return null;
  }

  return <HomeFooter />;
}

export default function App() {
  const { getAccessTokenSilently, isAuthenticated, logout } = useAuth0();

  const [showIdleModal, setShowIdleModal] = useState(false);
  const showIdleModalRef = useRef(false);

  const setShowIdleModalSafe = v => {
    showIdleModalRef.current = v;
    setShowIdleModal(v);
  };
  const [remainingSeconds, setRemainingSeconds] = useState(120);

  const idleTimerRef = useRef(null);
  const countdownTimerRef = useRef(null);
  const resetIdleTimerRef = useRef(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const errorDescription = params.get('error_description');

    if (
      error === 'access_denied' &&
      errorDescription === 'verification_pending'
    ) {
      window.history.replaceState(
        {},
        document.title,
        window.location.pathname + '#/portal/login'
      );

      // Optional: If you have a global toast/notification state, trigger it here.
      // Otherwise, the PortalLogin page will handle the "blocked" state.
    }
  }, []);
  /* ----------------------------------
     Axios + Auth0 Interceptor
  -----------------------------------*/
  useEffect(() => {
    if (!isAuthenticated) return;

    setupAxiosInterceptors(getAccessTokenSilently, () => {
      try {
        clearAppSession();
      } catch (e) {
        // exception needed
      }

      logout({
        logoutParams: {
          returnTo: window.location.origin + '/portal/login',
        },
      });
    });
  }, [isAuthenticated, getAccessTokenSilently, logout]);

  useEffect(() => {
    const measurementId = import.meta.env.VITE_GA_MEASUREMENT_ID;
    if (measurementId) {
      ReactGA.initialize(measurementId);
    }
  }, []);

  /* ----------------------------------
     Idle Timeout Logic
  -----------------------------------*/
  useEffect(() => {
    if (!isAuthenticated) return;

    const idleMinutes = parseInt(
      import.meta.env.VITE_SESSION_IDLE_MINUTES || '30',
      10
    );

    const timeoutMs = Math.max(1, idleMinutes) * 60 * 1000;
    const warningDurationSec = 120;

    const clearTimers = () => {
      if (idleTimerRef.current) {
        clearTimeout(idleTimerRef.current);
        idleTimerRef.current = null;
      }
      if (countdownTimerRef.current) {
        clearInterval(countdownTimerRef.current);
        countdownTimerRef.current = null;
      }
    };

    const performLogout = () => {
      clearTimers();
      setShowIdleModal(false);
      showIdleModalRef.current = false;
      clearAppSession();

      // Perform local-only logout and redirect immediately to avoid showing Auth0 error pages
      try {
        window.location.assign(window.location.origin + '/portal/login');
      } catch (e) {
        // last-resort
        window.location.href = window.location.origin + '/portal/login';
      }
    };

    const startCountdown = () => {
      setRemainingSeconds(warningDurationSec);

      countdownTimerRef.current = setInterval(() => {
        setRemainingSeconds(s => {
          if (s <= 1) {
            clearInterval(countdownTimerRef.current);
            countdownTimerRef.current = null;
            performLogout();
            return 0;
          }
          return s - 1;
        });
      }, 1000);
    };

    const onIdle = () => {
      setShowIdleModalSafe(true);
      startCountdown();
    };

    const resetIdleTimer = () => {
      // If modal is visible, don't close it on incidental activity — keep the countdown running.
      if (showIdleModalRef.current) {
        return;
      }
      if (idleTimerRef.current) clearTimeout(idleTimerRef.current);
      idleTimerRef.current = setTimeout(onIdle, timeoutMs);
    };

    // Expose reset function so outside handlers (like Stay button) can reset timers
    resetIdleTimerRef.current = resetIdleTimer;

    const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'click'];
    events.forEach(ev => window.addEventListener(ev, resetIdleTimer));

    resetIdleTimer();

    // Dev helper: support ?forceIdle=true to immediately show the idle modal for testing
    try {
      const params = new URLSearchParams(window.location.search);
      if (params.get('forceIdle') === 'true') {
        onIdle();
      }
    } catch (e) {
      /* ignore */
    }

    return () => {
      clearTimers();
      events.forEach(ev => window.removeEventListener(ev, resetIdleTimer));
    };
  }, [isAuthenticated, logout]);

  /* ----------------------------------
     Render
  -----------------------------------*/
  return (
    <BrowserRouter>
      <NavigationSetter />
      <PageViewTracker />
      <div className="app-container">
        <AppNavBar />

        <main style={{ padding: '16px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
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
            <Route
              path="/reports"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<ReportsPage />}
                />
              }
            />
            <Route path="/realizations" element={<RealizationsPage />} />
            <Route path="/renovations" element={<RenovationsPage />} />
            <Route
              path="/projectmanagement"
              element={<ProjectManagementPage />}
            />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/error" element={<ServerError />} />

            <Route
              path="/inquiries"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<OwnerInquiriesPage />}
                />
              }
            />
            <Route
              path="/users"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<UsersPage />}
                />
              }
            />
            <Route
              path="/owner/dashboard"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<OwnerDashboard />}
                />
              }
            />

            <Route
              path="/projects/:projectId/metadata"
              element={<ProjectMetadata />}
            />
            {/* Specific project routes must come before the generic /projects/:projectIdentifier route */}
            <Route
              path="/projects/:projectId/files"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectFilesPage />}
                />
              }
            />
            <Route
              path="/projects/:projectId/photos"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectPhotosPage />}
                />
              }
            />
            <Route
              path="/projects/:projectId/schedule"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectSchedulePage />}
                />
              }
            />
            <Route
              path="/projects/:projectId/lots/:lotId/metadata"
              element={<LotMetadata />}
            />
            <Route
              path="/projects/:projectIdentifier/lots/select"
              element={<LotSelectPage />}
            />
            {/* Generic project route must come last to avoid catching specific routes */}
            <Route
              path="/projects/:projectIdentifier"
              element={<ProjectEntryRouter />}
            />

            {/* Project team management removed */}

            <Route
              path="/customer/dashboard"
              element={
                <ProtectedRoute
                  allowedRoles={['CUSTOMER']}
                  element={<CustomerDashboard />}
                />
              }
            />

            <Route
              path="/salesperson/dashboard"
              element={
                <ProtectedRoute
                  allowedRoles={['SALESPERSON']}
                  element={<SalespersonDashboard />}
                />
              }
            />

            <Route
              path="/residential-projects"
              element={<ResidentialProjectsPage />}
            />

            <Route
              path="/contractor/dashboard"
              element={
                <ProtectedRoute
                  allowedRoles={['CONTRACTOR']}
                  element={<ContractorDashboard />}
                />
              }
            />

            {/* Inbox routes */}
            <Route
              path="/inbox"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<InboxPage />}
                />
              }
            />
            <Route
              path="/customers/inbox"
              element={
                <ProtectedRoute
                  allowedRoles={['CUSTOMER']}
                  element={<InboxPage />}
                />
              }
            />
            <Route
              path="/owner/inbox"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<InboxPage />}
                />
              }
            />
            <Route
              path="/contractors/inbox"
              element={
                <ProtectedRoute
                  allowedRoles={['CONTRACTOR']}
                  element={<InboxPage />}
                />
              }
            />
            <Route
              path="/salesperson/inbox"
              element={
                <ProtectedRoute
                  allowedRoles={['SALESPERSON']}
                  element={<InboxPage />}
                />
              }
            />

            {/* Forms routes */}
            <Route
              path="/salesperson/forms"
              element={
                <ProtectedRoute
                  allowedRoles={['SALESPERSON', 'OWNER']}
                  element={<SalespersonFormsPage />}
                />
              }
            />
            <Route
              path="/customers/forms"
              element={
                <ProtectedRoute
                  allowedRoles={['CUSTOMER']}
                  element={<CustomerFormsSelectionPage />}
                />
              }
            />
            <Route
              path="/projects/:projectId/lots/:lotId/forms"
              element={
                <ProtectedRoute
                  allowedRoles={['CUSTOMER']}
                  element={<CustomerFormsPage />}
                />
              }
            />

            <Route
              path="/contractors/documents"
              element={
                <ProtectedRoute
                  allowedRoles={['CONTRACTOR', 'OWNER']}
                  element={<ContractorLotsDocuments />}
                />
              }
            />

            <Route
              path="/owner/documents"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<ContractorLotsDocuments />}
                />
              }
            />

            <Route
              path="/lots"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR']}
                  element={<ContractorLotsDocuments />}
                />
              }
            />

            <Route
              path="/dashboard/lots/:lotId/documents"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR']}
                  element={<LotDocumentsPage />}
                />
              }
            />

            {/* Project-based lot documents route */}
            <Route
              path="/projects/:projectIdentifier/lots/:lotId/documents"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR', 'CUSTOMER']}
                  element={<LotDocumentsPage />}
                />
              }
            />

            <Route
              path="/quotes"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR']}
                  element={<QuoteListPage />}
                />
              }
            />

            <Route
              path="/quotes/create"
              element={
                <ProtectedRoute
                  allowedRoles={['CONTRACTOR']}
                  element={<QuoteFormPage />}
                />
              }
            />

            <Route
              path="/quotes/:quoteNumber"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR', 'CUSTOMER']}
                  element={<QuoteDetailPage />}
                />
              }
            />

            <Route
              path="/projects/:projectId/files"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectFilesPage />}
                />
              }
            />

            <Route
              path="/projects/:projectId/photos"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectPhotosPage />}
                />
              }
            />

            <Route
              path="/projects/:projectId/schedule"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProjectSchedulePage />}
                />
              }
            />

            <Route
              path="/tasks/:taskId"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<TaskDetailsPage />}
                />
              }
            />

            <Route
              path="/contractor/tasks"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER', 'CONTRACTOR']}
                  element={<ContractorTasksPage />}
                />
              }
            />

            <Route path="/portal/login" element={<PortalLogin />} />

            <Route
              path="/profile"
              element={
                <ProtectedRoute
                  allowedRoles={[
                    'OWNER',
                    'SALESPERSON',
                    'CONTRACTOR',
                    'CUSTOMER',
                  ]}
                  element={<ProfilePage />}
                />
              }
            />

            <Route path="/unauthorized" element={<Unauthorized />} />
            <Route
              path="/projects/:projectIdentifier/overview"
              element={<ProjectsOverviewPage />}
            />
            <Route
              path="/projects/:projectIdentifier/living-environment"
              element={<LivingEnvironmentPage />}
            />
            <Route
              path="/projects/:projectIdentifier/lots"
              element={<LotsPage />}
            />
            <Route
              path="/projects/:projectIdentifier/manage-lots"
              element={
                <ProtectedRoute
                  allowedRoles={['OWNER']}
                  element={<OwnerLotsPage />}
                />
              }
            />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>

        <ConditionalFooter />

        {showIdleModal && (
          <IdleTimeoutModal
            remainingSeconds={remainingSeconds}
            onStay={() => {
              // user stays: close modal and reset idle timer
              setShowIdleModalSafe(false);
              setRemainingSeconds(120);
              try {
                resetIdleTimerRef.current && resetIdleTimerRef.current();
              } catch (e) {
                //exception needed
              }
            }}
            onLogout={() => {
              // Local-only logout: clear app session and navigate to homepage
              clearAppSession();
              try {
                window.location.assign(window.location.origin + '/');
              } catch (e) {
                window.location.href = window.location.origin + '/';
              }
            }}
          />
        )}
      </div>
    </BrowserRouter>
  );
}
