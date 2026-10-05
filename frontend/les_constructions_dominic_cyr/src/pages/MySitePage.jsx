import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import {
  fetchSiteContentGroups,
  fetchSiteContent,
  updateSiteContent,
} from '../features/siteContent/api/siteContentApi';
import '../styles/MySitePage.css';

const SiteImagePreview = ({ imageIdentifier }) => {
  const [attempts, setAttempts] = useState(0);
  const [source, setSource] = useState(() => {
    if (!imageIdentifier) return '';
    if (String(imageIdentifier).startsWith('http')) return imageIdentifier;
    const base = import.meta.env.VITE_FILES_SERVICE_URL || '/files';
    return `${base.replace(/\/$/, '')}/files/${imageIdentifier}`;
  });

  if (!source || attempts >= 3) return null;

  return (
    <img
      className="my-site-image-preview"
      src={source}
      alt=""
      onError={() => {
        const nextAttempt = attempts + 1;
        setAttempts(nextAttempt);
        if (nextAttempt < 3) {
          setSource(`${source.split('?')[0]}?retry=${nextAttempt}`);
        }
      }}
    />
  );
};

SiteImagePreview.propTypes = {
  imageIdentifier: PropTypes.string,
};

const MySitePage = () => {
  const { t } = usePageTranslations('mySite');
  const { i18n } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const [language, setLanguage] = useState(
    i18n.language?.startsWith('fr') ? 'fr' : 'en'
  );
  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState('');
  const [items, setItems] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [error, setError] = useState(null);
  const [savedId, setSavedId] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const loadGroups = async () => {
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      return fetchSiteContentGroups(language, token);
    };

    loadGroups()
      .then(data => {
        if (cancelled) return;
        const nextGroups = Array.isArray(data) ? data : [];
        setGroups(nextGroups);
        setSelectedGroup(current =>
          current && nextGroups.includes(current)
            ? current
            : nextGroups[0] || ''
        );
      })
      .catch(() => {
        if (!cancelled) setError('loadError');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [language, getAccessTokenSilently]);

  useEffect(() => {
    if (!selectedGroup) {
      setItems([]);
      return undefined;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    const loadGroup = async () => {
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      return fetchSiteContent(language, token, selectedGroup);
    };

    loadGroup()
      .then(data => {
        if (cancelled) return;
        setItems(Array.isArray(data) ? data : []);
        setDrafts(
          Object.fromEntries(
            (data || []).map(item => [
              item.id,
              {
                contentText: item.contentText || '',
                imageIdentifier: item.imageIdentifier || '',
              },
            ])
          )
        );
      })
      .catch(() => {
        if (!cancelled) setError('loadError');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [language, selectedGroup, getAccessTokenSilently]);

  const groupedItems = useMemo(
    () =>
      items.reduce((groups, item) => {
        const group = item.pageGroup || 'other';
        groups[group] = groups[group] || [];
        groups[group].push(item);
        return groups;
      }, {}),
    [items]
  );

  const updateDraft = (id, field, value) => {
    setDrafts(current => ({
      ...current,
      [id]: { ...current[id], [field]: value },
    }));
  };

  const saveItem = async item => {
    const draft = drafts[item.id] || {};
    try {
      setSavingId(item.id);
      setSavedId(null);
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const updated = await updateSiteContent(
        item.id,
        draft.contentText || '',
        draft.imageIdentifier || null,
        token
      );
      setItems(current =>
        current.map(entry => (entry.id === item.id ? updated : entry))
      );
      setSavedId(item.id);
    } catch (saveError) {
      setError('saveError');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) {
    return (
      <div className="my-site-page">
        {t('loading', 'Loading site content...')}
      </div>
    );
  }

  return (
    <div className="my-site-page">
      <header className="my-site-header">
        <div>
          <p className="my-site-kicker">{t('kicker', 'Website management')}</p>
          <h1>{t('title', 'My site')}</h1>
          <p>
            {t(
              'subtitle',
              'Manage public page text and image references by language.'
            )}
          </p>
        </div>
        <label className="my-site-language">
          {t('language', 'Language')}
          <select
            value={language}
            onChange={event => setLanguage(event.target.value)}
          >
            <option value="en">English</option>
            <option value="fr">Français</option>
          </select>
        </label>
        <label className="my-site-language">
          {t('pageGroup', 'Page')}
          <select
            value={selectedGroup}
            onChange={event => setSelectedGroup(event.target.value)}
          >
            {groups.map(group => (
              <option key={group} value={group}>
                {group}
              </option>
            ))}
          </select>
        </label>
      </header>

      {error && (
        <p className="my-site-error">
          {t(error, 'Unable to load site content.')}
        </p>
      )}

      {Object.keys(groupedItems).length === 0 ? (
        <p className="my-site-empty">
          {t('empty', 'No editable content has been seeded yet.')}
        </p>
      ) : (
        <div className="my-site-groups">
          {Object.entries(groupedItems).map(([group, groupItems]) => (
            <section className="my-site-group" key={group}>
              <h2>{group}</h2>
              {groupItems.map(item => {
                const draft = drafts[item.id] || {};
                return (
                  <article className="my-site-item" key={item.id}>
                    <div className="my-site-item-heading">
                      <h3>{item.pageKey}</h3>
                      <span>{language.toUpperCase()}</span>
                    </div>
                    <label>
                      {t('textLabel', 'Text')}
                      <textarea
                        value={draft.contentText || ''}
                        onChange={event =>
                          updateDraft(
                            item.id,
                            'contentText',
                            event.target.value
                          )
                        }
                        rows={4}
                      />
                    </label>
                    <label>
                      {t('imageLabel', 'Image identifier or URL')}
                      <input
                        value={draft.imageIdentifier || ''}
                        onChange={event =>
                          updateDraft(
                            item.id,
                            'imageIdentifier',
                            event.target.value
                          )
                        }
                      />
                    </label>
                    <SiteImagePreview
                      key={draft.imageIdentifier || item.id}
                      imageIdentifier={draft.imageIdentifier}
                    />
                    <button
                      type="button"
                      onClick={() => saveItem(item)}
                      disabled={savingId === item.id}
                    >
                      {savingId === item.id
                        ? t('saving', 'Saving...')
                        : savedId === item.id
                          ? t('saved', 'Saved')
                          : t('save', 'Save changes')}
                    </button>
                  </article>
                );
              })}
            </section>
          ))}
        </div>
      )}
    </div>
  );
};

export default MySitePage;
