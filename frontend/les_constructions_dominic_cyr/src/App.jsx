import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import ProjectsPage from './pages/ProjectsPage';
import LotsPage from './pages/LotsPage';

import ServerError from './pages/ServerError';
import NotFound from './pages/NotFound';
import NavigationSetter from './components/NavigationSetter';

export default function App() {
  return (
    <BrowserRouter>
      <NavigationSetter />
      <AppNavBar />
      <main style={{ padding: '16px' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/lots" element={<LotsPage />} />
          <Route path="/error" element={<ServerError />} />
          <Route path="*" element={<NotFound />} />
          {/* Remember to add more routes here as App grows */}
        </Routes>
      </main>
    </BrowserRouter>
  );

}

export default App;