/**
 * Test script for Mailer Service
 * 
 * Usage:
 *   1. Start the mailer service: cd mailer-service && go run cmd/mailer/main.go
 *   2. Set environment variables (or use mailer.env):
 *      - SMTP_SERVER=smtp.mailersend.net
 *      - SMTP_USER=MS_SEsYLJ@trial-0r83ql3086mlzw1j.mlsender.net
 *      - SMTP_PASS=5JjZ69nqW71dLeC1
 *      - MAILER_PORT=8083
 *   3. Run this script: node test-mailer-service.js
 */

const axios = require('axios');

const MAILER_SERVICE_URL = process.env.MAILER_SERVICE_URL || 'http://localhost:8083';
const TEST_EMAIL = process.env.TEST_EMAIL || 'your-email@example.com'; // Change this to your email

async function testMailerService() {
  console.log('🧪 Testing Mailer Service...\n');
  console.log(`📍 Service URL: ${MAILER_SERVICE_URL}`);
  console.log(`📧 Test Email: ${TEST_EMAIL}\n`);

  // Test 1: Health check (if available)
  console.log('Test 1: Checking service availability...');
  try {
    const healthResponse = await axios.get(`${MAILER_SERVICE_URL}/metrics`, { timeout: 5000 });
    console.log('✅ Service is running (metrics endpoint accessible)\n');
  } catch (error) {
    if (error.code === 'ECONNREFUSED') {
      console.log('❌ Service is not running. Please start it first.\n');
      console.log('   Run: cd mailer-service && go run cmd/mailer/main.go\n');
      process.exit(1);
    }
    // Metrics endpoint might not be accessible, but service might still work
    console.log('⚠️  Could not reach metrics endpoint, but continuing...\n');
  }

  // Test 2: Send a test email
  console.log('Test 2: Sending test email...');
  const emailPayload = {
    EmailSendTo: TEST_EMAIL,
    EmailTitle: 'Test Email from Les Constructions Dominic Cyr',
    Body: `
      <html>
        <body style="font-family: Arial, sans-serif; padding: 20px;">
          <h1 style="color: #2c3e50;">Test Email</h1>
          <p>This is a test email from the Mailer Service.</p>
          <p>If you received this, the mailer service is working correctly! ✅</p>
          <hr>
          <p style="color: #7f8c8d; font-size: 12px;">
            Sent from Les Constructions Dominic Cyr Mailer Service
          </p>
        </body>
      </html>
    `,
    SenderName: 'Les Constructions Dominic Cyr'
  };

  try {
    const response = await axios.post(`${MAILER_SERVICE_URL}/mail`, emailPayload, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 30000 // 30 seconds timeout for SMTP
    });

    console.log('✅ Email sent successfully!');
    console.log(`   Response: ${JSON.stringify(response.data)}\n`);
    console.log(`📬 Check your inbox at: ${TEST_EMAIL}\n`);
  } catch (error) {
    if (error.response) {
      console.log('❌ Email sending failed!');
      console.log(`   Status: ${error.response.status}`);
      console.log(`   Error: ${JSON.stringify(error.response.data)}\n`);
    } else if (error.request) {
      console.log('❌ Could not reach mailer service!');
      console.log(`   Error: ${error.message}\n`);
      console.log('   Make sure the service is running on:', MAILER_SERVICE_URL);
    } else {
      console.log('❌ Unexpected error:', error.message);
    }
    process.exit(1);
  }

  // Test 3: Test validation (missing required field)
  console.log('Test 3: Testing validation (should fail)...');
  try {
    await axios.post(`${MAILER_SERVICE_URL}/mail`, {
      EmailTitle: 'Invalid Email Test'
      // Missing EmailSendTo - should fail validation
    }, {
      headers: { 'Content-Type': 'application/json' }
    });
    console.log('❌ Validation test failed - should have rejected invalid payload\n');
  } catch (error) {
    if (error.response && error.response.status === 400) {
      console.log('✅ Validation working correctly (rejected invalid payload)\n');
    } else {
      console.log('⚠️  Unexpected error during validation test:', error.message);
    }
  }

  // Test 4: Test with minimal payload
  console.log('Test 4: Testing minimal payload...');
  const minimalPayload = {
    EmailSendTo: TEST_EMAIL,
    EmailTitle: 'Minimal Test Email',
    Body: '<p>Minimal test</p>'
  };

  try {
    const response = await axios.post(`${MAILER_SERVICE_URL}/mail`, minimalPayload, {
      headers: { 'Content-Type': 'application/json' },
      timeout: 30000
    });
    console.log('✅ Minimal payload accepted and email sent\n');
  } catch (error) {
    console.log('❌ Minimal payload test failed:', error.message);
  }

  console.log('✨ All tests completed!\n');
  console.log('💡 To test from backend, ensure:');
  console.log('   1. Backend has MAILER_SERVICE_BASE_URL configured');
  console.log('   2. Backend has a MailerService client implemented');
  console.log('   3. Backend calls the mailer service when needed\n');
}

// Run tests
testMailerService().catch(error => {
  console.error('Fatal error:', error);
  process.exit(1);
});
