# Translation System Guide

Complete guide for adding multi-language support to new web pages in the Les Constructions Dominic Cyr application.

## Table of Contents

1. [Overview](#overview)
2. [How It Works](#how-it-works)
3. [Adding Translations to a New Page](#adding-translations-to-a-new-page)
4. [Using Translations in React Components](#using-translations-in-react-components)
5. [Troubleshooting](#troubleshooting)
6. [Best Practices](#best-practices)

---

## Overview

The application supports **French (fr)** and **English (en)** translations. Translations are:
- Stored as JSON files in the file service (MinIO)
- Dynamically loaded by the frontend via the backend API
- Managed through a registry that maps language/page combinations to file IDs
- Automatically applied based on user's cookie preference

---

## How It Works

### Architecture

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Frontend  │ ──────> │   Backend    │ ──────> │ File Service│
│  (React)    │         │ (Spring Boot)│         │   (MinIO)   │
└─────────────┘         └──────────────┘         └─────────────┘
     │                         │                         │
     │                         │                         │
     │ 1. Request translations │                         │
     │<────────────────────────│                         │
     │                         │                         │
     │                         │ 2. Look up file ID      │
     │                         │    from registry        │
     │                         │<────────────────────────│
     │                         │                         │
     │                         │ 3. Fetch JSON file      │
     │                         │────────────────────────>│
     │                         │                         │
     │                         │ 4. Return translations  │
     │<────────────────────────│                         │
     │                         │                         │
     │ 5. Display translated   │                         │
     │    content              │                         │
```

### File Structure

- **Translation Files**: `translation-scripts/translation-files/{page}_{language}.json`
  - Example: `translation-scripts/translation-files/home_en.json`, `translation-scripts/translation-files/home_fr.json`
  
- **Backend Registry**: `src/main/java/.../backend/utils/translation/TranslationRegistry.java`
  - Maps `{language}.{page}` → `fileId`
  - Example: `"en.home"` → `"d5e2467b-fb0b-4c60-abae-f99355f1b259"`

- **Frontend Hook**: `frontend/.../src/hooks/usePageTranslations.js`
  - Custom hook for loading page-specific translations

---

## Adding Translations to a New Page

### Step 1: Create Translation JSON Files

Create two JSON files in the `translation-scripts/translation-files/` directory:

**File: `translation-scripts/translation-files/projects_en.json`**
```json
{
  "nav": {
    "projects": "Residential Projects",
    "renovation": "Renovation",
    "projectManagement": "Project Management"
  },
  "footer": {
    "companyName": "Les Constructions Dominic Cyr Inc.",
    "address": "155 rue Bourgeois"
  },
  "pageTitle": "Our Projects",
  "pageDescription": "Discover our residential construction projects",
  "section1": {
    "title": "Featured Projects",
    "description": "Explore our portfolio of completed projects"
  }
}
```

**File: `translation-scripts/translation-files/projects_fr.json`**
```json
{
  "nav": {
    "projects": "Projets résidentiels",
    "renovation": "Rénovation",
    "projectManagement": "Gestion de projet"
  },
  "footer": {
    "companyName": "Les Constructions Dominic Cyr Inc.",
    "address": "155 rue Bourgeois"
  },
  "pageTitle": "Nos projets",
  "pageDescription": "Découvrez nos projets de construction résidentielle",
  "section1": {
    "title": "Projets vedettes",
    "description": "Explorez notre portefeuille de projets réalisés"
  }
}
```

**Important Notes:**
- ✅ Always include `nav` and `footer` sections (they're used globally)
- ✅ Use nested objects for organization (e.g., `section1.title`, `section1.description`)
- ✅ Keep the structure identical between `_en.json` and `_fr.json` files
- ✅ Use descriptive keys (e.g., `pageTitle` not `title1`)

### Step 2: Upload Translation Files

Run the upload script to upload files to the file service and register them:

```bash
# Make sure you're in the project root directory
cd C:\Les-Constructions-Dominic-Cyr

# Run the upload script
node translation-scripts/upload-translations.js
```

**What the script does:**
1. Uploads `translation-scripts/translation-files/projects_en.json` to file service
2. Uploads `translation-scripts/translation-files/projects_fr.json` to file service
3. Gets file IDs from the file service
4. Registers file IDs in the backend's `TranslationRegistry`
5. Prints confirmation messages

**Expected Output:**
```
🚀 Starting translation file upload...

Uploading projects_en.json...
✅ Uploaded successfully!
   File ID: abc123-def456-...

Uploading projects_fr.json...
✅ Uploaded successfully!
   File ID: xyz789-uvw012-...

✅ Registered en.projects -> abc123-def456-...
✅ Registered fr.projects -> xyz789-uvw012-...
```

**Note:** If the backend restarts, you'll need to run this script again (the registry is in-memory).

### Step 3: Update the Upload Script (Optional)

If you want to automate future uploads, you can update `translation-scripts/upload-translations.js` to include your new page:

```javascript
// In translation-scripts/upload-translations.js, add to the main() function:
const projectsEnResult = await uploadTranslationFile('projects_en.json', 'en');
results.push(projectsEnResult);

const projectsFrResult = await uploadTranslationFile('projects_fr.json', 'fr');
results.push(projectsFrResult);
```

---

## Using Translations in React Components

### Method 1: Using `usePageTranslations` Hook (Recommended)

This is the recommended approach for page components:

```jsx
import React from 'react';
import { usePageTranslations } from '../hooks/usePageTranslations';

export default function Projects() {
  const { t, isLoading } = usePageTranslations('projects');
  
  if (isLoading) {
    return <div>Loading translations...</div>;
  }
  
  return (
    <div>
      <h1>{t('pageTitle')}</h1>
      <p>{t('pageDescription')}</p>
      
      <section>
        <h2>{t('section1.title')}</h2>
        <p>{t('section1.description')}</p>
      </section>
    </div>
  );
}
```

**How it works:**
- `usePageTranslations('projects')` loads translations for the 'projects' namespace
- `t('pageTitle')` translates to `projects:pageTitle`
- Translations are loaded automatically when the component mounts
- Language changes trigger automatic reload

### Method 2: Using `useTranslation` Hook (For Global Components)

For components that need global translations (like navigation, footer):

```jsx
import React from 'react';
import { useTranslation } from 'react-i18next';

export default function AppNavBar() {
  const { t, i18n } = useTranslation();
  
  const toggleLanguage = () => {
    const newLang = i18n.language === 'fr' ? 'en' : 'fr';
    i18n.changeLanguage(newLang);
  };
  
  return (
    <nav>
      <Link to="/projects">{t('nav.projects')}</Link>
      <button onClick={toggleLanguage}>
        {i18n.language.toUpperCase()}
      </button>
    </nav>
  );
}
```

**Note:** `nav` and `footer` translations are automatically added to the `translation` namespace by `i18n.js`, so they're available globally.

### Translation Key Syntax

```jsx
// Simple key
t('pageTitle')                    // → projects:pageTitle

// Nested key
t('section1.title')               // → projects:section1.title

// With default value (fallback if translation missing)
t('pageTitle', 'Default Title')   // → Uses default if key not found

// With interpolation
t('welcome', { name: 'John' })    // → "Welcome, John" (if JSON has "welcome": "Welcome, {{name}}")
```

---

## Troubleshooting

### Problem: Translations not showing up

**Check 1: Registry is populated**
```bash
# Test if file IDs are registered
curl http://localhost:8080/api/v1/translations/registry/en/projects
# Should return a file ID, not 404
```

**Solution:** Run `node translation-scripts/upload-translations.js` again

**Check 2: Backend is returning translations**
```bash
# Test the translations endpoint
curl http://localhost:8080/api/v1/translations/en
# Should return JSON with translations object
```

**Check 3: Browser console**
- Open browser DevTools (F12)
- Look for errors in Console tab
- Check Network tab for failed API requests

**Check 4: Hard refresh browser**
- Press `Ctrl+Shift+R` (Windows/Linux) or `Cmd+Shift+R` (Mac)
- Clears cached responses

### Problem: "No translations loaded" in console

**Symptoms:**
```
[i18n] No translations loaded for en
[TranslationAPI] Translation namespaces: Array(0)
```

**Causes:**
1. Backend registry is empty (backend restarted)
2. File service is not accessible
3. Translation files not uploaded

**Solution:**
1. Run `node translation-scripts/upload-translations.js`
2. Verify file service is running: `curl http://localhost:8082/files/{fileId}`
3. Check backend logs for errors

### Problem: Language toggle doesn't work

**Check:**
- Is `i18n.changeLanguage()` being called?
- Are translations loaded for both languages?
- Check browser console for errors

**Solution:**
- Ensure both `_en.json` and `_fr.json` files exist
- Verify both are uploaded and registered
- Check that `i18n.js` is properly initialized

### Problem: Some translations missing

**Check:**
- Are the keys identical in both language files?
- Are you using the correct namespace?
- Check browser console for warnings

**Solution:**
- Verify JSON structure matches between languages
- Use `t('key', 'Fallback text')` for missing translations
- Check that keys exist in the JSON file

---

## Best Practices

### 1. Translation File Organization

✅ **DO:**
- Use descriptive, hierarchical keys: `section1.title`, `section1.description`
- Keep structure consistent between languages
- Include `nav` and `footer` in every page's translation file
- Use meaningful key names: `pageTitle` not `title1`

❌ **DON'T:**
- Use generic keys like `text1`, `text2`
- Mix languages in the same file
- Forget to include required sections (nav, footer)

### 2. Component Structure

✅ **DO:**
```jsx
// Use the hook at the top level
const { t } = usePageTranslations('pageName');

// Use descriptive keys
<h1>{t('hero.title')}</h1>
<p>{t('hero.description')}</p>
```

❌ **DON'T:**
```jsx
// Don't hardcode text
<h1>Welcome</h1>  // ❌ Bad

// Don't use generic keys
<h1>{t('text1')}</h1>  // ❌ Bad
```

### 3. Translation Keys Naming

✅ **Good Examples:**
- `pageTitle` - Clear and descriptive
- `hero.title` - Organized by section
- `contactForm.submitButton` - Specific and hierarchical
- `nav.projects` - Clear namespace

❌ **Bad Examples:**
- `t1`, `t2` - Too generic
- `title` - Too vague (which title?)
- `button` - Not descriptive

### 4. Handling Missing Translations

Always provide fallback text:
```jsx
// Good: Has fallback
t('pageTitle', 'Projects Page')

// Better: Descriptive fallback
t('hero.title', 'Welcome to Our Projects')
```

### 5. Testing Translations

1. **Test both languages:**
   - Switch language using the toggle
   - Verify all text changes
   - Check that layout doesn't break with longer/shorter text

2. **Test edge cases:**
   - Missing translation keys
   - Special characters (é, è, à, etc.)
   - Long text that might break layout

3. **Check console:**
   - No errors or warnings
   - Translations loading successfully
   - Namespaces properly registered

---

## Quick Reference

### Commands

```bash
# Upload translation files
node translation-scripts/upload-translations.js

# Test translation system
node scripts/test-translations.js

# Check if registry has file ID
curl http://localhost:8080/api/v1/translations/registry/en/{pageName}

# Get all translations for a language
curl http://localhost:8080/api/v1/translations/en

# Get page-specific translations
curl http://localhost:8080/api/v1/translations/en/page/{pageName}
```

### File Locations

- Translation files: `translation-scripts/translation-files/{page}_{lang}.json`
- Upload script: `translation-scripts/upload-translations.js`
- Frontend hook: `frontend/.../src/hooks/usePageTranslations.js`
- i18n config: `frontend/.../src/utils/i18n.js`
- Backend service: `src/main/java/.../backend/utils/translation/TranslationService.java`
- Backend registry: `src/main/java/.../backend/utils/translation/TranslationRegistry.java`

### Translation Key Format

```
{namespace}:{key.path}
```

Examples:
- `home:hero.title` - In 'home' namespace, 'hero.title' key
- `projects:pageTitle` - In 'projects' namespace, 'pageTitle' key
- `nav.projects` - In default 'translation' namespace, 'nav.projects' key

---

## Example: Complete Workflow

Let's add translations to a new "About" page:

### 1. Create Translation Files

**`translation-scripts/translation-files/about_en.json`:**
```json
{
  "nav": {
    "projects": "Residential Projects",
    "about": "About Us"
  },
  "footer": {
    "companyName": "Les Constructions Dominic Cyr Inc."
  },
  "pageTitle": "About Us",
  "hero": {
    "title": "Building Dreams Since 1990",
    "subtitle": "Your trusted partner in residential construction"
  },
  "history": {
    "title": "Our History",
    "description": "With over 30 years of experience..."
  }
}
```

**`translation-scripts/translation-files/about_fr.json`:**
```json
{
  "nav": {
    "projects": "Projets résidentiels",
    "about": "À propos"
  },
  "footer": {
    "companyName": "Les Constructions Dominic Cyr Inc."
  },
  "pageTitle": "À propos",
  "hero": {
    "title": "Construire des rêves depuis 1990",
    "subtitle": "Votre partenaire de confiance en construction résidentielle"
  },
  "history": {
    "title": "Notre histoire",
    "description": "Avec plus de 30 ans d'expérience..."
  }
}
```

### 2. Upload Files

```bash
node translation-scripts/upload-translations.js
```

### 3. Create React Component

**`frontend/.../src/pages/About.jsx`:**
```jsx
import React from 'react';
import { usePageTranslations } from '../hooks/usePageTranslations';

export default function About() {
  const { t, isLoading } = usePageTranslations('about');
  
  if (isLoading) {
    return <div>Loading...</div>;
  }
  
  return (
    <div className="about-page">
      <h1>{t('pageTitle')}</h1>
      
      <section className="hero">
        <h2>{t('hero.title')}</h2>
        <p>{t('hero.subtitle')}</p>
      </section>
      
      <section className="history">
        <h3>{t('history.title')}</h3>
        <p>{t('history.description')}</p>
      </section>
    </div>
  );
}
```

### 4. Test

1. Start the application
2. Navigate to the About page
3. Toggle language - text should change
4. Check browser console for any errors

---

## Support

If you encounter issues:

1. Check the [Troubleshooting](#troubleshooting) section
2. Run `node scripts/test-translations.js` to verify backend setup
3. Check browser console for errors
4. Verify file service is running: `curl http://localhost:8082/files/{fileId}`

---

**Last Updated:** December 2025

