#!/usr/bin/env node
/**
 * Upload script specifically for Living Environment translations
 * Uploads livingEnvironment_en.json and livingEnvironment_fr.json
 */

const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// Configuration
const fileServiceUrl = process.env.FILE_SERVICE_URL || 'http://localhost:8082';
const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
const translationsDir = path.join(__dirname, 'translation-files');

const files = [
  { language: 'en', filename: 'livingEnvironment_en.json', pageName: 'livingenvironment' },
  { language: 'fr', filename: 'livingEnvironment_fr.json', pageName: 'livingenvironment' }
];

async function uploadFile(filepath, filename, language, pageName) {
  try {
    const form = new FormData();
    form.append('file', fs.createReadStream(filepath));
    form.append('category', 'DOCUMENT');
    form.append('projectId', 'translations');
    form.append('uploadedBy', 'system');

    const response = await axios.post(`${fileServiceUrl}/files`, form, {
      headers: form.getHeaders()
    });

    const fileId = response.data.fileId;
    console.log(`✅ Uploaded ${filename}`);
    console.log(`   File ID: ${fileId}`);
    
    return { language, fileId, pageName };
  } catch (error) {
    console.error(`❌ Failed to upload ${filename}:`, error.message);
    throw error;
  }
}

async function main() {
  try {
    console.log('🚀 Starting Living Environment translation file upload...\n');
    
    const results = [];
    
    for (const file of files) {
      const filepath = path.join(translationsDir, file.filename);
      
      if (!fs.existsSync(filepath)) {
        console.error(`❌ File not found: ${filepath}`);
        continue;
      }
      
      console.log(`Uploading ${file.filename}...`);
      const result = await uploadFile(filepath, file.filename, file.language, file.pageName);
      results.push(result);
      console.log('');
    }
    
    console.log('📝 Registering files with backend at ${backendUrl}...\n');
    
    for (const { language, fileId, pageName } of results) {
      try {
        await axios.post(
          `${backendUrl}/api/v1/translations/registry/${language}/${pageName}`,
          fileId,
          { headers: { 'Content-Type': 'text/plain' } }
        );
        console.log(`✅ Registered ${language}.${pageName} -> ${fileId}`);
      } catch (error) {
        console.log(`⚠️  Could not register ${language}.${pageName} (backend may not be running)`);
        console.log(`   Manual registration needed in TranslationRegistry.java`);
      }
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('📝 To update TranslationRegistry.java manually:\n');
    
    results.forEach(({ language, fileId, pageName }) => {
      console.log(`  fileIdMap.put("${language}.${pageName}", "${fileId}");`);
    });
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ Upload complete!\n');
    
  } catch (error) {
    console.error('\n❌ Upload failed:', error.message);
    process.exit(1);
  }
}

main();
