/**
 * Test script to verify translation system is working
 * Run with: node test-translations.js
 */

const axios = require('axios');

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';
const FILE_SERVICE_URL = process.env.FILE_SERVICE_URL || 'http://localhost:8082';

async function testTranslations() {
  console.log('🧪 Testing Translation System\n');
  console.log('='.repeat(60));

  // Test 1: Check if backend is running
  console.log('\n1️⃣  Testing Backend Connection...');
  try {
    const response = await axios.get(`${BACKEND_URL}/api/v1/translations/languages`);
    console.log('✅ Backend is running');
    console.log('   Supported languages:', response.data);
  } catch (error) {
    console.error('❌ Backend is not accessible:', error.message);
    console.log('   Make sure the backend is running on', BACKEND_URL);
    return;
  }

  // Test 2: Check registry
  console.log('\n2️⃣  Testing Translation Registry...');
  try {
    const enFileId = await axios.get(`${BACKEND_URL}/api/v1/translations/registry/en/home`);
    console.log('✅ English file ID registered:', enFileId.data);
    
    const frFileId = await axios.get(`${BACKEND_URL}/api/v1/translations/registry/fr/home`);
    console.log('✅ French file ID registered:', frFileId.data);
  } catch (error) {
    if (error.response && error.response.status === 404) {
      console.error('❌ File IDs not registered in registry!');
      console.log('   Run: node upload-translations.js');
      return;
    }
    console.error('❌ Error checking registry:', error.message);
  }

  // Test 3: Check if file service can serve the files
  console.log('\n3️⃣  Testing File Service Access...');
  try {
    // Get file IDs from registry first
    const enFileId = (await axios.get(`${BACKEND_URL}/api/v1/translations/registry/en/home`)).data;
    const frFileId = (await axios.get(`${BACKEND_URL}/api/v1/translations/registry/fr/home`)).data;

    const enFile = await axios.get(`${FILE_SERVICE_URL}/files/${enFileId}`);
    console.log('✅ English file accessible from file service');
    console.log('   File size:', JSON.stringify(enFile.data).length, 'bytes');
    
    const frFile = await axios.get(`${FILE_SERVICE_URL}/files/${frFileId}`);
    console.log('✅ French file accessible from file service');
    console.log('   File size:', JSON.stringify(frFile.data).length, 'bytes');
  } catch (error) {
    console.error('❌ File service error:', error.message);
    if (error.response) {
      console.error('   Status:', error.response.status);
      console.error('   Data:', error.response.data);
    }
  }

  // Test 4: Test translation endpoint
  console.log('\n4️⃣  Testing Translation Endpoint...');
  try {
    const enResponse = await axios.get(`${BACKEND_URL}/api/v1/translations/en`);
    console.log('✅ English translations endpoint working');
    console.log('   Response keys:', Object.keys(enResponse.data.translations || {}));
    console.log('   Sample data:', JSON.stringify(enResponse.data).substring(0, 200) + '...');
    
    const frResponse = await axios.get(`${BACKEND_URL}/api/v1/translations/fr`);
    console.log('✅ French translations endpoint working');
    console.log('   Response keys:', Object.keys(frResponse.data.translations || {}));
  } catch (error) {
    console.error('❌ Translation endpoint error:', error.message);
    if (error.response) {
      console.error('   Status:', error.response.status);
      console.error('   Data:', JSON.stringify(error.response.data).substring(0, 200));
    }
  }

  // Test 5: Test specific page endpoint
  console.log('\n5️⃣  Testing Page Translation Endpoint...');
  try {
    const homeEn = await axios.get(`${BACKEND_URL}/api/v1/translations/en/page/home`);
    console.log('✅ Home page translations (EN) working');
    console.log('   Keys:', Object.keys(homeEn.data.translations || {}));
    
    const homeFr = await axios.get(`${BACKEND_URL}/api/v1/translations/fr/page/home`);
    console.log('✅ Home page translations (FR) working');
    console.log('   Keys:', Object.keys(homeFr.data.translations || {}));
  } catch (error) {
    console.error('❌ Page translation endpoint error:', error.message);
    if (error.response) {
      console.error('   Status:', error.response.status);
    }
  }

  console.log('\n' + '='.repeat(60));
  console.log('✅ Testing complete!');
}

testTranslations().catch(console.error);

