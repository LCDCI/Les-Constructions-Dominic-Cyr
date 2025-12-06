import React from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import '../styles/home.css';

export default function Home() {
  return (
    <div className="home-page">
      <Navbar />

      <div className="home-content">
        <div className="home-container">
          <h1>Welcome</h1>
          <p>
            This is the home page. Use the navigation to go to the Lots page.
          </p>
        </div>
      </div>

      <Footer />
    </div>
  );
}
