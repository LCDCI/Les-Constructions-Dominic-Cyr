import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import { fetchRenovations } from '../features/renovations/api/renovations';
import RenovationCard from '../features/renovations/components/RenovationCard';

const defaultResolveAssetUrl = identifier => identifier ?? '';

const RenovationsPage = ({ resolveAssetUrl }) => {
  // Load translations from the root namespace since your JSON is flat
  const { t } = usePageTranslations('renovations');

  const [renovations, setRenovations] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isError, setIsError] = useState(false);

  useEffect(() => {
    loadRenovations();
  }, []);

  const loadRenovations = async () => {
    try {
      setIsLoading(true);
      setIsError(false);
      const data = await fetchRenovations();
      setRenovations(data);
    } catch (error) {
      console.error('Failed to fetch renovations:', error);
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  const refetch = () => {
    loadRenovations();
  };

  return (
    <section className="renovations-page" aria-live="polite">
      {/* HERO */}
      <header className="renovations-page__hero">
        <h1 className="renovations-page__title">
          {t('hero.title1', 'RÉNOVER')}
          <br />
          {t('hero.title2', 'MODERNISER')}
          <br />
          {t('hero.title3', 'TRANSFORMER')}
        </h1>
        <p className="renovations-page__subtitle">
          {t('hero.subtitle', "Votre maison mérite ce qu'il y a de mieux")}
        </p>
      </header>

      {/* INTRO */}
      <section className="renovations-page__intro">
        <h2>
          {t(
            'intro.title',
            'Transformez votre maison en un espace de vie dont vous rêvez !'
          )}
        </h2>
        <p>
          {t(
            'intro.description',
            'Que ce soit pour moderniser votre cuisine, repenser entièrement votre sous-sol, ouvrir des espaces ou augmenter la valeur de votre propriété, notre équipe réalise votre projet avec précision, style et efficacité.'
          )}
        </p>
      </section>

      {/* SERVICES */}
      <section className="renovations-page__services">
        <p>
          {t(
            'services.title',
            'Nous offrons un service clé en main : planification, design, gestion de chantier et finition haut de gamme. Chaque détail est pensé pour refléter votre personnalité et améliorer votre confort au quotidien.'
          )}
        </p>
      </section>

      {/* ERROR */}
      {isError && (
        <div
          className="renovations-page__status renovations-page__status--error"
          role="alert"
        >
          <p>
            {t(
              'states.error.message',
              'Impossible de charger les rénovations. Veuillez réessayer.'
            )}
          </p>
          <button type="button" onClick={refetch}>
            {t('states.error.retry', 'Réessayer')}
          </button>
        </div>
      )}

      {/* LOADING */}
      {isLoading && (
        <div className="renovations-page__grid">
          {[0, 1, 2].map(skeleton => (
            <div
              key={skeleton}
              className="renovations-page__skeleton"
              aria-hidden="true"
            />
          ))}
        </div>
      )}

      {/* EMPTY */}
      {!isLoading && !isError && renovations.length === 0 && (
        <div className="renovations-page__status renovations-page__status--empty">
          <h2>{t('states.empty.title', 'Aucune rénovation pour le moment')}</h2>
          <p>
            {t(
              'states.empty.body',
              'Revenez bientôt pour voir nos derniers projets de rénovation !'
            )}
          </p>
        </div>
      )}

      {/* RENOVATIONS LIST */}
      {!isLoading && renovations.length > 0 && (
        <div className="renovations-page__grid">
          {renovations.map(
            ({
              renovationId,
              beforeImageIdentifier,
              afterImageIdentifier,
              description,
            }) => (
              <RenovationCard
                key={renovationId}
                renovationIdentifier={renovationId}
                beforeImageIdentifier={beforeImageIdentifier}
                afterImageIdentifier={afterImageIdentifier}
                description={description}
                resolveAssetUrl={resolveAssetUrl}
              />
            )
          )}
        </div>
      )}

      {/* CALL TO ACTION */}
      <section className="renovations-page__cta">
        <h2>
          {t(
            'callToAction.title',
            'Confiez votre rénovation à des professionnels passionnés !'
          )}
        </h2>
      </section>
    </section>
  );
};

RenovationsPage.propTypes = {
  resolveAssetUrl: PropTypes.func,
};

RenovationsPage.defaultProps = {
  resolveAssetUrl: defaultResolveAssetUrl,
};

export default RenovationsPage;
