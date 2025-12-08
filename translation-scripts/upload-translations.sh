#!/bin/bash
# Script to upload translation files to the file service
# Make sure the file service is running on http://localhost:8082

FILE_SERVICE_URL="http://localhost:8082/files"
TRANSLATIONS_DIR="translation-files"

echo "Uploading translation files to file service..."

# Upload English translation
echo ""
echo "Uploading home_en.json..."
EN_RESPONSE=$(curl -X POST $FILE_SERVICE_URL \
  -F "file=@$TRANSLATIONS_DIR/home_en.json" \
  -F "category=DOCUMENT" \
  -F "projectId=translations" \
  -F "uploadedBy=system")

echo "Response: $EN_RESPONSE"

# Extract file ID (assuming JSON response)
EN_FILE_ID=$(echo $EN_RESPONSE | grep -o '"fileId":"[^"]*' | cut -d'"' -f4)
echo "English file ID: $EN_FILE_ID"

# Upload French translation
echo ""
echo "Uploading home_fr.json..."
FR_RESPONSE=$(curl -X POST $FILE_SERVICE_URL \
  -F "file=@$TRANSLATIONS_DIR/home_fr.json" \
  -F "category=DOCUMENT" \
  -F "projectId=translations" \
  -F "uploadedBy=system")

echo "Response: $FR_RESPONSE"

# Extract file ID
FR_FILE_ID=$(echo $FR_RESPONSE | grep -o '"fileId":"[^"]*' | cut -d'"' -f4)
echo "French file ID: $FR_FILE_ID"

echo ""
echo "========================================"
echo "Update TranslationRegistry.java with:"
echo "  fileIdMap.put(\"en.home\", \"$EN_FILE_ID\");"
echo "  fileIdMap.put(\"fr.home\", \"$FR_FILE_ID\");"
echo "========================================"

