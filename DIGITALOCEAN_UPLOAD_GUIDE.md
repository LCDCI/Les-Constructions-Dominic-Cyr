# Living Environment Translations - DigitalOcean Upload Guide

## Prerequisites

You'll need:
1. DigitalOcean account with access to your backend and file service instances
2. Backend URL (e.g., `https://lcdci-portal-jmjxt.ondigitalocean.app`)
3. File Service URL (e.g., `https://files-service-app-xubs2.ondigitalocean.app`)

## Step 1: Upload Translation Files to DigitalOcean File Service

### Option A: Using Node.js Script (Recommended)

```bash
# Navigate to translation scripts
cd translation-scripts

# Set environment variables pointing to your DigitalOcean services
export FILE_SERVICE_URL=https://files-service-app-xubs2.ondigitalocean.app
export BACKEND_URL=https://lcdci-portal-jmjxt.ondigitalocean.app

# Run the upload script
node upload-living-environment-translations.js
```

The script will:
1. Upload `livingEnvironment_en.json` to file service
2. Upload `livingEnvironment_fr.json` to file service
3. Register both files with the backend
4. Display the file IDs

**Expected Output:**
```
🚀 Starting Living Environment translation file upload...

Uploading livingEnvironment_en.json...
✅ Uploaded livingEnvironment_en.json
   File ID: abc123-def456-ghi789

Uploading livingEnvironment_fr.json...
✅ Uploaded livingEnvironment_fr.json
   File ID: xyz789-uvw012-abc345

📝 Registering files with backend...
✅ Registered en.livingenvironment -> abc123-def456-ghi789
✅ Registered fr.livingenvironment -> xyz789-uvw012-abc345

====================================================================
📝 To update TranslationRegistry.java manually:

  fileIdMap.put("en.livingenvironment", "abc123-def456-ghi789");
  fileIdMap.put("fr.livingenvironment", "xyz789-uvw012-abc345");

====================================================================
✅ Upload complete!
```

### Option B: Using PowerShell Script

```powershell
cd translation-scripts

# Set environment variables
$env:FILE_SERVICE_URL = "https://files-service-app-xubs2.ondigitalocean.app"
$env:BACKEND_URL = "https://lcdci-portal-jmjxt.ondigitalocean.app"

# Run the upload script
.\upload-living-environment-translations.ps1
```

### Option C: Manual Upload via curl/Postman

If the scripts don't work, you can manually upload:

```bash
# Upload English file
curl -X POST https://files-service-app-xubs2.ondigitalocean.app/files \
  -F "file=@translation-files/livingEnvironment_en.json" \
  -F "category=DOCUMENT" \
  -F "projectId=translations" \
  -F "uploadedBy=system"

# Upload French file
curl -X POST https://files-service-app-xubs2.ondigitalocean.app/files \
  -F "file=@translation-files/livingEnvironment_fr.json" \
  -F "category=DOCUMENT" \
  -F "projectId=translations" \
  -F "uploadedBy=system"
```

Save the returned file IDs.

## Step 2: Update TranslationRegistry.java

Copy the file IDs from the upload output and update the backend code:

1. Open: `src/main/java/com/ecp/les_constructions_dominic_cyr/backend/utils/translation/dataaccesslayer/TranslationRegistry.java`

2. Find and replace:
   ```java
   // Living Environment page translations
   fileIdMap.put("en.livingenvironment", "PLACEHOLDER_EN_LIVINGENVIRONMENT");
   fileIdMap.put("fr.livingenvironment", "PLACEHOLDER_FR_LIVINGENVIRONMENT");
   ```

   With actual IDs from the upload:
   ```java
   // Living Environment page translations
   fileIdMap.put("en.livingenvironment", "abc123-def456-ghi789");
   fileIdMap.put("fr.livingenvironment", "xyz789-uvw012-abc345");
   ```

3. Save the file

## Step 3: Deploy to DigitalOcean

### Option A: Using GitHub Actions (If configured)

If you have CI/CD set up:
```bash
git add src/main/java/com/ecp/les_constructions_dominic_cyr/backend/utils/translation/dataaccesslayer/TranslationRegistry.java
git commit -m "Update living environment translation file IDs"
git push origin main
```

The CI/CD pipeline will automatically rebuild and deploy.

### Option B: Manual Deployment

Build locally and deploy:

```bash
# Clean build
gradle clean build

# Build Docker image
docker build -t lcdci-backend:latest .

# Push to DigitalOcean Container Registry
docker tag lcdci-backend:latest <your-do-registry>/lcdci-backend:latest
docker push <your-do-registry>/lcdci-backend:latest

# Restart the app on DigitalOcean
# (Use DigitalOcean App Platform UI or CLI to trigger deployment)
```

## Step 4: Verify Upload

Test that translations are working:

1. Navigate to: `https://lcdci-portal-jmjxt.ondigitalocean.app/projects/proj-001-foresta/living-environment`

2. Check:
   - ✅ Page loads without errors
   - ✅ Content displays with correct language
   - ✅ Project colors are applied
   - ✅ Amenity icons are visible

3. If translations don't appear:
   - Check browser console for errors
   - Verify backend is running: `curl https://lcdci-portal-jmjxt.ondigitalocean.app/api/v1/projects`
   - Check file service is accessible: `curl https://files-service-app-xubs2.ondigitalocean.app/files`

## Troubleshooting

### Upload Script Says "Could not register with backend"

This is OK! It means:
- The script successfully uploaded the files ✅
- But couldn't automatically register them (backend might require authentication)
- Solution: Manually update `TranslationRegistry.java` with the file IDs (Step 2)

### File IDs Not Found

If the upload didn't return file IDs:
1. Check file service is running and accessible
2. Verify you have correct environment variables set
3. Check file service logs for errors

### Still Getting Translation Errors After Deploy

1. Verify the TranslationRegistry.java changes were deployed
2. Restart the backend application
3. Clear browser cache (Ctrl+Shift+Delete or Cmd+Shift+Delete)
4. Test in incognito/private window

## Environment Variables Reference

Use these URLs for your DigitalOcean setup:

```bash
# Backend
BACKEND_URL=https://lcdci-portal-jmjxt.ondigitalocean.app
VITE_API_BASE_URL=https://lcdci-portal-jmjxt.ondigitalocean.app/api/v1

# File Service
FILE_SERVICE_URL=https://files-service-app-xubs2.ondigitalocean.app
VITE_FILES_SERVICE_URL=https://files-service-app-xubs2.ondigitalocean.app

# Auth0 (keep your existing values)
VITE_AUTH0_DOMAIN=your-auth0-domain
VITE_AUTH0_CLIENT_ID=your-client-id
VITE_AUTH0_AUDIENCE=your-audience
```

## Summary Checklist

- [ ] Upload translation files using script or curl
- [ ] Copy file IDs from upload response
- [ ] Update TranslationRegistry.java with file IDs
- [ ] Commit and push changes (or manually deploy)
- [ ] Wait for deployment to complete
- [ ] Test the living environment page
- [ ] Verify translations display correctly
- [ ] Check both EN and FR languages work

---

**Need help?** Check the browser console (F12) for detailed error messages!
