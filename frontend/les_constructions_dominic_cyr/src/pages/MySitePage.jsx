import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import {
  fetchSiteContent,
  updateSiteContent,
} from '../features/siteContent/api/siteContentApi';
import { deleteFile, uploadFile } from '../features/files/api/filesApi';
import { fetchHouses, deleteHouse } from '../features/houses/api/housesApi';
import HouseEditorModal from '../features/houses/components/HouseEditorModal';
import { getFileUrl } from '../utils/filesService';
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
  heroTitle: 'fields.heroTitle',
  heroSubtitle: 'fields.heroSubtitle',
  heroDescription: 'fields.heroDescription',
  overviewSectionTitle: 'fields.overviewTitle',
  overviewSectionContent: 'fields.overviewContent',
  locationSectionTitle: 'fields.locationTitle',
  locationDescription: 'fields.locationDescription',
  locationAddress: 'fields.locationAddress',
  heroImage: 'fields.heroImage',
};

const EDITORIAL_PAGES = [
  { key: 'home', label: 'homePage' },
  { key: 'residential-projects', label: 'residentialProjectsPage' },
  { key: 'renovations', label: 'renovationsPage' },
  { key: 'project-management', label: 'projectManagementPage' },
  { key: 'realizations', label: 'realizationsPage' },
  { key: 'contact', label: 'contactPage' },
  { key: 'footer', label: 'footerPage' },
];

const PROJECT_PAGE_GROUPS = [
  { key: 'project-overview', label: 'projectOverviewPage' },
  { key: 'living-environment', label: 'livingEnvironmentPage' },
  { key: 'houses', label: 'housesPage' },
  { key: 'lots', label: 'lotsPage' },
  { key: 'project-footer', label: 'projectFooterPage' },
];

const sanitizeEditorText = value =>
  String(value ?? '')
    .replace(/\0/g, '')
    .trim()
    .slice(0, 100000);

const getHouseImageUrl = imageIdentifier => {
  if (!imageIdentifier) return '';
  return getFileUrl(imageIdentifier);
};

const getContentLabel = (pageKey, translate) => {
  const fallback = pageKey
    .split(/[._-]+|(?=[A-Z])/)
    .filter(Boolean)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
  return translate(FIELD_LABEL_KEYS[pageKey] || pageKey, fallback);
};

const SiteImagePreview = ({ imageIdentifier }) => {
  const [attempts, setAttempts] = useState(0);
  const [failed, setFailed] = useState(false);
  const [source, setSource] = useState(() => {
    if (!imageIdentifier) return '/fallback.jpg';
    return getFileUrl(imageIdentifier);
  });

  useEffect(() => {
    setAttempts(0);
    setFailed(false);
    if (!imageIdentifier) {
      setSource('/fallback.jpg');
      return;
    }
    if (String(imageIdentifier).startsWith('http')) {
      setSource(imageIdentifier);
      return;
    }
    setSource(getFileUrl(imageIdentifier));
  }, [imageIdentifier]);

  return (
    <img
      className="my-site-image-preview"
      src={failed || attempts >= 3 ? '/fallback.jpg' : source}
      alt=""
      onError={() => {
        if (failed || attempts >= 3) return;
        const nextAttempt = attempts + 1;
        setAttempts(nextAttempt);
        if (nextAttempt < 3) {
          setSource(`${source.split('?')[0]}?retry=${nextAttempt}`);
        } else {
          setFailed(true);
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
  const navigate = useNavigate();
  const params = useParams();
  const sitePath = params['*'] || '';
  const [selectedPath, projectIdentifier, selectedSubpage] = sitePath
    .split('/')
    .filter(Boolean);
  const [language, setLanguage] = useState(
    i18n.language?.startsWith('fr') ? 'fr' : 'en'
  );
  const [groups, setGroups] = useState([]);
  const [items, setItems] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);
  const [error, setError] = useState(null);
  const [projects, setProjects] = useState([]);
  const [houses, setHouses] = useState([]);
  const [houseModal, setHouseModal] = useState(null);
  const [houseManagerOpen, setHouseManagerOpen] = useState(false);
  const [contentModal, setContentModal] = useState(null);
  const [editorLanguage, setEditorLanguage] = useState(language);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const loadGroups = async () => {
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const [projectResponse, houseData] = await Promise.all([
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
      const nextGroups = EDITORIAL_PAGES.map(page => page.key);
      return nextGroups;
    };

    loadGroups()
      .then(data => {
        if (cancelled) return;
        setGroups(Array.isArray(data) ? data : []);
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

  const getPageLabel = pageKey => {
    const configured = [...EDITORIAL_PAGES, ...PROJECT_PAGE_GROUPS].find(
      page => page.key === pageKey
    );
    const fallback = pageKey
      .split('-')
      .map(part => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
    return t(configured?.label || pageKey, fallback);
  };

  const openContentPage = async (pageGroup, selectedLanguage = language) => {
    try {
      setError(null);
      setSavingId('page');
      const token = await getToken();
      let data = await fetchSiteContent(selectedLanguage, token, pageGroup);
      if (selectedSubpage === 'project-overview' && projectIdentifier) {
        const response = await fetch(
          `${import.meta.env.VITE_API_BASE || '/api/v1'}/projects/${projectIdentifier}/overview`
        );
        if (response.ok) {
          const overview = await response.json();
          data = [
            ['heroTitle', overview.heroTitle || overview.projectName],
            ['heroSubtitle', overview.heroSubtitle],
            ['heroDescription', overview.heroDescription],
            ['overviewSectionTitle', overview.overviewSectionTitle],
            ['overviewSectionContent', overview.overviewSectionContent],
            ['locationSectionTitle', overview.locationSectionTitle],
            ['locationDescription', overview.locationDescription],
            ['locationAddress', overview.locationAddress],
            ['heroImage', overview.imageIdentifier],
            ...(overview.galleryImages || []).map((image, index) => [
              `galleryImage.${index + 1}`,
              image.imageIdentifier,
            ]),
          ]
            .filter(([, value]) => value)
            .map(([pageKey, value], index) => ({
              id: `project-overview-${pageKey}`,
              pageGroup: pageGroup,
              pageKey,
              contentText: pageKey.toLowerCase().includes('image')
                ? ''
                : String(value),
              imageIdentifier: pageKey.toLowerCase().includes('image')
                ? String(value)
                : '',
              sortOrder: index,
              projectOverviewContent: true,
              projectIdentifier,
            }));
        }
      }
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
      setEditorLanguage(selectedLanguage);
      setContentModal(pageGroup);
    } catch {
      setError('loadError');
    } finally {
      setSavingId(null);
    }
  };

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

      if (item.projectOverviewContent) {
        await updateProjectOverview(
          item.projectIdentifier,
          {
            imageIdentifier,
          },
          token
        );
        updateDraft(item.id, 'imageIdentifier', imageIdentifier);
        setSavingId(null);
        return;
      }
      const updated = await updateSiteContent(
        item.id,
        drafts[item.id]?.contentText ?? item.contentText ?? '',
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
      if (item.projectOverviewContent) {
        await updateProjectOverview(
          item.projectIdentifier,
          { imageIdentifier: '' },
          token
        );
        updateDraft(item.id, 'imageIdentifier', '');
        return;
      }
      const updated = await updateSiteContent(
        item.id,
        drafts[item.id]?.contentText ?? item.contentText ?? '',
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
      throw saveError;
    } finally {
      setSavingId(null);
    }
  };

  const updateProjectOverview = async (projectIdentifier, values, token) => {
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE || '/api/v1'}/projects/${projectIdentifier}/overview`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(values),
      }
    );
    if (!response.ok) throw new Error('Project overview update failed');
    return response.json();
  };

  const saveProjectOverview = async () => {
    const values = {};
    items.forEach(item => {
      const value = drafts[item.id]?.contentText;
      if (!value) return;
      if (item.pageKey === 'heroTitle') values.heroTitle = value;
      if (item.pageKey === 'heroSubtitle') values.heroSubtitle = value;
      if (item.pageKey === 'heroDescription') values.heroDescription = value;
      if (item.pageKey === 'overviewSectionTitle')
        values.overviewSectionTitle = value;
      if (item.pageKey === 'overviewSectionContent')
        values.overviewSectionContent = value;
      if (item.pageKey === 'locationSectionTitle')
        values.locationSectionTitle = value;
      if (item.pageKey === 'locationDescription')
        values.locationDescription = value;
      if (item.pageKey === 'locationAddress') values.locationAddress = value;
    });
    const image = items.find(item => item.pageKey === 'heroImage');
    if (image?.imageIdentifier) values.imageIdentifier = image.imageIdentifier;
    const token = await getToken();
    await updateProjectOverview(projectIdentifier, values, token);
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
      </header>

      {error && (
        <p className="my-site-error">
          {t(error, 'Unable to process site content.')}
        </p>
      )}

      {!selectedPath && (
        <div className="my-site-page-grid">
          {groups.map(group => (
            <article className="my-site-page-card" key={group}>
              <h2>{getPageLabel(group)}</h2>
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={() => navigate(`/owner/my-site/${group}`)}
              >
                {t('modify', 'Modify')}
              </button>
            </article>
          ))}
        </div>
      )}
      {selectedPath === 'residential-projects' && !selectedSubpage && (
        <section className="my-site-subpage-panel">
          <button
            type="button"
            className="my-site-modal-secondary"
            onClick={() => navigate('/owner/my-site')}
          >
            {t('backToPages', 'Back to pages')}
          </button>
          <h2>{getPageLabel('residential-projects')}</h2>
          <div className="my-site-page-grid">
            {projects.map(project => (
              <article
                className="my-site-page-card"
                key={project.projectIdentifier}
              >
                <h3>{project.projectName}</h3>
                <div className="my-site-subpage-actions">
                  {[
                    'project-overview',
                    'living-environment',
                    'houses',
                    'lots',
                  ].map(subpage => (
                    <button
                      type="button"
                      className="my-site-nested-page"
                      key={subpage}
                      onClick={() =>
                        navigate(
                          `/owner/my-site/residential-projects/${project.projectIdentifier}/${subpage}`
                        )
                      }
                    >
                      {getPageLabel(subpage)}
                    </button>
                  ))}
                </div>
              </article>
            ))}
          </div>
        </section>
      )}
      {selectedSubpage && (
        <section className="my-site-subpage-panel">
          <button
            type="button"
            className="my-site-modal-secondary"
            onClick={() => navigate(`/owner/my-site/${selectedPath}`)}
          >
            {t('backToSubpages', 'Back to project pages')}
          </button>
          <article className="my-site-page-card">
            <p className="my-site-kicker">{projectIdentifier}</p>
            <h2>{getPageLabel(selectedSubpage)}</h2>
            <button
              type="button"
              className="my-site-modal-primary"
              onClick={() => openContentPage(selectedSubpage)}
            >
              {t('modify', 'Modify')}
            </button>
          </article>
        </section>
      )}
      {selectedPath &&
        selectedPath !== 'residential-projects' &&
        !selectedSubpage && (
          <section className="my-site-subpage-panel">
            <button
              type="button"
              className="my-site-modal-secondary"
              onClick={() => navigate('/owner/my-site')}
            >
              {t('backToPages', 'Back to pages')}
            </button>
            <article className="my-site-page-card">
              <h2>{getPageLabel(selectedPath)}</h2>
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={() => openContentPage(selectedPath)}
              >
                {t('modify', 'Modify')}
              </button>
            </article>
          </section>
        )}
      {houseManagerOpen && (
        <div
          className="my-site-modal-backdrop"
          role="presentation"
          onMouseDown={event =>
            event.target === event.currentTarget && setHouseManagerOpen(false)
          }
        >
          <div className="my-site-modal">
            <div className="my-site-modal-header">
              <div>
                <p className="my-site-kicker">
                  {t('projectContent', 'Project content')}
                </p>
                <h2>{t('housesTitle', 'House models')}</h2>
              </div>
              <button
                type="button"
                className="my-site-modal-close"
                onClick={() => setHouseManagerOpen(false)}
                aria-label={t('close', 'Close')}
              >
                ×
              </button>
            </div>
            <div className="my-site-group">
              <div className="my-site-section-heading">
                <div>
                  <p className="my-site-kicker">
                    {t('housesKicker', 'Project content')}
                  </p>
                  <h2>{t('housesTitle', 'Houses')}</h2>
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
                                project.projectIdentifier ===
                                house.projectIdentifier
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
            </div>
          </div>
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
                  {t('pageContent', 'Page content')}
                </p>
                <h2>{getPageLabel(contentModal)}</h2>
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
            <label className="my-site-language">
              {t('language', 'Language')}
              <select
                value={editorLanguage}
                onChange={event =>
                  openContentPage(contentModal, event.target.value)
                }
              >
                <option value="en">English</option>
                <option value="fr">Français</option>
              </select>
            </label>
            {items.length === 0 ? (
              <p className="my-site-empty">
                {t(
                  'emptyPage',
                  'No content has been seeded for this page yet.'
                )}
              </p>
            ) : (
              items.map(item => {
                const draft = drafts[item.id] || {};
                return (
                  <div className="my-site-modal-item" key={item.id}>
                    <label>
                      {getContentLabel(item.pageKey, t)}
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
                      {t('imageLabel', 'Image')}
                      <input
                        type="file"
                        accept="image/png,image/jpeg,image/webp"
                        onChange={event =>
                          handleImageUpload(item, event.target.files?.[0])
                        }
                        disabled={savingId === item.id}
                      />
                    </label>
                    <SiteImagePreview
                      imageIdentifier={draft.imageIdentifier || ''}
                    />
                    {draft.imageIdentifier && !item.projectOverviewContent && (
                      <button
                        type="button"
                        className="my-site-modal-link danger"
                        onClick={() =>
                          handleImageDelete({
                            ...item,
                            imageIdentifier: draft.imageIdentifier,
                          })
                        }
                        disabled={savingId === item.id}
                      >
                        {t('deleteImage', 'Delete image')}
                      </button>
                    )}
                  </div>
                );
              })
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
                  try {
                    if (items.some(item => item.projectOverviewContent)) {
                      await saveProjectOverview();
                    } else {
                      for (const item of items) await saveItem(item);
                    }
                    setContentModal(null);
                  } catch {
                    setError('saveError');
                  }
                }}
                disabled={savingId !== null || items.length === 0}
              >
                {t('save', 'Save changes')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MySitePage;
