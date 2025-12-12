// frontend/les_constructions_dominic_cyr/src/features/renovations/components/RenovationCard.jsx
import React from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import './RenovationCard.css';

const identityAssetResolver = (identifier) => (identifier ? identifier : '');

const RenovationCard = ({
                            renovationIdentifier,
                            beforeImageIdentifier,
                            afterImageIdentifier,
                            description,
                            resolveAssetUrl,
                        }) => {
    const { t, isLoading } = usePageTranslations('renovationCard');

    const buildImageSrc = (identifier) => (identifier ? resolveAssetUrl(identifier) : '');

    const mediaItems = [
        {
            slot: 'before',
            src: buildImageSrc(beforeImageIdentifier),
            label: t('media.before'),
        },
        {
            slot: 'after',
            src: buildImageSrc(afterImageIdentifier),
            label: t('media.after'),
        },
    ].filter(({ src }) => Boolean(src));

    if (isLoading) {
        return (
            <article className="project-card renovation-card" aria-busy="true">
                <div className="project-card__skeleton" />
            </article>
        );
    }

    return (
        <article className="project-card renovation-card" data-testid="renovation-card">
            <header className="project-card__header renovation-card__header">
                <div>
                    <p className="project-card__eyebrow">
                        {t('labels.identifier')}
                    </p>
                    <h3 className="project-card__title renovation-card__title">
                        {renovationIdentifier}
                    </h3>
                </div>
                <span className="project-card__badge renovation-card__badge">
          {t('labels.cardType')}
        </span>
            </header>

            <section className="project-card__body">
                <h4 className="project-card__section-title">
                    {t('labels.description')}
                </h4>
                <p className="project-card__description renovation-card__description">
                    {description || t('labels.descriptionFallback')}
                </p>
            </section>

            {mediaItems.length > 0 && (
                <section
                    className="project-card__media-grid renovation-card__media-grid"
                    aria-label={t('media.sectionLabel')}
                >
                    {mediaItems.map(({ slot, src, label }) => (
                        <figure key={slot} className="renovation-card__figure">
                            <img
                                src={src}
                                alt={`${label} - ${renovationIdentifier}`}
                                loading="lazy"
                                className="renovation-card__image"
                            />
                            <figcaption className="renovation-card__caption">{label}</figcaption>
                        </figure>
                    ))}
                </section>
            )}
        </article>
    );
};

RenovationCard.propTypes = {
    renovationIdentifier: PropTypes.string.isRequired,
    beforeImageIdentifier: PropTypes.string,
    afterImageIdentifier: PropTypes.string,
    description: PropTypes.string,
    resolveAssetUrl: PropTypes.func,
};

RenovationCard.defaultProps = {
    beforeImageIdentifier: '',
    afterImageIdentifier: '',
    description: '',
    resolveAssetUrl: identityAssetResolver,
};

export default RenovationCard;
