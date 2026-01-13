import React, { useEffect, useMemo } from 'react';
import '../../styles/Public_Facing/home.css';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';

export default function Home() {
  const { t } = usePageTranslations('home');
  const filesServiceUrl = import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

  const photos = useMemo(
    () => ({
      hero: '2186d36c-4dc7-400b-8d9e-824a5b06f7ba',
      think: '1634e9ee-2680-41d1-b28a-47353f842d9c',
      build: '1659ff85-b160-4111-b419-84834eb4375a',
      live: '1681b3d5-8f0a-4daf-9590-53a1ce37cf20',
      collage1: '48f50cea-f368-41d6-91c3-ae55157bd868',
      collage2: '55378cf7-c0a0-48be-b5f9-9d2507eff177',
      collage3: '610354b2-8a9c-4e87-95a1-3cc63f494c6e',
      project2: 'bb6dd250-ed32-4041-8b4e-020e2ef45e2f',
      contact: 'dff685b4-05a5-443a-9c84-848fa9dbd905',
    }),
    []
  );

  useEffect(() => {
    const elements = Array.from(document.querySelectorAll('[data-animate]'));
    if (!elements.length) return;

    const prefersReduced =
      typeof window !== 'undefined' &&
      window.matchMedia &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReduced) {
      elements.forEach(el => el.classList.add('animated'));
      return;
    }

    const io = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animated');
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -80px 0px' }
    );

    elements.forEach(el => io.observe(el));
    return () => io.disconnect();
  }, []);

  return (
    <div className="home">
      {/* HERO SECTION */}
      <section className="hero">
        <div className="hero-background">
          <video
            src={`${filesServiceUrl}/files/${photos.hero}`}
            className="hero-image"
            autoPlay
            muted
            loop
            playsInline
            preload="auto"
          />
          <div className="hero-overlay" />
        </div>
        <div className="hero-container">
          <div className="hero-content" data-animate>
            <p className="hero-label">DESIGN & BUILD</p>
            <h1 className="hero-heading">Crafting Your Dream Space</h1>
            <p className="hero-description">Quality construction, timeless design, since 1990</p>
            <div className="hero-buttons">
              <Link to="/realisation" className="btn btn-primary">Discover</Link>
              <Link to="/contact" className="btn btn-secondary">Get In Touch</Link>
            </div>
          </div>
        </div>
        <div className="scroll-down" data-animate>
          <div className="scroll-icon"></div>
          <p>Scroll</p>
        </div>
      </section>

      {/* THINK SECTION */}
      <section className="content-section featured-section">
        <div className="section-image-container" data-animate>
          <img
            src={`${filesServiceUrl}/files/${photos.think}`}
            alt="Design concept"
            className="section-image"
            loading="lazy"
          />
        </div>
        <div className="section-text-wrapper" data-animate>
          <div className="section-header">
            <h2 className="section-title">
              <em>Think</em> intelligent, practical and comfortable spaces
            </h2>
            <p className="section-subtitle">
              Passionate about architecture and design, our mission is to provide you with a unique and memorable experience.
            </p>
            <Link to="/realisation" className="link-arrow">
              Discover
            </Link>
          </div>
        </div>
      </section>

      {/* BUILD SECTION */}
      <section className="content-section featured-section reverse">
        <div className="section-text-wrapper" data-animate>
          <div className="section-header">
            <h2 className="section-title">
              <em>Build</em> a strong relationship and quality partnership
            </h2>
            <p className="section-subtitle">
              Driven by our desire for perfection, we do everything to ensure you are more than satisfied with our work.
            </p>
            <Link to="/a-propos" className="link-arrow">
              Discover Our Team
            </Link>
          </div>
        </div>
        <div className="section-image-container" data-animate>
          <img
            src={`${filesServiceUrl}/files/${photos.build}`}
            alt="Construction"
            className="section-image"
            loading="lazy"
          />
        </div>
      </section>

      {/* LIVE SECTION */}
      <section className="content-section featured-section">
        <div className="section-image-container" data-animate>
          <img
            src={`${filesServiceUrl}/files/${photos.live}`}
            alt="Lifestyle"
            className="section-image"
            loading="lazy"
          />
        </div>
        <div className="section-text-wrapper" data-animate>
          <div className="section-header">
            <h2 className="section-title">
              <em>Live</em> in a space that reflects your values
            </h2>
            <p className="section-subtitle">
              Your home is one of the most important investments of your life. Choose reliability and lasting quality.
            </p>
            <Link to="/projects/foresta" className="link-arrow">
              Explore Projects
            </Link>
          </div>
        </div>
      </section>

      {/* PORTFOLIO GRID */}
      <section className="portfolio-section">
        <div className="container">
          <div className="section-header center" data-animate>
            <span className="section-kicker">Our Work</span>
            <h2 className="section-title">Projects & Achievements</h2>
          </div>
          <div className="portfolio-grid">
            <Link to="/projects/foresta" className="portfolio-card" data-animate>
              <img
                src={`${filesServiceUrl}/files/${photos.collage1}`}
                alt="Foresta Project"
                loading="lazy"
                className="card-image-bg"
              />
              <div className="card-overlay" />
              <div className="card-content">
                <h3 className="card-title">Foresta</h3>
                <p className="card-subtitle">Residential Project</p>
              </div>
            </Link>

            <Link to="/realisation" className="portfolio-card" data-animate>
              <img
                src={`${filesServiceUrl}/files/${photos.collage2}`}
                alt="Panorama Project"
                loading="lazy"
                className="card-image-bg"
              />
              <div className="card-overlay" />
              <div className="card-content">
                <h3 className="card-title">Panorama</h3>
                <p className="card-subtitle">Condominiums</p>
              </div>
            </Link>

            <Link to="/realisation" className="portfolio-card" data-animate>
              <img
                src={`${filesServiceUrl}/files/${photos.collage3}`}
                alt="Project Management"
                loading="lazy"
                className="card-image-bg"
              />
              <div className="card-overlay" />
              <div className="card-content">
                <h3 className="card-title">Management</h3>
                <p className="card-subtitle">Project Services</p>
              </div>
            </Link>

            <Link to="/realisation" className="portfolio-card" data-animate>
              <img
                src={`${filesServiceUrl}/files/${photos.project2}`}
                alt="Our Achievements"
                loading="lazy"
                className="card-image-bg"
              />
              <div className="card-overlay" />
              <div className="card-content">
                <h3 className="card-title">Achievements</h3>
                <p className="card-subtitle">Portfolio</p>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* FEATURES SECTION */}
      <section className="features-section">
        <div className="container">
          <div className="section-header center" data-animate>
            <span className="section-kicker">Why Choose Us</span>
            <h2 className="section-title">Personalized Support</h2>
          </div>
          <p className="features-intro" data-animate>
            Dominic Cyr Construction Inc. is a dynamic family business with over 30 years of experience in residential construction. We are here to support you in realizing your ideas and projects with personalized, professional service built on transparency.
          </p>
          <div className="features-grid">
            <div className="feature-card" data-animate>
              <h3 className="feature-title">Passionate & Professional</h3>
            </div>
            <div className="feature-card" data-animate>
              <h3 className="feature-title">Creative Approach</h3>
            </div>
            <div className="feature-card" data-animate>
              <h3 className="feature-title">Attention to Detail</h3>
            </div>
            <div className="feature-card" data-animate>
              <h3 className="feature-title">Eco-Friendly Practices</h3>
            </div>
            <div className="feature-card" data-animate>
              <h3 className="feature-title">Excellence Standards</h3>
            </div>
            <div className="feature-card" data-animate>
              <h3 className="feature-title">After-Sales Service</h3>
            </div>
          </div>
        </div>
      </section>

      {/* CONTACT CTA */}
      <section className="contact-cta">
        <div className="contact-wrapper">
          <div className="contact-image" data-animate>
            <img
              src={`${filesServiceUrl}/files/${photos.contact}`}
              alt="Contact us"
              loading="lazy"
            />
          </div>
          <div className="contact-content" data-animate>
            <span className="section-kicker">Get In Touch</span>
            <h2 className="contact-title">
              Let's Build Your Dream Home Together
            </h2>
            <p className="contact-description">
              Your home is one of the most important investments you'll ever make. Choose a trusted partner with Dominic Cyr Construction Inc.
            </p>
            <Link to="/contact" className="btn btn-primary">
              Contact Us
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}