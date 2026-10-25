import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  FiAward,
  FiHeart,
  FiPhoneCall,
  FiStar,
  FiTarget,
} from 'react-icons/fi';
import { RiLeafLine } from 'react-icons/ri';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/home.css';
import '../../styles/Public_Facing/RenovationsPage.css';
import '../../styles/Public_Facing/residential-projects.css';

const RenovationsPage = () => {
  const { t } = usePageTranslations('renovations');

  useEffect(() => {
    const elements = Array.from(document.querySelectorAll('[data-animate]'));
    if (!elements.length) return undefined;

    const prefersReduced =
      typeof window !== 'undefined' &&
      window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;

    if (prefersReduced) {
      elements.forEach(element => element.classList.add('animated'));
      return undefined;
    }

    const observer = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animated');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -80px 0px' }
    );

    elements.forEach(element => observer.observe(element));
    return () => observer.disconnect();
  }, []);

  const features = [
    {
      icon: FiHeart,
      title: 'features.feature1',
      description: 'features.feature1Description',
      fallbackTitle: 'Passionate and professional',
      fallbackDescription:
        'Committed expertise in every detail of your project.',
    },
    {
      icon: FiTarget,
      title: 'features.feature2',
      description: 'features.feature2Description',
      fallbackTitle: 'Creative approach',
      fallbackDescription:
        'Thoughtful ideas to create a space that feels like you.',
    },
    {
      icon: FiStar,
      title: 'features.feature3',
      description: 'features.feature3Description',
      fallbackTitle: 'Attention to detail',
      fallbackDescription:
        'Careful planning and precise execution from start to finish.',
    },
    {
      icon: RiLeafLine,
      title: 'features.feature4',
      description: 'features.feature4Description',
      fallbackTitle: 'Sustainable practices',
      fallbackDescription:
        'Responsible choices for comfortable and lasting homes.',
    },
    {
      icon: FiAward,
      title: 'features.feature5',
      description: 'features.feature5Description',
      fallbackTitle: 'Standards of excellence',
      fallbackDescription:
        'High standards for every material, finish, and decision.',
    },
    {
      icon: FiPhoneCall,
      title: 'features.feature6',
      description: 'features.feature6Description',
      fallbackTitle: 'After-sales service',
      fallbackDescription:
        'Reliable support that continues after your project is complete.',
    },
  ];

  return (
    <div className="public-page renovations-page">
      <section
        className="public-page__hero"
        aria-labelledby="renovations-hero-title"
      >
        <div className="public-page__hero-content" data-animate>
          <span className="public-page__kicker">
            {t('hero.label', 'Renovate, Modernize & Transform')}
          </span>
          <h1 className="public-page__title" id="renovations-hero-title">
            {t('hero.heading', 'Renovations')}
          </h1>
          <p className="public-page__lead">
            {t('hero.lede', 'Your home deserves the very best')}
          </p>
        </div>
      </section>

      <section
        className="public-page__section renovations-intro-section"
        aria-labelledby="renovations-intro-title"
      >
        <div className="public-page__intro" data-animate>
          <span className="public-page__eyebrow">
            {t('intro.kicker', 'Transform')}
          </span>
          <h2 className="public-page__heading" id="renovations-intro-title">
            {t(
              'intro.title',
              'your home into the living space of your dreams!'
            )}
          </h2>
          <p className="public-page__copy">
            {t(
              'intro.description',
              "Whether it's modernizing your kitchen, completely rethinking your basement, opening up spaces, or increasing your property value, our team delivers your project with precision, style, and efficiency. We offer a turnkey service: planning, design, site management, and high-end finishing. Every detail is crafted to reflect your personality and improve your daily comfort. Entrust your renovation to passionate professionals!"
            )}
          </p>
        </div>
      </section>

      <section
        className="public-page__section public-page__section--tint renovations-features-section"
        aria-labelledby="renovations-features-title"
      >
        <div className="public-page__intro">
          <span className="public-page__eyebrow">
            {t('features.kicker', 'What sets us apart')}
          </span>
          <h2 className="public-page__heading" id="renovations-features-title">
            {t('features.title', 'A renovation experience shaped around you')}
          </h2>
        </div>
        <div className="public-page__feature-grid">
          {features.map(
            ({
              icon: Icon,
              title,
              description,
              fallbackTitle,
              fallbackDescription,
            }) => (
              <article
                className="public-page__feature"
                data-animate
                key={title}
              >
                <div className="public-page__feature-icon" aria-hidden="true">
                  <Icon />
                </div>
                <h3 className="public-page__feature-title">
                  {t(title, fallbackTitle)}
                </h3>
                <p className="public-page__copy">
                  {t(description, fallbackDescription)}
                </p>
              </article>
            )
          )}
        </div>
      </section>

      <section className="public-page__cta">
        <span className="public-page__eyebrow">
          {t('cta.kicker', 'Get in touch')}
        </span>
        <h2 className="public-page__heading">
          {t('cta.title', "Let's build your dream home together")}
        </h2>
        <p className="public-page__copy">
          {t(
            'cta.subtitle',
            "Your home is one of the most important investments you'll ever make. Choose a trusted partner with Dominic Cyr Construction Inc."
          )}
        </p>
        <Link to="/contact" className="public-page__button">
          {t('cta.button', 'Talk to an expert')}
        </Link>
      </section>
    </div>
  );
};

export default RenovationsPage;
