# Translation Scripts

Utility scripts for managing translation files in the Les Constructions Dominic Cyr application.

## Scripts

### `upload-translations.js` ⭐ **Recommended**

Node.js script to upload translation JSON files to the file service and automatically register them with the backend.

**Usage:**
```bash
node scripts/upload-translations.js [pageName]

# Examples:
node scripts/upload-translations.js home        # Upload home translations
node scripts/upload-translations.js renovations # Upload renovations translations
node scripts/upload-translations.js projects    # Upload projects translations
node scripts/upload-translations.js             # Defaults to 'home'
```

**What it does:**
- Uploads translation files from `translation-scripts/translation-files/` to the file service
- Automatically registers file IDs with the backend registry
- Works on Windows, Mac, and Linux

**Requirements:**
- Node.js installed
- Dependencies: `npm install form-data axios` (in project root)

---

### `upload-translations.ps1`

PowerShell alternative for Windows users.

**Usage:**
```powershell
.\scripts\upload-translations.ps1
```

**Note:** Does not automatically register with backend (manual step required).

---

### `upload-translations.sh`

Bash alternative for Linux/Mac users.

**Usage:**
```bash
chmod +x scripts/upload-translations.sh
./scripts/upload-translations.sh
```

**Note:** Does not automatically register with backend (manual step required).

---

### `test-translations.js`

Diagnostic tool to verify the translation system is working correctly.

**Usage:**
```bash
node scripts/test-translations.js
```

**What it tests:**
1. Backend connection
2. Translation registry (file IDs)
3. File service access
4. Translation API endpoints
5. Page-specific endpoints

**Use when:**
- Troubleshooting translation issues
- Verifying setup after changes
- Testing if registry is populated

---

## Quick Reference

| Script | Platform | Auto-register | Best For |
|--------|----------|---------------|----------|
| `upload-translations.js` | All | ✅ Yes | Primary method |
| `upload-translations.ps1` | Windows | ❌ No | PowerShell users |
| `upload-translations.sh` | Linux/Mac | ❌ No | Bash users |
| `test-translations.js` | All | N/A | Troubleshooting |

---

## File Locations

- **Translation files**: `translation-scripts/translation-files/`
- **File service**: `http://localhost:8082`
- **Backend**: `http://localhost:8080`

---

## See Also

- `TRANSLATION_GUIDE.md` - Complete guide for adding translations to new pages
- `QUICK_START.md` - Quick setup instructions

