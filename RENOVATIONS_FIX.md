# Translation Keys Missing - Quick Fix

## Issue
The Renovations page shows translation keys (like `hero.title1`, `states.empty.title`) instead of actual text.

## Root Cause
Translation files exist but haven't been uploaded to the file service and registered with the backend.

## Solution (2 Steps)

### 1. Ensure Services Are Running
```bash
# Start all services (if not already running)
docker-compose up -d

# Wait for services to be ready (~30 seconds)
```

### 2. Upload Renovations Translations
```bash
# Install dependencies (if first time)
npm install form-data axios

# Upload renovations translation files
node translation-scripts/upload-translations.js renovations
```

## What This Does
- Uploads `renovations_en.json` and `renovations_fr.json` to MinIO
- Registers file IDs with backend's TranslationRegistry
- Makes translations available via API endpoints

## Verification
After running the command, test with:
```bash
curl http://localhost:8080/api/v1/translations/en/page/renovations
```

You should see JSON with all translation keys and values (not a 404 error).

## View the Page
1. Start frontend: `cd frontend/les_constructions_dominic_cyr && npm run dev`
2. Navigate to: `http://localhost:3000/renovations`
3. Content should now display properly!

## For More Details
- **Comprehensive Setup Guide**: See `RENOVATIONS_SETUP.md`
- **Complete Translation Documentation**: See `TRANSLATION_GUIDE.md`
- **Script Documentation**: See `translation-scripts/README.md`

## Note
If the backend restarts, you'll need to re-run the upload command since the registry is stored in memory.
