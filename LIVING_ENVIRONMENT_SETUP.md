# Living Environment Page Setup Guide

## Overview
A new "Living Environment" page has been created for the Föresta project, displaying information about the exceptional living environment with amenities and proximity features.

## Files Created

### Frontend Files
1. **Component**: `frontend/les_constructions_dominic_cyr/src/pages/Public_Facing/LivingEnvironmentPage.jsx`
   - Main page component with all content sections
   - Displays header, description, amenities grid, and footer
   - Uses translation hooks for multi-language support

2. **CSS**: `frontend/les_constructions_dominic_cyr/src/styles/Public_Facing/living-environment.css`
   - Custom styling for the Living Environment page
   - Responsive design for mobile, tablet, and desktop
   - Gradient backgrounds and hover effects on amenity boxes

### Translation Files
3. **English**: `translation-scripts/translation-files/livingEnvironment_en.json`
4. **French**: `translation-scripts/translation-files/livingEnvironment_fr.json`

### Backend Configuration
5. **TranslationRegistry.java**: Updated with placeholder entries
   - Located at: `src/main/java/com/ecp/les_constructions_dominic_cyr/backend/utils/translation/dataaccesslayer/TranslationRegistry.java`
   - Added placeholder entries for `en.livingenvironment` and `fr.livingenvironment`

### Upload Scripts
6. **Node.js Script**: `translation-scripts/upload-living-environment-translations.js`
7. **PowerShell Script**: `translation-scripts/upload-living-environment-translations.ps1`

### Routing
8. **App.jsx**: Updated with new route
   - Route: `/projects/:projectIdentifier/living-environment`
   - Links from ProjectsOverviewPage's "Living Environment" icon will now work

## Setup Instructions

### Step 1: Install Dependencies (if needed)
```bash
cd frontend/les_constructions_dominic_cyr
npm install react-icons
```

### Step 2: Upload Translations to Database

You need to upload the translation files to get proper file IDs. Choose one method:

#### Option A: Using Node.js (Recommended)
```bash
# Make sure you're in the project root
cd C:\Users\olivi\Les-Constructions-Dominic-Cyr

# Navigate to translation scripts
cd translation-scripts

# Run the upload script
node upload-living-environment-translations.js
```

#### Option B: Using PowerShell
```powershell
# Make sure you're in the project root
cd C:\Users\olivi\Les-Constructions-Dominic-Cyr

# Navigate to translation scripts
cd translation-scripts

# Run the upload script
.\upload-living-environment-translations.ps1
```

**Important**: Make sure your file service is running on `http://localhost:8082` before running the upload script.

### Step 3: Update TranslationRegistry.java

After running the upload script, you'll see output like:
```
✅ Uploaded livingEnvironment_en.json
   File ID: abc123-def456-...

✅ Uploaded livingEnvironment_fr.json
   File ID: xyz789-uvw012-...

Update TranslationRegistry.java with:
  fileIdMap.put("en.livingenvironment", "abc123-def456-...");
  fileIdMap.put("fr.livingenvironment", "xyz789-uvw012-...");
```

1. Copy the file IDs from the script output
2. Open `src/main/java/com/ecp/les_constructions_dominic_cyr/backend/utils/translation/dataaccesslayer/TranslationRegistry.java`
3. Replace the placeholder entries:
   ```java
   // Replace these lines:
   fileIdMap.put("en.livingenvironment", "PLACEHOLDER_EN_ID");    
   fileIdMap.put("fr.livingenvironment", "PLACEHOLDER_FR_ID");
   
   // With the actual file IDs:
   fileIdMap.put("en.livingenvironment", "abc123-def456-...");    
   fileIdMap.put("fr.livingenvironment", "xyz789-uvw012-...");
   ```

### Step 4: Deploy to DigitalOcean

To deploy the translations to DigitalOcean:

1. **Update the upload script** to point to your production file service:
   ```bash
   # Set environment variable before running
   export FILE_SERVICE_URL=https://files-service-app-xubs2.ondigitalocean.app
   export BACKEND_URL=https://your-backend-url.ondigitalocean.app
   
   # Then run the script
   node upload-living-environment-translations.js
   ```

2. **Or manually upload** through your file service UI and update the production TranslationRegistry.

### Step 5: Rebuild and Test

1. **Rebuild the backend**:
   ```bash
   # In project root
   ./gradlew clean build
   ```

2. **Restart your development server**:
   ```bash
   cd frontend/les_constructions_dominic_cyr
   npm run dev
   ```

3. **Test the page**:
   - Navigate to: `http://localhost:3000/projects/proj-001-foresta/overview`
   - Click on the "Living Environment" icon
   - You should see the new page with all content

## Page Features

### Header Section
- Large centered title: "FÖRESTA, UN MILIEU DE VIE EXCEPTIONNEL"
- Subtitle: "Au rythme de la nature" (smaller text)

### Description Section
- Detailed description of the Föresta project
- Information about location and proximity

### Amenities Section
- Title: "À PROXIMITÉ DE TOUT !"
- 12 amenity boxes with icons:
  - Ski
  - Golf
  - Vélo (Cycling)
  - Bromont
  - Parc National de la Yamaska
  - Vergers & Vignobles (Orchards & Vineyards)
  - Restaurants
  - Épiceries (Grocery Stores)
  - Magasins (Shops)
  - Hôpitaux (Hospitals)
  - Écoles (Schools)
  - Spas

### Footer Section
- Project signature: "FÖRESTA est un projet signé Les Constructions Dominic Cyr inc."

## Responsive Design

The page is fully responsive with breakpoints for:
- **Desktop**: Full layout with large text and grid
- **Tablet** (768px): Adjusted spacing and font sizes
- **Mobile** (480px): Compact layout, 2-column grid for amenities

## Troubleshooting

### Translations not loading
- Check that the upload script ran successfully
- Verify the file IDs in TranslationRegistry.java
- Ensure the backend is running and accessible
- Check browser console for errors

### Icons not displaying
- Make sure `react-icons` is installed: `npm install react-icons`
- Check that the import statement is correct in LivingEnvironmentPage.jsx

### Route not working
- Verify App.jsx has the new route
- Check that the path matches: `/projects/:projectIdentifier/living-environment`
- Restart the development server

### Styling issues
- Make sure the CSS file is imported in LivingEnvironmentPage.jsx
- Check for CSS conflicts with other stylesheets
- Clear browser cache

## Content Updates

To update the content in the future:

1. **Edit translation files**:
   - `translation-scripts/translation-files/livingEnvironment_en.json`
   - `translation-scripts/translation-files/livingEnvironment_fr.json`

2. **Re-run the upload script**:
   ```bash
   node upload-living-environment-translations.js
   ```

3. **Update TranslationRegistry.java** with new file IDs

4. **Rebuild and restart** the application

## Notes

- The page is currently specific to the Föresta project (`proj-001-foresta`)
- The amenities use Font Awesome icons via `react-icons/fa`
- All text is fully translatable through the translation system
- The design uses a gradient background and hover effects for interactivity
