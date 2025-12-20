import React from 'react';
import '../../styles/Public_Facing/home.css';
import { Link } from 'react-router-dom';
import { usePageTranslations } from '../../hooks/usePageTranslations';

export default function Home() {
  const { t } = usePageTranslations('home');
  return (
    <div className="home-content">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-image-container">
          <img
            src="https://placehold.co/1920x600/4A90A4/FFFFFF?text=Les+Constructions+Dominic+Cyr&font=roboto"
            alt={t('hero.alt', 'Construction résidentielle')}
            className="hero-image"
            onError={e => {
              e.target.style.backgroundColor = '#4A90A4';
              e.target.style.color = '#FFFFFF';
              e.target.style.display = 'flex';
              e.target.style.alignItems = 'center';
              e.target.style.justifyContent = 'center';
            }}
          />
        </div>
      </section>

      {/* Photo Collage Section */}
      <section className="photo-collage-section">
        <div className="collage-wrapper">
          {/* Top Row - Three Images */}
          <div className="collage-top-row">
            <div className="collage-item collage-landscape-left">
              <img
                src="https://placehold.co/600x400/4A90A4/FFFFFF?text=Maisons+Style+Grange&font=roboto"
                  alt={t('gallery.terrace', 'Terrasse moderne')}
                className="collage-image"
              />
            </div>
            <div className="collage-item collage-landscape-middle">
              <img
                src="https://placehold.co/600x400/5A7D8C/FFFFFF?text=Maison+Moderne+2+Etages&font=roboto"
                  alt={t('gallery.modernHouse', 'Maison moderne illuminée')}
                className="collage-image"
              />
            </div>
            <div className="collage-item collage-portrait-right">
              <img
                src="https://placehold.co/400x600/2C3E50/FFFFFF?text=Terrasse+Moderne&font=roboto"
                alt="Terrasse moderne"
                className="collage-image"
              />
            </div>
          </div>

          {/* Bottom Row - Panoramic Image */}
          <div className="collage-bottom-row">
            <div className="collage-panoramic">
              <img
                src="https://placehold.co/1800x500/4A90A4/FFFFFF?text=Piscine+et+Residence&font=roboto"
                  alt={t('gallery.pool', 'Piscine et résidence')}
                className="collage-panoramic-image"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Main Content Section */}
      <section className="main-content-section">
        <div className="content-wrapper">
          {/* Accompaniment Section */}
          <div className="accompaniment-section">
            <div className="section-header">
              <h2 className="section-title">{t('accompaniment.title', 'UN ACCOMPAGNEMENT SUR MESURE')}</h2>
              <h3 className="section-subtitle">{t('accompaniment.subtitle', 'Une expérience conviviale')}</h3>
            </div>

            <div className="section-content">
              <div className="text-content">
                <p className="intro-text">
                  {t('accompaniment.intro', 'Les Constructions Dominic Cyr Inc. est une entreprise familiale et dynamique qui compte plus de 30 ans d\'expérience dans le domaine de la construction résidentielle. Nous sommes là pour vous accompagner dans la réalisation de vos idées et de vos projets, en assurant un service personnalisé et professionnel misant sur la transparence.')}
                </p>
              </div>

              <div className="values-grid">
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x1f3d7, 0xfe0f)}</div>
                  <h4>{t('accompaniment.values.passionate', 'Passionné et professionnel')}</h4>
                </div>
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x2728)}</div>
                  <h4>{t('accompaniment.values.creative', 'Approche créative')}</h4>
                </div>
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x1f50d)}</div>
                  <h4>{t('accompaniment.values.detail', 'Souci du détail')}</h4>
                </div>
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x1f331)}</div>
                  <h4>{t('accompaniment.values.eco', 'Pratiques écoresponsables')}</h4>
                </div>
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x2b50)}</div>
                  <h4>{t('accompaniment.values.excellence', 'Critère d\'excellence')}</h4>
                </div>
                <div className="value-item">
                  <div className="value-icon">{String.fromCodePoint(0x1f91d)}</div>
                  <h4>{t('accompaniment.values.afterSale', 'Service après-vente')}</h4>
                </div>
              </div>
            </div>
          </div>

          {/* Photo Gallery Section */}
          <div className="photo-gallery-section">
            <div className="gallery-grid">
              {/* Row 1: 2 items */}
              <div className="gallery-item gallery-logo-box">
                <div className="logo-box-content">
                  <Link to="/projects/foresta" className="logo-link">
                    <div className="logo-icon">{String.fromCodePoint(0x1f33f)}</div>
                    <h2 className="logo-text">{t('projects.foresta.title', 'FORESTA')}</h2>
                    <p className="logo-subtext">{t('projects.foresta.subtitle', 'PROJET DOMICILIAIRE')}</p>
                  </Link>
                </div>
              </div>
              <div className="gallery-item gallery-image-forest">
                <img
                  src="https://placehold.co/800x400/4A90A4/FFFFFF?text=Foret+Panoramique&font=roboto"
                  alt={t('gallery.forest', 'Forêt panoramique')}
                  className="gallery-image"
                />
              </div>

              {/* Row 2: 2 items */}
              <div className="gallery-item gallery-image-modern-house">
                <img
                  src="https://placehold.co/800x500/5A7D8C/FFFFFF?text=Maison+Moderne+Illuminee&font=roboto"
                  alt={t('gallery.modernHouse', 'Maison moderne illuminée')}
                  className="gallery-image"
                />
              </div>
              <div className="gallery-item gallery-box gallery-box-blue">
                <div className="gallery-box-content">
                  <Link to="/projects/panorama" className="logo-link">
                    <h3>{t('projects.panorama.title', 'Panorama')}</h3>
                    <p>{t('projects.panorama.subtitle', 'condos')}</p>
                  </Link>
                </div>
              </div>

              {/* Row 3: 3 items */}
              <div className="gallery-item gallery-image-cabin">
                <img
                  src="https://placehold.co/500x400/2C3E50/FFFFFF?text=Chalet+Traditionnel&font=roboto"
                  alt={t('gallery.cabin', 'Chalet traditionnel')}
                  className="gallery-image"
                />
              </div>
              <div className="gallery-item gallery-box gallery-box-grey">
                <div className="gallery-box-content">
                  <Link to="/projectmanagement" className="logo-link">
                    <h3>{t('projects.management.title', 'Gestion')}</h3>
                    <p>{t('projects.management.subtitle', 'de projet')}</p>
                  </Link>
                </div>
              </div>
              <div className="gallery-item gallery-image-stacked-house">
                <img
                  src="https://placehold.co/500x400/4A90A4/FFFFFF?text=Maison+Empilee+Moderne&font=roboto"
                  alt={t('gallery.stackedHouse', 'Maison empilée moderne')}
                  className="gallery-image"
                />
              </div>

              {/* Row 4: 3 items */}
              <div className="gallery-item gallery-box gallery-box-green">
                <div className="gallery-box-content">
                  <Link to="/renovation" className="logo-link">
                    <h3>{t('projects.renovation.title', 'Rénovations')}</h3>
                  </Link>
                </div>
              </div>
              <div className="gallery-item gallery-image-interior">
                <img
                  src="https://placehold.co/500x400/5A7D8C/FFFFFF?text=Interieur+Contemporain&font=roboto"
                  alt={t('gallery.interior', 'Intérieur contemporain')}
                  className="gallery-image"
                />
              </div>
              <div className="gallery-item gallery-box gallery-box-grey">
                <div className="gallery-box-content">
                  <Link to="/realisation" className="logo-link">
                    <h3>{t('projects.realisations.title', 'Réalisations')}</h3>
                  </Link>
                </div>
              </div>
            </div>
          </div>

          {/* Collaboration Section */}
          <div className="collaboration-section">
            <div className="collaboration-content">
              <div className="collaboration-image">
                <img
                  src="https://placehold.co/600x400/5A7D8C/FFFFFF?text=Construction+Residentielle&font=roboto"
                  alt={t('collaboration.title', 'NOUS COLLABORONS AVEC VOUS À LA RÉALISATION DE VOS RÊVES !')}
                  className="section-image"
                />
              </div>
              <div className="collaboration-text">
                <h2 className="collaboration-title">
                  {t('collaboration.title', 'NOUS COLLABORONS AVEC VOUS À LA RÉALISATION DE VOS RÊVES !')}
                </h2>
                <p className="collaboration-description">
                  {t('collaboration.description', 'Votre maison constitue l\'un des plus importants investissements que vous ferez dans votre vie. Optez pour une valeur sûre et sans pareil avec Les Constructions Dominic Cyr Inc.')}
                </p>
              </div>
            </div>
          </div>

          
        </div>
      </section>
    </div>
  );
}
