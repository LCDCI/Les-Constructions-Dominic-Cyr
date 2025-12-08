import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setNavigate } from '../utils/navigation';

/**
 * Component that sets up navigation for use in axios interceptors
 * Should be rendered inside BrowserRouter
 */
export default function NavigationSetter() {
  const navigate = useNavigate();

  useEffect(() => {
    setNavigate(navigate);
  }, [navigate]);

  return null; // This component doesn't render anything
}
