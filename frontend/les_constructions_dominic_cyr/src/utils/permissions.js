/**
 * Role-Based Access Control (RBAC) Permissions Utility
 * 
 * OWNER: Full system access - can do everything
 * SALESPERSON: Project visibility + client interaction, upload/view documents for assigned projects
 * CONTRACTOR: Task execution - upload documents/photos, view project docs, cannot manage users
 * CUSTOMER: Read-only on own projects, view shared documents, limited upload capability
 */

export const ROLES = {
  OWNER: 'OWNER',
  SALESPERSON: 'SALESPERSON',
  CONTRACTOR: 'CONTRACTOR',
  CUSTOMER: 'CUSTOMER',
  VISITOR: 'VISITOR',
};

/**
 * Permission definitions by feature
 */
const permissions = {
  // User management
  users: {
    create: [ROLES.OWNER],
    view: [ROLES.OWNER],
    update: [ROLES.OWNER],
    delete: [ROLES.OWNER],
    assignRoles: [ROLES.OWNER],
  },

  // Project management
  projects: {
    create: [ROLES.OWNER],
    viewAll: [ROLES.OWNER],
    viewAssigned: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER],
    update: [ROLES.OWNER],
    delete: [ROLES.OWNER],
    viewPublic: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER, ROLES.VISITOR],
  },

  // Documents/Files
  documents: {
    upload: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER],
    view: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER],
    delete: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR], // CUSTOMER cannot delete
    viewInternal: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR], // Internal docs not for customers
  },

  // Photos - Customers cannot upload photos (contractors document their work)
  photos: {
    upload: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR],
    view: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER],
    delete: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR], // CUSTOMER cannot delete
  },

  // Schedules
  schedules: {
    view: [ROLES.OWNER, ROLES.SALESPERSON, ROLES.CONTRACTOR, ROLES.CUSTOMER],
    create: [ROLES.OWNER],
    update: [ROLES.OWNER, ROLES.CONTRACTOR], // Contractor can mark tasks complete
    delete: [ROLES.OWNER],
  },

  // Admin pages
  admin: {
    dashboard: [ROLES.OWNER],
    inquiries: [ROLES.OWNER],
    settings: [ROLES.OWNER],
  },

  // Dashboards
  dashboards: {
    owner: [ROLES.OWNER],
    salesperson: [ROLES.SALESPERSON],
    contractor: [ROLES.CONTRACTOR],
    customer: [ROLES.CUSTOMER],
  },
};

/**
 * Check if a role has permission for a specific action on a feature
 * @param {string} role - The user's role
 * @param {string} feature - The feature name (e.g., 'documents', 'photos')
 * @param {string} action - The action (e.g., 'upload', 'delete', 'view')
 * @returns {boolean}
 */
export function hasPermission(role, feature, action) {
  if (!role){
      role = ROLES.VISITOR;
  };
  
  const featurePermissions = permissions[feature];
  if (!featurePermissions) return false;
  
  const allowedRoles = featurePermissions[action];
  if (!allowedRoles) return false;
  
  return allowedRoles.includes(role);
}

/**
 * Check if user can upload documents
 * @param {string} role 
 * @returns {boolean}
 */
export function canUploadDocuments(role) {
  return hasPermission(role, 'documents', 'upload');
}

/**
 * Check if user can delete documents
 * @param {string} role 
 * @returns {boolean}
 */
export function canDeleteDocuments(role) {
  return hasPermission(role, 'documents', 'delete');
}

/**
 * Check if user can upload photos
 * @param {string} role 
 * @returns {boolean}
 */
export function canUploadPhotos(role) {
  return hasPermission(role, 'photos', 'upload');
}

/**
 * Check if user can delete photos
 * @param {string} role 
 * @returns {boolean}
 */
export function canDeletePhotos(role) {
  return hasPermission(role, 'photos', 'delete');
}

/**
 * Check if user can manage users
 * @param {string} role 
 * @returns {boolean}
 */
export function canManageUsers(role) {
  return hasPermission(role, 'users', 'create');
}

/**
 * Check if user can access admin features
 * @param {string} role 
 * @returns {boolean}
 */
export function canAccessAdmin(role) {
  return hasPermission(role, 'admin', 'dashboard');
}

/**
 * Check if user can create projects
 * @param {string} role 
 * @returns {boolean}
 */
export function canCreateProjects(role) {
  return hasPermission(role, 'projects', 'create');
}

/**
 * Check if user can view all projects (not just assigned)
 * @param {string} role 
 * @returns {boolean}
 */
export function canViewAllProjects(role) {
  return hasPermission(role, 'projects', 'viewAll');
}

/**
 * Check if user can modify schedules
 * @param {string} role 
 * @returns {boolean}
 */
export function canModifySchedules(role) {
  return hasPermission(role, 'schedules', 'update');
}

export function canViewProjectOverview(role) {
    return hasPermission(role, 'projects', 'viewPublic');
}

export default permissions;
