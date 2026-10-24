import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const SITE_URL = 'https://constructions-dominiccyr.com';
const COMPANY_NAME = 'Les Constructions Dominic Cyr Inc.';

const getLanguage = language =>
  language?.toLowerCase().startsWith('fr') ? 'fr' : 'en';

const getMetadata = (pathname, language) => {
  const isFrench = language === 'fr';
  const forestaOverview = pathname === '/projects/proj-001-foresta/overview';
  const contact = pathname === '/contact';
  const residentialProjects = pathname === '/residential-projects';

  if (forestaOverview) {
    return isFrench
      ? {
          title:
            'Projet Föresta à Saint-Alphonse-de-Granby | Les Constructions Dominic Cyr',
          description:
            'Découvrez le projet résidentiel Föresta à Saint-Alphonse-de-Granby : maisons unifamiliales, grands terrains et milieu de vie en harmonie avec la nature.',
        }
      : {
          title:
            'Föresta Residential Project in Saint-Alphonse-de-Granby | Les Constructions Dominic Cyr',
          description:
            'Discover the Föresta residential project in Saint-Alphonse-de-Granby: single-family homes, large lots and a natural living environment.',
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

  if (residentialProjects) {
    return isFrench
      ? {
          title: 'Projets résidentiels | Les Constructions Dominic Cyr Inc.',
          description:
            'Explorez les projets résidentiels de Les Constructions Dominic Cyr, dont le projet Föresta à Saint-Alphonse-de-Granby.',
        }
      : {
          title: 'Residential Projects | Les Constructions Dominic Cyr Inc.',
          description:
            'Explore Les Constructions Dominic Cyr residential projects, including the Föresta project in Saint-Alphonse-de-Granby.',
        };
  }

  return isFrench
    ? {
        title:
          'Les Constructions Dominic Cyr Inc. | Construction résidentielle',
        description:
          'Entreprise familiale de construction résidentielle, rénovation et gestion de projets à Granby, Saint-Alphonse-de-Granby et partout en Montérégie.',
      }
    : {
        title: 'Les Constructions Dominic Cyr Inc. | Residential Construction',
        description:
          'Family-owned residential construction, renovation and project management company serving Granby, Saint-Alphonse-de-Granby and the surrounding area.',
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
          '@type': 'WebSite',
          '@id': `${SITE_URL}/#website`,
          name: COMPANY_NAME,
          url: SITE_URL,
          inLanguage: language,
          publisher: { '@id': `${SITE_URL}/#organization` },
        },
        ...(pathname === '/' || pathname === '/residential-projects'
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
                    url: `${SITE_URL}/projects/proj-001-foresta/overview`,
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
    setMeta('property', 'og:title', metadata.title);
    setMeta('property', 'og:description', metadata.description);
    setMeta('property', 'og:url', canonicalUrl);
    setMeta('property', 'og:locale', isFrench ? 'fr_CA' : 'en_CA');
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
