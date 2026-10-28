import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const SITE_URL = 'https://constructions-dominiccyr.com';
const COMPANY_NAME = 'Les Constructions Dominic Cyr Inc.';
const FORESTA_URL = `${SITE_URL}/projects/proj-001-foresta/overview`;
const SOCIAL_IMAGE_URL = `${SITE_URL}/fallback.jpg`;

const getLanguage = language =>
  language?.toLowerCase().startsWith('fr') ? 'fr' : 'en';

const getMetadata = (pathname, language) => {
  const isFrench = language === 'fr';
  const forestaOverview = pathname === '/projects/proj-001-foresta/overview';
  const contact = pathname === '/contact';
  const projectPageMatch = pathname.match(
    /^\/projects\/([^/]+)\/(overview|living-environment|houses|lots)$/
  );
  const publicPages = {
    '/realizations': isFrench
      ? {
          title: 'Réalisations | Les Constructions Dominic Cyr',
          description:
            'Découvrez les réalisations de Les Constructions Dominic Cyr : construction résidentielle, maisons et rénovations à Granby et en Montérégie.',
        }
      : {
          title: 'Our Realizations | Les Constructions Dominic Cyr',
          description:
            'Explore Les Constructions Dominic Cyr residential construction, home building and renovation projects in Granby and the Montérégie region.',
        },
    '/renovations': isFrench
      ? {
          title: 'Rénovation résidentielle | Les Constructions Dominic Cyr',
          description:
            'Planifiez votre rénovation résidentielle avec Les Constructions Dominic Cyr, de la conception à la réalisation à Granby, Saint-Alphonse-de-Granby et en Montérégie.',
        }
      : {
          title: 'Home Renovations | Les Constructions Dominic Cyr',
          description:
            'Plan your home renovation with Les Constructions Dominic Cyr, from design to completion in Granby, Saint-Alphonse-de-Granby and the Montérégie region.',
        },
  };

  if (publicPages[pathname]) return publicPages[pathname];

  if (projectPageMatch && !forestaOverview) {
    const [, projectIdentifier, page] = projectPageMatch;
    const projectName =
      projectIdentifier === 'proj-001-foresta'
        ? 'Föresta'
        : 'Residential project';
    const pageNames = {
      overview: isFrench ? 'Projet résidentiel' : 'Residential project',
      'living-environment': isFrench ? 'Milieu de vie' : 'Living environment',
      houses: isFrench ? 'Maisons' : 'Homes',
      lots: isFrench ? 'Terrains' : 'Lots',
    };
    return {
      title: `${projectName} — ${pageNames[page]} | Les Constructions Dominic Cyr`,
      description: isFrench
        ? `${projectName}, projet de construction résidentielle à Saint-Alphonse-de-Granby près de Granby : ${pageNames[page].toLowerCase()} et qualité de vie en harmonie avec la nature.`
        : `${projectName}, a residential construction project in Saint-Alphonse-de-Granby near Granby: ${pageNames[page].toLowerCase()} and a natural living environment.`,
    };
  }

  if (forestaOverview) {
    return isFrench
      ? {
          title:
            'Föresta à Saint-Alphonse-de-Granby | Les Constructions Dominic Cyr',
          description:
            'Découvrez Föresta, le projet résidentiel de Les Constructions Dominic Cyr à Saint-Alphonse-de-Granby, près de Granby : maisons, terrains et milieu de vie en harmonie avec la nature.',
        }
      : {
          title:
            'Föresta in Saint-Alphonse-de-Granby | Les Constructions Dominic Cyr',
          description:
            'Discover Föresta, Les Constructions Dominic Cyr’s residential project in Saint-Alphonse-de-Granby near Granby, Quebec, with homes, lots and a natural living environment.',
        };
  }

  if (contact) {
    return isFrench
      ? {
          title: 'Contactez-nous | Les Constructions Dominic Cyr Inc.',
          description:
            'Contactez Les Constructions Dominic Cyr Inc. pour discuter de votre projet de construction résidentielle, de rénovation ou de gestion de projet.',
        }
      : {
          title: 'Contact Us | Les Constructions Dominic Cyr Inc.',
          description:
            'Contact Les Constructions Dominic Cyr Inc. about your residential construction, renovation or project management needs.',
        };
  }

  return isFrench
    ? {
        title:
          'Les Constructions Dominic Cyr Inc. | Construction résidentielle',
        description:
          'Les Constructions Dominic Cyr Inc. — construction résidentielle, rénovation et gestion de projets à Granby, Saint-Alphonse-de-Granby et partout en Montérégie. Découvrez aussi le projet Föresta.',
      }
    : {
        title: 'Les Constructions Dominic Cyr Inc. | Residential Construction',
        description:
          'Les Constructions Dominic Cyr Inc. — residential construction, renovation and project management in Granby, Saint-Alphonse-de-Granby and the surrounding area. Discover the Föresta project.',
      };
};

const setMeta = (attribute, key, content) => {
  let element = document.head.querySelector(`meta[${attribute}="${key}"]`);
  if (!element) {
    element = document.createElement('meta');
    element.setAttribute(attribute, key);
    document.head.appendChild(element);
  }
  element.setAttribute('content', content);
};

const setLink = (rel, href, id) => {
  let element = document.head.querySelector(`link[rel="${rel}"]`);
  if (!element) {
    element = document.createElement('link');
    element.setAttribute('rel', rel);
    if (id) element.id = id;
    document.head.appendChild(element);
  }
  element.setAttribute('href', href);
};

export default function SeoManager() {
  const { pathname } = useLocation();
  const { i18n } = useTranslation();
  const language = getLanguage(i18n.language);

  useEffect(() => {
    const metadata = getMetadata(pathname, language);
    const canonicalUrl = `${SITE_URL}${pathname === '/' ? '/' : pathname}`;
    const isFrench = language === 'fr';
    const keywords = isFrench
      ? 'Les Constructions Dominic Cyr, Les Contructions Dominic Cyr, Föresta, Foresta, Foresta Granby, Saint-Alphonse-de-Granby, Granby, construction résidentielle, rénovation résidentielle, projets résidentiels'
      : 'Les Constructions Dominic Cyr, Les Contructions Dominic Cyr, Föresta, Foresta, Foresta Granby, Saint-Alphonse-de-Granby, Granby, residential construction, home renovations, residential projects';
    const structuredData = {
      '@context': 'https://schema.org',
      '@graph': [
        {
          '@type': 'Organization',
          '@id': `${SITE_URL}/#organization`,
          name: COMPANY_NAME,
          url: SITE_URL,
          email: 'mailto:constructions.dcyr@gmail.com',
          telephone: '+1-514-705-7848',
          address: {
            '@type': 'PostalAddress',
            streetAddress: '155 rue Bourgeois',
            addressLocality: 'Saint-Mathieu-de-Beloeil',
            addressRegion: 'QC',
            postalCode: 'J3G 0M9',
            addressCountry: 'CA',
          },
        },
        {
          '@type': 'LocalBusiness',
          '@id': `${SITE_URL}/#local-business`,
          name: COMPANY_NAME,
          alternateName: ['Foresta', 'Föresta', 'Les Contructions Dominic Cyr'],
          url: SITE_URL,
          areaServed: [
            'Granby',
            'Saint-Alphonse-de-Granby',
            'Montérégie',
            'Quebec',
          ],
          parentOrganization: { '@id': `${SITE_URL}/#organization` },
        },
        {
          '@type': 'WebSite',
          '@id': `${SITE_URL}/#website`,
          name: COMPANY_NAME,
          url: SITE_URL,
          inLanguage: language,
          publisher: { '@id': `${SITE_URL}/#organization` },
        },
        ...(pathname === '/'
          ? [
              {
                '@type': 'ItemList',
                name: isFrench ? 'Pages importantes' : 'Important pages',
                itemListElement: [
                  {
                    '@type': 'ListItem',
                    position: 1,
                    name: isFrench
                      ? 'Projet Föresta'
                      : 'Föresta residential project',
                    url: FORESTA_URL,
                  },
                  {
                    '@type': 'ListItem',
                    position: 2,
                    name: isFrench ? 'Contactez-nous' : 'Contact us',
                    url: `${SITE_URL}/contact`,
                  },
                ],
              },
            ]
          : []),
      ],
    };

    document.documentElement.lang = language;
    document.title = metadata.title;
    setMeta('name', 'description', metadata.description);
    setMeta('name', 'keywords', keywords);
    setMeta('property', 'og:title', metadata.title);
    setMeta('property', 'og:description', metadata.description);
    setMeta('property', 'og:url', canonicalUrl);
    setMeta('property', 'og:locale', isFrench ? 'fr_CA' : 'en_CA');
    setMeta('property', 'og:type', 'website');
    setMeta('property', 'og:image', SOCIAL_IMAGE_URL);
    setMeta('property', 'og:image:width', '1000');
    setMeta('property', 'og:image:height', '511');
    setMeta('property', 'og:image:alt', COMPANY_NAME);
    setMeta('name', 'twitter:card', 'summary_large_image');
    setMeta('name', 'twitter:title', metadata.title);
    setMeta('name', 'twitter:description', metadata.description);
    setMeta('name', 'twitter:image', SOCIAL_IMAGE_URL);
    setLink('canonical', canonicalUrl, 'canonical-link');

    let jsonLd = document.head.querySelector('#site-structured-data');
    if (!jsonLd) {
      jsonLd = document.createElement('script');
      jsonLd.id = 'site-structured-data';
      jsonLd.type = 'application/ld+json';
      document.head.appendChild(jsonLd);
    }
    jsonLd.textContent = JSON.stringify(structuredData);
  }, [language, pathname]);

  return null;
}
