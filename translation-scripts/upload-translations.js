/**
 * Node.js script to upload translation files to the file service
 * 
 * Usage:
 *   node upload-translations.js [pageName]
 * 
 * Examples:
 *   node upload-translations.js home        # Upload home_en.json and home_fr.json
 *   node upload-translations.js projects    # Upload projects_en.json and projects_fr.json
 *   node upload-translations.js renovations # Upload renovations_en.json and renovations_fr.json
 *   node upload-translations.js             # Defaults to 'home'
 * 
 * Make sure the file service is running on http://localhost:8082
 */

const fs = require('fs');
const path = require('path');
const FormData = require('form-data');
const axios = require('axios');

// Get file service URL from command line or environment, default to production
const FILE_SERVICE_URL = process.argv[3] || process.env.FILE_SERVICE_URL || 'https://files-service-app-xubs2.ondigitalocean.app/files';
const TRANSLATIONS_DIR = path.join(__dirname, 'translation-files');

// Get page name from command line argument, default to 'home'
const pageName = process.argv[2] || 'home';

console.log(`🌐 File Service URL: ${FILE_SERVICE_URL}`);

async function uploadTranslationFile(filename, language, pageName) {
  const filePath = path.join(TRANSLATIONS_DIR, filename);
  
  if (!fs.existsSync(filePath)) {
    throw new Error(`File not found: ${filePath}`);
  }

  const formData = new FormData();
  formData.append('file', fs.createReadStream(filePath));
  formData.append('category', 'DOCUMENT');
  formData.append('projectId', 'translations');
  formData.append('uploadedBy', 'system');
  formData.append('uploaderRole', 'OWNER');

  console.log(`\nUploading ${filename}...`);
  
  try {
    const response = await axios.post(FILE_SERVICE_URL, formData, {
      headers: formData.getHeaders(),
    });

    const fileId = response.data.fileId;
    console.log(`✅ Uploaded successfully!`);
    console.log(`   File ID: ${fileId}`);
    
    return { language, fileId, pageName };
  } catch (error) {
    console.error(`❌ Error uploading ${filename}:`, error.message);
    if (error.response) {
      console.error(`   Status: ${error.response.status}`);
      console.error(`   Data:`, error.response.data);
    }
    throw error;
  }
}

async function main() {
  console.log('🚀 Starting translation file upload...\n');
  console.log(`📄 Page: ${pageName}`);
  console.log(`🌐 File Service: ${FILE_SERVICE_URL}\n`);

  const results = [];

  try {
    // Upload English translation
    const enFilename = `${pageName}_en.json`;
    const enResult = await uploadTranslationFile(enFilename, 'en', pageName);
    results.push(enResult);

    // Upload French translation
    const frFilename = `${pageName}_fr.json`;
    const frResult = await uploadTranslationFile(frFilename, 'fr', pageName);
    results.push(frResult);

    // Display summary
    console.log('\n' + '='.repeat(50));
    console.log('✅ All files uploaded successfully!');
    console.log('='.repeat(50));
    
    // Try to register files with backend if available
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
    console.log(`\n📝 Registering files with backend at ${backendUrl}...\n`);
    
    for (const { language, fileId, pageName: page } of results) {
      try {
        await axios.post(
          `${backendUrl}/api/v1/translations/registry/${language}/${page}`,
          fileId,
          { headers: { 'Content-Type': 'text/plain' } }
        );
        console.log(`✅ Registered ${language}.${page} -> ${fileId}`);
      } catch (error) {
        console.log(`⚠️  Could not register ${language}.${page} (backend may not be running)`);
        console.log(`   Manual registration: fileIdMap.put("${language}.${page}", "${fileId}");`);
      }
    }
    
    console.log('\n' + '='.repeat(50));
    console.log('📝 Alternative: Update TranslationRegistry.java manually:\n');
    
    results.forEach(({ language, fileId, pageName: page }) => {
      console.log(`   fileIdMap.put("${language}.${page}", "${fileId}");`);
    });
    
    console.log('\n' + '='.repeat(50));
  } catch (error) {
    console.error('\n❌ Upload failed:', error.message);
    process.exit(1);
  }
}

// Check if form-data is available
try {
  require('form-data');
} catch (e) {
  console.error('❌ Missing dependency: form-data');
  console.log('   Install it with: npm install form-data axios');
  process.exit(1);
}

main();

