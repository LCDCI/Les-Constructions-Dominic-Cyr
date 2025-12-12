import React from 'react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import { fetchRenovations } from '../features/renovations/api/renovations';
import RenovationCard from '../features/renovations/components/RenovationCard';

const defaultResolveAssetUrl = (identifier) => identifier ?? '';

const RenovationsPage = ({ resolveAssetUrl }) => {
    // Load translations from the root namespace since your JSON is flat
    const { t } = usePageTranslations('renovations');

    const {
        data: renovations = [],
        isLoading,
        isError,
        refetch,
    } = fetchRenovations();

    return (
        <section className="renovations-page" aria-live="polite">

            {/* HERO */}
            <header className="renovations-page__hero">
                <h1 className="renovations-page__title">
                    {t('hero.title1')}<br />
                    {t('hero.title2')}<br />
                    {t('hero.title3')}
                </h1>
                <p className="renovations-page__subtitle">{t('hero.subtitle')}</p>
            </header>

            {/* INTRO */}
            <section className="renovations-page__intro">
                <h2>{t('intro.title')}</h2>
                <p>{t('intro.description')}</p>
            </section>

            {/* SERVICES */}
            <section className="renovations-page__services">
                <p>{t('services.title')}</p>
            </section>

            {/* ERROR */}
            {isError && (
                <div className="renovations-page__status renovations-page__status--error" role="alert">
                    <p>{t('states.error.message')}</p>
                    <button type="button" onClick={refetch}>
                        {t('states.error.retry')}
                    </button>
                </div>
            )}

            {/* LOADING */}
            {isLoading && (
                <div className="renovations-page__grid">
                    {[0, 1, 2].map((skeleton) => (
                        <div key={skeleton} className="renovations-page__skeleton" aria-hidden="true" />
                    ))}
                </div>
            )}

            {/* EMPTY */}
            {!isLoading && !isError && renovations.length === 0 && (
                <div className="renovations-page__status renovations-page__status--empty">
                    <h2>{t('states.empty.title')}</h2>
                    <p>{t('states.empty.body')}</p>
                </div>
            )}

            {/* RENOVATIONS LIST */}
            {!isLoading && renovations.length > 0 && (
                <div className="renovations-page__grid">
                    {renovations.map(
                        ({ renovationIdentifier, beforeImageIdentifier, afterImageIdentifier, description }) => (
                            <RenovationCard
                                key={renovationIdentifier}
                                renovationIdentifier={renovationIdentifier}
                                beforeImageIdentifier={beforeImageIdentifier}
                                afterImageIdentifier={afterImageIdentifier}
                                description={description}
                                resolveAssetUrl={resolveAssetUrl}
                            />
                        ),
                    )}
                </div>
            )}

            {/* CALL TO ACTION */}
            <section className="renovations-page__cta">
                <h2>{t('callToAction.title')}</h2>
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
