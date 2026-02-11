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
        fileIdMap.put("en.home", "cb5d4f09-eabb-47df-9161-866ba10bce72");
        fileIdMap.put("fr.home", "edfca72f-b970-410c-b88c-2f3a97301662");
        
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
        fileIdMap.put("en.projectoverview", "9a9f9bb2-c3da-4896-a452-57b6156e1d3b");
        fileIdMap.put("fr.projectoverview", "84fa52c6-e821-43be-872a-6d8cc099677d");

        fileIdMap.put("en.lots", "dd3ea708-475f-4cef-ab1c-4303331cf803");
        fileIdMap.put("fr.lots", "9ee50174-382f-467f-b30c-5f64ca470335");

        // Owner Lots page translations
        fileIdMap.put("en.ownerlots", "62178381-21ab-415a-a604-9d80c5d7774a");
        fileIdMap.put("fr.ownerlots", "29509c00-9273-4df9-96ae-6fba61a0d1b4");

        // Task Details page translations
        fileIdMap.put("en.taskdetails", "0d1566a4-ace6-4341-9b8e-e108bd562091");
        fileIdMap.put("fr.taskdetails", "c4a519cf-957b-46d0-a099-0228e0e933cc");

        // Living Environment page translations
        fileIdMap.put("en.livingenvironment", "99949cf5-8d72-4155-aaea-3df009823664");
        fileIdMap.put("fr.livingenvironment", "1e49e1c9-1dc8-4ecf-8212-47e51d62abd2");

        // Projects page translations
        fileIdMap.put("en.projects", "ba8bdea5-82ff-46a5-80b5-217356ddd314");
        fileIdMap.put("fr.projects", "7b26f6ac-cf77-4ed8-921a-b641bff89228");

        // Inbox page translations
        fileIdMap.put("en.inbox", "3fbc18df-6d7f-4159-bfce-94431f8d6eeb");
        fileIdMap.put("fr.inbox", "7a7c4dba-747d-4228-b34d-6c83b6647337");

        // Dashboard page translations
        fileIdMap.put("en.ownerdashboard", "64afdd86-06a3-4a16-b9c9-628dbc34d475");
        fileIdMap.put("fr.ownerdashboard", "bf1b332f-7866-4910-99f7-1f09a324ea53");
        fileIdMap.put("en.customerdashboard", "bc1fa28b-dc6e-4653-b3e9-b0a9ce3bfa64");
        fileIdMap.put("fr.customerdashboard", "6742d6a9-bcfd-4780-b2ec-5c50c27e9493");
        fileIdMap.put("en.salespersondashboard", "6792727a-faac-4fa1-af3b-0c850c6ed022");
        fileIdMap.put("fr.salespersondashboard", "3379cd57-8243-414f-b891-2fcc7a19bd6c");
        fileIdMap.put("en.contractordashboard", "129b09a5-32d9-426d-98b9-f5da4e2555da");
        fileIdMap.put("fr.contractordashboard", "6230c4c3-3d42-4b65-bd0c-b402dc24ac9b");

        // User management page translations
        fileIdMap.put("en.userspage", "9dd08e21-bfad-452f-9461-8db4c4a9f53a");
        fileIdMap.put("fr.userspage", "8b464bee-4e4b-49cc-b679-0154bfbe5a07");
        fileIdMap.put("en.profilepage", "18a23a69-2f38-4fc4-8d73-748d815ec849");
        fileIdMap.put("fr.profilepage", "31ed222a-2651-4d7f-8624-5a19f5a15b88");

        // Other page translations
        fileIdMap.put("en.ownerinquiriespage", "214a7763-2400-40f3-9fa8-6a24c3e4ef34");
        fileIdMap.put("fr.ownerinquiriespage", "7a5b33bd-0cf8-4b8d-99e8-85e91dc4c40d");
        fileIdMap.put("en.portallogin", "c2bfbed4-4863-4ece-962d-615d1ef01d94");
        fileIdMap.put("fr.portallogin", "01ec1381-fdab-47ff-b576-80f62cab4d89");
        fileIdMap.put("en.reportspage", "27cbcb6c-935e-4758-a420-fb81fd62b0b2");
        fileIdMap.put("fr.reportspage", "1ac3ffaa-0613-4b27-94c2-34cf08b1d62c");
        fileIdMap.put("en.unauthorized", "ec316287-8fde-44db-b51e-e3692b9ce510");
        fileIdMap.put("fr.unauthorized", "8fa54e9d-84eb-444a-aa9f-d7eb986c68a8");

        // Project page translations
        fileIdMap.put("en.createprojectpage", "32d23b1a-805a-40dc-a311-bfd9b4614339");
        fileIdMap.put("fr.createprojectpage", "5cd86972-f085-4edd-9c87-ad07ec3314f2");
        fileIdMap.put("en.lotselectpage", "0738514f-6b6f-4dd2-a4f9-b57e83a007df");
        fileIdMap.put("fr.lotselectpage", "c60d9ed6-83f4-4c22-9466-8278032e7edd");
        fileIdMap.put("en.projectfilespage", "9ad4557e-ebec-4d86-9c55-a848e81a9e17");
        fileIdMap.put("fr.projectfilespage", "fe765f9e-d3bc-4da8-a65d-d966c3f213d2");
        fileIdMap.put("en.projectphotospage", "d9d1892f-2b9a-4135-af0b-9292e21a222b");
        fileIdMap.put("fr.projectphotospage", "96a109f6-15f9-4b27-9cd7-096850d232a8");
        fileIdMap.put("en.projectschedulepage", "54f8eebf-9af5-4376-944e-a87cc268b442");
        fileIdMap.put("fr.projectschedulepage", "718dc85f-6582-4a62-9be8-83a76602aa0b");
        
        // Project Metadata page translations
        fileIdMap.put("en.projectmetadata", "09e765c8-b637-4199-8349-950db3ccf1df");
        fileIdMap.put("fr.projectmetadata", "8a02a6c8-e64c-4d8c-8109-c1a2e86c27ec");
        
        // Lot Metadata page translations
        fileIdMap.put("en.lotmetadata", "fedbe51f-2362-49c7-bbc7-d91055f3d8a8");
        fileIdMap.put("fr.lotmetadata", "58a85d61-34dd-457a-ad89-4de96056a19a");

        // Contractor Tasks page translations
        fileIdMap.put("en.contractortasks", "30c5add1-5926-40c4-80d9-8e56714e0d74");
        fileIdMap.put("fr.contractortasks", "a62edee1-b73f-41e6-a727-ab23e29f03a4");

        // Navbar translations (for role-specific navbars)
        fileIdMap.put("en.navbar", "c3d1ed30-20e7-4b87-8c15-8f18ff19489a");
        fileIdMap.put("fr.navbar", "c9ed527b-48a8-427f-9248-da9e87efbf24");
        // Quotes page translations
        fileIdMap.put("en.quotes", "946a87c9-b8bb-465e-a463-8d5397c72ab4");
        fileIdMap.put("fr.quotes", "c6f01f88-86a9-4811-8449-1ac1bd492910");

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
