export const API_BASE_URL =
  process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

export const ROUTES = {
  OWNER_DASHBOARD: '/owner/dashboard',
  OWNER_UPLOADS: '/owner/uploads',
  OWNER_REPORTS: '/owner/reports',
  OWNER_PROJECTS: '/owner/projects',
  OWNER_INBOX: '/owner/inbox',
  OWNER_BILLING: '/owner/billing',
  OWNER_DOCUMENTS: '/owner/documents',
  OWNER_USERS: '/owner/users',
  OWNER_SETTINGS: '/owner/settings',
  OWNER_MESSAGES: '/owner/messages',
  OWNER_HOME_CONTENT: '/owner/home-content',
  OWNER_FORMS: '/owner/forms',
  OWNER_CONTRACTORS: '/owner/contractors',
  OWNER_SCHEDULES_ALL: '/owner/schedules/all',
};

export const DASHBOARD_FEATURES = [
  {
    icon: '📤',
    title: 'Uploads',
    buttonText: 'Begin Upload',
    route: ROUTES.OWNER_UPLOADS,
  },
  {
    icon: '📊',
    title: 'Analytics & Reports',
    buttonText: 'View Reports',
    route: ROUTES.OWNER_REPORTS,
  },
  {
    icon: '📦',
    title: 'Projects',
    buttonText: 'View Projects',
    route: ROUTES.OWNER_PROJECTS,
  },
  {
    icon: '📨',
    title: 'Inbox',
    buttonText: 'View Inbox',
    route: ROUTES.OWNER_INBOX,
  },
  {
    icon: '💵',
    title: 'Billing',
    buttonText: 'View Billing',
    route: ROUTES.OWNER_BILLING,
  },
  {
    icon: '📄',
    title: 'Documents',
    buttonText: 'View Documents',
    route: ROUTES.OWNER_DOCUMENTS,
  },
  {
    icon: '👥',
    title: 'Users',
    buttonText: 'View Users',
    route: ROUTES.OWNER_USERS,
  },
  {
    icon: '⚙️',
    title: 'Settings',
    buttonText: 'View Settings',
    route: ROUTES.OWNER_SETTINGS,
  },
  {
    icon: '✈️',
    title: 'Send Messages',
    buttonText: 'Send Message',
    route: ROUTES.OWNER_MESSAGES,
  },
  {
    icon: '🏠',
    title: 'Home Content',
    buttonText: 'Edit Home',
    route: ROUTES.OWNER_HOME_CONTENT,
  },
  {
    icon: '📋',
    title: 'Create Forms',
    buttonText: 'Create Form',
    route: ROUTES.OWNER_FORMS,
  },
  {
    icon: '👷',
    title: 'Add Contractors & Salesmen',
    buttonText: 'Add',
    route: ROUTES.OWNER_CONTRACTORS,
  },
];
