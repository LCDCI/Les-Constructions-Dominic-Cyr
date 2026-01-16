package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.DeepLService;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.FileServiceUploader;
import com.ecp.les_constructions_dominic_cyr.backend.utils.translation.businesslayer.TranslationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/pdf-translation")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PDFTranslationController {

    private final DeepLService deepLService;
    private final TranslationCacheService cacheService;
    private final FileServiceUploader fileServiceUploader;

    /**
     * Translates a PDF document from one language to another.
     * Automatically detects source language and translates to the opposite (EN <-> FR).
     * 
     * @param file the PDF file to translate
     * @param sourceLanguage optional source language (EN or FR). If not provided, will be auto-detected.
     * @param targetLanguage optional target language (EN or FR). If not provided, will be opposite of source.
     * @return Mono containing the translated PDF file
     */
    @PostMapping(value = "/translate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<Flux<DataBuffer>>> translatePdf(
            @RequestPart("file") Mono<FilePart> file,
            @RequestParam(value = "sourceLanguage", required = false) String sourceLanguage,
            @RequestParam(value = "targetLanguage", required = false) String targetLanguage) {
        
        return file.flatMap(filePart -> {
            // Validate file type
            String filename = filePart.filename();
            String contentType = filePart.headers().getFirst(HttpHeaders.CONTENT_TYPE);
            boolean isValidPdf = (contentType != null && contentType.contains("pdf")) 
                    || (filename != null && filename.toLowerCase().endsWith(".pdf"));
            
            if (!isValidPdf) {
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Flux.empty()));
            }

            // Read file content
            return DataBufferUtils.join(filePart.content())
                    .flatMap(dataBuffer -> {
                        byte[] pdfBytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(pdfBytes);
                        DataBufferUtils.release(dataBuffer);

                        // Determine source and target languages (normalize to lowercase for cache)
                        String sourceLangInput = (sourceLanguage != null && !sourceLanguage.isEmpty())
                                ? sourceLanguage.toLowerCase()
                                : null;
                        
                        String targetLangInput;
                        if (targetLanguage != null && !targetLanguage.isEmpty()) {
                            targetLangInput = targetLanguage.toLowerCase();
                        } else if (sourceLangInput != null) {
                            // If source is specified but target is not, translate to opposite
                            targetLangInput = "en".equals(sourceLangInput) ? "fr" : "en";
                        } else {
                            // Default: translate to French if source is unknown
                            targetLangInput = "fr";
                        }

                        // Check cache first
                        String originalFilename = filename != null ? filename : "document.pdf";
                        byte[] cachedPdf = cacheService.getCachedTranslation(originalFilename, targetLangInput);
                        
                        if (cachedPdf != null) {
                            System.out.println("Returning cached translation for: " + originalFilename + " -> " + targetLangInput);
                            String cacheFilename = cacheService.generateCacheFilename(originalFilename, targetLangInput);
                            DataBuffer buffer = org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance
                                    .wrap(cachedPdf);
                            
                            return Mono.just(ResponseEntity.ok()
                                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                                            "attachment; filename=\"" + cacheFilename + "\"")
                                    .contentType(MediaType.APPLICATION_PDF)
                                    .body(Flux.just(buffer)));
                        }

                        // Not in cache, translate via DeepL (convert to uppercase for DeepL API)
                        String sourceLang = sourceLangInput != null 
                                ? deepLService.toDeepLLanguageCode(sourceLangInput) 
                                : null;
                        String targetLang = deepLService.toDeepLLanguageCode(targetLangInput);

                        // Translate PDF
                        return deepLService.translatePdf(pdfBytes, sourceLang, targetLang)
                                .flatMap(translatedPdf -> {
                                    // Generate cache filename for the response
                                    String cacheFilename = cacheService.generateCacheFilename(originalFilename, targetLangInput);
                                    
                                    // Upload to file service
                                    return fileServiceUploader.uploadTranslatedPdf(translatedPdf, cacheFilename, "system")
                                            .map(fileId -> {
                                                System.out.println("Translated PDF uploaded to file service with ID: " + fileId);
                                                
                                                DataBuffer buffer = org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance
                                                        .wrap(translatedPdf);
                                                
                                                return ResponseEntity.ok()
                                                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                                                "attachment; filename=\"" + cacheFilename + "\"")
                                                        .contentType(MediaType.APPLICATION_PDF)
                                                        .body(Flux.just(buffer));
                                            });
                                })
                                .onErrorResume(error -> {
                                    System.err.println("Error translating PDF or uploading to file service: " + error.getMessage());
                                    error.printStackTrace();
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body(Flux.empty()));
                                });
                    });
        });
    }


    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        boolean isConfigured = deepLService.isApiKeyConfigured();
        String apiUrl = deepLService.getApiUrl();
        
        String message;
        if (isConfigured) {
            message = String.format(
                "PDF Translation Service is running and ready.\n" +
                "API Key: Configured ✓\n" +
                "API URL: %s\n" +
                "Status: Ready to translate PDFs",
                apiUrl
            );
        } else {
            message = "PDF Translation Service is running.\n" +
                     "API Key: Not configured ✗\n" +
                     "Please set deepl.api.key in application.properties to enable translation.";
        }
        
        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(message));
    }
}

