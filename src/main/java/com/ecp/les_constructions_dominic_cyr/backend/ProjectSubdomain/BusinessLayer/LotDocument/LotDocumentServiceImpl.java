package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocument;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocumentRepository;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LotDocumentServiceImpl implements LotDocumentService {

    private final LotDocumentRepository lotDocumentRepository;
    private final LotRepository lotRepository;
    private final UsersRepository usersRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${files.service.base-url}")
    private String filesServiceBaseUrl;

    private static final List<String> IMAGE_MIME_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final List<String> ALLOWED_ROLES_UPLOAD = List.of("OWNER", "CONTRACTOR");

    @Override
    public List<LotDocumentResponseModel> getLotDocuments(String lotId, String search, String type) {
        log.debug("Getting documents for lot: {}, search: {}, type: {}", lotId, search, type);

        validateLotExists(lotId);
        UUID lotUuid = UUID.fromString(lotId);

        List<LotDocument> documents;

        if (search != null && !search.isBlank()) {
            documents = lotDocumentRepository.searchByLotIdAndFileName(lotUuid, search);
        } else if ("image".equalsIgnoreCase(type)) {
            documents = lotDocumentRepository.findByLotIdAndType(lotUuid, true);
        } else if ("file".equalsIgnoreCase(type)) {
            documents = lotDocumentRepository.findByLotIdAndType(lotUuid, false);
        } else {
            documents = lotDocumentRepository.findByLotId(lotUuid);
        }

        return documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<LotDocumentResponseModel> uploadDocuments(String lotId, MultipartFile[] files, String uploaderUserId) {
        log.info("Uploading {} documents to lot: {} by user: {}", files.length, lotId, uploaderUserId);

        // Validate inputs
        if (files == null || files.length == 0) {
            throw new InvalidInputException("No files provided for upload");
        }

        // Validate lot exists
        Lot lot = validateLotExists(lotId);

        // Validate uploader exists
        UUID uploaderUUID;
        try {
            uploaderUUID = UUID.fromString(uploaderUserId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid uploader user ID format: " + uploaderUserId);
        }

        Users uploader = usersRepository.findByUserIdentifier_UserId(uploaderUUID)
                .orElseThrow(() -> new NotFoundException("Uploader user not found: " + uploaderUserId));

        // Check if user has upload permissions (Owner or Contractor)
        String userRole = uploader.getUserRole() != null ? uploader.getUserRole().toString() : "";
        if (!ALLOWED_ROLES_UPLOAD.contains(userRole)) {
            throw new AccessDeniedException("User role '" + userRole + "' is not authorized to upload documents");
        }

        // OWNER users can upload to any lot; CONTRACTOR users must be assigned to this specific lot
        if ("CONTRACTOR".equals(userRole)) {
            boolean isAssignedToLot = lot.getAssignedUsers().stream()
                    .anyMatch(u -> u.getUserIdentifier().getUserId().equals(uploaderUUID));

            if (!isAssignedToLot) {
                throw new AccessDeniedException("Contractor is not assigned to this lot");
            }
        }

        List<LotDocumentResponseModel> uploadedDocuments = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Upload to files-service
                String storageKey = uploadToFilesService(file, lotId, uploaderUserId);

                // Determine if image
                String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
                boolean isImage = IMAGE_MIME_TYPES.contains(mimeType.toLowerCase());

                // Create database record
                LotDocument document = new LotDocument(
                        lot,
                        uploader,
                        uploader.getFirstName() + " " + uploader.getLastName(),
                        file.getOriginalFilename(),
                        storageKey,
                        mimeType,
                        file.getSize(),
                        isImage
                );

                LotDocument saved = lotDocumentRepository.save(document);
                uploadedDocuments.add(mapToResponse(saved));

                log.info("Uploaded document: {} to lot: {}", saved.getId(), lotId);
            } catch (Exception e) {
                log.error("Failed to upload file: {} to lot: {}", file.getOriginalFilename(), lotId, e);
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }

        return uploadedDocuments;
    }

    @Override
    public byte[] downloadDocument(String lotId, UUID documentId, String requestingUserId) {
        log.debug("Downloading document: {} from lot: {} by user: {}", documentId, lotId, requestingUserId);

        UUID lotUuid = UUID.fromString(lotId);
        // Validate document belongs to lot
        if (!lotDocumentRepository.existsByIdAndLotId(documentId, lotUuid)) {
            throw new NotFoundException("Document not found in this lot");
        }

        LotDocument document = lotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        // Validate requester
        UUID requesterUUID;
        try {
            requesterUUID = UUID.fromString(requestingUserId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid requesting user ID format: " + requestingUserId);
        }

        Users requester = usersRepository.findByUserIdentifier_UserId(requesterUUID)
                .orElseThrow(() -> new NotFoundException("Requesting user not found: " + requestingUserId));

        // OWNER users can download from any lot; others must be assigned to this specific lot
        boolean isOwner = requester.getUserRole() != null && "OWNER".equals(requester.getUserRole().toString());
        if (!isOwner) {
            Lot lot = document.getLot();
            boolean isAssignedToLot = lot.getAssignedUsers().stream()
                    .anyMatch(u -> u.getUserIdentifier().getUserId().equals(requesterUUID));

            if (!isAssignedToLot) {
                throw new AccessDeniedException("User is not assigned to this lot");
            }
        }

        // Download from files-service
        return downloadFromFilesService(document.getStorageKey());
    }

    @Override
    @Transactional
    public void deleteDocument(String lotId, UUID documentId, String requestingUserId) {
        log.info("Deleting document: {} from lot: {} by user: {}", documentId, lotId, requestingUserId);

        UUID lotUuid = UUID.fromString(lotId);
        // Validate document belongs to lot
        if (!lotDocumentRepository.existsByIdAndLotId(documentId, lotUuid)) {
            throw new NotFoundException("Document not found in this lot");
        }

        LotDocument document = lotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        // Validate requester
        UUID requesterUUID;
        try {
            requesterUUID = UUID.fromString(requestingUserId);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid requesting user ID format: " + requestingUserId);
        }

        Users requester = usersRepository.findByUserIdentifier_UserId(requesterUUID)
                .orElseThrow(() -> new NotFoundException("Requesting user not found: " + requestingUserId));

        // Check permissions: Owner OR original uploader assigned to lot
        boolean isOwner = requester.getUserRole() != null && "OWNER".equals(requester.getUserRole().toString());
        boolean isUploader = document.getUploader().getUserIdentifier().getUserId().equals(requesterUUID);
        
        // OWNER can delete any document; others must be assigned to lot and either be uploader or owner
        if (!isOwner) {
            Lot lot = document.getLot();
            boolean isAssignedToLot = lot.getAssignedUsers().stream()
                    .anyMatch(u -> u.getUserIdentifier().getUserId().equals(requesterUUID));

            if (!isAssignedToLot) {
                throw new AccessDeniedException("User is not assigned to this lot");
            }

            if (!isUploader) {
                throw new AccessDeniedException("Only the uploader can delete this document");
            }
        }

        // Delete from database (files-service cleanup can be async/scheduled)
        lotDocumentRepository.delete(document);
        log.info("Deleted document: {} from lot: {}", documentId, lotId);
    }

    @Override
    public String getDocumentContentType(String lotId, UUID documentId) {
        UUID lotUuid = UUID.fromString(lotId);
        if (!lotDocumentRepository.existsByIdAndLotId(documentId, lotUuid)) {
            throw new NotFoundException("Document not found in this lot");
        }

        LotDocument document = lotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        return document.getMimeType();
    }

    // ========== PRIVATE HELPER METHODS ==========

    private Lot validateLotExists(String lotId) {
        UUID lotUuid = UUID.fromString(lotId);
        Lot lot = lotRepository.findByLotIdentifier_LotId(lotUuid);
        if (lot == null) {
            throw new NotFoundException("Lot not found: " + lotId);
        }
        return lot;
    }

    private LotDocumentResponseModel mapToResponse(LotDocument document) {
        return LotDocumentResponseModel.builder()
                .id(document.getId())
                .lotId(document.getLot().getLotIdentifier().getLotId().toString())
                .uploaderUserId(document.getUploader().getUserIdentifier().getUserId())
                .uploaderName(document.getUploaderName())
                .fileName(document.getOriginalFileName())
                .mimeType(document.getMimeType())
                .sizeBytes(document.getSizeBytes())
                .isImage(document.getIsImage())
                .uploadedAt(document.getUploadedAt())
                .downloadUrl("/api/v1/lots/" + document.getLot().getLotIdentifier().getLotId() 
                        + "/documents/" + document.getId() + "/download")
                .build();
    }

    /**
     * Builds file upload metadata, ensuring a non-empty content type and deriving the category.
     */
    private FileUploadMetadata buildFileUploadMetadata(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        String category = determineCategory(contentType);
        return new FileUploadMetadata(contentType, category);
    }

    private static final class FileUploadMetadata {
        private final String contentType;
        private final String category;

        private FileUploadMetadata(String contentType, String category) {
            this.contentType = contentType;
            this.category = category;
        }

        private String getContentType() {
            return contentType;
        }

        private String getCategory() {
            return category;
        }
    }

    private String uploadToFilesService(MultipartFile file, String lotId, String uploadedBy) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(filesServiceBaseUrl).build();

            FileUploadMetadata metadata = buildFileUploadMetadata(file);
            String contentType = metadata.getContentType();
            String category = metadata.getCategory();
            
            log.info("Uploading to files-service: file={}, contentType={}, category={}, projectId={}, uploadedBy={}, uploaderRole=OWNER, fileSize={}", 
                    file.getOriginalFilename(), contentType, category, lotId, uploadedBy, file.getSize());

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            
            // Add file part using ByteArrayResource
            byte[] fileBytes = file.getBytes();
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            bodyBuilder.part("file", fileResource)
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.parseMediaType(contentType));
                    
            // Add form fields as strings
            bodyBuilder.part("category", category);
            bodyBuilder.part("projectId", lotId);
            bodyBuilder.part("uploadedBy", uploadedBy);
            bodyBuilder.part("uploaderRole", "OWNER");
            
            String response = webClient.post()
                    .uri("/files")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.debug("Files-service upload response: {}", response);
            
            // Extract file ID or storage key from response
            // Response format: {"fileId": "...", "url": "/files/..."}
            // For simplicity, return the file ID as storage key
            return extractFileIdFromResponse(response);

        } catch (Exception e) {
            log.error("Failed to upload file to files-service", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    private byte[] downloadFromFilesService(String storageKey) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(filesServiceBaseUrl).build();

            return webClient.get()
                    .uri("/files/" + storageKey)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

        } catch (Exception e) {
            log.error("Failed to download file from files-service", e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }

    private String determineCategory(String mimeType) {
        if (mimeType == null) {
            return "OTHER";
        }
        return IMAGE_MIME_TYPES.contains(mimeType.toLowerCase()) ? "PHOTO" : "DOCUMENT";
    }

    private String extractFileIdFromResponse(String jsonResponse) {
        // Simple JSON parsing - in production, use Jackson ObjectMapper
        try {
            if (jsonResponse.contains("\"fileId\"")) {
                int start = jsonResponse.indexOf("\"fileId\"") + 10;
                int end = jsonResponse.indexOf("\"", start);
                return jsonResponse.substring(start, end);
            } else if (jsonResponse.contains("\"id\"")) {
                int start = jsonResponse.indexOf("\"id\"") + 6;
                int end = jsonResponse.indexOf("\"", start);
                return jsonResponse.substring(start, end);
            }
            throw new RuntimeException("Could not extract file ID from response");
        } catch (Exception e) {
            log.error("Failed to parse files-service response: {}", jsonResponse, e);
            throw new RuntimeException("Invalid response from files-service", e);
        }
    }
}
