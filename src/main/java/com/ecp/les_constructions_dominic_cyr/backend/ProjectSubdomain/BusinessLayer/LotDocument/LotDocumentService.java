package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LotDocument.LotDocumentResponseModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface LotDocumentService {

    /**
     * Get all documents for a lot with optional filtering.
     * 
     * @param lotId The lot identifier
     * @param search Optional search query for filename
     * @param type Optional type filter: "image", "file", or "all"
     * @return List of lot documents
     */
    List<LotDocumentResponseModel> getLotDocuments(String lotId, String search, String type);

    /**
     * Upload one or more documents to a lot.
     * Validates that uploader is assigned to the lot and has Owner or Contractor role.
     * 
     * @param lotId The lot identifier
     * @param files The files to upload
     * @param uploaderUserId The uploader's user ID (UUID as string)
     * @return List of created documents
     */
    List<LotDocumentResponseModel> uploadDocuments(String lotId, MultipartFile[] files, String uploaderUserId);

    /**
     * Download a document. Any team member assigned to the lot can download.
     * 
     * @param lotId The lot identifier
     * @param documentId The document ID
     * @param requestingUserId The requesting user's ID
     * @return byte array of file data and content type
     */
    byte[] downloadDocument(String lotId, UUID documentId, String requestingUserId);

    /**
     * Delete a document. Only uploader or Owner assigned to lot can delete.
     * 
     * @param lotId The lot identifier
     * @param documentId The document ID
     * @param requestingUserId The requesting user's ID
     */
    void deleteDocument(String lotId, UUID documentId, String requestingUserId);

    /**
     * Get content type for download response header.
     */
    String getDocumentContentType(String lotId, UUID documentId);
}
