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
        // Home page translations
        fileIdMap.put("en.home", "f129c133-d8e3-4c4f-95b4-47d06d4635ad");
        fileIdMap.put("fr.home", "34da3a9d-fa77-4687-8233-a26ee24eab5e");
        
        // Contact page translations
        fileIdMap.put("en.contact", "de84e96b-4477-48e3-a643-68f622533285");
        fileIdMap.put("fr.contact", "e9563bff-cd4b-4621-9d1c-502b9047bd09");
        
        // Renovations page translations
        fileIdMap.put("en.renovations", "485cb6a4-ed37-4229-8555-e21d2d47493c");
        fileIdMap.put("fr.renovations", "3d6e56d3-758a-4f44-8045-b71b64ffcbd2");
        
        // Realizations page translations
        fileIdMap.put("en.realizations", "879ef57b-ea47-4083-9239-49e5eb2e98e9");
        fileIdMap.put("fr.realizations", "6f8e3efa-d5ca-4c7a-934d-85ac58c5537e");
        
        // Residential Projects page translations
        fileIdMap.put("en.residentialProjects", "93633f9e-9008-4b82-8eac-2db3cd9e7b4e");
        fileIdMap.put("fr.residentialProjects", "8395870d-96f5-4c70-abf3-87c7efd96118");
        
        // 404 Not Found page translations
        fileIdMap.put("en.notfound", "3f3f44c5-0f84-4fe3-839e-2b009ace7dea");
        fileIdMap.put("fr.notfound", "40385af1-5824-4811-8781-048b4d001272");
        
        // 500 Server Error page translations
        fileIdMap.put("en.servererror", "881cfbd7-86cb-45cd-a118-ffa819c4f824");
        fileIdMap.put("fr.servererror", "2dbfa250-fd3e-4cdf-9a40-651b21adb8ab");
        
        // Project Overview page translations
        fileIdMap.put("en.projectOverview", "02c23091-2191-49a8-9d47-59329a7e90f7");
        fileIdMap.put("fr.projectOverview", "adf40514-e136-4fc3-b2bf-323d363bcf8f");
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

