import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import PropTypes from 'prop-types';
import { usePageTranslations } from '../hooks/usePageTranslations';
import {
  createSiteContent,
  fetchSiteContent,
  updateSiteContent,
} from '../features/siteContent/api/siteContentApi';
import { fetchPageTranslations } from '../utils/translationApi';
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
  bannerImage: 'fields.bannerImage',
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

const EDITABLE_TEXT_KEYS = {
  contact: {
    namespace: 'contact',
    keys: [
      'hero.eyebrow',
      'hero.title',
      'hero.subtitle',
      'contactInfo.title',
      'contactInfo.companyName',
      'contactInfo.addressLine1',
      'contactInfo.addressLine2',
      'contactInfo.addressLine3',
      'contactInfo.directions',
      'contactInfo.phoneLabel',
      'contactInfo.phone',
      'contactInfo.emailLabel',
      'contactInfo.email',
      'contactInfo.rbqLabel',
      'contactInfo.rbqLicense',
      'contactInfo.rbqWebsite',
      'contactInfo.gcqLabel',
      'contactInfo.gcqAccreditation',
      'contactInfo.gcqWebsite',
    ],
  },
  renovations: {
    namespace: 'renovations',
    keys: [
      'hero.label',
      'hero.heading',
      'hero.lede',
      'intro.kicker',
      'intro.title',
      'intro.description',
      'features.kicker',
      'features.title',
      'features.feature1',
      'features.feature1Description',
      'features.feature2',
      'features.feature2Description',
      'features.feature3',
      'features.feature3Description',
      'features.feature4',
      'features.feature4Description',
      'features.feature5',
      'features.feature5Description',
      'features.feature6',
      'features.feature6Description',
      'cta.kicker',
      'cta.title',
      'cta.subtitle',
      'cta.button',
    ],
  },
  'project-management': {
    namespace: 'projectManagement',
    keys: [
      'hero.title',
      'hero.subtitle',
      'intro.heading.title',
      'intro.tagline',
      'intro.paragraph',
      'advantages.heading',
      'advantages.item1',
      'advantages.item2',
      'advantages.item3',
      'advantages.item4',
      'advantages.item5',
      'advantages.item6',
      'advantages.item7',
      'advantages.pricing',
      'advantages.contactLink',
    ],
  },
  realizations: {
    namespace: 'realizations',
    keys: [
      'hero.title',
      'hero.subtitle',
      'intro.title',
      'intro.description',
      'intro.portfolioText',
      'gallery.title',
      'gallery.previousAriaLabel',
      'gallery.nextAriaLabel',
      'gallery.imageAlt',
      'gallery.noResults',
      'gallery.contactLink',
      'galleryGrid.kicker',
      'galleryGrid.title',
      ...Array.from({ length: 9 }, (_, index) => [
        `galleryItem.${index + 1}.location`,
        `galleryItem.${index + 1}.description`,
      ]).flat(),
    ],
  },
  'residential-projects': {
    namespace: 'residentialProjects',
    keys: [
      'hero.kicker',
      'hero.title',
      'hero.subtitle',
      'search.placeholder',
      'gallery.title',
      'pagination.prev',
      'pagination.next',
    ],
  },
};

const EDITOR_PAGE_ORDER = {
  home: [
    'hero.label',
    'hero.title',
    'hero.subtitle',
    'think.title',
    'think.subtitle',
    'think.link',
    'build.title',
    'build.subtitle',
    'build.link',
    'live.title',
    'live.subtitle',
    'live.link',
    'features.kicker',
    'features.title',
    'features.intro',
    'features.feature1',
    'features.feature1Description',
    'features.feature2',
    'features.feature2Description',
    'features.feature3',
    'features.feature3Description',
    'features.feature4',
    'features.feature4Description',
    'features.feature5',
    'features.feature5Description',
    'features.feature6',
    'features.feature6Description',
    'portfolio.title',
    'portfolio.foresta',
    'portfolio.forestaSubtitle',
    'portfolio.panorama',
    'portfolio.panoramaSubtitle',
    'portfolio.management',
    'portfolio.managementSubtitle',
    'media.hero',
    'media.live',
    'media.build',
    'media.think',
    'media.foresta',
    'media.panorama',
  ],
  renovations: EDITABLE_TEXT_KEYS.renovations.keys,
  'project-management': EDITABLE_TEXT_KEYS['project-management'].keys,
  realizations: EDITABLE_TEXT_KEYS.realizations.keys,
  contact: EDITABLE_TEXT_KEYS.contact.keys,
  footer: [
    'information',
    'phone',
    'email',
    'accreditations',
    'rbqLabel',
    'rbq',
    'rbqUrl',
    'gcrLabel',
    'gcr',
    'gcrUrl',
    'copyright',
  ],
};

const sortEditorItems = (pageGroup, contentItems) => {
  const order = EDITOR_PAGE_ORDER[pageGroup] || [];
  const positions = new Map(order.map((key, index) => [key, index]));
  return [...contentItems].sort((left, right) => {
    const leftPosition = positions.get(left.pageKey) ?? Number.MAX_SAFE_INTEGER;
    const rightPosition =
      positions.get(right.pageKey) ?? Number.MAX_SAFE_INTEGER;
    return (
      leftPosition - rightPosition ||
      (left.sortOrder || 0) - (right.sortOrder || 0)
    );
  });
};

const PUBLIC_IMAGE_SLOTS = {
  'project-management': [
    [
      'galleryImage.1',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/BromontFrl.JPEG',
    ],
    [
      'galleryImage.2',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/shefford1.JPG',
    ],
    [
      'galleryImage.3',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/shefford2.JPG',
    ],
    [
      'galleryImage.4',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/shefford3.JPG',
    ],
    [
      'galleryImage.5',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-16/st-hilaire.png',
    ],
  ],
  realizations: [
    [
      'galleryImage.1',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/2026-02-19/3262-1.jpeg',
    ],
    [
      'galleryImage.2',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_1.jpeg',
    ],
    [
      'galleryImage.3',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_2.jpeg',
    ],
    [
      'galleryImage.4',
      'https://lcdi-storage.tor1.cdn.digitaloceanspaces.com/photos/global/Carousel/380_St_Bas_3.jpeg',
    ],
    ['galleryImage.5', '1242c6c5-6a3d-4ca0-bac0-06d25bebb0bd'],
    ['galleryImage.6', '1634e9ee-2680-41d1-b28a-47353f842d9c'],
    ['galleryImage.7', '1659ff85-b160-4111-b419-84834eb4375a'],
    ['galleryImage.8', '1681b3d5-8f0a-4daf-9590-53a1ce37cf20'],
    ...Array.from({ length: 9 }, (_, index) => [
      `galleryItem.${index + 1}.image`,
      '',
    ]),
  ],
};

const HOME_CONTENT_KEYS = new Set([
  'hero.label',
  'hero.title',
  'hero.subtitle',
  'think.title',
  'think.subtitle',
  'think.link',
  'build.title',
  'build.subtitle',
  'build.link',
  'live.title',
  'live.subtitle',
  'live.link',
  'features.kicker',
  'features.title',
  'features.intro',
  'features.feature1',
  'features.feature1Description',
  'features.feature2',
  'features.feature2Description',
  'features.feature3',
  'features.feature3Description',
  'features.feature4',
  'features.feature4Description',
  'features.feature5',
  'features.feature5Description',
  'features.feature6',
  'features.feature6Description',
  'portfolio.title',
  'portfolio.foresta',
  'portfolio.forestaSubtitle',
  'portfolio.panorama',
  'portfolio.panoramaSubtitle',
  'portfolio.management',
  'portfolio.managementSubtitle',
]);

const flattenContent = (value, prefix = '') => {
  if (typeof value === 'string' && value.trim()) return [[prefix, value]];
  if (!value || typeof value !== 'object' || Array.isArray(value)) return [];
  return Object.entries(value).flatMap(([key, child]) =>
    flattenContent(child, prefix ? `${prefix}.${key}` : key)
  );
};

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

  const createFallbackTextItems = async (pageGroup, selectedLanguage) => {
    const configuration = EDITABLE_TEXT_KEYS[pageGroup];
    const namespace = configuration?.namespace || pageGroup;
    const resource = await fetchPageTranslations(namespace, selectedLanguage);
    const configuredKeys = configuration?.keys || [];
    const textEntries = flattenContent(resource).filter(([pageKey]) =>
      pageGroup === 'home'
        ? HOME_CONTENT_KEYS.has(pageKey)
        : configuredKeys.includes(pageKey)
    );
    const textEntryMap = new Map(textEntries);
    const completeTextEntries =
      pageGroup === 'home'
        ? textEntries
        : configuredKeys.map(pageKey => [
            pageKey,
            textEntryMap.get(pageKey) || '',
          ]);
    const textItems = completeTextEntries.map(
      ([pageKey, contentText], index) => ({
        id: `fallback-${pageGroup}-${pageKey}`,
        pageGroup,
        pageKey,
        contentText,
        imageIdentifier: '',
        sortOrder: index,
        fallbackContent: true,
      })
    );
    const imageItems = (PUBLIC_IMAGE_SLOTS[pageGroup] || []).map(
      ([pageKey, imageIdentifier], index) => ({
        id: `fallback-${pageGroup}-${pageKey}`,
        pageGroup,
        pageKey,
        contentText: '',
        imageIdentifier,
        sortOrder: textItems.length + index,
        fallbackContent: true,
      })
    );
    return [...textItems, ...imageItems];
  };

  const openContentPage = async (pageGroup, selectedLanguage = language) => {
    try {
      setError(null);
      setSavingId('page');
      const token = await getToken();
      const effectivePageGroup =
        selectedSubpage === 'lots' && projectIdentifier
          ? `lots-${projectIdentifier}`
          : pageGroup;
      let data = await fetchSiteContent(
        selectedLanguage,
        token,
        effectivePageGroup
      );
      if (pageGroup === 'home') {
        data = (Array.isArray(data) ? data : []).filter(
          item =>
            HOME_CONTENT_KEYS.has(item.pageKey) ||
            [
              'media.hero',
              'media.live',
              'media.build',
              'media.think',
              'media.foresta',
              'media.panorama',
            ].includes(item.pageKey)
        );
      }
      if (selectedSubpage === 'lots') {
        data = (Array.isArray(data) ? data : []).filter(item =>
          ['title', 'subtitle', 'preTitle'].includes(item.pageKey)
        );
      }
      if (selectedSubpage === 'living-environment' && projectIdentifier) {
        const response = await fetch(
          `${import.meta.env.VITE_API_BASE || '/api/v1'}/projects/${projectIdentifier}/living-environment?lang=${selectedLanguage}`
        );
        if (response.ok) {
          const living = await response.json();
          data = [
            ['headerTitle', living.headerTitle],
            ['headerSubtitle', living.headerSubtitle],
            ['headerSubtitleLast', living.headerSubtitleLast],
            ['headerTagline', living.headerTagline],
            ['descriptionText', living.descriptionText],
            ['proximityTitle', living.proximityTitle],
            ['footerText', living.footerText],
            ...(living.amenities || []).map((amenity, index) => [
              `amenity.${amenity.key || index + 1}`,
              amenity.label,
            ]),
          ]
            .filter(
              ([pageKey, value]) =>
                value || pageKey === 'heroImage' || pageKey === 'bannerImage'
            )
            .map(([pageKey, value], index) => ({
              id: `living-environment-${projectIdentifier}-${pageKey}`,
              pageGroup,
              pageKey,
              contentText: String(value),
              imageIdentifier: '',
              sortOrder: index,
              projectLivingEnvironmentContent: true,
              projectIdentifier,
            }));
        }
      }
      if (
        selectedSubpage === 'lots' &&
        projectIdentifier &&
        (!Array.isArray(data) || data.length === 0)
      ) {
        const project = projects.find(
          item => item.projectIdentifier === projectIdentifier
        );
        const lotsResource = i18n.getResource(selectedLanguage, 'lots') || {};
        const isPanorama = projectIdentifier === 'proj-002-panorama';
        const fallbackValues = isPanorama
          ? {
              title: lotsResource.panorama?.headerTitle,
              subtitle: lotsResource.panorama?.tagline,
              preTitle: lotsResource.panorama?.preTitle,
            }
          : {
              title:
                `${lotsResource.header?.projectLots || '{{name}}'}`.replace(
                  '{{name}}',
                  project?.projectName || projectIdentifier
                ),
              subtitle: lotsResource.foresta?.peaceTagline,
              preTitle: lotsResource.foresta?.exclusiveCount,
            };
        data = Object.entries(fallbackValues)
          .filter(([, value]) => value)
          .map(([pageKey, contentText], index) => ({
            id: `fallback-${effectivePageGroup}-${pageKey}`,
            pageGroup: effectivePageGroup,
            pageKey,
            contentText,
            imageIdentifier: '',
            sortOrder: index,
            fallbackContent: true,
            projectIdentifier,
          }));
      }
      const fallbackItems = selectedSubpage
        ? []
        : await createFallbackTextItems(pageGroup, selectedLanguage);
      const existingKeys = new Set(
        (Array.isArray(data) ? data : []).map(item => item.pageKey)
      );
      data = [
        ...(Array.isArray(data) ? data : []),
        ...fallbackItems.filter(item => !existingKeys.has(item.pageKey)),
      ];
      if (
        (!Array.isArray(data) || data.length === 0) &&
        selectedSubpage &&
        selectedSubpage !== 'houses'
      ) {
        data = [
          {
            id: `fallback-${pageGroup}-content`,
            pageGroup,
            pageKey: 'content',
            contentText: '',
            imageIdentifier: '',
            sortOrder: 0,
            fallbackContent: true,
            projectIdentifier,
          },
        ];
      }
      if (selectedSubpage === 'project-overview' && projectIdentifier) {
        const response = await fetch(
          `${import.meta.env.VITE_API_BASE || import.meta.env.VITE_API_BASE_URL || '/api/v1'}/projects/${projectIdentifier}/overview`
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
            ['heroImage', overview.imageIdentifier || ''],
            ['bannerImage', overview.bannerImageIdentifier || ''],
            ...(overview.galleryImages || []).map((image, index) => [
              `galleryImage.${index + 1}`,
              image.imageIdentifier,
            ]),
          ]
            .filter(
              ([pageKey, value]) =>
                value || pageKey === 'heroImage' || pageKey === 'bannerImage'
            )
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
      const orderedData = selectedSubpage
        ? Array.isArray(data)
          ? data
          : []
        : sortEditorItems(pageGroup, Array.isArray(data) ? data : []);
      setItems(orderedData);
      setDrafts(
        Object.fromEntries(
          orderedData.map(item => [
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
      formData.append(
        'projectId',
        item.projectOverviewContent ? item.projectIdentifier : 'site-content'
      );
      formData.append('uploadedBy', user?.sub || 'site-content');
      formData.append('uploaderRole', 'OWNER');

      const uploaded = await uploadFile(formData, token);
      const imageIdentifier = uploaded.fileId || uploaded.id;
      if (!imageIdentifier)
        throw new Error('Upload returned no file identifier');

      if (item.projectOverviewContent) {
        const imageField =
          item.pageKey === 'bannerImage'
            ? 'bannerImageIdentifier'
            : 'imageIdentifier';
        await updateProjectOverview(
          item.projectIdentifier,
          { [imageField]: imageIdentifier },
          token
        );
        updateDraft(item.id, 'imageIdentifier', imageIdentifier);
        setSavingId(null);
        return;
      }
      const updated = item.fallbackContent
        ? await createSiteContent(
            {
              pageGroup: item.pageGroup,
              pageKey: item.pageKey,
              language: editorLanguage,
              contentText:
                drafts[item.id]?.contentText ?? item.contentText ?? '',
              imageIdentifier,
              sortOrder: item.sortOrder || 0,
            },
            token
          )
        : await updateSiteContent(
            item.id,
            drafts[item.id]?.contentText ?? item.contentText ?? '',
            imageIdentifier,
            token
          );
      setItems(current =>
        current.map(entry =>
          entry.id === item.id ? { ...updated, fallbackContent: false } : entry
        )
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
        const imageField =
          item.pageKey === 'bannerImage'
            ? 'bannerImageIdentifier'
            : 'imageIdentifier';
        await updateProjectOverview(
          item.projectIdentifier,
          { [imageField]: '' },
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
      const updated = item.fallbackContent
        ? await createSiteContent(
            {
              pageGroup: item.pageGroup,
              pageKey: item.pageKey,
              language: editorLanguage,
              contentText: sanitizeEditorText(draft.contentText),
              imageIdentifier: draft.imageIdentifier || null,
              sortOrder: item.sortOrder || 0,
            },
            token
          )
        : await updateSiteContent(
            item.id,
            sanitizeEditorText(draft.contentText),
            draft.imageIdentifier || null,
            token
          );
      setItems(current =>
        current.map(entry =>
          entry.id === item.id ? { ...updated, fallbackContent: false } : entry
        )
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
      `${import.meta.env.VITE_API_BASE || import.meta.env.VITE_API_BASE_URL || '/api/v1'}/projects/${projectIdentifier}/overview`,
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
    const bannerImage = items.find(item => item.pageKey === 'bannerImage');
    if (bannerImage?.imageIdentifier) {
      values.bannerImageIdentifier = bannerImage.imageIdentifier;
    }
    const token = await getToken();
    await updateProjectOverview(projectIdentifier, values, token);
  };

  const saveLivingEnvironment = async () => {
    const values = {};
    const amenities = [];
    items.forEach(item => {
      const value = sanitizeEditorText(drafts[item.id]?.contentText);
      if (item.pageKey === 'headerTitle') values.headerTitle = value;
      if (item.pageKey === 'headerSubtitle') values.headerSubtitle = value;
      if (item.pageKey === 'headerSubtitleLast')
        values.headerSubtitleLast = value;
      if (item.pageKey === 'headerTagline') values.headerTagline = value;
      if (item.pageKey === 'descriptionText') values.descriptionText = value;
      if (item.pageKey === 'proximityTitle') values.proximityTitle = value;
      if (item.pageKey === 'footerText') values.footerText = value;
      if (item.pageKey.startsWith('amenity.')) {
        amenities.push({
          key: item.pageKey.slice('amenity.'.length),
          label: value,
          displayOrder: item.sortOrder,
        });
      }
    });
    const token = await getToken();
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE || '/api/v1'}/projects/${projectIdentifier}/living-environment?lang=${editorLanguage}`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ...values, amenities }),
      }
    );
    if (!response.ok) throw new Error('Living environment update failed');
  };

  const availableImageCount = items.filter(item => {
    const draft = drafts[item.id];
    return Boolean(item.imageIdentifier || draft?.imageIdentifier);
  }).length;

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
            className="my-site-back-button"
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
            className="my-site-back-button"
            onClick={() => navigate(`/owner/my-site/${selectedPath}`)}
          >
            {t('backToSubpages', 'Back to project pages')}
          </button>
          <article className="my-site-page-card">
            <p className="my-site-kicker">{projectIdentifier}</p>
            <h2>{getPageLabel(selectedSubpage)}</h2>
            {selectedSubpage === 'houses' ? (
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={() => setHouseManagerOpen(true)}
              >
                {t('modify', 'Modify')}
              </button>
            ) : (
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={() => openContentPage(selectedSubpage)}
              >
                {t('modify', 'Modify')}
              </button>
            )}
          </article>
        </section>
      )}
      {selectedPath &&
        selectedPath !== 'residential-projects' &&
        !selectedSubpage && (
          <section className="my-site-subpage-panel">
            <button
              type="button"
              className="my-site-back-button"
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
                  {t('housesKicker', 'Project content')}
                </p>
                <h2>{t('housesTitle', 'House models')}</h2>
              </div>
              <button
                type="button"
                className="my-site-modal-primary"
                onClick={() =>
                  setHouseModal({ house: null, projectIdentifier })
                }
              >
                {t('addHouse', 'Add house')}
              </button>
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
                {houses.filter(
                  house => house.projectIdentifier === projectIdentifier
                ).length === 0 ? (
                  <p className="my-site-empty">
                    {t('noHouses', 'No houses have been added yet.')}
                  </p>
                ) : (
                  <div className="my-site-house-grid">
                    {houses
                      .filter(
                        house => house.projectIdentifier === projectIdentifier
                      )
                      .map(house => (
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
          initialProjectIdentifier={houseModal.projectIdentifier}
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
            {error && (
              <p className="my-site-error" role="alert">
                {t(error, 'Unable to process site content.')}
              </p>
            )}
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
            <p className="my-site-image-count" role="status">
              {t('imagesAvailable', 'Images available')}: {availableImageCount}.{' '}
              {t(
                'imageSlotsNote',
                'Only existing image slots can be replaced; no new image slots can be added here.'
              )}
            </p>
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
                const hasExistingImage = Boolean(
                  item.imageIdentifier || draft.imageIdentifier
                );
                const isImageSlot =
                  item.pageKey.toLowerCase().includes('image') &&
                  (item.projectOverviewContent ||
                    PUBLIC_IMAGE_SLOTS[item.pageGroup]);
                return (
                  <div className="my-site-modal-item" key={item.id}>
                    {!isImageSlot && (
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
                    )}
                    {(hasExistingImage || isImageSlot) && (
                      <>
                        <label className="my-site-image-editor">
                          <span>
                            {item.projectOverviewContent
                              ? getContentLabel(item.pageKey, t)
                              : t('imageLabel', 'Image')}{' '}
                            <small>
                              {t(
                                'imagePositionNote',
                                item.projectOverviewContent
                                  ? 'Choose an image for this slot'
                                  : 'Existing image slot for this content'
                              )}
                            </small>
                          </span>
                          <input
                            type="file"
                            accept="image/png,image/jpeg,image/webp"
                            onChange={event =>
                              handleImageUpload(item, event.target.files?.[0])
                            }
                            disabled={savingId === item.id}
                          />
                        </label>
                        {hasExistingImage && (
                          <SiteImagePreview
                            imageIdentifier={draft.imageIdentifier || ''}
                          />
                        )}
                        {draft.imageIdentifier &&
                          !item.projectOverviewContent && (
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
                      </>
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
                  setSavingId('page');
                  try {
                    if (items.some(item => item.projectOverviewContent)) {
                      await saveProjectOverview();
                    } else if (
                      items.some(item => item.projectLivingEnvironmentContent)
                    ) {
                      await saveLivingEnvironment();
                    } else {
                      for (const item of items) await saveItem(item);
                    }
                    setContentModal(null);
                  } catch {
                    setError('saveError');
                  } finally {
                    setSavingId(null);
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
