import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './layouts/AppNavBar';
import Home from './pages/Home';
import LotsPage from './pages/LotsPage';
import Error from './pages/ErrorPage';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <AppNavBar />
      <main style={{ padding: '16px' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/lots" element={<LotsPage />} />
          <Route path="/error" element={<Error />} />
          <Route path="/lots" element={<LotsPage />} />
          <Route path="*" element={<NotFound />} />
          {/* Remember to add more routes here as App grows */}
        </Routes>
      </main>
    </BrowserRouter>
  );
}
