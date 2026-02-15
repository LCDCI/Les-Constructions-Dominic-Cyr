const { execSync } = require('child_process');

const pages = [
  'contact',
  'contractorDashboard',
  'contractorTasks',
  'createProjectPage',
  'customerDashboard',
  'customerForms',
  'home',
  'inbox',
  'livingEnvironment',
  'lotMetadata',
  'lotSelectPage',
  'lots',
  'navbar',
  'notfound',
  'ownerDashboard',
  'ownerInquiriesPage',
  'ownerLots',
  'portalLogin',
  'profilePage',
  'projectFilesPage',
  'projectManagement',
  'projectMetadata',
  'projectOverview',
  'projectPhotosPage',
  'projectSchedulePage',
  'projects',
  'quotes',
  'realizations',
  'renovations',
  'reportsPage',
  'residentialProjects',
  'salespersonDashboard',
  'salespersonForms',
  'servererror',
  'taskDetails',
  'unauthorized',
  'usersPage'
];

const fileServiceUrl = 'http://localhost:8082/files';

console.log('🚀 Starting bulk upload to ' + fileServiceUrl);

for (const page of pages) {
  try {
    console.log(`\n-----------------------------------`);
    console.log(`Processing: ${page}`);
    // node upload-translations.js [pageName] [fileServiceUrl]
    execSync(`node upload-translations.js ${page} ${fileServiceUrl}`, { stdio: 'inherit' });
  } catch (error) {
    console.error(`❌ Failed to upload ${page}`);
  }
}

console.log('\n✅ Bulk upload complete.');
