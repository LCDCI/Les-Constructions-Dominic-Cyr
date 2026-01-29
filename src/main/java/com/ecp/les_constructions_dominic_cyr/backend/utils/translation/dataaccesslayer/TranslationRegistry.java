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
        fileIdMap.put("en.home", "4907b518-7b33-4b99-9c7f-927c67aa4a8a");
        fileIdMap.put("fr.home", "6bae8020-dd6e-4a10-93f8-d00f1348597d");
        
        // Contact page translations
        fileIdMap.put("en.contact", "54897f58-2f88-4e2a-81b2-cced943541bb");
        fileIdMap.put("fr.contact", "bb7203b2-975a-43be-9d69-5a2847543cec");
        
        // Renovations page translations
        fileIdMap.put("en.renovations", "c73f88fc-934c-4634-a523-917d88ec7dc9");
        fileIdMap.put("fr.renovations", "64d36b39-21d4-4d6a-b5b4-cf1dd670eb0a");
        
        // Realizations page translations
        fileIdMap.put("en.realizations", "cbb6c67a-bd74-4c07-8a04-95706446183a");
        fileIdMap.put("fr.realizations", "8fa54390-8e3c-4bcc-a409-f729572e2bde");
        
        // Residential Projects page translations
        fileIdMap.put("en.residentialprojects", "2323ba44-a51f-4385-b102-5559f04ef0c0");
        fileIdMap.put("fr.residentialprojects", "edc7a148-3150-48e5-b20f-e399f0d9911c");
        
        // Project Management page translations
        fileIdMap.put("en.projectmanagement", "94942154-b27b-4b48-a830-8623cd1275a8");
        fileIdMap.put("fr.projectmanagement", "a33e3eb0-83f0-4af6-8691-5684d9253298");
        
        // 404 Not Found page translations
        fileIdMap.put("en.notfound", "c0e10c2b-51e6-4ac3-9802-81f8b5ed7686");
        fileIdMap.put("fr.notfound", "995a0589-e141-40b0-b87e-a895273442c7");
        
        // 500 Server Error page translations
        fileIdMap.put("en.servererror", "befe6470-56ab-4e41-ae87-72e7f02a20eb");
        fileIdMap.put("fr.servererror", "f54a08b1-b0f6-4fda-9469-dac983b1ef42");
        
        // Project Overview page translations
        fileIdMap.put("en.projectoverview", "b690c9f9-a2ee-4a49-9e28-2d87cb3dc15e");
        fileIdMap.put("fr.projectoverview", "78c97b4d-2cdd-45e7-93b9-0e5e9d23d668");

        fileIdMap.put("en.lots", "dd3ea708-475f-4cef-ab1c-4303331cf803");
        fileIdMap.put("fr.lots", "9ee50174-382f-467f-b30c-5f64ca470335");

        // Owner Lots page translations
        fileIdMap.put("en.ownerlots", "62178381-21ab-415a-a604-9d80c5d7774a");
        fileIdMap.put("fr.ownerlots", "29509c00-9273-4df9-96ae-6fba61a0d1b4");

        // Living Environment page translations
        fileIdMap.put("en.livingenvironment", "PLACEHOLDER_EN_LIVINGENVIRONMENT");
        fileIdMap.put("fr.livingenvironment", "PLACEHOLDER_FR_LIVINGENVIRONMENT");

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

