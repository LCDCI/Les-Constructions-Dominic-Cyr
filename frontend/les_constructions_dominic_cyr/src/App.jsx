<<<<<<< HEAD
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './layouts/AppNavBar';
import AppFooter from './layouts/AppFooter';
=======
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
>>>>>>> 9cf03c10c020d92107af44a77391ae5ea4b4f06c
import Home from './pages/Home';
import ProjectsPage from './pages/ProjectsPage';
import LotsPage from './pages/LotsPage';

import ServerError from './pages/ServerError';
import NotFound from './pages/NotFound';
import NavigationSetter from './components/NavigationSetter';
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
            <Route path="/error" element={<ServerError />} />
            <Route path="*" element={<NotFound />} />
            {/* Remember to add more routes here as App grows */}
          </Routes>
        </main>
        <AppFooter />
      </div>
    </BrowserRouter>
  );

}

export default App;