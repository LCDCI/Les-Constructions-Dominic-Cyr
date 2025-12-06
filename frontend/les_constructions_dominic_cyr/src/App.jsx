import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './components/AppNavBar';
import AppFooter from './components/AppFooter';
import Home from './pages/Home';
import ProjectsPage from './pages/ProjectsPage';
import LotsPage from './pages/LotsPage';
import ContactPage from './pages/ContactPage';
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
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/contact" element={<ContactPage />} />
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