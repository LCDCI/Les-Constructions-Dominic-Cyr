# Script to upload translation files to the file service
# Make sure the file service is running on http://localhost:8082

$fileServiceUrl = "http://localhost:8082/files"
$translationsDir = "translation-files"

Write-Host "Uploading translation files to file service..." -ForegroundColor Green

# Upload English translation
Write-Host "`nUploading home_en.json..." -ForegroundColor Yellow
$enResponse = curl.exe -X POST $fileServiceUrl `
    -F "file=@$translationsDir/home_en.json" `
    -F "category=DOCUMENT" `
    -F "projectId=translations" `
    -F "uploadedBy=system"

Write-Host "Response: $enResponse" -ForegroundColor Cyan

# Extract file ID from response (assuming JSON response with fileId field)
# You may need to parse this manually
$enFileId = ($enResponse | ConvertFrom-Json).fileId
Write-Host "English file ID: $enFileId" -ForegroundColor Green

# Upload French translation
Write-Host "`nUploading home_fr.json..." -ForegroundColor Yellow
$frResponse = curl.exe -X POST $fileServiceUrl `
    -F "file=@$translationsDir/home_fr.json" `
    -F "category=DOCUMENT" `
    -F "projectId=translations" `
    -F "uploadedBy=system"

Write-Host "Response: $frResponse" -ForegroundColor Cyan

# Extract file ID from response
$frFileId = ($frResponse | ConvertFrom-Json).fileId
Write-Host "French file ID: $frFileId" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Update TranslationRegistry.java with:" -ForegroundColor Yellow
Write-Host "  fileIdMap.put(`"en.home`", `"$enFileId`");" -ForegroundColor Cyan
Write-Host "  fileIdMap.put(`"fr.home`", `"$frFileId`");" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green

