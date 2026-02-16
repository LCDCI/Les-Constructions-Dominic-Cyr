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

        fileIdMap.put("en.contact", "dfdcc350-fdca-4d64-be17-f4c75f0d4a1f");
        fileIdMap.put("fr.contact", "cc125a16-7546-44f2-933f-ec27f8805a8d");

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
        fileIdMap.put("en.ownerlots", "b071f51e-c04d-412c-adb4-4e56cadb734a");
        fileIdMap.put("fr.ownerlots", "bea7f8ce-9d27-4784-a006-0afaac0b3202");

        // Task Details page translations
        fileIdMap.put("en.taskdetails", "0d1566a4-ace6-4341-9b8e-e108bd562091");
        fileIdMap.put("fr.taskdetails", "c4a519cf-957b-46d0-a099-0228e0e933cc");

        // Living Environment page translations
        fileIdMap.put("en.livingenvironment", "07950131-c764-4b1e-9a17-6afbb0618615");
        fileIdMap.put("fr.livingenvironment", "bfad437e-c3e1-43b1-aa10-20978a1e7617");

        // Projects page translations
        fileIdMap.put("en.projects", "ba8bdea5-82ff-46a5-80b5-217356ddd314");
        fileIdMap.put("fr.projects", "7b26f6ac-cf77-4ed8-921a-b641bff89228");

        // Inbox page translations
        fileIdMap.put("en.inbox", "3fbc18df-6d7f-4159-bfce-94431f8d6eeb");
        fileIdMap.put("fr.inbox", "7a7c4dba-747d-4228-b34d-6c83b6647337");

        // Dashboard page translations
        fileIdMap.put("en.ownerdashboard", "fac46b1c-2158-484d-8839-10ff51fdd878");
        fileIdMap.put("fr.ownerdashboard", "a73f68c8-4a84-49c0-b86e-50039a1444c6");
        fileIdMap.put("en.customerdashboard", "cb174189-a044-442c-a9ae-665cddca66a1");
        fileIdMap.put("fr.customerdashboard", "1e6e4cc7-5413-4197-80b9-b0a262a065b0");
        fileIdMap.put("en.salespersondashboard", "d59b185f-d42d-47fb-a4bd-367dd86d0db1");
        fileIdMap.put("fr.salespersondashboard", "b6e4ab2b-80dd-4029-8342-4be54790883e");
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
        fileIdMap.put("en.reportspage", "b5edb946-ecae-4571-b0e5-bbf99910a198");
        fileIdMap.put("fr.reportspage", "86ec6fcd-af43-4fa1-addd-2ec0366fa1be");
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
        fileIdMap.put("en.navbar", "b20fb667-1007-45f0-8c77-021590ef87e2");
        fileIdMap.put("fr.navbar", "e2ad19a5-c365-42fe-b869-3a0de82f9964");
        // Quotes page translations
        fileIdMap.put("en.quotes", "4992e027-03c7-4592-afcf-b6b1b0d1102f");
        fileIdMap.put("fr.quotes", "2a0d3030-93dd-49b9-a1e0-3bc7990db2ed");

        // Customer Forms page translations
        fileIdMap.put("en.customerforms", "341fb5d4-4b0d-4efc-872c-b1e767377b64");
        fileIdMap.put("fr.customerforms", "5b84a408-6ec7-4b0c-81ad-f33ab7fd3fa0");

        // Salesperson Forms page translations
        fileIdMap.put("en.salespersonforms", "bf89b8c0-12d8-43f3-b713-8cf5133a95ac");
        fileIdMap.put("fr.salespersonforms", "0209707e-6022-4e1e-9a0c-4e166bf715e4");

    }

    /**
     * Gets the file ID for a given language and page.
     * 
     * @param language the language code (e.g., "en", "fr")
     * @param page     the page name (e.g., "home", "projects")
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
     * @param page     the page name
     * @param fileId   the file ID from file service
     */
    public void registerFileId(String language, String page, String fileId) {
        String key = language.toLowerCase() + "." + page.toLowerCase();
        fileIdMap.put(key, fileId);
    }

    /**
     * Checks if a translation file exists for the given language and page.
     * 
     * @param language the language code
     * @param page     the page name
     * @return true if file ID exists, false otherwise
     */
    public boolean hasTranslation(String language, String page) {
        return getFileId(language, page) != null;
    }
}
