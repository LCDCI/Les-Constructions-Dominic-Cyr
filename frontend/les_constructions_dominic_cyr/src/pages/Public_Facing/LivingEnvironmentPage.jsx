import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import '../../styles/Public_Facing/home.css';
import '../../styles/Public_Facing/living-environment.css';
import {
  FaSkiing,
  FaGolfBall,
  FaBicycle,
  FaMountain,
  FaTree,
  FaWineGlass,
  FaUtensils,
  FaShoppingCart,
  FaShoppingBag,
  FaHospital,
  FaSchool,
  FaSpa,
} from 'react-icons/fa';

const LivingEnvironmentPage = () => {
  const { t } = useTranslation('livingEnvironment');
  const { projectIdentifier } = useParams();

  const amenities = [
    { icon: <FaSkiing />, label: t('amenities.ski', 'Ski') },
    { icon: <FaGolfBall />, label: t('amenities.golf', 'Golf') },
    { icon: <FaBicycle />, label: t('amenities.bike', 'Vélo') },
    { icon: <FaMountain />, label: t('amenities.bromont', 'Bromont') },
    {
      icon: <FaTree />,
      label: t('amenities.yamaska', 'Parc National de la Yamaska'),
    },
    {
      icon: <FaWineGlass />,
      label: t('amenities.vineyards', 'Vergers & Vignobles'),
    },
    { icon: <FaUtensils />, label: t('amenities.restaurants', 'Restaurants') },
    { icon: <FaShoppingCart />, label: t('amenities.groceries', 'Épiceries') },
    { icon: <FaShoppingBag />, label: t('amenities.stores', 'Magasins') },
    { icon: <FaHospital />, label: t('amenities.hospitals', 'Hôpitaux') },
    { icon: <FaSchool />, label: t('amenities.schools', 'Écoles') },
    { icon: <FaSpa />, label: t('amenities.spas', 'Spas') },
  ];

  return (
    <div className="living-environment-page">
      <div className="container">
        {/* Header Section */}
        <section className="le-header-section">
          <h1 className="le-main-title">{t('header.foresta', 'FÖRESTA, UN')}</h1>
          <h2 className="le-subtitle">
            {t('header.livingEnvironment', 'MILIEU DE VIE')}
          </h2>
          <h3 className="le-subtitle-last">
            {t('header.exceptional', 'EXCEPTIONNEL')}
          </h3>
          <p className="le-tagline">
            {t('header.tagline', 'Au rythme de la nature')}
          </p>
        </section>

        {/* Description Section */}
        <section className="le-description-section">
          <p className="le-description-text">
            {t(
              'description.main',
              "Un milieu de vie exceptionnel pour ceux qui rêvent de s'évader du quotidien et de se reconnecter à l'essentiel, le projet Fõresta offre l'opportunité d'habiter une demeure au cœur de la nature. Ce nouveau quartier est situé à 5 km de l'autoroute 10 (A-10), à 15 minutes du centre-ville de Granby et à portée de main d'une panoplie de services et d'activités. Les adeptes de plein air seront comblés."
            )}
          </p>
        </section>

        {/* Proximity Section */}
        <section className="le-proximity-section">
          <h2 className="le-proximity-title">
            {t('proximity.title', 'À PROXIMITÉ DE TOUT !')}
          </h2>

          <div className="le-amenities-grid">
            {amenities.map((amenity, index) => (
              <div key={index} className="le-amenity-box">
                <div className="le-amenity-icon">{amenity.icon}</div>
                <p className="le-amenity-label">{amenity.label}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Footer Section */}
        <section className="le-footer-section">
          <p className="le-footer-text">
            {t(
              'footer.signature',
              'FÖRESTA est un projet signé Les Constructions Dominic Cyr inc.'
            )}
          </p>
        </section>
      </div>
    </div>
  );
};

export default LivingEnvironmentPage;
