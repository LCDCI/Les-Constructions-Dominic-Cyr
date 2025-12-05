import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppNavBar from './layouts/AppNavBar';
import Home from './pages/Home';
import LotsPage from './pages/Lots/LotsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AppNavBar />
      <main style={{ padding: '16px' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/lots" element={<LotsPage />} />
          {/* Remember to add more routes here as App grows */}
        </Routes>
      </main>
    </BrowserRouter>
  );
}
