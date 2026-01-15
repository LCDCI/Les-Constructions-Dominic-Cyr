// frontend/les_constructions_dominic_cyr/src/features/renovations/components/RenovationCard.jsx
import React from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import './RenovationCard.css';

const identityAssetResolver = identifier => (identifier ? identifier : '');

const RenovationCard = ({
  renovationIdentifier: _renovationIdentifier,
  beforeImageIdentifier,
  afterImageIdentifier,
  description,
  resolveAssetUrl,
  showTitle,
}) => {
  const { t, isLoading } = usePageTranslations('renovations');

  const buildImageSrc = identifier =>
    identifier ? resolveAssetUrl(identifier) : '';

  const mediaItems = [
    {
      slot: 'before',
      src: buildImageSrc(beforeImageIdentifier),
      label: t('renovationCard.media.before', 'Avant'),
    },
    {
      slot: 'after',
      src: buildImageSrc(afterImageIdentifier),
      label: t('renovationCard.media.after', 'Après'),
    },
  ].filter(({ src }) => Boolean(src));

  if (isLoading) {
    return (
      <div className="renovation-card" aria-busy="true">
        <div className="project-card__skeleton" />
      </div>
    );
  }

  const noMedia = mediaItems.length === 0;

  return (
    <div className="renovation-card" data-testid="renovation-card">
      <div className={`renovation-card__content${noMedia ? ' no-media' : ''}`}>
        <div className="project-card__body renovation-card__text">
          {showTitle && (
            <h3 className="renovation-card__title">
              {t('renovationCard.labels.cardType', 'Renovation')}
            </h3>
          )}
          <p className="project-card__description renovation-card__description">
            {description ||
              t(
                'renovationCard.labels.descriptionFallback',
                'No description available'
              )}
          </p>
        </div>

        {mediaItems.length > 0 && (
          <div
            className="project-card__media-grid renovation-card__media-grid"
            aria-label={t(
              'renovationCard.media.sectionLabel',
              'Renovation images'
            )}
          >
            {mediaItems.map(({ slot, src, label }) => (
              <figure key={slot} className="renovation-card__figure">
                <img
                  src={src}
                  alt={label}
                  loading="lazy"
                  className="renovation-card__image"
                />
                <figcaption className="renovation-card__caption">
                  {label}
                </figcaption>
              </figure>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

RenovationCard.propTypes = {
  renovationIdentifier: PropTypes.string.isRequired,
  beforeImageIdentifier: PropTypes.string,
  afterImageIdentifier: PropTypes.string,
  description: PropTypes.string,
  resolveAssetUrl: PropTypes.func,
  showTitle: PropTypes.bool,
};

RenovationCard.defaultProps = {
  beforeImageIdentifier: '',
  afterImageIdentifier: '',
  description: '',
  resolveAssetUrl: identityAssetResolver,
  showTitle: true,
};

export default RenovationCard;
