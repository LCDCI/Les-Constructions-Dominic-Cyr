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
        fileIdMap.put("en.home", "c9bef8d7-cc81-458e-8517-6d9ff5c0b9ac");
        fileIdMap.put("fr.home", "b4d690e7-1a03-4296-87be-5790120ba40a");

        fileIdMap.put("en.contact", "5f96bbc3-023b-49ee-a808-f737202fafb9");
        fileIdMap.put("fr.contact", "a03ca984-d7ed-4e9a-80c7-935be30b279c");

        // Renovations page translations
        fileIdMap.put("en.renovations", "b31de4b2-3a74-492e-b366-8874db534e02");
        fileIdMap.put("fr.renovations", "58755bbb-1e97-41d1-8a66-db3846a0722e");

        // Realizations page translations
        fileIdMap.put("en.realizations", "df6ba6de-ad9a-4d35-9f4e-8b31065413d4");
        fileIdMap.put("fr.realizations", "29812add-7bdb-4e01-b3e0-4ad7f6754500");

        // Residential Projects page translations
        fileIdMap.put("en.residentialprojects", "c45abc19-a1b8-4ba3-a879-3b06a8045e32");
        fileIdMap.put("fr.residentialprojects", "41a333de-ee3f-4942-9b67-8de15bb8487b");

        // Project Management page translations
        fileIdMap.put("en.projectmanagement", "98e1603e-2435-4d18-8d9a-adfe4d43e5d4");
        fileIdMap.put("fr.projectmanagement", "ed0ddd9f-3378-4f95-bbac-0c4087fb79c4");

        // 404 Not Found page translations
        fileIdMap.put("en.notfound", "3e59cebe-cb8d-456f-8abf-f87df0c5cc05");
        fileIdMap.put("fr.notfound", "bf1921c3-3ff3-466f-8cc0-cbe99aa71358");

        // 500 Server Error page translations
        fileIdMap.put("en.servererror", "a67e77e4-9ad3-425d-9b6a-68a782982d74");
        fileIdMap.put("fr.servererror", "8c925380-e59c-4749-883f-f37b2049f20c");

        // Project Overview page translations
        fileIdMap.put("en.projectoverview", "76ff0e02-6599-4f44-b730-2e3d654ff5ca");
        fileIdMap.put("fr.projectoverview", "8d4bb23a-8b6b-4f6b-a42a-4419d5dcf306");

        fileIdMap.put("en.lots", "773222c5-e0bb-4575-a4ad-590003d26f92");
        fileIdMap.put("fr.lots", "2e09292e-61e4-4c52-8736-702674a78817");

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
        fileIdMap.put("en.salespersondashboard", "51cf8dbf-4d8c-4cc4-8575-b2a7397bbcc3");
        fileIdMap.put("fr.salespersondashboard", "78d36821-f798-4a60-be10-36ad0abf66e0");
        fileIdMap.put("en.contractordashboard", "129b09a5-32d9-426d-98b9-f5da4e2555da");
        fileIdMap.put("fr.contractordashboard", "6230c4c3-3d42-4b65-bd0c-b402dc24ac9b");

        // User management page translations
        fileIdMap.put("en.userspage", "861ec6cd-1bf0-405e-83f4-4dc1c31a98b7");
        fileIdMap.put("fr.userspage", "b44dba45-75ec-41b3-8985-51ba838936e1");
        fileIdMap.put("en.profilepage", "18a23a69-2f38-4fc4-8d73-748d815ec849");
        fileIdMap.put("fr.profilepage", "31ed222a-2651-4d7f-8624-5a19f5a15b88");

        // Other page translations
        fileIdMap.put("en.ownerinquiriespage", "214a7763-2400-40f3-9fa8-6a24c3e4ef34");
        fileIdMap.put("fr.ownerinquiriespage", "7a5b33bd-0cf8-4b8d-99e8-85e91dc4c40d");
        fileIdMap.put("en.portallogin", "c2bfbed4-4863-4ece-962d-615d1ef01d94");
        fileIdMap.put("fr.portallogin", "01ec1381-fdab-47ff-b576-80f62cab4d89");
        fileIdMap.put("en.reportspage", "cc48e2b3-894c-478c-a6ac-758e766e524b");
        fileIdMap.put("fr.reportspage", "b580a52f-cb2a-45d4-9c45-9b3f499c5988");
        fileIdMap.put("en.unauthorized", "ec316287-8fde-44db-b51e-e3692b9ce510");
        fileIdMap.put("fr.unauthorized", "8fa54e9d-84eb-444a-aa9f-d7eb986c68a8");

        // Project page translations
        fileIdMap.put("en.createprojectpage", "32d23b1a-805a-40dc-a311-bfd9b4614339");
        fileIdMap.put("fr.createprojectpage", "5cd86972-f085-4edd-9c87-ad07ec3314f2");
        fileIdMap.put("en.lotselectpage", "0738514f-6b6f-4dd2-a4f9-b57e83a007df");
        fileIdMap.put("fr.lotselectpage", "c60d9ed6-83f4-4c22-9466-8278032e7edd");
        fileIdMap.put("en.projectfilespage", "f6497a49-947f-44c7-8649-7a6d1652a3e9");
        fileIdMap.put("fr.projectfilespage", "6fe17158-0ffa-49ab-acee-28a6cf107e5c");
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
        fileIdMap.put("en.navbar", "c0b77c56-5529-4865-8656-2be6c358f572");
        fileIdMap.put("fr.navbar", "bff0fe86-a8ec-4a71-aa2b-875696b6e0f2");
        // Quotes page translations
        fileIdMap.put("en.quotes", "4992e027-03c7-4592-afcf-b6b1b0d1102f");
        fileIdMap.put("fr.quotes", "2a0d3030-93dd-49b9-a1e0-3bc7990db2ed");

        // Customer Forms page translations
        fileIdMap.put("en.customerforms", "edc60ff8-12f4-4cb5-bd79-f0415ce314b9");
        fileIdMap.put("fr.customerforms", "bb5d5ef7-0ebc-487c-b192-51e873a2aadf");

        // Salesperson Forms page translations
        fileIdMap.put("en.salespersonforms", "bf89b8c0-12d8-43f3-b713-8cf5133a95ac");
        fileIdMap.put("fr.salespersonforms", "0209707e-6022-4e1e-9a0c-4e166bf715e4");

        // Owner Review Forms page translations
        fileIdMap.put("en.ownerreviewforms", "3bea59f8-9011-4258-90b2-25ef587deff6");
        fileIdMap.put("fr.ownerreviewforms", "f6bbaa26-b7ef-4bf9-b28f-637950d8d196");

        // Coming Soon page translations
        fileIdMap.put("en.comingsoon", "04e7fe1f-6cd3-422d-9717-af8efa050033");
        fileIdMap.put("fr.comingsoon", "c0e054ae-c4ce-46db-b712-67aa8998c1d1");

        // Lots List Dashboard page translations
        fileIdMap.put("en.lotslistdashboard", "29afb49d-0d39-4ce4-a91c-d31e0b72fd92");
        fileIdMap.put("fr.lotslistdashboard", "dec0ecbd-6da2-4d41-a099-f5c98842809a");

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
