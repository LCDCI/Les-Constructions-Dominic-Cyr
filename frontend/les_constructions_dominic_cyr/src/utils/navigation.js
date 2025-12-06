/**
 * Navigation utility for use outside React components
 * Allows axios interceptors to navigate using React Router
 */

let navigateRef = null;

/**
 * Set the navigate function from React Router
 * Should be called from within a component that has access to useNavigate()
 * @param {Function} navigate - The navigate function from useNavigate()
 */
export const setNavigate = (navigate) => {
  navigateRef = navigate;
};

/**
 * Navigate to a route
 * @param {string} path - The path to navigate to
 * @param {object} options - Navigation options (state, replace, etc.)
 */
export const navigate = (path, options = {}) => {
  if (navigateRef) {
    navigateRef(path, options);
  } else {
    // Fallback to window.location if navigate is not set
    console.warn('Navigation not initialized, using window.location');
    window.location.href = path;
  }
};

