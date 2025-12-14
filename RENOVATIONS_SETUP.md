# Renovations Page Translation Setup

## Problem

The Renovations page is currently showing translation keys instead of actual content:
- `hero.title1`, `hero.title2`, `hero.title3`
- `hero.subtitle`
- `intro.title`, `intro.description`
- `services.title`
- `states.empty.title`, `states.empty.body`
- `callToAction.title`

## Solution

The translation files exist and have been updated with all necessary keys. You just need to **upload them to the file service and register them with the backend**.

## Setup Steps

### Prerequisites

Ensure the following services are running:
1. **File Service** on `http://localhost:8082`
2. **Backend** on `http://localhost:8080`
3. **MinIO** (object storage)
4. **PostgreSQL** databases

If not already running, start them with:
```bash
docker-compose up -d
```

### Step 1: Install Dependencies (if needed)

```bash
npm install form-data axios
```

### Step 2: Upload Renovations Translation Files

Run the upload script with the `renovations` page name:

```bash
node translation-scripts/upload-translations.js renovations
```

**What this does:**
1. Uploads `renovations_en.json` to the file service
2. Uploads `renovations_fr.json` to the file service  
3. Gets file IDs from the file service
4. Automatically registers the file IDs with the backend's TranslationRegistry
5. Displays confirmation messages

**Expected Output:**
```
🚀 Starting translation file upload...

📄 Page: renovations
Make sure the file service is running on http://localhost:8082

Uploading renovations_en.json...
✅ Uploaded successfully!
   File ID: abc123-def456-...

Uploading renovations_fr.json...
✅ Uploaded successfully!
   File ID: xyz789-uvw012-...

📝 Registering files with backend at http://localhost:8080...

✅ Registered en.renovations -> abc123-def456-...
✅ Registered fr.renovations -> xyz789-uvw012-...

==================================================
✅ All files uploaded successfully!
==================================================
```

### Step 3: Verify the Setup

Test that translations are registered:

```bash
# Check English translations are registered
curl http://localhost:8080/api/v1/translations/registry/en/renovations

# Check French translations are registered
curl http://localhost:8080/api/v1/translations/registry/fr/renovations

# Fetch English translations for renovations page
curl http://localhost:8080/api/v1/translations/en/page/renovations

# Fetch French translations for renovations page
curl http://localhost:8080/api/v1/translations/fr/page/renovations
```

Each command should return data (not 404 errors).

### Step 4: Test in Browser

1. Start the frontend (if not already running):
   ```bash
   cd frontend/les_constructions_dominic_cyr
   npm run dev
   ```

2. Navigate to the renovations page: `http://localhost:3000/renovations`

3. You should now see:
   - ✅ **English**: "RENOVATE MODERNIZE TRANSFORM" (instead of `hero.title1`, etc.)
   - ✅ **French**: "RÉNOVER MODERNISER TRANSFORMER" (when you switch language)

4. Test the language toggle (EN/FR button) to verify both languages work correctly.

## Translation Keys Added

The following translation keys are now available in both English and French:

### Hero Section
- `hero.title1` - "RENOVATE" / "RÉNOVER"
- `hero.title2` - "MODERNIZE" / "MODERNISER"
- `hero.title3` - "TRANSFORM" / "TRANSFORMER"
- `hero.subtitle` - "Your home deserves the very best." / "Votre maison mérite ce qu'il y a de mieux"

### Intro Section
- `intro.title` - "Transform your home into the living space of your dreams!" / "Transformez votre maison en un espace de vie dont vous rêvez !"
- `intro.description` - Complete description text in both languages

### Services Section
- `services.title` - Service description in both languages

### Call to Action
- `callToAction.title` - "Entrust your renovation to passionate professionals!" / "Confiez votre rénovation à des professionnels passionnés !"

### States (NEW)
- `states.empty.title` - "No renovations yet" / "Aucune rénovation pour le moment"
- `states.empty.body` - "Check back soon..." / "Revenez bientôt..."
- `states.error.message` - "Unable to load renovations..." / "Impossible de charger..."
- `states.error.retry` - "Retry" / "Réessayer"

### Navigation & Footer
- `nav.*` - Navigation menu items
- `footer.*` - Footer content (company info, contact details, etc.)

## Troubleshooting

### Issue: "File service not accessible"

**Solution:**
```bash
# Check if file service is running
curl http://localhost:8082/health

# If not running, start services
docker-compose up -d files-service
```

### Issue: "Backend not accessible"

**Solution:**
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# If not running, start services
docker-compose up -d backend
```

### Issue: "Translations still showing as keys"

**Solutions:**
1. Hard refresh browser: `Ctrl+Shift+R` (Windows/Linux) or `Cmd+Shift+R` (Mac)
2. Clear browser cache
3. Check browser console (F12) for errors
4. Verify translations were uploaded (see Step 3 verification commands)
5. Re-run upload script: `node translation-scripts/upload-translations.js renovations`

### Issue: Backend restarted and translations are gone

**Explanation:** The TranslationRegistry stores file IDs in memory, so they're lost on restart.

**Solution:** Simply re-run the upload script:
```bash
node translation-scripts/upload-translations.js renovations
```

The files are still in MinIO, so the script will just re-register the existing files with the backend.

## Alternative Upload Methods

If you prefer not to use Node.js:

### PowerShell (Windows)
Modify `translation-scripts/upload-translations.ps1` to use "renovations" instead of "home", then run:
```powershell
.\translation-scripts\upload-translations.ps1
```

### Bash (Linux/Mac)
Modify `translation-scripts/upload-translations.sh` to use "renovations" instead of "home", then run:
```bash
chmod +x translation-scripts/upload-translations.sh
./translation-scripts/upload-translations.sh
```

## Related Documentation

- **Complete Translation Guide**: See `TRANSLATION_GUIDE.md` for detailed documentation
- **Quick Start**: See `QUICK_START.md` for initial setup
- **Translation Scripts**: See `translation-scripts/README.md` for script documentation

## Summary

The issue was that the renovations translation files existed but weren't uploaded to the file service or registered with the backend. Running the upload script resolves this by:

1. ✅ Uploading JSON files to MinIO via file service
2. ✅ Registering file IDs with backend's TranslationRegistry
3. ✅ Making translations available to the frontend via API

After running `node translation-scripts/upload-translations.js renovations`, the Renovations page will display proper content instead of translation keys.
