import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth0 } from '@auth0/auth0-react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import {
  fetchSiteContentGroups,
  fetchSiteContent,
  createSiteContent,
  deleteSiteContent,
  updateSiteContent,
} from '../features/siteContent/api/siteContentApi';
import { deleteFile, uploadFile } from '../features/files/api/filesApi';
import { fetchHouses, deleteHouse } from '../features/houses/api/housesApi';
import HouseEditorModal from '../features/houses/components/HouseEditorModal';
import '../styles/MySitePage.css';

const FIELD_LABEL_KEYS = {
  'hero.label': 'fields.heroLabel',
  'hero.title': 'fields.heroTitle',
  'hero.subtitle': 'fields.heroSubtitle',
  'features.title': 'fields.featuresTitle',
  'features.intro': 'fields.featuresIntro',
  'live.title': 'fields.liveTitle',
  'live.subtitle': 'fields.liveSubtitle',
  'build.title': 'fields.buildTitle',
  'build.subtitle': 'fields.buildSubtitle',
  'think.title': 'fields.thinkTitle',
  'think.subtitle': 'fields.thinkSubtitle',
  'portfolio.title': 'fields.portfolioTitle',
};

const sanitizeEditorText = value =>
  String(value ?? '')
    .replace(/\0/g, '')
    .trim()
    .slice(0, 100000);

const getHouseImageUrl = imageIdentifier => {
  if (!imageIdentifier) return '';
  if (String(imageIdentifier).startsWith('http')) return imageIdentifier;
  const base = (import.meta.env.VITE_FILES_SERVICE_URL || '/files').replace(
    /\/$/,
    ''
  );
  const filesEndpoint = base.endsWith('/files') ? base : `${base}/files`;
  return `${filesEndpoint}/${imageIdentifier}`;
};

const getContentLabel = (pageKey, translate) => {
  const fallback = pageKey
    .split(/[._-]+/)
    .filter(Boolean)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
  return translate(FIELD_LABEL_KEYS[pageKey] || pageKey, fallback);
};

const SiteImagePreview = ({ imageIdentifier }) => {
  const [attempts, setAttempts] = useState(0);
  const [source, setSource] = useState(() => {
    if (!imageIdentifier) return '';
    if (String(imageIdentifier).startsWith('http')) return imageIdentifier;
    const base = import.meta.env.VITE_FILES_SERVICE_URL || '/files';
    const normalizedBase = base.replace(/\/$/, '');
    const filesEndpoint = normalizedBase.endsWith('/files')
      ? normalizedBase
      : `${normalizedBase}/files`;
    return `${filesEndpoint}/${imageIdentifier}`;
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
  const { getAccessTokenSilently, user } = useAuth0();
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
  const [newPageKey, setNewPageKey] = useState('');
  const [newContentText, setNewContentText] = useState('');
  const [projects, setProjects] = useState([]);
  const [houses, setHouses] = useState([]);
  const [selectedPage, setSelectedPage] = useState('');
  const [houseModal, setHouseModal] = useState(null);
  const [contentModal, setContentModal] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const loadGroups = async () => {
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const [groupData, projectResponse, houseData] = await Promise.all([
        fetchSiteContentGroups(language, token),
        fetch(`${import.meta.env.VITE_API_BASE || '/api/v1'}/projects`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetchHouses(null, token),
      ]);
      const projectData = projectResponse.ok
        ? await projectResponse.json()
        : [];
      setProjects(Array.isArray(projectData) ? projectData : []);
      setHouses(Array.isArray(houseData) ? houseData : []);
      return groupData;
    };

    loadGroups()
      .then(data => {
        if (cancelled) return;
        const nextGroups = Array.isArray(data) ? data : [];
        setGroups(nextGroups);
        setSelectedPage(current =>
          current === 'houses' || nextGroups.includes(current)
            ? current
            : nextGroups[0] || 'houses'
        );
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
    if (!selectedGroup || selectedPage === 'houses') {
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
  }, [language, selectedGroup, selectedPage, getAccessTokenSilently]);

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

  const getToken = async () => {
    const { getAuthAudience } = await import('../utils/authConfig');
    return getAccessTokenSilently({
      authorizationParams: { audience: getAuthAudience() },
    });
  };

  const handleImageUpload = async (item, file) => {
    if (!file) return;
    const oldImageIdentifier = item.imageIdentifier;
    try {
      setSavingId(item.id);
      const token = await getToken();
      const formData = new FormData();
      formData.append('file', file);
      formData.append('category', 'PHOTO');
      formData.append('projectId', 'site-content');
      formData.append('uploadedBy', user?.sub || '');
      formData.append('uploaderRole', 'OWNER');

      const uploaded = await uploadFile(formData, token);
      const imageIdentifier = uploaded.fileId || uploaded.id;
      if (!imageIdentifier)
        throw new Error('Upload returned no file identifier');

      const updated = await updateSiteContent(
        item.id,
        item.contentText || '',
        imageIdentifier,
        token
      );
      setItems(current =>
        current.map(entry => (entry.id === item.id ? updated : entry))
      );
      updateDraft(item.id, 'imageIdentifier', imageIdentifier);

      if (oldImageIdentifier && !oldImageIdentifier.startsWith('http')) {
        try {
          await deleteFile(oldImageIdentifier, {
            deletedBy: user?.sub || 'site-content',
            token,
          });
        } catch {
          // Preserve the successful replacement if an old/shared object is not deletable.
        }
      }
    } catch (uploadError) {
      setError('imageSaveError');
    } finally {
      setSavingId(null);
    }
  };

  const handleImageDelete = async item => {
    if (!item.imageIdentifier) return;
    try {
      setSavingId(item.id);
      const token = await getToken();
      const updated = await updateSiteContent(
        item.id,
        item.contentText || '',
        null,
        token
      );
      setItems(current =>
        current.map(entry => (entry.id === item.id ? updated : entry))
      );
      updateDraft(item.id, 'imageIdentifier', '');
      if (!item.imageIdentifier.startsWith('http')) {
        try {
          await deleteFile(item.imageIdentifier, {
            deletedBy: user?.sub || 'site-content',
            token,
          });
        } catch {
          // The database reference is removed even when the object is already missing.
        }
      }
    } catch (deleteError) {
      setError('imageDeleteError');
    } finally {
      setSavingId(null);
    }
  };

  const handleCreateText = async () => {
    if (!selectedGroup || !newPageKey.trim()) return;
    const sanitizedPageKey = sanitizeEditorText(newPageKey).replace(
      /[^a-zA-Z0-9_.-]/g,
      ''
    );
    if (!sanitizedPageKey) {
      setError('invalidKey');
      return;
    }
    try {
      const token = await getToken();
      const created = await createSiteContent(
        {
          pageGroup: selectedGroup,
          pageKey: sanitizedPageKey,
          language,
          contentText: sanitizeEditorText(newContentText),
          imageIdentifier: null,
          sortOrder: items.length,
        },
        token
      );
      setItems(current => [...current, created]);
      setDrafts(current => ({
        ...current,
        [created.id]: {
          contentText: created.contentText || '',
          imageIdentifier: '',
        },
      }));
      setNewPageKey('');
      setNewContentText('');
    } catch (createError) {
      setError('createError');
    }
  };

  const handleDeleteText = async item => {
    try {
      const token = await getToken();
      await deleteSiteContent(item.id, token);
      setItems(current => current.filter(entry => entry.id !== item.id));
      setDrafts(current => {
        const next = { ...current };
        delete next[item.id];
        return next;
      });
    } catch (deleteError) {
      setError('deleteError');
    }
  };

  const handleDeleteHouse = async house => {
    try {
      const token = await getToken();
      if (
        house.imageIdentifier &&
        !String(house.imageIdentifier).startsWith('http')
      ) {
        try {
          await deleteFile(house.imageIdentifier, {
            deletedBy: user?.sub || 'house-editor',
            token,
          });
        } catch {
          // Continue deleting the house record if the object is already missing.
        }
      }
      await deleteHouse(house.houseIdentifier, token);
      setHouses(current =>
        current.filter(item => item.houseIdentifier !== house.houseIdentifier)
      );
    } catch (deleteError) {
      setError('deleteError');
    }
  };

  const saveItem = async item => {
    const draft = drafts[item.id] || {};
    try {
      setSavingId(item.id);
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const updated = await updateSiteContent(
        item.id,
        sanitizeEditorText(draft.contentText),
        draft.imageIdentifier || null,
        token
      );
      setItems(current =>
        current.map(entry => (entry.id === item.id ? updated : entry))
      );
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
            value={selectedPage}
            onChange={event => {
              const page = event.target.value;
              setSelectedPage(page);
              if (page !== 'houses') setSelectedGroup(page);
            }}
          >
            <option value="houses">{t('housesPage', 'Houses')}</option>
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
          {t(error, 'Unable to process site content.')}
        </p>
      )}

      {selectedPage === 'houses' ? (
        <div className="my-site-group">
          <div className="my-site-section-heading">
            <div>
              <p className="my-site-kicker">
                {t('housesKicker', 'Project content')}
              </p>
              <h2>{t('housesTitle', 'Houses')}</h2>
            </div>
            <button
              type="button"
              className="my-site-modal-primary"
              onClick={() => setHouseModal({ house: null })}
            >
              {t('addHouse', 'Add house')}
            </button>
          </div>
          {houses.length === 0 ? (
            <p className="my-site-empty">
              {t('noHouses', 'No houses have been added yet.')}
            </p>
          ) : (
            <div className="my-site-house-grid">
              {houses.map(house => (
                <article
                  className="my-site-house-card"
                  key={house.houseIdentifier}
                >
                  {house.imageIdentifier ? (
                    <img
                      src={getHouseImageUrl(house.imageIdentifier)}
                      alt={house.houseName}
                      onError={event => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = '/fallback.jpg';
                      }}
                    />
                  ) : (
                    <div className="my-site-house-placeholder">
                      {t('noImage', 'No image')}
                    </div>
                  )}
                  <div className="my-site-house-card-content">
                    <span>
                      {projects.find(
                        project =>
                          project.projectIdentifier === house.projectIdentifier
                      )?.projectName || house.projectIdentifier}
                    </span>
                    <h3>{house.houseName}</h3>
                    <div className="my-site-house-actions">
                      <button
                        type="button"
                        onClick={() => setHouseModal({ house })}
                      >
                        {t('editHouse', 'Edit')}
                      </button>
                      <button
                        type="button"
                        className="danger"
                        onClick={() => handleDeleteHouse(house)}
                      >
                        {t('deleteHouse', 'Delete')}
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      ) : Object.keys(groupedItems).length === 0 ? (
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
                      <h3>{getContentLabel(item.pageKey, t)}</h3>
                      <span>{language.toUpperCase()}</span>
                    </div>
                    <p className="my-site-item-preview">
                      {draft.contentText || t('noText', 'No text yet')}
                    </p>
                    {draft.imageIdentifier && (
                      <SiteImagePreview
                        key={draft.imageIdentifier || item.id}
                        imageIdentifier={draft.imageIdentifier}
                      />
                    )}
                    <button type="button" onClick={() => setContentModal(item)}>
                      {t('editContent', 'Edit content')}
                    </button>
                  </article>
                );
              })}
              <div className="my-site-new-item">
                <input
                  placeholder={t('newKey', 'New content key')}
                  value={newPageKey}
                  onChange={event => setNewPageKey(event.target.value)}
                />
                <textarea
                  placeholder={t('newText', 'New content text')}
                  value={newContentText}
                  onChange={event => setNewContentText(event.target.value)}
                  rows={3}
                />
                <button type="button" onClick={handleCreateText}>
                  {t('createText', 'Create text')}
                </button>
              </div>
            </section>
          ))}
        </div>
      )}
      {houseModal && (
        <HouseEditorModal
          house={houseModal.house}
          projects={projects}
          onClose={() => setHouseModal(null)}
          onSaved={saved => {
            setHouses(current => {
              const exists = current.some(
                item => item.houseIdentifier === saved.houseIdentifier
              );
              return exists
                ? current.map(item =>
                    item.houseIdentifier === saved.houseIdentifier
                      ? saved
                      : item
                  )
                : [...current, saved];
            });
            setHouseModal(null);
          }}
        />
      )}
      {contentModal && (
        <div
          className="my-site-modal-backdrop"
          role="presentation"
          onMouseDown={event =>
            event.target === event.currentTarget && setContentModal(null)
          }
        >
          <div className="my-site-modal">
            <div className="my-site-modal-header">
              <div>
                <p className="my-site-kicker">
                  {groupedItems[contentModal.pageGroup]?.length
                    ? contentModal.pageGroup
                    : 'Page content'}
                </p>
                <h2>{getContentLabel(contentModal.pageKey, t)}</h2>
              </div>
              <button
                type="button"
                className="my-site-modal-close"
                onClick={() => setContentModal(null)}
                aria-label={t('close', 'Close')}
              >
                ×
              </button>
            </div>
            <label>
              {t('textLabel', 'Text')}
              <textarea
                value={drafts[contentModal.id]?.contentText || ''}
                onChange={event =>
                  updateDraft(
                    contentModal.id,
                    'contentText',
                    event.target.value
                  )
                }
                rows={7}
              />
            </label>
            <label>
              {t('imageLabel', 'Image')}
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp"
                onChange={event =>
                  handleImageUpload(contentModal, event.target.files?.[0])
                }
                disabled={savingId === contentModal.id}
              />
            </label>
            <SiteImagePreview
              imageIdentifier={drafts[contentModal.id]?.imageIdentifier || ''}
            />
            {drafts[contentModal.id]?.imageIdentifier && (
              <button
                type="button"
                className="my-site-modal-link danger"
                onClick={() => handleImageDelete(contentModal)}
                disabled={savingId === contentModal.id}
              >
                {t('deleteImage', 'Delete image')}
              </button>
            )}
            <div className="my-site-modal-actions">
              <button
                type="button"
                className="my-site-modal-secondary"
                onClick={() => setContentModal(null)}
              >
                {t('cancel', 'Cancel')}
              </button>
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={async () => {
                  await saveItem(contentModal);
                  setContentModal(null);
                }}
                disabled={savingId === contentModal.id}
              >
                {t('save', 'Save changes')}
              </button>
              <button
                type="button"
                className="my-site-modal-link danger"
                onClick={async () => {
                  await handleDeleteText(contentModal);
                  setContentModal(null);
                }}
              >
                {t('deleteText', 'Delete text')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MySitePage;
