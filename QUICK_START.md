# Translation System - Quick Start Guide

## ✅ Everything is Ready!

All code has been implemented and packages installed. You just need to upload the translation files.

## 🚀 Quick Setup (3 Steps)

### Step 1: Start Services

Make sure your services are running:
- **File Service**: `http://localhost:8082`
- **Backend**: `http://localhost:8080`
- **Frontend**: `http://localhost:3000` (optional for now)

### Step 2: Upload Translation Files

**Easiest way - Use the Node.js script:**

```bash
# Install dependencies if needed
npm install form-data axios

# Run the upload script
node translation-scripts/upload-translations.js
```

This will:
- ✅ Upload both `home_en.json` and `home_fr.json` to the file service
- ✅ Automatically register them with the backend registry
- ✅ Display the file IDs for reference

**Alternative - Use PowerShell:**
```powershell
.\translation-scripts\upload-translations.ps1
```

**Alternative - Use Bash:**
```bash
chmod +x translation-scripts/upload-translations.sh
./translation-scripts/upload-translations.sh
```

### Step 3: Test It!

1. Start frontend: `cd frontend/les_constructions_dominic_cyr && npm run dev`
2. Open `http://localhost:3000`
3. Click the language switcher (EN/FR) in the navbar
4. Watch the page content change language! 🎉

## 📝 What Was Done

- ✅ **Backend**: Translation service, registry, DeepL service, all endpoints
- ✅ **Frontend**: i18n setup, language switcher, Home page translations
- ✅ **Packages**: react-i18next, i18next, js-cookie installed
- ✅ **Files**: Translation JSON files created
- ✅ **File Service**: JSON support added
- ✅ **Scripts**: Upload scripts created for easy setup

## 🎯 How It Works

1. **Language Preference**: Stored in cookies (`i18nextLng`)
2. **Translation Files**: Stored in MinIO via file service
3. **Registry**: Maps language+page to file IDs
4. **Dynamic Loading**: Frontend fetches translations from backend API

## 🐛 Troubleshooting

- **"Unsupported document type"**: Make sure you pulled the latest file service code with JSON support
- **Translations not loading**: Check browser console, verify file service is running
- **Registry not found**: Run the upload script again to register files

## 📚 More Details

See `TRANSLATION_GUIDE.md` for complete documentation on adding translations to new pages.

