# Script to upload Customer Forms translation files
# Make sure the file service is running on http://localhost:8082

$fileServiceUrl = "http://localhost:8082/files"
$backendUrl = "http://localhost:8080/api/v1/translations/registry"
$translationsDir = "translation-files"

Write-Host "Uploading Customer Forms translation files..." -ForegroundColor Green

# Upload English translation
Write-Host "`nUploading customerForms_en.json..." -ForegroundColor Yellow
$enResponse = curl.exe -X POST $fileServiceUrl `
    -F "file=@$translationsDir/customerForms_en.json" `
    -F "category=DOCUMENT" `
    -F "projectId=translations" `
    -F "uploadedBy=system"

Write-Host "Response: $enResponse" -ForegroundColor Cyan

# Extract file ID from response
$enFileId = ($enResponse | ConvertFrom-Json).fileId
Write-Host "English file ID: $enFileId" -ForegroundColor Green

# Upload French translation
Write-Host "`nUploading customerForms_fr.json..." -ForegroundColor Yellow
$frResponse = curl.exe -X POST $fileServiceUrl `
    -F "file=@$translationsDir/customerForms_fr.json" `
    -F "category=DOCUMENT" `
    -F "projectId=translations" `
    -F "uploadedBy=system"

Write-Host "Response: $frResponse" -ForegroundColor Cyan

# Extract file ID from response
$frFileId = ($frResponse | ConvertFrom-Json).fileId
Write-Host "French file ID: $frFileId" -ForegroundColor Green

# Try to register with backend
Write-Host "`nRegistering with backend..." -ForegroundColor Yellow
try {
    $enRegResponse = Invoke-RestMethod -Uri "$backendUrl/en/customerforms" -Method POST -Body $enFileId -ContentType "text/plain"
    Write-Host "Registered en.customerforms" -ForegroundColor Green
} catch {
    Write-Host "Could not register with backend (it may not be running)" -ForegroundColor Yellow
}

try {
    $frRegResponse = Invoke-RestMethod -Uri "$backendUrl/fr/customerforms" -Method POST -Body $frFileId -ContentType "text/plain"
    Write-Host "Registered fr.customerforms" -ForegroundColor Green
} catch {
    Write-Host "Could not register with backend (it may not be running)" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Update TranslationRegistry.java with:" -ForegroundColor Yellow
Write-Host "  fileIdMap.put(`"en.customerforms`", `"$enFileId`");" -ForegroundColor Cyan
Write-Host "  fileIdMap.put(`"fr.customerforms`", `"$frFileId`");" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
