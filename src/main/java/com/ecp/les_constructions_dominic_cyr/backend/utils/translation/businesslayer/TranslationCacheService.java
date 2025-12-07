package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for caching translated PDF files to avoid redundant DeepL API calls.
 * Saves translated files to resources/translations folder.
 */
@Service
public class TranslationCacheService {

    private static final String CACHE_DIR = "src/main/resources/translations";
    private static final String CACHE_DIR_ALT = "translations";

    /**
     * Checks that a provided string is a safe file path component (no separators or parent references).
     * Throws IllegalArgumentException if unsafe.
     */
    private void validatePathComponent(String input, String name) {
        if (input == null) return;
        // Disallow path separators, parent directory references, and leading/trailing dots or consecutive dots
        // Only allow alphanumeric, underscore, dash, and at most one dot (for file extension)
        if (!input.matches("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)?$")) {
            throw new IllegalArgumentException("Invalid " + name + ": must be alphanumeric, may contain one dot (for extension), underscores, or dashes, but no path separators, consecutive dots, or leading/trailing dots.");
        }
        if (input.contains("/") || input.contains("\\") ) {
            throw new IllegalArgumentException("Invalid " + name + ": contains path separator.");
        }
    }

    /**
     * Generates a cache filename based on original filename and target language.
     * Removes existing language suffix (_fr or _en) and adds target language suffix.
     * 
     * @param originalFilename the original filename (e.g., "car.pdf" or "car_fr.pdf")
     * @param targetLanguage the target language code (e.g., "fr" or "en")
     * @return the cache filename (e.g., "car_fr.pdf" or "car_en.pdf")
     */
    public String generateCacheFilename(String originalFilename, String targetLanguage) {
        validatePathComponent(targetLanguage, "targetLanguage");
        if (originalFilename != null && !originalFilename.isEmpty()) {
            validatePathComponent(originalFilename, "originalFilename");
        }
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "translated_" + targetLanguage.toLowerCase() + ".pdf";
        }

        // Normalize target language to lowercase
        String targetLang = targetLanguage.toLowerCase();
        
        // Extract base name and extension
        int lastDotIndex = originalFilename.lastIndexOf('.');
        String baseName = lastDotIndex > 0 
            ? originalFilename.substring(0, lastDotIndex) 
            : originalFilename;
        String extension = lastDotIndex > 0 
            ? originalFilename.substring(lastDotIndex) 
            : ".pdf";

        // Remove existing language suffix (_fr or _en)
        if (baseName.endsWith("_fr")) {
            baseName = baseName.substring(0, baseName.length() - 3);
        } else if (baseName.endsWith("_en")) {
            baseName = baseName.substring(0, baseName.length() - 3);
        }

        // Add target language suffix
        return baseName + "_" + targetLang + extension;
    }

    /**
     * Gets the cache file path for a given filename and target language.
     * 
     * @param filename the cache filename
     * @return the full path to the cache file
     */
    public Path getCacheFilePath(String filename) {
        // Try resources/translations first
        Path cacheDir = Paths.get(CACHE_DIR);
        if (!Files.exists(cacheDir)) {
            // Try alternative path (for runtime)
            cacheDir = Paths.get(CACHE_DIR_ALT);
        }
        
        // Create directory if it doesn't exist
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            System.err.println("Failed to create cache directory: " + e.getMessage());
        }
        
        return cacheDir.resolve(filename);
    }

    /**
     * Checks if a cached translation exists for the given filename and target language.
     * 
     * @param originalFilename the original filename
     * @param targetLanguage the target language
     * @return true if cached file exists, false otherwise
     */
    public boolean isCached(String originalFilename, String targetLanguage) {
        String cacheFilename = generateCacheFilename(originalFilename, targetLanguage);
        Path cachePath = getCacheFilePath(cacheFilename);
        return Files.exists(cachePath) && Files.isRegularFile(cachePath);
    }

    /**
     * Retrieves a cached translated PDF file.
     * 
     * @param originalFilename the original filename
     * @param targetLanguage the target language
     * @return the cached PDF file as byte array, or null if not found
     */
    public byte[] getCachedTranslation(String originalFilename, String targetLanguage) {
        if (!isCached(originalFilename, targetLanguage)) {
            return null;
        }

        try {
            String cacheFilename = generateCacheFilename(originalFilename, targetLanguage);
            Path cachePath = getCacheFilePath(cacheFilename);
            return Files.readAllBytes(cachePath);
        } catch (IOException e) {
            System.err.println("Error reading cached translation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a translated PDF to the cache.
     * 
     * @param originalFilename the original filename
     * @param targetLanguage the target language
     * @param translatedPdf the translated PDF as byte array
     * @return true if saved successfully, false otherwise
     */
    public boolean saveTranslation(String originalFilename, String targetLanguage, byte[] translatedPdf) {
        try {
            String cacheFilename = generateCacheFilename(originalFilename, targetLanguage);
            Path cachePath = getCacheFilePath(cacheFilename);
            Files.write(cachePath, translatedPdf);
            System.out.println("Saved translated PDF to cache: " + cachePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving translation to cache: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the cache directory path.
     * 
     * @return the cache directory path
     */
    public Path getCacheDirectory() {
        Path cacheDir = Paths.get(CACHE_DIR);
        if (!Files.exists(cacheDir)) {
            cacheDir = Paths.get(CACHE_DIR_ALT);
        }
        return cacheDir;
    }
}

