# Living Environment Page Implementation

## Overview
A custom living environment page has been created for the public landing project overview, accessible through clicking the leaf icon. The content is fetched from the database (project colors) and uses a flexible, project-aware translation system.

## Changes Made

### 1. Updated LivingEnvironmentPage Component
**File**: `frontend/les_constructions_dominic_cyr/src/pages/Public_Facing/LivingEnvironmentPage.jsx`

**Features**:
- Dynamic data fetching from `/api/v1/projects/{projectIdentifier}/overview`
- Applies project colors to CSS variables (`--primary-color`, `--tertiary-color`, `--buyer-color`)
- Project-specific amenities and content based on project identifier
- Dynamic amenity icon mapping
- Proper error handling and loading states
- Support for multiple projects (currently Föresta and Panorama)

### 2. Updated Living Environment CSS
**File**: `frontend/les_constructions_dominic_cyr/src/styles/Public_Facing/living-environment.css`

**Updates**:
- Changed from hardcoded colors to dynamic CSS variables
- Uses `var(--primary-color)` for titles and amenity box gradients
- Uses `var(--tertiary-color)` for taglines and secondary colors
- Uses `var(--buyer-color)` for complementary colors
- Maintained responsive design for all screen sizes

### 3. Updated Translation Files

#### English Translations
**File**: `translation-scripts/translation-files/livingEnvironment_en.json`

**Structure**:
```json
{
  "header": {
    "foresta": { title, subtitle, subtitleLast, tagline },
    "panorama": { title, subtitle, subtitleLast, tagline }
  },
  "description": {
    "foresta": "...",
    "panorama": "..."
  },
  "proximity": {
    "foresta": "CLOSE TO EVERYTHING!",
    "panorama": "FOR OUTDOOR LOVERS!"
  },
  "amenities": {
    "foresta": { ski, golf, bike, bromont, yamaska, vineyards, ... },
    "panorama": { ski, golf, bike, vineyards, crosscountry, hiking }
  },
  "footer": {
    "foresta": "FÖRESTA is a project by Les Constructions Dominic Cyr inc.",
    "panorama": "PANORAMA is a project by Les Constructions Dominic Cyr inc."
  }
}
```

#### French Translations
**File**: `translation-scripts/translation-files/livingEnvironment_fr.json`

**Structure**: Same as English with French content:

**FÖRESTA Content**:
- **Title**: FÖRESTA, UN MILIEU DE VIE EXCEPTIONNEL
- **Tagline**: Au rythme de la nature
- **Description**: Un milieu de vie exceptionnel pour ceux qui rêvent de s'évader du quotidien...
- **Proximity Title**: À PROXIMITÉ DE TOUT!
- **Features**: Ski, Golf, Vélo, Bromont, Parc National de la Yamaska, Vergers & Vignobles, Restaurants, Épiceries, Magasins, Hôpitaux, Écoles, Spas

**PANORAMA Content**:
- **Title**: L'AUTHENTICITÉ AU COEUR DE LA MONTAGNE
- **Tagline**: La vie en montagne
- **Description**: Le projet Panorama est situé dans le charmant village de Sutton dans les Cantons-de-l'Est...
- **Proximity Title**: POUR LES AMATEURS DE PLEIN-AIR!
- **Features**: Ski à Sutton, Golf, Vélo, Vignobles, Ski de fond et raquette, Randonnées

### 4. Updated App Routing
**File**: `frontend/les_constructions_dominic_cyr/src/App.jsx`

**Changes**:
- Added import: `import LivingEnvironmentPage from './pages/Public_Facing/LivingEnvironmentPage';`
- Added new route: `/projects/:projectIdentifier/living-environment`
- Route properly maps to the LivingEnvironmentPage component

## Navigation Flow

1. User navigates to a project overview page (`/projects/{projectIdentifier}/overview`)
2. Project Overview displays a grid with "Living Environment" feature with a leaf icon
3. Clicking on the leaf icon navigates to `/projects/{projectIdentifier}/living-environment`
4. LivingEnvironmentPage:
   - Fetches project data (colors) from backend
   - Applies colors to page styling
   - Loads appropriate translations based on project identifier
   - Displays project-specific content and amenities

## Supported Projects

### Föresta
- Project identifier: `foresta`
- Color theme: Dynamic from database
- Amenities: 12 items (Ski, Golf, Cycling, Bromont, Yamaska National Park, Orchards & Vineyards, Restaurants, Groceries, Shops, Hospitals, Schools, Spas)

### Panorama
- Project identifier: `panorama`
- Color theme: Dynamic from database
- Amenities: 6 items (Skiing at Sutton, Golf, Cycling, Vineyards, Cross-Country Skiing & Snowshoeing, Hiking)

## Key Features

✅ **Dynamic Content**: All content is controlled through translation files, easily updatable without code changes
✅ **Color Theming**: Automatically applies project colors from database
✅ **Multi-language Support**: Full French and English support
✅ **Responsive Design**: Works perfectly on all screen sizes
✅ **Icon System**: Each amenity has a corresponding React icon
✅ **Flexible Architecture**: Easy to add new projects by adding entries to translation files

## Future Enhancements

- Store living environment data in database instead of translation files
- Add living environment images/gallery
- Support for custom amenity icons per project
- Analytics tracking for living environment page views
