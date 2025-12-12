package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.dataaccesslayer;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping translation file identifiers to file service file IDs.
 * Format: {language}.{page} -> fileId
 * 
 * Example:
 * - en.home -> <file-id-1>
 * - fr.home -> <file-id-2>
 * - en.projects -> <file-id-3>
 * - fr.projects -> <file-id-4>
 */
@Component
public class TranslationRegistry {
    
    private final Map<String, String> fileIdMap = new HashMap<>();
    
    public TranslationRegistry() {
        // Initialize with empty map
        // File IDs will be added after uploading translation files to file service
        // 
        // To add file IDs:
        // 1. Upload translation JSON files using upload-translations.ps1 or upload-translations.sh
        // 2. Copy the file IDs from the script output
        // 3. Add them below:
        //
        // Example (replace with actual file IDs after upload):
        // fileIdMap.put("en.home", "your-english-file-id-here");
        // fileIdMap.put("fr.home", "your-french-file-id-here");
        
        // Contact page translations
        fileIdMap.put("en.contact", "3c4ef848-4b70-4bfa-9596-e2fdc5541386");
        fileIdMap.put("fr.contact", "7853c9d6-52c2-4b2e-b9c0-fd10f2696fe3");
    }
    
    /**
     * Gets the file ID for a given language and page.
     * 
     * @param language the language code (e.g., "en", "fr")
     * @param page the page name (e.g., "home", "projects")
     * @return the file ID, or null if not found
     */
    public String getFileId(String language, String page) {
        String key = language.toLowerCase() + "." + page.toLowerCase();
        return fileIdMap.get(key);
    }
    
    /**
     * Registers a file ID for a language and page.
     * 
     * @param language the language code
     * @param page the page name
     * @param fileId the file ID from file service
     */
    public void registerFileId(String language, String page, String fileId) {
        String key = language.toLowerCase() + "." + page.toLowerCase();
        fileIdMap.put(key, fileId);
    }
    
    /**
     * Checks if a translation file exists for the given language and page.
     * 
     * @param language the language code
     * @param page the page name
     * @return true if file ID exists, false otherwise
     */
    public boolean hasTranslation(String language, String page) {
        return getFileId(language, page) != null;
    }
}

